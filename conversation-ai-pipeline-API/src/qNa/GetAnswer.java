package qNa;

import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;

import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.utils.URIBuilder;

import org.apache.http.impl.client.HttpClients;

import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.entity.StringEntity;



// 1. Replace variable values with your own from QnA Maker Publish page

// 2. Compile with: javac -cp "lib/*" GetAnswer.java

// 3. Execute with: java -cp ".;lib/*" GetAnswer

public class GetAnswer {


	
	public static String getAns (String questionParam) {
		try

        {

            // Represents the various elements used to create HTTP request URIs

            // for QnA Maker operations.

            // From Publish Page: HOST

            // Example: https://YOUR-RESOURCE-NAME.azurewebsites.net/qnamaker

            String host = "https://qamakershawn.azurewebsites.net/qnamaker";



            // Management APIs postpend the version to the route

            // From Publish Page, value after POST

            // Example: /knowledgebases/ZZZ15f8c-d01b-4698-a2de-85b0dbf3358c/generateAnswer

            String service = "/knowledgebases/3832a7d2-a72f-47f5-9e91-3675a3e5f52c/generateAnswer";



            // Authorization endpoint key

            // From Publish Page

            String endpointKey = "EndpointKey 3bb1a567-c8e8-43e9-a069-aa43b8c0cfea";



            // JSON format for passing question to service

            String question = "{ 'question' : '" + questionParam + "', 'top' : 3 }";

            
            System.out.println("*******   " + question + "   ***********");
           


            // Create http client

            HttpClient httpclient = HttpClients.createDefault();



            // Add host + service to get full URI

            String answer_uri = host + service;



            //

            HttpPost request = new HttpPost(answer_uri);



            // set question

            StringEntity entity = new StringEntity(question);

            request.setEntity(entity);

            request.setHeader("Content-type", "application/json");



            // set authorization

            request.setHeader("Authorization", endpointKey);



            // Send request to Azure service, get response

            HttpResponse response = httpclient.execute(request);



            HttpEntity entityResponse = response.getEntity();



            if (entityResponse != null) 

            {
                System.out.println("response back!");
                return EntityUtils.toString(entityResponse);
            }

        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
        }
		
		
		return "No answer";

	}
	
	
	
	public static String getTopAns(String ansJson) {
		
		JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(ansJson).getAsJsonObject();
    	JsonArray answers = json.getAsJsonArray("answers");
    	
		String finalAns = null;
		for (int i = 0; i < answers.size(); i++) {
			
			if (finalAns != null) break;
			
			JsonObject topAns = answers.get(i).getAsJsonObject();
			JsonArray potentialQuestions = topAns.getAsJsonArray("questions");
			
			for (int j = 0; j < potentialQuestions.size(); j++) {
				String pq = potentialQuestions.get(i).getAsString();
			}
			finalAns = topAns.get("answer").getAsString();
			
		}
		return finalAns;
	}
	
	

    public static void main(String[] args) 

    {
    	String q = "When should I restart my app service?";
    	q = "Is the QnA Maker Service free?";
    	String ans = getAns(q);
    	System.out.println("question = " + q);
    	System.out.println("answer = " + getTopAns(ans));
    	
    }

}