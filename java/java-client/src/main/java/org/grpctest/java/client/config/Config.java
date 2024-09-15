package org.grpctest.java.client.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Config {

    // Service discovery

    @Value("${rpc.client.service.host}")
    private String serviceHost;

    @Value("${rpc.client.service.port}")
    private Integer servicePort;
}
