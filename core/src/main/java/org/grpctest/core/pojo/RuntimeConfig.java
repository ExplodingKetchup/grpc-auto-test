package org.grpctest.core.pojo;

import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.MetadataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Specify (if testcase generation is enabled) whether to ignore generating values for certain fields
     * (0 = no omit; 1 = partial omit; 2 = full omit)
     */
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

    @Builder.Default
    private Map<String, Integer> support = new HashMap<>();

}
