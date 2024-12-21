package org.grpctest.core.freemarker.datamodels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDataModel implements DataModel {
    private RpcModelRegistry registry;
    private TestcaseRegistry testcaseRegistry;
}
