package org.grpctest.java.server.generated;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.server.config.Config;
import org.grpctest.java.server.generated.interceptor.MetadataInterceptor;
import org.grpctest.java.server.generated.service.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class JavaServer implements InitializingBean {

    private final Config config;

    private final Server server;

    @Autowired
    public JavaServer(Config config,
                      MetadataInterceptor metadataInterceptor,
                      HotpotService hotpotService    ) {
        this.config = config;
        this.server = ServerBuilder
                .forPort(config.getServerPort())
                .addService(ServerInterceptors.intercept(hotpotService, metadataInterceptor))
                .build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            server.start();
            log.info("Server started. Listening on {}", config.getServerPort());
            server.awaitTermination();
        } catch (IOException ioe) {
            log.error("An error occurred while starting server", ioe);
        } catch (InterruptedException ie) {
            log.error("Server thread interrupted", ie);
        } catch (Throwable t) {
            log.error("Unknown error occurred", t);
        }
    }
}
