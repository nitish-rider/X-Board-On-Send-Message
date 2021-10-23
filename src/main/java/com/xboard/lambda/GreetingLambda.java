package com.xboard.lambda;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GreetingLambda implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayV2WebSocketEvent input, Context context) {
        Gson s = new Gson();
        JsonObject object = JsonParser.parseString(s.toJson(input)).getAsJsonObject();
        String connectionId = object.get("requestContext").getAsJsonObject().get("connectionId").getAsString();
        JsonObject mess = JsonParser.parseString(input.getBody()).getAsJsonObject();
        System.out.println(mess.get("data"));
        CredData credData = new CredData();
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(credData.getACCESS_KEY(),
                        credData.getSECRET_KEY())
        );
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion("ap-south-1")
                .build();
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        List<XBoardTable3> result = loadData(mapper);
        List<String> allConnectionId = new ArrayList<>();
        for (XBoardTable3 xb : result) {
            if (!xb.getConnectionId().equals(connectionId)) {
                allConnectionId.add(xb.getConnectionId());
            }
        }
        for (String cId : allConnectionId) {
            ApiGatewayManagementApiClient apiGatewayManagementApiClient = null;
            try {
                apiGatewayManagementApiClient = ApiGatewayManagementApiClient.builder()
                        .endpointOverride(new URI("https://" + input.getRequestContext().getDomainName() + '/' + input.getRequestContext().getStage()))
                        .build();
                apiGatewayManagementApiClient.postToConnection(PostToConnectionRequest
                        .builder()
                        .data(SdkBytes.fromUtf8String(mess.get("data").toString()))
                        .connectionId(cId)
                        .build());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        response.setBody("Data Sent.");
        return response;
    }

    private static List<XBoardTable3> loadData(DynamoDBMapper mapper) {
        XBoardTable3 xBoardTable3 = new XBoardTable3();
        xBoardTable3.setTable("simplechat_connections");
        DynamoDBQueryExpression<XBoardTable3> queryExpression = new DynamoDBQueryExpression<XBoardTable3>()
                .withHashKeyValues(xBoardTable3)
                .withIndexName("table-index")
                .withConsistentRead(false);
        List<XBoardTable3> result = mapper.query(XBoardTable3.class, queryExpression);
        for (XBoardTable3 xb : result) {
            System.out.println(xb.getConnectionId());
        }
        return result;
    }
}
