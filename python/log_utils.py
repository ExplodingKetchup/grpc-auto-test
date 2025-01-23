import logging
import os
import time

from google.protobuf.message import Message

def configure_logger(log_file: str):
    """
    Configure the default logger to log to both console and file.

    :param log_file: Path to the log file.
    """
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)

    file_handler = logging.FileHandler(log_file, mode="a", encoding="utf-8")
    file_handler.setLevel(logging.INFO)

    formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
    console_handler.setFormatter(formatter)
    file_handler.setFormatter(formatter)

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

def get_log_file_for_this_instance(log_dir: str, log_file_prefix: str) -> str:
    return os.path.join(log_dir, log_file_prefix + "_" + str(int(time.time())) + ".log")

def log_fields_of_object(obj: object, obj_name: str, field_names: list[str]) -> None:
    for field_name in field_names:
        try:
            value = getattr(obj, field_name)
            if isinstance(obj, Message):
                field_presence = ""
                try:
                    field_presence = str(obj.HasField(field_name))
                except ValueError:
                    field_presence = "N/A"
                logging.info(f"[log_fields_of_object] {obj_name}: {field_name} ({type(value).__name__}; field_presence = {field_presence}) = {value}")
            else:
                logging.info(f"[log_fields_of_object] {obj_name}: {field_name} ({type(value).__name__}) = {value}")
        except AttributeError:
            logging.warning(f"[log_fields_of_object] Field {field_name} is not defined in message")
