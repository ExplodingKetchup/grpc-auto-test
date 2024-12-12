package org.grpctest.java.server.generated.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.common.util.MessageUtil;
import org.grpctest.java.server.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class PeopleService extends PeopleServiceGrpc.PeopleServiceImplBase {

    @Autowired
    private Config config;

    @Override
    public void getPerson(GetPersonRequest request, StreamObserver<GetPersonResponse> responseObserver) {
        try {
            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "person_PeopleService_getPerson_param.bin");
            responseObserver.onNext(getPersonImpl(request));
            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[getPerson] An error occurred", t);
        }
    }

    private GetPersonResponse getPersonImpl(GetPersonRequest request) {
        log.info("[getPersonImpl] Received request {}", request);
        return MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_return.bin", GetPersonResponse.class);
    }
}
