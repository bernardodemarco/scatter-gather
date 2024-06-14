package communication;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.UnknownHostException;

public class ClientConnection {
    private final String hostAddress;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientConnection(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(hostAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.printf("Unknown host: %s%n", hostAddress);
            System.exit(1);
        } catch (IOException e) {
            System.out.printf("Exception while getting %s connection input/output%n", hostAddress);
            System.exit(1);
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Exception while closing the socket and its streams.");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public synchronized String receive() {
        try {
            return (in != null) ? in.readLine() : null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Connection[%s:%d]", hostAddress, port);
    }
}