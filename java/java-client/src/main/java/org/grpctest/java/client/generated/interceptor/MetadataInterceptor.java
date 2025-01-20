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


    private static final Metadata.Key<byte[]> META_KEY_abbu908ml1796p471i =
            Metadata.Key.of("abbu908ml1796p471i" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_abbu908ml1796p471i = HexFormat.of().parseHex("297520bf");


    private static final Metadata.Key<byte[]> META_KEY_5go3gcv428n053tr5u7 =
            Metadata.Key.of("5go3gcv428n053tr5u7" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_5go3gcv428n053tr5u7 = HexFormat.of().parseHex("3035ed94");


    private static final Metadata.Key<byte[]> META_KEY_0bj5fadu =
            Metadata.Key.of("0bj5fadu" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_0bj5fadu = HexFormat.of().parseHex("c1f1e750673fb043fb54e1da3f76a242b7");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(META_KEY_abbu908ml1796p471i, META_VALUE_abbu908ml1796p471i);
                headers.put(META_KEY_5go3gcv428n053tr5u7, META_VALUE_5go3gcv428n053tr5u7);
                headers.put(META_KEY_0bj5fadu, META_VALUE_0bj5fadu);
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
