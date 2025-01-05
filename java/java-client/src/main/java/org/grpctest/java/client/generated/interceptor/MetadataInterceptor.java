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


    private static final Metadata.Key<byte[]> META_KEY_455z4ykfevy1th =
            Metadata.Key.of("455z4ykfevy1th" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_455z4ykfevy1th = HexFormat.of().parseHex("47d49bdf4f9a62d0e41f8133a558");


    private static final Metadata.Key<byte[]> META_KEY_540zm0t8bis65b7637 =
            Metadata.Key.of("540zm0t8bis65b7637" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_540zm0t8bis65b7637 = HexFormat.of().parseHex("fe9483efd323");


    private static final Metadata.Key<byte[]> META_KEY_6g3mdx3z633u =
            Metadata.Key.of("6g3mdx3z633u" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);

    private static final byte[] META_VALUE_6g3mdx3z633u = HexFormat.of().parseHex("feef64ea1f9208a0");


    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(META_KEY_455z4ykfevy1th, META_VALUE_455z4ykfevy1th);
                headers.put(META_KEY_540zm0t8bis65b7637, META_VALUE_540zm0t8bis65b7637);
                headers.put(META_KEY_6g3mdx3z633u, META_VALUE_6g3mdx3z633u);
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
