package org.grpctest.java.client.generated;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.client.config.Config;
import org.grpctest.java.common.util.MessageUtil;
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

    private final PeopleServiceGrpc.PeopleServiceBlockingStub peopleServiceBlockingStub;

    private final PeopleServiceGrpc.PeopleServiceStub peopleServiceAsyncStub;

    public JavaClient(Config config, ClientInterceptor clientInterceptor) {
        this.config = config;
        Channel originChannel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
        Channel channel = ClientInterceptors.intercept(originChannel, clientInterceptor);
        this.peopleServiceBlockingStub = PeopleServiceGrpc.newBlockingStub(channel);
        this.peopleServiceAsyncStub = PeopleServiceGrpc.newStub(channel);
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // Invoke test case: person.PeopleService.getPerson
        invokeUnaryRpcMethod(peopleServiceBlockingStub::getPerson, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_param_0.bin", GetPersonRequest.class), "person.PeopleService.getPerson");
        invokeClientStreamingRpcMethod(peopleServiceAsyncStub::registerPerson, MessageUtil.messageListFromMultipleFiles(config.getTestcaseDir() + File.separator + "person_PeopleService_registerPerson.bin", GetPersonRequest.class), "person.PeopleService.registerPerson");

        while(true);
    }

    private <T, R> void invokeUnaryRpcMethod(Function<T, R> method, T parameter, String methodId) {
        log.info("[invokeUnaryRpcMethod] Invoke method {} with parameter {}", methodId, parameter);
        try {
            R result = method.apply(parameter);
            log.info("[invokeUnaryRpcMethod] Method {} returns {}", methodId, result);
            if (result instanceof GeneratedMessageV3) {
                MessageUtil.messageToFile((GeneratedMessageV3) result, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return.bin");
            } else {
                log.error("[invokeUnaryRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, result.getClass());
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

    private <T, R> void invokeServerStreamingRpcMethod(Function<T, Iterator<R>> method, T parameter, String methodId) {
        log.info("[invokeServerStreamingRpcMethod] Invoke method {} with parameter {}", methodId, parameter);
        try {
            Iterator<R> result = method.apply(parameter);
            int i = 0;
            while (result.hasNext()) {
                R singleResult = result.next();
                log.info("[invokeServerStreamingRpcMethod] Method {} returns {}", methodId, singleResult);
                if (singleResult instanceof GeneratedMessageV3) {
                    MessageUtil.messageToFile((GeneratedMessageV3) singleResult, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_" + i + ".bin");
                    i++;
                } else {
                    log.error("[invokeServerStreamingRpcMethod] Method {} returns message of type [{}], incompatible with protobuf", methodId, result.getClass());
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

    private <T, R> void invokeClientStreamingRpcMethod(Function<StreamObserver<R>, StreamObserver<T>> method, List<T> parameters, String methodId) {
        StreamObserver<R> responseObserver = new StreamObserver<R>() {
            @Override
            public void onNext(R response) {
                try {
                    log.info("[invokeClientStreamingRpcMethod] Method {} returns {}", methodId, response);
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
            log.info("[invokeClientStreamingRpcMethod] Invoke method {} with parameter {}", methodId, parameter);
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }

    private <T, R> void invokeBidiStreamingRpcMethod(Function<StreamObserver<R>, StreamObserver<T>> method, List<T> parameters, String methodId) {
        StreamObserver<R> responseObserver = new StreamObserver<R>() {
            private int i = 0;
            @Override
            public void onNext(R response) {
                try {
                    log.info("[invokeBidiStreamingRpcMethod] Method {} returns {}", methodId, response);
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
            log.info("[invokeBidiStreamingRpcMethod] Invoke method {} with parameter {}", methodId, parameter);
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }
}