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
    public void unaryPot(MapPot request, StreamObserver<MapPotReversed> responseObserver) {
        try {
            log.info("[unaryPot] Request:\n{}", request);
            ObjectUtil.logFieldsOfObject(request, "map_hotpot.HotpotService.unaryPot - request", "getIntDoubleValueMap", "getIntIntValueMap", "getIntBoolValueMap", "getIntStringValueMap", "getIntBytesValueMap", "getIntEnumValueMap", "getBoolDoubleValueMap", "getBoolBoolValueMap", "getBoolStringValueMap", "getBoolBytesValueMap", "getBoolEnumValueMap", "getStringDoubleValueMap", "getStringStringValueMap", "getStringBytesValueMap", "getStringEnumValueMap");

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "map_hotpot_HotpotService_unaryPot_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_unaryPot_return.bin",
                    (filepath) -> {
                        MapPotReversed response = MessageUtil.messageFromFile(filepath, MapPotReversed.class);
                        log.info("[unaryPot] Response:\n{}", response);
                        ObjectUtil.logFieldsOfObject(response, "map_hotpot.HotpotService.unaryPot - response", "getStringEnumValueMap", "getStringBytesValueMap", "getStringStringValueMap", "getStringDoubleValueMap", "getBoolEnumValueMap", "getBoolBytesValueMap", "getBoolStringValueMap", "getBoolBoolValueMap", "getBoolDoubleValueMap", "getIntEnumValueMap", "getIntBytesValueMap", "getIntStringValueMap", "getIntBoolValueMap", "getIntIntValueMap", "getIntDoubleValueMap");
                        responseObserver.onNext(response);
                    }
            );

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[unaryPot] An error occurred", t);
        }
    }

    @Override
    public void serverStreamingPot(MapPot request, StreamObserver<MapPotReversed> responseObserver) {
        try {
            log.info("[serverStreamingPot] Request:\n{}", request);
            ObjectUtil.logFieldsOfObject(request, "map_hotpot.HotpotService.serverStreamingPot - request", "getIntDoubleValueMap", "getIntIntValueMap", "getIntBoolValueMap", "getIntStringValueMap", "getIntBytesValueMap", "getIntEnumValueMap", "getBoolDoubleValueMap", "getBoolBoolValueMap", "getBoolStringValueMap", "getBoolBytesValueMap", "getBoolEnumValueMap", "getStringDoubleValueMap", "getStringStringValueMap", "getStringBytesValueMap", "getStringEnumValueMap");

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "map_hotpot_HotpotService_serverStreamingPot_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_serverStreamingPot_return.bin",
                    (filepath) -> {
                        MapPotReversed response = MessageUtil.messageFromFile(filepath, MapPotReversed.class);
                        log.info("[serverStreamingPot] Response:\n{}", response);
                        ObjectUtil.logFieldsOfObject(response, "map_hotpot.HotpotService.serverStreamingPot - response", "getStringEnumValueMap", "getStringBytesValueMap", "getStringStringValueMap", "getStringDoubleValueMap", "getBoolEnumValueMap", "getBoolBytesValueMap", "getBoolStringValueMap", "getBoolBoolValueMap", "getBoolDoubleValueMap", "getIntEnumValueMap", "getIntBytesValueMap", "getIntStringValueMap", "getIntBoolValueMap", "getIntIntValueMap", "getIntDoubleValueMap");
                        responseObserver.onNext(response);
                    }
            );

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[serverStreamingPot] An error occurred", t);
        }
    }

    @Override
    public StreamObserver<MapPot> clientStreamingPot(StreamObserver<MapPotReversed> responseObserver) {
        return new StreamObserver<MapPot>() {
            private int requestIdx = 0;
            @Override
            public void onNext(MapPot request) {
                try {
                    log.info("[clientStreamingPot] Request:\n{}", request);
                    ObjectUtil.logFieldsOfObject(request, "map_hotpot.HotpotService.clientStreamingPot - request", "getIntDoubleValueMap", "getIntIntValueMap", "getIntBoolValueMap", "getIntStringValueMap", "getIntBytesValueMap", "getIntEnumValueMap", "getBoolDoubleValueMap", "getBoolBoolValueMap", "getBoolStringValueMap", "getBoolBytesValueMap", "getBoolEnumValueMap", "getStringDoubleValueMap", "getStringStringValueMap", "getStringBytesValueMap", "getStringEnumValueMap");
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "map_hotpot_HotpotService_clientStreamingPot_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[clientStreamingPot] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[clientStreamingPot] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "map_hotpot_HotpotService_clientStreamingPot_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[clientStreamingPot] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_clientStreamingPot_return.bin",
                        (filepath) -> {
                            MapPotReversed response = MessageUtil.messageFromFile(filepath, MapPotReversed.class);
                            log.info("[clientStreamingPot] Response:\n{}", response);
                            ObjectUtil.logFieldsOfObject(response, "map_hotpot.HotpotService.clientStreamingPot - response", "getStringEnumValueMap", "getStringBytesValueMap", "getStringStringValueMap", "getStringDoubleValueMap", "getBoolEnumValueMap", "getBoolBytesValueMap", "getBoolStringValueMap", "getBoolBoolValueMap", "getBoolDoubleValueMap", "getIntEnumValueMap", "getIntBytesValueMap", "getIntStringValueMap", "getIntBoolValueMap", "getIntIntValueMap", "getIntDoubleValueMap");
                            responseObserver.onNext(response);
                        }
                );

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<MapPot> bidiStreamingPot(StreamObserver<MapPotReversed> responseObserver) {
        return new StreamObserver<MapPot>() {
            private int requestIdx = 0;
            @Override
            public void onNext(MapPot request) {
                try {
                    log.info("[bidiStreamingPot] Request:\n{}", request);
                    ObjectUtil.logFieldsOfObject(request, "map_hotpot.HotpotService.bidiStreamingPot - request", "getIntDoubleValueMap", "getIntIntValueMap", "getIntBoolValueMap", "getIntStringValueMap", "getIntBytesValueMap", "getIntEnumValueMap", "getBoolDoubleValueMap", "getBoolBoolValueMap", "getBoolStringValueMap", "getBoolBytesValueMap", "getBoolEnumValueMap", "getStringDoubleValueMap", "getStringStringValueMap", "getStringBytesValueMap", "getStringEnumValueMap");
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "map_hotpot_HotpotService_bidiStreamingPot_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[bidiStreamingPot] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[bidiStreamingPot] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "map_hotpot_HotpotService_bidiStreamingPot_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[bidiStreamingPot] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "map_hotpot_HotpotService_bidiStreamingPot_return.bin",
                        (filepath) -> {
                            MapPotReversed response = MessageUtil.messageFromFile(filepath, MapPotReversed.class);
                            log.info("[bidiStreamingPot] Response:\n{}", response);
                            ObjectUtil.logFieldsOfObject(response, "map_hotpot.HotpotService.bidiStreamingPot - response", "getStringEnumValueMap", "getStringBytesValueMap", "getStringStringValueMap", "getStringDoubleValueMap", "getBoolEnumValueMap", "getBoolBytesValueMap", "getBoolStringValueMap", "getBoolBoolValueMap", "getBoolDoubleValueMap", "getIntEnumValueMap", "getIntBytesValueMap", "getIntStringValueMap", "getIntBoolValueMap", "getIntIntValueMap", "getIntDoubleValueMap");
                            responseObserver.onNext(response);
                        }
                );

                responseObserver.onCompleted();
            }
        };
    }

}
