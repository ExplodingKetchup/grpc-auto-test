package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.RuntimeConfig;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.grpctest.core.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.grpctest.core.constant.Constants.OUT_DIR_CLIENT;
import static org.grpctest.core.constant.Constants.OUT_DIR_SERVER;

/**
 * Class to analyze results written by client and server. <br>
 *
 * Will generate a txt output containing every result case for each run. <br>
 * Each result case consists of service name, method name, param (raw and parsed), return (raw and parsed), comparison
 * with expected output (in ./protobin)
 */
@Slf4j
@Service
@AllArgsConstructor
public class ResultAnalyzer {

    private static final String ANALYZER_OUT_DIR = "out/analyzer/";
    private static final String OUTPUT_FILE_EXT = ".txt";
    private static final String H1_UNDERLINE = "\n====================================\n\n";
    private static final String H2_UNDERLINE = "\n------------------------------------\n\n";
    private static final String SECTION_OPEN_LINE = ">>>>---->>>>---->>>>---->>>>---->>>>---->>>>\n>>>>---->>>>---->>>>---->>>>---->>>>---->>>>\n\n";
    private static final String SECTION_CLOSE_LINE = "\n<<<<----<<<<----<<<<----<<<<----<<<<----<<<<\n<<<<----<<<<----<<<<----<<<<----<<<<----<<<<\n\n";
    private static final String OUTPUT_TITLE = "OUTPUT ANALYZER RESULTS" + H1_UNDERLINE;

    private final String outputFileName = generateFileName();
    private final Config config;

    private final RpcModelRegistry rpcModelRegistry;

    private final TestcaseRegistry testcaseRegistry;

    private final DynamicMessageUtilService dynamicMessageUtilService;

    public void processAllMethods(RuntimeConfig runtimeConfig) throws Throwable {
        processSetupScript(runtimeConfig);
        processMetadata(runtimeConfig);
        for (RpcService.RpcMethod method : rpcModelRegistry.getAllMethods()) {
            processOneMethod(method);
        }
    }

    private void processSetupScript(RuntimeConfig runtimeConfig) {
        StringBuilder output = new StringBuilder();
        output.append(wrapTitle("SETUP")).append(H1_UNDERLINE);
        output.append(runtimeConfig.toString()).append("\n");
        try {
            appendToFile(wrapSection(output.toString()));
        } catch (Throwable t) {
            log.error("[processSetupScript] Failed to write to file", t);
        }
    }

    private void processMetadata(RuntimeConfig runtimeConfig) {
        StringBuilder output = new StringBuilder();
        output.append("METADATA").append(H1_UNDERLINE);
        try {
            int expectedNumberOfInvocations = rpcModelRegistry.getAllMethods().size();

            if (!runtimeConfig.getClientToServerMetadataType().equals(MetadataType.NONE) || Files.exists(Paths.get(OUT_DIR_SERVER, "received_metadata.txt"))) {
                output.append("Client -> Server").append(H2_UNDERLINE);
                Map<String, Pair<String, Integer>> serverReceivedMetadata = readMetadataFromFile(OUT_DIR_SERVER + File.separator + "received_metadata.txt");
                Map<String, String> expectedServerReceivedMetadata = rpcModelRegistry.getClientToServerMetadata()
                        .entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey().concat(entry.getValue().getLeft().equals(MetadataType.BIN) ? Metadata.BINARY_HEADER_SUFFIX : ""), entry.getValue().getRight()))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
                assert serverReceivedMetadata != null;
                output.append(formatMetadataResult(serverReceivedMetadata, expectedServerReceivedMetadata, expectedNumberOfInvocations));
            }

