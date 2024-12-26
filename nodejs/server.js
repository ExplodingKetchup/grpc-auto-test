import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, loopMultipleFilesWithSamePrefix, messageFromFile, messageToFile, metadataToFile } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
const META_KEY_0 = '0';
const META_VALUE_0 = 'ELIRFLWI';

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
    function person_PeopleService_getPerson(call, callback) {
        try {
            logger.info(`[person_PeopleService_getPerson] Received metadata ${JSON.stringify(call.metadata, null, 2)}`);
            metadataToFile(call.metadata, config.outDir + 'received_metadata.txt');

            let requestMessageType = root.lookupType("person.GetPersonRequest");
            let responseMessageType = root.lookupType("person.GetPersonResponse");
            logger.info(`[person_PeopleService_getPerson] Received request: ${JSON.stringify(call.request, null, 2)}`);
            messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "person_PeopleService_getPerson_param.bin");

            const metadata = new grpc.Metadata();
            metadata.set(META_KEY_0, META_VALUE_0);
            call.sendMetadata(metadata);

            let retval = messageFromFile(
                config.testcaseDir + "person_PeopleService_getPerson_return.bin",
                responseMessageType
            );
            logger.info(`[person_PeopleService_getPerson] Response: ${JSON.stringify(retval, null, 2)}`);
            callback(null, retval);
        } catch (e) {
            logger.error(`[person_PeopleService_getPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function person_PeopleService_listPerson(call) {
        let requestMessageType = root.lookupType("person.GetPersonRequest");
        let responseMessageType = root.lookupType("person.GetPersonResponse");
        logger.info(`[person_PeopleService_getPerson] Received request: ${JSON.stringify(call.request, null, 2)}`);
        messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "person_PeopleService_getPerson_param_0.bin");

        loopMultipleFilesWithSamePrefix(config.testcaseDir + 'person_PeopleService_getPerson_return', '.bin')
            .forEach(filepath => call.write(
                messageFromFile(filepath, responseMessageType)
            ));
        
        call.emit(''
        
    }

    // END RPC methods implementation

    // BEGIN Server
    function getServer() {
        let server = new grpc.Server();

        server.addService(protosGrpc.person.PeopleService.service, {
            getPerson: person_PeopleService_getPerson
        });

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
