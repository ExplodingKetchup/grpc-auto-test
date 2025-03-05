package org.grpctest.core.data;

import com.google.protobuf.Descriptors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.enums.MetadataType;
import org.grpctest.core.pojo.RpcMessage;
import org.grpctest.core.pojo.RpcService;
import org.grpctest.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry storing all data structures in RPC calls (including Services, Methods,
 * and Messages).
 */
@Component
@Slf4j
public class RpcModelRegistry {

    /** RpcService.id : RpcService */
    private final Map<String, RpcService> serviceLookupTable = new HashMap<>();

    /** RpcMethod.id : RpcMethod */
    private final Map<String, RpcService.RpcMethod> methodLookupTable = new HashMap<>();

    /** RpcMessage.id : RpcMessage */
    private final Map<String, RpcMessage> messageLookupTable = new HashMap<>();

    /** Client to Server metadata (fixed per run) */
    @Getter
    private final Map<String, Pair<MetadataType, String>> clientToServerMetadata = new HashMap<>();

    /** Server to Client metadata (fixed per run) */
    @Getter
    private final Map<String, Pair<MetadataType, String>> serverToClientMetadata = new HashMap<>();

    /** List of all proto files */
    @Getter
    private final List<String> protoFiles = new ArrayList<>();

    @Getter
    @Setter
    private String requestCompression;

    @Getter
    @Setter
    private String responseCompression;

    // serviceLookupTable

    public void addServiceToLookupTable(RpcService service) {
        serviceLookupTable.putIfAbsent(service.getId(), service);
    }

    public RpcService lookupService(String serviceId) {
        return serviceLookupTable.get(serviceId);
    }

    public RpcService lookupService(String namespace, String serviceName) {
        return serviceLookupTable.get(StringUtil.getServiceId(namespace, serviceName));
    }

    public List<RpcService> getAllServices() {
        return serviceLookupTable.values().stream().toList();
    }

    public List<RpcService> getAllServicesWithoutMethod() {
        return serviceLookupTable.values().stream().filter(service -> service.getMethods().isEmpty()).toList();
    }

    public void removeService(String serviceId) {
        serviceLookupTable.remove(serviceId);
    }

    // methodLookupTable

    public void addMethodToLookupTable(RpcService.RpcMethod method) {
        methodLookupTable.putIfAbsent(method.getId(), method);
    }

    public RpcService.RpcMethod lookupMethod(String methodId) {
        return methodLookupTable.get(methodId);
    }

    public RpcService.RpcMethod lookupMethod(String namespace, String serviceName, String methodName) {
        return methodLookupTable.get(StringUtil.getMethodId(namespace, serviceName, methodName));
    }

    public List<RpcService.RpcMethod> getAllMethods() {
        return methodLookupTable.values().stream().toList();
    }

    public void removeMethod(String methodId) {
        RpcService.RpcMethod methodToRemove = lookupMethod(methodId);
        lookupService(methodToRemove.getOwnerServiceId()).getMethods().removeIf(internalMethodId -> StringUtils.equals(methodId, internalMethodId));
        methodLookupTable.remove(methodId);
    }

    // serviceLookupTable + methodLookupTable

    public List<RpcService.RpcMethod> getAllMethods(RpcService service) {
        // Name of all methods in the service
        return service.getMethods().stream()
                .map(this::lookupMethod)
                .toList();
    }

    public List<RpcService.RpcMethod> getAllMethods(String serviceId) {
        return getAllMethods(lookupService(serviceId));
    }

    public RpcService getOwnerService(RpcService.RpcMethod method) {
        return lookupService(method.getOwnerServiceId());
    }

    // messageLookupTable

    public void addMessageToLookupTable(RpcMessage message) {
        messageLookupTable.putIfAbsent(message.getId(), message);
    }

    public void deleteMessageFromLookupTable(String messageId) {
        messageLookupTable.remove(messageId);
    }

    public RpcMessage lookupMessage(String messageId) {
        return messageLookupTable.get(messageId);
    }

    public RpcMessage lookupMessage(String namespace, String messageName) {
        return messageLookupTable.get(StringUtil.getMessageId(namespace, messageName));
    }

    public List<RpcMessage> getAllMessages() {
        return messageLookupTable.values().stream().toList();
    }

    public List<String> getAllFieldNames(String messageId) {
        return messageLookupTable.get(messageId).getMessageDescriptor().getFields().stream().map(Descriptors.FieldDescriptor::getName).toList();
    }

    public List<String> getAllFieldNamesAsCamelCase(String messageId) {
        return getAllFieldNames(messageId).stream().map((fieldname) -> StringUtil.snakeCaseToCamelCase(fieldname, true)).toList();
    }

    public List<String> getAllFieldsAsJavaGetters(String messageId) {
        return messageLookupTable.get(messageId)
                .getMessageDescriptor().getFields().stream().map(
                        (fieldDescriptor) -> {
                            String suffix = "";
                            if (fieldDescriptor.isMapField()) {
                                suffix = "Map";
                            } else if (fieldDescriptor.isRepeated()) {
                                suffix = "List";
                            }
                            return "get" + StringUtil.snakeCaseToCamelCase(fieldDescriptor.getName(), true) + suffix;
                        }
                ).toList();
    }

    // clientToServerMetadata

    public void addClientToServerMetadata(Map<String, Pair<MetadataType, String>> metadata) {
        clientToServerMetadata.putAll(metadata);
    }

    public void addClientToServerMetadata(String key, MetadataType type, String value) {
        clientToServerMetadata.putIfAbsent(key, Pair.of(type, value));
    }

    public Map<String, Pair<MetadataType, String>> getAllClientToServerMetadata() {
        return clientToServerMetadata;
    }

    public boolean haveClientToServerMetadata() {
        return !clientToServerMetadata.isEmpty();
    }

    // serverToClientMetadata

    public void addServerToClientMetadata(Map<String, Pair<MetadataType, String>> metadata) {
        serverToClientMetadata.putAll(metadata);
    }

    public void addServerToClientMetadata(String key, MetadataType type, String value) {
        serverToClientMetadata.putIfAbsent(key, Pair.of(type, value));
    }

    public Map<String, Pair<MetadataType, String>> getAllServerToClientMetadata() {
        return serverToClientMetadata;
    }

    public boolean haveServerToClientMetadata() {
        return !serverToClientMetadata.isEmpty();
    }

    // protoFiles

    public void addProtoFilename(String protoFilename) {
        protoFiles.add(protoFilename);
    }

    // requestCompression

    public boolean isRequestCompressionSet() {
        return StringUtils.isNotBlank(requestCompression);
    }

    // responseCompression

    public boolean isResponseCompressionSet() {
        return StringUtils.isNotBlank(responseCompression);
    }
}
