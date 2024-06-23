package com.github.bernardodemarco.textretrieval.worker;

import com.github.bernardodemarco.textretrieval.communication.Server;
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
    private final Server server = new Server();
    private final List<File> textFiles = initTextFiles();
    private final Gson gson = new Gson();
    private final Logger logger = LogManager.getLogger(getClass());

    public List<File> initTextFiles() {
        String textFilesDirectoryPath = "src/main/java/com/github/bernardodemarco/textretrieval/textfiles";
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

            return count;
        } catch (Exception e) {
            String errorMessage = "An error occurred while reading the queries file";
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void sendOccurrencesResponse(List<KeywordOccurrencesDTO> occurrences) {
        String response = gson.toJson(occurrences);
        server.send(response);
    }

    public String parseKeyword(String keywordJSON) {
        return gson.fromJson(keywordJSON, KeywordDTO.class).getKeyword();
    }

    public Server getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("A port number is required.");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        Worker worker = new Worker();
        worker.getServer().listen(port);
        worker.getLogger().debug("WORKER server listening on [{}:{}]", "127.0.0.1", port);

        String keyword;
        while ((keyword = worker.getServer().receive()) != null) {
            String parsedKeyword = worker.parseKeyword(keyword);
            List<KeywordOccurrencesDTO> occurrences = worker.findOccurrences(parsedKeyword);
            worker.sendOccurrencesResponse(occurrences);
        }

        worker.getServer().stop();
    }
}