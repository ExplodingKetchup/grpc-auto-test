package org.grpctest.core.pojo;

import io.grpc.MethodDescriptor;
import lombok.Builder;
import lombok.Data;
import org.grpctest.core.util.StringUtil;

@Data
@Builder
public class RpcMethod {

    /**
     * namespace.service.method
     */
    private String id;

    // Service that this method belongs to
    private String ownerServiceId;

    // Name of RPC method
    private String name;

    // Type of gRPC method
    private MethodDescriptor.MethodType type;

    // ID of the parameter
    private String inType;

    // ID of the return value
    private String outType;

    /**
     * Create id in format namespace.service.method<br>
     * Set {@code id} property.
     */
    public void deriveId() {
        String[] owner = ownerServiceId.split("\\.");
        this.id = StringUtil.getMethodId(owner[0], owner[1], name);
    }
}
