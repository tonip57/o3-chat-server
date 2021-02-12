package com.tonipennanen.chatserver;

import java.time.LocalDateTime;

public class ChatMessage {
    LocalDateTime sent;
    private String user;
    private String message;

   
    public ChatMessage(String user, String message, LocalDateTime sent){
        this.user = user;
        this.message = message;
        this.sent = sent;
    }

    public String getUser(){
        return user;
    }

    public String getMessage(){
        return message;
    }

    public LocalDateTime getSent(){
        return sent;
    }
}

