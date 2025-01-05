import logging
import os
from concurrent import futures

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_to_file, message_from_file, format_metadata_as_string, metadata_to_file
from rpc_pb2 import *
from rpc_pb2_grpc import *


# Constants
BIN_METADATA_SUFFIX = "-bin"
META_KEY_r6k02k2ey10 = "r6k02k2ey10" + BIN_METADATA_SUFFIX;
META_VALUE_r6k02k2ey10 = bytes.fromhex("1272fa")
META_KEY_sl461775 = "sl461775" + BIN_METADATA_SUFFIX;
META_VALUE_sl461775 = bytes.fromhex("30c79889d4ee6ad5a3b04c")
OUTBOUND_HEADERS = (
    (META_KEY_r6k02k2ey10, META_VALUE_r6k02k2ey10),
    (META_KEY_sl461775, META_VALUE_sl461775),
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

class PeopleServiceServicer(PeopleServiceServicer):

    def GetPerson(self, request, context):
        method_id = "person.PeopleService.getPerson"
        response_class = GetPersonResponse

        receive_header_metadata(context)
        handle_single_request(request, method_id)
        send_header_metadata(context)
        trailers = (
            ("fsydin9ggb12", "27K66G"),
            ("42l", "88cV"),
            ("5m7m7ovr01", "016x3"),
        )
        raise_grpc_exception(context, grpc.StatusCode.UNAUTHENTICATED, "0e2X", trailers)
        return get_single_response(method_id, response_class)

    def ListPerson(self, request, context):
        method_id = "person.PeopleService.listPerson"
        response_class = GetPersonResponse

        receive_header_metadata(context)
        handle_single_request(request, method_id)
        send_header_metadata(context)
        for response in get_streaming_response(method_id, response_class):
            yield response
        trailers = (
            ("5a0", "y72ZrDDRgmm4sy0ZBu"),
            ("4zmw0bkib", "924e"),
        )
        raise_grpc_exception(context, grpc.StatusCode.OUT_OF_RANGE, "n10kNX", trailers)

    def RegisterPerson(self, request_iterator, context):
        method_id = "person.PeopleService.registerPerson"
        response_class = GetPersonResponse

        receive_header_metadata(context)
        handle_streaming_request(request_iterator, method_id)
        send_header_metadata(context)
        trailers = (
            ("t9650vr8g8xku4", "k2939W2f4Tv753Q"),
            ("7s2u8ox25so0lk7", "Bs638d"),
        )
        raise_grpc_exception(context, grpc.StatusCode.PERMISSION_DENIED, "482Bi10914vvy19", trailers)
        return get_single_response(method_id, response_class)

    def StreamPerson(self, request_iterator, context):
        method_id = "person.PeopleService.streamPerson"
        response_class = GetPersonResponse
        receive_header_metadata(context)
        handle_streaming_request(request_iterator, method_id)
        send_header_metadata(context)
        for response in get_streaming_response(method_id, response_class):
            yield response
        trailers = (
            ("i7", "189L"),
            ("8h6r26h8gl3", "g7209G43r0z6D52ifj"),
            ("v7j40k8klx0dv4h8f", "231j0BF89g3RI59DU40"),
        )
        raise_grpc_exception(context, grpc.StatusCode.UNAUTHENTICATED, "zg42866", trailers)



def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_PeopleServiceServicer_to_server(PeopleServiceServicer(), server)
    server.add_insecure_port(f"[::]:{configs["server"]["port"]}")
    server.start()
    logging.info(f"[main] Server started on port {configs["server"]["port"]}")
    server.wait_for_termination()

if __name__ == '__main__':
    main()