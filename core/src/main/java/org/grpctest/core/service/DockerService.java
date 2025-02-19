package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.enums.Language;
import org.grpctest.core.exception.DockerException;
import org.grpctest.core.exception.ExternalProcessException;
import org.grpctest.core.service.util.ExternalProcessUtilService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class DockerService {

    private static final String WORKING_DIR = "./";     // Where the compose.yaml file is
    private static final Path WORKING_DIR_PATH = Path.of(WORKING_DIR);
    private static final String LOG_FILE_PREFIX = "docker_";
    private static final String CMD_DOCKER_COMPOSE_UP = "%s docker compose -f compose.yaml -p grpc-auto-test %s up -d %s";
    private static final String CMD_DOCKER_COMPOSE_DOWN = "%s docker compose %s down --rmi all";
    private static final String CMD_DOCKER_COMPOSE_PS = "%s docker compose %s ps --services";
//    private static final List<String> SUPPORTED_SERVICES = Stream.concat(Language.CLIENT_NAME.values().stream(), Language.SERVER_NAME.values().stream()).toList();
    private static final long CONTAINER_HEALTH_CHECK_INTERVAL_MS = 1000;
    private static final int STABLE_THRESHOLD_INTERVALS = 5;    // After this many consecutive successful health checks, service can be considered stable

    private Config config;

    private final ExternalProcessUtilService externalProcessUtilService;

    public void dockerComposeUp() throws DockerException {
        try {
            externalProcessUtilService.execute(WORKING_DIR, CMD_DOCKER_COMPOSE_UP, LOG_FILE_PREFIX, true);
        } catch (ExternalProcessException epe) {
            log.error("[dockerComposeUp]", epe);
            throw new DockerException(epe, CMD_DOCKER_COMPOSE_UP, WORKING_DIR_PATH);
        }
    }

    public void dockerComposeUpSpecifyServices(String profile, List<String> services, Map<String, String> env, boolean monitorUntilStable) throws DockerException {
        String validServices = services.stream().collect(Collectors.joining(" "));
        if (StringUtils.isBlank(validServices)) {
            log.warn("[dockerComposeUpSpecifyServices] No known services are specified. Will do nothing.");
            return;
        }
        StringBuilder envBuilder = new StringBuilder();
        for (Map.Entry<String, String> envVar : env.entrySet()) {
            envBuilder.append(envVar.getKey()).append("=").append(envVar.getValue()).append(" ");
        }
        String profileFlag = StringUtils.isNotBlank(profile) ? ("--profile " + profile) : "";
        String cmd = String.format(CMD_DOCKER_COMPOSE_UP, envBuilder, profileFlag, validServices).trim();

        try {
            // Launch services
            externalProcessUtilService.execute(WORKING_DIR, cmd, LOG_FILE_PREFIX, false);
            if (!monitorUntilStable) {
                log.info("[dockerComposeUpSpecifyServices] Successfully launched service [{}]", services);
                return;
            }

            // Monitor services periodically
            long currentTime = System.currentTimeMillis();
            long waitUntil = currentTime + config.getDockerComposeTimeoutMillis();
            int consecutiveSuccessHealthChecks = 0;

            while (currentTime < waitUntil) {
                if (healthCheck(profile, env, validServices.split(" "))) {
                    consecutiveSuccessHealthChecks++;
                    if (consecutiveSuccessHealthChecks >= STABLE_THRESHOLD_INTERVALS) {
                        log.info("[dockerComposeUpSpecifyServices] Successfully launched service [{}]", services);
                        return;
                    }
                } else {
                    consecutiveSuccessHealthChecks = 0;
                }
                Thread.sleep(CONTAINER_HEALTH_CHECK_INTERVAL_MS);
                currentTime = System.currentTimeMillis();
            }

            // Force quit on timeout
            dockerComposeDown(profile, env);
            throw new DockerException("Docker compose up for services [" + validServices + "] failed.", cmd, WORKING_DIR_PATH);
        } catch (ExternalProcessException epe) {
            log.error("[dockerComposeUpSpecifyServices]", epe);
            throw new DockerException(epe, cmd, WORKING_DIR_PATH);
        } catch (InterruptedException ie) {
            log.error("[dockerComposeUpSpecifyServices] Thread is interrupted while performing health check", ie);
            throw new DockerException("Thread is interrupted while performing health check", ie, cmd, WORKING_DIR_PATH);
        }
    }

    public void dockerComposeDown(String profile, Map<String, String> env) throws DockerException {
        try {
            StringBuilder envBuilder = new StringBuilder();
            for (Map.Entry<String, String> envVar : env.entrySet()) {
                envBuilder.append(envVar.getKey()).append("=").append(envVar.getValue()).append(" ");
            }
            String profileFlag = StringUtils.isNotBlank(profile) ? ("--profile " + profile) : "";
            String cmd = String.format(CMD_DOCKER_COMPOSE_DOWN, envBuilder, profileFlag).trim();
            externalProcessUtilService.execute(WORKING_DIR, cmd, LOG_FILE_PREFIX, true);
        } catch (ExternalProcessException epe) {
            log.error("[dockerComposeDown]", epe);
            throw new DockerException(epe, CMD_DOCKER_COMPOSE_DOWN, WORKING_DIR_PATH);
        }
    }

    public boolean healthCheck(String profile, Map<String, String> env, String[] services) {
        try {
            StringBuilder envBuilder = new StringBuilder();
            for (Map.Entry<String, String> envVar : env.entrySet()) {
                envBuilder.append(envVar.getKey()).append("=").append(envVar.getValue()).append(" ");
            }
            String profileFlag = StringUtils.isNotBlank(profile) ? ("--profile " + profile) : "";
            String cmd = String.format(CMD_DOCKER_COMPOSE_PS, envBuilder, profileFlag).trim();
            List<String> runningServices = List.of(externalProcessUtilService.executeAndReturnOutput(WORKING_DIR, cmd).split("\n"));
            for (String service : services) {
                if (!runningServices.contains(service)) {
                    return false;
                }
            }
            return true;
        } catch (ExternalProcessException epe) {
            log.error("[healthCheck] Failed to run health check", epe);
            return false;
        }
    }
}
