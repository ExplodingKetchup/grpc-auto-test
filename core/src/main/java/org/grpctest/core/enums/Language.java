package org.grpctest.core.enums;

import lombok.Getter;
import org.grpctest.core.pojo.RuntimeConfig;

import java.util.HashMap;

@Getter
public enum Language {
    JAVA("Java"),
    NODEJS("Node.JS"),
    PYTHON("Python"),
    NOT_APPLICABLE("N/A");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }
}
