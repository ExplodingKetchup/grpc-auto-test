package org.grpctest.core.data;

import lombok.extern.slf4j.Slf4j;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.pojo.TestCase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry to store all rpc methods under test and their respective test cases.<br>
 */
@Component
@Slf4j
public class RpcTestRegistry {

    /** If a method doesn't have test case, store an empty test case */
    private final Map<RpcService.RpcMethod, List<TestCase>> methodTestCaseMap = new HashMap<>();

    /** Key format: ServiceName (shortened) */
    private final Map<String, RpcService> serviceLookupTable = new HashMap<>();

    /** Key format: ServiceName.methodName */
    private final Map<String, RpcService.RpcMethod> methodLookupTable = new HashMap<>();

    public void addMethod(RpcService.RpcMethod method) {
        methodTestCaseMap.putIfAbsent(method, new ArrayList<>());
    }

    public void addTestCase(TestCase testCase) {
        if (methodLookupTable.containsKey(getMethodLookupTableKey(testCase.getServiceName(), testCase.getMethodName()))) {
            RpcService.RpcMethod method = lookupMethod(testCase.getServiceName(), testCase.getMethodName());
            addTestCase(method, testCase);
        } else {
            log.warn("[addTestCase] Cannot add test case for method [{}]. Reason: method not found",
                    getMethodLookupTableKey(testCase.getServiceName(), testCase.getMethodName()));
        }
    }

    public void addTestCase(RpcService.RpcMethod method, TestCase testCase) {
        methodTestCaseMap.get(method).add(testCase);
    }

    public void deleteMethod(RpcService.RpcMethod method) {
        methodTestCaseMap.remove(method);
    }

    public void deleteTestCase(RpcService.RpcMethod method, TestCase testCase) {
        methodTestCaseMap.get(method).removeIf(recordedTestCase -> recordedTestCase.getName().equals(testCase.getName()));
    }

    public List<TestCase> getMethodTestCases(RpcService.RpcMethod method) {
        return methodTestCaseMap.get(method);
    }

    public void addServiceToLookupTable(RpcService service) {
        serviceLookupTable.putIfAbsent(service.getName(), service);
    }

    public void addMethodToLookupTable(RpcService.RpcMethod method) {
        methodLookupTable.putIfAbsent(getMethodLookupTableKey(method.getOwnerServiceName(), method.getName()), method);
    }

    public void addServiceAndMethodsToLookupTable(RpcService service) {
        addServiceToLookupTable(service);
        for (RpcService.RpcMethod method : service.getMethods()) {
            addMethodToLookupTable(method);
        }
    }

    public RpcService lookupService(String serviceName) {
        return serviceLookupTable.get(serviceName);
    }

    public List<RpcService> getAllServices() {
        return serviceLookupTable.values().stream().toList();
    }

    public RpcService.RpcMethod lookupMethod(String serviceName, String methodName) {
        return methodLookupTable.get(getMethodLookupTableKey(serviceName, methodName));
    }

    public List<RpcService.RpcMethod> getAllMethods() {
        return methodLookupTable.values().stream().toList();
    }

    private String getMethodLookupTableKey(String serviceName, String methodName) {
        return serviceName + "."  + methodName;
    }
}
