<#macro nodejsParseException method>
    <#if testcaseRegistry.getExceptionForMethod(method)??>
    <#assign rpcException = testcaseRegistry.getExceptionForMethod(method)>
    <#assign trailers = rpcException.trailingMetadata>
const trailers = new grpc.Metadata();
        <#list trailers?keys as trailerKey>
trailers.set(${trailerKey}, ${trailers[trailerKey].getRight()});
        </#list>
const rpcException = {code: grpc.status.${rpcException.statusCode.name()}, details: '${rpcException.description}', metadata: trailers};
    </#if>
</#macro>

import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

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
            logger.info(`[${method_id}] Received request: ${"$"}{JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "${method_id}_param_0.bin");
    <#elseif method.type == "CLIENT_STREAMING" || method.type =="BIDI_STREAMING">
            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[${method_id}] Received request: ${"$"}{JSON.stringify(request, null, 2)}`);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `${method_id}_param_${"$"}{requestIdx++}.bin`);
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
        <#if testcaseRegistry.getExceptionForMethod(method)??>
            <@nodejsParseException method=method />
            callback(rpcException);
        <#else>
            let retval = messageFromFile(
                config.testcaseDir + "${method_id}_return_0.bin",
                responseMessageType
            );
            logger.info(`[${method_id}] Response: ${"$"}{JSON.stringify(retval, null, 2)}`);
            callback(null, retval);
        </#if>
    <#elseif method.type == "SERVER_STREAMING">
            loopMultipleFilesWithSamePrefix(config.testcaseDir + '${method_id}_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[${method_id}] Response: ${"$"}{JSON.stringify(response, null, 2)}`);
                        call.write(response);
                    });
        <#if testcaseRegistry.getExceptionForMethod(method)??>
            <@nodejsParseException method=method />
            call.emit('error', rpcException);
        <#else>
            call.end();
        </#if>
    <#elseif method.type == "CLIENT_STREAMING">
            call.on('end', () => {
        <#if testcaseRegistry.getExceptionForMethod(method)??>
                <@nodejsParseException method=method />
                callback(rpcException);
        <#else>
               let retval = messageFromFile(
                   config.testcaseDir + "${method_id}_return_0.bin",
                   responseMessageType
               );
               logger.info(`[${method_id}] Response: ${"$"}{JSON.stringify(retval, null, 2)}`);
               callback(null, retval);
        </#if>
            });
    <#elseif method.type == "BIDI_STREAMING">
            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + '${method_id}_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[${method_id}] Response: ${"$"}{JSON.stringify(response, null, 2)}`);
                            call.write(response);
                        });
        <#if testcaseRegistry.getExceptionForMethod(method)??>
                <@nodejsParseException method=method />
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
