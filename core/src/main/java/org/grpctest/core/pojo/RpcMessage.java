package org.grpctest.core.pojo;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.grpctest.core.util.StringUtil;

/**
 * Represents a protobuf message
 */
@Data
@AllArgsConstructor
public class RpcMessage {
    /** namespace.message */
    private String id;
    private String ownerNamespaceName;
    private String name;
    private Descriptors.Descriptor messageDescriptor;

    public RpcMessage(String ownerNamespaceName, String name, Descriptors.Descriptor messageDescriptor) {
        this.ownerNamespaceName = ownerNamespaceName;
        this.name = name;
        this.messageDescriptor = messageDescriptor;
        this.id = StringUtil.getMessageId(ownerNamespaceName, name);
    }
}
