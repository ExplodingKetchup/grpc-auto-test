package org.grpctest.core.service.util;

import org.grpctest.core.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalProcessUtilServiceTest {

    @Test
    void executeAndReturnOutput() {
        ExternalProcessUtilService externalProcessUtilService = new ExternalProcessUtilService(new Config());
        try {
            String output = externalProcessUtilService.executeAndReturnOutput("./", "ls");
            System.out.println(output);
        } catch (Exception e) {
            System.out.println("Got exception: " + e);
        }
    }
}