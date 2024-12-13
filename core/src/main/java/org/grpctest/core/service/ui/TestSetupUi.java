package org.grpctest.core.service.ui;

import org.grpctest.core.pojo.TestConfig;

public interface TestSetupUi {
    default TestConfig setupEverything() throws Exception {
        printHelloMessage();
        TestConfig.Language serverLanguage = chooseServer();
        TestConfig.Language clientLanguage = chooseClient();
        return new TestConfig(serverLanguage, clientLanguage);
    }

    void printHelloMessage();

    TestConfig.Language chooseServer() throws Exception;

    TestConfig.Language chooseClient() throws Exception;
}
