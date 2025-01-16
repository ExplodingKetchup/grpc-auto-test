<#function generateTabs indent>
    <#local tabs = "" />
    <#if (indent > 0)>
        <#list 1..indent as i>
            <#local tabs = tabs + "    " />
        </#list>
    </#if>
    <#return tabs>
</#function>
<#macro requestLogging invoker indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logRequests>
${tabs}logger.info(`[${invoker}] ${"$"}{methodId} - Request: ${"$"}{JSON.stringify(request, null, 2)}`);
        <#if logRequestsPrintFields>
${tabs}logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
        </#if>
    </#if>
</#macro>
<#macro responseLogging invoker indent=0>
    <#assign tabs = generateTabs(indent)>
    <#if logResponses>
${tabs}logger.info(`[${invoker}] ${"$"}{methodId} - Response: ${"$"}{JSON.stringify(response, null, 2)}`);
        <#if logResponsesPrintFields>
${tabs}logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
        </#if>
    </#if>
</#macro>
import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
<#assign metaMap = registry.getAllClientToServerMetadata()>
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

<#list registry.getAllMessages() as message>
const ${message.id?replace(".", "_")}_fields = [<#list registry.getAllFieldNames(message.id) as fieldname>"${fieldname}"<#sep>, </#list>];
</#list>

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/client/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir, config.log.filename.slice(0, -4));
    

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);
    const headers = new grpc.Metadata();
<#list metaMap?keys as metaKey>
    headers.set(META_KEY_${metaKey}, META_VALUE_${metaKey});
</#list>

    function invokeUnaryRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
                errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                <@responseLogging invoker="invokeUnaryRpc" indent=4/>
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        <@requestLogging invoker="invokeUnaryRpc" indent=2/>
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function invokeServerStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        let responseIdx = 0;
        const request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        <@requestLogging invoker="invokeServerStreamingRpc" indent=2/>
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            <@responseLogging invoker="invokeServerStreamingRpc" indent=3/>
            messageToFile(responseType.fromObject(response), responseType, `${"$"}{config.outDir}${"$"}{methodId.replaceAll('.', '_')}_return_${"$"}{responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeServerStreamingRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
            errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeServerStreamingRpc] RPC invoke finished without error');
        });
    }

    function invokeClientStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
                errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                <@responseLogging invoker="invokeClientStreamingRpc" indent=4/>
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        const call = method(headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeClientStreamingRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        loopMultipleFilesWithSamePrefix(`${"$"}{config.testcaseDir}${"$"}{methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    <@requestLogging invoker="invokeClientStreamingRpc" indent=5/>
                    call.write(request);
                });
        call.end();
    }

    function invokeBidiStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        let responseIdx = 0;
        const call = method(headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeBidiStreamingRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            <@responseLogging invoker="invokeBidiStreamingRpc" indent=3/>
            messageToFile(responseType.fromObject(response), responseType, `${"$"}{config.outDir}${"$"}{methodId.replaceAll('.', '_')}_return_${"$"}{responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeBidiStreamingRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
            errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeBidiStreamingRpc] RPC invoke finished without error');
        });
        loopMultipleFilesWithSamePrefix(`${"$"}{config.testcaseDir}${"$"}{methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    <@requestLogging invoker="invokeBidiStreamingRpc" indent=5/>
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

<#list registry.getAllServices() as service>
            // >>> SERVICE ${service.name}
            let ${service.name?uncap_first}Stub = new protosGrpc.${service.id}(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${"$"}{config.server.host}:${"$"}{config.server.port}`);

    <#list registry.getAllMethods(service) as method>
    <#assign requestTypeParam = "root.lookupType('" + method.inType + "')">
    <#assign responseTypeParam = "root.lookupType('" + method.outType + "')">
    <#assign requestFields = method.inType?replace(".", "_") + "_fields">
    <#assign responseFields = method.outType?replace(".", "_") + "_fields">
            // METHOD ${method.name}
        <#if method.type == "UNARY">
        <#assign methodParam = "(request, headers, callback) => " + service.name?uncap_first + "Stub." + method.name + "(request, headers, callback)">
            invokeUnaryRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}', ${requestFields}, ${responseFields});
        <#elseif method.type == "SERVER_STREAMING">
        <#assign methodParam = "(request, headers) => " + service.name?uncap_first + "Stub." + method.name + "(request, headers)">
            invokeServerStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}', ${requestFields}, ${responseFields});
        <#elseif method.type == "CLIENT_STREAMING">
        <#assign methodParam = "(headers, callback) => " + service.name?uncap_first + "Stub." + method.name + "(headers, callback)">
            invokeClientStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}', ${requestFields}, ${responseFields});
        <#elseif method.type == "BIDI_STREAMING">
        <#assign methodParam = "(headers) => " + service.name?uncap_first + "Stub." + method.name + "(headers)">
            invokeBidiStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}', ${requestFields}, ${responseFields});
        </#if>
    </#list>

            // <<< SERVICE ${service.name}
</#list>
        } catch (e) {
            logger.error(`[main] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }

        setTimeout(() => logger.info("[main] Shutdown delay timer elapsed"), 5000);

    }

    main();

    // Add shutdown handler
    process.on('exit', (code) => {
        logger.info('Client shutting down...');
    })
})();