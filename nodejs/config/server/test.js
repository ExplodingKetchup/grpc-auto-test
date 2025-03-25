let config = {};

config.protoDir = 'proto/';
config.log = {
    dir: '../log/',
    filename: 'node-server.log'
};
config.testcaseDir = '../test-cases/server/';
config.outDir = '../out/server/';
config.server = {
    host : 'node-server',
    port : 50051
};

export { config };