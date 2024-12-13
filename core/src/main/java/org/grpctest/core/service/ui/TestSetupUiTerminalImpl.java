package org.grpctest.core.service.ui;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.pojo.TestConfig;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TestSetupUiTerminalImpl implements TestSetupUi {
    @Override
    public void printHelloMessage() {
        System.out.println("Welcome to gRPC Testing tool");
        System.out.println("This tool tests gRPC remote procedural calls between server and client programs written in different languages.");
        System.out.println("-----------------------------------------------");
    }

    @Override
    public TestConfig.Language chooseServer() throws Exception {
        System.out.println("Please select a server:");
        return null;
    }

    @Override
    public TestConfig.Language chooseClient() throws Exception {
        return null;
    }
}
