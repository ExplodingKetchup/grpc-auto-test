package org.grpctest.core.pojo;

import io.grpc.MethodDescriptor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all information related to a single RPC service, which could be used to
 * generate a service implementation class.
 */
@Data
@NoArgsConstructor
public class RpcService {

    /** namespace.service */
    private String id;

    private String ownerNamespaceName;

    private String name;

    /** List of ids of methods belonging to this Service */
    private List<String> methods = new ArrayList<>();

    public RpcService(String ownerNamespaceName, String name) {
        this.ownerNamespaceName = ownerNamespaceName;
        this.name = name;
        this.id = StringUtil.getServiceId(ownerNamespaceName, name);
    }

    @Data
    @Builder
    public static class RpcMethod {

        /** namespace.service.method */
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
}
