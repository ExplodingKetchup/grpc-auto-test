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
        JAVA("Java"),
        NODEJS("Node.JS");

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }
    }
}
