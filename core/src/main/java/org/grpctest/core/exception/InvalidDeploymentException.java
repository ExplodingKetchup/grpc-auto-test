package org.grpctest.core.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidDeploymentException extends Exception {
    public InvalidDeploymentException(String message) {
        super(message);
    }
}
