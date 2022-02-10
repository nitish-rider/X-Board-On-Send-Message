package com.xboard.lambda;


import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;


@ToString
@NoArgsConstructor
@DynamoDbBean
public class XBoardTable3 {
    private String connectionId;
    private String xboardpk;

    @DynamoDbPartitionKey
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"xboardpk-index"})
    public String getxboardpk() {
        return xboardpk;
    }

    public void setxboardpk(String xboardpk) {
        this.xboardpk = xboardpk;
    }


}
