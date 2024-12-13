package org.grpctest.core.service.ui;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.pojo.TestConfig;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestSetupUiScriptImpl implements TestSetupUi {

    private final SetupScriptInterpreter interpreter;
    private final Config config;

    private TestConfig testConfig;

    public TestSetupUiScriptImpl(SetupScriptInterpreter interpreter, Config config) {
        this.interpreter = interpreter;
        this.config = config;
    }

    @Override
    public TestConfig setupEverything() throws Exception {
        printHelloMessage();
        testConfig = interpreter.interpretScript(config.getSetupScriptFilePath());
        return testConfig;
    }

    @Override
    public void printHelloMessage() {
        log.info("Using script setup, reading script at {}", config.getSetupScriptFilePath());
    }

    @Override
    public TestConfig.Language chooseServer() throws Exception {
        return testConfig.getServer();
    }

    @Override
    public TestConfig.Language chooseClient() throws Exception {
        return testConfig.getClient();
    }


}
