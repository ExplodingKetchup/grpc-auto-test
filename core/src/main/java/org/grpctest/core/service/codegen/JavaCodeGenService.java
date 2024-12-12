package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.freemarker.datamodels.ServerDataModel;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.freemarker.datamodels.ClientDataModel;
import org.grpctest.core.freemarker.datamodels.ServiceImplDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("javaCodeGenService")
@Slf4j
public class JavaCodeGenService extends BaseCodeGenService {

    private static final String JAVA_SVC_FTL = "java-service-impl.ftl";
    private static final String JAVA_SVC_DIR = "./java/java-server/src/main/java/org/grpctest/java/server/generated/service/";
    private static final String JAVA_CLIENT_FTL = "java-client.ftl";
    private static final String JAVA_CLIENT_FILE = "./java/java-client/src/main/java/org/grpctest/java/client/generated/JavaClient.java";
    private static final String JAVA_SERVER_FTL = "java-server.ftl";
    private static final String JAVA_SERVER_FILE = "./java/java-server/src/main/java/org/grpctest/java/server/generated/JavaServer.java";
    private static final String JAVA_FILE_EXT = ".java";

    @Autowired
    public JavaCodeGenService(Configuration freemarkerConfig, Config config, RpcModelRegistry registry) {
        super(freemarkerConfig, config, registry);
    }

    public void generateJavaService(RpcService service) throws Exception {
        generateFileFromFtl(
                JAVA_SVC_FTL,
                new ServiceImplDataModel(service.getId(), registry),
                JAVA_SVC_DIR + service.getName() + JAVA_FILE_EXT
        );
    }

    public void generateJavaClient() throws Exception {
        generateFileFromFtl(
                JAVA_CLIENT_FTL,
                new ClientDataModel(registry),
                JAVA_CLIENT_FILE
        );
    }

    public void generateJavaServer() throws Exception {
        generateFileFromFtl(
                JAVA_SERVER_FTL,
                new ServerDataModel(registry),
                JAVA_SERVER_FILE
        );
    }

    public void generateJavaClientConfig() throws Exception {

    }

    @Override
    public void generateServer() throws Exception {
        generateJavaServer();
        for (RpcService service : registry.getAllServices()) {
            generateJavaService(service);
        }
    }

    @Override
    public void generateClient() throws Exception {
        generateJavaClient();
    }
}
