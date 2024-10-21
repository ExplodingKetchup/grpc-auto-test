package org.grpctest.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

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
    public static void appendToFile(String filepath, String title, String content) throws Throwable {
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
        } catch (Throwable t) {
            log.error("[appendToFile] Failed with error", t);
            throw t;
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
}
