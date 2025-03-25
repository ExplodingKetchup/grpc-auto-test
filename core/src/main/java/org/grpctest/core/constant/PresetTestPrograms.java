package org.grpctest.core.constant;

import lombok.Getter;
import org.grpctest.core.config.Config;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.ProgramType;
import org.grpctest.core.pojo.TestProgram;
import org.grpctest.core.service.util.TestProgramUtilService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.grpctest.core.constant.Constants.TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS;

@Component
@Getter
public class PresetTestPrograms {

    private final TestProgram javaClient;
    private final TestProgram javaServer;
    private final TestProgram nodeClient;
    private final TestProgram nodeServer;
    private final TestProgram pyClient;
    private final TestProgram pyServer;
    private final TestProgram tcpdump;

    public PresetTestPrograms(Config config, TestProgramUtilService testProgramUtilService) {
        this.javaClient = new TestProgram("java-client", "java-client-container",ProgramType.CLIENT, Language.JAVA, "", false, null, -1, -1);
        this.javaServer = new TestProgram("java-server", "java-server-container", ProgramType.SERVER, Language.JAVA, "", true, () -> testProgramUtilService.checkServerRunning(Language.JAVA), TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS, config.getServerStartupTimeoutMillis());
        this.nodeClient = new TestProgram("node-client", "node-client-container", ProgramType.CLIENT, Language.NODEJS, "", false, null, -1, -1);
        this.nodeServer = new TestProgram("node-server", "node-server-container", ProgramType.SERVER, Language.NODEJS, "", true, () -> testProgramUtilService.checkServerRunning(Language.NODEJS), TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS, config.getServerStartupTimeoutMillis());
        this.pyClient = new TestProgram("py-client", "py-client-container", ProgramType.CLIENT, Language.PYTHON, "", false, null, -1, -1);
        this.pyServer = new TestProgram("py-server", "py-server-container", ProgramType.SERVER, Language.PYTHON, "", true, () -> testProgramUtilService.checkServerRunning(Language.PYTHON), TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS, config.getServerStartupTimeoutMillis());
        this.tcpdump = new TestProgram("tcpdump", "tcpdump-container", ProgramType.SUPPORT, Language.NOT_APPLICABLE, "tcpdump_enabled", true, null, -1, -1);
    }

    public TestProgram lookupTestProgramByServiceName(String serviceName) {
        return switch (serviceName) {
            case "java-client" -> javaClient;
            case "java-server" -> javaServer;
            case "node-client" -> nodeClient;
            case "node-server" -> nodeServer;
            case "py-client" -> pyClient;
            case "py-server" -> pyServer;
            case "tcpdump" -> tcpdump;
            default -> null;
        };
    }

    public TestProgram lookupClientByLanguage(Language language) {
        return switch (language) {
            case JAVA -> javaClient;
            case NODEJS -> nodeClient;
            case PYTHON -> pyClient;
            case NOT_APPLICABLE -> null;
        };
    }

    public TestProgram lookupServerByLanguage(Language language) {
        return switch (language) {
            case JAVA -> javaServer;
            case NODEJS -> nodeServer;
            case PYTHON -> pyServer;
            case NOT_APPLICABLE -> null;
        };
    }
}
