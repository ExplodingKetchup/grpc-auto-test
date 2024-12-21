package org.grpctest.core.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void snakeCaseToCamelCase() {
    }

    @Test
    void camelCaseToSnakeCase() {
    }

    @Test
    void isSnakeCase() {
    }

    @Test
    void uncapitalizeFirstLetter() {
    }

    @Test
    void getShortenedClassName() {
    }

    @Test
    void getServiceId() {
    }

    @Test
    void getMethodId() {
    }

    @Test
    void getMessageId() {
    }

    @Test
    void bytesToHexString() {
        byte[] byteArray = {0, 9, 3, -1, 5, 8, -2};
        String hex = StringUtil.bytesToHexString(byteArray);
        assertEquals("000903ff0508fe", hex);
        System.out.println(hex);
        System.out.println(Arrays.toString(HexFormat.of().parseHex(hex)));
    }
}