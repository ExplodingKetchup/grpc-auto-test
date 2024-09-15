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

    private final MavenInvoker mavenInvoker;

    private final ProtobufReader protobufReader;

    private final JavaCodeGenService javaCodeGenService;

    private final TestCaseReader testCaseReader;    // Although not used, we need the reader to run its init code before core service

    private final RpcTestRegistry registry;

    private Map<String, ServiceImplDataModel> dataModels;

    @Override
    public void afterPropertiesSet() throws Exception {

        // Compile .proto files for Java
//        mavenInvoker
//                .addMvnGoal(MavenInvoker.MavenGoal.CLEAN)
//                .addMvnGoal(MavenInvoker.MavenGoal.INSTALL)
//                .addParam("DskipTests", "")
//                .execute();
//        log.info("Finished compiling .proto file for Java");

        // Read content of .proto files
        ProtoContent protoContent = protobufReader.loadProtoContent();

        testCaseReader.loadTestCasesToRegistry();

        for (RpcService service : registry.getAllServices()) {
            // Generate source codes
            javaCodeGenService.generateJavaService(service);
        }

        javaCodeGenService.generateJavaClient();

        log.info("Finished setting up test");
    }
}
