package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class FileCopier {

    // Target directory to put proto files in a Java test program
    private static final String PROTO_TARGET_JAVA = "java/common/src/main/proto";

    private final Config config;
    private final ResourceLoader resourceLoader;

    public void copyProtos() throws Throwable {
        copyResourceFiles(config.getProtoClasspath(), PROTO_TARGET_JAVA);
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
}
