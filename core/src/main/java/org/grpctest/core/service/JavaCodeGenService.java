package org.grpctest.core.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.RpcTestRegistry;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.freemarker.ClientDataModel;
import org.grpctest.core.pojo.freemarker.JavaServerDataModel;
import org.grpctest.core.pojo.freemarker.ServiceImplDataModel;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class JavaCodeGenService {

    private static final String JAVA_SVC_DIR = "./java/java-server/src/main/java/org/grpctest/java/server/generated/service/";
    private static final String JAVA_CLIENT_FILE = "./java/java-client/src/main/java/org/grpctest/java/client/generated/JavaClient.java";
    private static final String JAVA_SERVER_FILE = "./java/java-server/src/main/java/org/grpctest/java/server/generated/JavaServer.java";
    private static final String JAVA_FILE_EXT = ".java";

    private Configuration freemarkerConfig;

    private final RpcTestRegistry registry;

    public void generateJavaService(RpcService service) throws Exception {
        ServiceImplDataModel dataModel = new ServiceImplDataModel(service, registry);
        // Get Java service template
        Template template = freemarkerConfig.getTemplate("java-service-impl.ftl");
        String filepath = JAVA_SVC_DIR + dataModel.getService().getName() + JAVA_FILE_EXT;
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) {
            template.process(dataModel, bufferedWriter);
        }
    }

    public void generateJavaClient() throws Exception {
        ClientDataModel dataModel = new ClientDataModel(registry);
        // Get JavaClient template
        Template template = freemarkerConfig.getTemplate("java-client.ftl");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(JAVA_CLIENT_FILE))) {
            template.process(dataModel, bufferedWriter);
        }
    }

    public void generateJavaServer() throws Exception {
        JavaServerDataModel dataModel = new JavaServerDataModel(registry.getAllServices());
        // Get JavaServer template
        Template template = freemarkerConfig.getTemplate("java-server.ftl");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(JAVA_SERVER_FILE))) {
            template.process(dataModel, bufferedWriter);
        }
    }
}
