import logging
import os
from concurrent import futures

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_to_file, message_from_file
from rpc_pb2 import *
from rpc_pb2_grpc import *


# Constants

# Configs
configs = load_config(is_server=True)
print(f"Configs: {configs}")


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

class PeopleServiceServicer(PeopleServiceServicer):

    def GetPerson(self, request, context):
        method_id = "person.PeopleService.getPerson"
        response_class = GetPersonResponse

        handle_single_request(request, method_id)
        return get_single_response(method_id, response_class)

    def ListPerson(self, request, context):
        method_id = "person.PeopleService.listPerson"
        response_class = GetPersonResponse

        handle_single_request(request, method_id)
        for response in get_streaming_response(method_id, response_class):
            yield response

    def RegisterPerson(self, request_iterator, context):
        method_id = "person.PeopleService.registerPerson"
        response_class = GetPersonResponse

        handle_streaming_request(request_iterator, method_id)
        return get_single_response(method_id, response_class)

    def StreamPerson(self, request_iterator, context):
        method_id = "person.PeopleService.streamPerson"
        response_class = GetPersonResponse

        handle_streaming_request(request_iterator, method_id)
        for response in get_streaming_response(method_id, response_class):
            yield response

def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_PeopleServiceServicer_to_server(PeopleServiceServicer(), server)
    server.add_insecure_port(f"[::]:{configs["server"]["port"]}")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    main()