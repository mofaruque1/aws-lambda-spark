package com.omor.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.Spark;

import static spark.Spark.before;
import static spark.Spark.get;


public class RequestStreamLambdaHandler implements RequestStreamHandler {
	private SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
	private boolean isInitialized = false;

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		if (!isInitialized) {
            isInitialized = true;
            try {
				handler = SparkLambdaContainerHandler.getAwsProxyHandler();
				defineBaseResources();
				Spark.awaitInitialization();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		AwsProxyRequest request = LambdaContainerHandler.getObjectMapper().readValue(inputStream, AwsProxyRequest.class);
		AwsProxyResponse response = handler.proxy(request, context);
		ObjectMapper mapper = LambdaContainerHandler.getObjectMapper();
		mapper.writeValue(outputStream, response);
		outputStream.close();
	}
	
	public void defineBaseResources() {
		before("/*", (req,res) -> {
			res.type("application/json");
			res.header("access-control-allow-headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
			res.header("access-control-allow-methods", "POST,GET,PUT,DELETE,OPTIONS");
			res.header("access-control-allow-origin", "*");
		});
		
		 get("/urlone", (req, res) -> {
	            res.status(200);
	            return "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
	        });
		 get("/urltwo", (req, res) -> {
	            res.status(200);
	            return "{ \"color\" : \"Black\", \"type\" : \"LAMBO\" }";
	        });
		
	}

}
