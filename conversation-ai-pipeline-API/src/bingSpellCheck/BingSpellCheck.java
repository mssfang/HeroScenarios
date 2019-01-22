package bingSpellCheck;

import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class BingSpellCheck {

//    static String host = "https://api.cognitive.microsoft.com";
//    static String path = "/bing/v7.0/spellcheck";

    // NOTE: Replace this example key with a valid subscription key.
    static String subscriptionKey, host, path;
   
    public BingSpellCheck(String subscriptionKey, String host, String path) {
    	this.subscriptionKey = subscriptionKey;
    	this.host = host;
    	this.path = path;
    }
    
    public static void check (String mkt, String mode, String text) throws Exception {
        String params = "?mkt=" + mkt + "&mode=" + mode;
        URL url = new URL(host + path + params);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" + text.length() + 5);
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes("text=" + text);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
        new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
    }
}
