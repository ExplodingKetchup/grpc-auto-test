log:
  dir: ${config.pyClientLogDir}
  file_prefix: ${config.pyClientLogFile}
in:
  dir: ${config.pyClientTestsDir}
out:
  dir: ${config.pyClientOutDir}
server:
  host: ${config.pyClientServerHost}
  port: ${config.pyClientServerPort?c}