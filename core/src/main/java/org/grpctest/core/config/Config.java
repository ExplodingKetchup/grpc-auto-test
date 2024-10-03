package org.grpctest.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor
public class Config {

    /** Log directory */
    @Value("${core.log.dir}")
    private String logDir;

    /** Proto descriptor file (classpath) */
    @Value("${core.proto.descriptor.path}")
    private String protoDescriptorPath;

    /** Custom test case location */
    @Value("${core.custom-tests.classpath:tests}")
    private String customTestsClasspath;

    /** Proto file location */
    @Value("${core.proto.path:proto}")
    private String protoClasspath;

    /** Test case directory (In binary form, for client and server to access) */
    @Value("${core.tests.dir:test-cases}")
    private String testsDir;

    // Dev option
    /** Enable debug mode */
    @Value("${core.dev.debug.enabled:false}")
    private boolean debugEnabled;
}
