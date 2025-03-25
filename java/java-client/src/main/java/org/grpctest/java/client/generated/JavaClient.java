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
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
@Slf4j
public class JavaClient implements InitializingBean {

    private final Config config;

    private final HotpotServiceGrpc.HotpotServiceBlockingStub hotpotServiceBlockingStub;

    private final HotpotServiceGrpc.HotpotServiceStub hotpotServiceAsyncStub;

    private final String[] map_hotpot_MapPot_fields = new String[]{"getBoolDoubleValueMap", "getBoolIntValueMap", "getBoolBoolValueMap", "getBoolStringValueMap"};
    private final String[] map_hotpot_MapPot_BoolIntValueEntry_fields = new String[]{"getKey", "getValue"};
    private final String[] map_hotpot_MapPot_BoolBoolValueEntry_fields = new String[]{"getKey", "getValue"};
    private final String[] map_hotpot_MapPot_BoolDoubleValueEntry_fields = new String[]{"getKey", "getValue"};
    private final String[] map_hotpot_MapPot_BoolStringValueEntry_fields = new String[]{"getKey", "getValue"};

    public JavaClient(Config config, ClientInterceptor clientInterceptor) {
        this.config = config;
        Channel originChannel = ManagedChannelBuilder.forAddress(config.getServiceHost(), config.getServicePort()).usePlaintext().build();
        Channel channel = ClientInterceptors.intercept(originChannel, clientInterceptor);
        this.hotpotServiceBlockingStub = HotpotServiceGrpc.newBlockingStub(channel);
        this.hotpotServiceAsyncStub = HotpotServiceGrpc.newStub(channel);
        log.info("Connected to server at {}:{}", config.getServiceHost(), config.getServicePort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Client shutting down...");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Invoke test case: map_hotpot.HotpotService.unaryPot
        // invokeUnaryRpcMethod(hotpotServiceBlockingStub::unaryPot, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_unaryPot_param_0.bin", MapPot.class), "map_hotpot.HotpotService.unaryPot", map_hotpot_MapPot_fields, map_hotpot_MapPot_fields);
        invokeUnaryRpcMethodAsync(hotpotServiceAsyncStub::unaryPot, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_unaryPot_param_0.bin", MapPot.class), "map_hotpot.HotpotService.unaryPot", map_hotpot_MapPot_fields, map_hotpot_MapPot_fields);

        Thread.sleep(5000);
    }

    private <T, R> void invokeUnaryRpcMethod(Function<T, R> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        log.info("[invokeUnaryRpcMethod] {} - Request:\n{}", methodId, parameter);
        ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        try {
            R response = method.apply(parameter);
            log.info("[invokeUnaryRpcMethod] {} - Response:\n{}", methodId, response);
            ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
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

    private <T, R> void invokeUnaryRpcMethodAsync(BiConsumer<T, StreamObserver<R>> method,  T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        StreamObserver<R> responseObserver = new StreamObserver<R>() {
            @Override
            public void onNext(R response) {
                try {
                    log.info("[invokeUnaryRpcMethodAsync] {} - Response:\n{}", methodId, response);
                    ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
                    if (response instanceof GeneratedMessageV3) {
                        MessageUtil.messageToFile((GeneratedMessageV3) response, config.getOutDir() + File.separator + methodId.replace(".", "_") + "_return_0.bin");
                    } else {
                        log.error("[invokeUnaryRpcMethodAsync] Method {} returns message of type [{}], incompatible with protobuf", methodId, response.getClass());
                    }
                } catch (Throwable t) {
                    log.error("[invokeUnaryRpcMethodAsync] Error processing response {}", response, t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[invokeUnaryRpcMethodAsync] Method {} throws error", methodId, throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + methodId.replace(".", "_") + "_error.txt", throwable);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCompleted() {

            }
        };
        log.info("[invokeUnaryRpcMethodAsync] {} - Request:\n{}", methodId, parameter);
        ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        method.accept(parameter, responseObserver);
    }

    private <T, R> void invokeServerStreamingRpcMethod(Function<T, Iterator<R>> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        log.info("[invokeServerStreamingRpcMethod] {} - Request:\n{}", methodId, parameter);
        ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        try {
            Iterator<R> responses = method.apply(parameter);
            int i = 0;
            while (responses.hasNext()) {
                R response = responses.next();
                log.info("[invokeServerStreamingRpcMethod] {} - Response:\n{}", methodId, response);
                ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
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
                    log.info("[invokeClientStreamingRpcMethod] {} - Response:\n{}", methodId, response);
                    ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
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
            log.info("[invokeClientStreamingRpcMethod] {} - Request:\n{}", methodId, parameter);
            ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
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
                    log.info("[invokeBidiStreamingRpcMethod] {} - Response:\n{}", methodId, response);
                    ObjectUtil.logFieldsOfObject(response, methodId + " - response", responseTypeFieldNames);
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
            log.info("[invokeBidiStreamingRpcMethod] {} - Request:\n{}", methodId, parameter);
            ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }
}