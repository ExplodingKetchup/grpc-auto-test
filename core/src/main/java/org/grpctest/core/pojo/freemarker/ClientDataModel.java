package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.data.Registry;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDataModel implements DataModel {
    private Registry registry;
    private String testsDir;
}
