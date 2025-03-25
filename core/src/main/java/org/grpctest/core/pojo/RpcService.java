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

}
