package com.nikitiuk.javawebserverhttp.serverside.singlethread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SomeServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(SomeServer.class);
    private static Properties properties = new Properties();
    private Socket client;
    private BufferedReader inClient = null;
    private DataOutputStream outClient = null;

    public SomeServer(Socket cl) {
        this.client = cl;
    }

    public static void main(String[] args) throws Exception {
        clearPropertiesFile();

        ServerSocket server = new ServerSocket(7070, 10, InetAddress.getByName("127.0.0.1"));

        logger.info("HTTPServer started on port 7070");

        while (true) {
            Socket connected = server.accept();
            SomeServer httpServer = new SomeServer(connected);
            httpServer.start();
        }
    }

    public static void clearPropertiesFile() {
        try {
            if (new File("data.properties").exists()) {  //check whether data.properties file exists and if so - DELETE ITS CONTENT,..
                properties.load(new FileInputStream("data.properties")); //be careful if you have something important there
                properties.clear();
                properties.store(new FileOutputStream("data.properties"), null);
            }
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    @Override
    public void run() {
        try {
            Map<String, String> dataMap = new HashMap<>();
            readPropertiesIntoMap(dataMap);
            logger.info("The Client " + client.getInetAddress() + ":" + client.getPort() + " is connected");

            inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outClient = new DataOutputStream(client.getOutputStream());

            String headerLine = inClient.readLine();

            while (headerLine == null) {
                headerLine = inClient.readLine();
            }
            /*if (headerLine == null) {
                return;
            }*/

            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();

            logger.info("The HTTP request:");
            logger.info(headerLine);

            char[] bodyOfRequest = new char[getContentLengthOfARequestBodyAndLogHeaders(inClient)];
            inClient.read(bodyOfRequest);
            logger.info(new String(bodyOfRequest));

            switch (httpMethod) {
                case "GET":
                    onGetRequest(httpQueryString, dataMap);
                    break;
                case "POST":
                case "PUT":
                    onPutAndUnRestfulPostRequest(httpQueryString, bodyOfRequest, dataMap);
                    break;
                case "DELETE":
                    onDeleteRequest(httpQueryString, dataMap);
                    break;
                default:
                    break;
            }
            inClient.close();
            outClient.close();
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public void readPropertiesIntoMap(Map<String, String> dataMap) {
        try {
            if (new File("data.properties").exists()) {
                properties.load(new FileInputStream("data.properties"));
            }
            if (!properties.isEmpty()) {
                for (String key : properties.stringPropertyNames()) {
                    dataMap.put(key, properties.get(key).toString());
                }
            }
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public Integer getContentLengthOfARequestBodyAndLogHeaders(BufferedReader inClient) throws Exception {
        try {
            boolean headersFinished = false;
            int conlen = 0;
            while (!headersFinished) {
                String line = inClient.readLine();
                headersFinished = line.isEmpty();
                logger.info(line);
                if (line.startsWith("Content-Length:")) {
                    String cl = line.substring("Content-Length:".length()).trim();
                    conlen = Integer.parseInt(cl);
                }
            }
            return conlen;
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
            throw e;
        }
    }

    public void onGetRequest(String httpQueryString, Map<String, String> dataMap) {
        try {
            if (httpQueryString.equals("/")) {
                mainPage();
            } else if (httpQueryString.equals("/results")) {
                resultsPage(dataMap);
            } else {
                sendResponse(404, "<b>The Requested resource not found.</b>");
            }
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public void onPutAndUnRestfulPostRequest(String httpQueryString, char[] bodyOfRequest, Map<String, String> dataMap) {
        try {
            if (httpQueryString.equals("/mapdata") && bodyOfRequest.length > 0) {
                putPairsFromRequestIntoMap(bodyOfRequest, dataMap);

                if (!dataMap.isEmpty()) {
                    logger.info(dataMap.toString());
                    properties.putAll(dataMap);
                    properties.store(new FileOutputStream("data.properties"), null);
                }
                sendResponse(200, "HTTPServer Home Page.");
            } else {
                sendResponse(204, "No Content In The Body Of Request");
            }

        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public void putPairsFromRequestIntoMap(char[] bodyOfRequest, Map<String, String> dataMap) {
        try {
            String query = new String(bodyOfRequest);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                if (dataMap.containsKey(key)) {
                    logger.info(String.format("Map already contains the key: %s", key));
                } else {
                    dataMap.put(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public void onDeleteRequest(String httpQueryString, Map<String, String> dataMap) {
        try {
            String keyToDelete;
            if (httpQueryString.length() > 1 && dataMap.containsKey(keyToDelete = httpQueryString.substring(1))) {
                dataMap.remove(keyToDelete);
                logger.info(dataMap.toString());
                sendResponse(200, String.format("%s successfully removed", keyToDelete));
            } else {
                sendResponse(404, "Resource Not Found");
            }
        } catch (Exception e) {
            logger.error("Exception thrown: ", e);
        }
    }

    public void sendResponse(int statusCode, String responseString) throws Exception {
        String HTML_START = "<html><head><meta charset=\"UTF-8\"><title>HTTP Server in Java</title></head><body>";
        String HTML_END = "</body></html>";
        String NEW_LINE = "\r\n";

        String statusLine;
        String serverDetails = Headers.SERVER + ": Java Server" + NEW_LINE;
        String contentLengthLine;
        String contentTypeLine = Headers.CONTENT_TYPE + ": text/html" + NEW_LINE;
        String currentDate = Headers.DATE + ": ";

        if (statusCode == 200) {
            statusLine = Status.HTTP_200;
        } else if (statusCode == 204) {
            statusLine = Status.HTTP_204;
        } else {
            statusLine = Status.HTTP_404;
        }

        statusLine += NEW_LINE;
        responseString = HTML_START + NEW_LINE + responseString + NEW_LINE + HTML_END;
        contentLengthLine = Headers.CONTENT_LENGTH + ": " + responseString.length() + NEW_LINE;
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O",
                Locale.ENGLISH)/*.withZone(ZoneOffset.UTC)*/;
        currentDate = currentDate + formatter2.format(ZonedDateTime.now(ZoneId.systemDefault())) + NEW_LINE;

        outClient.writeBytes(statusLine);
        outClient.writeBytes(serverDetails);
        outClient.writeBytes(contentTypeLine);
        outClient.writeBytes(contentLengthLine);
        outClient.writeBytes(currentDate);
        outClient.writeBytes(Headers.CONNECTION + ": close" + NEW_LINE);

        outClient.writeBytes(NEW_LINE);

        outClient.writeBytes(responseString);

        outClient.close();
    }

    public void mainPage() throws Exception {
        StringBuffer responseBuffer = new StringBuffer();
        responseBuffer.append("<b>HTTPServer First Attempt.</b>");
        sendResponse(200, responseBuffer.toString());
    }

    public void resultsPage(Map<String, String> dataMap) throws Exception {
        StringBuilder responseBuilder = new StringBuilder();
        if (dataMap != null && !dataMap.isEmpty()) {
            responseBuilder.append("<b>Data: ").append(dataMap.toString()).append("<b>");
        } else {
            responseBuilder.append("<b>HTTPServer First Attempt.</b>");
        }
        sendResponse(200, responseBuilder.toString());
    }

    private static class Headers {

        private static final String SERVER = "Server";
        private static final String CONNECTION = "Connection";
        private static final String CONTENT_LENGTH = "Content-Length";
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String DATE = "Date";
    }

    private static class Status {

        private static final String HTTP_200 = "HTTP/1.1 200 OK";
        private static final String HTTP_204 = "HTTP/1.1 204 No Content";
        private static final String HTTP_404 = "HTTP/1.1 404 Not Found";
    }
}