import java.io.*;
import java.net.*;

public class HeartBeatMonitor {

    // Inner class to handle connection to one server
    static class ServerHandler implements Runnable {
        private final String host;
        private final int port;
        private final String serverName;

        public ServerHandler(String host, int port, String serverName) {
            this.host = host;
            this.port = port;
            this.serverName = serverName;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("Connected to " + serverName + " (" + host + ":" + port + ").");

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(serverName + " -> " + line);
                }

            } catch (IOException e) {
                System.err.println("Connection to " + serverName + " failed: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Create threads for multiple servers
        Thread server1 = new Thread(new ServerHandler("localhost", 111, "Server 1"));
        Thread server2 = new Thread(new ServerHandler("localhost", 222, "Server 2"));
        Thread server3 = new Thread(new ServerHandler("localhost", 333, "Server 3"));


        // Start both listeners
        server1.start();
        server2.start();
        server3.start();

        System.out.println("Heartbeat Monitor started. Listening to multiple servers...");
    }
}