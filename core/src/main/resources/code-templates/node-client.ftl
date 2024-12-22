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
    const config = (await import('./config/client/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir, config.log.filename.slice(0, -4));
    

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // Client generic rpc callback
    function genericClientRpcCallback(err, response, responseType, methodId) {
        if (err) {
            logger.error(`[genericClientRpcCallback] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
        } else {
            logger.info(`[genericClientRpcCallback] Method ${"$"}{methodId} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return.bin");
        }
    }

    function main() {

        try {

<#list registry.getAllServices() as service>
            // >>> SERVICE ${service.name}
            let ${service.name?uncap_first}Stub = new protosGrpc.${service.id}(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${"$"}{config.server.host}:${"$"}{config.server.port}`);

    <#list registry.getAllMethods(service) as method>
        <#assign method_id = method.id?replace(".", "_")>
            // METHOD ${method.name}
            const meta_${method_id} = new grpc.Metadata();
            <#list metaMap?keys as metaKey>
            meta_${method_id}.set(META_KEY_${metaKey}, META_VALUE_${metaKey});
            </#list>
            let param_${method_id} = messageFromFile(
                config.testcaseDir + "${method_id}_param_0.bin",
                root.lookupType("${method.inType}")
            );
            logger.info(`[main] Invoke ${method.id}, param: ${"$"}{JSON.stringify(param_${method_id}, null, 2)}`);
            const call_${method_id} = ${service.name?uncap_first}Stub.${method.name}(
                param_${method_id},
                meta_${method_id},
                (err, response) => {
                    genericClientRpcCallback(
                        err,
                        response,
                        root.lookupType("${method.outType}"),
                        "${method.id}"
                    )
                }
            );
            call_${method_id}.on('metadata', metadata => {
                logger.info(`[main] ${method_id} - Received metadata ${JSON.stringify(metadata, null, 2)}`);
                metadataToFile(metadata, config.outDir + 'received_metadata.txt');
            })
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