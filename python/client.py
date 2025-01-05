import atexit
import logging
import os.path
from typing import Iterator

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_from_file, message_to_file, format_grpc_error_as_string, grpc_error_to_file, \
    metadata_to_file, format_metadata_as_string
from rpc_pb2 import *
from rpc_pb2_grpc import *


# Constants
BIN_METADATA_SUFFIX = "-bin"
META_KEY_vr0te2t50833xy4x92p = "vr0te2t50833xy4x92p" + BIN_METADATA_SUFFIX;
META_VALUE_vr0te2t50833xy4x92p = bytes.fromhex("476cfe")
META_KEY_ka = "ka" + BIN_METADATA_SUFFIX;
META_VALUE_ka = bytes.fromhex("c3")
META_KEY_w42u = "w42u" + BIN_METADATA_SUFFIX;
META_VALUE_w42u = bytes.fromhex("5a4191d178")
OUTBOUND_HEADERS = (
    (META_KEY_vr0te2t50833xy4x92p, META_VALUE_vr0te2t50833xy4x92p),
    (META_KEY_ka, META_VALUE_ka),
    (META_KEY_w42u, META_VALUE_w42u),
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
        response, call = method.with_call(request, metadata=OUTBOUND_HEADERS)
        receive_header_metadata(call)
        logging.info(f"[invoke_unary_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
        receive_header_metadata(rpc_error)
        logging.error(f"[invoke_unary_rpc] Received rpc error: {format_grpc_error_as_string(rpc_error)}")
        grpc_error_to_file(get_error_file_path(method_id), rpc_error)


def invoke_server_streaming_rpc(method, request: Message, method_id: str):
    try:
        logging.info(f"[invoke_server_streaming_rpc] Invoke {method_id} with request {request}")
        response_idx = 0
        call = method(request, metadata=OUTBOUND_HEADERS)
        receive_header_metadata(call)
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
            ),
            metadata=OUTBOUND_HEADERS
        )
        receive_header_metadata(call)
        logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                        response)
    except grpc.RpcError as rpc_error:
        receive_header_metadata(rpc_error)
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
            ),
            metadata=OUTBOUND_HEADERS
        )
        receive_header_metadata(call)
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

    # >>> SERVICE PeopleService
    PeopleService_stub = PeopleServiceStub(channel)

    # METHOD person.PeopleService.getPerson
    invoke_unary_rpc(method=PeopleService_stub.GetPerson,
                     request=read_request_from_file(
                         method_id="person.PeopleService.getPerson",
                         request_class=GetPersonRequest,
                         read_multiple=False
                     ),
                     method_id="person.PeopleService.getPerson")

    # METHOD person.PeopleService.listPerson
    invoke_server_streaming_rpc(method=PeopleService_stub.ListPerson,
                                request=read_request_from_file(
                                    method_id="person.PeopleService.listPerson",
                                    request_class=GetPersonRequest,
                                    read_multiple=False
                                ),
                                method_id="person.PeopleService.listPerson")

    # METHOD person.PeopleService.registerPerson
    invoke_client_streaming_rpc(method=PeopleService_stub.RegisterPerson,
                                request_iterator=read_request_from_file(
                                    method_id="person.PeopleService.registerPerson",
                                    request_class=GetPersonRequest,
                                    read_multiple=True
                                ),
                                method_id="person.PeopleService.registerPerson")

    # METHOD person.PeopleService.streamPerson
    invoke_bidi_streaming_rpc(method=PeopleService_stub.StreamPerson,
                              request_iterator=read_request_from_file(
                                  method_id="person.PeopleService.streamPerson",
                                  request_class=GetPersonRequest,
                                  read_multiple=True
                              ),
                              method_id="person.PeopleService.streamPerson")

    channel.close()


def at_shutdown():
    logging.info("Client shutting down...")


atexit.register(at_shutdown)


if __name__ == '__main__':
    main()
