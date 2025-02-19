package org.grpctest.core.service.ui;

import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TestSetupUi {
    default RuntimeConfig setupEverything() throws IOException {
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

    Language chooseServer() throws IOException;

    Language chooseClient() throws IOException;

    MetadataType chooseServerToClientMetadataType() throws IOException;

    MetadataType chooseClientToServerMetadataType() throws IOException;

    boolean chooseEnableException() throws IOException;

    boolean chooseEnableGeneratedTestcase() throws IOException;

    List<String> chooseIncludedRpcFiles() throws IOException;

    int chooseOmitFieldsInRandomTestcases() throws IOException;

    int chooseValueMode() throws IOException;

    List<String> chooseIncludedCustomTestcases() throws IOException;

    boolean chooseGenerateFilesOnly() throws IOException;

    String chooseRequestCompression() throws IOException;

    String chooseResponseCompression() throws IOException;
    
    Map<String, Integer> chooseSupport() throws IOException;
}
