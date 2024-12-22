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

<#assign metaMap = registry.getAllServerToClientMetadata()>
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
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        log.info("[interceptCall] Received metadata from client: {}", metadata);
        try {
            MessageUtil.metadataToFile(config.getOutDir() + File.separator + "received_metadata.txt", metadata);
        } catch (IOException ioe) {
        }
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseMetadata) {
<#list metaMap?keys as metaKey>
                responseMetadata.put(META_KEY_${metaKey}, META_VALUE_${metaKey});
</#list>
                log.info("[interceptCall] Server -> Client metadata:\n{}", MessageUtil.formatMetadataForOutput(responseMetadata));
                super.sendHeaders(responseMetadata);
            }
        }, metadata);
    }
}
