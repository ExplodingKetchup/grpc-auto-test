<#assign service = registry.lookupService(serviceId)>
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

<#list registry.getAllMethods(serviceId) as method>
    @Override
    <#if method.type == "UNARY">
    public void ${method.name}(${method.inType?split(".")?last} request, StreamObserver<${method.outType?split(".")?last}> responseObserver) {
        try {
            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "${method.id?replace(".", "_")}_param.bin");
            responseObserver.onNext(${method.name}Impl(request));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[${method.name}] An error occurred", t);
        }
    }
    </#if>

    private ${method.outType?split(".")?last} ${method.name}Impl(${method.inType?split(".")?last} request) {
        log.info("[${method.name}Impl] Received request {}", request);
        return MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_return.bin", ${method.outType?split(".")?last}.class);
    }
</#list>
}
