package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.service.util.ExternalProcessUtilService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class DockerService {

    private static final String WORKING_DIR = "./";     // Where the compose.yaml file is
    private static final String LOG_FILE_PREFIX = "docker_";
    private static final String CMD_DOCKER_COMPOSE_UP = "docker compose up";

    private final ExternalProcessUtilService externalProcessUtilService;

    public void dockerComposeUp() {
        try {
            externalProcessUtilService.execute(WORKING_DIR, CMD_DOCKER_COMPOSE_UP, LOG_FILE_PREFIX, true);
        } catch (Exception e) {
            log.error("[dockerComposeUp] Failed to run \"docker compose up\" at dir {}", WORKING_DIR);
        }
    }
}
