import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common';

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
import { config } from ('./config/server/' + env);

// Configuring logger
const logger = createLogger(config.log.dir + config.log.filename);

// Global variables
let protosGrpc = loadProtosGrpc(config.protoDir);
let root = loadProtosProtobufjs(config.protoDir);

// BEGIN RPC methods implementation
function getPerson(call, callback) {
    try {
        callback(null, getPersonImpl(call.request));
    } catch (e) {
        logger.error("[getPerson] An error occurred", e);
    }
}

function getPersonImpl(request) {
    logger.info("[getPersonImpl] Received request", request);

    let messageType = root.lookupType("person.GetPersonResponse");
    messageToFile(messageType.fromObject(request), messageType, config.outDir + "PeopleService_getPerson_param.bin");
    return messageFromFile(
        config.testcaseDir + "PeopleService_getPerson_return.bin", 
        messageType
    );
}

// END RPC methods implementation

// BEGIN Server
function getServer() {
    let server = new grpc.Server();
    server.addService(protosGrpc.person.PeopleService.service, {
        getPerson: getPerson
    });
    return server;
}
  
let server = getServer();
  
server.bindAsync(config.server.host + ':' + config.server.port, grpc.ServerCredentials.createInsecure(), () => {
    server.start();
});

// END Server

// EXPORT for testing
export { loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile }