package org.grpctest.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor
public class Config {

    /** Log directory */
    @Value("${core.log.dir}")
    private String logDir;

    /* Proto descriptor file */
    @Value("${core.proto.descriptor.path}")
    private String protoDescriptorPath;
}
