package worker;

import communication.Server;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

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

        return occurrences;
    }

    private long findOccurrences(String keyword, File file) {
        long count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                count = Arrays.stream(line.split("[\\s,.]"))
                        .filter(word -> word.equalsIgnoreCase(keyword))
                        .count();
            }
        } catch (Exception e) {
            System.out.println("An error occurred while reading the queries file.");
            System.out.println(e.getMessage());
        }

        return count;
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
            System.out.println("Received: " + keyword);
            System.out.println(worker.findOccurrences(keyword));
        }

        worker.stop();
    }
}