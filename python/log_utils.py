import logging
import os
import time


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
    if log_dir.endswith(os.sep):
        return log_dir + log_file_prefix + "_" + str(int(time.time())) + ".log"