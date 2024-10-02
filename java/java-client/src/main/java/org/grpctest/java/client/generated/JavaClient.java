package org.grpctest.java.client.generated;

import com.google.protobuf.Message.Builder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.common.define.*;
import org.grpctest.java.client.config.Config;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Slf4j
public class JavaClient implements InitializingBean {

    private final PeopleServiceGrpc.PeopleServiceBlockingStub peopleServiceBlockingStub;

    private final PeopleServiceGrpc.PeopleServiceStub peopleServiceAsyncStub;

    public JavaClient(Config config) {
        Channel channel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
        this.peopleServiceBlockingStub = PeopleServiceGrpc.newBlockingStub(channel);
        this.peopleServiceAsyncStub = PeopleServiceGrpc.newStub(channel);
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Invoke test case: Test case 1
        String paramGetPerson0Json = "{\"personId\":{\"id\":1}}";
        GetPersonRequest paramGetPerson0 = (GetPersonRequest) parseJson(paramGetPerson0Json, GetPersonRequest.newBuilder());
        invokeRpcMethod(peopleServiceBlockingStub::getPerson, paramGetPerson0, "PeopleService", "getPerson");

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