package org.grpctest.core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.DynamicMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {

    // Name of test case (Unique)
    private String name;

    // Identifies which method used to run test case (Use to map service and method field)
    private String methodId;

    // Method param (put in client code)
    private Object paramValue;
    @JsonIgnore
    private String paramValueJson;
    @JsonIgnore
    private DynamicMessage paramValueDynMsg;

    // Method return (put in server code)
    private Object returnValue;
    @JsonIgnore
    private String returnValueJson;
    @JsonIgnore
    private DynamicMessage returnValueDynMsg;
}
