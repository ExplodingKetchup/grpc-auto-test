let config = {};

config.protoDir = 'proto/';
config.log = {
    dir: '../log/',
    filename: 'node-client.log'
};
config.testcaseDir = '../test-cases/client/';
config.outDir = '../out/client/';
config.server = {
    host : 'localhost',
    port : 50051
};

export { config };