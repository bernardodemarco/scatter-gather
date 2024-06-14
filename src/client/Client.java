package client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;

import communication.SocketConnection;

public class Client {
    private final SocketConnection connection;
    private final List<String> queries;

    public Client() {
        this.queries = readQueries();
        connection = new SocketConnection("127.0.0.1", 8000);
        connection.connect();
    }

    private List<String> readQueries() {
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

    private String getResponse() {
        return connection.receive();
    }

    private void sendQuery(String query) {
        connection.send(query);
    }

    private void displayResponse(String response) {
        System.out.println(response);
    }

    private void sleep() {
        int sleepTime = (int) (Math.random() * (2000 - 1000 + 1)) + 1000;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting to send queries");
        }
    }

    public void run() {
        queries.forEach(query -> {
            sendQuery(query);
            String response = getResponse();
            displayResponse(response);
            sleep();
        });
    }

    public void closeServerConnection() {
        connection.send("end");
        connection.stop();
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
        client.closeServerConnection();
    }
}