import logging
import os

from config_utils import load_config
from log_utils import configure_logger, get_log_file_for_this_instance
from message_utils import message_to_file, message_from_file
from protoc_generated.rpc_pb2 import *
from protoc_generated.rpc_pb2_grpc import *


# Constants

# Configs
configs = load_config(is_server=True)

class PeopleServiceServicer(PeopleServiceServicer):

    def GetPerson(self, request, context):
        logging.info(f"[GetPerson] Received request: {request}")
        message_to_file(f"{configs["out"]["dir"]}{os.sep}person_PeopleService_getPerson_param_0.bin", request)
        retval = message_from_file(f"{configs["in"]["dir"]}{os.sep}person_PeopleService_getPerson_return_0.bin", GetPersonRequest)
        logging.info(f"[GetPerson] Response: {retval}")
        return retval

    def ListPerson(self, request, context):
        """SERVER_STREAMING
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def RegisterPerson(self, request_iterator, context):
        """CLIENT_STREAMING
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def StreamPerson(self, request_iterator, context):
        """BIDI_STREAMING
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

def main():
    # configure_logger(get_log_file_for_this_instance())
    retval = message_from_file(f"{configs["in"]["dir"]}{os.sep}person_PeopleService_getPerson_return_0.bin", GetPersonRequest)
    print("Done")

if __name__ == '__main__':
    main()