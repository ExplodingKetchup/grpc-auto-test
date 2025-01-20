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


    private static final Metadata.Key<byte[]> META_KEY_44deu1qso1 =
            Metadata.Key.of("44deu1qso1" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_44deu1qso1 = HexFormat.of().parseHex("b5854f1a");


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
                responseMetadata.put(META_KEY_44deu1qso1, META_VALUE_44deu1qso1);
                log.info("[interceptCall] Server -> Client metadata:\n{}", MessageUtil.formatMetadataForOutput(responseMetadata));
                super.sendHeaders(responseMetadata);
            }
        }, metadata);
    }
}
