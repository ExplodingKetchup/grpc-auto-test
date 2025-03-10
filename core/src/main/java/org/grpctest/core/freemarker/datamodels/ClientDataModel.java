package org.grpctest.core.freemarker.datamodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.RpcModelRegistry;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDataModel implements DataModel {
    private RpcModelRegistry registry;

    private boolean logRequests;

    private boolean logRequestsPrintFields;

    private boolean logResponses;

    private boolean logResponsesPrintFields;
}
