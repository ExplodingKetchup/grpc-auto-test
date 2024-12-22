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


    private static final Metadata.Key<byte[]> META_KEY_40 =
            Metadata.Key.of("40", Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_40 = HexFormat.of().parseHex("2941c33a9cd4");


    private static final Metadata.Key<byte[]> META_KEY_x406stC0I58 =
            Metadata.Key.of("x406stC0I58" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_x406stC0I58 = HexFormat.of().parseHex("c21db49568bbebf31f1919ee69ef64829a");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(META_KEY_40, META_VALUE_40);
                headers.put(META_KEY_x406stC0I58, META_VALUE_x406stC0I58);
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
