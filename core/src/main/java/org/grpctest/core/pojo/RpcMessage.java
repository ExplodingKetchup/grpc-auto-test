package org.grpctest.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a protobuf message
 */
@Data
@AllArgsConstructor
public class RpcMessage {
    private String name;
}
