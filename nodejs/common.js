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
                    enums: String,
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

        return message;

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

    return null;
}

/**
 * Encode and write protobuf.Message objects to files.
 * 
 * @param message 
 * @param messageType
 * @param filename 
 */
function messageToFile(message, messageType, filepath) {
    // Make sure the message and messageType matches
    let err = messageType.verify(message);
    if (err) {
        throw Error(err);
    }

    const buf = messageType.encode(message).finish();

    fs.writeFileSync(filepath, buf);
}

function metadataToFile(metadata, filepath) {
    // Convert metadata to a string format
    const metadataString = metadata.getMap()
        .map(([key, value]) => `${key}:${value}`)
        .join('\n');

    // Write the string to the specified file
    fs.appendFileSync(filepath, metadataString);
}

// EXPORT
export { createLogger, loadProtosGrpc, loadProtosProtobufjs, messageFromFile, messageToFile, metadataToFile }