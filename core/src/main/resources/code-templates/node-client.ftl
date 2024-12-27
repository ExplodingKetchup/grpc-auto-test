import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

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

    function invokeUnaryRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
                errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeUnaryRpc] Method ${"$"}{methodId} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] Invoke ${"$"}{methodId}, param: ${"$"}{JSON.stringify(request, null, 2)}`);
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function invokeServerStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeServerStreamingRpc] Invoke ${"$"}{methodId}, param: ${"$"}{JSON.stringify(request, null, 2)}`);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] Method ${"$"}{methodId} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
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

    function invokeClientStreamingRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
                errorToFile(err, `${"$"}{config.outDir}${"$"}{methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeClientStreamingRpc] Method ${"$"}{methodId} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
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
                    logger.info(`[invokeClientStreamingRpc] Invoke ${"$"}{methodId}, param: ${"$"}{JSON.stringify(request, null, 2)}`);
                    call.write(request);
                });
        call.end();
    }

    function invokeBidiStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const call = method(headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeBidiStreamingRpc] Received metadata ${"$"}{JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeBidiStreamingRpc] Method ${"$"}{methodId} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
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
                    logger.info(`[invokeBidiStreamingRpc] Invoke ${"$"}{methodId}, param: ${"$"}{JSON.stringify(request, null, 2)}`);
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
            // METHOD ${method.name}
        <#if method.type == "UNARY">
        <#assign methodParam = "(request, headers, callback) => " + service.name?uncap_first + "Stub." + method.name + "(request, headers, callback)">
            invokeUnaryRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}');
        <#elseif method.type == "SERVER_STREAMING">
        <#assign methodParam = "(request, headers) => " + service.name?uncap_first + "Stub." + method.name + "(request, headers)">
            invokeServerStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}');
        <#elseif method.type == "CLIENT_STREAMING">
        <#assign methodParam = "(headers, callback) => " + service.name?uncap_first + "Stub." + method.name + "(headers, callback)">
            invokeClientStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}');
        <#elseif method.type == "BIDI_STREAMING">
        <#assign methodParam = "(headers) => " + service.name?uncap_first + "Stub." + method.name + "(headers)">
            invokeBidiStreamingRpc(${methodParam}, ${requestTypeParam}, ${responseTypeParam}, '${method.id}');
        </#if>
    </#list>

            // <<< SERVICE ${service.name}
</#list>
        } catch (e) {
            logger.error(`[main] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }

    }

    main();

    // Keep running for 1 min (60000 ms)
    setTimeout(() => {}, 60000);

})();