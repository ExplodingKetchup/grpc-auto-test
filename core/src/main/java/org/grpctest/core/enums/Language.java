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

    public static String getServerName(Language language) {
        return SERVER_NAME.get(language);
    }

    public static String getClientName(Language language) {
        return CLIENT_NAME.get(language);
    }

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }
}
