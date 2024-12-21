package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/** Write test cases to proto messages and store in files for client and server to read later */
@Slf4j
@Service
@AllArgsConstructor
public class TestCaseWriter {
    private final Config config;
    private final TestcaseRegistry testcaseRegistry;
    private final DynamicMessageUtilService dynamicMessageUtilService;

    public void writeAllTestCases() throws Throwable {
        for (TestCase testCase : testcaseRegistry.getAllTestCases()) {
            writeTestCaseToFile(testCase);
        }
    }

    private void writeTestCaseToFile(TestCase testCase) throws Throwable {
        List<DynamicMessage> params = testCase.getParamValueDynMsg();
        for (int i = 0; i < params.size(); i++) {
            String paramFileName = "client" + File.separator + testCase.getMethodId().replace(".", "_") + "_param_" + i + ".bin";
            writeDynMsgToFile(params.get(i), paramFileName);
        }
        List<DynamicMessage> returns = testCase.getReturnValueDynMsg();
        for (int i = 0; i < returns.size(); i++) {
            String returnFileName = "server" + File.separator + testCase.getMethodId().replace(".", "_") + "_return_" + i + ".bin";
            writeDynMsgToFile(returns.get(i), returnFileName);
        }
    }

    private void writeDynMsgToFile(DynamicMessage dynamicMessage, String filename) throws Throwable {
        String filepath = config.getTestsDir();
        if (filepath.endsWith(File.separator)) {
            filepath = filepath + filename;
        } else {
            filepath = filepath + File.separator + filename;
        }
        dynamicMessageUtilService.dynMsgToFile(dynamicMessage, filepath);
    }
}
