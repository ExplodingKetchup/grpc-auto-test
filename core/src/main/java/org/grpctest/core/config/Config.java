package org.grpctest.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.grpctest.core.enums.CleanupMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
public class Config {

    // Core Service

    /** Test setup script (File path) */
    @Value("${core.setup.script:setup/setup.txt}")
    private String setupScriptFilePath;

    /** Max waiting time for docker compose to build and launch server / client */
    @Value("${core.docker.compose.timeout.ms:180000}")
    private long dockerComposeTimeoutMillis;

    /** Max waiting time after successfully launch server + client before force quit */
    @Value("${core.test.timeout.ms:300000}")
    private long testTimeoutMillis;

    /** Max time waiting for server to start (after successful docker compose up server) before launching client */
    @Value("${core.docker.server.startup.timeout.ms:300000}")
    private long serverStartupTimeoutMillis;

    /** Cleans up temporary files before or after test, or both, or none. Check {@link CleanupMode} for details */
    @Value("${core.cleanup.mode:BEFORE}")
    private CleanupMode cleanupMode;

    /** Enable debug mode */
    @Value("${core.dev.debug.enabled:false}")
    private boolean debugEnabled;

    /** Log requests to be sent from the client (in the client log) */
    @Value("${core.test.client.log.request:true}")
    private boolean clientLogRequests;

    /** Log each fields of the requests to be sent from the client (in the client log) */
    @Value("${core.test.client.log.request.print-fields:true}")
    private boolean clientLogRequestsPrintFields;

    /** Log responses received by the client (in the client log) */
    @Value("${core.test.client.log.response:true}")
    private boolean clientLogResponses;

    /** Log each fields of the responses received by the client (in the client log) */
    @Value("${core.test.client.log.response.print-fields:true}")
    private boolean clientLogResponsesPrintFields;

    /** Log requests received by the server (in the server log) */
    @Value("${core.test.server.log.request:true}")
    private boolean serverLogRequests;

    /** Log each fields of the requests received by the server(in the server log) */
    @Value("${core.test.server.log.request.print-fields:true}")
    private boolean serverLogRequestsPrintFields;

    /** Log responses to be sent from the server (in the server log) */
    @Value("${core.test.server.log.response:true}")
    private boolean serverLogResponses;

    /** Log each fields of the responses to be sent from the server (in the server log) */
    @Value("${core.test.server.log.response.print-fields:true}")
    private boolean serverLogResponsesPrintFields;

    // Nodejs Client
    /** Log directory for NodeJs client */
    @Value("${test.nodejs.client.log.dir:log/}")
    private String nodejsClientLogDir;

    /** Log file for NodeJs client */
    @Value("${test.nodejs.client.log.file:node-client.log}")
    private String nodejsClientLogFile;

    /** Output directory for NodeJs client */
    @Value("${test.nodejs.client.out.dir:out/client/}")
    private String nodejsClientOutDir;

    /** Directory containing .proto files for nodejs client */
    @Value("${test.nodejs.client.proto.dir:proto/}")
    private String nodejsClientProtoDir;

    /** Hostname of the server to which NodeJs client connects to */
    @Value("${test.nodejs.client.server.host:NOT_ASSIGNED}")
    private String nodejsClientServerHost;

    /** Port of the server to which NodeJs client connects to */
    @Value("${test.nodejs.client.server.port:50051}")
    private int nodejsClientServerPort;

    /** Directory containing .bin test cases */
    @Value("${test.nodejs.client.tests.dir:test-cases/}")
    private String nodejsClientTestsDir;

    // Nodejs Server
    /** Log directory for NodeJs server */
    @Value("${test.nodejs.server.log.dir:log/}")
    private String nodejsServerLogDir;

    /** Log file for NodeJs server */
    @Value("${test.nodejs.server.log.file:node-server.log}")
    private String nodejsServerLogFile;

    /** Output directory for NodeJs server */
    @Value("${test.nodejs.server.out.dir:out/server/}")
    private String nodejsServerOutDir;

    /** Directory containing .proto files for nodejs server */
    @Value("${test.nodejs.server.proto.dir:proto/}")
    private String nodejsServerProtoDir;

    /** Hostname of the NodeJs server */
    @Value("${test.nodejs.server.server.host:node-server}")
    private String nodejsServerServerHost;

    /** Port of the NodeJs server */
    @Value("${test.nodejs.server.server.port:50051}")
    private int nodejsServerServerPort;

    /** Directory containing NodeJs server .bin test cases */
    @Value("${test.nodejs.server.tests.dir:test-cases/}")
    private String nodejsServerTestsDir;

    // Java Client
    /** Output directory for Java client */
    @Value("${test.java.client.out.dir:out/client}")
    private String javaClientOutDir;

    /** Hostname of the server to which Java client connects to */
    @Value("${test.java.client.server.host:NOT_ASSIGNED}")
    private String javaClientServerHost;

    /** Port of the server to which Java client connects to */
    @Value("${test.java.client.server.port:50051}")
    private int javaClientServerPort;

    /** Test cases directory for Java client */
    @Value("${test.java.client.tests.dir:test-cases/client}")
    private String javaClientTestsDir;

    // Java Server
    /** Output directory for Java server */
    @Value("${test.java.server.out.dir:out/server}")
    private String javaServerOutDir;

    /** Port of the Java server */
    @Value("${test.java.server.server.port:50051}")
    private int javaServerServerPort;

    /** Test cases directory for Java server */
    @Value("${test.java.server.tests.dir:test-cases/server}")
    private String javaServerTestsDir;

    /** Log directory for Python client */
    @Value("${test.python.client.log.dir:log}")
    private String pyClientLogDir;

    /** Log file prefix for Python client */
    @Value("${test.python.client.log.file:py-client}")
    private String pyClientLogFile;

    /** Output directory for Python client */
    @Value("${test.python.client.out.dir:out/client}")
    private String pyClientOutDir;

    /** Hostname of the server to which Python client connects to */
    @Value("${test.python.client.server.host:NOT_ASSIGNED}")
    private String pyClientServerHost;

    /** Port of the server to which Python client connects to */
    @Value("${test.python.client.server.port:50051}")
    private int pyClientServerPort;

    /** Directory containing Python client .bin test cases */
    @Value("${test.python.client.tests.dir:test-cases/client}")
    private String pyClientTestsDir;

    /** Log directory for Python server */
    @Value("${test.python.server.log.dir:log}")
    private String pyServerLogDir;

    /** Log file prefix for Python server */
    @Value("${test.python.server.log.file:py-server}")
    private String pyServerLogFile;

    /** Output directory for Python server */
    @Value("${test.python.server.out.dir:out/server}")
    private String pyServerOutDir;

    /** Port of the Python server */
    @Value("${test.python.server.server.port:50051}")
    private int pyServerServerPort;

    /** Directory containing Python server .bin test cases */
    @Value("${test.python.server.tests.dir:test-cases/server}")
    private String pyServerTestsDir;

}
