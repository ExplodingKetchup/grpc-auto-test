package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.pojo.RpcService;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaServerDataModel {
    private List<RpcService> rpcServices;
}
