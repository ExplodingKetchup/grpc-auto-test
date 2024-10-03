package org.grpctest.java.server.util;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.java.common.define.GetPersonResponse;
import org.grpctest.java.common.define.Person;
import org.grpctest.java.common.util.MessageUtil;
import org.junit.jupiter.api.Test;

@Slf4j
class MessageUtilTest {

    @Test
    void messageFromFile() {
        GetPersonResponse response = MessageUtil.messageFromFile("../../test-cases/PeopleService_getPerson_return.bin", GetPersonResponse.class);
        log.info("response = {}", response);
    }
}