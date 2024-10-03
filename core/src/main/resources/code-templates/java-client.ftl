package org.grpctest.java.client.generated;

import com.google.protobuf.Message.Builder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.client.config.Config;
import org.grpctest.java.common.util.MessageUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Slf4j
public class JavaClient implements InitializingBean {

<#list registry.getAllServices() as service>
    private final ${service.name}Grpc.${service.name}BlockingStub ${service.name?uncap_first}BlockingStub;

    private final ${service.name}Grpc.${service.name}Stub ${service.name?uncap_first}AsyncStub;
</#list>

    public JavaClient(Config config) {
        Channel channel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
<#list registry.getAllServices() as service>
        this.${service.name?uncap_first}BlockingStub = ${service.name}Grpc.newBlockingStub(channel);
        this.${service.name?uncap_first}AsyncStub = ${service.name}Grpc.newStub(channel);
</#list>
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
<#list registry.getAllMethods() as method>

        // Invoke test case: ${method.ownerServiceName}.${method.name}
        ${method.inType} param${method.ownerServiceName}${method.name?cap_first} = MessageUtil.messageFromFile("${testsDir}/${method.ownerServiceName}_${method.name}_param.bin", ${method.inType}.class);
        invokeRpcMethod(${method.ownerServiceName?uncap_first}BlockingStub::${method.name}, param${method.ownerServiceName}${method.name?cap_first}, "${method.ownerServiceName}", "${method.name}");

</#list>
    }

    private <T, R> void invokeRpcMethod(Function<T, R> method, T parameter, String serviceName, String methodName) {
        log.info("[invokeRpcMethod] Invoke method {}.{} with parameter {}", serviceName, methodName, parameter);
        try {
            R result = method.apply(parameter);
            log.info("[invokeRpcMethod] Method {}.{} returns {}", serviceName, methodName, result);
        } catch (Throwable t) {
            log.error("[invokeRpcMethod] Method {}.{} throws error", serviceName, methodName, t);
        }
    }

    private Object parseJson(String json, Builder builder) {
        try {
            JsonFormat.parser().merge(json, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("[parseJson] Fail to parse return value from test case: [{}]", json, e);
            return builder.build();
        }
    }
}