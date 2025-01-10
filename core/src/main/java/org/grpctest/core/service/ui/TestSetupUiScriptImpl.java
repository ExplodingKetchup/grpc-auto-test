package org.grpctest.core.service.ui;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("script")
@Slf4j
public class TestSetupUiScriptImpl implements TestSetupUi {

    private final SetupScriptInterpreter interpreter;
    private final Config config;

    private RuntimeConfig runtimeConfig;

    public TestSetupUiScriptImpl(SetupScriptInterpreter interpreter, Config config) {
        this.interpreter = interpreter;
        this.config = config;
    }

    @Override
    public RuntimeConfig setupEverything() throws Exception {
        printHelloMessage();
        runtimeConfig = interpreter.interpretScript(config.getSetupScriptFilePath());
        return runtimeConfig;
    }

    @Override
    public void printHelloMessage() {
        log.info("Using script setup, reading script at {}", config.getSetupScriptFilePath());
    }

    @Override
    public RuntimeConfig.Language chooseServer() throws Exception {
        return runtimeConfig.getServer();
    }

    @Override
    public RuntimeConfig.Language chooseClient() throws Exception {
        return runtimeConfig.getClient();
    }

    @Override
    public MetadataType chooseServerToClientMetadataType() throws Exception {
        return runtimeConfig.getServerToClientMetadataType();
    }

    @Override
    public MetadataType chooseClientToServerMetadataType() throws Exception {
        return runtimeConfig.getClientToServerMetadataType();
    }

    @Override
    public boolean chooseEnableException() throws Exception {
        return runtimeConfig.getEnableException();
    }

    @Override
    public boolean chooseEnableAllRandomTestcase() throws Exception {
        return runtimeConfig.getEnableAllRandomTestcase();
    }

    @Override
    public List<String> chooseIncludedRpcFiles() throws Exception {
        return runtimeConfig.getIncludedProtos();
    }

    @Override
    public int chooseOmitFieldsInRandomTestcases() throws Exception {
        return runtimeConfig.getOmitFieldsInRandomTestcases();
    }


}
