package org.grpctest.core.service;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.RuntimeConfig;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

import static org.grpctest.core.constant.Constants.PROTO_DESCRIPTOR_PATH;

@Component
@Slf4j
@AllArgsConstructor
public class ProtobufReader {

    private final Config config;

    private final TestcaseRegistry testcaseRegistry;

    private final RpcModelRegistry rpcModelRegistry;

    public void loadProtoContent(RuntimeConfig runtimeConfig) throws Throwable {
        try (FileInputStream inputStream = new FileInputStream(PROTO_DESCRIPTOR_PATH)) {

            // Get content of descriptor set
            DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(inputStream);

            // Loop through each .proto files
            for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : descriptorSet.getFileList()) {
                if (!runtimeConfig.getIncludedProtos().isEmpty()) {
                    if (!runtimeConfig.getIncludedProtos().contains(fileDescriptorProto.getName())) {
                        continue;
                    }
                }
                log.info("[loadProtoContent] Reading content of [{}]", fileDescriptorProto.getName());
                rpcModelRegistry.addProtoFilename(fileDescriptorProto.getName().substring(0, fileDescriptorProto.getName().length() - 6));

                // Build Descriptor
                Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[]{});
                String namespace = fileDescriptor.getPackage();

                // Loop through message types
                for (DescriptorProtos.DescriptorProto message : fileDescriptorProto.getMessageTypeList()) {
                    RpcMessage rpcMessage = new RpcMessage(namespace, message.getName(), fileDescriptor.findMessageTypeByName(message.getName()));
                    rpcModelRegistry.addMessageToLookupTable(rpcMessage);
                }

                // Loop through service declarations
                for (DescriptorProtos.ServiceDescriptorProto service : fileDescriptorProto.getServiceList()) {
                    RpcService rpcService = new RpcService(namespace, service.getName());
                    for (DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
                        RpcService.RpcMethod rpcMethod = RpcService.RpcMethod.builder()
                                .ownerServiceId(rpcService.getId())
                                .name(StringUtil.uncapitalizeFirstLetter(method.getName()))
                                .type(determineMethodType(method))
                                .inType(method.getInputType().substring(1))
                                .outType(method.getOutputType().substring(1))
                                .build();
                        rpcMethod.deriveId();
                        rpcService.getMethods().add(rpcMethod.getId());
                        testcaseRegistry.addMethod(rpcMethod);
                        rpcModelRegistry.addMethodToLookupTable(rpcMethod);
                    }
                    rpcModelRegistry.addServiceToLookupTable(rpcService);
                }
            }
        } catch (IOException ioe) {
            log.error("[loadProtoContent] Fail to open resource at {}", PROTO_DESCRIPTOR_PATH, ioe);
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
