package org.grpctest.core.service;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.enums.CleanupMode;
import org.grpctest.core.enums.Language;
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

    private final TestProgramsManager testProgramsManager;
    
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
                       TestProgramsManager testProgramsManager,
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
        this.testProgramsManager = testProgramsManager;
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
            if (!runtimeConfig.getIncludedCustomTestcases().isEmpty()) {
                customTestCaseReader.loadTestCasesToRegistry(runtimeConfig.getIncludedCustomTestcases());
            }
//
//            Descriptors.Descriptor descriptor = rpcModelRegistry.lookupMessage("single_hotpot.BigHotpotOfTerror").getMessageDescriptor();
//            DynamicMessage msg1 = DynamicMessage.newBuilder(descriptor).setField(descriptor.findFieldByName("message_value"), null).build();
//            DynamicMessage msg2 = testCaseGenerator.generateMessage(rpcModelRegistry.lookupMessage("single_hotpot.SmallHotpotOfRickeridoo"), 0, 0);

            // Generate test cases
            if (runtimeConfig.getEnableGeneratedTestcase()) {
                generateTestcases(runtimeConfig);
            }
            log.info("[Step 4 of 9] Finished loading test cases");

            // Remove methods with 0 testcase from registry
            List<String> methodIdsToRemove = testcaseRegistry.getAllMethodsWithoutTestCases().stream().map(RpcService.RpcMethod::getId).toList();
            for (String methodIdToRemove : methodIdsToRemove) {
                rpcModelRegistry.removeMethod(methodIdToRemove);
                testcaseRegistry.deleteEntry(methodIdToRemove);
                log.warn("No testcase found for method {}. Method removed", methodIdToRemove);
            }

            // Also remove services without methods
            List<String> serviceIdsToRemove = rpcModelRegistry.getAllServicesWithoutMethod().stream().map(RpcService::getId).toList();
            for (String serviceIdToRemove : serviceIdsToRemove) {
                rpcModelRegistry.removeService(serviceIdToRemove);
                log.warn("Service {} contains no method with testcase. Service removed", serviceIdToRemove);
            }

            // Sanity check: if no method is available for testing, just end test
            if (rpcModelRegistry.getAllMethods().isEmpty()) {
                finalize(false, false);
            }

            // Write all test cases to binary file
            testCaseWriter.writeAllTestCases();
            log.info("[Step 5 of 9] Finished writing test cases to file");

            setServerInConfig(runtimeConfig);
            addCompressionSettings(runtimeConfig);
            addMetadata(runtimeConfig);
            log.info("Preparations completed. Launching server...");

            // Codegen server
            switch (runtimeConfig.getServer()) {
                case JAVA -> javaCodeGenService.generateServer();
                case NODEJS -> nodejsCodeGenService.generateServer();
                case PYTHON -> pythonCodeGenService.generateServer();
            }

            // Codegen client
            switch (runtimeConfig.getClient()) {
                case JAVA -> javaCodeGenService.generateClient();
                case NODEJS -> nodejsCodeGenService.generateClient();
                case PYTHON -> pythonCodeGenService.generateClient();
            }

            if (!runtimeConfig.getGenerateFilesOnly()) {
                testProgramsManager.resetDeployment();
                testProgramsManager.addClient(runtimeConfig.getClient());
                testProgramsManager.addServer(runtimeConfig.getServer());
                testProgramsManager.deploy();
                Thread.sleep(5000);     // Wait for stuff to finish before shutting down
            }

        } catch (Throwable t) {
            log.error("An error occurred, terminating test", t);
            hasError = true;
        } finally {
            finalize(hasError, !hasError);
        }
    }

    private void generateTestcases(RuntimeConfig runtimeConfig) {
        for (RpcService.RpcMethod method : testcaseRegistry.getAllMethodsWithoutTestCases()) {
            testcaseRegistry.addTestCase(testCaseGenerator.generateTestcase(method, runtimeConfig.getOmitFieldsInRandomTestcases(), runtimeConfig.getValueMode(), runtimeConfig.getEnableException()));
        }
    }

    private void setServerInConfig(RuntimeConfig runtimeConfig) {
        String serverName = Language.SERVER_NAME.get(runtimeConfig.getServer());
        switch (runtimeConfig.getClient()) {
            case JAVA -> config.setJavaClientServerHost(serverName);
            case NODEJS -> config.setNodejsClientServerHost(serverName);
            case PYTHON -> config.setPyClientServerHost(serverName);
        }
    }

    private void addCompressionSettings(RuntimeConfig runtimeConfig) {
        rpcModelRegistry.setRequestCompression(runtimeConfig.getRequestCompression());
        rpcModelRegistry.setResponseCompression(runtimeConfig.getResponseCompression());
    }

    private void addMetadata(RuntimeConfig runtimeConfig) {
        rpcModelRegistry.addClientToServerMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getClientToServerMetadataType()));
        rpcModelRegistry.addServerToClientMetadata(testCaseGenerator.generateRandomMetadata(runtimeConfig.getServerToClientMetadataType()));
    }

    private void finalize(boolean hasError, boolean analyzeResult) {
        try {
            // Clean up and Analyze results if there was no error last run
            log.info("Tests are finished. Did we encounter error? {}", hasError);

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
            log.info("Finished testing");

            System.exit(0);
        } catch (Throwable e) {
            log.error("Cleanup failed", e);
            throw new RuntimeException(e);
        }
    }
}
