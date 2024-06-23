package com.github.bernardodemarco.textretrieval.worker;

import com.github.bernardodemarco.textretrieval.communication.server.Server;
import com.github.bernardodemarco.textretrieval.communication.server.TCPServer;

import com.github.bernardodemarco.textretrieval.root.dto.KeywordDTO;
import com.github.bernardodemarco.textretrieval.worker.dto.KeywordOccurrencesDTO;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Worker {
    private final Logger logger = LogManager.getLogger(getClass());

    private final Server server = new TCPServer();
    private final List<File> textFiles = initTextFiles();
    
    private final Gson gson = new Gson();

    public Worker(int port) {
        this.server.listen(port);
    }

    private List<File> initTextFiles() {
        String textFilesDirectoryPath = "src/main/resources/textfiles";
        int numberOfFiles = 5;
        List<File> files = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++) {
            files.add(new File(String.format("%s/text%s.txt", textFilesDirectoryPath, i + 1)));
        }

        return files;
    }

    public List<KeywordOccurrencesDTO> findOccurrences(String keyword) {
        return textFiles
                .stream()
                .map((file -> {
                    long occurrences = findOccurrences(keyword, file);
                    return new KeywordOccurrencesDTO(file.getName(), occurrences);
                }))
                .filter(keywordOccurrencesDTO -> keywordOccurrencesDTO.getOccurrences() > 0)
                .collect(Collectors.toList());
    }

    private long findOccurrences(String keyword, File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            long count = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                long occurrencesInLine = Arrays.stream(line.split("[\\s,.]"))
                        .filter(word -> word.equalsIgnoreCase(keyword))
                        .count();

                count += occurrencesInLine;
            }

            logger.debug("Keyword [{}] has appeared [{}] times in [{}]", keyword, count, file.getName());
            return count;
        } catch (Exception e) {
            String errorMessage = "An error occurred while reading the queries file.";
            logger.error(errorMessage, e);
            throw new RuntimeException(e);
        }
    }

    private String parseKeyword(String keywordJSON) {
        return gson.fromJson(keywordJSON, KeywordDTO.class).getKeyword();
    }

    public void handleRequests() {
        String keyword;
        while ((keyword = server.receive()) != null) {
            logger.info("Received keyword [{}].", keyword);
            String parsedKeyword = parseKeyword(keyword);
            List<KeywordOccurrencesDTO> occurrences = findOccurrences(parsedKeyword);
            server.send(gson.toJson(occurrences));
        }

        server.stop();
    }
}