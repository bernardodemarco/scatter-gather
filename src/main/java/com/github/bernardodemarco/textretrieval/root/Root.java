package com.github.bernardodemarco.textretrieval.root;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.communication.scattergather.ScatterGather;
import com.github.bernardodemarco.textretrieval.communication.server.Server;
import com.github.bernardodemarco.textretrieval.root.dto.KeywordDTO;
import com.github.bernardodemarco.textretrieval.root.dto.QueryOccurrencesDTO;
import com.github.bernardodemarco.textretrieval.worker.dto.KeywordOccurrencesDTO;

import com.github.bernardodemarco.textretrieval.communication.server.TCPServer;
import com.github.bernardodemarco.textretrieval.communication.scattergather.ScatterGatherService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
    private final Logger logger = LogManager.getLogger(getClass());

    private final Gson gson = new Gson();

    private final Server server = new TCPServer();
    private final ScatterGather scatterGather = new ScatterGatherService(Arrays.asList(8001, 8002));

    private final Map<String, List<String>> fileContents;

    public Root() {
        this.fileContents = getFileContents();
    }

    public Set<String> parseQuery(String query) {
        String parsedQuery = gson.fromJson(query, QueryDTO.class).getQuery();
        return Arrays.stream(parsedQuery.split("\\s"))
                .map(KeywordDTO::new)
                .map(gson::toJson)
                .collect(Collectors.toSet());
    }

    public void handleRequests() {
        String query = server.receive();
        logger.info("Received query [{}] from client.", query);
        while (query != null && !query.equalsIgnoreCase("end")) {
            Set<String> keywords = parseQuery(query);
            scatterGather.scatter(keywords);

            List<String> workersResponses = scatterGather.gather();
            logger.debug("Received [{}] from workers.", workersResponses);
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

    private Map<String, List<String>> getFileContents() {
        Map<String, List<String>> fileContents = new HashMap<>();
        String textFilesDirectoryPath = "src/main/resources/textfiles";
        int numberOfFiles = 5;
        for (int i = 0; i < numberOfFiles; i++) {
            String fileName = String.format("text%s.txt", i + 1);
            String absoluteFileName = String.format("%s/%s", textFilesDirectoryPath, fileName);
            try {
                List<String> content = Files.readAllLines(Paths.get(absoluteFileName));
                fileContents.put(fileName, content);
            } catch (IOException e) {
                logger.error("An error occurred while reading the file [{}].", absoluteFileName, e);
                throw new RuntimeException(e);
            }
        }

        return fileContents;
    }

    public Server getServer() {
        return server;
    }

    public ScatterGather getScatterGather() {
        return scatterGather;
    }

    public static void main(String[] args) {
        Root root = new Root();
        root.getServer().listen(8000);

        root.handleRequests();

        root.getScatterGather().stop();
        root.getServer().stop();
    }
}