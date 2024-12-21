package org.grpctest.core.service;

import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class TestCaseGenerator {

    private static final Set<Status.Code> ERR_STATUS_CODES =
            Sets.newHashSet(
                    Status.Code.ABORTED,
                    Status.Code.ALREADY_EXISTS,
                    Status.Code.CANCELLED,
                    Status.Code.DATA_LOSS,
                    Status.Code.DEADLINE_EXCEEDED,
                    Status.Code.FAILED_PRECONDITION,
                    Status.Code.INTERNAL,
                    Status.Code.INVALID_ARGUMENT,
                    Status.Code.NOT_FOUND,
                    Status.Code.OUT_OF_RANGE,
                    Status.Code.PERMISSION_DENIED,
                    Status.Code.RESOURCE_EXHAUSTED,
                    Status.Code.UNAUTHENTICATED,
                    Status.Code.UNAVAILABLE,
                    Status.Code.UNIMPLEMENTED,
                    Status.Code.UNKNOWN);

    private final Random random = new Random(System.currentTimeMillis());

    private final RpcModelRegistry rpcModelRegistry;

    public String generateRandomMessageJson(RpcMessage message) {
        try {
            return JsonFormat.printer().includingDefaultValueFields().omittingInsignificantWhitespace().print(generateRandomMessage(message)).replace("\"", "\\\"");
        } catch (InvalidProtocolBufferException ipbe) {
            log.error("[generateRandomMessageJson] Failed to generate message as json", ipbe);
            return "";
        }
    }

    public DynamicMessage generateRandomMessage(RpcMessage message) {
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

    public TestCase generateRandomTestcase(RpcService.RpcMethod method, boolean hasException) {
        int numberOfParams;
        int numberOfReturns;
        TestCase.RpcException rpcException = hasException ? generateRandomException() : null;

        // Set number of params and returns to generate
        switch (method.getType()) {
            case UNARY -> {
                if (hasException) {
                    numberOfParams = 1;
                    numberOfReturns = 0;
                } else {
                    numberOfParams = 1;
                    numberOfReturns = 1;
                }
            }
            case SERVER_STREAMING -> {
                if (hasException) {
                    numberOfParams = 1;
                    numberOfReturns = random.nextInt(1, 4);
                } else {
                    numberOfParams = 1;
                    numberOfReturns = random.nextInt(1, 4);
                }
            }
            case CLIENT_STREAMING -> {
                if (hasException) {
                    numberOfParams = random.nextInt(1, 4);
                    numberOfReturns = 0;
                } else {
                    numberOfParams = random.nextInt(1, 4);
                    numberOfReturns = 1;
                }
            }
            case BIDI_STREAMING -> {
                if (hasException) {
                    numberOfParams = random.nextInt(1, 4);
                    numberOfReturns = random.nextInt(1, 4);
                } else {
                    numberOfParams = random.nextInt(1, 4);
                    numberOfReturns = random.nextInt(1, 4);
                }
            }
            default -> {
                numberOfParams = 0;
                numberOfReturns = 0;
            }
        }

        // Generate a number of DynamicMessage as params / returns
        List<DynamicMessage> paramList = new ArrayList<>(numberOfParams);
        for (int i = 0; i < numberOfParams; i++) {
            paramList.add(generateRandomMessage(rpcModelRegistry.lookupMessage(method.getInType())));
        }
        List<DynamicMessage> returnList = new ArrayList<>(numberOfReturns);
        for (int i = 0; i < numberOfReturns; i++) {
            returnList.add(generateRandomMessage(rpcModelRegistry.lookupMessage(method.getOutType())));
        }

        TestCase testCase = TestCase.builder()
                .name(method.getId() + "_random")
                .methodId(method.getId())
                .paramValueDynMsg(paramList)
                .returnValueDynMsg(returnList)
                .exception(rpcException)
                .build();
        log.info("[generateRandomTestcase] Added test case {}", testCase);
        return testCase;
    }

    public Map<String, Pair<MetadataType, String>> generateRandomMetadata(MetadataType metadataType) {
        if (metadataType == MetadataType.NONE) return new HashMap<>();
        int numberOfMetadata = random.nextInt(1, 4);
        Map<String, Pair<MetadataType, String>> metadataMap = new HashMap<>(numberOfMetadata);
        for (int i = 0; i < numberOfMetadata; i++) {
            Pair<String, String> entry = generateSingleRandomMetadata(metadataType);
            metadataMap.put(entry.getLeft(), Pair.of(metadataType, entry.getRight()));
        }
        return metadataMap;
    }

    private Pair<String, String> generateSingleRandomMetadata(MetadataType metadataType) {
        String metadataKey = randomString();
        while (metadataKey.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
            metadataKey = randomString();
        }
        return switch (metadataType) {
            case STRING -> Pair.of(metadataKey, randomString());
            case BIN -> Pair.of(metadataKey, StringUtil.bytesToHexString(randomBytes()));
            case NONE -> null;
        };
    }

    private TestCase.RpcException generateRandomException() {
        List<Status.Code> errCodes = new ArrayList<>(ERR_STATUS_CODES);
        return TestCase.RpcException.builder()
                .statusCode(errCodes.get(random.nextInt(0, ERR_STATUS_CODES.size())))
                .description(randomString())
                .isRuntimeException(random.nextBoolean())
                .trailingMetadata(generateRandomMetadata(MetadataType.STRING))
                .build();
    }

    /**
     * Generates a random single value for a message field
     */
    private Object generateRandomSingleValue(Descriptors.FieldDescriptor field) {
        return switch (field.getJavaType()) {
            case BOOLEAN -> random.nextBoolean();
            case BYTE_STRING -> ByteString.copyFrom(randomBytes());
            case DOUBLE -> random.nextDouble();
            case ENUM -> {
                List<Descriptors.EnumValueDescriptor> enumValues = field.getEnumType().getValues();
                yield enumValues.get(random.nextInt(enumValues.size()));
            }
            case FLOAT -> random.nextFloat();
            case INT -> random.nextInt();
            case LONG -> random.nextLong();
            case MESSAGE -> generateRandomMessage(rpcModelRegistry.lookupMessage(field.getMessageType().getFullName()));
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

    private byte[] randomBytes() {
        byte[] bytes = new byte[random.nextInt(1, 20)];
        random.nextBytes(bytes);
        return bytes;
    }
}
