import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/server/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir + config.log.filename);

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // BEGIN RPC methods implementation
<#list registry.getAllMethods() as method>
    function ${method.ownerServiceName}_${method.name}(call, callback) {
        try {
            callback(null, ${method.ownerServiceName}_${method.name}Impl(call.request));
        } catch (e) {
            logger.error(`[${method.ownerServiceName}_${method.name}] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }
    }

    function ${method.ownerServiceName}_${method.name}Impl(request) {
        logger.info(`[${method.ownerServiceName}_${method.name}Impl] Received request: ${"$"}{JSON.stringify(request, null, 2)}`);

        let requestMessageType = root.lookupType("${registry.lookupMessage(method.inType).ownerNamespaceName}.${registry.lookupMessage(method.inType).name}");
        let responseMessageType = root.lookupType("${registry.lookupMessage(method.outType).ownerNamespaceName}.${registry.lookupMessage(method.outType).name}");
        messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "${method.ownerServiceName}_${method.name}_param.bin");
        let retval = messageFromFile(
            config.testcaseDir + "${method.ownerServiceName}_${method.name}_return.bin",
            responseMessageType
        );
        logger.info(`[${method.ownerServiceName}_${method.name}Impl] Response: ${"$"}{JSON.stringify(retval, null, 2)}`)
        return retval;
    }

</#list>
    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

<#list registry.getAllServices() as service>
        server.addService(protosGrpc.${service.ownerNamespaceName}.${service.name}.service, {
    <#list registry.getAllMethods(service) as method>
            ${method.name}: ${method.ownerServiceName}_${method.name}
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
