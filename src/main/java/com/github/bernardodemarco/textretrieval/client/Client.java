package com.github.bernardodemarco.textretrieval.client;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.communication.ClientConnection;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;

import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class Client {
    private final ClientConnection connection;
    private final List<String> queries;
    private final Gson gson = new Gson();
    private final Gson gsonFormatter = new GsonBuilder().setPrettyPrinting().create();

    public Client() {
        this.queries = readQueries();
        connection = new ClientConnection("127.0.0.1", 8000);
        connection.connect();
    }

    private List<String> readQueries() {
        String queriesFilePath = "src/main/java/com/github/bernardodemarco/textretrieval/client/queries.json";
        try (JsonReader fileReader = new JsonReader(new FileReader(queriesFilePath))) {
            return Arrays.asList(gson.fromJson(fileReader, String[].class));
        } catch (IOException e) {
            String errorMessage = String.format(
                    "Queries file not found [%s], current path [%s]",
                    queriesFilePath, Paths.get(".").toAbsolutePath().normalize()
            );

            throw new RuntimeException(errorMessage, e);
        }
    }

    private String getResponse() {
        return connection.receive();
    }

    private void sendQuery(String query) {
        String payload = gson.toJson(new QueryDTO(query));
        System.out.println("PAYLOAD = " + payload);
        connection.send(payload);
    }

    private void displayResponse(String response) {
        String formattedJSONOutput = gsonFormatter.toJson(gsonFormatter.fromJson(response, QueryOccurrencesDTO[].class));
        System.out.println(formattedJSONOutput);
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