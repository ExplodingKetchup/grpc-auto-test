package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.freemarker.datamodels.ClientDataModel;
import org.grpctest.core.freemarker.datamodels.ConfigDataModel;
import org.grpctest.core.freemarker.datamodels.ServerDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("python")
@Slf4j
public class PythonCodeGenService extends BaseCodeGenService {

    private static final String PYTHON_CLIENT_FTL = "py-client.ftl";
    private static final String PYTHON_CLIENT_CONFIG_FTL = "py-client-config.ftl";
    private static final String PYTHON_SERVER_FTL = "py-server.ftl";
    private static final String PYTHON_SERVER_CONFIG_FTL = "py-server-config.ftl";
    private static final String PYTHON_CLIENT_FILE = "python/client.py";
    private static final String PYTHON_CLIENT_CONFIG_FILE = "python/config/client/deploy.yaml";
    private static final String PYTHON_SERVER_FILE = "python/server.py";
    private static final String PYTHON_SERVER_CONFIG_FILE = "python/config/server/deploy.yaml";

    @Autowired
    public PythonCodeGenService(Configuration freemarkerConfig, Config config, RpcModelRegistry registry, TestcaseRegistry testcaseRegistry) {
        super(freemarkerConfig, config, registry, testcaseRegistry);
    }

    public void generatePyClient() throws Exception {
        generateFileFromFtl(
                PYTHON_CLIENT_FTL,
                new ClientDataModel(
                        registry,
                        config.isClientLogRequests(),
                        config.isClientLogRequestsPrintFields(),
                        config.isClientLogResponses(),
                        config.isClientLogResponsesPrintFields()
                ),
                PYTHON_CLIENT_FILE
        );
    }

    public void generatePyClientConfig() throws Exception {
        generateFileFromFtl(
                PYTHON_CLIENT_CONFIG_FTL,
                new ConfigDataModel(config),
                PYTHON_CLIENT_CONFIG_FILE
        );
    }

    public void generatePyServer() throws Exception {
        generateFileFromFtl(
                PYTHON_SERVER_FTL,
                new ServerDataModel(
                        registry,
                        testcaseRegistry,
                        config.isServerLogRequests(),
                        config.isServerLogRequestsPrintFields(),
                        config.isServerLogResponses(),
                        config.isServerLogResponsesPrintFields()
                ),
                PYTHON_SERVER_FILE
        );
    }

    public void generatePyServerConfig() throws Exception {
        generateFileFromFtl(
                PYTHON_SERVER_CONFIG_FTL,
                new ConfigDataModel(config),
                PYTHON_SERVER_CONFIG_FILE
        );
    }

    @Override
    public void generateServer() throws Exception {
        generatePyServer();
        generatePyServerConfig();
    }

    @Override
    public void generateClient() throws Exception {
        generatePyClient();
        generatePyClientConfig();
    }
}
