package org.grpctest.core.service.java;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.grpctest.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * This class invokes maven operations.
 */
@Component
@Getter
@AllArgsConstructor
public class MavenInvoker {

    private static final String COMMON_PATH = "/java/common";

    private static final String LOG_FILE_PREFIX = "mvn_";

    private final Config config;

    private final ExecutorService executorService;

    private List<MavenGoal> mavenGoals = new ArrayList<>();

    private Map<String, String> params = new HashMap<>();

    @Autowired
    public MavenInvoker(Config config,
                        @Qualifier("singleThread") ExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
    }

    public MavenInvoker addMvnGoal(MavenGoal mavenGoal) {
        mavenGoals.add(mavenGoal);
        return this;
    }

    public MavenInvoker addAllMvnGoals(List<MavenGoal> mavenGoals) {
        this.mavenGoals.addAll(mavenGoals);
        return this;
    }

    public MavenInvoker addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public MavenInvoker addAllParams(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    public void execute() throws Exception {
        // Get command
        String cmd = toCmd();

        // Create log files
        String filename =
                LOG_FILE_PREFIX
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
        processBuilder.directory(new File(System.getProperty("user.dir") + COMMON_PATH));
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logfile));

        // Launch process
        processBuilder.start();
    }

    private String toCmd() {
        StringBuilder cmdBuilder = new StringBuilder("mvn ");
        for (MavenGoal mavenGoal : mavenGoals) {
            cmdBuilder.append(mavenGoal.mvnCmd).append(" ");
        }
        for (Map.Entry<String, String> param : params.entrySet()) {
            cmdBuilder.append("-").append(param.getKey());
            if (StringUtils.isNotBlank(param.getValue())) {
                cmdBuilder.append(" ").append(param.getValue());
            }
            cmdBuilder.append(" ");
        }
        cmdBuilder.deleteCharAt(cmdBuilder.length() - 1);
        return cmdBuilder.toString();
    }

    public enum MavenGoal {
        CLEAN("clean"),
        VALIDATE("validate"),
        COMPILE("compile"),
        TEST("test"),
        PACKAGE("package"),
        VERIFY("verify"),
        INSTALL("install"),
        DEPLOY("deploy");

        private final String mvnCmd;

        MavenGoal(String mvnCmd) {
            this.mvnCmd = mvnCmd;
        }
    }
}
