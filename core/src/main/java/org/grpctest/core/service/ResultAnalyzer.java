package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.A;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.grpctest.core.util.FileUtil;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String OUTPUT_TITLE = "OUTPUT ANALYZER RESULTS\n-----------------------\n\n";

    private final String outputFileName = generateFileName();

    private final RpcModelRegistry rpcModelRegistry;

    private final TestcaseRegistry testcaseRegistry;

    private final DynamicMessageUtilService dynamicMessageUtilService;

    public void processAllMethods() throws Throwable {
        for (RpcService.RpcMethod method : rpcModelRegistry.getAllMethods()) {
            processOneMethod(method);
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
            String actualParamFilePath = DynamicMessageUtilService.DIR_SERVER_OUT + method.getId().replace(".", "_") + "_param_" + i + ".bin";
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

        for (int i = 0; i < expectedReturns.size(); i++) {
            // File paths
            String actualReturnFilePath = DynamicMessageUtilService.DIR_CLIENT_OUT + method.getId().replace(".", "_") + "_return_" + i + ".bin";
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
        // Compile info into a String and append to File
//        appendToFile(
//                formatResultCaseAsString(
//                        method.getId(),
//
//                )
//        );
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

    /**
     * Format this bunch of information to a proper String format to be written to file
     */
    private String formatResultCaseAsString(String methodId,
                                            List<Pair<byte[], DynamicMessage>> actualParams,
                                            List<Pair<byte[], DynamicMessage>> expectedParams,
                                            List<Pair<byte[], DynamicMessage>> actualReturns,
                                            List<Pair<byte[], DynamicMessage>> expectedReturns,
                                            boolean rawComparison,
                                            boolean parsedComparison,
                                            String remarks) {
        StringBuilder sb = new StringBuilder();

        sb.append(">>>>============================\n");

        sb.append("--- ").append(methodId).append(" ---\n\n");

        sb.append(formatListOfMessages(actualParams, "ACTUAL REQUESTS"));

        sb.append(formatListOfMessages(expectedParams, "EXPECTED REQUESTS"));

        sb.append(formatListOfMessages(actualReturns, "ACTUAL RESPONSES"));

        sb.append(formatListOfMessages(expectedReturns, "EXPECTED RESPONSES"));

        sb.append("Raw comparison result: ").append(rawComparison).append("\n\n");
        sb.append("Parsed comparison result: ").append(parsedComparison).append("\n\n");

        sb.append("Remarks: ").append(remarks).append("\n\n");

        sb.append("<<<<============================\n\n");

        return sb.toString();
    }

    private String generateFileName() {
        return "analyzer_out_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss")) + OUTPUT_FILE_EXT;
    }

    private void appendToFile(String content) throws Throwable {
        FileUtil.appendToFile(ANALYZER_OUT_DIR + outputFileName, OUTPUT_TITLE, content);
    }

    private String formatListOfMessages(List<Pair<byte[], DynamicMessage>> messageList, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(":\n\n");
        for (Pair<byte[], DynamicMessage> messagePair : messageList) {
            sb
                    .append(messagePair.getRight().toString()).append("\n")
                    .append("(").append(Base64.getEncoder().encodeToString(messagePair.getLeft())).append(")\n\n");
        }
        return sb.toString();
    }
}
