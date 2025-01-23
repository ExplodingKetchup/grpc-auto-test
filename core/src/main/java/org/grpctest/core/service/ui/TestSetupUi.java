package org.grpctest.core.service.ui;

import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;

import java.util.List;

public interface TestSetupUi {
    default RuntimeConfig setupEverything() throws Exception {
        printHelloMessage();
        return RuntimeConfig.builder()
                .server(chooseServer())
                .client(chooseClient())
                .serverToClientMetadataType(chooseServerToClientMetadataType())
                .serverToClientMetadataType(chooseClientToServerMetadataType())
                .enableException(chooseEnableException())
                .enableGeneratedTestcase(chooseEnableGeneratedTestcase())
                .includedProtos(chooseIncludedRpcFiles())
                .omitFieldsInRandomTestcases(chooseOmitFieldsInRandomTestcases())
                .valueMode(chooseValueMode())
                .includedCustomTestcases(chooseIncludedCustomTestcases())
                .generateFilesOnly(chooseGenerateFilesOnly())
                .requestCompression(chooseRequestCompression())
                .responseCompression(chooseResponseCompression())
                .build();
    }

    void printHelloMessage();

    RuntimeConfig.Language chooseServer() throws Exception;

    RuntimeConfig.Language chooseClient() throws Exception;

    MetadataType chooseServerToClientMetadataType() throws Exception;

    MetadataType chooseClientToServerMetadataType() throws Exception;

    boolean chooseEnableException() throws Exception;

    boolean chooseEnableGeneratedTestcase() throws Exception;

    List<String> chooseIncludedRpcFiles() throws Exception;

    int chooseOmitFieldsInRandomTestcases() throws Exception;

    int chooseValueMode() throws Exception;

    List<String> chooseIncludedCustomTestcases() throws Exception;

    boolean chooseGenerateFilesOnly() throws Exception;

    String chooseRequestCompression() throws Exception;

    String chooseResponseCompression() throws Exception;
}
