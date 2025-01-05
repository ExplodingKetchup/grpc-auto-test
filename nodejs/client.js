import * as grpc from '@grpc/grpc-js';
import { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix } from './common.js';

// Constants
const BIN_SUFFIX = '-bin';
const META_KEY_1o9bb5z = '1o9bb5z' + BIN_SUFFIX;
const META_VALUE_1o9bb5z = Buffer.from('92e44137aba862a47c0840ba323255', 'hex');

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
    headers.set(META_KEY_1o9bb5z, META_VALUE_1o9bb5z);

    function invokeUnaryRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeUnaryRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeUnaryRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        let request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeUnaryRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
        const call = method(request, headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeUnaryRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
    }

    function invokeServerStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const request = messageFromFile(config.testcaseDir + methodId.replaceAll(".", "_") + '_param_0.bin', requestType);
        logger.info(`[invokeServerStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
        const call = method(request, headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeServerStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeServerStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeServerStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeServerStreamingRpc] RPC invoke finished without error');
        });
    }

    function invokeClientStreamingRpc(method, requestType, responseType, methodId) {
        const rpcCallback = (err, response) => {
            if (err) {
                logger.error(`[invokeClientStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
                errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
            } else {
                logger.info(`[invokeClientStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
                messageToFile(responseType.fromObject(response), responseType, config.outDir + methodId.replaceAll(".", "_") + "_return_0.bin");
            }
        }

        const call = method(headers, rpcCallback);
        call.on('metadata', metadata => {
            logger.info(`[invokeClientStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        loopMultipleFilesWithSamePrefix(`${config.testcaseDir}${methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    logger.info(`[invokeClientStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
                    call.write(request);
                });
        call.end();
    }

    function invokeBidiStreamingRpc(method, requestType, responseType, methodId) {
        let responseIdx = 0;
        const call = method(headers);
        call.on('metadata', (metadata) => {
            logger.info(`[invokeBidiStreamingRpc] Received metadata ${JSON.stringify(metadata, null, 2)}`);
            metadataToFile(metadata, config.outDir + 'received_metadata.txt');
        });
        call.on('data', (response) => {
            logger.info(`[invokeBidiStreamingRpc] Method ${methodId} returns ${JSON.stringify(responseType.fromObject(response), null, 2)}`);
            messageToFile(responseType.fromObject(response), responseType, `${config.outDir}${methodId.replaceAll('.', '_')}_return_${responseIdx++}.bin`);
        });
        call.on('error', (err) => {
            logger.error(`[invokeBidiStreamingRpc] RPC invoke failed: ${err.message}\n${err.stack}`);
            errorToFile(err, `${config.outDir}${methodId.replaceAll(".", "_")}_error.txt`);
        });
        call.on('end', () => {
            logger.info('[invokeBidiStreamingRpc] RPC invoke finished without error');
        });
        loopMultipleFilesWithSamePrefix(`${config.testcaseDir}${methodId.replaceAll('.', '_')}_param`, '.bin')
                .forEach((filepath) => {
                    const request = messageFromFile(filepath, requestType);
                    logger.info(`[invokeBidiStreamingRpc] Invoke ${methodId}, param: ${JSON.stringify(request, null, 2)}`);
                    call.write(request);
                });
        call.end();
    }

    function main() {

        try {

            // >>> SERVICE PeopleService
            let peopleServiceStub = new protosGrpc.person.PeopleService(config.server.host + ':' + config.server.port, grpc.credentials.createInsecure());
            logger.info(`[main] Connected to server at ${config.server.host}:${config.server.port}`);

            // METHOD getPerson
            invokeUnaryRpc((request, headers, callback) => peopleServiceStub.getPerson(request, headers, callback), root.lookupType('person.GetPersonRequest'), root.lookupType('person.GetPersonResponse'), 'person.PeopleService.getPerson');
            // METHOD listPerson
            invokeServerStreamingRpc((request, headers) => peopleServiceStub.listPerson(request, headers), root.lookupType('person.GetPersonRequest'), root.lookupType('person.GetPersonResponse'), 'person.PeopleService.listPerson');
            // METHOD registerPerson
            invokeClientStreamingRpc((headers, callback) => peopleServiceStub.registerPerson(headers, callback), root.lookupType('person.GetPersonRequest'), root.lookupType('person.GetPersonResponse'), 'person.PeopleService.registerPerson');
            // METHOD streamPerson
            invokeBidiStreamingRpc((headers) => peopleServiceStub.streamPerson(headers), root.lookupType('person.GetPersonRequest'), root.lookupType('person.GetPersonResponse'), 'person.PeopleService.streamPerson');

            // <<< SERVICE PeopleService
        } catch (e) {
            logger.error(`[main] An error occurred: ${e.message}\n${e.stack}`);
        }

    }

    main();

    // Add shutdown handler
    process.on('exit', (code) => {
        logger.info('Client shutting down...');
    })
})();