import { Long } from "@grpc/proto-loader";

const obj = {};

const fieldNames = ["sth"];

fieldNames.forEach((fieldname) => {
    let typeDisplayString = "";
    if (obj[fieldname] === null) {
        typeDisplayString = "null";
    } else if (typeof obj[fieldname] === "object") {
        typeDisplayString = obj[fieldname].constructor.name;
    } else {
        typeDisplayString = typeof obj[fieldname];
    }
    console.log(`[logFieldsOfObject]: ${fieldname} (${typeDisplayString}) = ${obj[fieldname]}`);
});

const arr = [];
console.log(arr.constructor.name)
