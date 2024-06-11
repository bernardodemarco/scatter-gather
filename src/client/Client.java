package client;

import communication.SocketConnection;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private SocketConnection connection;
    private List<String> queries;

    public Client() {
        this.queries = readQueries();
        connection = new SocketConnection("127.0.0.1", 8000);
        connection.connect();
    }

    public List<String> readQueries() {
        List<String> queries = new ArrayList<>();

        File queriesFile = new File("src/textFiles/queries.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(queriesFile))) {
            String query;

            while ((query = reader.readLine()) != null) {
                queries.add(query);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Queries file not found: " + queriesFile.getAbsolutePath());
            System.out.println("Current path (`.`): " + Paths.get(".").toAbsolutePath().normalize());
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the queries file.");
            System.out.println(e.getMessage());
        }

        return queries;
    }

    public void sendQueries() {
        queries.forEach(query -> {
            System.out.println(query);
            connection.send(query);

            int sleepTime = (int) (Math.random() * (2000 - 1000 + 1)) + 1000;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to send queries");
            }
        });
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.connection.send("Starting to send queries");
        client.sendQueries();
        client.connection.send("end");

        client.connection.stop();
    }
}