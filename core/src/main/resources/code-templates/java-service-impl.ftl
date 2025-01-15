<#function generateTabs indent>
    <#local tabs = "" />
    <#if (indent > 0)>
        <#list 1..indent as i>
            <#local tabs = tabs + "    " />
        </#list>
    </#if>
    <#return tabs>
</#function>
<#macro requestLogging method indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logRequests>
${tabs}log.info("[${method.name}] Received request {}", request);
        <#if logRequestsPrintFields>
${tabs}ObjectUtil.logFieldsOfObject(request, "${method.id} - request", <#list registry.getAllFieldNamesAsCamelCase(method.inType) as fieldname>"${fieldname}"<#sep>, </#list>);
        </#if>
    </#if>
</#macro>
<#macro responseLogging method indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logResponses>
${tabs}log.info("[${method.name}] Response: {}", response);
        <#if logResponsesPrintFields>
${tabs}ObjectUtil.logFieldsOfObject(response, "${method.id} - response", <#list registry.getAllFieldNamesAsCamelCase(method.outType) as fieldname>"${fieldname}"<#sep>, </#list>);
        </#if>
    </#if>
</#macro>
<#assign service = registry.lookupService(serviceId)>
package org.grpctest.java.server.generated.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.common.util.FileUtil;
import org.grpctest.java.common.util.MessageUtil;
import org.grpctest.java.common.util.ObjectUtil;
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
<#assign requestType = method.inType?split(".")?last>
<#assign responseType = method.outType?split(".")?last>
    @Override
    <#if method.type == "UNARY" || method.type == "SERVER_STREAMING">
    public void ${method.name}(${requestType} request, StreamObserver<${responseType}> responseObserver) {
        try {
            <@requestLogging method=method indent=3/>

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "${method.id?replace(".", "_")}_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_return.bin",
                    (filepath) -> {
                        ${responseType} response = MessageUtil.messageFromFile(filepath, ${responseType}.class);
                        <@responseLogging method=method indent=6/>
                        responseObserver.onNext(response);
                    }
            );

        <#if testcaseRegistry.getExceptionForMethod(method)??>
            <#assign rpcException = testcaseRegistry.getExceptionForMethod(method)>
            Metadata trailers = new Metadata();
            <#assign trailers = rpcException.trailingMetadata>
            <#list trailers?keys as trailerKey>
            trailers.put(Metadata.Key.of("${trailerKey}", Metadata.ASCII_STRING_MARSHALLER), "${trailers[trailerKey].getRight()}");
            </#list>
            responseObserver.onError(Status.${rpcException.statusCode.name()}.withDescription("${rpcException.description}").asRuntimeException(trailers));
        <#else>
            responseObserver.onCompleted();
        </#if>
        } catch (Throwable t) {
            log.error("[${method.name}] An error occurred", t);
        }
    }
    <#elseif method.type == "CLIENT_STREAMING" || method.type == "BIDI_STREAMING">
    public StreamObserver<${requestType}> ${method.name}(StreamObserver<${responseType}> responseObserver) {
        return new StreamObserver<${requestType}>() {
            private int requestIdx = 0;
            @Override
            public void onNext(${requestType} request) {
                try {
                    <@requestLogging method=method indent=5/>
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "${method.id?replace(".", "_")}_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[${method.name}] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[${method.name}] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "${method.id?replace(".", "_")}_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[${method.name}] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_return.bin",
                        (filepath) -> {
                            ${responseType} response = MessageUtil.messageFromFile(filepath, ${responseType}.class);
                            <@responseLogging method=method indent=7/>
                            responseObserver.onNext(response);
                        }
                );

        <#if testcaseRegistry.getExceptionForMethod(method)??>
        <#assign rpcException = testcaseRegistry.getExceptionForMethod(method)>
                Metadata trailers = new Metadata();
        <#assign trailers = rpcException.trailingMetadata>
            <#list trailers?keys as trailerKey>
                trailers.put(Metadata.Key.of("${trailerKey}", Metadata.ASCII_STRING_MARSHALLER), "${trailers[trailerKey].getRight()}");
            </#list>
                responseObserver.onError(Status.${rpcException.statusCode.name()}.withDescription("${rpcException.description}").asRuntimeException(trailers));
        <#else>
                responseObserver.onCompleted();
        </#if>
            }
        };
    }
    </#if>

</#list>
}
