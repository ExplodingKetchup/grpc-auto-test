package org.grpctest.core.service;

import org.grpctest.core.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
class ResultAnalyzerTest {
    @Autowired
    ProtobufReader protobufReader;

    @Autowired
    ResultAnalyzer resultAnalyzer;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void resultAnalyzerMockSpace() {

    }
}