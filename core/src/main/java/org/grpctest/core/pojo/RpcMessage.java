package org.grpctest.core.pojo;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a protobuf message
 */
@Data
@AllArgsConstructor
public class RpcMessage {
    private String ownerNamespaceName;
    private String name;
    private Descriptors.Descriptor messageDescriptor;
}
