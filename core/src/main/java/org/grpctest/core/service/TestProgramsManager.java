package org.grpctest.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.constant.PresetTestPrograms;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.ProgramType;
import org.grpctest.core.exception.DockerException;
import org.grpctest.core.exception.ExternalProcessException;
import org.grpctest.core.exception.InvalidDeploymentException;
import org.grpctest.core.pojo.Deployment;
import org.grpctest.core.pojo.TestProgram;
import org.grpctest.core.service.util.TestProgramUtilService;
import org.grpctest.core.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.grpctest.core.constant.Constants.TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS;
import static org.grpctest.core.constant.Constants.TEST_PROGRAM_MAX_WAIT_TIMEOUT_MS;

/**
 * Manage the run lifecycle of test programs and supporting services (build docker, launch,
 * and track completion).
 * <br>
 * Note: We assume that supporting services are only dependent on test programs, not on each
 * other.
 */
@Slf4j
@Service
public class TestProgramsManager {

    private final Config config;
    private final MavenInvoker mavenInvoker;
    private final DockerService dockerService;
    private final PresetTestPrograms presetTestPrograms;
    private final TestProgramUtilService testProgramUtilService;

    private Deployment deployment;

    public TestProgramsManager(Config config, MavenInvoker mavenInvoker, DockerService dockerService, PresetTestPrograms presetTestPrograms, TestProgramUtilService testProgramUtilService) {
        this.config = config;
        this.mavenInvoker = mavenInvoker;
        this.dockerService = dockerService;
        this.presetTestPrograms = presetTestPrograms;
        this.testProgramUtilService = testProgramUtilService;
    }

    /** Overwrite existing client (if already set) */
    public void addClient(Language language) {
        try {
            switch (language) {
                case JAVA -> deployment.setClient(presetTestPrograms.getJavaClient());
                case NODEJS -> deployment.setClient(presetTestPrograms.getNodeClient());
                case PYTHON -> deployment.setClient(presetTestPrograms.getPyClient());
            }
        } catch (InvalidDeploymentException ide) {
            log.error("[addClient(Language)]", ide);
        }
    }

    /** Overwrite existing server (if already set) */
    public void addServer(Language language) {
        try {
            switch (language) {
                case JAVA -> deployment.setServer(presetTestPrograms.getJavaServer());
                case NODEJS -> deployment.setServer(presetTestPrograms.getNodeServer());
                case PYTHON -> deployment.setServer(presetTestPrograms.getPyServer());
            }
        } catch (InvalidDeploymentException ide) {
            log.error("[addServer(Language)]", ide);
        }
    }

    /**
     * Delegate of {@link TestProgramsManager#attachSupportingService(TestProgram, ProgramType, boolean)}
     * @param supportDockerServiceName
     * @param position when to launch supporting service (0 = before server, 1 = after server, 2 = before client, 3 = after client)
     */
    public void attachSupportingService(String supportDockerServiceName, int position) {
        attachSupportingService(
                presetTestPrograms.lookupTestProgramByServiceName(supportDockerServiceName),
                position <= 1 ? ProgramType.SERVER : ProgramType.CLIENT,
                position % 2 == 0   // I.e. 0 or 2
        );
    }

    /**
     * Add supporting service to launch sequence. Note that currently we only support 1 run per
     * deployment per supporting service. Will overwrite added supporting services with the same
     * docker service name and/or docker container name.
     *
     * @param supportingService
     * @param attachTo Only take {@link ProgramType#CLIENT} or {@link ProgramType#SERVER}
     * @param launchBeforeAttachedTarget true if service is to be launched before attached target, false if after.
     */
    public void attachSupportingService(TestProgram supportingService, ProgramType attachTo, boolean launchBeforeAttachedTarget) {
        try {
            deployment.addSupportingService(supportingService, attachTo, launchBeforeAttachedTarget);
        } catch (InvalidDeploymentException ide) {
            log.error("[attachSupportingService]", ide);
        }
    }

    public void resetDeployment() {
        deployment = new Deployment();
    }

