package org.grpctest.core.pojo;

import lombok.*;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.MetadataType;

import java.util.ArrayList;
import java.util.List;

/** Configs that are better suited to be set at runtime (esp. if they vary between each run) */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuntimeConfig {

    private Language server;

    private Language client;

    @Builder.Default
    private MetadataType serverToClientMetadataType = MetadataType.NONE;

    @Builder.Default
    private MetadataType clientToServerMetadataType = MetadataType.NONE;

    @Builder.Default
    private Boolean enableException = false;

    @Builder.Default
    private Boolean enableGeneratedTestcase = false;

    @Builder.Default
    private int omitFieldsInRandomTestcases = 0;

    @Builder.Default
    private int valueMode = 0;

    @Builder.Default
    private List<String> includedProtos = new ArrayList<>();

    @Builder.Default
    private List<String> includedCustomTestcases = new ArrayList<>();

    @Builder.Default
    private Boolean generateFilesOnly = false;

    @Builder.Default
    private String requestCompression = "";

    @Builder.Default
    private String responseCompression = "";

}
