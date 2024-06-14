package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void listen(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.printf("Exception while listening on port %s or listening for a connection.%n", port);
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Exception while closing the sockets and their streams.");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String receive() {
        try {
            return (in != null) ? in.readLine() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
