package org.grpctest.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtoContent {

    private List<RpcMessage> messages = new ArrayList<>();

    private List<RpcService> services = new ArrayList<>();
}
