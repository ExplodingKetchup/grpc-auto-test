
import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';

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
    function map_hotpot_HotpotService_bidiStreamingPot(call) {
        try {
            logger.info(`[map_hotpot_HotpotService_bidiStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("map_hotpot.MapPot");
            let responseMessageType = root.lookupType("map_hotpot.MapPotReversed");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[map_hotpot.HotpotService.bidiStreamingPot] Request:\n${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "map_hotpot.HotpotService.bidiStreamingPot - request", ["int_double_value", "int_int_value", "int_bool_value", "int_string_value", "int_bytes_value", "int_enum_value", "bool_double_value", "bool_bool_value", "bool_string_value", "bool_bytes_value", "bool_enum_value", "string_double_value", "string_string_value", "string_bytes_value", "string_enum_value"]);
                messageToFile(request, requestMessageType, config.outDir + `map_hotpot_HotpotService_bidiStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + 'map_hotpot_HotpotService_bidiStreamingPot_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[map_hotpot.HotpotService.bidiStreamingPot] Response:\n${JSON.stringify(response, null, 2)}`);
                            logFieldsOfObject(logger, response, "map_hotpot.HotpotService.bidiStreamingPot - response", ["string_enum_value", "string_bytes_value", "string_string_value", "string_double_value", "bool_enum_value", "bool_bytes_value", "bool_string_value", "bool_bool_value", "bool_double_value", "int_enum_value", "int_bytes_value", "int_string_value", "int_bool_value", "int_int_value", "int_double_value"]);
                            call.write(response);
                        });
               call.end();
            });

        } catch (e) {
            logger.error(`[map_hotpot_HotpotService_bidiStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function map_hotpot_HotpotService_serverStreamingPot(call) {
        try {
            logger.info(`[map_hotpot_HotpotService_serverStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("map_hotpot.MapPot");
            let responseMessageType = root.lookupType("map_hotpot.MapPotReversed");

            const request = call.request;
            logger.info(`[map_hotpot.HotpotService.serverStreamingPot] Request:\n${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "map_hotpot.HotpotService.serverStreamingPot - request", ["int_double_value", "int_int_value", "int_bool_value", "int_string_value", "int_bytes_value", "int_enum_value", "bool_double_value", "bool_bool_value", "bool_string_value", "bool_bytes_value", "bool_enum_value", "string_double_value", "string_string_value", "string_bytes_value", "string_enum_value"]);
            messageToFile(request, requestMessageType, config.outDir + "map_hotpot_HotpotService_serverStreamingPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            loopMultipleFilesWithSamePrefix(config.testcaseDir + 'map_hotpot_HotpotService_serverStreamingPot_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[map_hotpot.HotpotService.serverStreamingPot] Response:\n${JSON.stringify(response, null, 2)}`);
                        logFieldsOfObject(logger, response, "map_hotpot.HotpotService.serverStreamingPot - response", ["string_enum_value", "string_bytes_value", "string_string_value", "string_double_value", "bool_enum_value", "bool_bytes_value", "bool_string_value", "bool_bool_value", "bool_double_value", "int_enum_value", "int_bytes_value", "int_string_value", "int_bool_value", "int_int_value", "int_double_value"]);
                        call.write(response);
                    });
            call.end();

        } catch (e) {
            logger.error(`[map_hotpot_HotpotService_serverStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function map_hotpot_HotpotService_clientStreamingPot(call, callback) {
        try {
            logger.info(`[map_hotpot_HotpotService_clientStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("map_hotpot.MapPot");
            let responseMessageType = root.lookupType("map_hotpot.MapPotReversed");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[map_hotpot.HotpotService.clientStreamingPot] Request:\n${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "map_hotpot.HotpotService.clientStreamingPot - request", ["int_double_value", "int_int_value", "int_bool_value", "int_string_value", "int_bytes_value", "int_enum_value", "bool_double_value", "bool_bool_value", "bool_string_value", "bool_bytes_value", "bool_enum_value", "string_double_value", "string_string_value", "string_bytes_value", "string_enum_value"]);
                messageToFile(request, requestMessageType, config.outDir + `map_hotpot_HotpotService_clientStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
               const response = messageFromFile(
                   config.testcaseDir + "map_hotpot_HotpotService_clientStreamingPot_return_0.bin",
                   responseMessageType
               );
                logger.info(`[map_hotpot.HotpotService.clientStreamingPot] Response:\n${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, "map_hotpot.HotpotService.clientStreamingPot - response", ["string_enum_value", "string_bytes_value", "string_string_value", "string_double_value", "bool_enum_value", "bool_bytes_value", "bool_string_value", "bool_bool_value", "bool_double_value", "int_enum_value", "int_bytes_value", "int_string_value", "int_bool_value", "int_int_value", "int_double_value"]);
               callback(null, response);
            });

        } catch (e) {
            logger.error(`[map_hotpot_HotpotService_clientStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function map_hotpot_HotpotService_unaryPot(call, callback) {
        try {
            logger.info(`[map_hotpot_HotpotService_unaryPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("map_hotpot.MapPot");
            let responseMessageType = root.lookupType("map_hotpot.MapPotReversed");

            const request = call.request;
            logger.info(`[map_hotpot.HotpotService.unaryPot] Request:\n${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "map_hotpot.HotpotService.unaryPot - request", ["int_double_value", "int_int_value", "int_bool_value", "int_string_value", "int_bytes_value", "int_enum_value", "bool_double_value", "bool_bool_value", "bool_string_value", "bool_bytes_value", "bool_enum_value", "string_double_value", "string_string_value", "string_bytes_value", "string_enum_value"]);
            messageToFile(request, requestMessageType, config.outDir + "map_hotpot_HotpotService_unaryPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            const response = messageFromFile(
                config.testcaseDir + "map_hotpot_HotpotService_unaryPot_return_0.bin",
                responseMessageType
            );
            logger.info(`[map_hotpot.HotpotService.unaryPot] Response:\n${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, "map_hotpot.HotpotService.unaryPot - response", ["string_enum_value", "string_bytes_value", "string_string_value", "string_double_value", "bool_enum_value", "bool_bytes_value", "bool_string_value", "bool_bool_value", "bool_double_value", "int_enum_value", "int_bytes_value", "int_string_value", "int_bool_value", "int_int_value", "int_double_value"]);
            callback(null, response);

        } catch (e) {
            logger.error(`[map_hotpot_HotpotService_unaryPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.map_hotpot.HotpotService.service, {
            unaryPot: map_hotpot_HotpotService_unaryPot,
            serverStreamingPot: map_hotpot_HotpotService_serverStreamingPot,
            clientStreamingPot: map_hotpot_HotpotService_clientStreamingPot,
            bidiStreamingPot: map_hotpot_HotpotService_bidiStreamingPot        });

        return server;
    }

    try {
        let server = getServer();

        server.bindAsync('0.0.0.0:' + config.server.port, grpc.ServerCredentials.createInsecure(), () => {
            logger.info("Server started on " + config.server.host + ":" + config.server.port);
        });
    } catch (e) {
        logger.error(`Server failed to start: ${e.message}\n${e.stack}`);
    }

    // END Server
})();
