import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import * as fs from 'fs';
import protobuf from 'protobufjs';
import * as winston from 'winston';
import DailyRotateFile from 'winston-daily-rotate-file';

// Common utilities

function createLogger(logFolder, logFilePrefix) {
    // Configure the transport for daily rotation
    const transport = new DailyRotateFile({
        filename: logFolder + logFilePrefix + '.%DATE%.log',    // Log file name with date placeholder
        datePattern: 'YYYY-MM-DD',                              // Rotation frequency based on date
        zippedArchive: true,                                    // Compress old log files
        maxSize: '20m',                                         // Maximum file size before rotation
        maxFiles: '14d',                                        // Keep logs for 14 days
    });
    return winston.createLogger({
        level: 'info',
        format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.printf(({ timestamp, level, message }) => {
                return `${timestamp} [${level}]: ${message}`;
            })
        ),
        transports: [
          transport,
          new winston.transports.Console({ format: winston.format.simple() })
        ],
    });
}

/**
 * Load all protos from the specified dirpath. Return a protos object. 
 * To get declared services and messages, get protos.namespace.message same structure as defined
 * in the proto files.
 * 
 * Note: This function uses gRPC method of parsing .proto files.
 * 
 * @param dirpath 
 * @returns 
 */
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

/**
 * Same as with {@link loadProtosGrpc} but uses the Protobufjs method to load .proto files instead.
 * 
 * @returns protobufjs.Root root such that root.namespace.message contains Message Types.
 */
function loadProtosProtobufjs(dirpath) {
    const root = new protobuf.Root();

    fs.readdirSync(dirpath).forEach(file => {
        if (file.endsWith(".proto")) {
            root.loadSync(dirpath + file, { keepCase: true });
        }
    })

    root.resolveAll();

    return root;
}

function convertMapKeysInObject(obj, messageType) {
    for (const field in obj) {
        if (messageType.fields[field] === undefined) continue;
        if (messageType.fields[field].map) {
            const mapFieldEntry = {};
            for (const key in obj[field]) {
                switch (messageType.fields[field].keyType) {    // Afaik, there are 3 keytypes: string, bool, or integer types
                    case 'string':
                        mapFieldEntry[key] = obj[field][key];
                        break;
                    case 'bool':
                        if (key === 'true') {
                            mapFieldEntry['true'] = obj[field][key];
                        } else {
                            mapFieldEntry[''] = obj[field][key];
                        }
                        break;
                    default:
                        if (messageType.fields[field].keyType.endsWith('32')) {
                            mapFieldEntry[key] = obj[field][key];
                        } else if (messageType.fields[field].keyType.endsWith('64')) {
                            // The key is a 8-character ascii string, encoded with little endian (MSB at key[7])
                            // Convert key to a bigint, and convert bigint to its string form as attribute
                            let result = 0n;
                            let isNegative = key.charCodeAt(7) > 127;
                            for (let i = 0; i < 8; i++) {
                            const byteValue = BigInt(key.charCodeAt(i));
                            result = result | (byteValue << (8n * BigInt(i)));
                            }
                            if (['int64', 'sint64', 'sfixed64'].includes(messageType.fields[field].keyType) && isNegative) {
                                result = result - (2n ** 64n);
                            }
                            mapFieldEntry[result] = obj[field][key];
                        }
                }
            }
            obj[field] = mapFieldEntry;
        }
    }
    return obj;
}

/** 
 * Read and convert message from files (encoded using protobuf) 
 * 
 * @returns protobuf.Message instance of the decoded message if decode success. null otherwise.
*/
function messageFromFile(filepath, messageType) {
    if (!fs.existsSync(filepath)) {
        throw new Error("[messageFromFile] Path [" + filepath + "] does not exist");
    }

    const data = fs.readFileSync(filepath);

    try {
        let message = messageType.decode(data);

        return messageType.toObject(message);
        // return convertMapKeysInObject(messageType.toObject(message), messageType);

    } catch (e) {
        if (e instanceof protobuf.util.ProtocolError) {
            // e.instance holds the so far decoded message with missing required fields
            throw new Error("[messageFromFile] Decode message of type [" + messageType + "]" +
                " from [" + filepath + "] failed: Missing required fields");
          } else {
            // wire format is invalid
            throw new Error("[messageFromFile] Decode message of type [" + messageType + "]" +
                " from [" + filepath + "] failed: Invalid wire format");
          }
    }
}

/**
 * Encode and write protobuf.Message objects to files.
 * 
 * @param message 
 * @param messageType
 * @param filename 
 */
function messageToFile(obj, messageType, filepath) {
    const message = messageType.fromObject(obj);
    // const message = messageType.fromObject(convertMapKeysInObject(obj, messageType));
    // Make sure the message and messageType matches
    let err = messageType.verify(message);
    // if (err) {
    //     throw Error(err);
    // }

    const buf = messageType.encode(message).finish();

    fs.writeFileSync(filepath, buf);
}

function formatMetadataForOutput(metadata) {
    const metadataMap = metadata.getMap();
    return Object.entries(metadataMap)
        .map(([key, value]) => {
            if (key.endsWith('-bin')) {
                return `${key}:${Buffer.from(value).toString('hex')}`
            } else {
                return `${key}:${value}`;
            }
        })
        .join('\n') + '\n';
}

function metadataToFile(metadata, filepath) {
    if (metadata === null) return;
    // Write the string to the specified file
    fs.appendFileSync(filepath, formatMetadataForOutput(metadata));
}

function formatErrorForOutput(err) {
    const statusName = Object.keys(grpc.status).find(
        (key) => grpc.status[key] === err.code
    );
    return `${statusName}\n${err.details}\n${formatMetadataForOutput(err.metadata)}`;
}

function errorToFile(err, filepath) {
    // Convert err to a string
    fs.appendFileSync(filepath, formatErrorForOutput(err));
} 

/**
 * Loop over all files with the specified path: (prefix)_(i)(suffix), where i is from 0 to n where n+1 
 * points to a nonexistent file. Returns the list of valid filepaths found.
 * 
 * @param {*} prefix 
 * @param {*} suffix 
 * 
 * @returns Array of valid filepaths
 */
function loopMultipleFilesWithSamePrefix(prefix, suffix) {
    let i = 0;
    let filepath = `${prefix}_${i}${suffix}`;
    const validFilepaths = new Array();
    while (fs.existsSync(filepath)) {
        validFilepaths.push(filepath);
        i++;
        filepath = `${prefix}_${i}${suffix}`;
    }
    return validFilepaths;
}

function logFieldsOfObject(logger, obj, objName, fieldNames) {
    fieldNames.forEach((fieldname) => {
        let typeDisplayString = "";
        let valueDisplayString = obj[fieldname];
        if (obj[fieldname] === null) {
            typeDisplayString = "null";
        } else if (typeof obj[fieldname] === "object") {
            typeDisplayString = obj[fieldname].constructor.name;
            valueDisplayString = JSON.stringify(obj[fieldname], null, 2);
        } else {
            typeDisplayString = typeof obj[fieldname];
        }
        let fieldPresence = obj.hasOwnProperty(fieldname);
        logger.info(`[logFieldsOfObject] ${objName}: ${fieldname} (${typeDisplayString}; fieldPresence = ${fieldPresence}) = ${valueDisplayString}`);
    });
}

// EXPORT
export { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, formatMetadataForOutput, metadataToFile, formatErrorForOutput, errorToFile, loopMultipleFilesWithSamePrefix, logFieldsOfObject }