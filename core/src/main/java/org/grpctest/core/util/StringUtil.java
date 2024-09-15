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

    public static String uncapitalizeFirstLetter(String str) {
        return StringUtils.uncapitalize(str.substring(0, 1)) + str.substring(1);
    }

    /**
     * org.grpctest.example.SomeClass --> SomeClass
     */
    public static String getShortenedClassName(String longClassName) {
        return longClassName.substring(longClassName.lastIndexOf(".") + 1);
    }
}
