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
    function genericClientRpcCallback(err, response, responseType, methodId) {
        if (err) {
            logger.error(`[genericClientRpcCallback] RPC invoke failed: ${err.message}\n${err.stack}`);
        } else {
            logger.info(`[genericClientRpcCallback] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return.bin");
        }
    }

    function main() {

        try {

            // >>> SERVICE PeopleService
            let peopleServiceStub = new protosGrpc.person.PeopleService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD getPerson
            let param_person_PeopleService_getPerson = messageFromFile(
                config.testcaseDir + "person_PeopleService_getPerson_param.bin",
                root.lookupType("person.GetPersonRequest")
            );
            logger.info(`[main] Invoke person.PeopleService.getPerson, param: ${JSON.stringify(param_person_PeopleService_getPerson, null, 2)}`);
            peopleServiceStub.getPerson(
                param_person_PeopleService_getPerson,
                (err, response) => {
                    genericClientRpcCallback(
                        err,
                        response,
                        root.lookupType("person.GetPersonResponse"),
                        "person.PeopleService.getPerson"
                    )
                }
            )

            // <<< SERVICE PeopleService
        } catch (e) {
            logger.error(`[main] An error occurred: ${e.message}\n${e.stack}`);
        }

    }

    main();

})();