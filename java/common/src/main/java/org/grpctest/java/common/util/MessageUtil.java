package org.grpctest.java.common.util;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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

    public static <T extends Message> List<T> messageListFromMultipleFiles(String filepathPattern, Class<T> messageType) {
        List<T> result = new ArrayList<>();
        FileUtil.loopMultipleFilesWithSamePrefix(filepathPattern, (filepath) -> result.add(messageFromFile(filepath, messageType)));
        return result;
    }

    public static <M extends GeneratedMessageV3> void messageToFile(M message, String filepath) throws Throwable {
        try {
            File file = new File(filepath);
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

    public static void metadataToFile(String filepath, Metadata metadata) throws IOException {
        try {
            FileUtil.appendLineToFile(filepath, formatMetadataForOutput(metadata));
        } catch (IOException ioe) {
            log.error("[interceptCall] Error writing metadata to file {}", filepath, ioe);
            throw ioe;
        }
    }

    public static String formatMetadataForOutput(Metadata metadata) {
        StringBuilder metaContent = new StringBuilder();

        for (String key : metadata.keys()) {
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                byte[] value = metadata.get(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER));
                if (value != null) {
                    metaContent.append(key).append(":").append(HexFormat.of().formatHex(value)).append("\n");
                }
            } else {
                String value = metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                if (value != null) {
                    metaContent.append(key).append(":").append(value).append("\n");
                }
            }
        }

        return metaContent.toString();
    }

    /** Log a gRPC exception (not a Java exception) to file */
    public static void grpcExceptionToFile(String filepath, Throwable t) throws Exception {
        try {
            FileUtil.stringToFile(filepath, formatGrpcException(
                    Status.fromThrowable(t), Status.trailersFromThrowable(t)));
        } catch (IOException ioe) {
            log.error("[grpcExceptionToFile] Error writing gRPC exception to file {}", filepath, ioe);
            throw ioe;
        } catch (Exception e) {
            log.error("[grpcExceptionToFile] Error processing throwable {}", t, e);
            throw e;
        }
    }

    public static String formatGrpcException(Status status, Metadata trailers) {
        if (Objects.isNull(status)) throw new IllegalArgumentException();
        return status.getCode().name() + "\n" +
                status.getDescription() + "\n" +
                formatMetadataForOutput(trailers);
    }
}
