package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.grpctest.core.config.Config;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class GenericFileService {

    // Target directory to put proto files in a Java test program
    private static final String PROTO_TARGET_JAVA = "java/common/src/main/proto";
    private static final String PROTO_TARGET_NODEJS = "nodejs/proto";
    private static final String PROTO_TARGET_PYTHON = "python/proto";

    private final Config config;
    private final ResourceLoader resourceLoader;

    public void copyProtos() throws Throwable {
        copyResourceFiles(config.getProtoClasspath(), PROTO_TARGET_JAVA);
        copyResourceFiles(config.getProtoClasspath(), PROTO_TARGET_NODEJS);
        copyResourceFiles(config.getProtoClasspath(), PROTO_TARGET_PYTHON);
    }

    /**
     * Cleans the following places (temporary storage areas during test):<br>
     * protobin/common.protobin<br>
     * test-cases/*<br>
     * out/client/*<br>
     * out/server/*<br>
     */
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File("protobin/"));
        FileUtils.cleanDirectory(new File("test-cases/"));
        FileUtils.cleanDirectory(new File("out/client/"));
        FileUtils.cleanDirectory(new File("out/server/"));
    }

    /**
     * Copy all files in a resource directory to a specified filesystem directory
     */
    private void copyResourceFiles(String src, String dest) throws Throwable {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String classpath = "classpath:" + src + "/*";
        try {
            Path destPath = Paths.get(dest);
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }
            for (Resource resource: resourcePatternResolver.getResources(classpath)) {
                Files.copy(resource.getInputStream(), Paths.get(dest + "/" + resource.getFilename()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ioe) {
            log.error("[copyResourceFiles] Failed to copy file from {} to {}", classpath, dest, ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[copyResourceFiles] An error occurred", t);
            throw t;
        }
    }

    /**
     * Read the last {@code lines} lines of a text file
     */
    public List<String> tail(String filepath, int lines) {
        File file = new File(filepath);
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
