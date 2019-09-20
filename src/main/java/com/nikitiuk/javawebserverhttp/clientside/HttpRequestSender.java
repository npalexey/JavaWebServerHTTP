package com.nikitiuk.javawebserverhttp.clientside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestSender {
    private static final Logger logger =  LoggerFactory.getLogger(HttpRequestSender.class);
    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        HttpRequestSender http = new HttpRequestSender();

        logger.info("Testing 1 - Send Http GET request");
        http.sendGet();

        logger.info("Testing 2 - Send Http POST request");
        http.sendPostOrPut("POST");

        /*logger.info("Testing 3 - Send Http DELETE request");
        http.sendDelete();*/
    }

    private void sendGet() throws Exception {

        String url = "http://localhost:7070/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        logger.info("Sending 'GET' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        logger.info(response.toString());
    }

    private void sendPostOrPut(String requestMethod) throws Exception {
        if(!requestMethod.equals("POST") && !requestMethod.equals("PUT")){
            logger.info("Wrong Request Method");
            return;
        }
        String url = "http://localhost:7070/mapdata";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(requestMethod);
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "number=4892&name=something";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        logger.info(String.format("Sending '%s' request to URL : " + url, requestMethod));
        logger.info("Post parameters : " + urlParameters);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        logger.info(response.toString());
    }

    private void sendDelete() throws Exception {
        String baseUrl = "http://localhost:7070/";
        String requestValueToDelete = "name";
        String url = baseUrl.concat(requestValueToDelete);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("DELETE");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        logger.info("Sending 'DELETE' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        logger.info(response.toString());
    }
}