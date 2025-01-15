<#function generateTabs indent>
    <#local tabs = "" />
    <#if (indent > 0)>
        <#list 1..indent as i>
            <#local tabs = tabs + "    " />
        </#list>
    </#if>
    <#return tabs>
</#function>
<#macro pyParseException method indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if testcaseRegistry.getExceptionForMethod(method)??>
    <#assign rpcException = testcaseRegistry.getExceptionForMethod(method)>
    <#assign trailers = rpcException.trailingMetadata>
${tabs}trailers = (
        <#list trailers?keys as trailerKey>
${tabs}    ("${trailerKey}", "${trailers[trailerKey].getRight()}"),
        </#list>
${tabs})
${tabs}raise_grpc_exception(context, grpc.StatusCode.${rpcException.statusCode.name()}, "${rpcException.description}", trailers)
    </#if>
</#macro>
<#macro pyReceiveHeaders indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if registry.haveClientToServerMetadata()>
${tabs}receive_header_metadata(context)
    </#if>
</#macro>
<#macro pySendHeaders indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if registry.haveServerToClientMetadata()>
${tabs}send_header_metadata(context)
    </#if>
</#macro>
<#macro requestLogging indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logRequests>
${tabs}logging.info(f"[{method_name}] Received request: {request}")
        <#if logRequestsPrintFields>
${tabs}log_fields_of_object(request, f"{method_id} - request", request_type_field_names)
        </#if>
    </#if>
</#macro>
<#macro responseLogging indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logResponses>
${tabs}logging.info(f"[{method_name}] Response: {response}")
        <#if logResponsesPrintFields>
${tabs}log_fields_of_object(response, f"{method_id} - response", response_type_field_names)
        </#if>
    </#if>
</#macro>
import logging
import os
from concurrent import futures

from google.protobuf.message import Message

from config_utils import load_config
from file_utils import list_files_with_same_prefix
from log_utils import configure_logger, get_log_file_for_this_instance, log_fields_of_object
from message_utils import message_to_file, message_from_file, format_metadata_as_string, metadata_to_file
<#list registry.getProtoFiles() as proto_file>
from ${proto_file}_pb2 import *
from ${proto_file}_pb2_grpc import *
</#list>


# Constants
BIN_METADATA_SUFFIX = "-bin"
<#assign metaMap = registry.getAllServerToClientMetadata()>
<#list metaMap?keys as metaKey>
    <#assign metaPair = metaMap[metaKey]>
    <#assign metaType = metaPair.getLeft().name()> <#-- MetadataType -->
    <#assign metaValue = metaPair.getRight()> <#-- Metadata value -->
    <#if metaType == "STRING">
META_KEY_${metaKey} = "${metaKey}"
META_VALUE_${metaKey} = "${metaValue}"
    <#elseif metaType == "BIN">
META_KEY_${metaKey} = "${metaKey}" + BIN_METADATA_SUFFIX
META_VALUE_${metaKey} = bytes.fromhex("${metaValue}")
    </#if>
</#list>
OUTBOUND_HEADERS = (
<#list metaMap?keys as metaKey>
    (META_KEY_${metaKey}, META_VALUE_${metaKey}),
</#list>
)
<#list registry.getAllMessages() as message>
${message.id?replace(".", "_")}_fields = [<#list registry.getAllFieldNames(message.id) as fieldname>"${fieldname}"<#sep>, </#list>]
</#list>


# Configs
configs = load_config(is_server=True)
print(f"Configs: {configs}")


# >>> Helper functions
def handle_single_request(request: Message, method_id: str, request_type_field_names: list[str]) -> None:
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    <@requestLogging indent=1/>
    message_to_file(os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_0.bin"), request)

def handle_streaming_request(request_iterator, method_id, request_type_field_names: list[str]):
    request_idx = 0
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    for request in request_iterator:
        <@requestLogging indent=2/>
        message_to_file(
            os.path.join(configs["out"]["dir"], f"{method_id_underscore}_param_{request_idx}.bin"),
            request)
        request_idx += 1

def get_single_response(method_id, response_class, response_type_field_names: list[str]):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response = message_from_file(os.path.join(configs["in"]["dir"], f"{method_id_underscore}_return_0.bin"),
                                 response_class)
    <@responseLogging indent=1/>

    return response

def get_streaming_response(method_id, response_class, response_type_field_names: list[str]):
    method_name = method_id.split(".")[-1]
    method_id_underscore = method_id.replace(".", "_")
    response_files = list_files_with_same_prefix(configs["in"]["dir"], f"{method_id_underscore}_return")
    for response_file in response_files:
        response = message_from_file(response_file, response_class)
        <@responseLogging indent=2/>
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

<#list registry.getAllServices() as service>
class ${service.name}Servicer(${service.name}Servicer):

    <#list registry.getAllMethods(service) as method>
    <#assign response_class = method.outType?split(".")?last>
    <#assign requestFields = method.inType?replace(".", "_") + "_fields">
    <#assign responseFields = method.outType?replace(".", "_") + "_fields">
        <#if method.type == "UNARY">
    def ${method.name?cap_first}(self, request, context):
        method_id = "${method.id}"
        response_class = ${response_class}

        <@pyReceiveHeaders indent=2/>
        handle_single_request(request, method_id, ${requestFields})
        <@pySendHeaders indent=2/>
        <@pyParseException method=method indent=2 />
        return get_single_response(method_id, response_class, ${responseFields})

        <#elseif method.type == "SERVER_STREAMING">
    def ${method.name?cap_first}(self, request, context):
        method_id = "${method.id}"
        response_class = ${response_class}

        <@pyReceiveHeaders indent=2/>
        handle_single_request(request, method_id, ${requestFields})
        <@pySendHeaders indent=2/>
        for response in get_streaming_response(method_id, response_class, ${responseFields}):
            yield response
        <@pyParseException method=method indent=2 />

        <#elseif method.type == "CLIENT_STREAMING">
    def ${method.name?cap_first}(self, request_iterator, context):
        method_id = "${method.id}"
        response_class = ${response_class}

        <@pyReceiveHeaders indent=2/>
        handle_streaming_request(request_iterator, method_id, ${requestFields})
        <@pySendHeaders indent=2/>
        <@pyParseException method=method indent=2 />
        return get_single_response(method_id, response_class, ${responseFields})

        <#elseif method.type == "BIDI_STREAMING">
    def ${method.name?cap_first}(self, request_iterator, context):
        method_id = "${method.id}"
        response_class = ${response_class}
        <@pyReceiveHeaders indent=2/>
        handle_streaming_request(request_iterator, method_id, ${requestFields})
        <@pySendHeaders indent=2/>
        for response in get_streaming_response(method_id, response_class, ${responseFields}):
            yield response
        <@pyParseException method=method indent=2 />

        </#if>
    </#list>
</#list>


def main():
    configure_logger(get_log_file_for_this_instance(configs["log"]["dir"], configs["log"]["file_prefix"]))

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
<#list registry.getAllServices() as service>
    add_${service.name}Servicer_to_server(${service.name}Servicer(), server)
</#list>
    server.add_insecure_port(f"[::]:{configs["server"]["port"]}")
    server.start()
    logging.info(f"[main] Server started on port {configs["server"]["port"]}")
    server.wait_for_termination()

if __name__ == '__main__':
    main()