package com.tonipennanen.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ChatHandler implements HttpHandler {
    
    private String responseBody = "";

    private ArrayList<String> messages = new ArrayList<String>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int status = 200;
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")){
                status = handlePOSTFromClient(exchange);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")){
                status = handleGETFromClient(exchange);
            } else {
                status = 400;
                responseBody = "Not supported";
            }
        } catch (IOException e) {
            status = 500;
            responseBody = "Error in handling the request: " + e.getMessage();
        } catch (Exception e) {
            status = 500;
            responseBody = "Server error: " + e.getMessage();
        }
        if (status < 200 || status > 299) {
            byte [] bytes = responseBody.getBytes("UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();
        }
    }
    

    private int handlePOSTFromClient(HttpExchange exchange) throws Exception {
        int status = 200;
        Headers headers = exchange.getRequestHeaders();
        int contentLength = 0;
        String contentType = "";
        if (headers.containsKey("Content-Length")) {
            contentLength = Integer.parseInt(headers.get("Content-Length").get(0));
        } else {
            status = 411;
            return status;
        }
        if (headers.containsKey("Content-Type")) {
            contentType = headers.get("Content-Type").get(0);
        } else {
            status = 400;
            responseBody = "No content type";
            return status;
        }
        if (contentType.equalsIgnoreCase("text/plain")) {
            InputStream input = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            input.close();
            if (text.trim().length() > 0) {
                processMessage(text);
                exchange.sendResponseHeaders(status, -1);
            } else {
                status = 400;
                responseBody = "No content in request";
            }
        } else {
            status = 411;
            responseBody = "Content-Type must be text/plain";
        }
        return status;
    }

    private void processMessage(String text) {
        messages.add(text);
    }

    private int handleGETFromClient(HttpExchange exchange) throws Exception {
        int status = 200;

        if (messages.isEmpty()) {
            status = 204;
            exchange.sendResponseHeaders(status, -1);
            return status;
        }
        responseBody = "";
        for (String message : messages) {
            responseBody += message + "\n";            
        }
        byte [] bytes;
        bytes = responseBody.toString().getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        return status;
    }
}
