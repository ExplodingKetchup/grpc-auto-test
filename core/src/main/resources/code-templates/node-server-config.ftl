let config = {};

config.protoDir = '${config.nodejsServerProtoDir}';
config.log = {
    dir: '${config.nodejsServerLogDir}',
    filename: '${config.nodejsServerLogFile}'
};
config.testcaseDir = '${config.nodejsServerTestsDir}';
config.outDir = '${config.nodejsServerOutDir}';
config.server = {
    host : '${config.nodejsServerServerHost}',
    port : ${config.nodejsServerServerPort?c}
};

export { config };