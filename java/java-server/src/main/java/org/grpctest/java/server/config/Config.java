package org.grpctest.java.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor
public class Config {

    // Filesystem

    @Value("${tests.dir:test-cases/server}")
    private String testcaseDir;

    @Value("${out.dir:out/server}")
    private String outDir;

    /** Port that gRPC server runs on */
    @Value("${server.port}")
    private int serverPort;

}
