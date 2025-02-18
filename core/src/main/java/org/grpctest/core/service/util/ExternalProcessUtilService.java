package org.grpctest.core.service.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.exception.ExternalProcessException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.grpctest.core.constant.Constants.LOG_DIR;

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
    public void execute(String workingDir, String cmd, String logFilePrefix, boolean wait) throws ExternalProcessException {
        // Get working directory
        String resolvedWorkingDir = StringUtils.startsWith(workingDir, "./") ? (System.getProperty("user.dir") + workingDir.substring(1)) : workingDir;

        // Create log files
        String filename =
                logFilePrefix
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"))
                        + ".log";
        String filepath;
        filepath = LOG_DIR + File.separator + filename;
        File logfile = new File(filepath);
        try {
            logfile.createNewFile();
        } catch (IOException ioe) {
            log.error("[execute] Failed to create log file at " + filepath, ioe);
            throw new ExternalProcessException("Failed to create log file at " + filepath, ioe);
        }

        // Create process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", cmd);
        processBuilder.directory(new File(resolvedWorkingDir));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logfile));

        // Launch process
        Path logLocation = Path.of(filepath);
        Path workingDirLocation = Path.of(resolvedWorkingDir);
        try {
            Process process = processBuilder.start();
            if (wait) {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("[execute] External process [{}], working dir [{}], failed with exit code {}", cmd, resolvedWorkingDir, exitCode);
                    throw new ExternalProcessException(cmd, exitCode, workingDirLocation, logLocation);
                }
            }
        } catch (IOException ioe) {
            log.error("[execute] Failed to launch external process [{}], working dir [{}]", cmd, resolvedWorkingDir, ioe);
            throw new ExternalProcessException(ioe, cmd, -1, workingDirLocation, logLocation);
        } catch (InterruptedException ie) {
            log.error("[execute] Thread is interrupted while waiting for external process [{}], working dir [{}]", cmd, resolvedWorkingDir, ie);
            throw new ExternalProcessException("Thread is interrupted while waiting for external process", ie, cmd, -1, workingDirLocation, logLocation);
        }
    }

    public String executeAndReturnOutput(String workingDir, String cmd) throws ExternalProcessException {
        // Get working directory
        String resolvedWorkingDir = StringUtils.startsWith(workingDir, "./") ? (System.getProperty("user.dir") + workingDir.substring(1)) : workingDir;

        // Create process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", cmd);
        processBuilder.directory(new File(resolvedWorkingDir));
        processBuilder.redirectErrorStream(true);

        // Launch process
        Path workingDirLocation = Path.of(resolvedWorkingDir);
        try {
            Process process = processBuilder.start();
            InputStream processInputStream = process.getInputStream();
            int exitCode = process.waitFor();
            String out = new BufferedReader(
                    new InputStreamReader(processInputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            if (exitCode != 0) {
                log.error("[executeAndReturnOutput] External process {} failed with exit code {}", cmd, exitCode);
                throw new ExternalProcessException(out, cmd, exitCode, workingDirLocation);
            } else {
                return out;
            }
        } catch (IOException ioe) {
            log.error("[executeAndReturnOutput]", ioe);
            throw new ExternalProcessException(ioe, cmd, -1, workingDirLocation);
        } catch (InterruptedException ie) {
            log.error("[executeAndReturnOutput] Thread is interrupted while waiting for external process", ie);
            throw new ExternalProcessException("Thread is interrupted while waiting for external process", ie, cmd, -1, workingDirLocation);
        }
    }
}
