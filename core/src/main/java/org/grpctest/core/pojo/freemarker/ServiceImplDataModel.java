package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.RpcTestRegistry;
import org.grpctest.core.pojo.RpcService;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceImplDataModel {
    private RpcService service;
    private RpcTestRegistry registry;
}
