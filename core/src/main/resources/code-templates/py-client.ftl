import logging
import os.path
from typing import Iterator

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_from_file, message_to_file, format_grpc_error_as_string, grpc_error_to_file, \
    metadata_to_file, format_metadata_as_string
<#list registry.getProtoFiles() as proto_file>
from ${proto_file}_pb2 import *
from ${proto_file}_pb2_grpc import *
</#list>


# Constants
BIN_METADATA_SUFFIX = "-bin"
<#assign metaMap = registry.getAllClientToServerMetadata()>
<#list metaMap?keys as metaKey>
    <#assign metaPair = metaMap[metaKey]>
    <#assign metaType = metaPair.getLeft().name()> <#-- MetadataType -->
    <#assign metaValue = metaPair.getRight()> <#-- Metadata value -->
    <#if metaType == "STRING">
META_KEY_${metaKey} = "${metaKey}";
META_VALUE_${metaKey} = "${metaValue}";
    <#elseif metaType == "BIN">
META_KEY_${metaKey} = "${metaKey}" + BIN_SUFFIX;
META_VALUE_${metaKey} = bytes.fromhex("${metaValue}")
    </#if>
</#list>
OUTBOUND_HEADERS = (
<#list metaMap?keys as metaKey>
    (META_KEY_${metaKey}, META_VALUE_${metaKey})<#sep>,
</#list>
)

# Configs
configs = load_config(is_server=False)
print(f"Configs: {configs}")


# >>> HELPER FUNCTIONS
def request_iterator_wrapper(request_iterator: Iterator[Message], calling_method_name: str, method_id: str) -> Iterator[
    Message]:
    for request in request_iterator:
        logging.info(f"[{calling_method_name}] Invoke {method_id} with request {request}")
        yield request


def read_request_from_file(method_id: str, request_class: type, read_multiple: bool = False) -> Message | Iterator[
    Message]:
    def read_requests_as_iterator():
        for file_path in list_files_with_same_prefix(configs["in"]["dir"], f"{method_id.replace(".", "_")}_param"):
            yield message_from_file(file_path, request_class)
    if read_multiple:
        return read_requests_as_iterator()
    else:
        return message_from_file(os.path.join(configs["in"]["dir"], f"{method_id.replace(".", "_")}_param_0.bin"),
                                 request_class)


def get_received_metadata_file_path():
    return os.path.join(configs["out"]["dir"], "received_metadata.txt")


def get_error_file_path(method_id: str):
    return os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_error.txt")


def receive_header_metadata(call):
    headers = call.initial_metadata()
    logging.info(f"[receive_header_metadata] Received metadata:\n{format_metadata_as_string(headers)}")
    metadata_to_file(os.path.join(configs["out"]["dir"], "received_metadata.txt"), headers)

# <<< HELPER FUNCTIONS

# >>> RPC INVOKERS
def invoke_unary_rpc(method, request: Message, method_id: str):
    try:
        logging.info(f"[invoke_unary_rpc] Invoke {method_id} with request {request}")
        response, call = method.with_call(request<#if registry.haveClientToServerMetadata()>, metadata=OUTBOUND_HEADERS</#if>)
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(call)
</#if>
        logging.info(f"[invoke_unary_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(rpc_error)
</#if>
        logging.error(f"[invoke_unary_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_server_streaming_rpc(method, request: Message, method_id: str):
    try:
        logging.info(f"[invoke_server_streaming_rpc] Invoke {method_id} with request {request}")
        response_idx = 0
        call = method(request<#if registry.haveClientToServerMetadata()>, metadata=OUTBOUND_HEADERS</#if>)
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(call)
</#if>
        for response in call:
            logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
            message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_{response_idx}.bin"),
                            response)
            response_idx += 1
    except grpc.RpcError as rpc_error:
            logging.error(f"[invoke_server_streaming_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
            grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_client_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str):
    try:
        response, call = method.with_call(
            request_iterator_wrapper(
                request_iterator,
                calling_method_name="invoke_client_streaming_rpc",
                method_id=method_id
            )<#if registry.haveClientToServerMetadata()>,
            metadata=OUTBOUND_HEADERS</#if>
        )
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(call)
</#if>
        logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(rpc_error)
</#if>
        logging.error(f"[invoke_client_streaming_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_bidi_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str):
    try:
        response_idx = 0
        call = method(
            request_iterator_wrapper(
                request_iterator,
                calling_method_name="invoke_bidi_streaming_rpc",
                method_id=method_id
            )<#if registry.haveClientToServerMetadata()>,
            metadata=OUTBOUND_HEADERS</#if>
        )
<#if registry.haveServerToClientMetadata()>
        receive_header_metadata(call)
</#if>
        for response in call:
            logging.info(f"[invoke_bidi_streaming_rpc] Method {method_id} returns {response}")
            message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_{response_idx}.bin"),
                            response)
            response_idx += 1
    except grpc.RpcError as rpc_error:
        logging.error(f"[invoke_bidi_streaming_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)

# <<< RPC INVOKERS


def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    channel = grpc.insecure_channel(f"{configs["server"]["host"]}:{configs["server"]["port"]}")

<#list registry.getAllServices() as service>
    # >>> SERVICE ${service.name}
    ${service.name}_stub = ${service.name}Stub(channel)

    <#list registry.getAllMethods(service) as method>
    <#assign request_class = method.inType?split(".")?last>
    # METHOD ${method.id}
        <#if method.type == "UNARY">
    invoke_unary_rpc(method=${service.name}_stub.${method.name?cap_first},
                     request=read_request_from_file(
                         method_id="${method.id}",
                         request_class=${request_class},
                         read_multiple=False
                     ),
                     method_id="${method.id}")

        <#elseif method.type == "SERVER_STREAMING">
    invoke_server_streaming_rpc(method=${service.name}_stub.${method.name?cap_first},
                                request=read_request_from_file(
                                    method_id="${method.id}",
                                    request_class=${request_class},
                                    read_multiple=False
                                ),
                                method_id="${method.id}")

        <#elseif method.type == "CLIENT_STREAMING">
    invoke_client_streaming_rpc(method=${service.name}_stub.${method.name?cap_first},
                                request_iterator=read_request_from_file(
                                    method_id="${method.id}",
                                    request_class=${request_class},
                                    read_multiple=True
                                ),
                                method_id="${method.id}")

        <#elseif method.type == "BIDI_STREAMING">
    invoke_bidi_streaming_rpc(method=${service.name}_stub.${method.name?cap_first},
                              request_iterator=read_request_from_file(
                                  method_id="${method.id}",
                                  request_class=${request_class},
                                  read_multiple=True
                              ),
                              method_id="${method.id}")

        </#if>
    </#list>
</#list>

    channel.close()

if __name__ == '__main__':
    main()
