package com.tonipennanen.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {
    private ChatAuthenticator ca = null;

    public RegistrationHandler(ChatAuthenticator ca) {
        this.ca = ca;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int status = 200;
        String responseBody = "";
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Headers headers = exchange.getRequestHeaders();
                int contentLength = 0;
                String contentType = "";
                if (headers.containsKey("Content-Length")) {
                    contentLength = Integer.parseInt(headers.get("Content-Length").get(0));
                } else {
                    status = 411;
                }
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    status = 400;
                    responseBody = "No content type";
                }
                if (contentType.equalsIgnoreCase("text/plain")) {
                    InputStream input = exchange.getRequestBody();
                    String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    input.close();
                    if (text.trim().length() > 0) {
                        String [] items = text.split(":");
                        if (items.length == 2) {
                            if (items[0].trim().length() > 0 && items[1].trim().length() > 0) {
                                if (ca.addUser(items[0], items[1])){
                                    exchange.sendResponseHeaders(status, -1);
                                } else {
                                    
                                }
                            } else {
                                status = 400;
                                responseBody = "Invalid user credentials";
                            }
                        } else {
                            status = 400;
                            responseBody = "Invalid user credentials";
                        }
                    } else {
                        status = 400;
                        responseBody = "No content in request";
                    }
                } else {
                    status = 411;
                    responseBody = "Content-Type must be text/plain";
                }
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
            byte[] bytes = responseBody.getBytes("UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();
        }
    }
}
