import { Long } from "@grpc/proto-loader";

const a = {};
const key = '³Ð(,¯%»';
// Initialize the 64-bit integer as a BigInt
let result = 0n;

// Iterate over each character and construct the 64-bit integer
let isNegative = key.charCodeAt(7) > 127;
for (let i = 0; i < 8; i++) {
  const byteValue = BigInt(key.charCodeAt(i));
  result = result | (byteValue << (8n * BigInt(i)));
}
if (isNegative) {
    result = result - (2n ** 64n);
}
console.log(result);

a[result] = 124;
console.log(a);