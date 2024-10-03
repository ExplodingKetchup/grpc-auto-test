package org.grpctest.java.common.util;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

@Slf4j
public class MessageUtil {

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
}
