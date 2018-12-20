package luis;


//This sample uses the Apache HTTP client from HTTP Components (http://hc.apache.org/httpcomponents-client-ga/)

//You need to add the following Apache HTTP client libraries to your project:
//httpclient-4.5.3.jar
//httpcore-4.4.6.jar
//commons-logging-1.2.jar

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Luis {
	
	public static String recognizeIntent(String param, String values) {
		HttpClient httpclient = HttpClients.createDefault();
	
	    try
	    {
	
	        // The ID of a public sample LUIS app that recognizes intents for turning on and off lights
	        String AppId = "29327343-4a02-4653-a1e4-f6a8112e4dc6";

	        // Add your endpoint key 
	        // You can use the authoring key instead of the endpoint key. 
	        // The authoring key allows 1000 endpoint queries a month.
	        String EndpointKey = "616f4ba49e514a7b8f202285d84b3699";
	
	        // Begin endpoint URL string building
	        URIBuilder endpointURLbuilder = new URIBuilder("https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/" + AppId + "?");
	
	        // query text
	        endpointURLbuilder.setParameter(param, values);
	
	        // create URL from string
	        URI endpointURL = endpointURLbuilder.build();
	
	        // create HTTP object from URL
	        HttpGet request = new HttpGet(endpointURL);
	
	        // set key to access LUIS endpoint
	        request.setHeader("Ocp-Apim-Subscription-Key", EndpointKey);
	
	        // access LUIS endpoint - analyze text
	        HttpResponse response = httpclient.execute(request);
	
	        // get response
	        HttpEntity entity = response.getEntity();
	
	
	        if (entity != null) 
	        {
	            return EntityUtils.toString(entity);
	        }
	    }
	
	    catch (Exception e)
	    {
	        System.out.println(e.getMessage());
	    }
		return "";
	}
	

	
	public static String getTopIntent(String luisResp) {
		
		JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(luisResp).getAsJsonObject();
        
        JsonObject topScoringIntent = json.getAsJsonObject("topScoringIntent");
        if (topScoringIntent == null) return "";
        String intentStr = topScoringIntent.get("intent").getAsString();
        
        
        if (intentStr == null || intentStr.isEmpty()) return "";
    	JsonArray answers = json.getAsJsonArray("entities");
		String entity = null;
    	for (int i = 0; i < answers.size(); i++) {
    		if (entity != null) {
    			break;
    		}
    		JsonObject enti = answers.get(i).getAsJsonObject();
    		entity = enti.get("entity").getAsString();
    	}
    	if (entity == null || entity.isEmpty()) return "";
		return intentStr + "." + entity;
	}
		
 public static void main(String[] args) 
 {
	 
//	 String resp = recognizeIntent("q", "turn off the left light");
	 String resp = recognizeIntent("q", "call");
	 System.out.println("resp = " + resp);
	 String  topIntent = getTopIntent(resp);
	 System.out.println("top intent = " + topIntent);
 }
}
