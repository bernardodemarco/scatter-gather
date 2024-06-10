package worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Worker {
    private ServerSocket workerServerSocket;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private void listen(int port) {
        try {
            workerServerSocket = new ServerSocket(port);
            clientSocket = workerServerSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println(String.format("Exception while listening on port %s or listening for a connection.", port));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            workerServerSocket.close();
        } catch (IOException e) {
            System.out.println(String.format("Exception while closing the sockets and their streams."));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("A port number is required.");
            System.exit(1);
        }

        Worker worker = new Worker();
        int port = Integer.parseInt(args[0]);
        worker.listen(port);
        while (true) {
            String keyword = worker.in.readLine();
            System.out.println("Received: " + keyword);

            if (worker.in.readLine() == null) {
                break;
            }
        }

        worker.stop();
//        System.out.println(root.clientSocket.getLocalAddress() + " " + root.clientSocket.getPort());

    }
}