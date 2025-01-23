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


    private static final Metadata.Key<byte[]> META_KEY_1wy4 =
            Metadata.Key.of("1wy4" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_1wy4 = HexFormat.of().parseHex("bb22825a211bdb");


    private static final Metadata.Key<byte[]> META_KEY_b2j8 =
            Metadata.Key.of("b2j8" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_b2j8 = HexFormat.of().parseHex("c95074f7be5ce711b203ce69f4");


    private static final Metadata.Key<byte[]> META_KEY_w5c6ej849bl4ug7e39 =
            Metadata.Key.of("w5c6ej849bl4ug7e39" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_w5c6ej849bl4ug7e39 = HexFormat.of().parseHex("9b5fcfa4");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(META_KEY_1wy4, META_VALUE_1wy4);
                headers.put(META_KEY_b2j8, META_VALUE_b2j8);
                headers.put(META_KEY_w5c6ej849bl4ug7e39, META_VALUE_w5c6ej849bl4ug7e39);
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
