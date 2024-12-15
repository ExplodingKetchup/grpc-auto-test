package org.grpctest.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestConfig {

    private Language server;

    private Language client;

    @Getter
    public enum Language {
        JAVA("Java"),
        NODEJS("Node.JS");

        public static final HashMap<Language, String> SERVER_NAME = new HashMap<>();
        static {
            SERVER_NAME.put(JAVA, "java-server");
            SERVER_NAME.put(NODEJS, "node-server");
        }

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }
    }
}
