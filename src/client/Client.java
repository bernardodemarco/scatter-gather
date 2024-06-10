package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private List<String> queries;

    public Client() {
        this.queries = readQueries();
    }

    private void connect(String hostAddress, int port) {
        try {
            clientSocket = new Socket(hostAddress, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.println(String.format("Unknown host: %s", hostAddress));
            System.exit(1);
        } catch (IOException e) {
            System.out.println(String.format("Exception while getting %s connection input/output", hostAddress));
            System.exit(1);
        }
    }

    private void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(String.format("Exception while closing the socket and its streams."));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private List<String> readQueries() {
        List<String> queries = new ArrayList<>();

        File queriesFile = new File("src/client/queries.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(queriesFile))) {
            String query;

            while ((query = reader.readLine()) != null) {
                queries.add(query);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Queries file not found: " + queriesFile.getAbsolutePath());
            System.out.println("Current path (`.`): " + Paths.get(".").toAbsolutePath().normalize());
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the queries file.");
            System.out.println(e.getMessage());
        }

        return queries;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect("127.0.0.1", 8000);

        client.out.println("Starting to send queries");
        System.out.println(client.clientSocket.getInetAddress() + " " + client.clientSocket.getLocalPort());

        client.queries.forEach(query -> {
            client.out.println(query);

            int sleepTime = (int) (Math.random() * (2000 - 1000 + 1)) + 1000;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to send queries");
            }
        });

        client.out.println("end");
        client.stop();
    }
}