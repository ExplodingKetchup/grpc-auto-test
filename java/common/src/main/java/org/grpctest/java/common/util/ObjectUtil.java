package org.grpctest.java.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ObjectUtil {

    public static String typeOf(byte b) {
        return "byte";
    }

    public static String typeOf(short s) {
        return "short";
    }

    public static String typeOf(int i) {
        return "int";
    }

    public static String typeOf(long l) {
        return "long";
    }

    public static String typeOf(float f) {
        return "float";
    }

    public static String typeOf(double d) {
        return "double";
    }

    public static String typeOf(boolean b) {
        return "boolean";
    }

    public static String typeOf(char c) {
        return "char";
    }

    public static String typeOf(Object o) {
        return o.getClass().getName();
    }

    public static void logFieldsOfObject(Object o, String objectName, String... getterNames) {
        for (String getterName : getterNames) {
            try {
                // Get the getter method
                Method getter = o.getClass().getDeclaredMethod(getterName);

                // Get the value of the field
                Object value = getter.invoke(o);
                log.info("[logFieldsOfObject] {}: {}() ({}) = {}", objectName, getterName, value.getClass().getName(), value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.warn("[logFieldsOfObject] Failed to access field {} of object {}", getterName, o);
            }
        }
    }
}
