package org.grpctest.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtil {

    public static String snakeCaseToCamelCase(String snakeCase, boolean capitalizeFirstLetter) {
        String camelCase = Arrays.stream(snakeCase.trim().split("_"))
                .map(str -> StringUtils.capitalize(str.substring(0, 1)) + str.substring(1))
                .collect(Collectors.joining());
        if (!capitalizeFirstLetter) {
            camelCase = StringUtils.uncapitalize(camelCase.substring(0, 1)) + camelCase.substring(1);
        }
        return camelCase;
    }

    public static String camelCaseToSnakeCase(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (char ch : camelCase.toCharArray()) {
            if ((ch >= 'A') && (ch <= 'Z')) {
                if (!sb.isEmpty()) {
                    sb.append('_');
                }
                sb.append((char)(ch + ('a' - 'A')));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** This check is NOT thorough, only checks if any letter in word is captalized */
    public static boolean isSnakeCase(String str) {
        for (char ch : str.toCharArray()) {
            if ((ch >= 'A') && (ch <= 'Z')) {
                return false;
            }
        }
        return true;
    }

    public static String uncapitalizeFirstLetter(String str) {
        return StringUtils.uncapitalize(str.substring(0, 1)) + str.substring(1);
    }

    /**
     * org.grpctest.example.SomeClass --> SomeClass
     */
    public static String getShortenedClassName(String longClassName) {
        return longClassName.substring(longClassName.lastIndexOf(".") + 1);
    }

    public static String getServiceId(String namespace, String serviceName) {
        return namespace + "." + serviceName;
    }

    public static String getMethodId(String namespace, String serviceName, String methodName) {
        return namespace + "." + serviceName + "." + methodName;
    }

    public static String getMessageId(String namespace, String messageName) {
        return namespace + "." + messageName;
    }

}
