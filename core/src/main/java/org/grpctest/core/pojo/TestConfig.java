package org.grpctest.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TestConfig {

    private Language server;

    private Language client;

    @Getter
    public enum Language {
        JAVA("JAVA", "Java"),
        NODEJS("NODE", "Node.JS");

        private final String id;
        private final String displayName;

        Language(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }
}
