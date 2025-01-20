<#function generateTabs indent>
    <#local tabs = "" />
    <#if (indent > 0)>
        <#list 1..indent as i>
            <#local tabs = tabs + "    " />
        </#list>
    </#if>
    <#return tabs>
</#function>
<#macro requestLogging invoker indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logRequests>
${tabs}log.info("[${invoker}] {} - Request: {}", methodId, parameter);
        <#if logRequestsPrintFields>
${tabs}ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        </#if>
    </#if>
</#macro>
<#macro responseLogging invoker indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logResponses>
${tabs}log.info("[${invoker}] {} - Response: {}", methodId, response);
        <#if logResponsesPrintFields>
${tabs}ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
        </#if>
    </#if>
</#macro>
package org.grpctest.java.client.generated;

import ch.qos.logback.classic.LoggerContext;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.client.config.Config;
import org.grpctest.java.common.util.MessageUtil;
import org.grpctest.java.common.util.ObjectUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Component
@Slf4j
public class JavaClient implements InitializingBean {

    private final Config config;

<#list registry.getAllServices() as service>
    private final ${service.name}Grpc.${service.name}BlockingStub ${service.name?uncap_first}BlockingStub;

    private final ${service.name}Grpc.${service.name}Stub ${service.name?uncap_first}AsyncStub;
</#list>

<#list registry.getAllMessages() as message>
    private final String[] ${message.id?replace(".", "_")}_fields = new String[]{<#list registry.getAllFieldsAsJavaGetters(message.id) as fieldname>"${fieldname}"<#sep>, </#list>};
</#list>

    public JavaClient(Config config, ClientInterceptor clientInterceptor) {
        this.config = config;
        Channel originChannel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
        Channel channel = ClientInterceptors.intercept(originChannel, clientInterceptor);
<#list registry.getAllServices() as service>
        this.${service.name?uncap_first}BlockingStub = ${service.name}Grpc.newBlockingStub(channel);
        this.${service.name?uncap_first}AsyncStub = ${service.name}Grpc.newStub(channel);
</#list>
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Client shutting down...");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
<#list registry.getAllMethods() as method>
    <#assign requestFields = method.inType?replace(".", "_") + "_fields">
    <#assign responseFields = method.outType?replace(".", "_") + "_fields">
        // Invoke test case: ${method.id}
    <#if method.type == "UNARY">
        invokeUnaryRpcMethod(${method.ownerServiceId?split(".")?last?uncap_first}BlockingStub::${method.name}, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_param_0.bin", ${method.inType?split(".")?last}.class), "${method.id}", ${requestFields}, ${responseFields});
    <#elseif method.type == "SERVER_STREAMING">
        invokeServerStreamingRpcMethod(${method.ownerServiceId?split(".")?last?uncap_first}BlockingStub::${method.name}, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_param_0.bin", ${method.inType?split(".")?last}.class), "${method.id}", ${requestFields}, ${responseFields});
    <#elseif method.type == "CLIENT_STREAMING">
        invokeClientStreamingRpcMethod(${method.ownerServiceId?split(".")?last?uncap_first}AsyncStub::${method.name}, MessageUtil.messageListFromMultipleFiles(config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_param.bin", ${method.inType?split(".")?last}.class), "${method.id}", ${requestFields}, ${responseFields});
    <#elseif method.type == "BIDI_STREAMING">
        invokeBidiStreamingRpcMethod(${method.ownerServiceId?split(".")?last?uncap_first}AsyncStub::${method.name}, MessageUtil.messageListFromMultipleFiles(config.getTestcaseDir() + File.separator + "${method.id?replace(".", "_")}_param.bin", ${method.inType?split(".")?last}.class), "${method.id}", ${requestFields}, ${responseFields});
    </#if>

</#list>
        for (int i = 0; i < 10000000; i++);
    }

    private <T, R> void invokeUnaryRpcMethod(Function<T, R> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        <@requestLogging invoker="invokeUnaryRpcMethod" indent=2/>
        try {
            R response = method.apply(parameter);
            <@responseLogging invoker="invokeUnaryRpcMethod" indent=3/>
            if (response instanceof GeneratedMessageV3) {
                MessageUtil.messageToFile((GeneratedMessageV3) response, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_0.bin");
            } else {
                log.error("[invokeUnaryRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, response.getClass());
            }
        } catch (Throwable t) {
            log.error("[invokeUnaryRpcMethod] Method {} throws error", methodId, t);
            try {
                MessageUtil.grpcExceptionToFile(
                        config.getOutDir() + File.separator + methodId.replace(".", "_") + "_error.txt",
                        t
                );
            } catch (Exception e) {
            }
        }
    }

    private <T, R> void invokeServerStreamingRpcMethod(Function<T, Iterator<R>> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        <@requestLogging invoker="invokeServerStreamingRpcMethod" indent=2/>
        try {
            Iterator<R> responses = method.apply(parameter);
            int i = 0;
            while (responses.hasNext()) {
                R response = responses.next();
                <@responseLogging invoker="invokeServerStreamingRpcMethod" indent=4/>
                if (response instanceof GeneratedMessageV3) {
                    MessageUtil.messageToFile((GeneratedMessageV3) response, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_" + i + ".bin");
                    i++;
                } else {
                    log.error("[invokeServerStreamingRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, response.getClass());
                }
            }
        } catch (Throwable t) {
            log.error("[invokeServerStreamingRpcMethod] Method {} throws error", methodId, t);
            try {
                MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + methodId.replace(".", "_") + "_error.txt", t);
            } catch (Exception e) {
            }
        }
    }

    private <T, R> void invokeClientStreamingRpcMethod(Function<StreamObserver<R>, StreamObserver<T>> method, List<T> parameters, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        StreamObserver<R> responseObserver = new StreamObserver<R>() {
            @Override
            public void onNext(R response) {
                try {
                    <@responseLogging invoker="invokeClientStreamingRpcMethod" indent=5/>
                    if (response instanceof GeneratedMessageV3) {
                        MessageUtil.messageToFile((GeneratedMessageV3) response, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_0.bin");
                    } else {
                        log.error("[invokeServerStreamingRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, response.getClass());
                    }
                } catch (Throwable t) {
                    log.error("[invokeClientStreamingRpcMethod] Error processing response {}", response, t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[invokeClientStreamingRpcMethod] Method {} throws error", methodId, throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + methodId.replace(".", "_") + "_error.txt", throwable);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCompleted() {

            }
        };
        StreamObserver<T> requestObserver = method.apply(responseObserver);

        for (T parameter : parameters) {
            <@requestLogging invoker="invokeClientStreamingRpcMethod" indent=3/>
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }

    private <T, R> void invokeBidiStreamingRpcMethod(Function<StreamObserver<R>, StreamObserver<T>> method, List<T> parameters, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        StreamObserver<R> responseObserver = new StreamObserver<R>() {
            private int i = 0;
            @Override
            public void onNext(R response) {
                try {
                    <@responseLogging invoker="invokeBidiStreamingRpcMethod" indent=5/>
                    if (response instanceof GeneratedMessageV3) {
                        MessageUtil.messageToFile((GeneratedMessageV3) response, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_" + i + ".bin");
                        i++;
                    } else {
                        log.error("[invokeBidiStreamingRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, response.getClass());
                    }
                } catch (Throwable t) {
                    log.error("[invokeBidiStreamingRpcMethod] Error processing response {}", response, t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[invokeBidiStreamingRpcMethod] Method {} throws error", methodId, throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + methodId.replace(".", "_") + "_error.txt", throwable);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCompleted() {

            }
        };
        StreamObserver<T> requestObserver = method.apply(responseObserver);

        for (T parameter : parameters) {
            <@requestLogging invoker="invokeBidiStreamingRpcMethod" indent=3/>
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }
}