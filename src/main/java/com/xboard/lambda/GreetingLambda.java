package com.xboard.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.*;

import java.util.HashMap;

public class GreetingLambda implements RequestHandler<HashMap<String, Object>, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(HashMap<String, Object> input, Context context) {
        Gson s = new Gson();


        JsonObject object = JsonParser.parseString(s.toJson(input)).getAsJsonObject();
        String connectionId = object.get("requestContext").getAsJsonObject().get("connectionId").getAsString();



        System.out.println(object);

        System.out.println(input.get("body"));
        System.out.println(input.get("body").getClass());


        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        response.setBody( "Data Sent.");
        return response;
    }
}
