package org.grpctest.core.service.util;

import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.ProgramType;
import org.grpctest.core.pojo.TestProgram;
import org.grpctest.core.service.DockerService;
import org.grpctest.core.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class TestProgramUtilService {

    private static final long STARTUP_TIMESTAMP = System.currentTimeMillis();

    private final TestcaseRegistry testcaseRegistry;

    private final DockerService dockerService;

    public TestProgramUtilService(TestcaseRegistry testcaseRegistry, DockerService dockerService) {
        this.testcaseRegistry = testcaseRegistry;
        this.dockerService = dockerService;
    }

    public boolean checkServerRunning(Language serverLanguage) {
        try {
            return searchLogContentNewerThan(
                    getLatestLogFile(ProgramType.SERVER, serverLanguage),
                    "Server started",
                    serverLanguage,
                    STARTUP_TIMESTAMP
            );
        } catch (IOException ioe) {
            return false;
        }
    }

    public boolean checkTestingFinished(String clientDockerServiceName) {
        for (String file : testcaseRegistry.getExpectedClientOutputFiles()) {
            if (Files.notExists(Paths.get(file))) {
                return false;
            }
        }
        for (String file : testcaseRegistry.getExpectedServerOutputFiles()) {
            if (Files.notExists(Paths.get(file))) {
                return false;
            }
        }
        return !dockerService.healthCheck("", new HashMap<>(), new String[] {clientDockerServiceName});
    }

    /**
     * Check if there's a line containing a {@code searchString} message logged after {@code timestamp}.
     * Note: For this use case, we only search the last 5 lines of the log file.
     * The {@code language} parameter dictates the format we will follow when reading the log file.
     *
     * @param logFile
     * @param searchString
     * @param language
     * @param timestamp
     * @return
     */
    public boolean searchLogContentNewerThan(String logFile, String searchString, Language language, long timestamp) {
        final int linesToRead = 5;
        List<String> log = FileUtil.tail(logFile, linesToRead);
        if (Objects.nonNull(log)) {
            for (String line : log) {
                long logTime = 0;
                try {
                    switch (language) {
                        case JAVA, PYTHON ->
                                logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-dd HH:mm:ss".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC) * 1000;
                        case NODEJS ->
                                logTime = LocalDateTime.parse(line.substring(0, "yyyy-MM-ddTHH:mm:ss.SSSX".length()), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")).toEpochSecond(ZoneOffset.UTC) * 1000;
                    }
                } catch (Exception e) {
                }
                if (logTime > timestamp) {
                    if (line.contains(searchString)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the path to the latest log file of a program of type {@code programType} and language {@code language}.
     *
     * @param programType   only accepts SERVER and CLIENT
     * @param language
     * @return
     * @throws IOException
     */
    public String getLatestLogFile(ProgramType programType, Language language) throws IOException {
        String currentLogFile = "";
        String service;
        if (programType.equals(ProgramType.SERVER)) {
            service = "server";
        } else if (programType.equals(ProgramType.CLIENT)) {
            service = "client";
        } else {
            throw new IllegalArgumentException("Getting log file for program type [" + programType + "] not supported");
        }
        switch (language) {
            case JAVA -> currentLogFile = "log/java-" + service + ".log";
            case NODEJS -> currentLogFile = "log/node-" + service + "." + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            case PYTHON -> {
                List<String> pythonLogFiles = FileUtil.listFilesWithSamePrefix("log", "py-" + service);
                for (String pyLog : pythonLogFiles) {
                    if (Long.parseLong(pyLog.substring(pyLog.lastIndexOf("_") + 1, pyLog.length() - 4)) * 1000 > STARTUP_TIMESTAMP) {
                        currentLogFile = pyLog;
                        break;
                    }
                }
            }
        }
        return currentLogFile;
    }
}
