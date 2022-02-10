package com.xboard.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GreetingLambda implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayV2WebSocketEvent input, Context context) {
        String connectionId = input.getRequestContext().getConnectionId();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(input.getBody());
            String data = jsonNode.get("data").asText();
            String secPartitionKeyVal = "simplechat_connections";
            DynamoDbClient ddb = DynamoDbClient.builder()
                    .build();
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddb)
                    .build();
            List<String> allConnectionId = new ArrayList<>();
            Iterator<XBoardTable3> results = scanTable(enhancedClient, secPartitionKeyVal);
            if(results!=null){
                while (results.hasNext()) {
                    XBoardTable3 issue = results.next();
                    allConnectionId.add(issue.getConnectionId());
                }
                for(String cId : allConnectionId){
                    System.out.println(cId);
                }
                for (String cId : allConnectionId) {
                    ApiGatewayManagementApiClient apiGatewayManagementApiClient;
                    try {
                        apiGatewayManagementApiClient = ApiGatewayManagementApiClient.builder()
                                .endpointOverride(new URI("https://" + input.getRequestContext().getDomainName() + '/' + input.getRequestContext().getStage()))
                                .build();
                        apiGatewayManagementApiClient.postToConnection(PostToConnectionRequest
                                .builder()
                                .data(SdkBytes.fromUtf8String(data))
                                .connectionId(cId)
                                .build());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                ddb.close();
            }
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setIsBase64Encoded(false);
            response.setStatusCode(200);
            response.setBody("Data Sent.");
            return response;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setIsBase64Encoded(false);
            response.setStatusCode(200);
            response.setBody("Data Not Sent.");
            return response;
        }
    }

    private Iterator<XBoardTable3> scanTable(DynamoDbEnhancedClient enhancedClient, String secPartitionKeyVal) {
        try {
            DynamoDbTable<XBoardTable3> table = enhancedClient.table("simplechat_connections", TableSchema.fromBean(XBoardTable3.class));
            AttributeValue attributeValue = AttributeValue.builder()
                    .s(secPartitionKeyVal)
                    .build();
            Map<String, AttributeValue> myMap = new HashMap<>();
            myMap.put(":val1", attributeValue);

            Map<String, String> myExMap = new HashMap<>();
            myExMap.put("#xboardpk", "xboardpk");

            Expression expression = Expression.builder()
                    .expressionValues(myMap)
                    .expressionNames(myExMap)
                    .expression("#xboardpk = :val1")
                    .build();

            ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder()
                    .filterExpression(expression)
                    .build();
            return table.scan(enhancedRequest).items().iterator();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


