package org.grpctest.core.service.ui;

import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;

public interface TestSetupUi {
    default RuntimeConfig setupEverything() throws Exception {
        printHelloMessage();
        return RuntimeConfig.builder()
                .server(chooseServer())
                .client(chooseClient())
                .serverToClientMetadataType(chooseServerToClientMetadataType())
                .serverToClientMetadataType(chooseClientToServerMetadataType())
                .enableException(chooseEnableException())
                .enableAllRandomTestcase(chooseEnableAllRandomTestcase())
                .build();
    }

    void printHelloMessage();

    RuntimeConfig.Language chooseServer() throws Exception;

    RuntimeConfig.Language chooseClient() throws Exception;

    MetadataType chooseServerToClientMetadataType() throws Exception;

    MetadataType chooseClientToServerMetadataType() throws Exception;

    boolean chooseEnableException() throws Exception;

    boolean chooseEnableAllRandomTestcase() throws Exception;
}
