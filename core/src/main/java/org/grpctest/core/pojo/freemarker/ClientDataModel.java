package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.RpcTestRegistry;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDataModel {
    private RpcTestRegistry registry;
}
