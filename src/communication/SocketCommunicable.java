package communication;

import java.io.IOException;

public interface SocketCommunicable {
    void send(String message);
    String receive() throws IOException;
}
