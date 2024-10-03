package org.grpctest.core.service.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class DynamicMessageUtilService {

    private final Registry registry;

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
                            builder.addRepeatedField(field, convertSingleFieldValue(value, field));
                        }
                    }
                } else {
                    builder.setField(field, convertSingleFieldValue(entry.getValue(), field));
                }
            }
        } else {
            log.warn("[objectToDynamicMessage] Object {} conversion is not supported", o);
        }
        return builder.build();
    }

    private Object convertSingleFieldValue(Object value, Descriptors.FieldDescriptor field) {
        return switch (field.getType()) {
            case BOOL -> (Boolean) value;
            case BYTES -> ByteString.copyFromUtf8((String) value);
            case DOUBLE -> (Double) value;
            case ENUM -> field.getEnumType().findValueByName((String) value);
            case FIXED32 -> (Integer) value;
            case FIXED64 -> (Long) value;
            case FLOAT -> (Float) value;
            case INT32 -> (Integer) value;
            case INT64 -> (Long) value;
            case MESSAGE -> objectToDynamicMessage(value, registry.lookupMessage(field.getMessageType().getName()));
            case SFIXED32 -> (Integer) value;
            case SFIXED64 -> (Long) value;
            case SINT32 -> (Integer) value;
            case SINT64 -> (Long) value;
            case STRING -> (String) value;
            case UINT32 -> (Integer) value;
            case UINT64 -> (Long) value;
            default -> throw new IllegalArgumentException("Field type not supported");
        };
    }
}
