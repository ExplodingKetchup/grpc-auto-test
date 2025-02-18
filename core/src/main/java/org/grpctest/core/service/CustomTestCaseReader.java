package org.grpctest.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.config.Config;
import org.grpctest.core.data.RpcModelRegistry;
import org.grpctest.core.data.TestcaseRegistry;
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

import static org.grpctest.core.constant.Constants.CUSTOM_TESTS_CLASSPATH;

@Component
@Slf4j
@AllArgsConstructor
public class CustomTestCaseReader {

    private final Config config;
    private final ResourceLoader resourceLoader;

    private final TestcaseRegistry testcaseRegistry;

    private final RpcModelRegistry rpcModelRegistry;

    private final DynamicMessageUtilService dynamicMessageUtilService;

    private List<TestCase> loadTestCases(List<String> includedFiles) throws Throwable {
        List<TestCase> testCases = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        String classpath = "classpath:" + CUSTOM_TESTS_CLASSPATH + "/*";
        try {
            for (Resource resource : resourcePatternResolver.getResources(classpath)) {
                if (includedFiles.isEmpty() || includedFiles.contains(resource.getFilename())) {
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        String json = bufferedReader.lines().collect(Collectors.joining("\n"));
                        testCases.add(JsonUtil.fromJson(json, TestCase.class));
                    }
                }
            }
            return testCases;
        } catch (IOException ioe) {
            log.error("[loadTestCases] Failed to read test cases at {}", classpath, ioe);
            throw ioe;
        } catch (Throwable t) {
            log.error("[loadTestCases] An error occurred", t);
            throw t;
        }
    }

    public void loadTestCasesToRegistry(List<String> includedFiles) throws Throwable {
        List<TestCase> testCases = loadTestCases(includedFiles);
        for (TestCase testCase : testCases) {
            for (Object param : testCase.getParamValue()) {
                testCase.getParamValueDynMsg().add(
                        dynamicMessageUtilService.objectToDynamicMessage(
                                param,
                                rpcModelRegistry.lookupMessage(
                                        rpcModelRegistry.lookupMethod(testCase.getMethodId()).getInType()
                                )
                        )
                );
            }
            for (Object returnVal : testCase.getReturnValue()) {
                testCase.getReturnValueDynMsg().add(
                        dynamicMessageUtilService.objectToDynamicMessage(
                                returnVal,
                                rpcModelRegistry.lookupMessage(
                                        rpcModelRegistry.lookupMethod(testCase.getMethodId()).getOutType()
                                )
                        )
                );
            }
            testcaseRegistry.addTestCase(testCase);
        }
        log.info("[loadTestCasesToRegistry] Finished loading test cases");
    }

    /**
     * Note: this json string is the string directly written into Java source code.
     * Therefore, the double quotes have to be preceded by "\".
     */
    @Deprecated
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
    @Deprecated
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
