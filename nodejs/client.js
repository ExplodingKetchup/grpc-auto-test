import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const SUPPORTED_COMPRESSION_ALGO = ["", "deflate", "gzip", "gzip-stream"];
const BIN_SUFFIX = '-bin';

const map_hotpot_MapPot_fields = ["int_double_value", "int_int_value", "int_bool_value", "int_string_value", "int_bytes_value", "int_enum_value", "bool_double_value", "bool_bool_value", "bool_string_value", "bool_bytes_value", "bool_enum_value", "string_double_value", "string_string_value", "string_bytes_value", "string_enum_value"];
const map_hotpot_MapPotReversed_fields = ["string_enum_value", "string_bytes_value", "string_string_value", "string_double_value", "bool_enum_value", "bool_bytes_value", "bool_string_value", "bool_bool_value", "bool_double_value", "int_enum_value", "int_bytes_value", "int_string_value", "int_bool_value", "int_int_value", "int_double_value"];

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

    function invokeUnaryRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeUnaryRpc] ${methodId} - Response:\n${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
                messageToFile(response, responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] ${methodId} - Request:\n${JSON.stringify(request, null, 2)}`);
        logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function invokeServerStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        let responseIdx = 0;
        const request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeServerStreamingRpc] ${methodId} - Request:\n${JSON.stringify(request, null, 2)}`);
        logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] ${methodId} - Response:\n${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
            messageToFile(response, responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeServerStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeServerStreamingRpc] RPC invoke finished without error');
        });
    }

    function invokeClientStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeClientStreamingRpc] ${methodId} - Response:\n${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
                messageToFile(response, responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
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
                    logger.info(`[invokeClientStreamingRpc] ${methodId} - Request:\n${JSON.stringify(request, null, 2)}`);
                    logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
                    call.write(request);
                });
        call.end();
    }

    function invokeBidiStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        let responseIdx = 0;
        const call = method(headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeBidiStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeBidiStreamingRpc] ${methodId} - Response:\n${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
            messageToFile(response, responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
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
                    logger.info(`[invokeBidiStreamingRpc] ${methodId} - Request:\n${JSON.stringify(request, null, 2)}`);
                    logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

            // >>> SERVICE HotpotService
            let hotpotServiceStub = new protosGrpc.map_hotpot.HotpotService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD unaryPot
            invokeUnaryRpc((request, headers, callback) => hotpotServiceStub.unaryPot(request, headers, callback), root.lookupType('map_hotpot.MapPot'), root.lookupType('map_hotpot.MapPotReversed'), 'map_hotpot.HotpotService.unaryPot', map_hotpot_MapPot_fields, map_hotpot_MapPotReversed_fields);
            // METHOD serverStreamingPot
            invokeServerStreamingRpc((request, headers) => hotpotServiceStub.serverStreamingPot(request, headers), root.lookupType('map_hotpot.MapPot'), root.lookupType('map_hotpot.MapPotReversed'), 'map_hotpot.HotpotService.serverStreamingPot', map_hotpot_MapPot_fields, map_hotpot_MapPotReversed_fields);
            // METHOD clientStreamingPot
            invokeClientStreamingRpc((headers, callback) => hotpotServiceStub.clientStreamingPot(headers, callback), root.lookupType('map_hotpot.MapPot'), root.lookupType('map_hotpot.MapPotReversed'), 'map_hotpot.HotpotService.clientStreamingPot', map_hotpot_MapPot_fields, map_hotpot_MapPotReversed_fields);
            // METHOD bidiStreamingPot
            invokeBidiStreamingRpc((headers) => hotpotServiceStub.bidiStreamingPot(headers), root.lookupType('map_hotpot.MapPot'), root.lookupType('map_hotpot.MapPotReversed'), 'map_hotpot.HotpotService.bidiStreamingPot', map_hotpot_MapPot_fields, map_hotpot_MapPotReversed_fields);

            // <<< SERVICE HotpotService
        } catch (e) {
            logger.error(`[main] An error occurred: ${e.message}\n${e.stack}`);
        }

        setTimeout(() => logger.info("[main] Shutdown delay timer elapsed"), 5000);

    }

    main();

    // Add shutdown handler
    process.on('exit', (code) => {
        logger.info('Client shutting down...');
    })
})();