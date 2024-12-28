import logging

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