package org.grpctest.java.server.generated;

import io.grpc.*;
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
<#list registry.getAllServices() as service>
                      ${service.name} ${service.name?uncap_first}<#sep>,
</#list>
    ) {
        this.config = config;
        this.server = ServerBuilder
                .forPort(config.getServerPort())
<#if registry.isResponseCompressionSet()>
                .intercept(new ServerInterceptor() {
                    @Override
                    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                        call.setCompression("${registry.getResponseCompression()}");
                        return next.startCall(call, headers);
                    }
                })
</#if>
<#list registry.getAllServices() as service>
                .addService(ServerInterceptors.intercept(${service.name?uncap_first}, metadataInterceptor))
</#list>
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
