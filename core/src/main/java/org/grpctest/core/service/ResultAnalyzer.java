package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.grpctest.core.util.FileUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

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

    private final Registry registry;

    private final DynamicMessageUtilService dynamicMessageUtilService;

    public void processAllMethods() throws Throwable {
        for (RpcService.RpcMethod method : registry.getAllMethods()) {
            processOneMethod(method);
        }
    }

    private void processOneMethod(RpcService.RpcMethod method) throws Throwable {
        // File paths
        String actualParamFilePath = DynamicMessageUtilService.DIR_SERVER_OUT + method.getOwnerServiceName() + "_" + method.getName() + "_param.bin";
        String actualReturnFilePath = DynamicMessageUtilService.DIR_CLIENT_OUT + method.getOwnerServiceName() + "_" + method.getName() + "_return.bin";

        // Read actual param
        byte[] paramRaw = FileUtil.readBytes(actualParamFilePath);
        DynamicMessage paramParsed = dynamicMessageUtilService.dynMsgFromFile(
                actualParamFilePath,
                registry.lookupMessage(method.getInType()).getMessageDescriptor()
        );

        // Read actual return
        byte[] returnRaw = FileUtil.readBytes(actualReturnFilePath);
        DynamicMessage returnParsed = dynamicMessageUtilService.dynMsgFromFile(
                actualReturnFilePath,
                registry.lookupMessage(method.getOutType()).getMessageDescriptor()
        );

        // Read expected values
        TestCase testCase = registry.getMethodTestCases(method).get(0);
        byte[] expectedParamRaw = testCase.getParamValueDynMsg().toByteArray();
        DynamicMessage expectedParamParsed = testCase.getParamValueDynMsg();
        byte[] expectedReturnRaw = testCase.getReturnValueDynMsg().toByteArray();
        DynamicMessage expectedReturnParsed = testCase.getReturnValueDynMsg();

        // Compare results
        boolean rawComparison = compareRaw(paramRaw, expectedParamRaw)
                && compareRaw(returnRaw, expectedReturnRaw);
        boolean parsedComparison = compareDynamicMessage(paramParsed, expectedParamParsed)
                && compareDynamicMessage(returnParsed, expectedReturnParsed);

        // Compile info into a String and append to File
        appendToFile(
                formatResultCaseAsString(
                        method.getOwnerServiceName(),
                        method.getName(),
                        paramRaw,
                        paramParsed,
                        expectedParamRaw,
                        expectedParamParsed,
                        returnRaw,
                        returnParsed,
                        expectedReturnRaw,
                        expectedReturnParsed,
                        rawComparison,
                        parsedComparison,
                        ""
                )
        );
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
     *
     * @param serviceName
     * @param methodName
     * @param paramRaw
     * @param paramParsed
     * @param expectedParamRaw
     * @param expectedParamParsed
     * @param returnRaw
     * @param returnParsed
     * @param expectedReturnRaw
     * @param expectedReturnParsed
     * @param rawComparison
     * @param parsedComparison
     * @param remarks
     * @return
     */
    private String formatResultCaseAsString(String serviceName,
                                            String methodName,
                                            byte[] paramRaw,
                                            DynamicMessage paramParsed,
                                            byte[] expectedParamRaw,
                                            DynamicMessage expectedParamParsed,
                                            byte[] returnRaw,
                                            DynamicMessage returnParsed,
                                            byte[] expectedReturnRaw,
                                            DynamicMessage expectedReturnParsed,
                                            boolean rawComparison,
                                            boolean parsedComparison,
                                            String remarks) {
        StringBuilder sb = new StringBuilder();

        sb.append(">>>>============================\n");

        sb.append("--- ").append(serviceName).append(".").append(methodName).append(" ---\n\n");

        sb.append("Actual param (raw):\n").append(Base64.getEncoder().encodeToString(paramRaw)).append("\n\n");
        sb.append("Actual param (parsed):\n").append(paramParsed.toString()).append("\n\n");
        sb.append("Expected param (raw):\n").append(Base64.getEncoder().encodeToString(expectedParamRaw)).append("\n\n");
        sb.append("Expected param (parsed):\n").append(expectedParamParsed.toString()).append("\n\n");

        sb.append("Actual return (raw):\n").append(Base64.getEncoder().encodeToString(returnRaw)).append("\n\n");
        sb.append("Actual return (parsed):\n").append(returnParsed.toString()).append("\n\n");
        sb.append("Expected return (raw):\n").append(Base64.getEncoder().encodeToString(expectedReturnRaw)).append("\n\n");
        sb.append("Expected return (parsed):\n").append(expectedReturnParsed.toString()).append("\n\n");

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
}
