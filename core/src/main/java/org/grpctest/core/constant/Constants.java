package org.grpctest.core.constant;

import org.springframework.beans.factory.annotation.Value;

/** A class containing constants which should be available globally, or are shared by multiple classes */
public class Constants {

    public static final long TEST_PROGRAM_MAX_WAIT_TIMEOUT_MS = 3600000L;

    public static final long TEST_PROGRAM_DEFAULT_POLL_INTERVAL_MS = 1000L;

    /** Custom test case location */
    public static final String CUSTOM_TESTS_CLASSPATH = "tests";

    /** Log directory */
    public static final String LOG_DIR = "./log";

    /** Output directory of client */
    public static final String OUT_DIR_CLIENT = "out/client";

    /** Output directory of server */
    public static final String OUT_DIR_SERVER = "out/server";

    /** Proto file location */
    public static final String PROTO_CLASSPATH = "proto";

    /** Proto descriptor file */
    public static final String PROTO_DESCRIPTOR_PATH = "protobin/common.protobin";

    /** Directory containing .bin test cases (for client) */
    public static final String TESTS_DIR_CLIENT = "test-cases/client";

    /** Directory containing .bin test cases (for server) */
    public static final String TESTS_DIR_SERVER = "test-cases/server";
}
