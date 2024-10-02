package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.RpcTestRegistry;
import org.grpctest.core.pojo.freemarker.ServiceImplDataModel;
import org.grpctest.core.pojo.ProtoContent;
import org.grpctest.core.pojo.RpcService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class CoreService implements InitializingBean {

    private final FileCopier fileCopier;

    private final MavenInvoker mavenInvoker;

    private final ProtobufReader protobufReader;

    private final JavaCodeGenService javaCodeGenService;

    private final TestCaseReader testCaseReader;    // Although not used, we need the reader to run its init code before core service

    private final RpcTestRegistry registry;
    
    private  final DockerService dockerService;

    @Override
    public void afterPropertiesSet() throws Exception {

        // Copy predefined files to destination
        fileCopier.copyProtos();
        log.info("[Step 1 of 7] Finished copy predefined files for Java");

        // Compile .proto files for Java
        mavenInvoker.defaultBuild();
        log.info("[Step 2 of 7] Finished compiling .proto file for Java");

        // Read content of .proto files
        ProtoContent protoContent = protobufReader.loadProtoContent();
        log.info("[Step 3 of 7] Finished reading content of .proto files");

        // Load tests case
        testCaseReader.loadTestCasesToRegistry();
        log.info("[Step 4 of 7] Finished loading test cases");

        // Generate Java server
        for (RpcService service : registry.getAllServices()) {
            // Generate source codes
            javaCodeGenService.generateJavaService(service);
        }
        javaCodeGenService.generateJavaServer();
        log.info("[Step 5 of 7] Finished generating Java server");

        // Generate Java client
        javaCodeGenService.generateJavaClient();
        log.info("[Step 6 of 7] Finished generating Java client");
        
        // Build Docker containers and Docker compose project
        dockerService.dockerComposeUp();
        log.info("[Step 7 of 7] Finished building and launched test containers");

        log.info("Finished setting up test");
    }
}
