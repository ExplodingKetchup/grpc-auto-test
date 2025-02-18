package org.grpctest.java.server.generated.service;

import io.grpc.Metadata;
import io.grpc.Status;
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
public class HotpotService extends HotpotServiceGrpc.HotpotServiceImplBase {

    @Autowired
    private Config config;

    @Override
    public void unaryPot(BigHotpotOfTerror request, StreamObserver<BigHotpotOfTerror> responseObserver) {
        try {
            log.info("[unaryPot] Request: {}", request);
            ObjectUtil.logFieldsOfObject(request, "default_hotpot.HotpotService.unaryPot - request", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "default_hotpot_HotpotService_unaryPot_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "default_hotpot_HotpotService_unaryPot_return.bin",
                    (filepath) -> {
                        BigHotpotOfTerror response = MessageUtil.messageFromFile(filepath, BigHotpotOfTerror.class);
                        log.info("[unaryPot] Response: {}", response);
                        ObjectUtil.logFieldsOfObject(response, "default_hotpot.HotpotService.unaryPot - response", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                        responseObserver.onNext(response);
                    }
            );

            responseObserver.onError();
            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[unaryPot] An error occurred", t);
        }
    }

    @Override
    public void serverStreamingPot(BigHotpotOfTerror request, StreamObserver<BigHotpotOfTerror> responseObserver) {
        try {
            log.info("[serverStreamingPot] Request: {}", request);
            ObjectUtil.logFieldsOfObject(request, "default_hotpot.HotpotService.serverStreamingPot - request", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "default_hotpot_HotpotService_serverStreamingPot_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "default_hotpot_HotpotService_serverStreamingPot_return.bin",
                    (filepath) -> {
                        BigHotpotOfTerror response = MessageUtil.messageFromFile(filepath, BigHotpotOfTerror.class);
                        log.info("[serverStreamingPot] Response: {}", response);
                        ObjectUtil.logFieldsOfObject(response, "default_hotpot.HotpotService.serverStreamingPot - response", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                        responseObserver.onNext(response);
                    }
            );

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[serverStreamingPot] An error occurred", t);
        }
    }

    @Override
    public StreamObserver<BigHotpotOfTerror> clientStreamingPot(StreamObserver<BigHotpotOfTerror> responseObserver) {
        return new StreamObserver<BigHotpotOfTerror>() {
            private int requestIdx = 0;
            @Override
            public void onNext(BigHotpotOfTerror request) {
                try {
                    log.info("[clientStreamingPot] Request: {}", request);
                    ObjectUtil.logFieldsOfObject(request, "default_hotpot.HotpotService.clientStreamingPot - request", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "default_hotpot_HotpotService_clientStreamingPot_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[clientStreamingPot] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[clientStreamingPot] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "default_hotpot_HotpotService_clientStreamingPot_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[clientStreamingPot] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "default_hotpot_HotpotService_clientStreamingPot_return.bin",
                        (filepath) -> {
                            BigHotpotOfTerror response = MessageUtil.messageFromFile(filepath, BigHotpotOfTerror.class);
                            log.info("[clientStreamingPot] Response: {}", response);
                            ObjectUtil.logFieldsOfObject(response, "default_hotpot.HotpotService.clientStreamingPot - response", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                            responseObserver.onNext(response);
                        }
                );

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<BigHotpotOfTerror> bidiStreamingPot(StreamObserver<BigHotpotOfTerror> responseObserver) {
        return new StreamObserver<BigHotpotOfTerror>() {
            private int requestIdx = 0;
            @Override
            public void onNext(BigHotpotOfTerror request) {
                try {
                    log.info("[bidiStreamingPot] Request: {}", request);
                    ObjectUtil.logFieldsOfObject(request, "default_hotpot.HotpotService.bidiStreamingPot - request", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "default_hotpot_HotpotService_bidiStreamingPot_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[bidiStreamingPot] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[bidiStreamingPot] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "default_hotpot_HotpotService_bidiStreamingPot_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[bidiStreamingPot] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "default_hotpot_HotpotService_bidiStreamingPot_return.bin",
                        (filepath) -> {
                            BigHotpotOfTerror response = MessageUtil.messageFromFile(filepath, BigHotpotOfTerror.class);
                            log.info("[bidiStreamingPot] Response: {}", response);
                            ObjectUtil.logFieldsOfObject(response, "default_hotpot.HotpotService.bidiStreamingPot - response", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                            responseObserver.onNext(response);
                        }
                );

                responseObserver.onCompleted();
            }
        };
    }

}
