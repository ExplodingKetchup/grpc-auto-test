let config = {};

config.protoDir = 'proto/';
config.log = {
    dir: 'log/',
    filename: 'node-client.log'
};
config.testcaseDir = 'test-cases/';
config.outDir = 'out/client/';
config.server = {
    host : 'py-server',
    port : 50051
};

export { config };