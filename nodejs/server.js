import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/server/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir + config.log.filename);

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // BEGIN RPC methods implementation
    function person_PeopleService_getPerson(call, callback) {
        try {
            callback(null, person_PeopleService_getPerson_impl(call.request));
        } catch (e) {
            logger.error(`[person_PeopleService_getPerson] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    function person_PeopleService_getPerson_impl(request) {
        logger.info(`[person_PeopleService_getPerson_impl] Received request: ${JSON.stringify(request, null, 2)}`);

        let requestMessageType = root.lookupType("person.GetPersonRequest");
        let responseMessageType = root.lookupType("person.GetPersonResponse");
        messageToFile(requestMessageType.fromObject(request), requestMessageType, config.outDir + "person_PeopleService_getPerson_param.bin");
        let retval = messageFromFile(
            config.testcaseDir + "person_PeopleService_getPerson_return.bin",
            responseMessageType
        );
        logger.info(`[person_PeopleService_getPerson_impl] Response: ${JSON.stringify(retval, null, 2)}`)
        return retval;
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
