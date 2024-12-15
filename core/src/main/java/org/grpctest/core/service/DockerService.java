package org.grpctest.core.service;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.service.util.ExternalProcessUtilService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DockerService {

    private static final String WORKING_DIR = "./";     // Where the compose.yaml file is
    private static final String LOG_FILE_PREFIX = "docker_";
    private static final String CMD_DOCKER_COMPOSE_UP = "docker compose -f compose.yaml -p grpc-auto-test up -d";
    private static final String CMD_DOCKER_COMPOSE_DOWN = "docker compose down --rmi all";
    private static final String CMD_DOCKER_COMPOSE_PS = "docker compose ps --services";
    private static final List<String> SUPPORTED_SERVICES = Lists.newArrayList("java-client", "java-server", "node-client", "node-server");
    private static final long CONTAINER_HEALTH_CHECK_INTERVAL_MS = 1000;
    private static final int STABLE_THRESHOLD_INTERVALS = 5;    // After this many consecutive successful health checks, service can be considered stable

    private Config config;

    private final ExternalProcessUtilService externalProcessUtilService;

    public void dockerComposeUp() throws Throwable {
        try {
            externalProcessUtilService.execute(WORKING_DIR, CMD_DOCKER_COMPOSE_UP, LOG_FILE_PREFIX, true);
        } catch (Exception e) {
            log.error("[dockerComposeUp] Failed to run \"docker compose up\" at dir {}", WORKING_DIR);
            throw e;
        }
    }

    public void dockerComposeUpSpecifyServices(String... services) throws Exception {
        String validServices = Arrays.stream(services).filter(SUPPORTED_SERVICES::contains).collect(Collectors.joining(" "));
        String cmd = String.join( " ", CMD_DOCKER_COMPOSE_UP, validServices);
        try {
            // Launch services
            externalProcessUtilService.execute(WORKING_DIR, cmd, LOG_FILE_PREFIX, false);

            // Monitor services periodically
            long currentTime = System.currentTimeMillis();
            long waitUntil = currentTime + config.getDockerComposeTimeoutMillis();
            int consecutiveSuccessHealthChecks = 0;

            while (currentTime < waitUntil) {
                if (healthCheck(validServices.split(" "))) {
                    consecutiveSuccessHealthChecks++;
                    if (consecutiveSuccessHealthChecks >= STABLE_THRESHOLD_INTERVALS) {
                        log.info("[dockerComposeUpSpecifyServices] Successfully launched service [{}]", (Object) services);
                        return;
                    }
                } else {
                    consecutiveSuccessHealthChecks = 0;
                }
                Thread.sleep(CONTAINER_HEALTH_CHECK_INTERVAL_MS);
                currentTime = System.currentTimeMillis();
            }

            // Force quit on timeout
            dockerComposeDown();
            throw new Exception("Docker compose up for services [" + validServices + "] failed. Cmd: [" + cmd + "] at dir [" + WORKING_DIR + "]");
        } catch (Exception e) {
            log.error("[dockerComposeUpSpecifyServices] Failed to run [{}] at dir [{}]", cmd, WORKING_DIR);
            throw e;
        }
    }

    public void dockerComposeDown() throws Exception {
        try {
            externalProcessUtilService.execute(WORKING_DIR, CMD_DOCKER_COMPOSE_DOWN, LOG_FILE_PREFIX, true);
        } catch (Exception e) {
            log.error("[dockerComposeDown] Failed to run \"docker compose down\" at dir {}", WORKING_DIR);
            throw e;
        }
    }

    private boolean healthCheck(String[] services) throws Exception {
        try {
            List<String> runningServices = List.of(externalProcessUtilService.executeAndReturnOutput(WORKING_DIR, CMD_DOCKER_COMPOSE_PS).split("\n"));
            for (String service : services) {
                if (!runningServices.contains(service)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("[healthCheck] Failed to run health check");
            throw e;
        }
    }
}
