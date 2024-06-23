package com.github.bernardodemarco.textretrieval.communication.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.UnknownHostException;

public class ClientConnection {
    private final Logger logger = LogManager.getLogger(getClass());

    private final String hostAddress;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientConnection(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(hostAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("Successfully connected to server [{}:{}].", hostAddress, port);
        } catch (UnknownHostException e) {
            logger.error("Unknown host [{}:{}].", hostAddress, port, e);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Exception while getting [{}:{}] connection input/output.", hostAddress, port, e);
            System.exit(1);
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Exception while closing the socket and its streams.", e);
            System.exit(1);
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public synchronized String receive() {
        try {
            return (in != null) ? in.readLine() : null;
        } catch (IOException e) {
            return null;
        }
    }
}