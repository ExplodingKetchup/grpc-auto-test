package org.grpctest.core.service;

import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.TestCase;
import org.springframework.stereotype.Service;

import java.io.*;

/** Write test cases to proto messages and store in files for client and server to read later */
@Slf4j
@Service
@AllArgsConstructor
public class TestCaseWriter {
    private final Config config;
    private final Registry registry;

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
        try {
            String filepath = config.getTestsDir();
            if (filepath.endsWith("/")) {
                filepath = filepath + filename;
            } else {
                filepath = filepath + "/" + filename;
            }
            File file = new File(filepath);
            file.createNewFile();

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                dynamicMessage.writeTo(fileOutputStream);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("[writeDynMsgToFile] Output file not created", fnfe);
            throw fnfe;
        } catch (IOException ioe) {
            log.error("[writeDynMsgToFile] Error in file I/O", ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[writeDynMsgToFile] An error occurred", t);
            throw t;
        }
    }
}
