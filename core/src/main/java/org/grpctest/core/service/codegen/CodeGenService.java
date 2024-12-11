package org.grpctest.core.service.codegen;

public interface CodeGenService {
    default void generateAllFiles() throws Exception {
        generateClient();
        generateServer();
    }

    /** Generate all files related to server */
    void generateServer() throws Exception;

    /** Generate all files related to server */
    void generateClient() throws Exception;
}
