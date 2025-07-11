package com.github.bernardodemarco.textretrieval.client;

import com.github.bernardodemarco.textretrieval.utils.FileUtils;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;

import com.github.bernardodemarco.textretrieval.communication.client.ClientConnection;
import com.github.bernardodemarco.textretrieval.communication.client.TCPClientConnection;

import java.util.List;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Client {
    private final Logger logger = LogManager.getLogger(getClass());

    private final Gson gson = new Gson();
    private final Gson gsonFormatter = new GsonBuilder().setPrettyPrinting().create();

    private final ClientConnection connection;
    private final List<String> queries;

    public Client() {
        this.queries = Arrays.asList(FileUtils.readJSONFile("/client/queries.json", String[].class));

        Properties properties = FileUtils.readPropertiesFile("/client/client.properties");
        connection = new TCPClientConnection(properties.getProperty("root.server.ip"), Integer.parseInt(properties.getProperty("root.server.port")));

        logger.debug("Connecting to ROOT server.");
        connection.connect();
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
            logger.debug("Sleeping for {} milliseconds.", sleepTime);
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

        connection.stop();
    }

    public static void main(String[] args) {
        new Client().run();
    }
}