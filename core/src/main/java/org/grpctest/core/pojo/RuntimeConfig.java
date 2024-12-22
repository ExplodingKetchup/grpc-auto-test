package org.grpctest.core.pojo;

import lombok.*;
import org.grpctest.core.enums.MetadataType;

import java.util.HashMap;

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
    private Boolean enableAllRandomTestcase = false;

    @Getter
    public enum Language {
        JAVA("Java"),
        NODEJS("Node.JS");

        public static final HashMap<Language, String> SERVER_NAME = new HashMap<>();
        public static final HashMap<Language, String> CLIENT_NAME = new HashMap<>();
        static {
            SERVER_NAME.put(JAVA, "java-server");
            SERVER_NAME.put(NODEJS, "node-server");

            CLIENT_NAME.put(JAVA, "java-client");
            CLIENT_NAME.put(NODEJS, "node-client");
        }

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }
    }
}
