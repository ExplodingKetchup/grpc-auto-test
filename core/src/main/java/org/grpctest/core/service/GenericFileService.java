package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.grpctest.core.config.Config;
import org.grpctest.core.pojo.RuntimeConfig;
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

import static org.grpctest.core.constant.Constants.*;

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

    public void copyProtos(RuntimeConfig runtimeConfig) throws Throwable {
        copyResourceFiles(PROTO_CLASSPATH, PROTO_TARGET_JAVA, runtimeConfig.getIncludedProtos());
        copyResourceFiles(PROTO_CLASSPATH, PROTO_TARGET_NODEJS, runtimeConfig.getIncludedProtos());
        copyResourceFiles(PROTO_CLASSPATH, PROTO_TARGET_PYTHON, runtimeConfig.getIncludedProtos());
    }

    /**
     * Cleans the following places (temporary storage areas during test):<br>
     * protobin/common.protobin<br>
     * test-cases/client/*<br>
     * test-cases/server/*<br>
     * out/client/*<br>
     * out/server/*<br>
     * out/tcpdump/*<br>
     * java/common/src/main/proto/*<br>
     * nodejs/proto/*<br>
     * python/proto/*<br>
     */
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File("protobin/"));
        FileUtils.cleanDirectory(new File(TESTS_DIR_CLIENT));
        FileUtils.cleanDirectory(new File(TESTS_DIR_SERVER));
        FileUtils.cleanDirectory(new File(OUT_DIR_CLIENT));
        FileUtils.cleanDirectory(new File(OUT_DIR_SERVER));
        FileUtils.cleanDirectory(new File("out/tcpdump/"));
        FileUtils.cleanDirectory(new File(PROTO_TARGET_JAVA));
        FileUtils.cleanDirectory(new File(PROTO_TARGET_NODEJS));
        FileUtils.cleanDirectory(new File(PROTO_TARGET_PYTHON));
    }

    /**
     * Copy all files in a resource directory to a specified filesystem directory
     */
    private void copyResourceFiles(String src, String dest, List<String> filenames) throws Throwable {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String classpath = "classpath:" + src + "/*";
        try {
            Path destPath = Paths.get(dest);
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }
            for (Resource resource: resourcePatternResolver.getResources(classpath)) {
                if (filenames.isEmpty() || filenames.contains(resource.getFilename())) {
                    Files.copy(resource.getInputStream(), Paths.get(dest + "/" + resource.getFilename()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException ioe) {
            log.error("[copyResourceFiles] Failed to copy file from {} to {}", classpath, dest, ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[copyResourceFiles] An error occurred", t);
            throw t;
        }
    }
}
