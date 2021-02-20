package com.tonipennanen.chatserver;

import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatDatabase {
    private static ChatDatabase singleton = null;
    Connection connection = null;

    public static synchronized ChatDatabase getInstance() {
        if (null == singleton) {
            singleton = new ChatDatabase();
        }
        return singleton;
    }

    private ChatDatabase() {
    }

    public void open(String dbName) throws SQLException {
        File db = new File(dbName);
        Boolean b = false;
        if (db.exists() && !db.isDirectory()) {
            System.out.println("Opening database");
            b = true;
            String jdbc = String.join("", "jdbc:sqlite:", dbName);
            connection = DriverManager.getConnection(jdbc);

        } else {
            System.out.println("Creating database");
            initializeDatabase(dbName);
        }
    }

    private boolean initializeDatabase(String dbName) throws SQLException {
        String jdbc = String.join("", "jdbc:sqlite:", dbName);
        connection = DriverManager.getConnection(jdbc);
        if (null != connection) {
            String createUsersString = "create table USERS " + "(USERNAME varchar(40) NOT NULL, "
                    + "PASSWORD varchar(40) NOT NULL, " + "EMAIL varchar(40) NOT NULL, " + "PRIMARY KEY (USERNAME))";

            String createMessagesString = "create table MESSAGES " + "(MESSAGE varchar(255) NOT NULL, "
                    + "USERNAME varchar(40) NOT NULL, " + "SENT numeric NOT NULL, "  
                    +  "INTEGER PRIMARY KEY, " + "FOREIGN KEY (USERNAME) REFERENCES USERS (USERNAME))";

            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(createUsersString);
            createStatement.executeUpdate(createMessagesString);
            createStatement.close();
            return true;
        }
        return false;
    }

    public boolean checkAddUser(String username, User user) throws SQLException {
        
        String query = "SELECT EXISTS(SELECT 1 FROM USERS WHERE username = '" + username + "')";
        try {
            ResultSet resultSet;
            Statement statem = connection.createStatement();
            resultSet = statem.executeQuery(query);
            
            if (resultSet.next()) {
                String addUserString = "insert into USERS values('" + username + "', '" + user.getPassword() + "' , '" + user.getEmail() + "')";
                Statement createStatement = connection.createStatement();
                createStatement.executeUpdate(addUserString);
                createStatement.close();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

    }

    public boolean checkCredentials(String username, String password) {
        String query = "SELECT PASSWORD FROM USERS WHERE USERNAME = '" + username + "';";

        try {
            ResultSet resultSet;
            Statement statem = connection.createStatement();
            resultSet = statem.executeQuery(query);

            if (resultSet.getString(1).equalsIgnoreCase(password)) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("SQLException");
            return false;
        }
        return false;
    }

    public void addMessageToDatabase(ChatMessage message) {
        String addMessageString = "insert into MESSAGES values('" +  message.getMessage() 
        + "', '" + message.getNick() + "' , '" + message.dateAsInt() + "' , " + null +  ")";

        try {

            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(addMessageString);
            createStatement.close();
           
        } catch (SQLException e) {
            System.out.println("SQLException. Can't insert messages");
        }
    }

    public ArrayList<ChatMessage> getMessages() {
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
        ResultSet resultSet;
        String query = "SELECT * FROM MESSAGES;";
        

        try {
            Statement createStatement = connection.createStatement();
            resultSet = createStatement.executeQuery(query);
            while (resultSet.next()) {
                LocalDateTime sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(resultSet.getLong(3)), ZoneOffset.UTC);
                ChatMessage cm = new ChatMessage(resultSet.getString(2), resultSet.getString(1), sent);
                messages.add(cm);
            }
            createStatement.close();
        } catch (SQLException e) {
            System.out.println("SQLException. Can't print messages");
        }

        return messages;
    }
}