package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.enums.CleanupMode;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.RuntimeConfig;
import org.grpctest.core.service.codegen.JavaCodeGenService;
import org.grpctest.core.service.codegen.NodejsCodeGenService;
import org.grpctest.core.service.ui.TestSetupUi;
import org.grpctest.core.util.TimeUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class CoreService implements InitializingBean {

    private final long startupTimestamp = System.currentTimeMillis();

    private final Config config;

    private final GenericFileService genericFileService;

    private final MavenInvoker mavenInvoker;

    private final ProtobufReader protobufReader;

    @Qualifier("script")
    private final TestSetupUi testSetupUi;

    private final JavaCodeGenService javaCodeGenService;

    private final NodejsCodeGenService nodejsCodeGenService;

    private final CustomTestCaseReader customTestCaseReader;

    private final TestCaseGenerator testCaseGenerator;
    
    private final TestCaseWriter testCaseWriter;

    private final RpcModelRegistry rpcModelRegistry;

    private final TestcaseRegistry testcaseRegistry;
    
    private final DockerService dockerService;
    
    private final ResultAnalyzer resultAnalyzer;

    public CoreService(Config config,
                       GenericFileService genericFileService,
                       MavenInvoker mavenInvoker,
                       ProtobufReader protobufReader,
                       @Qualifier("script") TestSetupUi testSetupUi,
                       JavaCodeGenService javaCodeGenService,
                       NodejsCodeGenService nodejsCodeGenService,
                       CustomTestCaseReader customTestCaseReader,
                       TestCaseGenerator testCaseGenerator,
                       TestCaseWriter testCaseWriter,
                       RpcModelRegistry rpcModelRegistry,
                       TestcaseRegistry testcaseRegistry,
                       DockerService dockerService,
                       ResultAnalyzer resultAnalyzer) {
        this.config = config;
        this.genericFileService = genericFileService;
        this.mavenInvoker = mavenInvoker;
        this.protobufReader = protobufReader;
        this.testSetupUi = testSetupUi;
        this.javaCodeGenService = javaCodeGenService;
        this.nodejsCodeGenService = nodejsCodeGenService;
        this.customTestCaseReader = customTestCaseReader;
        this.testCaseGenerator = testCaseGenerator;
        this.testCaseWriter = testCaseWriter;
        this.rpcModelRegistry = rpcModelRegistry;
        this.testcaseRegistry = testcaseRegistry;
        this.dockerService = dockerService;
        this.resultAnalyzer = resultAnalyzer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        boolean hasError = false;

        try {
            // Clean up before test
            if ((config.getCleanupMode().equals(CleanupMode.BEFORE)) ||
                    (config.getCleanupMode().equals(CleanupMode.BEFORE_AND_AFTER))) {
                genericFileService.cleanup();
            }

            // Read user's runtimeConfig
            RuntimeConfig runtimeConfig = testSetupUi.setupEverything();
            log.info("runtimeConfig: {}", runtimeConfig);

            // Copy predefined .proto files to destination
            genericFileService.copyProtos();
            log.info("[Step 1 of 9] Finished copy predefined files for Java");

            // Compile .proto files for Java
            mavenInvoker.buildCommon();
            log.info("[Step 2 of 9] Finished compiling .proto file for Java");

            // Read content of .proto files
            protobufReader.loadProtoContent();
            log.info("[Step 3 of 9] Finished reading content of .proto files");

            // Load test cases
            if (!runtimeConfig.getEnableAllRandomTestcase()) {
                customTestCaseReader.loadTestCasesToRegistry();
            }

            // Generate random test cases for services without custom test cases
            generateRandomTestcases(runtimeConfig);
            log.info("[Step 4 of 9] Finished loading test cases");

            // Write all test cases to binary file
            testCaseWriter.writeAllTestCases();
            log.info("[Step 5 of 9] Finished writing test cases to file");

            setServerInConfig(runtimeConfig);
            addMetadata(runtimeConfig);

            // Build and launch server
            switch (runtimeConfig.getServer()) {
                case JAVA -> buildAndLaunchJavaServer();
                case NODEJS -> buildAndLaunchNodejsServer();
            }

            // Wait for server to start properly
            TimeUtil.pollForCondition(() -> checkServerRunning(runtimeConfig.getServer()), 1000, config.getServerStartupTimeoutMillis());
            log.info("Server started, will start launching client...");

            // Build and launch client
            switch (runtimeConfig.getClient()) {
                case JAVA -> buildAndLaunchJavaClient();
                case NODEJS -> buildAndLaunchNodejsClient();
            }

            // Wait until all tests are finished
            TimeUtil.pollForCondition(() -> checkTestsFinished(runtimeConfig), 1000, config.getTestTimeoutMillis());

        } catch (Throwable t) {
            log.error("An error occurred, terminating test", t);
            hasError = true;
        } finally {
            try {
                // Clean up and Analyze results if there was no error last run
                log.info("Tests are finished. Did we encounter error? {}", hasError);
                finalize(!hasError);
                log.info("Finished testing");
            } catch (Throwable e) {
                log.error("Cleanup failed", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void buildAndLaunchJavaServer() throws Throwable {
        javaCodeGenService.generateServer();
        mavenInvoker.buildServer();
        dockerService.dockerComposeUpSpecifyServices("java-server");
    }

    private void buildAndLaunchJavaClient() throws Throwable {
        javaCodeGenService.generateClient();
        mavenInvoker.buildClient();
        dockerService.dockerComposeUpSpecifyServices("java-client");
    }

    private void buildAndLaunchNodejsServer() throws Exception {
        nodejsCodeGenService.generateServer();
        dockerService.dockerComposeUpSpecifyServices("node-server");
    }

    private void buildAndLaunchNodejsClient() throws Exception {
        nodejsCodeGenService.generateClient();
        dockerService.dockerComposeUpSpecifyServices("node-client");
    }

    private void generateRandomTestcases(RuntimeConfig runtimeConfig) {
        for (RpcService.RpcMethod method : testcaseRegistry.getAllMethodsWithoutTestCases()) {
            testcaseRegistry.addTestCase(testCaseGenerator.generateRandomTestcase(method, runtimeConfig.getEnableException()));
        }
    }

    private void setServerInConfig(RuntimeConfig runtimeConfig) {
        String serverName = RuntimeConfig.Language.SERVER_NAME.get(runtimeConfig.getServer());
        switch (runtimeConfig.getClient()) {
            case JAVA -> config.setJavaClientServerHost(serverName);
            case NODEJS -> config.setNodejsClientServerHost(serverName);
        }
    }

    private void addMetadata(RuntimeConfig runtimeConfig) {
        rpcModelRegistry.addClientToServerMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getClientToServerMetadataType()));
        rpcModelRegistry.addServerToClientMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getServerToClientMetadataType()));
    }

    private boolean checkServerRunning(RuntimeConfig.Language serverLanguage) {
        final int linesToRead = 5;
        String currentLogFile = "";
        switch (serverLanguage) {
            case JAVA -> currentLogFile = "log/java-server.log";
            case NODEJS -> currentLogFile = "log/node-server." + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
        }
        List<String> log = genericFileService.tail(currentLogFile, linesToRead);
        for (String line : log) {
            // Check if line contains a "Server started..." message logged after the start of CoreService startup
            long logTime = 0;
            switch (serverLanguage) {
                case JAVA -> logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-dd HH:mm:ss".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC) * 1000;
                case NODEJS -> logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-ddTHH:mm:ss.SSSX".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")).toEpochSecond(ZoneOffset.UTC) * 1000;
            }
            if (logTime > startupTimestamp) {
                if (line.contains("Server started")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkTestsFinished(RuntimeConfig runtimeConfig) {
        Path clientDir = Paths.get("out", "client");
        Path serverDir = Paths.get("out", "server");
        long fileCountClient;
        long fileCountServer;
        long expectedFileCountClient = runtimeConfig.getEnableException() ? 1 : rpcModelRegistry.getAllMethods().size();
        long expectedFileCountServer = rpcModelRegistry.getAllMethods().size();
        if (runtimeConfig.getServerToClientMetadataType() != MetadataType.NONE) expectedFileCountClient++;
        if (runtimeConfig.getClientToServerMetadataType() != MetadataType.NONE) expectedFileCountServer++;
        try {
            try (Stream<Path> pathStream = Files.list(clientDir)) {
                fileCountClient = pathStream.filter(Files::isRegularFile).count();
            }
            try (Stream<Path> pathStream = Files.list(serverDir)) {
                fileCountServer = pathStream.filter(Files::isRegularFile).count();
            }
            return (fileCountClient == expectedFileCountClient)
                    && (fileCountServer == expectedFileCountServer);
        } catch (IOException ioe) {
            log.error("[checkTestsFinished] File I/O Exception", ioe);
            return false;
        }
    }

    private void finalize(boolean analyzeResult) throws Throwable {
        // Shut down and clean test containers
        dockerService.dockerComposeDown();

        // Analyze result
        if (analyzeResult) {
            resultAnalyzer.processAllMethods();
            log.info("[Step 9 of 9] Finished analyzing results");
        }

        // Clean up after test
        if ((config.getCleanupMode().equals(CleanupMode.AFTER)) ||
                (config.getCleanupMode().equals(CleanupMode.BEFORE_AND_AFTER))) {
            genericFileService.cleanup();
        }
    }
}
