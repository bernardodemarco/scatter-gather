package root;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import communication.Server;
import scatterGather.ScatterGatherService;

public class Root extends Server {
    private ScatterGatherService scatterGather = new ScatterGatherService(List.of(8001, 8002));

    public Set<String> parseQuery(String query) {
        return new HashSet<>(List.of(query.split("\\s")));
    }

    public void handleRequests() throws IOException, ExecutionException, InterruptedException {
        String query = this.receive();
        while (query != null && !query.equalsIgnoreCase("end")) {
            this.sendJobs(query);
            List<String> responses = this.receiveJobsResponse();
            this.send(this.generateClientResponse(responses).toString());

            query = this.receive();
        }
    }

    public Map<String, Integer> generateClientResponse(List<String> rawResponses) {
//        this.parseClientResponses(rawResponses);
//        String textFilesDirectoryPath = "src/textFiles";
        Map<String, Integer> responses = new HashMap<>();

        for (String rawResponse : rawResponses) {
            String[] rawOccurrences = rawResponse.split(";");
            for (String rawOccurrence : rawOccurrences) {
                String[] occurrence = rawOccurrence.split("\\s");
                String fileName = occurrence[0];
                int numberOfOccurrences = Integer.parseInt(occurrence[1]);

                if (numberOfOccurrences == 0) {
                    continue;
                }

                if (responses.containsKey(fileName)) {
                    responses.put(fileName, responses.get(fileName) + numberOfOccurrences);
                } else {
                    responses.put(fileName, numberOfOccurrences);
                }
            }
        }

        return responses;
    }

//    public String parseClientResponses(List<String> responses) {
//
//    }

    public List<String> receiveJobsResponse() throws ExecutionException, InterruptedException {
        return scatterGather.gather();
    }

    public void sendJobs(String query) {
            Set<String> keywords = parseQuery(query);
            scatterGather.scatter(keywords);
    }


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Root root = new Root();
        root.listen(8000);

        root.handleRequests();

        root.scatterGather.stopService();
        root.stop();
    }
}