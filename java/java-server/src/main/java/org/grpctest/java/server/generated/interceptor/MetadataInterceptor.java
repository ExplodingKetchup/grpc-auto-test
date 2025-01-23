package org.grpctest.java.server.generated.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.util.FileUtil;
import org.grpctest.java.common.util.MessageUtil;
import org.grpctest.java.server.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

@Component
@Slf4j
public class MetadataInterceptor implements ServerInterceptor {


    private static final Metadata.Key<byte[]> META_KEY_95kpg7 =
            Metadata.Key.of("95kpg7" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_95kpg7 = HexFormat.of().parseHex("507bcdf1e6d7b25e434766811f7fd7473cfa");


    private static final Metadata.Key<byte[]> META_KEY_3t7im2hev089d383 =
            Metadata.Key.of("3t7im2hev089d383" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_3t7im2hev089d383 = HexFormat.of().parseHex("1d08c0cf8f26cb8eb7f4c538f427");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        log.info("[interceptCall] Received metadata from client: {}", metadata);
        try {
            MessageUtil.metadataToFile(config.getOutDir() + File.separator + "received_metadata.txt", metadata);
        } catch (IOException ioe) {
        }
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseMetadata) {
                responseMetadata.put(META_KEY_95kpg7, META_VALUE_95kpg7);
                responseMetadata.put(META_KEY_3t7im2hev089d383, META_VALUE_3t7im2hev089d383);
                log.info("[interceptCall] Server -> Client metadata:\n{}", MessageUtil.formatMetadataForOutput(responseMetadata));
                super.sendHeaders(responseMetadata);
            }
        }, metadata);
    }
}
