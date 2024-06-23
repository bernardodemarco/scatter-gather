package com.github.bernardodemarco.textretrieval.communication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final Logger logger = LogManager.getLogger(getClass());

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void listen(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            logger.info("Listening on port {}.", port);
        } catch (IOException e) {
            logger.error("Exception while listening on port {} or listening for a connection.", port, e);
            System.exit(1);
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Exception while closing the sockets and their streams.", e);
            System.exit(1);
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String receive() {
        try {
            return (in != null) ? in.readLine() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
