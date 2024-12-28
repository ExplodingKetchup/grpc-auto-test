import logging
from google.protobuf.message import Message


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
            return message_class().ParseFromString(f.read())
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