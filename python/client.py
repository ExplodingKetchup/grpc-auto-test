import atexit
import logging
import os.path
from typing import Iterator

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance, log_fields_of_object
from message_utils import message_from_file, message_to_file, format_grpc_error_as_string, grpc_error_to_file, \
    metadata_to_file, format_metadata_as_string
from basic_single_fields_pb2 import *
from basic_single_fields_pb2_grpc import *


# Constants
BIN_METADATA_SUFFIX = "-bin"
OUTBOUND_HEADERS = (
)

single_hotpot_RequestMessage_fields = ["small_hotpot", "float_boat"]
single_hotpot_BigHotpotOfTerror_fields = ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]
single_hotpot_ResponseMessage_fields = ["big_hotpot", "flex_tape"]
single_hotpot_SmallHotpotOfRickeridoo_fields = ["small_uint32_value", "small_string_value"]

# Configs
configs = load_config(is_server=False)
print(f"Configs: {configs}")


# >>> HELPER FUNCTIONS
def request_iterator_wrapper(request_iterator: Iterator[Message], calling_method_name: str, method_id: str) -> Iterator[
    Message]:
    for request in request_iterator:
        logging.info(f"[request_iterator_wrapper] Invoke {method_id} with request {request}")
        log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
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
def invoke_unary_rpc(method, request: Message, method_id: str, request_type_field_names: list[str], response_type_field_names: list[str]):
    try:
        logging.info(f"[invoke_unary_rpc] Invoke {method_id} with request {request}")
        log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
        response, call = method.with_call(request)
        logging.info(f"[invoke_unary_rpc] Method {method_id} returns {response}")
        log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
        logging.error(f"[invoke_unary_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_server_streaming_rpc(method, request: Message, method_id: str, request_type_field_names: list[str], response_type_field_names: list[str]):
    try:
        logging.info(f"[invoke_server_streaming_rpc] Invoke {method_id} with request {request}")
        log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
        response_idx = 0
        call = method(request)
        for response in call:
            logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
            log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
            message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_{response_idx}.bin"),
                            response)
            response_idx += 1
    except grpc.RpcError as rpc_error:
            logging.error(f"[invoke_server_streaming_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
            grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_client_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str, request_type_field_names: list[str], response_type_field_names: list[str]):
    try:
        response, call = method.with_call(
            request_iterator_wrapper(
                request_iterator,
                calling_method_name="invoke_client_streaming_rpc",
                method_id=method_id
            )
        )
        logging.info(f"[invoke_client_streaming_rpc] Method {method_id} returns {response}")
        log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
        logging.error(f"[invoke_client_streaming_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_bidi_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str, request_type_field_names: list[str], response_type_field_names: list[str]):
    try:
        response_idx = 0
        call = method(
            request_iterator_wrapper(
                request_iterator,
                calling_method_name="invoke_bidi_streaming_rpc",
                method_id=method_id
            )
        )
        for response in call:
            logging.info(f"[invoke_bidi_streaming_rpc] Method {method_id} returns {response}")
            log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
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

    # >>> SERVICE HotpotService
    HotpotService_stub = HotpotServiceStub(channel)

    # METHOD single_hotpot.HotpotService.unaryPot
    invoke_unary_rpc(method=HotpotService_stub.UnaryPot,
                     request=read_request_from_file(
                         method_id="single_hotpot.HotpotService.unaryPot",
                         request_class=RequestMessage,
                         read_multiple=False
                     ),
                     method_id="single_hotpot.HotpotService.unaryPot",
                     request_type_field_names=single_hotpot_RequestMessage_fields,
                     response_type_field_names=single_hotpot_ResponseMessage_fields)

    # METHOD single_hotpot.HotpotService.serverStreamingPot
    invoke_server_streaming_rpc(method=HotpotService_stub.ServerStreamingPot,
                                request=read_request_from_file(
                                    method_id="single_hotpot.HotpotService.serverStreamingPot",
                                    request_class=RequestMessage,
                                    read_multiple=False
                                ),
                                method_id="single_hotpot.HotpotService.serverStreamingPot",
                                request_type_field_names=single_hotpot_RequestMessage_fields,
                                response_type_field_names=single_hotpot_ResponseMessage_fields)

    # METHOD single_hotpot.HotpotService.clientStreamingPot
    invoke_client_streaming_rpc(method=HotpotService_stub.ClientStreamingPot,
                                request_iterator=read_request_from_file(
                                    method_id="single_hotpot.HotpotService.clientStreamingPot",
                                    request_class=RequestMessage,
                                    read_multiple=True
                                ),
                                method_id="single_hotpot.HotpotService.clientStreamingPot",
                                request_type_field_names=single_hotpot_RequestMessage_fields,
                                response_type_field_names=single_hotpot_ResponseMessage_fields)

    # METHOD single_hotpot.HotpotService.bidiStreamingPot
    invoke_bidi_streaming_rpc(method=HotpotService_stub.BidiStreamingPot,
                              request_iterator=read_request_from_file(
                                  method_id="single_hotpot.HotpotService.bidiStreamingPot",
                                  request_class=RequestMessage,
                                  read_multiple=True
                              ),
                              method_id="single_hotpot.HotpotService.bidiStreamingPot",
                              request_type_field_names=single_hotpot_RequestMessage_fields,
                              response_type_field_names=single_hotpot_ResponseMessage_fields)

    channel.close()


def at_shutdown():
    logging.info("Client shutting down...")


atexit.register(at_shutdown)


if __name__ == '__main__':
    main()
