package root;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import communication.SocketConnection;
import communication.Server;

public class Root extends Server {
    private List<SocketConnection> workerConnections;

    public Set<String> parseQuery(String query) {
        return new HashSet<>(List.of(query.split(" ")));
    }

    public void connectToWorkers() {
        List<Integer> workerPorts = new ArrayList<>(List.of(8001, 8002));
        List<SocketConnection> connections = new ArrayList<>();

        workerPorts.forEach(port -> {
            SocketConnection connection = new SocketConnection("127.0.0.1", port);
            connection.connect();
            connections.add(connection);
        });

        workerConnections = connections;
    }

    public void closeWorkerConnections() {
        workerConnections.forEach(SocketConnection::stop);
    }

    public void sendWork() throws IOException {
        int targetConnection = 0;

        String query = this.receive();
        while (query != null && !query.equalsIgnoreCase("end")) {
            Set<String> keywords = parseQuery(query);
            for (String keyword : keywords) {
                SocketConnection connection = workerConnections.get(targetConnection);
                connection.send(keyword);
                targetConnection = (targetConnection + 1) % workerConnections.size();
            }

            receiveWorkerResponses(query);

            query = this.receive();
        }
    }

    public void receiveWorkerResponses(String query) throws IOException {
        System.out.println("query: " + query);
        for (SocketConnection workerConnection : workerConnections) {
            String response = workerConnection.receive();
            while (response != null && !response.equalsIgnoreCase("end")) {
                System.out.println(workerConnection);
                System.out.println(response);
                System.out.println("----------");
                response = workerConnection.receive();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Root root = new Root();
        root.listen(8000);

        root.connectToWorkers();
        root.sendWork();
        root.closeWorkerConnections();

        root.stop();
    }
}