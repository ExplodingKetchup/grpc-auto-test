import logging
import os


def string_to_file(filepath: str, content: str):
    """
    Write a string to a file. Creates a new file if it doesn't exist or overwrites if it does.

    :param filepath: Path to the file
    :param content: String content to write to the file
    """
    try:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
    except Exception as e:
        logging.error("[string_to_file] An error occurred:", exc_info=e)


def file_to_string(filepath: str) -> str:
    """
    Read and return content of a file as a string. Assume that the file is text encoded with
    utf-8.
    :param filepath:
    :return:
    """
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        logging.error("[file_to_string] An error occurred:", exc_info=e)


def list_files_with_same_prefix(search_dir: str, prefix: str) -> list:
    """
    Lists all files in the specified directory that start with the given prefix.

    :param search_dir: str - Path to the directory to search in.
    :param prefix: str - Prefix to match file names against.
    :return: list - List of file paths with the matching prefix.
    """
    if not os.path.isdir(search_dir):
        raise ValueError(f"The specified path '{search_dir}' is not a valid directory.")

    # Get all files in the directory with the matching prefix
    matching_files = [os.path.join(search_dir, f) for f in os.listdir(search_dir) if f.startswith(prefix)]

    return matching_files
