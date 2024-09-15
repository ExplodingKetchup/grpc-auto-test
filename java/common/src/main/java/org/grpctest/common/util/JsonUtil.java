package org.grpctest.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object o) throws Exception {
        if (Objects.isNull(o)) {
            return null;
        }
        return OBJECT_MAPPER.writerFor(o.getClass()).writeValueAsString(o);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return OBJECT_MAPPER.readerFor(clazz).readValue(json);
    }
}

