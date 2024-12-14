import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

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
    function ${method.id?replace(".", "_")}(call, callback) {
        try {
            callback(null, ${method.id?replace(".", "_")}_impl(call.request));
        } catch (e) {
            logger.error(`[${method.id?replace(".", "_")}] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }
    }

    function ${method.id?replace(".", "_")}_impl(request) {
        logger.info(`[${method.id?replace(".", "_")}_impl] Received request: ${"$"}{JSON.stringify(request, null, 2)}`);

        let requestMessageType = root.lookupType("${method.inType}");
        let responseMessageType = root.lookupType("${method.outType}");
        messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "${method.id?replace(".", "_")}_param.bin");
        let retval = messageFromFile(
            config.testcaseDir + "${method.id?replace(".", "_")}_return.bin",
            responseMessageType
        );
        logger.info(`[${method.id?replace(".", "_")}_impl] Response: ${"$"}{JSON.stringify(retval, null, 2)}`)
        return retval;
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
