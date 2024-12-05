import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile } from './common.js';

// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/client/' + env + '.js')).config;
    console.log(config);

    // Configuring logger
    const logger = createLogger(config.log.dir + config.log.filename);
    

    // Global variables
    let protosGrpc = loadProtosGrpc(config.protoDir);
    let root = loadProtosProtobufjs(config.protoDir);

    // Client generic rpc callback
    function genericClientRpcCallback(err, response, responseType, serviceName, methodName) {
        if (err) {
            logger.error("[genericClientRpcCallback] RPC invoke failed", err);
        } else {
            logger.info("[genericClientRpcCallback] Method " + serviceName + "." + methodName + " returns " + response);
            messageToFile(responseType.fromObject(response), responseType, config.outDir + serviceName + "_" + methodName + "_return.bin");
        }
    }

    function main() {

        try {

            // >>> SERVICE PeopleService
            let peopleServiceStub = new protosGrpc.person.PeopleService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());

            // METHOD getPerson
            let req0 = messageFromFile(
                config.testcaseDir + "PeopleService_getPerson_param.bin",
                root.lookupType("person.GetPersonRequest")
            );
            logger.info("[main] Invoke method PeopleService.getPerson, see metadata for request", req0);
            peopleServiceStub.getPerson(
                req0,
                (err, getPersonResponse) => {
                    genericClientRpcCallback(
                        err,
                        getPersonResponse, 
                        root.lookupType("person.GetPersonResponse"),
                        "PeopleService",
                        "getPerson"
                    )
                }
            )

            // <<< SERVICE PeopleService
        } catch (e) {
            logger.error("[main] An error occurred", e);
        }

    }

    main();

})();