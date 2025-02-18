package org.grpctest.core.exception;

import java.nio.file.Path;

public class DockerException extends ExternalProcessException {

    public DockerException() {
    }

    public DockerException(String message) {
        super(message);
    }

    public DockerException(Throwable cause) {
        super(cause);
    }

    public DockerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerException(String cmd, Path workingDir) {
        super(cmd, -1, workingDir);
    }

    public DockerException(String message, String cmd, Path workingDir) {
        super(message, cmd, -1, workingDir);
    }

    public DockerException(String message, Throwable cause, String cmd, Path workingDir) {
        super(message, cause, cmd, -1, workingDir);
    }

    public DockerException(Throwable cause, String cmd, Path workingDir) {
        super(cause, cmd, -1, workingDir);
    }

}
