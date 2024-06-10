package root;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Root {
    private ServerSocket rootServerSocket;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private void listen(int port) {
        try {
            rootServerSocket = new ServerSocket(port);
            clientSocket = rootServerSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println(String.format("Exception while listening on port %s or listening for a connection.", port));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            rootServerSocket.close();
        } catch (IOException e) {
            System.out.println(String.format("Exception while closing the sockets and their streams."));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private Set<String> parseQuery(String query) {
        Set<String> keywords = new HashSet<>(List.of(query.split(" ")));
        System.out.println(keywords);
        return keywords;
    }

    private List<Connection> connectToWorkers() {
        List<Integer> workerPorts = new ArrayList<>(List.of(8001, 8002));
        List<Connection> connections = new ArrayList<>();

        workerPorts.forEach(port -> {
            Connection connection = new Connection("127.0.0.1", port);
            connection.connect();
            connections.add(connection);
        });

        return connections;
    }

    private void closeWorkerConnections(List<Connection> connections) {
        connections.forEach(Connection::stop);
    }

    public static void main(String[] args) throws IOException {
        Root root = new Root();
        root.listen(8000);
        System.out.println(root.clientSocket.getLocalAddress() + " " + root.clientSocket.getPort());

        List<Connection> workerConnections = root.connectToWorkers();
        int targetConnection = 0;

        String query = root.in.readLine();
        while (!query.equalsIgnoreCase("end")) {
            System.out.println("Received: " + query);
            Set<String> keywords = root.parseQuery(query);
            for (String keyword : keywords) {
                System.out.println("sending: " + keyword);
                Connection connection = workerConnections.get(targetConnection);
                System.out.println(connection);
                connection.send(keyword);
                targetConnection = (targetConnection + 1) % workerConnections.size();
            }

            query = root.in.readLine();
        }

        root.closeWorkerConnections(workerConnections);
        root.stop();
    }
}