import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject } from './common.js';

// Constants
const SUPPORTED_COMPRESSION_ALGO = ["", "deflate", "gzip", "gzip-stream"];
const BIN_SUFFIX = '-bin';

const map_hotpot_MapPot_fields = ["int_double_value", "int_int_value", "int_bool_value", "int_string_value"];
const map_hotpot_MapPot_IntDoubleValueEntry_fields = ["key", "value"];
const map_hotpot_MapPot_IntStringValueEntry_fields = ["key", "value"];
const map_hotpot_MapPot_IntBoolValueEntry_fields = ["key", "value"];
const map_hotpot_MapPot_IntIntValueEntry_fields = ["key", "value"];

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

    function invokeUnaryRpc(method, requestType, responseType, , requestTypeFieldNames, responseTypeFieldNames) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            } else {
                logger.info(`[invokeUnaryRpc] - Response:\n${JSON.stringify(response, null, 2)}`);
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] ${methodId} - Request:\n${JSON.stringify(request, null, 2)}`);
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function main() {

        // >>> SERVICE HotpotService
        let hotpotServiceStub = new protosGrpc.map_hotpot.HotpotService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
        logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

        // METHOD unaryPot
        invokeUnaryRpc((request, headers, callback) => hotpotServiceStub.unaryPot(request, headers, callback), root.lookupType('map_hotpot.MapPot'), root.lookupType('map_hotpot.MapPot'), 'map_hotpot.HotpotService.unaryPot', map_hotpot_MapPot_fields, map_hotpot_MapPot_fields);

        // <<< SERVICE HotpotService

        setTimeout(() => logger.info("[main] Shutdown delay timer elapsed"), 5000);

    }

    main();
})();