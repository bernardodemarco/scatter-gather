package communication;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnection implements SocketCommunicable {
    private String hostAddress;
    private int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public SocketConnection(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void connect() {
        try {
            socket = new Socket(hostAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            socket.close();
        } catch (IOException e) {
            System.out.println(String.format("Exception while closing the socket and its streams."));
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public synchronized String receive() throws IOException {
        return (in != null) ? in.readLine() : null;
    }

    public Socket getSocket() {
        return socket;
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