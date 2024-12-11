package org.grpctest.java.client.generated;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.client.config.Config;
import org.grpctest.java.common.util.MessageUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Function;

@Component
@Slf4j
public class JavaClient implements InitializingBean {

    private final Config config;

<#list registry.getAllServices() as service>
    private final ${service.name}Grpc.${service.name}BlockingStub ${service.name?uncap_first}BlockingStub;

    private final ${service.name}Grpc.${service.name}Stub ${service.name?uncap_first}AsyncStub;
</#list>

    public JavaClient(Config config) {
        this.config = config;
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
        ${method.inType} param${method.ownerServiceName}${method.name?cap_first} = MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "${method.ownerServiceName}_${method.name}_param.bin", ${method.inType}.class);
        invokeRpcMethod(${method.ownerServiceName?uncap_first}BlockingStub::${method.name}, param${method.ownerServiceName}${method.name?cap_first}, "${method.ownerServiceName}", "${method.name}");

</#list>
    }

    private <T, R> void invokeRpcMethod(Function<T, R> method, T parameter, String serviceName, String methodName) {
        log.info("[invokeRpcMethod] Invoke method {}.{} with parameter {}", serviceName, methodName, parameter);
        try {
            R result = method.apply(parameter);
            log.info("[invokeRpcMethod] Method {}.{} returns {}", serviceName, methodName, result);
            if (result instanceof GeneratedMessageV3) {
                MessageUtil.messageToFile((GeneratedMessageV3) result, config.getOutDir() + File.separator + serviceName + "_" + methodName + "_return.bin");
            } else {
                log.error("[invokeRpcMethod] Method {}.{} returns message of type [{}], incompatible with protobuf", serviceName, methodName, result.getClass());
            }
        } catch (Throwable t) {
            log.error("[invokeRpcMethod] Method {}.{} throws error", serviceName, methodName, t);
        }
    }
}