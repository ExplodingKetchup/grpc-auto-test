package org.grpctest.core.service.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.config.Config;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ExternalProcessUtilService {

    private final Config config;

    /**
     * Launch a shell process to execute command {@code cmd} at {@code workingDir},
     * also generates a log file for the process at log/{@code logFilePrefix}_yyMMdd_HHmmss.log
     * @param workingDir    If relative to grpc-auto-test, supply the value starting with {@literal ./}
     *                      If absolute path, workingDir can start with {@literal /}
     * @param cmd
     * @param wait
     * @param logFilePrefix
     */
    public void execute(String workingDir, String cmd, String logFilePrefix, boolean wait) throws Exception {
        // Get working directory
        String resolvedWorkingDir = StringUtils.startsWith(workingDir, "./") ? (System.getProperty("user.dir") + workingDir.substring(1)) : workingDir;

        // Create log files
        String filename =
                logFilePrefix
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"))
                        + ".log";
        String filepath;
        if (config.getLogDir().endsWith("/")) {
            filepath = config.getLogDir() + filename;
        } else {
            filepath = config.getLogDir() + "/" + filename;
        }
        File logfile = new File(filepath);
        logfile.createNewFile();

        // Create process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", cmd);
        processBuilder.directory(new File(resolvedWorkingDir));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logfile));

        // Launch process
        Process process = processBuilder.start();
        if (wait) {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception("External process [" + cmd + "] failed with exit code " + exitCode);
            }
        }
    }

    public String executeAndReturnOutput(String workingDir, String cmd) throws Exception {
        // Get working directory
        String resolvedWorkingDir = StringUtils.startsWith(workingDir, "./") ? (System.getProperty("user.dir") + workingDir.substring(1)) : workingDir;

        // Create process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", cmd);
        processBuilder.directory(new File(resolvedWorkingDir));
        processBuilder.redirectErrorStream(true);

        // Launch process
        Process process = processBuilder.start();
        InputStream processInputStream = process.getInputStream();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("External process [" + cmd + "] failed with exit code " + exitCode);
        } else {
            return new BufferedReader(
                    new InputStreamReader(processInputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }
}
