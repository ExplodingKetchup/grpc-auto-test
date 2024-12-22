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

<#assign metaMap = registry.getAllClientToServerMetadata()>
<#list metaMap?keys as metaKey>
    <#assign metaPair = metaMap[metaKey]>
    <#assign metaType = metaPair.getLeft().name()> <#-- MetadataType -->
    <#assign metaValue = metaPair.getRight()> <#-- Metadata value -->

    <#if metaType != "NONE">
        <#-- Generate the Metadata Key -->
        <#if metaType == "BIN">
    private static final Metadata.Key<byte[]> META_KEY_${metaKey} =
            Metadata.Key.of("${metaKey}" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);
        <#else>
    private static final Metadata.Key<String> META_KEY_${metaKey} =
            Metadata.Key.of("${metaKey}", Metadata.ASCII_STRING_MARSHALLER);
        </#if>

        <#-- Generate the Metadata Value -->
        <#if metaType == "BIN">
    private static final byte[] META_VALUE_${metaKey} = HexFormat.of().parseHex("${metaValue}");
        <#else>
    private static final String META_VALUE_${metaKey} = "${metaValue}";
        </#if>

    </#if>
</#list>

    @Autowired
    private Config config;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
<#list metaMap?keys as metaKey>
                headers.put(META_KEY_${metaKey}, META_VALUE_${metaKey});
</#list>
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
