package org.grpctest.core.service.ui;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RuntimeConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@NoArgsConstructor
@Service
@Slf4j
public class SetupScriptInterpreter {

    private static final String COMMENT_INDICATOR = "#";    // Lines started with # will be ignored
    private static final List<String> SUPPORTED_COMPRESSION_ALGOS = Lists.newArrayList("gzip", "deflate");

    private RuntimeConfig runtimeConfig;

    public RuntimeConfig interpretScript(String filepath) throws IllegalArgumentException, IOException {
        runtimeConfig = new RuntimeConfig();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith(COMMENT_INDICATOR)) {
                    interpretSingleLine(line);
                }
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
        Option option;
        try {
            option = Option.valueOf(StringUtils.capitalize(words[0]));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretSingleLine] Illegal opcode: {}", words[0]);
            throw new IllegalArgumentException("Illegal opcode [" + words[0] + "]", iae);
        }
        switch (option) {
            case SERVER -> interpretServerOpCode(words[1]);
            case CLIENT -> interpretClientOpCode(words[1]);
            case TESTCASE -> interpretTestcaseOpCode(getArgs(words));
            case COMPRESSION -> interpretCompressionOpCode(getArgs(words));
            case METADATA -> interpretMetadataOpCode(getArgs(words));
            case MOCK_EXCEPTION -> interpretMockExceptionOpCode();
            case INCLUDE_PROTO -> interpretIncludeProtoOpCode(getArgs(words));
            case GENERATE_FILES_ONLY -> interpretGenerateFilesOnlyOpCode(getArgs(words));
            case SUPPORT -> interpretSupportOpCode(getArgs(words));
        }
    }

    private void interpretServerOpCode(String arg) throws IllegalArgumentException {
        try {
            runtimeConfig.setServer(Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretServerOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode SERVER", iae);
        }
    }

    private void interpretClientOpCode(String arg) throws IllegalArgumentException {
        try {
            runtimeConfig.setClient(Language.valueOf(StringUtils.capitalize(arg)));
        } catch (IllegalArgumentException iae) {
            log.error("[interpretClientOpCode] Illegal argument: {}", arg);
            throw new IllegalArgumentException("Illegal argument for opcode CLIENT", iae);
        }
    }

    private void interpretTestcaseOpCode(String[] args) {
        boolean encounteredValueFlag = false;
        for (String arg: args) {
            // Flags
            Pair<String, String> flag = parseFlagArg(arg);
            if (Objects.nonNull(flag)) {
                switch (flag.getLeft()) {
                    case "random" -> {
                        if (!encounteredValueFlag) {
                            runtimeConfig.setEnableGeneratedTestcase(true);
                            runtimeConfig.setValueMode(1);
                            if (StringUtils.isNotBlank(flag.getRight())) {
                                runtimeConfig.setOmitFieldsInRandomTestcases(Integer.parseInt(flag.getRight()));
                            } else {
                                runtimeConfig.setOmitFieldsInRandomTestcases(0);
                            }
                            encounteredValueFlag = true;
                        }
                    }
                    case "default" -> {
                        if (!encounteredValueFlag) {
                            runtimeConfig.setEnableGeneratedTestcase(true);
                            runtimeConfig.setValueMode(0);
                            if (StringUtils.isNotBlank(flag.getRight())) {
                                runtimeConfig.setOmitFieldsInRandomTestcases(Integer.parseInt(flag.getRight()));
                            } else {
                                runtimeConfig.setOmitFieldsInRandomTestcases(0);
                            }
                            encounteredValueFlag = true;
                        }
                    }
                    default -> {
                        log.warn("[interpretTestcaseOpCode] Invalid flag will be ignored: {}", flag.getLeft());
                    }
                }
            } else {
                runtimeConfig.getIncludedCustomTestcases().add(arg);
            }
        }
    }

    private void interpretCompressionOpCode(String[] args) {
        for (String arg : args) {
            Pair<String, String> flag = parseFlagArg(arg);
            if (Objects.nonNull(flag)) {
                if (StringUtils.equals(flag.getLeft(), "request")){
                    if (SUPPORTED_COMPRESSION_ALGOS.contains(flag.getRight())) {
                        runtimeConfig.setRequestCompression(flag.getRight());
                    }
                } else if (StringUtils.equals(flag.getLeft(), "response")) {
                    if (SUPPORTED_COMPRESSION_ALGOS.contains(flag.getRight())) {
                        runtimeConfig.setResponseCompression(flag.getRight());
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

    private void interpretGenerateFilesOnlyOpCode(String[] args) {
        runtimeConfig.setGenerateFilesOnly(true);
    }

    private void interpretSupportOpCode(String[] args) {
        String supportService = "";
        Integer position = -1;
        for (String arg : args) {
            Pair<String, String> flag = parseFlagArg(arg);
            if (Objects.isNull(flag)) {
                supportService = arg;
            } else {
                if (flag.getLeft().equals("position")) {
                    position = Integer.parseInt(flag.getRight());
                }
            }
        }
        if (StringUtils.isBlank(supportService) || position < 0 || position > 3) {
            log.warn("[interpretSupportOpCode] Invalid SUPPORT config [SUPPORT {}]. Will ignore this line", String.join(" ", args));
            return;
        }
        if (Objects.isNull(runtimeConfig.getSupport())) {
            runtimeConfig.setSupport(new HashMap<>());
        }
        runtimeConfig.getSupport().putIfAbsent(supportService, position);
    }

    private String[] getArgs(String[] line) {
        if (line.length > 1) {
            return Arrays.copyOfRange(line, 1, line.length);
        }
        return new String[]{};
    }

    /**
     * Extract information from a "flag" arg (in the form --(flag_key)=(flag_value) or --(flag_key))
     * @param arg
     * @return a {@link Pair} of (flag_key) = (flag_value); (flag_value) will be {@literal null}
     * when arg is in the form "--(flag_key)"; returns {@literal null} if {@code arg} is not in the flag format
     */
    private Pair<String, String> parseFlagArg(String arg) {
        if (arg.startsWith("--")) {
            String[] splitFlagArg = arg.substring(2).split("=");
            if (splitFlagArg.length > 2) return null;
            String flagKey = splitFlagArg[0];
            String flagValue = "";
            if (splitFlagArg.length > 1) flagValue = splitFlagArg[1];
            return Pair.of(flagKey, flagValue);
        } else {
            return null;
        }
    }

    private enum Option {
        /** SERVER {@link Language} */
        SERVER,
        /** CLIENT {@link Language} */
        CLIENT,
        /** TESTCASE (--{random, default}(={0, 1, 2})) (custom_testcase1.json custom_testcase2.json) <br>
         * Note that if custom testcases are specified, "random" and "default" flags will be nullified. <br>
         * "--random" and "--default" are mutually exclusive. If both are present, will interpret the 1st flag only.<br>
         * --random: Randomly assign values for fields<br>
         * --default: Assign default values for fields<br>
         * Flag value of random / default flag: Specify whether to ignore generating values for certain fields
         * (0 = no omit; 1 = partial omit; 2 = full omit)
         */
        TESTCASE,
        /** COMPRESSION --request=compression_algo --response=compression_algo <br>
         * Currently only support "gzip" and "deflate". If not specified, will interpret as uncompressed. <br>
         * Note that there is no mechanism to check if the algo is supported in a language. May cause errors in such cases.
         */
        COMPRESSION,
        /** METADATA (--server-client={@link org.grpctest.core.enums.MetadataType}) (--client-server={@link org.grpctest.core.enums.MetadataType}) [Optional] */
        METADATA,
        /** MOCK_EXCEPTION [Optional] */
        MOCK_EXCEPTION,
        /** INCLUDE_PROTO rpc_1.proto rpc_2.proto [Optional] */
        INCLUDE_PROTO,
        /** GENERATE_FILES_ONLY */
        GENERATE_FILES_ONLY,
        /** SUPPORT service-name --position=(0,1,2,3) <br>
         * position: when to launch supporting service (0 = before server, 1 = after server, 2 = before client, 3 = after client)
         */
        SUPPORT
    }
}
