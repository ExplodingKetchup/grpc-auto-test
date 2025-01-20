package org.grpctest.core.service.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class DynamicMessageUtilService {

    private static final String NUMERIC_MAX = "MAX";
    private static final String NUMERIC_MIN = "MIN";
    private static final String NUMERIC_FLOAT_MIN_FRACTION = "MIN_FRACTION";

    private final RpcModelRegistry rpcModelRegistry;

    public void dynMsgToFile(DynamicMessage dynamicMessage, String filepath) throws Throwable {
        try {
            Path path = Paths.get(filepath);
            if (Objects.nonNull(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            File file = new File(filepath);
            file.createNewFile();

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                dynamicMessage.writeTo(fileOutputStream);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("[dynMsgToFile] Output file not created", fnfe);
            throw fnfe;
        } catch (IOException ioe) {
            log.error("[dynMsgToFile] Error in file I/O", ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[dynMsgToFile] An error occurred", t);
            throw t;
        }
    }

    public DynamicMessage dynMsgFromFile(String filepath, Descriptors.Descriptor descriptor) throws Throwable {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
                return DynamicMessage.parseFrom(descriptor, fileInputStream);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("[dynMsgFromFile] File not found", fnfe);
            throw fnfe;
        } catch (IOException ioe) {
            log.error("[dynMsgFromFile] File I/O exception", ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[dynMsgFromFile] An error occurred", t);
            throw t;
        }
    }

    public DynamicMessage objectToDynamicMessage(Object o, RpcMessage rpcMessage) {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(rpcMessage.getMessageDescriptor());
        if (o instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet()) {
                String fieldName = (String) entry.getKey();
                if (!StringUtil.isSnakeCase(fieldName)) {
                    fieldName = StringUtil.camelCaseToSnakeCase(fieldName);
                }
                Descriptors.FieldDescriptor field = rpcMessage.getMessageDescriptor().findFieldByName(fieldName);
                if (field.isRepeated()) {
                    if (entry.getValue() instanceof List) {
                        for (Object value : (List)entry.getValue()) {
                            Object convertedValue = convertSingleFieldValue(value, field);
                            if (Objects.nonNull(convertedValue)) {
                                builder.addRepeatedField(field, convertedValue);
                            }
                        }
                    }
                } else {
                    Object convertedValue = convertSingleFieldValue(entry.getValue(), field);
                    if (Objects.nonNull(convertedValue)) {
                        builder.setField(field, convertedValue);
                    }
                }
            }
        } else {
            log.warn("[objectToDynamicMessage] Object {} conversion is not supported", o);
        }
        return builder.build();
    }

    private Object convertSingleFieldValue(Object value, Descriptors.FieldDescriptor field) {
        try {
            return switch (field.getType()) {
                case BOOL -> (Boolean) value;
                case BYTES -> ByteString.fromHex((String) value);
                case DOUBLE -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield Double.MAX_VALUE;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield -Double.MAX_VALUE;
                        } else if (value.equals(NUMERIC_FLOAT_MIN_FRACTION)) {
                            yield Double.MIN_VALUE;
                        }
                    }
                    yield (Double) value;
                }
                case ENUM -> {
                    if (value instanceof String) {
                        yield field.getEnumType().findValueByName((String) value);
                    } else if (value instanceof Long) {
                        yield field.getEnumType().findValueByNumberCreatingIfUnknown(Math.toIntExact((Long) value));
                    } else {
                        yield null;
                    }
                }
                case FIXED32, UINT32 -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield -1;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield 0;
                        }
                    }
                    yield ((Long) value).intValue();
                }
                case FIXED64, UINT64 -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield -1L;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield 0L;
                        }
                    }
                    yield (Long) value;
                }
                case FLOAT -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield Float.MAX_VALUE;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield -Float.MAX_VALUE;
                        } else if (value.equals(NUMERIC_FLOAT_MIN_FRACTION)) {
                            yield Float.MIN_VALUE;
                        }
                    }
                    yield ((Double) value).floatValue();
                }
                case INT32, SFIXED32, SINT32 -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield Integer.MAX_VALUE;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield Integer.MIN_VALUE;
                        }
                    }
                    yield ((Long) value).intValue();
                }
                case INT64, SFIXED64, SINT64 -> {
                    if (value instanceof String) {
                        if (value.equals(NUMERIC_MAX)) {
                            yield Long.MAX_VALUE;
                        } else if (value.equals(NUMERIC_MIN)) {
                            yield Long.MIN_VALUE;
                        }
                    }
                    yield (Long) value;
                }
                case MESSAGE ->
                        objectToDynamicMessage(value, rpcModelRegistry.lookupMessage(field.getMessageType().getFullName()));
                case STRING -> (String) value;
                default -> throw new IllegalArgumentException("Field type not supported");
            };
        } catch (Exception e) {
            return null;
        }
    }
}
