import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';

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

    function invokeUnaryRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeUnaryRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function invokeServerStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeServerStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeServerStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeServerStreamingRpc] RPC invoke finished without error');
        });
    }

    function invokeClientStreamingRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeClientStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        const call = method(headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeClientStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        loopMultipleFilesWithSamePrefix(`${config.testcaseDir}${methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    logger.info(`[invokeClientStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
                    call.write(request);
                });
        call.end();
    }

    function invokeBidiStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const call = method(headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeBidiStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeBidiStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeBidiStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeBidiStreamingRpc] RPC invoke finished without error');
        });
        loopMultipleFilesWithSamePrefix(`${config.testcaseDir}${methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    logger.info(`[invokeBidiStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

            // >>> SERVICE HotpotService
            let hotpotServiceStub = new protosGrpc.repeated_hotpots.HotpotService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD unaryPot
            invokeUnaryRpc((request, headers, callback) => hotpotServiceStub.unaryPot(request, headers, callback), root.lookupType('repeated_hotpots.RequestMessage'), root.lookupType('repeated_hotpots.ResponseMessage'), 'repeated_hotpots.HotpotService.unaryPot');
            // METHOD serverStreamingPot
            invokeServerStreamingRpc((request, headers) => hotpotServiceStub.serverStreamingPot(request, headers), root.lookupType('repeated_hotpots.RequestMessage'), root.lookupType('repeated_hotpots.ResponseMessage'), 'repeated_hotpots.HotpotService.serverStreamingPot');
            // METHOD clientStreamingPot
            invokeClientStreamingRpc((headers, callback) => hotpotServiceStub.clientStreamingPot(headers, callback), root.lookupType('repeated_hotpots.RequestMessage'), root.lookupType('repeated_hotpots.ResponseMessage'), 'repeated_hotpots.HotpotService.clientStreamingPot');
            // METHOD bidiStreamingPot
            invokeBidiStreamingRpc((headers) => hotpotServiceStub.bidiStreamingPot(headers), root.lookupType('repeated_hotpots.RequestMessage'), root.lookupType('repeated_hotpots.ResponseMessage'), 'repeated_hotpots.HotpotService.bidiStreamingPot');

            // <<< SERVICE HotpotService
        } catch (e) {
            logger.error(`[main] An error occurred: ${e.message}\n${e.stack}`);
        }

    }

    main();

    // Add shutdown handler
    process.on('exit', (code) => {
        logger.info('Client shutting down...');
    })
})();