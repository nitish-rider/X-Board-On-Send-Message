package com.xboard.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GreetingLambda implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayV2WebSocketEvent input, Context context) {
        String connectionId = input.getRequestContext().getConnectionId();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(input.getBody());
            String data = jsonNode.get("data").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String tableName = "simplechat_connections";
        String secParttitionIdx = "xboardpk-index";
        String secPartitionKeyVal = "simplechat_connections";

        DynamoDbClient ddb = DynamoDbClient.builder()
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

//        queryTable(enhancedClient, tableName, secPartitionKeyVal);
        scanTable(enhancedClient,tableName,secPartitionKeyVal,secParttitionIdx);
//        System.out.println(result);
        ddb.close();

//        QueryResponse queryResponse=queryTable(ddb,tableName,partitionKeyName,partitionKeyVal);
//        if(queryResponse!=null) {
//            List<Map<String, AttributeValue>> items = queryResponse.items();
//
//
//            for (Map<String, AttributeValue> mp : items) {
//                for (Map.Entry<String, AttributeValue> entry : mp.entrySet()) {
//                    System.out.println(entry.getKey() + ":" + entry.getValue());
//                }
//            }
//        }
//        else{
//            System.out.println("entry.getKey() + : + entry.getValue()");
//        }


        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        response.setBody("Data Sent.");
        return response;
    }

    private void scanTable(DynamoDbEnhancedClient enhancedClient, String tableName, String secPartitionKeyVal, String secParttitionIdx) {
        try{
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
            Iterator<XBoardTable3> results = table.scan(enhancedRequest).items().iterator();

            while (results.hasNext()) {
                XBoardTable3 issue = results.next();
                System.out.println("The record description is " + issue.getConnectionId());
                System.out.println("The record title is " + issue.getxboardpk());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void queryTable(DynamoDbEnhancedClient enhancedClient, String tableName, String secPartitionKeyVal) {
//        try {
//            DynamoDbIndex<XBoardTable3> secIndex =
//                    enhancedClient.table("simplechat_connections",
//                                    TableSchema.fromBean(XBoardTable3.class))
//                            .index("xboardpk-index");
//
//            AttributeValue attributeValue = AttributeValue.builder()
//                    .s(secPartitionKeyVal)
//                    .build();
//
//            QueryConditional queryConditional = QueryConditional
//                    .keyEqualTo(Key.builder().partitionValue(attributeValue).build());
//
//            SdkIterable<Page<XBoardTable3>> results = secIndex.query(
//                    QueryEnhancedRequest.builder()
//                            .queryConditional(queryConditional)
//                            .build()
//            );
//
//            AtomicInteger atomicInteger = new AtomicInteger();
//            atomicInteger.set(0);
//
//            results.forEach(page -> {
//                XBoardTable3 xBoardTable3 = page.items().get(atomicInteger.get());
//                System.out.println("The issue title is " + xBoardTable3.getConnectionId());
//                atomicInteger.incrementAndGet();
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
//    private String queryTable(DynamoDbEnhancedClient enhancedClient) {
//        try{
//            DynamoDbTable<XBoardTable3> mappedTable = enhancedClient.table("simplechat_connections", TableSchema.fromBean(XBoardTable3.class));
//            QueryConditional queryConditional = QueryConditional
//                    .keyEqualTo(Key.builder()
//                            .partitionValue("simplechat_connections")
//                            .build());
//
//            // Get items in the table and write out the ID value
//            Iterator<XBoardTable3> results = mappedTable.query(queryConditional).items().iterator();
//            String result="";
//
//            while (results.hasNext()) {
//                XBoardTable3 rec = results.next();
//                result = rec.getConnectionId();
//                System.out.println("The record id is "+result);
//            }
//            return result;
//
//        } catch (DynamoDbException e) {
//            System.err.println(e.getMessage());
//            System.exit(1);
//        }
//        return "";
//    }
//    }

//    private QueryResponse queryTable(DynamoDbClient ddb, String tableName, String partitionKeyName, String partitionKeyVal) {
//        HashMap<String, AttributeValue> attrValues =
//                new HashMap<>();
//        attrValues.put(":"+partitionKeyName, AttributeValue.builder()
//                .s(partitionKeyVal)
//                .build());
//
//
//        QueryRequest queryRequest=QueryRequest.builder()
//                .tableName(tableName)
//                .keyConditionExpression(partitionKeyVal +"= :"+partitionKeyName)
//                .expressionAttributeValues(attrValues)
//                .build();
//
//        try{
//            QueryResponse queryResponse=ddb.query(queryRequest);
//            return queryResponse;
//        } catch (AwsServiceException e) {
//            e.printStackTrace();
//        } catch (SdkClientException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


//}


//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withRegion("ap-south-1")
//                .build();
//        DynamoDBMapper mapper = new DynamoDBMapper(client);
//        List<XBoardTable3> result = loadData(mapper);
//        List<String> allConnectionId = new ArrayList<>();
//        for (XBoardTable3 xb : result) {
//            if (!xb.getConnectionId().equals(connectionId)) {
//                allConnectionId.add(xb.getConnectionId());
//            }
//        }
//        for (String cId : allConnectionId) {
//            ApiGatewayManagementApiClient apiGatewayManagementApiClient;
//            try {
//                apiGatewayManagementApiClient = ApiGatewayManagementApiClient.builder()
//                        .endpointOverride(new URI("https://" + input.getRequestContext().getDomainName() + '/' + input.getRequestContext().getStage()))
//                        .build();
//                apiGatewayManagementApiClient.postToConnection(PostToConnectionRequest
//                        .builder()
//                        .data(SdkBytes.fromUtf8String(message.get("data").toString()))
//                        .connectionId(cId)
//                        .build());
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        }
//        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
//        response.setIsBase64Encoded(false);
//        response.setStatusCode(200);
//        response.setBody("Data Sent.");
//        return response;
//    }
//
//    private static List<XBoardTable3> loadData(DynamoDBMapper mapper) {
//        XBoardTable3 xBoardTable3 = new XBoardTable3();
//        xBoardTable3.setTable("simplechat_connections");
//        DynamoDBQueryExpression<XBoardTable3> queryExpression = new DynamoDBQueryExpression<XBoardTable3>()
//                .withHashKeyValues(xBoardTable3)
//                .withIndexName("table-index")
//                .withConsistentRead(false);
//        List<XBoardTable3> result = mapper.query(XBoardTable3.class, queryExpression);
//        for (XBoardTable3 xb : result) {
//            System.out.println(xb.getConnectionId());
//        }
//        return result;
//    }
//}
