package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.springframework.stereotype.Service;

import java.io.*;

/** Write test cases to proto messages and store in files for client and server to read later */
@Slf4j
@Service
@AllArgsConstructor
public class TestCaseWriter {
    private final Config config;
    private final Registry registry;
    private final DynamicMessageUtilService dynamicMessageUtilService;

    public void writeAllTestCases() throws Throwable {
        for (TestCase testCase : registry.getAllTestCases()) {
            writeTestCaseToFile(testCase);
        }
    }

    private void writeTestCaseToFile(TestCase testCase) throws Throwable {
        String paramFileName = testCase.getServiceName() + "_" + testCase.getMethodName() + "_param.bin";
        String returnFileName = testCase.getServiceName() + "_" + testCase.getMethodName() + "_return.bin";
        writeDynMsgToFile(testCase.getParamValueDynMsg(), paramFileName);
        writeDynMsgToFile(testCase.getReturnValueDynMsg(), returnFileName);
    }

    private void writeDynMsgToFile(DynamicMessage dynamicMessage, String filename) throws Throwable {
        String filepath = config.getTestsDir();
        if (filepath.endsWith("/")) {
            filepath = filepath + filename;
        } else {
            filepath = filepath + "/" + filename;
        }
        dynamicMessageUtilService.dynMsgToFile(dynamicMessage, filepath);
    }
}
