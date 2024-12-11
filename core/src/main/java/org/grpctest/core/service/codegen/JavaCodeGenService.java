package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.freemarker.ClientDataModel;
import org.grpctest.core.pojo.freemarker.JavaServerDataModel;
import org.grpctest.core.pojo.freemarker.ServiceImplDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Service
@Qualifier("javaCodeGenService")
@Slf4j
public class JavaCodeGenService extends BaseCodeGenService {

    private static final String JAVA_SVC_DIR = "./java/java-server/src/main/java/org/grpctest/java/server/generated/service/";
    private static final String JAVA_CLIENT_FILE = "./java/java-client/src/main/java/org/grpctest/java/client/generated/JavaClient.java";
    private static final String JAVA_SERVER_FILE = "./java/java-server/src/main/java/org/grpctest/java/server/generated/JavaServer.java";
    private static final String JAVA_FILE_EXT = ".java";

    @Autowired
    public JavaCodeGenService(Configuration freemarkerConfig, Config config, Registry registry) {
        super(freemarkerConfig, config, registry);
    }

    public void generateJavaService(RpcService service) throws Exception {
        ServiceImplDataModel dataModel = new ServiceImplDataModel(service, registry, config.getTestsDir());
        // Get Java service template
        Template template = freemarkerConfig.getTemplate("java-service-impl.ftl");
        String filepath = JAVA_SVC_DIR + dataModel.getService().getName() + JAVA_FILE_EXT;
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) {
            template.process(dataModel, bufferedWriter);
        }
    }

    public void generateJavaClient() throws Exception {
        ClientDataModel dataModel = new ClientDataModel(registry, config.getTestsDir());
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

    @Override
    public void generateServer() throws Exception {

    }

    @Override
    public void generateClient() throws Exception {

    }
}
