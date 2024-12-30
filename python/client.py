import logging
import os.path
from typing import Iterator

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from message_utils import message_from_file, message_to_file
from rpc_pb2 import *
from rpc_pb2_grpc import *

configs = load_config(is_server=False)
print(f"Configs: {configs}")


def request_iterator_wrapper(request_iterator: Iterator[Message], calling_method_name: str, method_id: str) -> Iterator[
    Message]:
    for request in request_iterator:
        logging.info(f"[{calling_method_name}] Invoke {method_id} with request {request}")
        yield request


def read_request_from_file(method_id: str, request_class: type, read_multiple: bool = False) -> Message | Iterator[
    Message]:
    if read_multiple:
        for file_path in list_files_with_same_prefix(configs["in"]["dir"], f"{method_id.replace(".", "_")}_param"):
            yield message_from_file(file_path, request_class)
    else:
        return message_from_file(os.path.join(configs["in"]["dir"], f"{method_id.replace(".", "_")}_param"),
                                 request_class)


def invoke_unary_rpc(method, request: Message, method_id: str):
    logging.info(f"[invoke_unary_rpc] Invoke {method_id} with request {request}")
    response = method(request)
    logging.info(f"[invoke_unary_rpc] Method {method_id} returns {response}")
    message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                    response)


def invoke_server_streaming_rpc(method, request: Message, method_id: str):
    logging.info(f"[invoke_server_streaming_rpc] Invoke {method_id} with request {request}")
    response_idx = 0
    for response in method(request):
        logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_{response_idx}.bin"),
                        response)
        response_idx += 1


def invoke_client_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str):
    response = method(request_iterator_wrapper(request_iterator,
                                               calling_method_name="invoke_client_streaming_rpc",
                                               method_id=method_id))
    logging.info(f"[invoke_server_streaming_rpc] Method {method_id} returns {response}")
    message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_0.bin"),
                    response)


def invoke_bidi_streaming_rpc(method, request_iterator: Iterator[Message], method_id: str):
    response_idx = 0
    for response in method(request_iterator_wrapper(request_iterator,
                                                    calling_method_name="invoke_bidi_streaming_rpc",
                                                    method_id=method_id)):
        logging.info(f"[invoke_bidi_streaming_rpc] Method {method_id} returns {response}")
        message_to_file(os.path.join(configs["out"]["dir"], f"{method_id.replace(".", "_")}_return_{response_idx}.bin"),
                        response)
        response_idx += 1


def main():
    channel = grpc.insecure_channel(f"{configs["server"]["host"]}:{configs["server"]["port"]}")

    # >>> SERVICE PeopleService
    people_service_stub = PeopleServiceStub(channel)

    # METHOD person.PeopleService.getPerson
    invoke_unary_rpc(method=people_service_stub.GetPerson,
                     request=read_request_from_file(
                         method_id="person.PeopleService.getPerson",
                         request_class=GetPersonRequest,
                         read_multiple=False
                     ),
                     method_id="person.PeopleService.getPerson")

    # METHOD person.PeopleService.listPerson
    invoke_server_streaming_rpc(method=people_service_stub.ListPerson,
                                request=read_request_from_file(
                                    method_id="person.PeopleService.listPerson",
                                    request_class=GetPersonRequest,
                                    read_multiple=False
                                ),
                                method_id="person.PeopleService.listPerson")

    # METHOD person.PeopleService.registerPerson
    invoke_server_streaming_rpc(method=people_service_stub.RegisterPerson,
                                request=read_request_from_file(
                                    method_id="person.PeopleService.registerPerson",
                                    request_class=GetPersonRequest,
                                    read_multiple=True
                                ),
                                method_id="person.PeopleService.registerPerson")

    # METHOD person.PeopleService.streamPerson
    invoke_server_streaming_rpc(method=people_service_stub.StreamPerson,
                                request=read_request_from_file(
                                    method_id="person.PeopleService.streamPerson",
                                    request_class=GetPersonRequest,
                                    read_multiple=True
                                ),
                                method_id="person.PeopleService.streamPerson")


if __name__ == '__main__':
    main()
