log:
  dir: ${config.pyServerLogDir}
  file_prefix: ${config.pyServerLogFile}
in:
  dir: ${config.pyServerTestsDir}
out:
  dir: ${config.pyServerOutDir}
server:
  port: ${config.pyServerServerPort?c}