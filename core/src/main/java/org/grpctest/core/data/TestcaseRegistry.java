package org.grpctest.core.data;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.pojo.RpcMethod;
import org.grpctest.core.pojo.TestCase;
import org.grpctest.core.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static org.grpctest.core.constant.Constants.OUT_DIR_CLIENT;
import static org.grpctest.core.constant.Constants.OUT_DIR_SERVER;

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

    public void addMethod(RpcMethod method) {
        methodTestCaseMap.putIfAbsent(method.getId(), new ArrayList<>());
    }

    public void addTestCase(TestCase testCase) {
        RpcMethod targetMethod = rpcModelRegistry.lookupMethod(testCase.getMethodId());
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

    public void deleteEntry(RpcMethod method) {
        deleteEntry(method.getId());
    }

    public void deleteTestCase(String methodId, TestCase testCase) {
        if (methodTestCaseMap.containsKey(methodId)) {
            methodTestCaseMap.get(methodId).removeIf(recordedTestCase -> recordedTestCase.getName().equals(testCase.getName()));
        }
    }

    public List<RpcMethod> getAllMethodsWithoutTestCases() {
        return methodTestCaseMap.keySet().stream()
                .filter(methodId -> methodTestCaseMap.get(methodId).isEmpty())
                .map(methodId -> rpcModelRegistry.lookupMethod(methodId))
                .toList();
    }

    public List<TestCase> getMethodTestCases(String methodId) {
        return methodTestCaseMap.get(methodId);
    }

    public List<TestCase> getMethodTestCases(RpcMethod method) {
        return getMethodTestCases(method.getId());
    }

    public List<TestCase> getAllTestCases() {
        return methodTestCaseMap.values().stream().flatMap(Collection::stream).toList();
    }

    public TestCase.RpcException getExceptionForMethod(String methodId) {
        if (Objects.isNull(getMethodTestCases(methodId)) || getMethodTestCases(methodId).isEmpty()) return null;
        return getMethodTestCases(methodId).get(0).getException();
    }

    public TestCase.RpcException getExceptionForMethod(RpcMethod rpcMethod) {
        return getExceptionForMethod(rpcMethod.getId());
    }

    private TestCase preprocessTestcase(TestCase testCase, RpcMethod rpcMethod) {
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

    /**
     * Same as {@link TestcaseRegistry#getExpectedClientOutputFiles(String)}, but this method lists all
     * files from all methods, plus received_metadata.txt if server -> client metadata is enabled.
     * @return
     */
    public List<String> getExpectedClientOutputFiles() {
        List<String> result = new ArrayList<>();
        if (rpcModelRegistry.haveServerToClientMetadata()) {
            result.add(OUT_DIR_CLIENT + File.separator + "received_metadata.txt");
        }
        for (RpcMethod rpcMethod : rpcModelRegistry.getAllMethods()) {
            result.addAll(getExpectedClientOutputFiles(rpcMethod.getId()));
        }
        return result;
    }

    /**
     * List all files related to the specified method that should AT LEAST be present after client finishes.<br>
     * It will return: <br>
     * - All received responses files (_return.bin), IF there is no exception <br>
     * - Exception files (_error.txt), IF an exception will be raised on the server side
     *
     * @param methodId
     * @return file paths corresponding to expected files
     */
    public List<String> getExpectedClientOutputFiles(String methodId) {
        List<String> result = new ArrayList<>();
        if (Objects.nonNull(getExceptionForMethod(methodId))) {
            result.add(OUT_DIR_CLIENT + File.separator + methodId.replace(".", "_") + "_error.txt");
            return result;
        }
        for (int i = 0; i < getMethodTestCases(methodId).get(0).getReturnValueDynMsg().size(); i++) {
            result.add(OUT_DIR_CLIENT + File.separator + methodId.replace(".", "_") + "_return_" + i + ".bin");
        }
        return result;
    }

    /**
     * Same as {@link TestcaseRegistry#getExpectedServerOutputFiles(String)}, but this method lists all
     * files from all methods, plus received_metadata.txt if client -> server metadata is enabled.
     * @return
     */
    public List<String> getExpectedServerOutputFiles() {
        List<String> result = new ArrayList<>();
        if (rpcModelRegistry.haveClientToServerMetadata()) {
            result.add(OUT_DIR_SERVER + File.separator + "received_metadata.txt");
        }
        for (RpcMethod rpcMethod : rpcModelRegistry.getAllMethods()) {
            result.addAll(getExpectedClientOutputFiles(rpcMethod.getId()));
        }
        return result;
    }

    /**
     * List all files related to the specified method that should AT LEAST be present after server finishes.<br>
     * It will return all received responses files (_param.bin).
     *
     * @param methodId
     * @return file paths corresponding to expected files
     */
    public List<String> getExpectedServerOutputFiles(String methodId) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < getMethodTestCases(methodId).get(0).getParamValueDynMsg().size(); i++) {
            result.add(OUT_DIR_SERVER + File.separator + methodId.replace(".", "_") + "_param_" + i + ".bin");
        }
        return result;
    }
}
