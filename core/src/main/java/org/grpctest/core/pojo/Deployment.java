package org.grpctest.core.pojo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.grpctest.core.enums.ProgramType;
import org.grpctest.core.exception.InvalidDeploymentException;

import java.util.*;

/**
 * Represent a single deployment cycle. To delete deployment, delete the whole object.
 * It's not recommended to clear every field in this object.
 */
@Data
public class Deployment {
    private TestProgram client;
    private TestProgram server;

    private final Set<TestProgram> beforeClientSupports = new HashSet<>();
    private final Set<TestProgram> afterClientSupports = new HashSet<>();
    private final Set<TestProgram> beforeServerSupports = new HashSet<>();
    private final Set<TestProgram> afterServerSupports = new HashSet<>();

    public void setClient(TestProgram client) throws InvalidDeploymentException {
        if (!client.verifyValidClient()) throw new InvalidDeploymentException("Not a valid client: " + client);
        this.client = client;
    }

    public void setServer(TestProgram server) throws InvalidDeploymentException {
        if (!server.verifyValidServer()) throw new InvalidDeploymentException("Not a valid server: " + server);
        this.server = server;
    }

    /** Overwrites existing supporting services (same dockerServiceName) if already registered */
    public void addSupportingService(TestProgram supportToInsert, ProgramType attachTo, boolean launchBeforeAttachedTarget) throws InvalidDeploymentException {
        if (!supportToInsert.verifyValidSupport()) {
            throw new InvalidDeploymentException("Not a valid supporting service: " + supportToInsert);
        }
        beforeClientSupports.removeIf(
                support ->
                        supportToInsert.getDockerServiceName().equals(support.getDockerServiceName()) ||
                                supportToInsert.getDockerContainerName().equals(support.getDockerContainerName())
        );
        afterClientSupports.removeIf(
                support ->
                        supportToInsert.getDockerServiceName().equals(support.getDockerServiceName()) ||
                                supportToInsert.getDockerContainerName().equals(support.getDockerContainerName())
        );
        beforeServerSupports.removeIf(
                support ->
                        supportToInsert.getDockerServiceName().equals(support.getDockerServiceName()) ||
                                supportToInsert.getDockerContainerName().equals(support.getDockerContainerName())
        );
        afterServerSupports.removeIf(
                support ->
                        supportToInsert.getDockerServiceName().equals(support.getDockerServiceName()) ||
                                supportToInsert.getDockerContainerName().equals(support.getDockerContainerName())
        );
        if (attachTo.equals(ProgramType.CLIENT)) {
            if (launchBeforeAttachedTarget) {
                beforeClientSupports.add(supportToInsert);
            } else {
                afterClientSupports.add(supportToInsert);
            }
        } else if (attachTo.equals(ProgramType.SERVER)) {
            if (launchBeforeAttachedTarget) {
                beforeServerSupports.add(supportToInsert);
            } else {
                afterServerSupports.add(supportToInsert);
            }
        } else {
            throw new InvalidDeploymentException("Cannot attach support to services other than Client or Server");
        }
    }

    /** Throw {@link org.grpctest.core.exception.InvalidDeploymentException} if invalid */
    public void verify() throws InvalidDeploymentException {
        if (!client.verifyValidClient()) {
            throw new InvalidDeploymentException();
        }
        if (!server.verifyValidServer()) {
            throw new InvalidDeploymentException();
        }
        List<TestProgram> supports = new ArrayList<>();
        supports.addAll(beforeClientSupports);
        supports.addAll(afterClientSupports);
        supports.addAll(beforeServerSupports);
        supports.addAll(afterServerSupports);
        for (TestProgram support : supports) {
            if (!support.verifyValidSupport()) {
                throw new InvalidDeploymentException();
            }
        }
    }

    public Set<TestProgram> listAllPrograms() {
        Set<TestProgram> allPrograms = new HashSet<>();
        allPrograms.add(client);
        allPrograms.add(server);
        allPrograms.addAll(beforeClientSupports);
        allPrograms.addAll(afterClientSupports);
        allPrograms.addAll(beforeServerSupports);
        allPrograms.addAll(afterServerSupports);
        return allPrograms;
    }

    public Set<String> listProfiles() {
        Set<String> allProfiles = new HashSet<>();
        for (TestProgram program : listAllPrograms()) {
            if (StringUtils.isNotBlank(program.getProfile())) {
                allProfiles.add(program.getProfile());
            }
        }
        return allProfiles;
    }

    public Map<String, Map<String, String>> listProfilesWithEnv() {
        Map<String, Map<String, String>> profilesWithEnv = new HashMap<>();
        for (TestProgram program : listAllPrograms()) {
            if (!profilesWithEnv.containsKey(program.getProfile())) {
                profilesWithEnv.put(program.getProfile(), new HashMap<>());
            }
            for (Map.Entry<String, String> envEntry : program.getDockerEnv().entrySet()) {
                profilesWithEnv.get(program.getProfile()).putIfAbsent(envEntry.getKey(), envEntry.getValue());
            }
        }
        return profilesWithEnv;
    }

    /**
     * Indicates when the program will be launched. Possible return values are:<br>
     * -1 = No such program in deployment   <br>
     * 0 = Before server launch             <br>
     * 1 = Server launch                    <br>
     * 2 = After server launch              <br>
     * 3 = Before client launch             <br>
     * 4 = Client launch                    <br>
     * 5 = After client launch              <br>
     *
     * @param programServiceName
     * @return
     */
    public int getLaunchOrderOfProgram(String programServiceName) {
        if (StringUtils.isBlank(programServiceName)) return -1;
        if (beforeServerSupports.stream().anyMatch(program -> StringUtils.equals(program.getDockerServiceName(), programServiceName))) {
            return 0;
        }
        if (StringUtils.equals(server.getDockerServiceName(), programServiceName)) {
            return 1;
        }
        if (afterServerSupports.stream().anyMatch(program -> StringUtils.equals(program.getDockerServiceName(), programServiceName))) {
            return 2;
        }
        if (beforeClientSupports.stream().anyMatch(program -> StringUtils.equals(program.getDockerServiceName(), programServiceName))) {
            return 3;
        }
        if (StringUtils.equals(client.getDockerServiceName(), programServiceName)) {
            return 4;
        }
        if (afterClientSupports.stream().anyMatch(program -> StringUtils.equals(program.getDockerServiceName(), programServiceName))) {
            return 5;
        }
        return -1;
    }
}
