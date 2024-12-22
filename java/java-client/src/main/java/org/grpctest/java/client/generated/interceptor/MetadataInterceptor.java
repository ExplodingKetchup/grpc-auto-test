package org.grpctest.java.client.generated.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.client.config.Config;
import org.grpctest.java.common.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

@Component
@Slf4j
public class MetadataInterceptor implements ClientInterceptor {


    private static final Metadata.Key<byte[]> META_KEY_Q8I6P48a0f57 =
            Metadata.Key.of("Q8I6P48a0f57" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_Q8I6P48a0f57 = HexFormat.of().parseHex("b1bf3c45c90a4a2480f2");


    private static final Metadata.Key<byte[]> META_KEY_7K7tj3P10c =
            Metadata.Key.of("7K7tj3P10c" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_7K7tj3P10c = HexFormat.of().parseHex("5d890b60fb091ca4674f73eb67cb9f94ad");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(META_KEY_Q8I6P48a0f57, META_VALUE_Q8I6P48a0f57);
                headers.put(META_KEY_7K7tj3P10c, META_VALUE_7K7tj3P10c);
                log.info("[interceptCall] Client -> Server metadata:\n{}", MessageUtil.formatMetadataForOutput(headers));
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        log.info("[onHeaders] Received metadata from server: {}", headers);
                        try {
                            MessageUtil.metadataToFile(config.getOutDir() + File.separator + "received_metadata.txt", headers);
                        } catch (IOException ioe) {
                        }
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
