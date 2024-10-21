package org.grpctest.core.service;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.ProtoContent;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.util.StringUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
@AllArgsConstructor
public class ProtobufReader {

    private final Config config;

    private final Registry registry;

    public ProtoContent loadProtoContent() throws Throwable {
        try (FileInputStream inputStream = new FileInputStream(config.getProtoDescriptorPath())) {

            // Get content of descriptor set
            DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(inputStream);

            // Loop through each .proto files
            ProtoContent protoContent = new ProtoContent();
            for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : descriptorSet.getFileList()) {
                log.info("[loadProtoContent] Reading content of [{}]", fileDescriptorProto.getName());

                // Build Descriptor
                Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[]{});

                // Loop through message types
                for (DescriptorProtos.DescriptorProto message : fileDescriptorProto.getMessageTypeList()) {
                    RpcMessage rpcMessage = new RpcMessage(message.getName(), fileDescriptor.findMessageTypeByName(message.getName()));
                    protoContent.getMessages().add(rpcMessage);
                    registry.addMessageToLookupTable(rpcMessage);
                }

                // Loop through service declarations
                for (DescriptorProtos.ServiceDescriptorProto service : fileDescriptorProto.getServiceList()) {
                    RpcService rpcService = new RpcService();
                    rpcService.setName(service.getName());
                    for (DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
                        RpcService.RpcMethod rpcMethod = RpcService.RpcMethod.builder()
                                .ownerServiceName(rpcService.getName())
                                .name(StringUtil.uncapitalizeFirstLetter(method.getName()))
                                .type(determineMethodType(method))
                                .inType(StringUtil.getShortenedClassName(method.getInputType()))
                                .outType(StringUtil.getShortenedClassName(method.getOutputType()))
                                .build();
                        rpcService.getMethods().add(rpcMethod);
                        registry.addMethod(rpcMethod);
                    }
                    protoContent.getServices().add(rpcService);
                    registry.addServiceAndMethodsToLookupTable(rpcService);
                }
            }
            return protoContent;
        } catch (IOException ioe) {
            log.error("[loadProtoContent] Fail to open resource at {}", config.getProtoDescriptorPath(), ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[loadProtoContent] An error occurred", t);
            throw t;
        }
    }

    private MethodDescriptor.MethodType determineMethodType(DescriptorProtos.MethodDescriptorProto method) {
        if (method.getClientStreaming()) {
            if (method.getServerStreaming()) {
                return MethodDescriptor.MethodType.BIDI_STREAMING;
            } else {
                return MethodDescriptor.MethodType.CLIENT_STREAMING;
            }
        } else {
            if (method.getServerStreaming()) {
                return MethodDescriptor.MethodType.SERVER_STREAMING;
            } else {
                return MethodDescriptor.MethodType.UNARY;
            }
        }
    }
}
