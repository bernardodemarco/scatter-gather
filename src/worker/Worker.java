package worker;

import communication.Server;

import java.io.IOException;

public class Worker extends Server {
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
        }

        worker.stop();
    }
}