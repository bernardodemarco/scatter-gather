package com.github.bernardodemarco.textretrieval.root;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import com.github.bernardodemarco.textretrieval.client.dto.QueryDTO;
import com.github.bernardodemarco.textretrieval.communication.ScatterGatherService;
import com.github.bernardodemarco.textretrieval.communication.Server;
import com.google.gson.Gson;

public class Root {
    private final Server server = new Server();
    private final ScatterGatherService scatterGather = new ScatterGatherService(List.of(8001, 8002));
    private final Gson gson = new Gson();

    public Set<String> parseQuery(String query) {
        String parsedQuery = gson.fromJson(query, QueryDTO.class).getQuery();
        return new HashSet<>(List.of(parsedQuery.split("\\s")));
    }

    public void handleRequests() {
        String query = this.server.receive();
        while (query != null && !query.equalsIgnoreCase("end")) {
            Set<String> keywords = parseQuery(query);
            this.sendJobs(keywords);
            List<String> responses = this.receiveJobsResponse();

            //
            this.server.send(this.generateClientResponse(responses).toString());

            query = this.server.receive();
        }
    }

    //
    public Map<String, Integer> generateClientResponse(List<String> rawResponses) {
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

    public List<String> receiveJobsResponse() {
        return scatterGather.gather();
    }

    public void sendJobs(Set<String> keywords) {
        scatterGather.scatter(keywords);
    }

    public Server getServer() {
        return server;
    }

    public ScatterGatherService getScatterGather() {
        return scatterGather;
    }

    public static void main(String[] args) {
        Root root = new Root();
        root.getServer().listen(8000);

        root.handleRequests();

        root.getScatterGather().stopService();
        root.getServer().stop();
    }
}