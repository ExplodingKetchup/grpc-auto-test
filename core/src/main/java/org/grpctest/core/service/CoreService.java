package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.service.codegen.JavaCodeGenService;
import org.grpctest.core.service.codegen.NodejsCodeGenService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CoreService implements InitializingBean {

    private final FileCopier fileCopier;

    private final MavenInvoker mavenInvoker;

    private final ProtobufReader protobufReader;

    private final JavaCodeGenService javaCodeGenService;

    private final NodejsCodeGenService nodejsCodeGenService;

    private final CustomTestCaseReader customTestCaseReader;    // Although not used, we need the reader to run its init code before core service

    private final TestCaseGenerator testCaseGenerator;
    
    private final TestCaseWriter testCaseWriter;

    private final RpcModelRegistry rpcModelRegistry;

    private final TestcaseRegistry testcaseRegistry;
    
    private final DockerService dockerService;
    
    private final ResultAnalyzer resultAnalyzer;

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
//            // Copy predefined files to destination
//            fileCopier.copyProtos();
//            log.info("[Step 1 of 9] Finished copy predefined files for Java");
//
//            // Compile .proto files for Java
//            mavenInvoker.buildCommon();
//            log.info("[Step 2 of 9] Finished compiling .proto file for Java");
//
            // Read content of .proto files
            protobufReader.loadProtoContent();
            log.info("[Step 3 of 9] Finished reading content of .proto files");

            // Load test cases
            customTestCaseReader.loadTestCasesToRegistry();

            // Generate random test cases for services without custom test cases
            generateRandomTestcases();
            log.info("[Step 4 of 9] Finished loading test cases");

//            // Write all test cases to binary file
            testCaseWriter.writeAllTestCases();
//            log.info("[Step 5 of 9] Finished writing test cases to file");
//
//            // Generate Java server
//            for (RpcService service : registry.getAllServices()) {
//                // Generate source codes
//                javaCodeGenService.generateJavaService(service);
//            }
//            javaCodeGenService.generateJavaServer();
//            log.info("[Step 6 of 9] Finished generating Java server");
//
            // Generate Java client
//            javaCodeGenService.generateServer();
//            log.info("[Step 7 of 9] Finished generating Java client");
//
            // Generate Nodejs
//            nodejsCodeGenService.generateNodeClient();
//            // Build Docker containers and Docker compose project
//            mavenInvoker.buildClientServer();
//            dockerService.dockerComposeUp();
//            log.info("[Step 8 of 9] Finished building and launched test containers");
            
//            // Analyze result
//            resultAnalyzer.processAllMethods();
//            log.info("[Step 9 of 9] Finished analyzing results");

            log.info("Finished testing");
        } catch (Throwable t) {
            log.error("An error occurred, terminating test", t);
        }
    }

    private void generateRandomTestcases() {
        for (RpcService.RpcMethod method : testcaseRegistry.getAllMethodsWithoutTestCases()) {
            DynamicMessage paramDynMsg = testCaseGenerator.generateRandomMessage(rpcModelRegistry.lookupMessage(method.getInType()));
            DynamicMessage returnDynMsg = testCaseGenerator.generateRandomMessage(rpcModelRegistry.lookupMessage(method.getOutType()));
            TestCase testCase = new TestCase(method.getId() + "_random",
                    method.getId(),
                    null,
                    null,
                    paramDynMsg,
                    null,
                    null,
                    returnDynMsg
            );
            log.info("[generateRandomTestcases] Added test case {}", testCase);
            testcaseRegistry.addTestCase(testCase);
        }
    }
}
