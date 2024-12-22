package org.grpctest.java.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileUtil {

    public static void stringToFile(String filePath, String content) throws IOException {
        Path path = Path.of(filePath);
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void appendLineToFile(String filePath, String content) throws IOException {
        Path path = Path.of(filePath);

        // Ensure the file exists, create it if not
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        // Write the string with a new line appended
        Files.write(path, Collections.singletonList(content), StandardOpenOption.APPEND);
    }


    public static String fileToString(String filePath) throws IOException {
        Path path = Path.of(filePath);
        return Files.readString(path);
    }

    public static void loopMultipleFilesWithSamePrefix(String filepathPattern, Consumer<String> consumer) {
        int i = 0;
        while (Files.exists(Paths.get(buildNumberedFilepath(filepathPattern, i)))) {
            consumer.accept(buildNumberedFilepath(filepathPattern, i));
            i++;
        }
    }

    private static String buildNumberedFilepath(String filepathPattern, int index) {
        int fileExtStartIdx = filepathPattern.lastIndexOf('.');
        return filepathPattern.substring(0, fileExtStartIdx) + "_" + index + filepathPattern.substring(fileExtStartIdx);
    }
}
