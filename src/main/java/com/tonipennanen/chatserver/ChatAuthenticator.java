package com.tonipennanen.chatserver;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

//ChatAutheticator class adds new users and checks if they already exist
//also checks user credentials

public class ChatAuthenticator extends BasicAuthenticator {
    private Map<String, User> users = null;

    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String, User>();
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        ChatDatabase cdb = ChatDatabase.getInstance();
        if (cdb.checkCredentials(username, password)) {
            return true;
        }
        System.out.println("Invalid password or username");
        return false;
    }

    public boolean addUser(String username, User user) throws SQLException {
        ChatDatabase cdb = ChatDatabase.getInstance();
        if (cdb.checkAddUser(username, user)) {
            return true;
        }
        return false;
    }
}
