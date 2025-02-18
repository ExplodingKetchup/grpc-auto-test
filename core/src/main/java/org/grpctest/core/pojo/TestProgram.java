package org.grpctest.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.enums.Language;
import org.grpctest.core.enums.ProgramType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@AllArgsConstructor
@Data
public class TestProgram {

    private final String dockerServiceName;

    private final String dockerContainerName;

    private final ProgramType programType;

    private final Language language;

    private final String profile;

    private final Map<String, String> dockerEnv = new HashMap<>();    // Env for docker, not env of the containerized program

    private final boolean monitorUntilStable;

    /**
     * After launch of this test program, waits until waitFunction returns true or timeout, whichever is earlier.<br>
     * If waitFunction not specified, waits until timeout (if waitTimeout specified), or does not wait (if waitTimeout
     * negative).
     */
    private final Supplier<Boolean> waitFunction;

    /**
     * Polling interval to check waitFunction (ms), default 1000 ms. Put a negative value to use default value.
     * Not applicable (ignored) if waitFunction is not specified.
     */
    private final long waitPollInterval;

    /**
     * After launch of this test program, waits until waitFunction returns true or timeout, whichever is earlier.<br>
     * Unit: ms <br>
     * Default value: 3_600_000L <br>
     * Put a negative value to use default value (if waitFunction is specified), or does not wait (if waitFunction is
     * not specified).
     */
    private final long waitTimeout;

    public boolean verify() {
        if (StringUtils.isBlank(dockerServiceName) ||
                StringUtils.isBlank(dockerContainerName) ||
                Objects.isNull(programType) ||
                Objects.isNull(language) ||
                Objects.isNull(profile)
        ) {
            return false;
        }
        if (programType.equals(ProgramType.CLIENT) || programType.equals(ProgramType.SERVER)) {
            return !language.equals(Language.NOT_APPLICABLE);
        }
        return true;
    }

    public boolean verifyValidClient() {
        return this.verify() && this.getProgramType().equals(ProgramType.CLIENT);
    }

    public boolean verifyValidServer() {
        return this.verify() && this.getProgramType().equals(ProgramType.SERVER);
    }

    public boolean verifyValidSupport() {
        return this.verify() && this.getProgramType().equals(ProgramType.SUPPORT);
    }
}
