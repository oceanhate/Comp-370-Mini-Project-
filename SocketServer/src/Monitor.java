import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Monitor {

    // --- GLOBAL CONSTANTS (Used by both main and checker) ---
    private static final String PRIMARY_ID = "PRIMARY-1";
    private static final int PRIMARY_PORT = 8090;

    // Helper method for three key/value pairs (6 arguments total)
    private static <K, V> Map<K, V> MapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new ConcurrentHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    // Map of all server IDs and their corresponding ports
    // This map is used in the failover logic
    private static final Map<String, Integer> ALL_SERVER_PORTS = MapOf(
            PRIMARY_ID, PRIMARY_PORT,
            "BACKUP-1", 8089,
            "BACKUP-2", 8088
    );

    public static void main(String[] args) {
        final int PORT = 9000;
        final int TIMEOUT_MS = 5000;

        // Map of serverId -> last seen timestamp (ms)
        final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
        // Set of currently-known-alive servers
        final Set<String> alive = ConcurrentHashMap.newKeySet();

        // 1. START THE BACKGROUND DEATH CHECKER THREAD
        Thread checkerThread = new Thread(() -> runDeathChecker(lastSeen, alive, TIMEOUT_MS));
        checkerThread.setDaemon(true);
        checkerThread.start();

        // 2. MAIN THREAD ONLY ACCEPTS HEARTBEATS
        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Monitor listening for heartbeats on port " + PORT);

            while (true) {
                try {
                    Socket s = ss.accept();

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        String line = in.readLine();
                        if (line != null) {
                            String[] parts = line.trim().split("\\|");

                            // 1. CHECK FORMAT
                            if (parts.length == 2) {
                                String id = parts[0].trim();
                                long sentTimestamp;

                                // 2. PARSE TIMESTAMP
                                try {
                                    sentTimestamp = Long.parseLong(parts[1].trim());
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid timestamp received: " + line);
                                    continue; // Skip this heartbeat
                                }

                                // 3. PROCESS HEARTBEAT (Now fully within the correct scope)
                                if (!id.isEmpty()) {
                                    long now = System.currentTimeMillis();
                                    lastSeen.put(id, now);

                                    boolean wasNew = !alive.contains(id);
                                    if (wasNew) {
                                        alive.add(id);
                                    }
                                    if (wasNew) {
                                        System.out.println("New heartbeat: " + id + " sent: " + sentTimestamp);
                                    } else {
                                        System.out.println("Heartbeat: " + id + " sent: " + sentTimestamp);
                                    }
                                }
                            } else {
                                // Log malformed messages that aren't ID|TIMESTAMP
                                System.err.println("Malformed heartbeat received (no delimiter): " + line);
                            }
                        }
                    } finally {
                        try { s.close(); } catch (IOException ignored) {}
                    }
                } catch (IOException e) {
                    System.err.println("Monitor heartbeat reception failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Monitor failed: " + e.getMessage());
        }
    }

    // 3. THE RUN DEATH CHECKER METHOD
    private static void runDeathChecker(Map<String, Long> lastSeen, Set<String> alive, final int TIMEOUT_MS) {
        final int CHECK_INTERVAL = 2000;

        // Note: The BACKUP_PORTS map is no longer needed here, but we use ALL_SERVER_PORTS logic
        // to filter for available backups. We'll reuse the names to match your old logic structure.
        final Map<String, Integer> BACKUP_PORTS = ALL_SERVER_PORTS;


        while (true) {
            try {
                Thread.sleep(CHECK_INTERVAL);
                long now = System.currentTimeMillis();

                boolean primaryFailed = false;

                // --- 1. DETECT DEATHS AND PURGE STALE ENTRIES ---
                for (String id : new HashSet<>(lastSeen.keySet())) {
                    Long t = lastSeen.get(id);
                    if (t == null) continue;

                    long timeLapsed = now - t;

                    if (timeLapsed > TIMEOUT_MS) {

                        // Check if it was ALIVE before removal (i.e., this is a failure event)
                        if (alive.remove(id)) {
                            if (id.equals(PRIMARY_ID)) {
                                primaryFailed = true;
                            }

                            // Report Death accurately using the calculated timeLapsed
                            System.err.println("!!! Server " + id + " is DEAD (no heartbeat for " + timeLapsed + "ms) !!!");
                        }

                        // CRITICAL: Remove the entry from lastSeen regardless of its previous 'alive' status.
                        lastSeen.remove(id);
                    }
                }

                // --- 2. TRIGGER PROMOTION IF PRIMARY IS DOWN ---
                if (primaryFailed) {
                    System.out.println("\n*** PRIMARY SERVER FAILED. INITIATING FAILOVER ***");

                    // Filter the alive set to find a backup (i.e., not the PRIMARY_ID)
                    String newPrimaryId = alive.stream()
                            .filter(id -> !id.equals(PRIMARY_ID))
                            .filter(BACKUP_PORTS::containsKey) // Ensure it's a known backup
                            .findFirst()
                            .orElse(null);

                    if (newPrimaryId != null) {
                        int newPrimaryPort = ALL_SERVER_PORTS.get(newPrimaryId);

                        // Attempt to connect to the backup and send the PROMOTE command
                        try (Socket failoverSocket = new Socket("localhost", newPrimaryPort);
                             PrintWriter out = new PrintWriter(failoverSocket.getOutputStream(), true)) {

                            out.println("PROMOTE");
                            System.out.println("-> SENT PROMOTE COMMAND to " + newPrimaryId + " on port " + newPrimaryPort);

                        } catch (IOException promotionEx) {
                            System.err.println("-> ERROR: Failed to promote " + newPrimaryId + ". Server may be down: " + promotionEx.getMessage());
                        }
                    } else {
                        System.err.println("-> FATAL: No available backup servers to promote.");
                    }
                }

                // Report Status Summary
                if (primaryFailed || !alive.isEmpty()) {
                    System.out.println("SYSTEM STATUS: " + alive.size() + " server(s) alive. Remaining: " + alive + "\n");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}