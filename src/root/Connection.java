package root;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {
    private String hostAddress;
    private int port;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Connection(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void connect() {
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

    public void stop() {
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

    public void send(String message) {
        out.println(message + "\n");
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    @Override
    public String toString() {
        return String.format("Connection[%s:%d]", hostAddress, port);
    }
}