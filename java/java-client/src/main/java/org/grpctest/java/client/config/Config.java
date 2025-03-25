package org.grpctest.java.client.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config {

    // Filesystem
    @Value("${tests.dir:test-cases/client}")
    private String testcaseDir;

    @Value("${out.dir:out/client}")
    private String outDir;

    // Service discovery

    @Value("${server.host:localhost}")
    private String serviceHost;

    @Value("${server.port:50051}")
    private Integer servicePort;
}
