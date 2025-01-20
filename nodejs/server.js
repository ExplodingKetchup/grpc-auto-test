
import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
const META_KEY_6 = '6' + BIN_SUFFIX;
const META_VALUE_6 = Buffer.from('8dd4e1cf88b396093b19', 'hex');

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
    function default_hotpot_HotpotService_bidiStreamingPot(call) {
        try {
            logger.info(`[default_hotpot_HotpotService_bidiStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");
            let responseMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[default_hotpot.HotpotService.bidiStreamingPot] Request: ${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "default_hotpot.HotpotService.bidiStreamingPot - request", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `default_hotpot_HotpotService_bidiStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_6, META_VALUE_6);
            call.sendMetadata(metadata);

            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + 'default_hotpot_HotpotService_bidiStreamingPot_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[default_hotpot.HotpotService.bidiStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                            logFieldsOfObject(logger, response, "default_hotpot.HotpotService.bidiStreamingPot - response", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
                            call.write(response);
                        });
                const trailers = new grpc.Metadata();
                trailers.set("key1", "value1");
                const rpcException = {code: grpc.status.INTERNAL, details: 'pickled bananas', metadata: trailers};
                call.emit('error', rpcException);
            });

        } catch (e) {
            logger.error(`[default_hotpot_HotpotService_bidiStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function default_hotpot_HotpotService_unaryPot(call, callback) {
        try {
            logger.info(`[default_hotpot_HotpotService_unaryPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");
            let responseMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");

            const request = call.request;
            logger.info(`[default_hotpot.HotpotService.unaryPot] Request: ${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "default_hotpot.HotpotService.unaryPot - request", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
            messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "default_hotpot_HotpotService_unaryPot_param_0.bin");

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_6, META_VALUE_6);
            call.sendMetadata(metadata);

            const trailers = new grpc.Metadata();
            trailers.set("key1", "value1");
            const rpcException = {code: grpc.status.INTERNAL, details: 'pickled bananas', metadata: trailers};
            callback(rpcException);

        } catch (e) {
            logger.error(`[default_hotpot_HotpotService_unaryPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function default_hotpot_HotpotService_serverStreamingPot(call) {
        try {
            logger.info(`[default_hotpot_HotpotService_serverStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");
            let responseMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");

            const request = call.request;
            logger.info(`[default_hotpot.HotpotService.serverStreamingPot] Request: ${JSON.stringify(request, null, 2)}`);
            logFieldsOfObject(logger, request, "default_hotpot.HotpotService.serverStreamingPot - request", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
            messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "default_hotpot_HotpotService_serverStreamingPot_param_0.bin");

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_6, META_VALUE_6);
            call.sendMetadata(metadata);

            loopMultipleFilesWithSamePrefix(config.testcaseDir + 'default_hotpot_HotpotService_serverStreamingPot_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[default_hotpot.HotpotService.serverStreamingPot] Response: ${JSON.stringify(response, null, 2)}`);
                        logFieldsOfObject(logger, response, "default_hotpot.HotpotService.serverStreamingPot - response", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
                        call.write(response);
                    });
            const trailers = new grpc.Metadata();
            trailers.set("key1", "value1");
            const rpcException = {code: grpc.status.INTERNAL, details: 'pickled bananas', metadata: trailers};
            call.emit('error', rpcException);

        } catch (e) {
            logger.error(`[default_hotpot_HotpotService_serverStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function default_hotpot_HotpotService_clientStreamingPot(call, callback) {
        try {
            logger.info(`[default_hotpot_HotpotService_clientStreamingPot] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");
            let responseMessageType = root.lookupType("default_hotpot.BigHotpotOfTerror");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[default_hotpot.HotpotService.clientStreamingPot] Request: ${JSON.stringify(request, null, 2)}`);
                logFieldsOfObject(logger, request, "default_hotpot.HotpotService.clientStreamingPot - request", ["double_value", "float_value", "int32_value", "int64_value", "uint32_value", "uint64_value", "sint32_value", "sint64_value", "fixed32_value", "fixed64_value", "sfixed32_value", "sfixed64_value", "bool_value", "string_value", "bytes_value", "enum_value", "message_value"]);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `default_hotpot_HotpotService_clientStreamingPot_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_6, META_VALUE_6);
            call.sendMetadata(metadata);

            call.on('end', () => {
                const trailers = new grpc.Metadata();
                trailers.set("key1", "value1");
                const rpcException = {code: grpc.status.INTERNAL, details: 'pickled bananas', metadata: trailers};
                callback(rpcException);
            });

        } catch (e) {
            logger.error(`[default_hotpot_HotpotService_clientStreamingPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.default_hotpot.HotpotService.service, {
            unaryPot: default_hotpot_HotpotService_unaryPot,
            serverStreamingPot: default_hotpot_HotpotService_serverStreamingPot,
            clientStreamingPot: default_hotpot_HotpotService_clientStreamingPot,
            bidiStreamingPot: default_hotpot_HotpotService_bidiStreamingPot        });

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
