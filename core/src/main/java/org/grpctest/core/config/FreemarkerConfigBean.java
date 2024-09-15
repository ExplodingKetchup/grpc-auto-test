package org.grpctest.core.config;

import freemarker.template.TemplateExceptionHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class FreemarkerConfigBean {

    private final Config config;

    @Bean
    public freemarker.template.Configuration freemarkerConfig() {
        freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/code-templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        if (config.isDebugEnabled()) {
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
            freemarkerConfig.setLogTemplateExceptions(true);
        } else {
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            freemarkerConfig.setLogTemplateExceptions(false);
        }
        freemarkerConfig.setWrapUncheckedExceptions(true);
        freemarkerConfig.setFallbackOnNullLoopVariable(false);
        return freemarkerConfig;
    }
}
