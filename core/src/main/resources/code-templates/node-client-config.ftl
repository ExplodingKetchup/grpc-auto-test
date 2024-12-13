let config = {};

config.protoDir = '${config.nodejsClientProtoDir}';
config.log = {
    dir: '${config.nodejsClientLogDir}',
    filename: '${config.nodejsClientLogFile}'
};
config.testcaseDir = '${config.nodejsClientTestsDir}';
config.outDir = '${config.nodejsClientOutDir}';
config.server = {
    host : '${config.nodejsClientServerHost}',
    port : ${config.nodejsClientServerPort?c}
};

export { config };