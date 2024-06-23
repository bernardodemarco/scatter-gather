package com.github.bernardodemarco.textretrieval.root;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.root.dto.KeywordDTO;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;
import com.github.bernardodemarco.textretrieval.worker.dto.KeywordOccurrencesDTO;

import com.github.bernardodemarco.textretrieval.communication.Server;
import com.github.bernardodemarco.textretrieval.communication.ScatterGatherService;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Root {
    private final Server server = new Server();
    private final ScatterGatherService scatterGather = new ScatterGatherService(Arrays.asList(8001, 8002));
    private final Gson gson = new Gson();
    private final Logger logger = LogManager.getLogger(getClass());

    public Set<String> parseQuery(String query) {
        String parsedQuery = gson.fromJson(query, QueryDTO.class).getQuery();
        return Arrays.stream(parsedQuery.split("\\s"))
                .map(KeywordDTO::new)
                .map(gson::toJson)
                .collect(Collectors.toSet());
    }

    public void handleRequests() {
        String query = server.receive();
        logger.debug("Received query [{}] from client.", query);
        while (query != null && !query.equalsIgnoreCase("end")) {
            Set<String> keywords = parseQuery(query);
            scatterGather.scatter(keywords);

            List<String> workersResponses = scatterGather.gather();
            server.send(generateClientResponse(workersResponses));

            query = server.receive();
        }
    }

    private String generateClientResponse(List<String> workersResponses) {
        List<KeywordOccurrencesDTO> keywordsOccurrences = parseKeywordsResponses(workersResponses);
        List<QueryOccurrencesDTO> queryOccurrences = getQueryOccurrences(keywordsOccurrences);
        return gson.toJson(queryOccurrences);
    }

    private List<KeywordOccurrencesDTO> parseKeywordsResponses(List<String> keywordsResponses) {
        TypeToken<List<KeywordOccurrencesDTO>> responseType = new TypeToken<List<KeywordOccurrencesDTO>>(){};
        return keywordsResponses.stream()
                .map((response) -> gson.fromJson(response, responseType))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<QueryOccurrencesDTO> getQueryOccurrences(List<KeywordOccurrencesDTO> keywordsOccurrences) {
        Map<String, List<KeywordOccurrencesDTO>> occurrencesGroupedByText = keywordsOccurrences.stream()
                .collect(Collectors.groupingBy(KeywordOccurrencesDTO::getFileName));

        List<QueryOccurrencesDTO> clientResponse = new ArrayList<>();
        for (Map.Entry<String, List<KeywordOccurrencesDTO>> entry : occurrencesGroupedByText.entrySet()) {
            long occurrencesInFile = occurrencesGroupedByText.get(entry.getKey())
                    .stream()
                    .mapToLong(KeywordOccurrencesDTO::getOccurrences)
                    .sum();

            clientResponse.add(new QueryOccurrencesDTO(entry.getKey(), occurrencesInFile));
        }
        
        return clientResponse;
    }

    public Server getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ScatterGatherService getScatterGather() {
        return scatterGather;
    }

    public static void main(String[] args) {
        Root root = new Root();
        root.getServer().listen(8000);
        root.getLogger().debug("ROOT server listening on [{}:{}]", "127.0.0.1", 8000);

        root.handleRequests();

        root.getScatterGather().stopService();
        root.getServer().stop();
    }
}