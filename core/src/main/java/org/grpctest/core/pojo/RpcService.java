package org.grpctest.core.pojo;

import io.grpc.MethodDescriptor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all information related to a single RPC service, which could be used to
 * generate a service implementation class.
 */
@Data
@NoArgsConstructor
public class RpcService {

    private String ownerNamespaceName;

    private String name;

    private List<RpcMethod> methods = new ArrayList<>();

    @Data
    @Builder
    public static class RpcMethod {

        // Service that this method belongs to
        private String ownerServiceName;

        // Name of RPC method
        private String name;

        // Type of gRPC method
        private MethodDescriptor.MethodType type;

        // Name of the parameter (shortened class name)
        private String inType;

        // Name of the return (shortened class name)
        private String outType;
    }
}
