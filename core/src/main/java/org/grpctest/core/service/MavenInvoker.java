package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.service.util.ExternalProcessUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * This class invokes maven operations.
 */
@Slf4j
@Component
@Getter
@AllArgsConstructor
public class MavenInvoker {

    // Some known locations to run maven
    private static final String JAVA_COMMON = "./java/common";
    private static final String JAVA_CLIENT = "./java/java-client";
    private static final String JAVA_SERVER = "./java/java-server";

    private static final String LOG_FILE_PREFIX = "mvn_";

    private final Config config;

    private final ExternalProcessUtilService externalProcessUtilService;

    private String workingDir;

    private List<MavenGoal> mavenGoals = new ArrayList<>();

    private Map<String, String> params = new HashMap<>();

    @Autowired
    public MavenInvoker(Config config, ExternalProcessUtilService externalProcessUtilService) {
        this.config = config;
        this.externalProcessUtilService = externalProcessUtilService;
    }

    public MavenInvoker addMvnGoal(MavenGoal mavenGoal) {
        mavenGoals.add(mavenGoal);
        return this;
    }

    /**
     * Set working dir to run mvn in, relative to the base directory (grpc-auto-test/).
     * If not set, run mvn at base directory.
     * @param workingDir
     * @return
     */
    public MavenInvoker setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
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

    /**
     * Executes the mvn command.
     * @param name  name given to this execution. Used to name log file for easy navigation
     * @throws Exception
     */
    public void execute(String name) throws Exception {
        // Get command
        String cmd = toCmd();

        // Create log files
        String logFilePrefix = LOG_FILE_PREFIX + (StringUtils.isNotBlank(name) ? (name + "_") : "");

        // Launch process
        externalProcessUtilService.execute(workingDir, cmd, logFilePrefix, true);

        // Clear data
        clear();
    }

    public void buildCommon() {
        try {
            this
                    .setWorkingDir(JAVA_COMMON)
                    .addMvnGoal(MavenGoal.CLEAN)
                    .addMvnGoal(MavenGoal.INSTALL)
                    .addParam("DskipTests", "")
                    .execute("common");
        } catch (Exception e) {
            log.error("[defaultBuild] An error occurred", e);
        }
    }

    public void buildClientServer() {
        try {
            this.
                    setWorkingDir(JAVA_CLIENT)
                    .addMvnGoal(MavenGoal.CLEAN)
                    .addMvnGoal(MavenGoal.PACKAGE)
                    .addParam("DskipTests", "")
                    .execute("client");

            this.
                    setWorkingDir(JAVA_SERVER)
                    .addMvnGoal(MavenGoal.CLEAN)
                    .addMvnGoal(MavenGoal.PACKAGE)
                    .addParam("DskipTests", "")
                    .execute("server");
        } catch (Exception e) {
            log.error("[buildClientServer] An error occurred", e);
        }
    }

    private void clear() {
        workingDir = "";
        mavenGoals.clear();
        params.clear();
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
