package com.tonipennanen.chatserver;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

//Class for chat message objects

public class ChatMessage {
    LocalDateTime sent;
    private String nick;
    private String message;

   
    public ChatMessage(String nick, String message, LocalDateTime sent){
        this.nick = nick;
        this.message = message;
        this.sent = sent;
    }

    public String getNick(){
        return nick;
    }

    public String getMessage(){
        return message;
    }

    public LocalDateTime getSent(){
        return sent;
    }

    long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}

