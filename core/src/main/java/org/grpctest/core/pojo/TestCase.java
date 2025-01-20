package org.grpctest.core.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.DynamicMessage;
import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.grpctest.core.enums.MetadataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    // Name of test case (Unique)
    private String name;

    // Identifies which method used to run test case (Use to map service and method field)
    private String methodId;

    // Method param (put in client code)
    private List<Object> paramValue;
    @JsonIgnore
    @Builder.Default
    private List<String> paramValueJson = new ArrayList<>();
    @JsonIgnore
    @Builder.Default
    private List<DynamicMessage> paramValueDynMsg = new ArrayList<>();

    // Method return (put in server code)
    private List<Object> returnValue;
    @JsonIgnore
    @Builder.Default
    private List<String> returnValueJson = new ArrayList<>();
    @JsonIgnore
    @Builder.Default
    private List<DynamicMessage> returnValueDynMsg = new ArrayList<>();

    private RpcException exception;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RpcException {
        private Status.Code statusCode;
        private String description;
        private Map<String, Pair<MetadataType, String>> trailingMetadata;
        private boolean isRuntimeException;

        @JsonCreator
        public RpcException(@JsonProperty("statusCode") String statusCode,
                            @JsonProperty("description") String description,
                            @JsonProperty("trailers") Map<String, String> trailers,
                            @JsonProperty("runtimeException") Boolean isRuntimeException) {
            this.statusCode = Status.Code.valueOf(statusCode);
            this.description = description;
            this.trailingMetadata = new HashMap<>();
            for (Map.Entry<String, String> trailerEntry : trailers.entrySet()) {
                MetadataType metadataType = MetadataType.STRING;
                String trailerValue = trailerEntry.getValue();
                if (trailerEntry.getValue().startsWith("BIN")) {
                    metadataType = MetadataType.BIN;
                    trailerValue = trailerValue.substring(3);
                } else if (trailerEntry.getValue().startsWith("STR")) {
                    trailerValue = trailerValue.substring(3);
                }
                trailingMetadata.put(trailerEntry.getKey(), Pair.of(metadataType, trailerValue));
            }
            this.isRuntimeException = isRuntimeException;
        }
    }

}
