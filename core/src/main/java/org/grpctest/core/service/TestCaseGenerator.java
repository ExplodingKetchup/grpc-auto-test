package org.grpctest.core.service;

import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
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


    public TestCase generateTestcase(RpcService.RpcMethod method, int omitFields, int valueMode, boolean hasException) {
        int numberOfParams;
        int numberOfReturns;
        TestCase.RpcException rpcException = hasException ? generateRandomException() : null;

        // Set number of params and returns to generate
        switch (method.getType()) {
            case UNARY -> {
                if (hasException) {
                    numberOfParams = 1;
                    numberOfReturns = 1;
                } else {
                    numberOfParams = 1;
                    numberOfReturns = 1;
                }
            }
            case SERVER_STREAMING -> {
                if (hasException) {
                    numberOfParams = 1;
                    numberOfReturns = random.nextInt(2, 4);
                } else {
                    numberOfParams = 1;
                    numberOfReturns = random.nextInt(2, 4);
                }
            }
            case CLIENT_STREAMING -> {
                if (hasException) {
                    numberOfParams = random.nextInt(2, 4);
                    numberOfReturns = 1;
                } else {
                    numberOfParams = random.nextInt(2, 4);
                    numberOfReturns = 1;
                }
            }
            case BIDI_STREAMING -> {
                if (hasException) {
                    numberOfParams = random.nextInt(2, 4);
                    numberOfReturns = random.nextInt(2, 4);
                } else {
                    numberOfParams = random.nextInt(2, 4);
                    numberOfReturns = random.nextInt(2, 4);
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
            paramList.add(generateMessage(rpcModelRegistry.lookupMessage(method.getInType()), omitFields, valueMode));
        }
        List<DynamicMessage> returnList = new ArrayList<>(numberOfReturns);
        for (int i = 0; i < numberOfReturns; i++) {
            returnList.add(generateMessage(rpcModelRegistry.lookupMessage(method.getOutType()), omitFields, valueMode));
        }

        TestCase testCase = TestCase.builder()
                .name(method.getId() + "_random")
                .methodId(method.getId())
                .paramValueDynMsg(paramList)
                .returnValueDynMsg(returnList)
                .exception(rpcException)
                .build();
//        log.info("[generateRandomTestcase] Added test case {}", testCase);
        return testCase;
    }

    /**
     * Create a message of a given type, and supplies message fields with random values.
     * Can set omitFields to generate messages with unset fields.
     * omitFields behaviour will be inherited by sub-messages.
     *
     * @param message
     * @param omitFields 0 = no omit, 1 = partial omit, 2 = full omit (empty message)
     * @param valueMode 0 = default values, 1 = random values
     * @return
     */
    public DynamicMessage generateMessage(RpcMessage message, int omitFields, int valueMode) {
        DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(message.getMessageDescriptor());
        if (omitFields < 2) {
            for (Descriptors.FieldDescriptor field : message.getMessageDescriptor().getFields()) {
                if (omitFields == 1) {
                    if (random.nextBoolean()) continue;
                }
                if (field.isRepeated()) {
                    int repetitions = random.nextInt(4);
                    for (int i = 0; i < repetitions; i++) {
                        switch (valueMode) {
                            case 0 -> {
                                Object value = generateDefaultSingleValue(field, omitFields);
                                if (Objects.nonNull(value)) {
                                    messageBuilder.addRepeatedField(field, value);
                                }
                            }
                            case 1 -> messageBuilder.addRepeatedField(field, generateRandomSingleValue(field, omitFields));
                        }

                    }
                } else {
                    switch (valueMode) {
                        case 0 -> {
                            Object value = generateDefaultSingleValue(field, omitFields);
                            if (Objects.nonNull(value)) {
                                messageBuilder.setField(field, value);
                            }
                        }
                        case 1 -> messageBuilder.setField(field, generateRandomSingleValue(field, omitFields));
                    }
                }
            }
        }
        return messageBuilder.build();
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
        String metadataKey = randomString(false, true, true);
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
    private Object generateRandomSingleValue(Descriptors.FieldDescriptor field, int omitSubMsgFields) {
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
            case MESSAGE -> {
                RpcMessage rpcMessage = rpcModelRegistry.lookupMessage(field.getMessageType().getFullName());
                if (Objects.isNull(rpcMessage)) {   // Likely a result of implicit message types (e.g. map entries)
                    rpcMessage = new RpcMessage(
                            field.getMessageType().getFullName().substring(0, field.getMessageType().getFullName().lastIndexOf(".")),
                            StringUtil.getShortenedClassName(field.getMessageType().getFullName()),
                            field.getMessageType()
                    );
                    rpcModelRegistry.addMessageToLookupTable(rpcMessage);
                }
                yield generateMessage(rpcMessage, omitSubMsgFields, 1);
            }
            case STRING -> randomString();
        };
    }

    private Object generateDefaultSingleValue(Descriptors.FieldDescriptor field, int omitSubMsgFields) {
        return switch (field.getJavaType()) {
            case BOOLEAN -> Boolean.FALSE;
            case BYTE_STRING -> ByteString.copyFrom(new byte[]{});
            case DOUBLE -> 0.0;
            case ENUM -> {
                List<Descriptors.EnumValueDescriptor> enumValues = field.getEnumType().getValues();
                yield enumValues.get(0);
            }
            case FLOAT -> 0.0f;
            case INT -> 0;
            case LONG -> 0L;
            case MESSAGE -> null;
            case STRING -> "";
        };
    }

    private String randomString() {
        return randomString(true, true, true);
    }

    private String randomString(boolean includeUppercase, boolean includeLowercase, boolean includeNumber) {
        int length = random.nextInt(1, 20);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if ((!includeNumber) || (random.nextBoolean())) {
                int uncapitalize = random.nextInt(2);
                if (!includeLowercase) uncapitalize = 0;
                if (!includeUppercase) uncapitalize = 1;
                if (includeUppercase || includeLowercase) {
                    sb.append((char) (random.nextInt('A', 'Z' + 1) + uncapitalize * ('a' - 'A')));
                }
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
