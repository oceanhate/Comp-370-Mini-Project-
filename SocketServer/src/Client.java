import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    // Define all known server ports
    private static final int[] SERVER_PORTS = {8090, 8089, 8088};
    private static final int RECONNECT_INTERVAL = 5000; // 5 seconds

    /**
     * The main method is the entry point for the client application.
     * It establishes a connection to the first available server port and handles failover.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner consoleInput = new Scanner(System.in);

        while (true) {
            boolean connected = false;

            // Iterate through all known ports to find an active server
            for (int port : SERVER_PORTS) {
                try {
                    System.out.println("Attempting connection to server on port: " + port);
                    // Attempt connection on the current port
                    connectAndRun(consoleInput, port);
                    connected = true;
                    // If successful, break the port loop and remain connected until exception
                    break;
                } catch (IOException e) {
                    // Connection failed on this specific port, try the next one
                    System.out.println("Connection failed on port " + port + ". Trying next port...");
                }
            }

            // If the loop finished without connecting to any port
            if (!connected) {
                System.out.println("All known servers are down. Retrying all ports in " + RECONNECT_INTERVAL / 1000 + " seconds...");
                try {
                    Thread.sleep(RECONNECT_INTERVAL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        consoleInput.close();
    }

    /**
     * Attempts to connect to a specific port and runs the communication loop.
     * @param consoleInput Scanner for user input.
     * @param port The specific server port to connect to.
     * @throws IOException if the connection is lost or fails.
     */
    private static void connectAndRun(Scanner consoleInput, int port) throws IOException {
        // Use the specific port passed as argument
        try (Socket socket = new Socket(HOST, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on port " + port + ". Type messages to send. Type 'exit' to quit.");

            // Thread to read from server
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("server said: " + serverMessage);
                    }
                } catch (IOException e) {
                    // This is expected when the server closes the connection
                    System.out.println("Server disconnected from reader thread.");
                }
            });

            readerThread.setDaemon(true);
            readerThread.start();

            // Main thread reads user input and sends to server
            while (true) {
                if (!consoleInput.hasNextLine()) {
                    break; // EOF on stdin
                }

                String message = consoleInput.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Client disconnected by user.");
                    System.exit(0);
                }

                out.println(message);

                // Force flush and check for errors immediately
                out.flush();

                // If write fails, throw exception to trigger connection retry loop in main
                if (out.checkError()) {
                    throw new IOException("Connection lost during send operation.");
                }
            }
        }
    }
}
