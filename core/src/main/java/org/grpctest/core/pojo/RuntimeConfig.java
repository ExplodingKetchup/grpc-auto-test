package org.grpctest.core.pojo;

import lombok.*;
import org.grpctest.core.enums.MetadataType;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Getter
    public enum Language {
        JAVA("Java"),
        NODEJS("Node.JS"),
        PYTHON("Python");

        public static final HashMap<Language, String> SERVER_NAME = new HashMap<>();
        public static final HashMap<Language, String> CLIENT_NAME = new HashMap<>();
        static {
            SERVER_NAME.put(JAVA, "java-server");
            SERVER_NAME.put(NODEJS, "node-server");
            SERVER_NAME.put(PYTHON, "py-server");

            CLIENT_NAME.put(JAVA, "java-client");
            CLIENT_NAME.put(NODEJS, "node-client");
            CLIENT_NAME.put(PYTHON, "py-client");
        }

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }
    }
}
