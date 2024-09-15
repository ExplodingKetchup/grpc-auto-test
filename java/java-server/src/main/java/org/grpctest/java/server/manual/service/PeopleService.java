package org.grpctest.java.server.manual.service;

import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.common.define.*;

@Slf4j
public class PeopleService extends PeopleServiceGrpc.PeopleServiceImplBase {

    @Override
    public void getPerson(GetPersonRequest request, StreamObserver<GetPersonResponse> responseObserver) {
        responseObserver.onNext(getPersonUnaryImpl(request));
        responseObserver.onCompleted();
    }

    private GetPersonResponse getPersonUnaryImpl(GetPersonRequest request) {
        log.info("[getPersonUnaryImpl] Received request {}", request);
        String returnJson = "";
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
