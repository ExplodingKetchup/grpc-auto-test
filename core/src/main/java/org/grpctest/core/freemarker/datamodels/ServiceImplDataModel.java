package org.grpctest.core.freemarker.datamodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
import org.grpctest.core.pojo.RpcService;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceImplDataModel implements DataModel {
    private String serviceId;
    private RpcModelRegistry registry;
    private TestcaseRegistry testcaseRegistry;

    private boolean logRequests;

    private boolean logRequestsPrintFields;

    private boolean logResponses;

    private boolean logResponsesPrintFields;
}
