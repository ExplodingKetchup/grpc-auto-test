import logging

import grpc
from google.protobuf.message import Message

from file_utils import string_to_file


def message_from_file(filepath: str, message_class: type) -> Message | None:
    """
    Read and parse content of a file (.bin) as an instance of the message class specified.
    :param filepath:
    :param message_class:
    :return: An instance of message_class parsed from file at filepath
    """
    if (not isinstance(message_class, type)) or (not issubclass(message_class, Message)):
        return None
    try:
        with open(filepath, "rb") as f:
            message = message_class()
            message.ParseFromString(f.read())
            return message
    except IOError as ioe:
        logging.error("[messageFromFile] Reading from file failed", exc_info=ioe)

def message_to_file(filepath: str, message: Message):
    """
    Serialize contents of the message using protobuf and write the binary content to file.
    :param filepath:
    :param message:
    :return:
    """
    with open(filepath, "wb") as f:
        f.write(message.SerializeToString())

def format_metadata_as_string(metadata) -> str:
    if metadata is None:
        return ""
    return "\n".join(f"{key}:{value.hex() if key.endswith("-bin") else value}" for key, value in metadata)

def format_grpc_error_as_string(rpc_error: grpc.RpcError) -> str:
    return "\n".join([rpc_error.code().name, rpc_error.details(), format_metadata_as_string(rpc_error.trailing_metadata())])

def metadata_to_file(filepath: str, metadata):
    string_to_file(filepath, format_metadata_as_string(metadata) + "\n", append=True)

def grpc_error_to_file(filepath: str, rpc_error: grpc.RpcError):
    string_to_file(filepath, format_grpc_error_as_string(rpc_error))