package org.grpctest.java.common.util;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;

@Slf4j
public class MessageUtil {

    private static final String DIR_SERVER_OUT = "out/server/";
    private static final String DIR_CLIENT_OUT = "out/client/";

    public static <T extends Message> T messageFromFile(String filepath, Class<T> messageType) {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
                Method parseMethod = messageType.getMethod("parseFrom", InputStream.class);
                return (T) parseMethod.invoke(null, fileInputStream);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("[messageFromFile] File not found", fnfe);
        } catch (IOException ioe) {
            log.error("[messageFromFile] File I/O exception", ioe);
        } catch (NoSuchMethodException nsme) {
            log.error("[messageFromFile] Invalid Message type {}", messageType, nsme);
        } catch (Throwable t) {
            log.error("[messageFromFile] An error occurred", t);
        }
        return null;
    }

    public static <M extends GeneratedMessageV3> void messageToFile(M message, boolean isServer, String filename) throws Throwable {
        try {
            File file;
            if (isServer) {
                file = new File(DIR_SERVER_OUT + filename);
            } else {
                file = new File(DIR_CLIENT_OUT + filename);
            }
            file.createNewFile();

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                message.writeTo(fileOutputStream);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("[messageToFile] Output file not created", fnfe);
            throw fnfe;
        } catch (IOException ioe) {
            log.error("[messageToFile] Error in file I/O", ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[messageToFile] An error occurred", t);
            throw t;
        }
    }
}
