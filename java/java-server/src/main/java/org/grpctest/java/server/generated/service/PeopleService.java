package org.grpctest.java.server.generated.service;

import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.common.define.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PeopleService extends PeopleServiceGrpc.PeopleServiceImplBase {

    @Override
    public void getPerson(GetPersonRequest request, StreamObserver<GetPersonResponse> responseObserver) {
        responseObserver.onNext(getPersonImpl(request));
        responseObserver.onCompleted();
    }

    private GetPersonResponse getPersonImpl(GetPersonRequest request) {
        log.info("[getPersonImpl] Received request {}", request);
        String returnJson = "{\"person\":{\"id\":-927237482,\"name\":\"W7j1X\",\"age\":1181559083,\"occupation\":\"mgOj577\",\"gender\":\"MALE\",\"emails\":[\"KeW6x3zsR47Hy56\",\"5uPnJQTh1\",\"7Up06712\",\"A7dA3\",\"lI\"]}}";
        GetPersonResponse.Builder builder = GetPersonResponse.newBuilder();
        try {
            JsonFormat.parser().merge(returnJson, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("[getPersonImpl] Fail to parse return value from test case: [{}]", returnJson, e);
            return GetPersonResponse.newBuilder().build();
        }
    }
}
