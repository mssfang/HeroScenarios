package qNa;

import java.net.URI;

import org.apache.http.HttpEntity;

import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.client.utils.URIBuilder;

import org.apache.http.impl.client.HttpClients;

import org.apache.http.util.EntityUtils;

import org.apache.http.entity.StringEntity;



// 1. Replace variable values with your own

// 2. Compile with: javac -cp "lib/*" PublishKB.java

// 3. Execute with: java -cp ".;lib/*" PublishKB



public class PublishKB {



    public static void main(String[] args) 

    {



        try

        {
            String knowledge_base_id = "3832a7d2-a72f-47f5-9e91-3675a3e5f52c";

            String resource_key = "19de0fc8ad0f4e9989e8fd6fed47870e";



            String host = "https://westus.api.cognitive.microsoft.com/qnamaker/v4.0/knowledgebases/" + knowledge_base_id;



            // Create http client

            HttpClient httpclient = HttpClients.createDefault();



            HttpPost request = new HttpPost(host);



            // set authorization
            request.setHeader("Ocp-Apim-Subscription-Key",resource_key);

            // Send request to Azure service, get response
            HttpResponse response = httpclient.execute(request);

            // No returned content, 204 == success
            System.out.println(response.getStatusLine().getStatusCode());

        }



        catch (Exception e)

        {

            System.out.println(e.getMessage());

        }

    }

}
