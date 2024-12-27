
import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
const META_KEY_h08u3Bm3X7083V = 'h08u3Bm3X7083V';
const META_VALUE_h08u3Bm3X7083V = 'j98';
const META_KEY_6B6 = '6B6';
const META_VALUE_6B6 = 'C';
const META_KEY_K = 'K';
const META_VALUE_K = '18SF11';

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
    function person_PeopleService_listPerson(call) {
        try {
            logger.info(`[person_PeopleService_listPerson] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("person.GetPersonRequest");
            let responseMessageType = root.lookupType("person.GetPersonResponse");

            logger.info(`[person_PeopleService_listPerson] Received request: ${JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "person_PeopleService_listPerson_param_0.bin");

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_h08u3Bm3X7083V, META_VALUE_h08u3Bm3X7083V);
            metadata.set(META_KEY_6B6, META_VALUE_6B6);
            metadata.set(META_KEY_K, META_VALUE_K);
            call.sendMetadata(metadata);

            loopMultipleFilesWithSamePrefix(config.testcaseDir + 'person_PeopleService_listPerson_return', '.bin')
                    .forEach((filepath) => {
                        const response = messageFromFile(filepath, responseMessageType);
                        logger.info(`[person_PeopleService_listPerson] Response: ${JSON.stringify(response, null, 2)}`);
                        call.write(response);
                    });
            call.end();

        } catch (e) {
            logger.error(`[person_PeopleService_listPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function person_PeopleService_registerPerson(call, callback) {
        try {
            logger.info(`[person_PeopleService_registerPerson] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("person.GetPersonRequest");
            let responseMessageType = root.lookupType("person.GetPersonResponse");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[person_PeopleService_registerPerson] Received request: ${JSON.stringify(request, null, 2)}`);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `person_PeopleService_registerPerson_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_h08u3Bm3X7083V, META_VALUE_h08u3Bm3X7083V);
            metadata.set(META_KEY_6B6, META_VALUE_6B6);
            metadata.set(META_KEY_K, META_VALUE_K);
            call.sendMetadata(metadata);

            call.on('end', () => {
               let retval = messageFromFile(
                   config.testcaseDir + "person_PeopleService_registerPerson_return_0.bin",
                   responseMessageType
               );
               logger.info(`[person_PeopleService_registerPerson] Response: ${JSON.stringify(retval, null, 2)}`);
               callback(null, retval);
            });

        } catch (e) {
            logger.error(`[person_PeopleService_registerPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function person_PeopleService_getPerson(call, callback) {
        try {
            logger.info(`[person_PeopleService_getPerson] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("person.GetPersonRequest");
            let responseMessageType = root.lookupType("person.GetPersonResponse");

            logger.info(`[person_PeopleService_getPerson] Received request: ${JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(call.request), requestMessageType, config.outDir + "person_PeopleService_getPerson_param_0.bin");

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_h08u3Bm3X7083V, META_VALUE_h08u3Bm3X7083V);
            metadata.set(META_KEY_6B6, META_VALUE_6B6);
            metadata.set(META_KEY_K, META_VALUE_K);
            call.sendMetadata(metadata);

            let retval = messageFromFile(
                config.testcaseDir + "person_PeopleService_getPerson_return_0.bin",
                responseMessageType
            );
            logger.info(`[person_PeopleService_getPerson] Response: ${JSON.stringify(retval, null, 2)}`);
            callback(null, retval);

        } catch (e) {
            logger.error(`[person_PeopleService_getPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function person_PeopleService_streamPerson(call) {
        try {
            logger.info(`[person_PeopleService_streamPerson] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("person.GetPersonRequest");
            let responseMessageType = root.lookupType("person.GetPersonResponse");

            let requestIdx = 0;
            call.on('data', (request) => {
                logger.info(`[person_PeopleService_streamPerson] Received request: ${JSON.stringify(request, null, 2)}`);
                messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + `person_PeopleService_streamPerson_param_${requestIdx++}.bin`);
            });

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_h08u3Bm3X7083V, META_VALUE_h08u3Bm3X7083V);
            metadata.set(META_KEY_6B6, META_VALUE_6B6);
            metadata.set(META_KEY_K, META_VALUE_K);
            call.sendMetadata(metadata);

            call.on('end', () => {
                loopMultipleFilesWithSamePrefix(config.testcaseDir + 'person_PeopleService_streamPerson_return', '.bin')
                        .forEach((filepath) => {
                            const response = messageFromFile(filepath, responseMessageType);
                            logger.info(`[person_PeopleService_streamPerson] Response: ${JSON.stringify(response, null, 2)}`);
                            call.write(response);
                        });
               call.end();
            });

        } catch (e) {
            logger.error(`[person_PeopleService_streamPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.person.PeopleService.service, {
            getPerson: person_PeopleService_getPerson,
            listPerson: person_PeopleService_listPerson,
            registerPerson: person_PeopleService_registerPerson,
            streamPerson: person_PeopleService_streamPerson        });

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
