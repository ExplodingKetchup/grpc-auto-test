package org.grpctest.core.exception;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class ExternalProcessException extends Exception {

    private String cmd = "";
    private int exitCode = -1;
    private Path workingDir;
    private Path logLocation;

    public ExternalProcessException() {
    }

    public ExternalProcessException(String message) {
        super(message);
    }

    public ExternalProcessException(Throwable cause) {
        super(cause);
    }

    public ExternalProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalProcessException(String cmd, int exitCode, Path workingDir) {
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
    }

    public ExternalProcessException(String message, String cmd, int exitCode, Path workingDir) {
        super(message);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
    }

    public ExternalProcessException(String message, Throwable cause, String cmd, int exitCode, Path workingDir) {
        super(message, cause);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
    }

    public ExternalProcessException(Throwable cause, String cmd, int exitCode, Path workingDir) {
        super(cause);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
    }

    public ExternalProcessException(String cmd, int exitCode, Path workingDir, Path logLocation) {
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
        this.logLocation = logLocation;
    }

    public ExternalProcessException(String message, String cmd, int exitCode, Path workingDir, Path logLocation) {
        super(message);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
        this.logLocation = logLocation;
    }

    public ExternalProcessException(String message, Throwable cause, String cmd, int exitCode, Path workingDir, Path logLocation) {
        super(message, cause);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
        this.logLocation = logLocation;
    }

    public ExternalProcessException(Throwable cause, String cmd, int exitCode, Path workingDir, Path logLocation) {
        super(cause);
        this.cmd = cmd;
        this.exitCode = exitCode;
        this.workingDir = workingDir;
        this.logLocation = logLocation;
    }
}
