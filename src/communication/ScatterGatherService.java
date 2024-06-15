package communication;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ScatterGatherService {
    private final List<ClientConnection> connections;
    private final ExecutorService threadPool;
    private final List<Future<String>> futures = new ArrayList<>();

    public ScatterGatherService(List<Integer> ports) {
        connections = openConnections(ports);
        threadPool = Executors.newFixedThreadPool(connections.size());
    }

    private List<ClientConnection> openConnections(List<Integer> ports) {
        List<ClientConnection> connections = new ArrayList<>();

        ports.forEach(port -> {
            ClientConnection connection = new ClientConnection("127.0.0.1", port);
            connection.connect();
            connections.add(connection);
        });

        return connections;
    }

    private void closeConnections() {
        connections.forEach(ClientConnection::stop);
    }

    public <T> void scatter(Collection<T> data) {
        int targetConnectionIndex = 0;
        int numberOfConnections = connections.size();

        for (T item : data) {
            ClientConnection connection = connections.get(targetConnectionIndex);
            connection.send(item.toString());

            Future<String> future = threadPool.submit(connection::receive);
            futures.add(future);

            targetConnectionIndex = (targetConnectionIndex + 1) % numberOfConnections;
        }
    }

    public List<String> gather() {
        List<String> responses = new ArrayList<>();

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

    public void stopService() {
        threadPool.shutdown();
        closeConnections();
    }
}
