package org.grpctest.java.server.generated.service;

import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.common.define.*;

@Slf4j
public class ${service.name} extends ${service.name}Grpc.${service.name}ImplBase {

<#list service.methods as method>
    @Override
    <#if method.type == "UNARY">
    public void ${method.name}(${method.inType} request, StreamObserver<${method.outType}> responseObserver) {
        responseObserver.onNext(${method.name}Impl(request));
        responseObserver.onCompleted();
    }
    </#if>

    private ${method.outType} ${method.name}Impl(${method.inType} request) {
        log.info("[${method.name}Impl] Received request {}", request);
        String returnJson = "${registry.getMethodTestCases(method)[0].returnValueJson}";
        ${method.outType}.Builder builder = ${method.outType}.newBuilder();
        try {
            JsonFormat.parser().merge(returnJson, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("[${method.name}Impl] Fail to parse return value from test case: [{}]", returnJson, e);
            return ${method.outType}.newBuilder().build();
        }
    }
</#list>
}
