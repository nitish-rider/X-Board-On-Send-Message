package com.xboard.lambda;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.NoArgsConstructor;
import lombok.ToString;


@ToString
@NoArgsConstructor
@DynamoDBTable(tableName = "simplechat_connections")
public class XBoardTable3 {
    @DynamoDBHashKey(attributeName = "connectionId")
    private String connectionId;

    @DynamoDBRangeKey(attributeName = "table")
    @DynamoDBIndexHashKey(attributeName = "table",globalSecondaryIndexName = "table-index")
    private String table;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
