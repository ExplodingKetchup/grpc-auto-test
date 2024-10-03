package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.RpcService;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceImplDataModel {
    private RpcService service;
    private Registry registry;
    private String testsDir;
}
