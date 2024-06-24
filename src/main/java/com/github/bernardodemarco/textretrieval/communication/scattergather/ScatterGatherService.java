package com.github.bernardodemarco.textretrieval.communication.scattergather;

import com.github.bernardodemarco.textretrieval.communication.client.ClientConnection;
import com.github.bernardodemarco.textretrieval.communication.client.TCPClientConnection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ScatterGatherService implements ScatterGather {
    private final Logger logger = LogManager.getLogger(getClass());

    private final List<ClientConnection> connections;
    private final ExecutorService threadPool;
    private final List<Future<String>> futures = new ArrayList<>();

    public ScatterGatherService(List<Map.Entry<String, Integer>> addresses) {
        connections = openConnections(addresses);
        threadPool = Executors.newFixedThreadPool(connections.size());
    }

    private List<ClientConnection> openConnections(List<Map.Entry<String, Integer>> addresses) {
        List<ClientConnection> connections = new ArrayList<>();

        addresses.forEach((address) -> {
            String ip = address.getKey();
            int port = address.getValue();
            ClientConnection connection = new TCPClientConnection(ip, port);
            connection.connect();
            connections.add(connection);
        });

        return connections;
    }

    private void closeConnections() {
        connections.forEach(ClientConnection::stop);
    }

    @Override
    public <T> void scatter(Collection<T> data) {
        int targetConnectionIndex = 0;
        int numberOfConnections = connections.size();
        logger.debug("Scattering data [{}] to [{}] connections using round-robin algorithm.", data, numberOfConnections);

        for (T item : data) {
            ClientConnection connection = connections.get(targetConnectionIndex);
            connection.send(item.toString());

            Future<String> future = threadPool.submit(connection::receive);
            futures.add(future);

            targetConnectionIndex = (targetConnectionIndex + 1) % numberOfConnections;
        }
    }

    @Override
    public List<String> gather() {
        List<String> responses = new ArrayList<>();

        logger.debug("Gathering data.");
        for (Future<String> future : futures) {
            String response;
            try {
                response = future.get();
            } catch (Exception e) {
                continue;
            }
            responses.add(response);
        }

        futures.clear();
        return responses;
    }

    @Override
    public void stop() {
        threadPool.shutdown();
        closeConnections();
    }
}
