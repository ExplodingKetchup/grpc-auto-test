package org.grpctest.java.server.generated.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.*;
import org.grpctest.java.common.util.FileUtil;
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
            log.info("[getPerson] Received request {}", request);
            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "person_PeopleService_getPerson_param_0.bin");
            FileUtil.loopMultipleFilesWithSamePrefix(
                    config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_return.bin",
                    (filepath) -> responseObserver.onNext(MessageUtil.messageFromFile(filepath, GetPersonResponse.class))
            );

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[getPerson] An error occurred", t);
        }
    }

    @Override
    public void listPerson(GetPersonRequest request, StreamObserver<GetPersonResponse> responseObserver) {
        try {
            log.info("[listPerson] Received request {}", request);
            MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "person_PeopleService_listPerson_param.bin");
            responseObserver.onNext(MessageUtil.messageFromFile(config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_return_0.bin", GetPersonResponse.class));

            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("[listPerson] An error occurred", t);
        }
    }

    @Override
    public StreamObserver<GetPersonRequest> registerPerson(StreamObserver<GetPersonResponse> responseObserver) {
        return new StreamObserver<GetPersonRequest>() {
            private int requestIdx = 0;
            @Override
            public void onNext(GetPersonRequest request) {
                try {
                    log.info("[registerPerson] Received request {}", request);
                    MessageUtil.messageToFile(request, config.getOutDir() + File.separator + "person_PeopleService_listPerson_param_" + requestIdx + ".bin");
                    requestIdx++;
                } catch (Throwable t) {
                    log.error("[registerPerson] onNext: An exception occurred", t);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("[registerPerson] Client throw error", throwable);
                try {
                    MessageUtil.grpcExceptionToFile(config.getOutDir() + File.separator + "person_PeopleService_regusterPerson_error.txt", throwable);
                } catch (Exception e) {
                    log.error("[registerPerson] onError: An exception occurred", e);
                }
            }

            @Override
            public void onCompleted() {
                FileUtil.loopMultipleFilesWithSamePrefix(
                        config.getTestcaseDir() + File.separator + "person_PeopleService_getPerson_return.bin",
                        (filepath) -> responseObserver.onNext(MessageUtil.messageFromFile(filepath, GetPersonResponse.class))
                );

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GetPersonRequest> streamPerson(StreamObserver<GetPersonResponse> responseObserver) {
        return super.streamPerson(responseObserver);
    }
}
