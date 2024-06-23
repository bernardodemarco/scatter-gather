package com.github.bernardodemarco.textretrieval.communication.server;

public interface Server {
    void listen(int port);
    void stop();
    void send(String message);
    String receive();
}