    public void deploy() throws InvalidDeploymentException, ExternalProcessException, InterruptedException, TimeoutException {
        boolean dockerLaunched = false;     // Flag to see if cleanup's necessary
        try {
            // Preparation
            deployment.verify();
            resolveDockerEnv();
            log.info("[deploy] Deployment info verified");

            // Build and launch
            dockerLaunched = true;
            log.info("[deploy] Deploying server and supporting services");
            for (TestProgram program : deployment.getBeforeServerSupports()) {
                buildAndLaunchProgram(program);
            }
            buildAndLaunchProgram(deployment.getServer());
            log.info("[deploy] Server deployed");
            for (TestProgram program : deployment.getAfterServerSupports()) {
                buildAndLaunchProgram(program);
            }
            log.info("[deploy] Deploying client and supporting services");
            for (TestProgram program : deployment.getBeforeClientSupports()) {
                buildAndLaunchProgram(program);
            }
            buildAndLaunchProgram(deployment.getClient());
            log.info("[deploy] Client deployed");
            for (TestProgram program : deployment.getAfterClientSupports()) {
                buildAndLaunchProgram(program);
            }

            // Wait until testing is finished before cleaning up
            TimeUtil.pollForCondition(
                    () -> testProgramUtilService.checkTestingFinished(deployment.getClient().getDockerServiceName()),
                    TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS,
                    config.getTestTimeoutMillis()
            );
            Thread.sleep(5000);
            log.info("[deploy] Deployment finished successfully. Cleaning up...");

        } finally {
            if (dockerLaunched) {
                try {
                    cleanup();
                } catch (Throwable t) {
                    log.warn("[deploy] Cleanup failed. Please perform manual cleanup", t);
                }
            }
            resetDeployment();
            log.info("[deploy] Cleanup finished");
        }
    }

    private void resolveDockerEnv() {
        Map<String, Map<String, String>> dockerEnvByProfile = new HashMap<>();

        for (String profile : deployment.listProfiles()) {
            switch (profile) {
                case "tcpdump_enabled": {
                    int tcpdumpLaunchOrder = deployment.getLaunchOrderOfProgram("tcpdump");
                    Map<String, String> dockerEnv;
                    if (tcpdumpLaunchOrder == 2) {
                        dockerEnv = Map.of("TCPDUMP_ATTACHED_SERVICE", deployment.getServer().getDockerServiceName());
                    } else if (tcpdumpLaunchOrder == 5) {
                        dockerEnv = Map.of("TCPDUMP_ATTACHED_SERVICE", deployment.getClient().getDockerServiceName());
                    } else {
                        dockerEnv = new HashMap<>();
                        log.warn("[resolveDockerEnv] Cannot resolve [TCPDUMP_ATTACHED_SERVICE], tcpdump might be registered incorrectly, launch order = {}", tcpdumpLaunchOrder);
                    }
                    dockerEnvByProfile.put("tcpdump_enabled", dockerEnv);
                }
            }
        }

        for (TestProgram program : deployment.listAllPrograms()) {
            if (StringUtils.isNotBlank(program.getProfile())) {
                program.getDockerEnv().putAll(dockerEnvByProfile.get(program.getProfile()));
            }
        }
    }

    private void buildAndLaunchProgram(TestProgram testProgram) throws ExternalProcessException, InterruptedException, TimeoutException {
        if (testProgram.getLanguage().equals(Language.JAVA)) {
            if (testProgram.getProgramType().equals(ProgramType.SERVER)) {
                mavenInvoker.buildServer();
            } else if (testProgram.getProgramType().equals(ProgramType.CLIENT)) {
                mavenInvoker.buildClient();
            }
        }
        dockerService.dockerComposeUpSpecifyServices(
                testProgram.getProfile(),
                Collections.singletonList(testProgram.getDockerServiceName()),
                testProgram.getDockerEnv(),
                testProgram.isMonitorUntilStable()
        );

        // Execute wait function for launched program
        if (Objects.nonNull(testProgram.getWaitFunction())) {
            long pollInterval = testProgram.getWaitPollInterval() >= 0 ? testProgram.getWaitPollInterval() : TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS;
            long timeout = testProgram.getWaitTimeout() >= 0 ? testProgram.getWaitTimeout() : TEST_PROGRAM_MAX_WAIT_TIMEOUT_MS;
            TimeUtil.pollForCondition(testProgram.getWaitFunction(), pollInterval, timeout);
        } else if (testProgram.getWaitTimeout() >= 0) {
            Thread.sleep(testProgram.getWaitTimeout());
        }
    }

    private void cleanup() throws DockerException {
        for (Map.Entry<String, Map<String, String>> profileEnvEntry : deployment.listProfilesWithEnv().entrySet()) {
            dockerService.dockerComposeDown(profileEnvEntry.getKey(), profileEnvEntry.getValue());
        }
    }

}
