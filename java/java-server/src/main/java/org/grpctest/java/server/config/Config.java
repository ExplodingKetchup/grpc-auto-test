package org.grpctest.java.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor
public class Config {

    /** Port that gRPC server runs on */
    @Value("${rpc.server.port}")
    private int serverPort;

}
