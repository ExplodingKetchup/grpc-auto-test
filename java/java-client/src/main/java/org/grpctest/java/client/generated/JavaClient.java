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

    private final HotpotServiceGrpc.HotpotServiceBlockingStub hotpotServiceBlockingStub;

    private final HotpotServiceGrpc.HotpotServiceStub hotpotServiceAsyncStub;

    private final String[] single_hotpot_RequestMessage_fields = new String[]{"SmallHotpot", "FloatBoat"};
    private final String[] single_hotpot_BigHotpotOfTerror_fields = new String[]{"DoubleValue", "FloatValue", "Int32Value", "Int64Value", "Uint32Value", "Uint64Value", "Sint32Value", "Sint64Value", "Fixed32Value", "Fixed64Value", "Sfixed32Value", "Sfixed64Value", "BoolValue", "StringValue", "BytesValue", "EnumValue", "MessageValue"};
    private final String[] single_hotpot_ResponseMessage_fields = new String[]{"BigHotpot", "FlexTape"};
    private final String[] single_hotpot_SmallHotpotOfRickeridoo_fields = new String[]{"SmallUint32Value", "SmallStringValue"};

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
        // Invoke test case: single_hotpot.HotpotService.serverStreamingPot
        invokeServerStreamingRpcMethod(hotpotServiceBlockingStub::serverStreamingPot, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "single_hotpot_HotpotService_serverStreamingPot_param_0.bin", RequestMessage.class), "single_hotpot.HotpotService.serverStreamingPot", single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);

        // Invoke test case: single_hotpot.HotpotService.clientStreamingPot
        invokeClientStreamingRpcMethod(hotpotServiceAsyncStub::clientStreamingPot, MessageUtil.messageListFromMultipleFiles(config.getTestcaseDir() + File.separator + "single_hotpot_HotpotService_clientStreamingPot_param.bin", RequestMessage.class), "single_hotpot.HotpotService.clientStreamingPot", single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);

        // Invoke test case: single_hotpot.HotpotService.bidiStreamingPot
        invokeBidiStreamingRpcMethod(hotpotServiceAsyncStub::bidiStreamingPot, MessageUtil.messageListFromMultipleFiles(config.getTestcaseDir() + File.separator + "single_hotpot_HotpotService_bidiStreamingPot_param.bin", RequestMessage.class), "single_hotpot.HotpotService.bidiStreamingPot", single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);

        // Invoke test case: single_hotpot.HotpotService.unaryPot
        invokeUnaryRpcMethod(hotpotServiceBlockingStub::unaryPot, MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "single_hotpot_HotpotService_unaryPot_param_0.bin", RequestMessage.class), "single_hotpot.HotpotService.unaryPot", single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);

    }

    private <T, R> void invokeUnaryRpcMethod(Function<T, R> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        log.info("[invokeUnaryRpcMethod] Received request {}", parameter);
        ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        try {
            R response = method.apply(parameter);
            log.info("[invokeUnaryRpcMethod] Response: {}", response);
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

    private <T, R> void invokeServerStreamingRpcMethod(Function<T, Iterator<R>> method, T parameter, String methodId, String[] requestTypeFieldNames, String[] responseTypeFieldNames) {
        log.info("[invokeServerStreamingRpcMethod] Received request {}", parameter);
        ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
        try {
            Iterator<R> responses = method.apply(parameter);
            int i = 0;
            while (responses.hasNext()) {
                R response = responses.next();
                log.info("[invokeServerStreamingRpcMethod] Response: {}", response);
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
                    log.info("[invokeClientStreamingRpcMethod] Response: {}", response);
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
            log.info("[invokeClientStreamingRpcMethod] Received request {}", parameter);
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
                    log.info("[invokeBidiStreamingRpcMethod] Response: {}", response);
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
            log.info("[invokeBidiStreamingRpcMethod] Received request {}", parameter);
            ObjectUtil.logFieldsOfObject(parameter, methodId + " - request", requestTypeFieldNames);
            requestObserver.onNext(parameter);
        }
        requestObserver.onCompleted();
    }
}