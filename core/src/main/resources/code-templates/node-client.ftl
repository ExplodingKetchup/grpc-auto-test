import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/client/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir + config.log.filename);
    

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // Client generic rpc callback
    function genericClientRpcCallback(err, response, responseType, serviceName, methodName) {
        if (err) {
            logger.error(`[genericClientRpcCallback] RPC invoke failed: ${"$"}{err.message}\n${"$"}{err.stack}`);
        } else {
            logger.info(`[genericClientRpcCallback] Method ${"$"}{serviceName}.${"$"}{methodName} returns ${"$"}{JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, config.outDir + serviceName + "_" + methodName + "_return.bin");
        }
    }

    function main() {

        try {

<#list registry.getAllServices() as service>
            // >>> SERVICE ${service.name}
            let ${service.name?uncap_first}Stub = new protosGrpc.${service.ownerNamespaceName}.${service.name}(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${"$"}{config.server.host}:${"$"}{config.server.port}`);

    <#list registry.getAllMethods(service) as method>
            // METHOD ${method.name}
            let param_${method.ownerServiceName}_${method.name} = messageFromFile(
                config.testcaseDir + "${method.ownerServiceName}_${method.name}_param.bin",
                root.lookupType("${registry.lookupMessage(method.inType).ownerNamespaceName}.${method.inType}")
            );
            logger.info(`[main] Invoke ${method.ownerServiceName}.${method.name}, param: ${"$"}{JSON.stringify(param_${method.ownerServiceName}_${method.name}, null, 2)}`);
            ${method.ownerServiceName?uncap_first}Stub.${method.name}(
                param_${method.ownerServiceName}_${method.name},
                (err, response) => {
                    genericClientRpcCallback(
                        err,
                        response,
                        root.lookupType("${registry.lookupMessage(method.outType).ownerNamespaceName}.${method.outType}"),
                        "${method.ownerServiceName}",
                        "${method.name}"
                    )
                }
            )
    </#list>

            // <<< SERVICE ${service.name}
</#list>
        } catch (e) {
            logger.error(`[main] An error occurred: ${"$"}{e.message}\n${"$"}{e.stack}`);
        }

    }

    main();

})();