package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.Registry;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.service.util.DynamicMessageUtilService;
import org.grpctest.core.util.JsonUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class CustomTestCaseReader {

    private final Config config;
    private final ResourceLoader resourceLoader;

    private final Registry registry;

    private final DynamicMessageUtilService dynamicMessageUtilService;

    private List<TestCase> loadTestCases() {
        List<TestCase> testCases = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String classpath = "classpath:" + config.getCustomTestsClasspath() + "/*";
        try {
            for (Resource resource : resourcePatternResolver.getResources(classpath)) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String json = bufferedReader.lines().collect(Collectors.joining("\n"));
                    testCases.add(JsonUtil.fromJson(json, TestCase.class));
                }
            }
        } catch (IOException ioe) {
            log.error("[loadTestCases] Failed to read test cases at {}", classpath, ioe);
        } catch (Throwable t) {
            log.error("[loadTestCases] An error occurred", t);
        }
        return testCases;
    }

    public void loadTestCasesToRegistry() {
        List<TestCase> testCases = loadTestCases();
        for (TestCase testCase : testCases) {
            testCase.setParamValueJson(generateParamValueJson(testCase));
            testCase.setParamValueDynMsg(
                    dynamicMessageUtilService.objectToDynamicMessage(
                            testCase.getParamValue(),
                            registry.lookupMessage(
                                    registry.lookupMethod(testCase.getServiceName(), testCase.getMethodName()).getInType()
                            )
                    )
            );
            testCase.setReturnValueJson(generateReturnValueJson(testCase));
            testCase.setReturnValueDynMsg(
                    dynamicMessageUtilService.objectToDynamicMessage(
                            testCase.getReturnValue(),
                            registry.lookupMessage(
                                    registry.lookupMethod(testCase.getServiceName(), testCase.getMethodName()).getOutType()
                            )
                    )
            );
            registry.addTestCase(testCase);
        }
        log.info("[loadTestCasesToRegistry] Finished loading test cases");
    }

    /**
     * Note: this json string is the string directly written into Java source code.
     * Therefore, the double quotes have to be preceded by "\".
     */
    private String generateReturnValueJson(TestCase testCase) {
        String json = null;
        try {
            json = JsonUtil.toJson(testCase.getReturnValue());
        } catch (Exception e) {
            log.error("[testCaseToReturnValueJson] Error when converting to json", e);
        }
        if (Objects.isNull(json)) {
            return json;
        }
        return json.replace("\"", "\\\"");
    }

    /**
     * Note: this json string is the string directly written into Java source code.
     * Therefore, the double quotes have to be preceded by "\".
     */
    private String generateParamValueJson(TestCase testCase) {
        String json = null;
        try {
            json = JsonUtil.toJson(testCase.getParamValue());
        } catch (Exception e) {
            log.error("[testCaseToReturnValueJson] Error when converting to json", e);
        }
        if (Objects.isNull(json)) {
            return json;
        }
        return json.replace("\"", "\\\"");
    }
}
