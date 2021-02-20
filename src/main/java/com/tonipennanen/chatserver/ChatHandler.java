package com.tonipennanen.chatserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatHandler implements HttpHandler {
    
    private String responseBody = "";
    

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
        String message = "";
        String nick = "";
        String sent = "";
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
        if (contentType.equalsIgnoreCase("application/json")) {
            InputStream input = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            input.close();
            try {
                JSONObject jsonObject = new JSONObject(text);;
                nick = jsonObject.getString("user");
                message = jsonObject.getString("message");
                String dateStr = jsonObject.getString("sent");
                OffsetDateTime odt = OffsetDateTime.parse(dateStr);
                
                System.out.println("Trying to POST message");
                if (!nick.isBlank() || !message.isBlank() || !sent.isBlank()) {
                    ChatDatabase cdb = ChatDatabase.getInstance();
                    cdb.addMessageToDatabase(new ChatMessage(nick, message, odt.toLocalDateTime()));
                    exchange.sendResponseHeaders(status, -1);
                    System.out.println("Message sent");
                } else {
                    status = 400;
                    System.out.println("Username, message or sent is missing");
                    responseBody = "Username, message or sent is missing";
                }
            } catch (JSONException e) {
                status = 400;
                responseBody = "JSON Exception";
                System.out.println("Couldn't read JSON file or file doesn't exist");
            }
        } else {
            status = 411;
            responseBody = "Content-Type must be application/json";
        }
        return status;
    }


    private int handleGETFromClient(HttpExchange exchange) throws Exception {
        int status = 200;
        JSONArray responseMessages = new JSONArray();
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
        ChatDatabase cdb = ChatDatabase.getInstance();
        messages = cdb.getMessages();
        
        if (messages.isEmpty()) {
            status = 204; // response code is 20, No Content
            exchange.sendResponseHeaders(status, -1); // -1 as content length: No content
            return status;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            for (ChatMessage message : messages){
                System.out.println(message.getSent() + " " + message.getNick() + " " + message.getMessage());

                ZonedDateTime zdt = message.sent.atZone(ZoneId.of( "UTC" ));
                String sentUTC = zdt.format(formatter);

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("user", message.getNick());
                jsonObject.put("message", message.getMessage());
                jsonObject.put("sent", sentUTC);

                responseMessages.put(jsonObject);
            }

            byte [] bytes;
            bytes = responseMessages.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(status, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
            return status;
        }        
    }
}
