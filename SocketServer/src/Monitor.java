import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Monitor {

    // --- GLOBAL CONSTANTS ---
    private static final int PRIMARY_PORT_DEFAULT = 8090;
    private static final int HEARTBEAT_PORT = 9000;
    private static final int CLIENT_API_PORT = 9001;

    // Volatile field to track the current Primary PORT
    private static volatile int currentPrimaryPort = PRIMARY_PORT_DEFAULT;

    // List of all server ports, sorted descending (highest port first)
    private static final List<Integer> ALL_SERVER_PORTS_DESC = new ArrayList<>(
            // Explicitly define all known ports here
            Arrays.asList(8090, 8089, 8088)
    );

    static {
        // Ensure the list is sorted in descending order for promotion priority
        ALL_SERVER_PORTS_DESC.sort(Collections.reverseOrder());
    }


    public static void main(String[] args) {
        final int TIMEOUT_MS = 5000;

        // Map key is now the Port Number
        final Map<Integer, Long> lastSeen = new ConcurrentHashMap<>();
        final Set<Integer> alive = ConcurrentHashMap.newKeySet();

        // 1. START THE BACKGROUND DEATH CHECKER THREAD
        Thread checkerThread = new Thread(() -> runDeathChecker(lastSeen, alive, TIMEOUT_MS));
        checkerThread.setDaemon(true);
        checkerThread.start();

        // 2. START THE CLIENT API LISTENER THREAD
        Thread clientApiThread = new Thread(() -> runClientApiListener());
        clientApiThread.setDaemon(true);
        clientApiThread.start();

        // 3. MAIN THREAD ACCEPTS HEARTBEATS
        try (ServerSocket ss = new ServerSocket(HEARTBEAT_PORT)) {
            System.out.println("Monitor listening for heartbeats on port " + HEARTBEAT_PORT);
            System.out.println("Monitor listening for client API requests on port " + CLIENT_API_PORT);

            while (true) {
                try {
                    Socket s = ss.accept();
                    handleHeartbeat(s, lastSeen, alive);
                } catch (IOException e) {
                    System.err.println("Monitor heartbeat reception failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Monitor failed to start: " + e.getMessage());
        }
    }

    /**
     * Handles incoming heartbeats, expecting [Port #] | [timestamp] format.
     */
    private static void handleHeartbeat(Socket s, Map<Integer, Long> lastSeen, Set<Integer> alive) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line = in.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\|");

                if (parts.length == 2) {
                    int port;
                    long sentTimestamp;

                    try {
                        port = Integer.parseInt(parts[0].trim());
                        sentTimestamp = Long.parseLong(parts[1].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port or timestamp received: " + line);
                        return;
                    }

                    if (ALL_SERVER_PORTS_DESC.contains(port)) {
                        long now = System.currentTimeMillis();
                        lastSeen.put(port, now);

                        if (!alive.contains(port)) {
                            alive.add(port);
                        }

                        // Output format requested: Heartbeat received from [port #] + timestamp
                        System.out.println("Heartbeat received from " + port + " (sent at: " + sentTimestamp + ")");
                    } else {
                        System.err.println("Heartbeat received from unknown port: " + port);
                    }
                } else {
                    System.err.println("Malformed heartbeat received: " + line);
                }
            }
        } catch (IOException e) {
            // Ignore socket read errors
        } finally {
            try { s.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Listens on the API port and sends the current primary port number.
     */
    private static void runClientApiListener() {
        try (ServerSocket apiSocket = new ServerSocket(CLIENT_API_PORT)) {
            while (true) {
                try {
                    Socket client = apiSocket.accept();
                    handleClientApiRequest(client);
                } catch (IOException e) {
                    System.err.println("Monitor API reception failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Monitor API failed to start: " + e.getMessage());
        }
    }

    private static void handleClientApiRequest(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            String request = in.readLine();
            if ("GET_PRIMARY".equals(request)) {
                // Send the current Primary port number
                out.println(currentPrimaryPort > 0 ? currentPrimaryPort : 0);
            } else {
                out.println("ERROR: Invalid Request");
            }
        } catch (IOException e) {
            System.err.println("Error handling client API request: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Checks for dead servers and initiates promotion if the primary fails or is unset.
     */
    private static void runDeathChecker(Map<Integer, Long> lastSeen, Set<Integer> alive, final int TIMEOUT_MS) {
        final int CHECK_INTERVAL = 2000;

        while (true) {
            try {
                Thread.sleep(CHECK_INTERVAL);
                long now = System.currentTimeMillis();

                if (alive.size() == ALL_SERVER_PORTS_DESC.size() && currentPrimaryPort != PRIMARY_PORT_DEFAULT) {
                    // All servers are back online, and the current primary is NOT the highest port (8090).
                    System.out.println("\n*** FULL HEALTH RESTORED. FORCING PRIMARY RESET TO HIGHEST PORT (" + PRIMARY_PORT_DEFAULT + ") ***");
                    // Force a promotion attempt to the highest port
                    currentPrimaryPort = 0; // Set to 0 to trigger the promotion logic below
                }

                // Flag is true if the current primary port is detected as dead
                boolean primaryFailed = false;

                // --- 1. DETECT DEATHS AND PURGE STALE ENTRIES ---
                for (int port : new HashSet<>(lastSeen.keySet())) {
                    Long t = lastSeen.get(port);
                    if (t == null) continue;

                    long timeLapsed = now - t;

                    if (timeLapsed > TIMEOUT_MS) {
                        if (alive.remove(port)) {
                            if (port == currentPrimaryPort) {
                                primaryFailed = true;
                            }
                            System.err.println("!!! Server on Port " + port + " is DEAD (no heartbeat for " + timeLapsed + "ms) !!!");
                        }
                        lastSeen.remove(port);
                    }
                }

                // --- 2. TRIGGER PROMOTION IF PRIMARY IS DOWN (Highest Port Wins) ---
                // The condition is met if the primary failed OR if the primary is currently unset (<= 0)
                if (primaryFailed || currentPrimaryPort <= 0) {

                    if (currentPrimaryPort <= 0) {
                        System.out.println("\n*** PRIMARY IS UNSET. INITIATING RE-PROMOTION ***");
                    } else {
                        System.out.println("\n*** PRIMARY SERVER FAILED. INITIATING FAILOVER ***");
                    }

                    int newPrimaryPort = 0;

                    // CORE FAILOVER LOGIC: Find the ABSOLUTE highest port server that is ALIVE AND ACCEPTS CONNECTION.
                    for (int port : ALL_SERVER_PORTS_DESC) {

                        // 1. Check if candidate is in the 'alive' set
                        if (alive.contains(port)) {

                            try (Socket failoverSocket = new Socket("localhost", port);
                                 PrintWriter out = new PrintWriter(failoverSocket.getOutputStream(), true)) {

                                // --- SUCCESSFUL PROMOTION ---
                                out.println("PROMOTE");
                                System.out.println("-> SENT PROMOTE COMMAND to Port " + port);

                                // Promotion Successful: Update and break loop
                                currentPrimaryPort = port;
                                newPrimaryPort = port;

                                // Log the client notification (as requested)
                                System.out.println("-> CLIENT NOTIFICATION: New Primary is Port " + currentPrimaryPort);

                                break;

                            } catch (IOException promotionEx) {
                                // --- PROMOTION FAILURE (e.g., Connection Refused) ---
                                System.err.println("-> ERROR: Failed to promote Port " + port + ". Server is ALIVE but won't accept promotion: " + promotionEx.getMessage());
                                // Do NOT remove from alive, just move to the next highest port.
                            }
                        }
                    } // End of port iteration loop

                    // --- 3. FINAL STATUS CHECK AFTER PROMOTION ATTEMPTS ---
                    if (newPrimaryPort == 0) {
                        System.err.println("-> FATAL: No available server could be promoted.");
                        currentPrimaryPort = 0; // Set to 0 to indicate no active primary
                    }
                }

                // --- 4. REPORT STATUS SUMMARY (Requested Port-Based Format) ---

                StringBuilder statusLine = new StringBuilder("SYSTEM STATUS: ");
                List<Integer> aliveList = new ArrayList<>(alive);

                // Sort the list for consistent output (A->Z, or in this case, low-to-high port)
                aliveList.sort(Comparator.naturalOrder());

                for (int i = 0; i < aliveList.size(); i++) {
                    int port = aliveList.get(i);

                    if (port == currentPrimaryPort) {
                        statusLine.append("[").append(port).append(" and Primary] | ");
                    } else {
                        statusLine.append("[").append(port).append(" and Alive] | ");
                    }
                }

                // If there are no alive servers, print a simple status
                if (aliveList.isEmpty()) {
                    statusLine.append("[No servers alive]");
                } else {
                    // Remove the trailing " | "
                    statusLine.setLength(statusLine.length() - 3);
                }

                // Append the current designated primary port if it's not present (e.g., if it's 0)
                if (currentPrimaryPort > 0 && !alive.contains(currentPrimaryPort)) {
                    statusLine.append(" | [Designated Primary Port ").append(currentPrimaryPort).append(" is DEAD]");
                } else if (currentPrimaryPort == 0) {
                    statusLine.append(" | [No designated Primary]");
                }


                System.out.println(statusLine.toString() + "\n");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}