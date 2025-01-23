import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
const META_KEY_6ak680h0y = '6ak680h0y' + BIN_SUFFIX;
const META_VALUE_6ak680h0y = Buffer.from('2639dd09d4c63390d40bf5297920f2fad2', 'hex');
const META_KEY_30ju7j578986s9 = '30ju7j578986s9' + BIN_SUFFIX;
const META_VALUE_30ju7j578986s9 = Buffer.from('e2b2', 'hex');
const META_KEY_3w0981nx15wu021d = '3w0981nx15wu021d' + BIN_SUFFIX;
const META_VALUE_3w0981nx15wu021d = Buffer.from('eb9873a5d7ce4c67', 'hex');

const default_hotpot_SmallHotpotOfRickeridoo_fields = ["small_uint32_value", "small_string_value"];
const default_hotpot_BigHotpotOfTerror_fields = ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"];

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
    headers.set(META_KEY_6ak680h0y, META_VALUE_6ak680h0y);
    headers.set(META_KEY_30ju7j578986s9, META_VALUE_30ju7j578986s9);
    headers.set(META_KEY_3w0981nx15wu021d, META_VALUE_3w0981nx15wu021d);

    function invokeUnaryRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeUnaryRpc] ${methodId} - Response: ${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] ${methodId} - Request: ${JSON.stringify(request, null, 2)}`);
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
        logger.info(`[invokeServerStreamingRpc] ${methodId} - Request: ${JSON.stringify(request, null, 2)}`);
        logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] ${methodId} - Response: ${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
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

    function invokeClientStreamingRpc(method, requestType, responseType, methodId, requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeClientStreamingRpc] ${methodId} - Response: ${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
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
                    logger.info(`[invokeClientStreamingRpc] ${methodId} - Request: ${JSON.stringify(request, null, 2)}`);
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
            logger.info(`[invokeBidiStreamingRpc] ${methodId} - Response: ${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
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
                    logger.info(`[invokeBidiStreamingRpc] ${methodId} - Request: ${JSON.stringify(request, null, 2)}`);
                    logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

            // >>> SERVICE HotpotService
            let hotpotServiceStub = new protosGrpc.default_hotpot.HotpotService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD unaryPot
            invokeUnaryRpc((request, headers, callback) => hotpotServiceStub.unaryPot(request, headers, callback), root.lookupType('default_hotpot.BigHotpotOfTerror'), root.lookupType('default_hotpot.BigHotpotOfTerror'), 'default_hotpot.HotpotService.unaryPot', default_hotpot_BigHotpotOfTerror_fields, default_hotpot_BigHotpotOfTerror_fields);
            // METHOD serverStreamingPot
            invokeServerStreamingRpc((request, headers) => hotpotServiceStub.serverStreamingPot(request, headers), root.lookupType('default_hotpot.BigHotpotOfTerror'), root.lookupType('default_hotpot.BigHotpotOfTerror'), 'default_hotpot.HotpotService.serverStreamingPot', default_hotpot_BigHotpotOfTerror_fields, default_hotpot_BigHotpotOfTerror_fields);
            // METHOD clientStreamingPot
            invokeClientStreamingRpc((headers, callback) => hotpotServiceStub.clientStreamingPot(headers, callback), root.lookupType('default_hotpot.BigHotpotOfTerror'), root.lookupType('default_hotpot.BigHotpotOfTerror'), 'default_hotpot.HotpotService.clientStreamingPot', default_hotpot_BigHotpotOfTerror_fields, default_hotpot_BigHotpotOfTerror_fields);
            // METHOD bidiStreamingPot
            invokeBidiStreamingRpc((headers) => hotpotServiceStub.bidiStreamingPot(headers), root.lookupType('default_hotpot.BigHotpotOfTerror'), root.lookupType('default_hotpot.BigHotpotOfTerror'), 'default_hotpot.HotpotService.bidiStreamingPot', default_hotpot_BigHotpotOfTerror_fields, default_hotpot_BigHotpotOfTerror_fields);

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