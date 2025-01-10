import logging
import os
from concurrent import futures

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_to_file, message_from_file, format_metadata_as_string, metadata_to_file
from basic_repeated_fields_pb2 import *
from basic_repeated_fields_pb2_grpc import *


# Constants
BIN_METADATA_SUFFIX = "-bin"
OUTBOUND_HEADERS = (
)

# Configs
configs = load_config(is_server=True)
print(f"Configs: {configs}")


# >>> Helper functions
def handle_single_request(request: Message, method_id: str) -> None:
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    logging.info(f"[{method_name}] Received request: {request}")
    message_to_file(os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_0.bin"), request)

def handle_streaming_request(request_iterator, method_id):
    request_idx = 0
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    for request in request_iterator:
        logging.info(f"[{method_name}] Received request: {request}")
        message_to_file(
            os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_{request_idx}.bin"),
            request)
        request_idx += 1

def get_single_response(method_id, response_class):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response = message_from_file(os.path.join(configs["in"]["dir"], f"{method_id_underscore}_return_0.bin"),
                                 response_class)
    logging.info(f"[{method_name}] Response: {response}")

    return response

def get_streaming_response(method_id, response_class):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response_files = list_files_with_same_prefix(configs["in"]["dir"], f"{method_id_underscore}_return")
    for response_file in response_files:
        response = message_from_file(response_file, response_class)
        logging.info(f"[{method_name}] Response: {response}")
        yield response

def raise_grpc_exception(context, status_code, description, trailers):
    context.set_trailing_metadata(trailers)
    context.abort(status_code, description)


def receive_header_metadata(context):
    headers = context.invocation_metadata()
    logging.info(f"[receive_header_metadata] Received metadata:\n{format_metadata_as_string(headers)}")
    metadata_to_file(os.path.join(configs["out"]["dir"], "received_metadata.txt"), headers)


def send_header_metadata(context):
    context.send_initial_metadata(OUTBOUND_HEADERS)

# <<< Helper functions

class HotpotServiceServicer(HotpotServiceServicer):

    def UnaryPot(self, request, context):
        method_id = "repeated_hotpots.HotpotService.unaryPot"
        response_class = ResponseMessage

        handle_single_request(request, method_id)
        return get_single_response(method_id, response_class)

    def ServerStreamingPot(self, request, context):
        method_id = "repeated_hotpots.HotpotService.serverStreamingPot"
        response_class = ResponseMessage

        handle_single_request(request, method_id)
        for response in get_streaming_response(method_id, response_class):
            yield response

    def ClientStreamingPot(self, request_iterator, context):
        method_id = "repeated_hotpots.HotpotService.clientStreamingPot"
        response_class = ResponseMessage

        handle_streaming_request(request_iterator, method_id)
        return get_single_response(method_id, response_class)

    def BidiStreamingPot(self, request_iterator, context):
        method_id = "repeated_hotpots.HotpotService.bidiStreamingPot"
        response_class = ResponseMessage
        handle_streaming_request(request_iterator, method_id)
        for response in get_streaming_response(method_id, response_class):
            yield response



def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_HotpotServiceServicer_to_server(HotpotServiceServicer(), server)
    server.add_insecure_port(f"[::]:{configs["server"]["port"]}")
    server.start()
    logging.info(f"[main] Server started on port {configs["server"]["port"]}")
    server.wait_for_termination()

if __name__ == '__main__':
    main()