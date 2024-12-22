import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

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
const META_VALUE_${metaKey} = '${metaValue}';
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
    function ${method_id}(call, callback) {
        try {
            logger.info(`[method_id] Received metadata ${"$"}{JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("${method.inType}");
            let responseMessageType = root.lookupType("${method.outType}");

            logger.info(`[${method_id}] Received request: ${"$"}{JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "${method_id}_param_0.bin");

            const metadata = new grpc.Metadata();
<#list metaMap?keys as metaKey>
            metadata.set(META_KEY_${metaKey}, META_VALUE_${metaKey});
</#list>
            call.sendMetadata(metadata);

            let retval = messageFromFile(
                config.testcaseDir + "${method_id}_return_0.bin",
                responseMessageType
            );
            logger.info(`[${method_id}] Response: ${"$"}{JSON.stringify(retval, null, 2)}`)
            callback(null, retval);
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
            ${method.name}: ${method.id?replace(".", "_")}
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
