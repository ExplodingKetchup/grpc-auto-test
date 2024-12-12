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

    private final PeopleServiceGrpc.PeopleServiceBlockingStub peopleServiceBlockingStub;

    private final PeopleServiceGrpc.PeopleServiceStub peopleServiceAsyncStub;

    public JavaClient(Config config) {
        this.config = config;
        Channel channel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
        this.peopleServiceBlockingStub = PeopleServiceGrpc.newBlockingStub(channel);
        this.peopleServiceAsyncStub = PeopleServiceGrpc.newStub(channel);
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Invoke test case: person.PeopleService.getPerson
        GetPersonRequest param_person_PeopleService_getPerson = MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_param.bin", GetPersonRequest.class);
        invokeRpcMethod(peopleServiceBlockingStub::getPerson, param_person_PeopleService_getPerson, "person.PeopleService.getPerson");

    }

    private <T, R> void invokeRpcMethod(Function<T, R> method, T parameter, String methodId) {
        log.info("[invokeRpcMethod] Invoke method {} with parameter {}", methodId, parameter);
        try {
            R result = method.apply(parameter);
            log.info("[invokeRpcMethod] Method {} returns {}", methodId, result);
            if (result instanceof GeneratedMessageV3) {
                MessageUtil.messageToFile((GeneratedMessageV3) result, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return.bin");
            } else {
                log.error("[invokeRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, result.getClass());
            }
        } catch (Throwable t) {
            log.error("[invokeRpcMethod] Method {} throws error", methodId, t);
        }
    }
}