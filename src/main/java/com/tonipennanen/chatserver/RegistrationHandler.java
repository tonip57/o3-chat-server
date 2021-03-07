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

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {
    private ChatAuthenticator ca = null;

    public RegistrationHandler(ChatAuthenticator ca) {
        this.ca = ca;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request handled in thread " +
        Thread.currentThread().getId());
        int status = 200;
        int contentLength = 0;
        String responseBody = "";
        String username = "";
        String password = "";
        String email = "";
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Headers headers = exchange.getRequestHeaders();
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
                if (contentType.equalsIgnoreCase("application/json")) {
                    InputStream input = exchange.getRequestBody();
                    String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    input.close();
                    try {
                        JSONObject registrationMsg = new JSONObject(text);
                        username = registrationMsg.getString("username");
                        password = registrationMsg.getString("password");
                        email = registrationMsg.getString("email");
                        System.out.println("Trying to create an account. Username: " + username);
                        if (!username.isBlank() || !password.isBlank() || !email.isBlank()) {
                            User user = new User(username, password, email);
                            if (ca.addUser(username, user)) {
                                System.out.println("Account created");
                                responseBody = "Account created";
                                exchange.sendResponseHeaders(status, -1);
                            } else {
                                status = 400;
                                System.out.println("User with this username already exists");
                                responseBody = "User with this username already exists";
                            }
                        } else {
                            status = 400;
                            System.out.println("Some registration credentials is missing");
                            responseBody = "Some registration credentials is missing";
                        }
                    } catch (JSONException e) {
                        status = 400;
                        responseBody = "JSON Exception";
                        System.out.println("Couldn't read JSON file or file doesn't exist");
                    }
                } else {
                    status = 411;
                    System.out.println("Content-Type must be application/json");
                    responseBody = "Content-Type must be application/json";
                }
            } else {
                status = 400;
                System.out.println("Request method not supported");
                responseBody = "Not supported";
            }
        } catch (IOException e) {
            status = 500;
            System.out.println("Error in handling the request");
            responseBody = "Error in handling the request: " + e.getMessage();
        } catch (Exception e) {
            status = 500;
            System.out.println("Server error");
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
