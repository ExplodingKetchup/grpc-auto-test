let config = {};

config.protoDir = 'proto/';
config.log.dir = '../log/';
config.log.filename = 'node-client.log'
config.testcaseDir = '../test-cases/';
config.outDir = '../out/client/';
config.server.host = 'node-server';
config.server.port = 50051;

export { config };