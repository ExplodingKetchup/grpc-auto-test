
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import * as fs from 'fs';


// Load configs dynamically depending on environment
const env = process.env.NODE_ENV || 'test';
console.log("Using environment " + env);
(async () => {
    const config = (await import('./config/server/' + env + '.js')).config;
    console.log(config);

    // Load proto
    function loadProtosGrpc(dirpath) {
        const protos = {};
    
        fs.readdirSync(dirpath).forEach(file => {
            if (file.endsWith(".proto")) {
                // Suggested options for similarity to existing grpc.load behavior
                let packageDefinition = protoLoader.loadSync(
                    dirpath + file,
                    {
                        keepCase: true,
                        longs: String,
                        enums: String,      // This setting determine how enums are represented, could be String or Number
                        defaults: true,
                        oneofs: true
                    }
                );
                let protoDescriptor = grpc.loadPackageDefinition(packageDefinition);
                Object.assign(protos, protoDescriptor);
            }
        })
    
        return protos;
    }
    let protosGrpc = loadProtosGrpc(config.protoDir);

    // BEGIN RPC methods implementation
    /**
     * This RPC return the request it received.
     */
    function map_hotpot_HotpotService_unaryPot(call, callback) {
        try {
            const request = call.request;
            console.log(`[map_hotpot.HotpotService.unaryPot] Request:\n${JSON.stringify(request, null, 2)}`);

            callback(null, request);

        } catch (e) {
            console.log(`[map_hotpot_HotpotService_unaryPot] An error occurred: ${e.message}\n${e.stack}`);
        }
    }

    // END RPC methods implementation

    // BEGIN Server
    let server = new grpc.Server();

    server.addService(protosGrpc.map_hotpot.HotpotService.service, {
        unaryPot: map_hotpot_HotpotService_unaryPot        });

    server.bindAsync('0.0.0.0:' + config.server.port, grpc.ServerCredentials.createInsecure(), () => {
        console.log("Server started on " + config.server.host + ":" + config.server.port);
    });

    // END Server
})();
