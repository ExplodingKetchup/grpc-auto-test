import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';

const single_hotpot_RequestMessage_fields = ["small_hotpot", "float_boat"];
const single_hotpot_BigHotpotOfTerror_fields = ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"];
const single_hotpot_ResponseMessage_fields = ["big_hotpot", "flex_tape"];
const single_hotpot_SmallHotpotOfRickeridoo_fields = ["small_uint32_value", "small_string_value"];

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
                logger.info(`[invokeUnaryRpc] Method ${methodId} returns ${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, methodId + " - response", responseTypeFieldNames);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
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
        logger.info(`[invokeServerStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
        logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] Method ${methodId} returns ${JSON.stringify(response, null, 2)}`);
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
                logger.info(`[invokeClientStreamingRpc] Method ${methodId} returns ${JSON.stringify(response, null, 2)}`);
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
                    logger.info(`[invokeClientStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
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
            logger.info(`[invokeBidiStreamingRpc] Method ${methodId} returns ${JSON.stringify(response, null, 2)}`);
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
                    logger.info(`[invokeBidiStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
                    logFieldsOfObject(logger, request, methodId + " - request", requestTypeFieldNames);
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

            // >>> SERVICE HotpotService
            let hotpotServiceStub = new protosGrpc.single_hotpot.HotpotService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD unaryPot
            invokeUnaryRpc((request, headers, callback) => hotpotServiceStub.unaryPot(request, headers, callback), root.lookupType('single_hotpot.RequestMessage'), root.lookupType('single_hotpot.ResponseMessage'), 'single_hotpot.HotpotService.unaryPot', single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);
            // METHOD serverStreamingPot
            invokeServerStreamingRpc((request, headers) => hotpotServiceStub.serverStreamingPot(request, headers), root.lookupType('single_hotpot.RequestMessage'), root.lookupType('single_hotpot.ResponseMessage'), 'single_hotpot.HotpotService.serverStreamingPot', single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);
            // METHOD clientStreamingPot
            invokeClientStreamingRpc((headers, callback) => hotpotServiceStub.clientStreamingPot(headers, callback), root.lookupType('single_hotpot.RequestMessage'), root.lookupType('single_hotpot.ResponseMessage'), 'single_hotpot.HotpotService.clientStreamingPot', single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);
            // METHOD bidiStreamingPot
            invokeBidiStreamingRpc((headers) => hotpotServiceStub.bidiStreamingPot(headers), root.lookupType('single_hotpot.RequestMessage'), root.lookupType('single_hotpot.ResponseMessage'), 'single_hotpot.HotpotService.bidiStreamingPot', single_hotpot_RequestMessage_fields, single_hotpot_ResponseMessage_fields);

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