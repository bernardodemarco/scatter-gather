package worker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

import communication.Server;

public class Worker {
    private final Server server = new Server();
    private final List<File> textFiles = initTextFiles();

    public List<File> initTextFiles() {
        String textFilesDirectoryPath = "src/textFiles";
        int numberOfFiles = 5;
        List<File> files = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++) {
            files.add(new File(String.format("%s/text%s.txt", textFilesDirectoryPath, i + 1)));
        }

        return files;
    }

    public Map<String, Long> findOccurrences(String keyword) {
        Map<String, Long> occurrences = new HashMap<>();
        for (File file : textFiles) {
            long numberOfOccurrences = findOccurrences(keyword, file);
            occurrences.put(file.getName(), numberOfOccurrences);
        }

        return occurrences;
    }

    private long findOccurrences(String keyword, File file) {
        long count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                long occurrencesInLine = Arrays.stream(line.split("[\\s,.]"))
                        .filter(word -> word.equalsIgnoreCase(keyword))
                        .count();

                count += occurrencesInLine;
            }
        } catch (Exception e) {
            System.out.println("An error occurred while reading the queries file.");
            System.out.println(e.getMessage());
        }

        return count;
    }

    public void sendOccurrences(Map<String, Long> occurrences) {
        String output = occurrences.entrySet().stream()
                .map(entry -> String.format("%s %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(";"));

        this.server.send(output);
    }

    public Server getServer() {
        return server;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("A port number is required.");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        Worker worker = new Worker();
        worker.getServer().listen(port);

        String keyword;
        while ((keyword = worker.getServer().receive()) != null) {
            Map<String, Long> occurrences = worker.findOccurrences(keyword);
            worker.sendOccurrences(occurrences);
        }

        worker.getServer().stop();
    }
}