package org.grpctest.core.data;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/** A registry to store all rpc methods under test and their respective test cases.<br> */
@Component
@Slf4j
public class TestcaseRegistry {

    @Autowired
    private RpcModelRegistry rpcModelRegistry;

    /**
     * Methods without test cases will be stored with an empty list<br>
     * methodId : List of TestCase
     */
    private final Map<String, List<TestCase>> methodTestCaseMap = new HashMap<>();

    // methodTestCaseMap

    public void addMethod(RpcService.RpcMethod method) {
        methodTestCaseMap.putIfAbsent(method.getId(), new ArrayList<>());
    }

    public void addTestCase(TestCase testCase) {
        RpcService.RpcMethod targetMethod = rpcModelRegistry.lookupMethod(testCase.getMethodId());
        if (Objects.nonNull(targetMethod)) {
            if (!methodTestCaseMap.containsKey(targetMethod.getId())) {
                addMethod(targetMethod);
            }
            methodTestCaseMap.get(targetMethod.getId()).add(preprocessTestcase(testCase, targetMethod));
        } else {
            log.warn("[addTestCase] Cannot add test case for method [{}]. Reason: method not found", testCase.getMethodId());
        }
    }

    public void deleteEntry(String methodId) {
        methodTestCaseMap.remove(methodId);
    }

    public void deleteEntry(RpcService.RpcMethod method) {
        deleteEntry(method.getId());
    }

    public void deleteTestCase(String methodId, TestCase testCase) {
        if (methodTestCaseMap.containsKey(methodId)) {
            methodTestCaseMap.get(methodId).removeIf(recordedTestCase -> recordedTestCase.getName().equals(testCase.getName()));
        }
    }

    public List<RpcService.RpcMethod> getAllMethodsWithoutTestCases() {
        return methodTestCaseMap.keySet().stream()
                .filter(methodId -> methodTestCaseMap.get(methodId).isEmpty())
                .map(methodId -> rpcModelRegistry.lookupMethod(methodId))
                .toList();
    }

    public List<TestCase> getMethodTestCases(String methodId) {
        return methodTestCaseMap.get(methodId);
    }

    public List<TestCase> getMethodTestCases(RpcService.RpcMethod method) {
        return getMethodTestCases(method.getId());
    }

    public List<TestCase> getAllTestCases() {
        return methodTestCaseMap.values().stream().flatMap(Collection::stream).toList();
    }

    public TestCase.RpcException getExceptionForMethod(String methodId) {
        if (Objects.isNull(getMethodTestCases(methodId)) || getMethodTestCases(methodId).isEmpty()) return null;
        return getMethodTestCases(methodId).get(0).getException();
    }

    public TestCase.RpcException getExceptionForMethod(RpcService.RpcMethod rpcMethod) {
        return getExceptionForMethod(rpcMethod.getId());
    }

    private TestCase preprocessTestcase(TestCase testCase, RpcService.RpcMethod rpcMethod) {
        switch (rpcMethod.getType()) {
            case UNARY -> {
                testCase.setReturnValueDynMsg(
                        CollectionUtil.trimAllExceptFirstElement(testCase.getReturnValueDynMsg())
                );
                testCase.setParamValueDynMsg(
                        CollectionUtil.trimAllExceptFirstElement(testCase.getParamValueDynMsg())
                );
            }
            case CLIENT_STREAMING -> testCase.setReturnValueDynMsg(
                    CollectionUtil.trimAllExceptFirstElement(testCase.getReturnValueDynMsg())
            );
            case SERVER_STREAMING -> testCase.setParamValueDynMsg(
                    CollectionUtil.trimAllExceptFirstElement(testCase.getParamValueDynMsg())
            );
        }
        return testCase;
    }
}
