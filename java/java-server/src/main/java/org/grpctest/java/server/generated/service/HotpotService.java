package org.grpctest.java.server.generated.service;

import com.google.protobuf.ByteString;
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
            log.info("[unaryPot] Request:\n{}", request);
            ObjectUtil.logFieldsOfObject(request, "default_hotpot.HotpotService.unaryPot - request", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");

            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "default_hotpot_HotpotService_unaryPot_param_0.bin");

            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "default_hotpot_HotpotService_unaryPot_return.bin",
                    (filepath) -> {
                        BigHotpotOfTerror response = BigHotpotOfTerror.newBuilder()
                                .setDoubleValue(0)
                                .setFloatValue(0)
                                .setInt32Value(0)
                                .setInt64Value(0)
                                .setUint32Value(0)
                                .setUint64Value(0)
                                .setSint32Value(0)
                                .setSint64Value(0)
                                .setFixed32Value(0)
                                .setFixed64Value(0)
                                .setSfixed32Value(0)
                                .setSfixed64Value(0)
                                .setBoolValue(false)
                                .setStringValue("")
                                .setBytesValue(ByteString.EMPTY)
                                .setEnumValue(AnExampleEnum.AEE_ZERO)
                                .build();
                        log.info("[unaryPot] Response:\n{}", response);
                        ObjectUtil.logFieldsOfObject(response, "default_hotpot.HotpotService.unaryPot - response", "getDoubleValue", "getFloatValue", "getInt32Value", "getInt64Value", "getUint32Value", "getUint64Value", "getSint32Value", "getSint64Value", "getFixed32Value", "getFixed64Value", "getSfixed32Value", "getSfixed64Value", "getBoolValue", "getStringValue", "getBytesValue", "getEnumValue", "getMessageValue");
                        responseObserver.onNext(response);
                    }
            );

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[unaryPot] An error occurred", t);
        }
    }

}
