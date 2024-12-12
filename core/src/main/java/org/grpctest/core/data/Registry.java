//package org.grpctest.core.data;
//
//import lombok.extern.slf4j.Slf4j;
//import org.grpctest.core.pojo.RpcMessage;
//import org.grpctest.core.pojo.RpcService;
//import org.grpctest.core.pojo.TestCase;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
///**
// * A registry to store all rpc methods under test and their respective test cases.<br>
// */
//@Component
//@Slf4j
//public class Registry {
//
//    /** Methods without test cases will be stored with an empty list */
//    private final Map<RpcService.RpcMethod, List<TestCase>> methodTestCaseMap = new HashMap<>();
//
//    /** Key format: ServiceName (shortened) */
//    private final Map<String, RpcService> serviceLookupTable = new HashMap<>();
//
//    /** Key format: ServiceName.methodName */
//    private final Map<String, RpcService.RpcMethod> methodLookupTable = new HashMap<>();
//
//    /** Key format: Message name (shortened) */
//    private final Map<String, RpcMessage> messageLookupTable = new HashMap<>();
//
//    /** methodTestCaseMap */
//    public void addMethodToMethodTestCaseMap(RpcService.RpcMethod method) {
//        methodTestCaseMap.putIfAbsent(method, new ArrayList<>());
//    }
//
//    public void addTestCase(TestCase testCase) {
//        if (methodLookupTable.containsKey(getMethodLookupTableKey(testCase.getServiceName(), testCase.getMethodName()))) {
//            RpcService.RpcMethod method = lookupMethod(testCase.getServiceName(), testCase.getMethodName());
//            addTestCase(method, testCase);
//        } else {
//            log.warn("[addTestCase] Cannot add test case for method [{}]. Reason: method not found",
//                    getMethodLookupTableKey(testCase.getServiceName(), testCase.getMethodName()));
//        }
//    }
//
//    public void addTestCase(RpcService.RpcMethod method, TestCase testCase) {
//        methodTestCaseMap.get(method).add(testCase);
//    }
//
//    public void deleteMethodTestCaseMapEntry(RpcService.RpcMethod method) {
//        methodTestCaseMap.remove(method);
//    }
//
//    public void deleteTestCase(RpcService.RpcMethod method, TestCase testCase) {
//        methodTestCaseMap.get(method).removeIf(recordedTestCase -> recordedTestCase.getName().equals(testCase.getName()));
//    }
//
//    public List<RpcService.RpcMethod> getAllMethodsWithoutTestCases() {
//        return methodTestCaseMap.keySet().stream()
//                .filter(method -> methodTestCaseMap.get(method).isEmpty())
//                .toList();
//    }
//
//    public List<TestCase> getMethodTestCases(RpcService.RpcMethod method) {
//        return methodTestCaseMap.get(method);
//    }
//
//    public List<TestCase> getAllTestCases() {
//        return methodTestCaseMap.values().stream().flatMap(Collection::stream).toList();
//    }
//
//    /** serviceLookupTable */
//    public void addServiceToLookupTable(RpcService service) {
//        serviceLookupTable.putIfAbsent(service.getName(), service);
//    }
//
//    public RpcService lookupService(String serviceName) {
//        return serviceLookupTable.get(serviceName);
//    }
//
//    public List<RpcService> getAllServices() {
//        return serviceLookupTable.values().stream().toList();
//    }
//
//    /** methodLookupTable*/
//    public void addMethodToLookupTable(RpcService.RpcMethod method) {
//        methodLookupTable.putIfAbsent(getMethodLookupTableKey(method.getOwnerServiceName(), method.getName()), method);
//    }
//
//    public RpcService.RpcMethod lookupMethod(String serviceName, String methodName) {
//        return methodLookupTable.get(getMethodLookupTableKey(serviceName, methodName));
//    }
//
//    public List<RpcService.RpcMethod> getAllMethods() {
//        return methodLookupTable.values().stream().toList();
//    }
//
//    /** serviceLookupTable + methodLookupTable */
//    public void addServiceAndMethodsToLookupTable(RpcService service) {
//        addServiceToLookupTable(service);
//        for (RpcService.RpcMethod method : service.getMethods()) {
//            addMethodToLookupTable(method);
//        }
//    }
//
//    public List<RpcService.RpcMethod> getAllMethods(RpcService service) {
//        // Name of all methods in the service
//        return service.getMethods().stream()
//                .map(RpcService.RpcMethod::getName)
//                .map(methodName -> lookupMethod(service.getName(), methodName))
//                .toList();
//    }
//
//    public List<RpcService.RpcMethod> getAllMethods(String serviceName) {
//        return getAllMethods(lookupService(serviceName));
//    }
//
//    public RpcService getOwnerService(RpcService.RpcMethod method) {
//        return lookupService(method.getOwnerServiceName());
//    }
//
//    /** messageLookupTable */
//    public void addMessageToLookupTable(RpcMessage message) {
//        messageLookupTable.putIfAbsent(message.getName(), message);
//    }
//
//    public void deleteMessageFromLookupTable(String messageName) {
//        messageLookupTable.remove(messageName);
//    }
//
//    public RpcMessage lookupMessage(String messageName) {
//        return messageLookupTable.get(messageName);
//    }
//
//    public List<RpcMessage> getAllMessages() {
//        return messageLookupTable.values().stream().toList();
//    }
//
//    private String getMethodLookupTableKey(String serviceName, String methodName) {
//        return serviceName + "."  + methodName;
//    }
//}
