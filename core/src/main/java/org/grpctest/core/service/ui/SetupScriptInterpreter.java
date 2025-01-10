package org.grpctest.core.service.ui;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
@Service
@Slf4j
public class SetupScriptInterpreter {

    private RuntimeConfig runtimeConfig;

    public RuntimeConfig interpretScript(String filepath) throws IllegalArgumentException, FileNotFoundException, IOException {
        runtimeConfig = new RuntimeConfig();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                interpretSingleLine(line);
            }
            return runtimeConfig;
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
            case METADATA -> interpretMetadataOpCode(Arrays.copyOfRange(words, 1, words.length));
            case MOCK_EXCEPTION -> interpretMockExceptionOpCode();
            case INCLUDE_PROTO -> interpretIncludeProtoOpCode(Arrays.copyOfRange(words, 1, words.length));
        }
    }

    private void interpretServerOpCode(String arg) throws IllegalArgumentException {
        try {
            runtimeConfig.setServer(RuntimeConfig.Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretServerOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode SERVER", iae);
        }
    }

    private void interpretClientOpCode(String arg) throws IllegalArgumentException {
        try {
            runtimeConfig.setClient(RuntimeConfig.Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretClientOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode CLIENT", iae);
        }
    }

    private void interpretTestcaseOpCode(String[] args) {
        for (String arg: args) {
            // Flags
            Pair<String, String> flag = parseFlagArg(arg);
            if (Objects.nonNull(flag)) {
                switch (flag.getLeft()) {
                    case "random" -> {
                        runtimeConfig.setEnableAllRandomTestcase(true);
                        if (StringUtils.isNotBlank(flag.getRight())) {
                            runtimeConfig.setOmitFieldsInRandomTestcases(Integer.parseInt(flag.getRight()));
                        } else {
                            runtimeConfig.setOmitFieldsInRandomTestcases(0);
                        }
                    }
                    default -> {
                        log.warn("[interpretTestcaseOpCode] Invalid flag will be ignored: {}", flag.getLeft());
                    }
                }
            }
        }
    }

    private void interpretMetadataOpCode(String[] args) {
        for (String arg: args) {
            // Flags
            Pair<String, String> flag = parseFlagArg(arg);
            if (Objects.nonNull(flag)) {
                switch (flag.getLeft()) {
                    case "server-client" -> {
                        runtimeConfig.setServerToClientMetadataType(MetadataType.valueOf(flag.getRight()));
                    }
                    case "client-server" -> {
                        runtimeConfig.setClientToServerMetadataType(MetadataType.valueOf(flag.getRight()));
                    }
                    default -> {
                        log.warn("[interpretMetadataOpCode] Invalid flag will be ignored: {}", flag.getLeft());
                    }
                }
            }
        }
    }

    private void interpretMockExceptionOpCode() {
        runtimeConfig.setEnableException(true);
    }

    private void interpretIncludeProtoOpCode(String[] args) {
        for (String arg : args) {
            if (arg.endsWith(".proto")) {
                runtimeConfig.getIncludedProtos().add(arg);
            } else {
                runtimeConfig.getIncludedProtos().add(arg + ".proto");
            }
        }
    }

    /**
     * Extract information from a "flag" arg (in the form --(flag_key):(flag_value) or --(flag_key))
     * @param arg
     * @return a {@link Pair} of (flag_key) : (flag_value); (flag_value) will be {@literal null}
     * when arg is in the form "--(flag_key)"; returns {@literal null} if {@code arg} is not in the flag format
     */
    private Pair<String, String> parseFlagArg(String arg) {
        if (arg.startsWith("--")) {
            String[] splitFlagArg = arg.substring(2).split(":");
            if (splitFlagArg.length > 2) return null;
            String flagKey = splitFlagArg[0];
            String flagValue = "";
            if (splitFlagArg.length > 1) flagValue = splitFlagArg[1];
            return Pair.of(flagKey, flagValue);
        } else {
            return null;
        }
    }

    private enum OpCode {
        /** SERVER {@link org.grpctest.core.pojo.RuntimeConfig.Language} */
        SERVER,
        /** CLIENT {@link org.grpctest.core.pojo.RuntimeConfig.Language} */
        CLIENT,
        /** TESTCASE (--random(:{0, 1, 2})) [Optional] */
        TESTCASE,
        /** METADATA (--server-client:{@link org.grpctest.core.enums.MetadataType}) (--client-server:{@link org.grpctest.core.enums.MetadataType}) [Optional] */
        METADATA,
        /** MOCK_EXCEPTION [Optional] */
        MOCK_EXCEPTION,
        /** INCLUDE_PROTO rpc_1.proto rpc_2.proto [Optional] */
        INCLUDE_PROTO
    }
}
