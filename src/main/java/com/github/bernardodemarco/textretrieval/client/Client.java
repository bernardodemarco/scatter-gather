package com.github.bernardodemarco.textretrieval.client;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.communication.ClientConnection;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;

import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.Logger;

public class Client {
    private final Logger logger = LogManager.getLogger(Client.class);

    private final Gson gson = new Gson();
    private final Gson gsonFormatter = new GsonBuilder().setPrettyPrinting().create();

    private final ClientConnection connection;
    private final List<String> queries;

    public Client() {
        this.queries = readQueries();
        connection = new ClientConnection("127.0.0.1", 8000);
        logger.debug("Connecting to ROOT server.");
        connection.connect();
    }

    private List<String> readQueries() {
        String queriesFilePath = "src/main/java/com/github/bernardodemarco/textretrieval/client/queries.json";
        logger.debug("Reading queries file at [{}].", queriesFilePath);

        try (JsonReader fileReader = new JsonReader(new FileReader(queriesFilePath))) {
            return Arrays.asList(gson.fromJson(fileReader, String[].class));
        } catch (IOException e) {
            String errorMessage = String.format(
                    "Queries file not found [%s], current path [%s]",
                    queriesFilePath, Paths.get(".").toAbsolutePath().normalize()
            );

            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private String getResponse() {
        logger.debug("Waiting for a response.");
        return connection.receive();
    }

    private void sendQuery(String query) {
        String payload = gson.toJson(new QueryDTO(query));
        logger.debug("Sending query [{}].", payload);
        connection.send(payload);
    }

    private void displayResponse(String response) {
        String formattedJSONOutput = gsonFormatter.toJson(gsonFormatter.fromJson(response, QueryOccurrencesDTO[].class));
        logger.info("Received query response [{}].", formattedJSONOutput);
    }

    private void sleep() {
        int sleepTime = (int) (Math.random() * (2000 - 1000 + 1)) + 1000;
        try {
            logger.debug("Sleeping for {} seconds.", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to send queries.", e);
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