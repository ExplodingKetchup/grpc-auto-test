
import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

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
    function repeated_hotpots_HotpotService_bidiStreamingPot(call) {
        try {
            logger.info(`[repeated_hotpots_HotpotService_bidiStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("repeated_hotpots.RequestMessage");
            let responseMessageType = root.lookupType("repeated_hotpots.ResponseMessage");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[repeated_hotpots_HotpotService_bidiStreamingPot] Received request: ${JSON.stringify(request, null, 2)}`);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `repeated_hotpots_HotpotService_bidiStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + 'repeated_hotpots_HotpotService_bidiStreamingPot_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[repeated_hotpots_HotpotService_bidiStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                            call.write(response);
                        });
               call.end();
            });

        } catch (e) {
            logger.error(`[repeated_hotpots_HotpotService_bidiStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function repeated_hotpots_HotpotService_serverStreamingPot(call) {
        try {
            logger.info(`[repeated_hotpots_HotpotService_serverStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("repeated_hotpots.RequestMessage");
            let responseMessageType = root.lookupType("repeated_hotpots.ResponseMessage");

            logger.info(`[repeated_hotpots_HotpotService_serverStreamingPot] Received request: ${JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "repeated_hotpots_HotpotService_serverStreamingPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            loopMultipleFilesWithSamePrefix(config.testcaseDir + 'repeated_hotpots_HotpotService_serverStreamingPot_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[repeated_hotpots_HotpotService_serverStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                        call.write(response);
                    });
            call.end();

        } catch (e) {
            logger.error(`[repeated_hotpots_HotpotService_serverStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function repeated_hotpots_HotpotService_clientStreamingPot(call, callback) {
        try {
            logger.info(`[repeated_hotpots_HotpotService_clientStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("repeated_hotpots.RequestMessage");
            let responseMessageType = root.lookupType("repeated_hotpots.ResponseMessage");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[repeated_hotpots_HotpotService_clientStreamingPot] Received request: ${JSON.stringify(request, null, 2)}`);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `repeated_hotpots_HotpotService_clientStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            call.on('end', () => {
               let retval = messageFromFile(
                   config.testcaseDir + "repeated_hotpots_HotpotService_clientStreamingPot_return_0.bin",
                   responseMessageType
               );
               logger.info(`[repeated_hotpots_HotpotService_clientStreamingPot] Response: ${JSON.stringify(retval, null, 2)}`);
               callback(null, retval);
            });

        } catch (e) {
            logger.error(`[repeated_hotpots_HotpotService_clientStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function repeated_hotpots_HotpotService_unaryPot(call, callback) {
        try {
            logger.info(`[repeated_hotpots_HotpotService_unaryPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("repeated_hotpots.RequestMessage");
            let responseMessageType = root.lookupType("repeated_hotpots.ResponseMessage");

            logger.info(`[repeated_hotpots_HotpotService_unaryPot] Received request: ${JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "repeated_hotpots_HotpotService_unaryPot_param_0.bin");

            const metadata = new grpc.Metadata();
            call.sendMetadata(metadata);

            let retval = messageFromFile(
                config.testcaseDir + "repeated_hotpots_HotpotService_unaryPot_return_0.bin",
                responseMessageType
            );
            logger.info(`[repeated_hotpots_HotpotService_unaryPot] Response: ${JSON.stringify(retval, null, 2)}`);
            callback(null, retval);

        } catch (e) {
            logger.error(`[repeated_hotpots_HotpotService_unaryPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.repeated_hotpots.HotpotService.service, {
            unaryPot: repeated_hotpots_HotpotService_unaryPot,
            serverStreamingPot: repeated_hotpots_HotpotService_serverStreamingPot,
            clientStreamingPot: repeated_hotpots_HotpotService_clientStreamingPot,
            bidiStreamingPot: repeated_hotpots_HotpotService_bidiStreamingPot        });

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
