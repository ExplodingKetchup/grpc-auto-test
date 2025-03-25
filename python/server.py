import logging
import os
from concurrent import futures

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance, log_fields_of_object
from message_utils import message_to_file, message_from_file, format_metadata_as_string, metadata_to_file
from map_values_int64_simplified_pb2 import *
from map_values_int64_simplified_pb2_grpc import *


# Constants
_COMPRESSION_ALGOS = {
    "none": grpc.Compression.NoCompression,
    "deflate": grpc.Compression.Deflate,
    "gzip": grpc.Compression.Gzip,
}
BIN_METADATA_SUFFIX = "-bin"
OUTBOUND_HEADERS = (
)
map_hotpot_MapPot_fields = ["int_double_value", "int_int_value", "int_bool_value", "int_string_value"]
map_hotpot_MapPot_IntDoubleValueEntry_fields = ["key", "value"]
map_hotpot_MapPot_IntStringValueEntry_fields = ["key", "value"]
map_hotpot_MapPot_IntBoolValueEntry_fields = ["key", "value"]
map_hotpot_MapPot_IntIntValueEntry_fields = ["key", "value"]


# Configs
configs = load_config(is_server=True)
print(f"Configs: {configs}")


# >>> Helper functions
def handle_single_request(request: Message, method_id: str, request_type_field_names: list[str]) -> None:
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    logging.info(f"[{method_name}] Request:\n{request}")
    log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
    message_to_file(os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_0.bin"), request)

def handle_streaming_request(request_iterator, method_id, request_type_field_names: list[str]):
    request_idx = 0
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    for request in request_iterator:
        logging.info(f"[{method_name}] Request:\n{request}")
        log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
        message_to_file(
            os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_{request_idx}.bin"),
            request)
        request_idx += 1

def get_single_response(method_id, response_class, response_type_field_names: list[str]):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response = message_from_file(os.path.join(configs["in"]["dir"], f"{method_id_underscore}_return_0.bin"),
                                 response_class)
    logging.info(f"[{method_name}] Response:\n{response}")
    log_fields_of_object(response, f"{method_id} - response", response_type_field_names)

    return response

def get_streaming_response(method_id, response_class, response_type_field_names: list[str]):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response_files = list_files_with_same_prefix(configs["in"]["dir"], f"{method_id_underscore}_return")
    for response_file in response_files:
        response = message_from_file(response_file, response_class)
        logging.info(f"[{method_name}] Response:\n{response}")
        log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
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
        method_id = "map_hotpot.HotpotService.unaryPot"
        response_class = MapPot

        handle_single_request(request, method_id, map_hotpot_MapPot_fields)
        return get_single_response(method_id, response_class, map_hotpot_MapPot_fields)



def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10), compression=_COMPRESSION_ALGOS["none"])
    add_HotpotServiceServicer_to_server(HotpotServiceServicer(), server)
    server.add_insecure_port(f"[::]:{configs["server"]["port"]}")
    server.start()
    logging.info(f"[main] Server started on port {configs["server"]["port"]}")
    server.wait_for_termination()

if __name__ == '__main__':
    main()