            if (!runtimeConfig.getServerToClientMetadataType().equals(MetadataType.NONE) || Files.exists(Paths.get(OUT_DIR_CLIENT, "received_metadata.txt"))) {
                output.append("\nServer -> Client").append(H2_UNDERLINE);
                Map<String, Pair<String, Integer>> clientReceivedMetadata = readMetadataFromFile(OUT_DIR_CLIENT + File.separator + "received_metadata.txt");
                Map<String, String> expectedClientReceivedMetadata = rpcModelRegistry.getServerToClientMetadata()
                        .entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey().concat(entry.getValue().getLeft().equals(MetadataType.BIN) ? Metadata.BINARY_HEADER_SUFFIX : ""), entry.getValue().getRight()))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
                assert clientReceivedMetadata != null;
                output.append(formatMetadataResult(clientReceivedMetadata, expectedClientReceivedMetadata, expectedNumberOfInvocations));
            }

        } catch (InconsistentMetadataException | IOException e) {
            output.append("ERROR!!! ").append(e.getMessage()).append("\n");
        }
        try {
            appendToFile(wrapSection(output.toString()));
        } catch (Throwable t) {
            log.error("[processMetadata] Appending to file failed", t);
        }
    }

    private void processOneMethod(RpcService.RpcMethod method) throws Throwable {
        TestCase testCase = testcaseRegistry.getMethodTestCases(method).get(0);
        List<Pair<byte[], DynamicMessage>> expectedParams = testCase
                .getParamValueDynMsg()
                .stream()
                .map(dynamicMessage -> Pair.of(dynamicMessage.toByteArray(), dynamicMessage))
                .toList();
        List<Pair<byte[], DynamicMessage>> expectedReturns = testCase
                .getReturnValueDynMsg()
                .stream()
                .map(dynamicMessage -> Pair.of(dynamicMessage.toByteArray(), dynamicMessage))
                .toList();
        List<Pair<byte[], DynamicMessage>> actualParams = new ArrayList<>();
        List<Pair<byte[], DynamicMessage>> actualReturns = new ArrayList<>();

        // Process param outputs
        for (int i = 0; i < expectedParams.size(); i++) {
            // File paths
            String actualParamFilePath = OUT_DIR_SERVER + File.separator + method.getId().replace(".", "_") + "_param_" + i + ".bin";
            if (!Files.exists(Paths.get(actualParamFilePath))) {
                log.warn("[processOneMethod] Expected request output file {} not found", actualParamFilePath);
                continue;
            }

            // Read actual param
            actualParams.add(Pair.of(
                    FileUtil.readBytes(actualParamFilePath),
                    dynamicMessageUtilService.dynMsgFromFile(
                            actualParamFilePath,
                            rpcModelRegistry.lookupMessage(method.getInType()).getMessageDescriptor()
                    )
            ));
        }

        // Process returns output
        for (int i = 0; i < expectedReturns.size(); i++) {
            // File paths
            String actualReturnFilePath = OUT_DIR_CLIENT + File.separator + method.getId().replace(".", "_") + "_return_" + i + ".bin";
            if (!Files.exists(Paths.get(actualReturnFilePath))) {
                log.warn("[processOneMethod] Expected response output file {} not found", actualReturnFilePath);
                continue;
            }

            // Read actual return
            actualReturns.add(Pair.of(
                    FileUtil.readBytes(actualReturnFilePath),
                    dynamicMessageUtilService.dynMsgFromFile(
                            actualReturnFilePath,
                            rpcModelRegistry.lookupMessage(method.getOutType()).getMessageDescriptor()
                    )
            ));
        }

        // Process errors
        TestCase.RpcException expectedException = testCase.getException();
        TestCase.RpcException actualException = null;
        if (Objects.nonNull(expectedException)) {
            actualException = readExceptionFromFile(OUT_DIR_CLIENT + File.separator + method.getId().replace(".", "_") + "_error.txt");
        }

        // Compile info into a String and append to File
        appendToFile(
                formatResultCaseAsString(
                        method.getId(),
                        actualParams,
                        expectedParams,
                        actualReturns,
                        expectedReturns,
                        compareRawList(actualParams, expectedParams),
                        compareDynMsgList(actualParams, expectedParams),
                        compareRawList(actualReturns, expectedReturns),
                        compareDynMsgList(actualReturns, expectedReturns),
                        actualException,
                        expectedException,
                        compareException(actualException, expectedException),
                        ""
                )
        );
    }

    private boolean compareRawList(List<Pair<byte[], DynamicMessage>> msgList1, List<Pair<byte[], DynamicMessage>> msgList2) {
        if (msgList1.size() != msgList2.size()) return false;
        for (int i = 0; i < msgList1.size(); i++) {
            if (!compareRaw(msgList1.get(i).getLeft(), msgList2.get(i).getLeft())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareDynMsgList(List<Pair<byte[], DynamicMessage>> msgList1, List<Pair<byte[], DynamicMessage>> msgList2) {
        if (msgList1.size() != msgList2.size()) return false;
        for (int i = 0; i < msgList1.size(); i++) {
            if (!compareDynamicMessage(msgList1.get(i).getRight(), msgList2.get(i).getRight())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareRaw(byte[] rawMsg1, byte[] rawMsg2) {
        return Arrays.equals(rawMsg1, rawMsg2);
    }

    private boolean compareDynamicMessage(DynamicMessage dynMsg1, DynamicMessage dynMsg2) {
        return Objects.nonNull(dynMsg1)
                && Objects.nonNull(dynMsg2)
                && dynMsg1.equals(dynMsg2);
    }

    private TestCase.RpcException readExceptionFromFile(String filepath) throws IOException, InconsistentMetadataException {
        if (filepath.endsWith(".txt")) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(filepath));
                return TestCase.RpcException.builder()
                        .statusCode(Status.Code.valueOf(lines.get(0)))
                        .description(lines.get(1))
                        .trailingMetadata(
                                readMetadata(lines.subList(2, lines.size())).entrySet().stream()
                                        .map(entry -> Pair.of(entry.getKey(), Pair.of(MetadataType.NONE, entry.getValue().getLeft())))
                                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                        )
                        .build();
            } catch (IOException ioe) {
                log.error("[readExceptionFromFile] File I/O failed", ioe);
                throw ioe;
            } catch (InconsistentMetadataException ime) {
                log.error("[readExceptionFromFile] Failed to read trailing metadata", ime);
                throw ime;
            }
        } else {
            return null;
        }
    }

    private boolean compareException(TestCase.RpcException actual, TestCase.RpcException expected) {
        if (Objects.isNull(actual) && Objects.isNull(expected)) {
            return true;
        }
        if (!expected.getStatusCode().equals(actual.getStatusCode())) {
            return false;
        }
        if (!StringUtils.equals(actual.getDescription(), expected.getDescription())) {
            return false;
        }
//        if (actual.getTrailingMetadata().size() != expected.getTrailingMetadata().size()) {
//            return false;
//        }
        for (Map.Entry<String, Pair<MetadataType, String>> trailerExpectedEntry : expected.getTrailingMetadata().entrySet()) {
            if (!actual.getTrailingMetadata().containsKey(trailerExpectedEntry.getKey())) {
                return false;
            }
            if (!StringUtils.equals(trailerExpectedEntry.getValue().getRight(), actual.getTrailingMetadata().get(trailerExpectedEntry.getKey()).getRight())) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Pair<String, Integer>> readMetadataFromFile(String filepath) throws IOException, InconsistentMetadataException {
        if (filepath.endsWith(".txt")) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(filepath));
                return readMetadata(lines);
            } catch (IOException ioe) {
                log.error("[readMetadataFromFile] File I/O failed", ioe);
                throw ioe;
            }
        } else {
            return null;
        }
    }

    /**
     *
     * @param lines
     * @return Key : (Value : Count)
     * @throws InconsistentMetadataException
     */
    private Map<String, Pair<String, Integer>> readMetadata(List<String> lines) throws InconsistentMetadataException {
        Map<String, Pair<String, Integer>> metadata = new HashMap<>();
        Map<String, String> metaContent = new HashMap<>();
        Map<String, Integer> metaCount = new HashMap<>();
        for (String line : lines) {
            if (line.split(":").length < 2) continue;
            String key = line.split(":")[0];
            String value = line.split(":")[1];
            if (metaContent.containsKey(key)) {
                if (value.equals(metaContent.get(key))) {
                    metaCount.put(key, metaCount.get(key) + 1);
                } else {
                    throw new InconsistentMetadataException("Key [" + key + "] has 2 different corresponding values: [" + metaContent.get(key) + "] and [" + value + "]");
                }
            } else {
                metaContent.put(key, value);
                metaCount.put(key, 1);
            }
        }

        for (String key : metaContent.keySet()) {
            metadata.put(key, Pair.of(metaContent.get(key), metaCount.get(key)));
        }

        return metadata;
    }

    /**
     * Returns true if actual metadata map contains all key-value pairs in expected map.
     * Returns false otherwise.
     *
     * @param actual
     * @param expected
     * @return
     */
    private boolean compareMetadata(Map<String, Pair<String, Integer>> actual, Map<String, String> expected, int expectedNumberOfInvocations) {
        for (Map.Entry<String, String> expectedEntry : expected.entrySet()) {
            if (actual.containsKey(expectedEntry.getKey())) {
                String actualValue = actual.get(expectedEntry.getKey()).getLeft();
                Integer actualNumberOfInvocations = actual.get(expectedEntry.getKey()).getRight();
                if (!StringUtils.equals(actualValue, expectedEntry.getValue())
                        || (actualNumberOfInvocations != expectedNumberOfInvocations)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Format this bunch of information to a proper String format to be written to file
     */
    private String formatResultCaseAsString(String methodId,
                                            List<Pair<byte[], DynamicMessage>> actualParams,
                                            List<Pair<byte[], DynamicMessage>> expectedParams,
                                            List<Pair<byte[], DynamicMessage>> actualReturns,
                                            List<Pair<byte[], DynamicMessage>> expectedReturns,
                                            boolean rawRequestComparison,
                                            boolean parsedRequestComparison,
                                            boolean rawResponseComparison,
                                            boolean parsedResponseComparison,
                                            TestCase.RpcException actualException,
                                            TestCase.RpcException expectedException,
                                            boolean exceptionComparison,
                                            String remarks) {
        StringBuilder sb = new StringBuilder();

        sb.append(wrapTitle(methodId)).append(H1_UNDERLINE);

        sb.append(formatListOfMessages(actualParams, "ACTUAL REQUESTS"));

        sb.append(formatListOfMessages(expectedParams, "EXPECTED REQUESTS"));

        sb.append(formatListOfMessages(actualReturns, "ACTUAL RESPONSES"));

        sb.append(formatListOfMessages(expectedReturns, "EXPECTED RESPONSES"));

        sb.append("*** Request comparison (Raw): ").append(rawRequestComparison).append("\n\n");
        sb.append("*** Request comparison (Parsed): ").append(parsedRequestComparison).append("\n\n");
        sb.append("*** Response comparison (Raw): ").append(rawResponseComparison).append("\n\n");
        sb.append("*** Response comparison (Parsed): ").append(parsedResponseComparison).append("\n\n");

        if (Objects.nonNull(actualException) && Objects.nonNull(expectedException)) {
            sb.append(formatRpcException(actualException, expectedException, exceptionComparison)).append("\n");
        }

        sb.append("Remarks: ").append(remarks).append("\n\n");

        return wrapSection(sb.toString());
    }

    private String generateFileName() {
        return "analyzer_out_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss")) + OUTPUT_FILE_EXT;
    }

    private void appendToFile(String content) throws Throwable {
        FileUtil.appendToFile(ANALYZER_OUT_DIR + outputFileName, OUTPUT_TITLE, content);
    }

    private String formatListOfMessages(List<Pair<byte[], DynamicMessage>> messageList, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(":").append(H2_UNDERLINE);
        for (Pair<byte[], DynamicMessage> messagePair : messageList) {
            sb
                    .append(messagePair.getRight().toString())
                    .append("(").append(Base64.getEncoder().encodeToString(messagePair.getLeft())).append(")\n\n");
        }
        return sb.toString();
    }

    private String formatMetadataResult(Map<String, Pair<String, Integer>> actual, Map<String, String> expected, int expectedNumberOfInvocations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Actual").append("\n\n");
        for (Map.Entry<String, Pair<String, Integer>> actualEntry : actual.entrySet()) {
            sb.append(actualEntry.getKey())
                    .append(":")
                    .append(actualEntry.getValue().getLeft())
                    .append(" (x")
                    .append(actualEntry.getValue().getRight())
                    .append(")\n");
        }
        sb.append("\n").append("Expected").append("\n\n");
        for (Map.Entry<String, String> expectedEntry : expected.entrySet()) {
            sb.append(expectedEntry.getKey())
                    .append(":")
                    .append(expectedEntry.getValue())
                    .append(" (x")
                    .append(expectedNumberOfInvocations)
                    .append(")\n");
        }
        sb.append("\n").append("*** Metadata comparison: ").append(compareMetadata(actual, expected, expectedNumberOfInvocations)).append("\n");
        return sb.toString();
    }

    private String formatRpcException(TestCase.RpcException actual, TestCase.RpcException expected, boolean comparison) {
        StringBuilder sb = new StringBuilder();
        sb.append("RPC EXCEPTIONS:\n");
        sb.append("----------------------------------\n\n");

        sb.append("Actual:\n\n");

        sb.append(actual.getStatusCode().name()).append("\n");
        sb.append(actual.getDescription()).append("\n");
        for (Map.Entry<String, Pair<MetadataType, String>> trailerEntry : actual.getTrailingMetadata().entrySet()) {
            sb.append(trailerEntry.getKey()).append(":").append(trailerEntry.getValue().getRight()).append("\n");
        }

        sb.append("\nExpected:\n\n");

        sb.append(expected.getStatusCode().name()).append("\n");
        sb.append(expected.getDescription()).append("\n");
        for (Map.Entry<String, Pair<MetadataType, String>> trailerEntry : expected.getTrailingMetadata().entrySet()) {
            sb.append(trailerEntry.getKey()).append(":").append(trailerEntry.getValue().getRight()).append("\n");
        }

        sb.append("\n*** COMPARISON: ").append(comparison).append("\n");

        return sb.toString();
    }

    private String wrapTitle(String title) {
        return "--- " + title + " ---";
    }

    private String wrapSection(String section) {
        return SECTION_OPEN_LINE + section + SECTION_CLOSE_LINE;
    }

    private static class InconsistentMetadataException extends Exception {
        public InconsistentMetadataException() {
            super();
        }

        public InconsistentMetadataException(String message) {
            super(message);
        }
    }
}
