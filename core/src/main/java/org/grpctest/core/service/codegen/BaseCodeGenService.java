package org.grpctest.core.service.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.freemarker.DataModel;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Slf4j
@AllArgsConstructor
public abstract class BaseCodeGenService implements CodeGenService {

    protected final Configuration freemarkerConfig;

    protected final Config config;

    protected final Registry registry;

    public void generateFileFromFtl(String ftlFilename, DataModel dataModel, String targetFilepath) throws Exception {
        Template template = freemarkerConfig.getTemplate(ftlFilename);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(targetFilepath))) {
            template.process(dataModel, bufferedWriter);
        }
    }
}
