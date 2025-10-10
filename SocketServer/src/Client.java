import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    // Define all known server ports
    private static final List<Integer> SERVER_PORTS_LIST = new java.util.ArrayList<>(Arrays.asList(8090, 8089, 8088));
    private static final int RECONNECT_INTERVAL = 5000; // 5 seconds

    /**
     * The main method is the entry point for the client application.
     * It establishes a connection to the first available server port and handles failover.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SERVER_PORTS_LIST.sort(Comparator.reverseOrder());

        Scanner consoleInput = new Scanner(System.in);

        while (true) {
            boolean connected = false;

            // Iterate through all known ports to find an active server
            for (int port : SERVER_PORTS_LIST) {
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
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
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
                    // Check if interrupted before reading
                    while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                        System.out.println("from server: " + serverMessage);
                    }
                } catch (IOException e) {
                    // This is expected when the socket is closed by the server or by the main thread (during 'exit')
                    // Only print if the thread was not interrupted (i.e., server initiated the close)
                    if (!Thread.currentThread().isInterrupted()) {
                        System.out.println("Server connection lost.");
                    }
                }
                // Confirmation that the reader thread has finished its work
                System.out.println("Reader thread finished.");
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
                    System.out.println("Client disconnecting by user.");

                    // 1. Send interrupt signal to the reader thread
                    readerThread.interrupt();
                    break; // Exit the user input loop
                }

                out.println(message);

                // Force flush and check for errors immediately
                out.flush();

                // If write fails, throw exception to trigger connection retry loop in main
                if (out.checkError()) {
                    throw new IOException("Connection lost during send operation.");
                }
            }

            // 2. WAIT FOR READER THREAD TO JOIN/EXIT GRACEFULLY
            // This is correctly placed after the 'while(true)' loop finishes (by 'break')
            try  {
                readerThread.join(100); // Wait a short time for thread to complete
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        } // The try-with-resources block automatically closes the socket here.
    }
}
