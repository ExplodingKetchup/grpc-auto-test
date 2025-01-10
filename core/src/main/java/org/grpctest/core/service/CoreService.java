package org.grpctest.core.service;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.enums.CleanupMode;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.RuntimeConfig;
import org.grpctest.core.service.codegen.JavaCodeGenService;
import org.grpctest.core.service.codegen.NodejsCodeGenService;
import org.grpctest.core.service.codegen.PythonCodeGenService;
import org.grpctest.core.service.ui.TestSetupUi;
import org.grpctest.core.util.FileUtil;
import org.grpctest.core.util.TimeUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CoreService implements InitializingBean {

    private static final long STARTUP_TIMESTAMP = System.currentTimeMillis();

    private final Config config;

    private final GenericFileService genericFileService;

    private final MavenInvoker mavenInvoker;

    private final ProtobufReader protobufReader;

    @Qualifier("script")
    private final TestSetupUi testSetupUi;

    private final JavaCodeGenService javaCodeGenService;

    private final NodejsCodeGenService nodejsCodeGenService;

    private final PythonCodeGenService pythonCodeGenService;

    private final CustomTestCaseReader customTestCaseReader;

    private final TestCaseGenerator testCaseGenerator;
    
    private final TestCaseWriter testCaseWriter;

    private final RpcModelRegistry rpcModelRegistry;

    private final TestcaseRegistry testcaseRegistry;
    
    private final DockerService dockerService;
    
    private final ResultAnalyzer resultAnalyzer;

    private RuntimeConfig runtimeConfig;

    public CoreService(Config config,
                       GenericFileService genericFileService,
                       MavenInvoker mavenInvoker,
                       ProtobufReader protobufReader,
                       @Qualifier("script") TestSetupUi testSetupUi,
                       JavaCodeGenService javaCodeGenService,
                       NodejsCodeGenService nodejsCodeGenService,
                       PythonCodeGenService pythonCodeGenService,
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
        this.pythonCodeGenService = pythonCodeGenService;
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
            runtimeConfig = testSetupUi.setupEverything();
            log.info("runtimeConfig: {}", runtimeConfig);

            // Copy predefined .proto files to destination
            genericFileService.copyProtos(runtimeConfig);
            log.info("[Step 1 of 9] Finished copy predefined files for Java");

            // Compile .proto files for Java
            mavenInvoker.buildCommon();
            log.info("[Step 2 of 9] Finished compiling .proto file for Java");

            // Read content of .proto files
            protobufReader.loadProtoContent(runtimeConfig);
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
            log.info("Preparations completed. Launching server...");

            // Build and launch server
            switch (runtimeConfig.getServer()) {
                case JAVA -> buildAndLaunchJavaServer();
                case NODEJS -> buildAndLaunchNodejsServer();
                case PYTHON -> buildAndLaunchPythonServer();
            }

            // Wait for server to start properly
            TimeUtil.pollForCondition(() -> {
                try {
                    return checkServerRunning(runtimeConfig.getServer());
                } catch (IOException ioe) {
                    return false;
                }
            }, 1000, config.getServerStartupTimeoutMillis());
            log.info("Server started, will start launching client...");

            // Build and launch client
            switch (runtimeConfig.getClient()) {
                case JAVA -> buildAndLaunchJavaClient();
                case NODEJS -> buildAndLaunchNodejsClient();
                case PYTHON -> buildAndLaunchPythonClient();
            }

            // Wait until all tests are finished
            TimeUtil.pollForCondition(() -> checkTestsFinished(runtimeConfig), 1000, config.getTestTimeoutMillis());
            log.info("Client shut down. Concluding tests...");
            Thread.sleep(5000);     // Wait for stuff to finish before shutting down

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
        dockerService.dockerComposeUpSpecifyServices(true, "java-server");
    }

    private void buildAndLaunchJavaClient() throws Throwable {
        javaCodeGenService.generateClient();
        mavenInvoker.buildClient();
        dockerService.dockerComposeUpSpecifyServices(false, "java-client");
    }

    private void buildAndLaunchNodejsServer() throws Exception {
        nodejsCodeGenService.generateServer();
        dockerService.dockerComposeUpSpecifyServices(true, "node-server");
    }

    private void buildAndLaunchNodejsClient() throws Exception {
        nodejsCodeGenService.generateClient();
        dockerService.dockerComposeUpSpecifyServices(false, "node-client");
    }

    private void buildAndLaunchPythonServer() throws Exception {
        pythonCodeGenService.generateServer();
        dockerService.dockerComposeUpSpecifyServices(true, "py-server");
    }

    private void buildAndLaunchPythonClient() throws Exception {
        pythonCodeGenService.generateClient();
        dockerService.dockerComposeUpSpecifyServices(false, "py-client");
    }

    private void generateRandomTestcases(RuntimeConfig runtimeConfig) {
        for (RpcService.RpcMethod method : testcaseRegistry.getAllMethodsWithoutTestCases()) {
            testcaseRegistry.addTestCase(testCaseGenerator.generateRandomTestcase(method, runtimeConfig.getOmitFieldsInRandomTestcases(), runtimeConfig.getEnableException()));
        }
    }

    private void setServerInConfig(RuntimeConfig runtimeConfig) {
        String serverName = RuntimeConfig.Language.SERVER_NAME.get(runtimeConfig.getServer());
        switch (runtimeConfig.getClient()) {
            case JAVA -> config.setJavaClientServerHost(serverName);
            case NODEJS -> config.setNodejsClientServerHost(serverName);
            case PYTHON -> config.setPyClientServerHost(serverName);
        }
    }

    private void addMetadata(RuntimeConfig runtimeConfig) {
        rpcModelRegistry.addClientToServerMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getClientToServerMetadataType()));
        rpcModelRegistry.addServerToClientMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getServerToClientMetadataType()));
    }

    private boolean checkServerRunning(RuntimeConfig.Language serverLanguage) throws IOException {
        return searchLogContentInThisRun(
                getLatestLogFile(true, serverLanguage),
                "Server started",
                serverLanguage
        );
    }

    /**
     * Check if client has finished its work, by 2 criteria:<br>
     * 1. Expected output files are present in the output folder of both client and server<br>
     * 2. Client program is not active (using {@link DockerService#healthCheck(String[])})
     */
    private boolean checkClientShutdown(RuntimeConfig.Language clientLanguage) throws Exception {
        for (String file : resultAnalyzer.getExpectedClientOutputFiles()) {
            if (Files.notExists(Paths.get(file))) {
                return false;
            }
        }
        for (String file : resultAnalyzer.getExpectedServerOutputFiles()) {
            if (Files.notExists(Paths.get(file))) {
                return false;
            }
        }
        return !dockerService.healthCheck(new String[]{RuntimeConfig.Language.CLIENT_NAME.get(clientLanguage)});
    }

    @Deprecated
    /** This way of checking for client shutdown is not reliable, because sometimes logs are not flushed before shutdown */
    private boolean checkClientShutdownByLog(RuntimeConfig.Language clientLanguage) throws IOException {
        boolean result = searchLogContentInThisRun(
                getLatestLogFile(false, clientLanguage),
                "Client shutting down",
                clientLanguage
        );
        // Java only, because for some reason the log line at shutdown hook sometimes doesn't appear
        if (clientLanguage.equals(RuntimeConfig.Language.JAVA)) {
            if (searchLogContentInThisRun(
                    getLatestLogFile(false, clientLanguage),
                    "[main] o.grpctest.java.client.Application - Started Application in",
                    clientLanguage
            )) {
                return true;
            }
        }
        return result;
    }

    private String getLatestLogFile(boolean isServer, RuntimeConfig.Language language) throws IOException {
        String currentLogFile = "";
        String service;
        if (isServer) {
            service = "server";
        } else {
            service = "client";
        }
        switch (language) {
            case JAVA -> currentLogFile = "log/java-" + service + ".log";
            case NODEJS -> currentLogFile = "log/node-" + service + "." + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            case PYTHON -> {
                List<String> pythonLogFiles = FileUtil.listFilesWithSamePrefix("log", "py-" + service);
                for (String pyLog : pythonLogFiles) {
                    if (Long.parseLong(pyLog.substring(pyLog.lastIndexOf("_") + 1, pyLog.length() - 4)) * 1000 > STARTUP_TIMESTAMP) {
                        currentLogFile = pyLog;
                        break;
                    }
                }
            }
        }
        return currentLogFile;
    }

    /**
     * Check if there's a line containing a {@code searchString} message logged after the start of CoreService startup.
     * Note: For this use case, we only search the last 5 lines of the log file.
     * The {@code language} parameter dictates the format we will follow when reading the log file.
     *
     * @param logFile
     * @param searchString
     * @param language
     * @return
     */
    private boolean searchLogContentInThisRun(String logFile, String searchString, RuntimeConfig.Language language) {
        final int linesToRead = 5;
        List<String> log = FileUtil.tail(logFile, linesToRead);
        if (Objects.nonNull(log)) {
            for (String line : log) {
                // Check if line contains a "Client shutting down..." message logged after the start of CoreService startup
                long logTime = 0;
                try {
                    switch (language) {
                        case JAVA, PYTHON ->
                                logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-dd HH:mm:ss".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC) * 1000;
                        case NODEJS ->
                                logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-ddTHH:mm:ss.SSSX".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")).toEpochSecond(ZoneOffset.UTC) * 1000;
                    }
                } catch (Exception e) {
                }
                if (logTime > STARTUP_TIMESTAMP) {
                    if (line.contains(searchString)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkTestsFinished(RuntimeConfig runtimeConfig) {
        try {
            return checkClientShutdown(runtimeConfig.getClient());
        } catch (Exception e) {
            log.error("[checkTestsFinished] Exception occurred", e);
            return false;
        }
    }

    private void finalize(boolean analyzeResult) throws Throwable {
        // Shut down and clean test containers
        dockerService.dockerComposeDown();

        // Analyze result
        if (analyzeResult && Objects.nonNull(this.runtimeConfig)) {
            resultAnalyzer.processAllMethods(this.runtimeConfig);
            log.info("[Step 9 of 9] Finished analyzing results");
        }

        // Clean up after test
        if ((config.getCleanupMode().equals(CleanupMode.AFTER)) ||
                (config.getCleanupMode().equals(CleanupMode.BEFORE_AND_AFTER))) {
            genericFileService.cleanup();
        }
    }
}
