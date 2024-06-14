package scatterGather;

import communication.SocketConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScatterGatherService {
    private List<SocketConnection> connections;
    private ExecutorService threadPool;
    List<Future<String>> futures = new ArrayList<>();

    public ScatterGatherService(List<Integer> ports) {
        connections = openConnections(ports);
        threadPool = Executors.newFixedThreadPool(connections.size());
    }

    private List<SocketConnection> openConnections(List<Integer> ports) {
        List<SocketConnection> connections = new ArrayList<>();

        ports.forEach(port -> {
            SocketConnection connection = new SocketConnection("127.0.0.1", port);
            connection.connect();
            connections.add(connection);
        });

        return connections;
    }

    private void closeConnections() {
        connections.forEach(SocketConnection::stop);
    }

    public <T> void scatter(Collection<T> data) {
        int targetConnectionIndex = 0;
        int numberOfConnections = connections.size();

        for (T item : data) {
//            System.out.println(item);
            SocketConnection connection = connections.get(targetConnectionIndex);
            connection.send(item.toString());

            Future<String> future = threadPool.submit(connection::receive);
//            System.out.println("adding future: " + future);
            futures.add(future);

            targetConnectionIndex = (targetConnectionIndex + 1) % numberOfConnections;
        }
    }

    public List<String> gather() throws ExecutionException, InterruptedException {
        List<String> responses = new ArrayList<>();
//        System.out.println(futures.size());

        for (Future<String> future : futures) {
            String response = future.get();
//            System.out.println(response);
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
