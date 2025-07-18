package com.github.bernardodemarco.textretrieval.root;

import com.github.bernardodemarco.textretrieval.communication.server.Server;
import com.github.bernardodemarco.textretrieval.communication.server.TCPServer;
import com.github.bernardodemarco.textretrieval.communication.scattergather.ScatterGather;
import com.github.bernardodemarco.textretrieval.communication.scattergather.ScatterGatherService;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.root.dto.KeywordDTO;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;
import com.github.bernardodemarco.textretrieval.worker.dto.KeywordOccurrencesDTO;

import com.github.bernardodemarco.textretrieval.utils.FileUtils;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
import java.util.AbstractMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Root {
    private final Logger logger = LogManager.getLogger(getClass());

    private final Gson gson = new Gson();

    private final Server server = new TCPServer();
    private final ScatterGather scatterGather;

    private final Map<String, List<String>> fileContents;

    public Root() {
        Properties properties = FileUtils.readPropertiesFile("/root/root.properties");
        this.server.listen(Integer.parseInt(properties.getProperty("root.server.port")));
        this.scatterGather = new ScatterGatherService(this.getWorkersAddresses(properties));
        this.fileContents = getFileContents();
    }

    private List<Map.Entry<String, Integer>> getWorkersAddresses(Properties properties) {
        List<Map.Entry<String, Integer>> addresses = new ArrayList<>();
        int numberOfWorkers = Integer.parseInt(properties.getProperty("workers.count"));
        for (int i = 0; i < numberOfWorkers; i++) {
            String ip = properties.getProperty(String.format("worker.%s.server.ip", i + 1));
            Integer port = Integer.parseInt(properties.getProperty(String.format("worker.%s.server.port", i + 1)));
            addresses.add(new AbstractMap.SimpleImmutableEntry<>(ip, port));
        }

        return addresses;
    }

    private Map<String, List<String>> getFileContents() {
        Map<String, List<String>> fileContents = new HashMap<>();
        int numberOfFiles = 5;
        for (int i = 0; i < numberOfFiles; i++) {
            String fileName = String.format("text%s.txt", i + 1);
            List<String> content = FileUtils.readTextFile("/textfiles/" + fileName);
            fileContents.put(fileName, content);
        }

        return fileContents;
    }

    public void handleRequests() {
        String query;
        while ((query = server.receive()) != null) {
            logger.info("Received query [{}] from client.", query);
            Set<String> keywords = parseQuery(query);
            scatterGather.scatter(keywords);

            List<String> workersResponses = scatterGather.gather();
            logger.debug("Received [{}] from workers.", workersResponses);
            server.send(generateClientResponse(workersResponses));
        }

        scatterGather.stop();
        server.stop();
    }

    private Set<String> parseQuery(String query) {
        String parsedQuery = gson.fromJson(query, QueryDTO.class).getQuery();
        return Arrays.stream(parsedQuery.split("\\s"))
                .map(KeywordDTO::new)
                .map(gson::toJson)
                .collect(Collectors.toSet());
    }

    private String generateClientResponse(List<String> workersResponses) {
        List<KeywordOccurrencesDTO> keywordsOccurrences = parseKeywordsResponses(workersResponses);
        List<QueryOccurrencesDTO> queryOccurrences = getQueryOccurrences(keywordsOccurrences);
        return gson.toJson(queryOccurrences);
    }

    private List<KeywordOccurrencesDTO> parseKeywordsResponses(List<String> keywordsResponses) {
        logger.debug("Parsing keywords responses.");
        TypeToken<List<KeywordOccurrencesDTO>> responseType = new TypeToken<List<KeywordOccurrencesDTO>>(){};
        return keywordsResponses.stream()
                .map((response) -> gson.fromJson(response, responseType))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<QueryOccurrencesDTO> getQueryOccurrences(List<KeywordOccurrencesDTO> keywordsOccurrences) {
        logger.debug("Calculating query occurrences.");
        Map<String, List<KeywordOccurrencesDTO>> occurrencesGroupedByText = keywordsOccurrences.stream()
                .collect(Collectors.groupingBy(KeywordOccurrencesDTO::getFileName));

        List<QueryOccurrencesDTO> clientResponse = new ArrayList<>();
        for (Map.Entry<String, List<KeywordOccurrencesDTO>> entry : occurrencesGroupedByText.entrySet()) {
            String fileName = entry.getKey();
            long occurrencesInFile = occurrencesGroupedByText.get(fileName)
                    .stream()
                    .mapToLong(KeywordOccurrencesDTO::getOccurrences)
                    .sum();

            clientResponse.add(new QueryOccurrencesDTO(fileName, fileContents.get(fileName), occurrencesInFile));
        }
        
        return clientResponse;
    }

    public static void main(String[] args) {
        new Root().handleRequests();
    }
}