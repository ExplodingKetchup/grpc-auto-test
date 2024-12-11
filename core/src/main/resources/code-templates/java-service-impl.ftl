package org.grpctest.java.server.generated.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.common.util.MessageUtil;
import org.grpctest.java.server.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class ${service.name} extends ${service.name}Grpc.${service.name}ImplBase {

    @Autowired
    private Config config;

<#list service.methods as method>
    @Override
    <#if method.type == "UNARY">
    public void ${method.name}(${method.inType} request, StreamObserver<${method.outType}> responseObserver) {
        try {
            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "${service.name}_${method.name}_param.bin");
            responseObserver.onNext(${method.name}Impl(request));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[${method.name}] An error occurred", t);
        }
    }
    </#if>

    private ${method.outType} ${method.name}Impl(${method.inType} request) {
        log.info("[${method.name}Impl] Received request {}", request);
        return MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "${service.name}_${method.name}_return.bin", ${method.outType}.class);
    }
</#list>
}
