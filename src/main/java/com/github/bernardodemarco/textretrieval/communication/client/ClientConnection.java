package com.github.bernardodemarco.textretrieval.communication.client;

public interface ClientConnection {
    void connect();
    void stop();
    void send(String message);
    String receive();
}
