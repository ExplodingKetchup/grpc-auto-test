package org.grpctest.core.service.ui;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.pojo.TestConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@AllArgsConstructor
@Service
@Slf4j
public class SetupScriptInterpreter {

    private TestConfig testConfig;

    public TestConfig interpretScript(String filepath) throws IllegalArgumentException, FileNotFoundException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                interpretSingleLine(line);
            }
            return testConfig;
        } catch (FileNotFoundException fnfe) {
            log.error("[interpretScript] Invalid file path: {}", filepath);
            throw fnfe;
        } catch (IOException ioe) {
            log.error("[interpretScript] File I/O exception", ioe);
            throw ioe;
        }
    }

    private void interpretSingleLine(String line) throws IllegalArgumentException {
        String[] words = line.split(" ");
        OpCode opCode;
        try {
            opCode = OpCode.valueOf(StringUtils.capitalize(words[0]));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretSingleLine] Illegal opcode: {}", words[0]);
            throw new IllegalArgumentException("Illegal opcode [" + words[0] + "]", iae);
        }
        switch (opCode) {
            case SERVER -> interpretServerOpCode(words[1]);
            case CLIENT -> interpretClientOpCode(words[1]);
            case TESTCASE -> interpretTestcaseOpCode(Arrays.copyOfRange(words, 1, words.length));
        }
    }

    private void interpretServerOpCode(String arg) throws IllegalArgumentException {
        try {
            testConfig.setServer(TestConfig.Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretServerOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode SERVER", iae);
        }
    }

    private void interpretClientOpCode(String arg) throws IllegalArgumentException {
        try {
            testConfig.setClient(TestConfig.Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretClientOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode CLIENT", iae);
        }
    }

    private void interpretTestcaseOpCode(String[] args) {

    }

    private enum OpCode {
        SERVER,
        CLIENT,
        TESTCASE
    }
}
