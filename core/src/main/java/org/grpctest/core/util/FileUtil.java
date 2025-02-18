package org.grpctest.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileUtil {

    /**
     * Append {@code content} to the end of the file. If file doesn't exist, create a new file
     * and write {@code title} at the beginning of the file.
     *
     * @param filepath
     * @param title
     * @param content
     * @throws Throwable
     */
    public static void appendToFile(String filepath, String title, String content) throws IOException {
        File file = new File(filepath);
        try {
            boolean isNewFile = file.createNewFile();
            try (FileWriter fr = new FileWriter(file, true)) {
                try (BufferedWriter br = new BufferedWriter(fr)) {
                    if (isNewFile) {
                        br.write(title);
                    }
                    br.write(content);
                }
            }
        } catch (IOException ioe) {
            log.error("[appendToFile] File I/O got error", ioe);
            throw ioe;
        }
    }

    public static byte[] readBytes(String filepath) throws Throwable {
        File file = new File(filepath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return fileInputStream.readAllBytes();
        } catch (FileNotFoundException fnfe) {
            log.error("[readBytes] File {} not found", filepath, fnfe);
            throw fnfe;
        } catch (Throwable t) {
            log.error("[readBytes] Failed with error", t);
            throw t;
        }
    }

    public static List<String> listFilesWithSamePrefix(String dirpath, String prefix) throws IOException {
        List<String> matchingFilepaths = new ArrayList<>();
        Path dir = Paths.get(dirpath);

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("[listFilesWithSamePrefix] Not a directory: " + dirpath);
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                dir,
                path -> Files.isRegularFile(path) && path.getFileName().toString().startsWith(prefix)
        )) {
            for (Path filepath : directoryStream) {
                matchingFilepaths.add(filepath.toString());
            }
        } catch (IOException ioe) {
            log.error("[listFilesWithSamePrefix] File I/O failed", ioe);
            throw ioe;
        }

        return matchingFilepaths;
    }

    /**
     * Read the last {@code lines} lines of a text file
     */
    public static List<String> tail(Path filepath, int lines) {
        return tail(filepath.toFile(), lines);
    }

    /**
     * Read the last {@code lines} lines of a text file
     */
    public static List<String> tail(String filepath, int lines) {
        return tail(new File(filepath), lines);
    }

    /**
     * Read the last {@code lines} lines of a text file
     */
    public static List<String> tail(File file, int lines) {
        RandomAccessFile fileHandler = null;
        List<String> content = new ArrayList<>();
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                        content.add(sb.reverse().toString());
                        sb.delete(0, sb.length());
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength-1) {
                        line = line + 1;
                        content.add(sb.reverse().toString());
                        sb.delete(0, sb.length());
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append( ( char ) readByte );
            }

            if ((line < lines) && (!sb.isEmpty())) {
                content.add(sb.reverse().toString());
            }

            return content;
        } catch( java.io.FileNotFoundException e ) {
            log.error("[tail] File not found: {}", file.getAbsolutePath());
            return null;
        } catch( java.io.IOException e ) {
            log.error("[tail] File I/O Exception", e);
            return null;
        }
        finally {
            if (fileHandler != null ) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    log.error("[tail] File handler failed to close");
                }
            }
        }
    }
}
