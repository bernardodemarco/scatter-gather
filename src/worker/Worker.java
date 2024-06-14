package worker;

import communication.Server;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Worker extends Server {
    private List<File> textFiles = initTextFiles();

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

        System.out.println(keyword + " -> " + occurrences);
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
//                .filter(entry -> entry.getValue() > 0)
                .map(entry -> String.format("%s %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(";"));

        System.out.println(output);
//        if (!output.isBlank()) {
        this.send(output);
//        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("A port number is required.");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        Worker worker = new Worker();
        worker.listen(port);

        String keyword;
        while ((keyword = worker.receive()) != null) {
//            System.out.println(keyword);
            Map<String, Long> occurrences = worker.findOccurrences(keyword);
//            System.out.println(keyword + " " + occurrences); //ok
            worker.sendOccurrences(occurrences);
        }

        worker.stop();
    }
}