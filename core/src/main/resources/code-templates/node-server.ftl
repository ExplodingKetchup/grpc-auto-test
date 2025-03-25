<#function generateTabs indent>
    <#local tabs = "" />
    <#if (indent > 0)>
        <#list 1..indent as i>
            <#local tabs = tabs + "    " />
        </#list>
    </#if>
    <#return tabs>
</#function>
<#macro nodejsParseException method indent=0>
    <#assign tabs = generateTabs(indent)/>
    <#if testcaseRegistry.getExceptionForMethod(method)??>
    <#assign rpcException = testcaseRegistry.getExceptionForMethod(method)>
    <#assign trailers = rpcException.trailingMetadata>
${tabs}const trailers = new grpc.Metadata();
        <#list trailers?keys as trailerKey>
${tabs}trailers.set("${trailerKey}", "${trailers[trailerKey].getRight()}");
        </#list>
${tabs}const rpcException = {code: grpc.status.${rpcException.statusCode.name()}, details: '${rpcException.description}', metadata: trailers};
    </#if>
</#macro>
<#macro requestLogging method indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logRequests>
${tabs}logger.info(`[${method.id}] Request:\n${"$"}{JSON.stringify(request, null, 2)}`);
        <#if logRequestsPrintFields>
${tabs}logFieldsOfObject(logger, request, "${method.id} - request", [<#list registry.getAllFieldNames(method.inType) as fieldname>"${fieldname}"<#sep>, </#list>]);
        </#if>
    </#if>
</#macro>
<#macro responseLogging method indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logResponses>
${tabs}logger.info(`[${method.id}] Response:\n${"$"}{JSON.stringify(response, null, 2)}`);
        <#if logResponsesPrintFields>
${tabs}logFieldsOfObject(logger, response, "${method.id} - response", [<#list registry.getAllFieldNames(method.outType) as fieldname>"${fieldname}"<#sep>, </#list>]);
        </#if>
    </#if>
</#macro>

import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
<#assign metaMap = registry.getAllServerToClientMetadata()>
<#list metaMap?keys as metaKey>
    <#assign metaPair = metaMap[metaKey]>
    <#assign metaType = metaPair.getLeft().name()> <#-- MetadataType -->
    <#assign metaValue = metaPair.getRight()> <#-- Metadata value -->
    <#if metaType == "STRING">
const META_KEY_${metaKey} = '${metaKey}';
const META_VALUE_${metaKey} = '${metaValue}';
    <#elseif metaType == "BIN">
const META_KEY_${metaKey} = '${metaKey}' + BIN_SUFFIX;
const META_VALUE_${metaKey} = Buffer.from('${metaValue}', 'hex');
    </#if>
</#list>

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/server/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir, config.log.filename.slice(0, -4));

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // BEGIN RPC methods implementation
<#list registry.getAllMethods() as method>
<#assign method_id = method.id?replace(".", "_")>
<#-- RPC method signature -->
    <#if method.type == "UNARY" || method.type == "CLIENT_STREAMING">
    function ${method_id}(call, callback) {
    <#elseif method.type == "SERVER_STREAMING" || method.type =="BIDI_STREAMING">
    function ${method_id}(call) {
    </#if>
<#-- Process received metadata and get request and response data type -->
        try {
            logger.info(`[${method_id}] Received metadata ${"$"}{JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("${method.inType}");
            let responseMessageType = root.lookupType("${method.outType}");

<#-- Process incoming requests -->
    <#if method.type == "UNARY" || method.type == "SERVER_STREAMING">
            const request = call.request;
            <@requestLogging method=method indent=3/>
            messageToFile(request, requestMessageType, config.outDir + "${method_id}_param_0.bin");
    <#elseif method.type == "CLIENT_STREAMING" || method.type =="BIDI_STREAMING">
            let requestIdx = 0;
            call.on('data', (request) => {
                <@requestLogging method=method indent=4/>
                messageToFile(request, requestMessageType, config.outDir + `${method_id}_param_${"$"}{requestIdx++}.bin`);
            });
    </#if>

<#-- Send headers -->
            const metadata = new grpc.Metadata();
    <#list metaMap?keys as metaKey>
            metadata.set(META_KEY_${metaKey}, META_VALUE_${metaKey});
    </#list>
            call.sendMetadata(metadata);

<#-- Send headers, then responses / exceptions -->
    <#if method.type == "UNARY">
            const response = messageFromFile(
                config.testcaseDir + "${method_id}_return_0.bin",
                responseMessageType
            );
            <@responseLogging method=method indent=3/>
            callback(null, response);
        <#if testcaseRegistry.getExceptionForMethod(method)??>
            <@nodejsParseException method=method indent=3 />
            callback(rpcException);
        </#if>
    <#elseif method.type == "SERVER_STREAMING">
            loopMultipleFilesWithSamePrefix(config.testcaseDir + '${method_id}_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        <@responseLogging method=method indent=6/>
                        call.write(response);
                    });
        <#if testcaseRegistry.getExceptionForMethod(method)??>
            <@nodejsParseException method=method indent=3 />
            call.emit('error', rpcException);
        <#else>
            call.end();
        </#if>
    <#elseif method.type == "CLIENT_STREAMING">
            call.on('end', () => {
                const response = messageFromFile(
                    config.testcaseDir + "${method_id}_return_0.bin",
                    responseMessageType
                );
                <@responseLogging method=method indent=4/>
                callback(null, response);
        <#if testcaseRegistry.getExceptionForMethod(method)??>
                <@nodejsParseException method=method indent=4 />
                callback(rpcException);
        </#if>
            });
    <#elseif method.type == "BIDI_STREAMING">
            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + '${method_id}_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            <@responseLogging method=method indent=7/>
                            call.write(response);
                        });
        <#if testcaseRegistry.getExceptionForMethod(method)??>
                <@nodejsParseException method=method indent=4 />
                call.emit('error', rpcException);
        <#else>
               call.end();
        </#if>
            });
    </#if>

<#-- Catch exceptions and close function -->
        } catch (e) {
            logger.error(`[${method_id}] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }
    }

</#list>
    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

<#list registry.getAllServices() as service>
        server.addService(protosGrpc.${service.id}.service, {
    <#list registry.getAllMethods(service) as method>
            ${method.name}: ${method.id?replace(".", "_")}<#sep>,
    </#list>
        });

</#list>
        return server;
    }

    try {
        let server = getServer();

        server.bindAsync('0.0.0.0:' + config.server.port, grpc.ServerCredentials.createInsecure(), () => {
            logger.info("Server started on " + config.server.host + ":" + config.server.port);
        });
    } catch (e) {
        logger.error(`Server failed to start: ${"$"}{e.message}\n${"$"}{e.stack}`);
    }

    // END Server
})();
