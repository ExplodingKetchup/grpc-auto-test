package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.freemarker.ClientDataModel;
import org.grpctest.core.pojo.freemarker.ConfigDataModel;
import org.grpctest.core.pojo.freemarker.ServerDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("NodejsCodeGenService")
@Slf4j
public class NodejsCodeGenService extends BaseCodeGenService {

    private static final String NODEJS_CLIENT_FTL = "node-client.ftl";
    private static final String NODEJS_SERVER_FTL = "node-server.ftl";
    private static final String NODEJS_CLIENT_CONFIG_FTL = "node-client-config.ftl";
    private static final String NODEJS_SERVER_CONFIG_FTL = "node-server-config.ftl";
    private static final String NODEJS_CLIENT_FILE = "./nodejs/client.js";
    private static final String NODEJS_SERVER_FILE = "./nodejs/server.js";
    private static final String NODEJS_CLIENT_CONFIG_FILE = "./nodejs/config/client/deploy.js";
    private static final String NODEJS_SERVER_CONFIG_FILE = "./nodejs/config/server/deploy.js";

    @Autowired
    public NodejsCodeGenService(Configuration freemarkerConfig, Config config, Registry registry) {
        super(freemarkerConfig, config, registry);
    }

    public void generateNodeClient() throws Exception {
        generateFileFromFtl(
                NODEJS_CLIENT_FTL,
                new ClientDataModel(registry, config.getTestsDir()),
                NODEJS_CLIENT_FILE
        );
    }

    public void generateNodeServer() throws Exception {
        generateFileFromFtl(
                NODEJS_SERVER_FTL,
                new ServerDataModel(registry),
                NODEJS_SERVER_FILE
        );
    }

    public void generateNodeClientConfig() throws Exception {
        generateFileFromFtl(
                NODEJS_CLIENT_CONFIG_FTL,
                new ConfigDataModel(config),
                NODEJS_CLIENT_CONFIG_FILE
        );
    }

    public void generateNodeServerConfig() throws Exception {
        generateFileFromFtl(
                NODEJS_SERVER_CONFIG_FTL,
                new ConfigDataModel(config),
                NODEJS_SERVER_CONFIG_FILE
        );
    }

    @Override
    public void generateServer() throws Exception {
        generateNodeServer();
    }

    @Override
    public void generateClient() throws Exception {
        generateNodeClient();
    }
}
