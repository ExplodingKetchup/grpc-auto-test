package org.grpctest.core.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.RpcTestRegistry;
import org.grpctest.core.pojo.RpcMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class TestCaseGenerator {
    private final Random random = new Random(System.currentTimeMillis());

    private final RpcTestRegistry registry;

    public String generateRandomMessageJson(RpcMessage message) {
        try {
            return JsonFormat.printer().includingDefaultValueFields().omittingInsignificantWhitespace().print(generateRandomMessage(message)).replace("\"", "\\\"");
        } catch (InvalidProtocolBufferException ipbe) {
            log.error("[generateRandomMessageJson] Failed to generate message as json", ipbe);
            return "";
        }
    }

    private DynamicMessage generateRandomMessage(RpcMessage message) {
        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(message.getMessageDescriptor());
        for (Descriptors.FieldDescriptor field : message.getMessageDescriptor().getFields()) {
            if (field.isRepeated()) {
                int repetitions = random.nextInt(8);
                for (int i = 0; i < repetitions; i++) {
                    messageBuilder.addRepeatedField(field, generateRandomSingleValue(field));
                }
            } else {
                messageBuilder.setField(field, generateRandomSingleValue(field));
            }
        }
        return messageBuilder.build();
    }

    /**
     * Generates a random single value for a message field
     */
    private Object generateRandomSingleValue(Descriptors.FieldDescriptor field) {
        return switch (field.getJavaType()) {
            case BOOLEAN -> random.nextBoolean();
            case BYTE_STRING -> {
                byte[] bytes = new byte[random.nextInt(1, 20)];
                random.nextBytes(bytes);
                yield ByteString.copyFrom(bytes);
            }
            case DOUBLE -> random.nextDouble();
            case ENUM -> {
                List<Descriptors.EnumValueDescriptor> enumValues = field.getEnumType().getValues();
                yield enumValues.get(random.nextInt(enumValues.size()));
            }
            case FLOAT -> random.nextFloat();
            case INT -> random.nextInt();
            case LONG -> random.nextLong();
            case MESSAGE -> generateRandomMessage(registry.lookupMessage(field.getMessageType().getName()));
            case STRING -> randomString();
        };
    }

    private String randomString() {
        int length = random.nextInt(1, 20);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                sb.append((char)(random.nextInt('A', 'Z' + 1) + random.nextInt(2) * ('a' - 'A')));
            } else {
                sb.append(random.nextInt(0, 10));
            }
        }
        return sb.toString();
    }
}
