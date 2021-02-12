package com.tonipennanen.chatserver;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class ChatAuthenticator extends BasicAuthenticator{
    private Map<String,User> users = null;

    public ChatAuthenticator(){
        super("chat");
        users = new Hashtable<String,User>();
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        if (users.containsKey(username)){
            if (password.equals(users.get(username).getPassword())) {
                return true;
            }
        }
        System.out.println("Invalid password or username");
        return false;
    }
    public boolean addUser(String username, User user) {
        if (!users.containsKey(username)) {
            users.put(username, user);
            return true;
        }
        return false;
    }
}
