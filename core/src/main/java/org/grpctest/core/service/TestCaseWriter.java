package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.grpctest.core.util.FileUtil;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.grpctest.core.constant.Constants.*;

/** Write test cases to proto messages and store in files for client and server to read later */
@Slf4j
@Service
@AllArgsConstructor
public class TestCaseWriter {
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
            String paramFilePath = StringUtil.trimFileSeparatorAtEnd(TESTS_DIR_CLIENT) + File.separator + testCase.getMethodId().replace(".", "_") + "_param_" + i + ".bin";
            dynamicMessageUtilService.dynMsgToFile(params.get(i), paramFilePath);
        }
        List<DynamicMessage> returns = testCase.getReturnValueDynMsg();
        for (int i = 0; i < returns.size(); i++) {
            String returnFilePath = StringUtil.trimFileSeparatorAtEnd(TESTS_DIR_SERVER) + File.separator + testCase.getMethodId().replace(".", "_") + "_return_" + i + ".bin";
            dynamicMessageUtilService.dynMsgToFile(returns.get(i), returnFilePath);
        }
    }
}
