import * as nodeServer from './server';

const TESTCASE_PARAM_PATH = '../../test-cases/PeopleService_getPerson_param.bin';
const TESTCASE_RETURN_PATH = '../../test-cases/PeopleService_getPerson_return.bin';
const PROTO_DIRPATH = '../common/proto/'

let root = loadProtosProtobufjs(PROTO_DIRPATH);
const sampleResponse = 
{ 
    "person": 
    { 
        "id": 1,
        "name": "John",
        "age": 20,
        "occupation": "student",
        "gender": "MALE",
        "emails": [
            "john.doe@example.com"
        ]
    }
}

test('messageFromFile test', () => {
    expect(nodeServer.messageFromFile(TESTCASE_RETURN_PATH, root.lookupType('person.GetPersonResponse'))).toEqual(sampleResponse);
})