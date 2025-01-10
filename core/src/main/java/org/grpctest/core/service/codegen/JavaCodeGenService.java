package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.freemarker.datamodels.ConfigDataModel;
import org.grpctest.core.freemarker.datamodels.ServerDataModel;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.freemarker.datamodels.ClientDataModel;
import org.grpctest.core.freemarker.datamodels.ServiceImplDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Qualifier("javaCodeGenService")
@Slf4j
public class JavaCodeGenService extends BaseCodeGenService {

    private static final String JAVA_SVC_FTL = "java-service-impl.ftl";
    private static final String JAVA_SVC_DIR = "./java/java-server/src/main/java/org/grpctest/java/server/generated/service/";
    private static final String JAVA_CLIENT_FTL = "java-client.ftl";
    private static final String JAVA_CLIENT_FILE = "./java/java-client/src/main/java/org/grpctest/java/client/generated/JavaClient.java";
    private static final String JAVA_CLIENT_CONFIG_FTL = "java-client-config.ftl";
    private static final String JAVA_CLIENT_CONFIG_FILE = "./java/java-client/src/main/resources/application.properties";
    private static final String JAVA_CLIENT_INTERCEPTOR_FTL = "java-client-interceptor.ftl";
    private static final String JAVA_CLIENT_INTERCEPTOR_FILE = "./java/java-client/src/main/java/org/grpctest/java/client/generated/interceptor/MetadataInterceptor.java";
    private static final String JAVA_SERVER_FTL = "java-server.ftl";
    private static final String JAVA_SERVER_FILE = "./java/java-server/src/main/java/org/grpctest/java/server/generated/JavaServer.java";
    private static final String JAVA_SERVER_CONFIG_FTL = "java-server-config.ftl";
    private static final String JAVA_SERVER_CONFIG_FILE = "./java/java-server/src/main/resources/application.properties";
    private static final String JAVA_SERVER_INTERCEPTOR_FTL = "java-server-interceptor.ftl";
    private static final String JAVA_SERVER_INTERCEPTOR_FILE = "./java/java-server/src/main/java/org/grpctest/java/server/generated/interceptor/MetadataInterceptor.java";
    private static final String JAVA_FILE_EXT = ".java";

    @Autowired
    public JavaCodeGenService(Configuration freemarkerConfig, Config config, RpcModelRegistry registry, TestcaseRegistry testcaseRegistry) {
        super(freemarkerConfig, config, registry, testcaseRegistry);
    }

    public void generateJavaService(RpcService service) throws Exception {
        generateFileFromFtl(
                JAVA_SVC_FTL,
                new ServiceImplDataModel(service.getId(), registry, testcaseRegistry),
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
                new ServerDataModel(registry, testcaseRegistry),
                JAVA_SERVER_FILE
        );
    }

    public void generateJavaClientConfig() throws Exception {
        generateFileFromFtl(
                JAVA_CLIENT_CONFIG_FTL,
                new ConfigDataModel(config),
                JAVA_CLIENT_CONFIG_FILE
        );
    }

    public void generateJavaServerConfig() throws Exception {
        generateFileFromFtl(
                JAVA_SERVER_CONFIG_FTL,
                new ConfigDataModel(config),
                JAVA_SERVER_CONFIG_FILE
        );
    }

    public void generateJavaServerInterceptor() throws Exception {
        generateFileFromFtl(
                JAVA_SERVER_INTERCEPTOR_FTL,
                new ServerDataModel(registry, testcaseRegistry),
                JAVA_SERVER_INTERCEPTOR_FILE
        );
    }

    public void generateJavaClientInterceptor() throws Exception {
        generateFileFromFtl(
                JAVA_CLIENT_INTERCEPTOR_FTL,
                new ClientDataModel(registry),
                JAVA_CLIENT_INTERCEPTOR_FILE
        );
    }

    @Override
    public void generateServer() throws Exception {
        FileUtils.cleanDirectory(new File(JAVA_SVC_DIR));
        generateJavaServer();
        for (RpcService service : registry.getAllServices()) {
            generateJavaService(service);
        }
        generateJavaServerInterceptor();
        generateJavaServerConfig();
    }

    @Override
    public void generateClient() throws Exception {
        generateJavaClient();
        generateJavaClientInterceptor();
        generateJavaClientConfig();
    }
}
