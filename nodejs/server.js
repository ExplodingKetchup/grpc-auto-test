
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
    function single_hotpot_HotpotService_serverStreamingPot(call) {
        try {
            logger.info(`[single_hotpot_HotpotService_serverStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("single_hotpot.RequestMessage");
            let responseMessageType = root.lookupType("single_hotpot.ResponseMessage");

            const request = call.request;
            logger.info(`[single_hotpot.HotpotService.serverStreamingPot] Received request: ${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "single_hotpot.HotpotService.serverStreamingPot - request", ["small_hotpot", "float_boat"]);
            messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "single_hotpot_HotpotService_serverStreamingPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            loopMultipleFilesWithSamePrefix(config.testcaseDir + 'single_hotpot_HotpotService_serverStreamingPot_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[single_hotpot.HotpotService.serverStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                        logFieldsOfObject(logger, response, "single_hotpot.HotpotService.serverStreamingPot - response", ["big_hotpot", "flex_tape"]);
                        call.write(response);
                    });
            call.end();

        } catch (e) {
            logger.error(`[single_hotpot_HotpotService_serverStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function single_hotpot_HotpotService_clientStreamingPot(call, callback) {
        try {
            logger.info(`[single_hotpot_HotpotService_clientStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("single_hotpot.RequestMessage");
            let responseMessageType = root.lookupType("single_hotpot.ResponseMessage");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[single_hotpot.HotpotService.clientStreamingPot] Received request: ${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "single_hotpot.HotpotService.clientStreamingPot - request", ["small_hotpot", "float_boat"]);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `single_hotpot_HotpotService_clientStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
               const response = messageFromFile(
                   config.testcaseDir + "single_hotpot_HotpotService_clientStreamingPot_return_0.bin",
                   responseMessageType
               );
                logger.info(`[single_hotpot.HotpotService.clientStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                logFieldsOfObject(logger, response, "single_hotpot.HotpotService.clientStreamingPot - response", ["big_hotpot", "flex_tape"]);
               callback(null, response);
            });

        } catch (e) {
            logger.error(`[single_hotpot_HotpotService_clientStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function single_hotpot_HotpotService_bidiStreamingPot(call) {
        try {
            logger.info(`[single_hotpot_HotpotService_bidiStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("single_hotpot.RequestMessage");
            let responseMessageType = root.lookupType("single_hotpot.ResponseMessage");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[single_hotpot.HotpotService.bidiStreamingPot] Received request: ${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "single_hotpot.HotpotService.bidiStreamingPot - request", ["small_hotpot", "float_boat"]);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `single_hotpot_HotpotService_bidiStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + 'single_hotpot_HotpotService_bidiStreamingPot_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[single_hotpot.HotpotService.bidiStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                            logFieldsOfObject(logger, response, "single_hotpot.HotpotService.bidiStreamingPot - response", ["big_hotpot", "flex_tape"]);
                            call.write(response);
                        });
               call.end();
            });

        } catch (e) {
            logger.error(`[single_hotpot_HotpotService_bidiStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function single_hotpot_HotpotService_unaryPot(call, callback) {
        try {
            logger.info(`[single_hotpot_HotpotService_unaryPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("single_hotpot.RequestMessage");
            let responseMessageType = root.lookupType("single_hotpot.ResponseMessage");

            const request = call.request;
            logger.info(`[single_hotpot.HotpotService.unaryPot] Received request: ${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "single_hotpot.HotpotService.unaryPot - request", ["small_hotpot", "float_boat"]);
            messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "single_hotpot_HotpotService_unaryPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            const response = messageFromFile(
                config.testcaseDir + "single_hotpot_HotpotService_unaryPot_return_0.bin",
                responseMessageType
            );
            logger.info(`[single_hotpot.HotpotService.unaryPot] Response: ${JSON.stringify(response, null, 2)}`);
            logFieldsOfObject(logger, response, "single_hotpot.HotpotService.unaryPot - response", ["big_hotpot", "flex_tape"]);
            callback(null, response);

        } catch (e) {
            logger.error(`[single_hotpot_HotpotService_unaryPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.single_hotpot.HotpotService.service, {
            unaryPot: single_hotpot_HotpotService_unaryPot,
            serverStreamingPot: single_hotpot_HotpotService_serverStreamingPot,
            clientStreamingPot: single_hotpot_HotpotService_clientStreamingPot,
            bidiStreamingPot: single_hotpot_HotpotService_bidiStreamingPot        });

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
