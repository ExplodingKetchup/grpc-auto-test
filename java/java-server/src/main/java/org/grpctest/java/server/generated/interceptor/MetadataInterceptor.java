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


    private static final Metadata.Key<String> META_KEY_84d =
            Metadata.Key.of("84d", Metadata.ASCII_STRING_MARSHALLER);

    private static final String META_VALUE_84d = "5r0f271h";


    private static final Metadata.Key<String> META_KEY_885L =
            Metadata.Key.of("885L", Metadata.ASCII_STRING_MARSHALLER);

    private static final String META_VALUE_885L = "D7L4Q3";


    private static final Metadata.Key<String> META_KEY_Ul =
            Metadata.Key.of("Ul", Metadata.ASCII_STRING_MARSHALLER);

    private static final String META_VALUE_Ul = "K48Low2p859x85";


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
                responseMetadata.put(META_KEY_84d, META_VALUE_84d);
                responseMetadata.put(META_KEY_885L, META_VALUE_885L);
                responseMetadata.put(META_KEY_Ul, META_VALUE_Ul);
                super.sendHeaders(responseMetadata);
            }
        }, metadata);
    }
}
