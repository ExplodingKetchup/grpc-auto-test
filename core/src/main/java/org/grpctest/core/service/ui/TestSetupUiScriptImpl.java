package org.grpctest.core.service.ui;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("script")
@Slf4j
public class TestSetupUiScriptImpl implements TestSetupUi {

    private final SetupScriptInterpreter interpreter;
    private final Config config;

    private RuntimeConfig runtimeConfig;
    
    private boolean setupCompleted = false;

    public TestSetupUiScriptImpl(SetupScriptInterpreter interpreter, Config config) {
        this.interpreter = interpreter;
        this.config = config;
    }

    @Override
    public RuntimeConfig setupEverything() throws IOException {
        printHelloMessage();
        runtimeConfig = interpreter.interpretScript(config.getSetupScriptFilePath());
        setupCompleted = true;
        return runtimeConfig;
    }

    @Override
    public void printHelloMessage() {
        log.info("Using script setup, reading script at {}", config.getSetupScriptFilePath());
    }

    @Override
    public Language chooseServer() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getServer();
    }

    @Override
    public Language chooseClient() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getClient();
    }

    @Override
    public MetadataType chooseServerToClientMetadataType() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getServerToClientMetadataType();
    }

    @Override
    public MetadataType chooseClientToServerMetadataType() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getClientToServerMetadataType();
    }

    @Override
    public boolean chooseEnableException() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getEnableException();
    }

    @Override
    public boolean chooseEnableGeneratedTestcase() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getEnableGeneratedTestcase();
    }

    @Override
    public List<String> chooseIncludedRpcFiles() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getIncludedProtos();
    }

    @Override
    public int chooseOmitFieldsInRandomTestcases() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getOmitFieldsInRandomTestcases();
    }

    @Override
    public int chooseValueMode() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getValueMode();
    }

    @Override
    public List<String> chooseIncludedCustomTestcases() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getIncludedCustomTestcases();
    }

    @Override
    public boolean chooseGenerateFilesOnly() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getGenerateFilesOnly();
    }

    @Override
    public String chooseRequestCompression() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getRequestCompression();
    }

    @Override
    public String chooseResponseCompression() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getResponseCompression();
    }

    @Override
    public Map<String, Integer> chooseSupport() throws IOException {
        if (!setupCompleted) setupEverything();
        return runtimeConfig.getSupport();
    }


}
