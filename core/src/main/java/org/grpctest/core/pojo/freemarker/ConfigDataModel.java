package org.grpctest.core.pojo.freemarker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.grpctest.core.config.Config;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigDataModel implements DataModel {
    private Config config;
}
