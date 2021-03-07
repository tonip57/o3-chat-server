package com.tonipennanen.chatserver;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

public class ChatServer {
    private static SSLContext chatServerSSLContext(String pass, String jksfile) throws Exception {
        char[] passphrase = pass.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(jksfile), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLSv1.2");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Launching ChatServer..");
            System.out.println("Initializing database..");
            if (args.length != 3) {
                System.out.println("Usage java -jar jar-file.jar dbname.db cert.jks c3rt-p4ssw0rd");
                return;
            }
            boolean running = true;
            ChatDatabase database = ChatDatabase.getInstance();
            database.open(args[0]);
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = chatServerSSLContext(args[2], args[1]);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            ChatAuthenticator ca = new ChatAuthenticator();
            HttpContext chatContext = server.createContext("/chat", new ChatHandler());
            chatContext.setAuthenticator(ca);
            server.createContext("/registration", new RegistrationHandler(ca));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Started");

            Console console = System.console();

            while (running == true) {
                String quitStr = console.readLine();
                System.out.println(quitStr);
                if (quitStr.equals("/quit")) {
                    running = false;
                    server.stop(3);
                    System.out.println("Server stopped");
                    database.close();
                    System.out.println("Database closed");
                }
            }
        } catch (FileNotFoundException e) {
            // Certificate file not found!
            System.out.println("Certificate not found");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }
}
