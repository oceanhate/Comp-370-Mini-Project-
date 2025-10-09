import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter; // <-- Necessary for promotion logic
import java.net.ServerSocket;
import java.net.Socket;
// Note: SocketTimeoutException is no longer needed in main
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Monitor {
    public static void main(String[] args) {
        final int PORT = 9000;
        final int TIMEOUT_MS = 5000;

        // Map of serverId -> last seen timestamp (ms)
        final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
        // Set of currently-known-alive servers
        final Set<String> alive = ConcurrentHashMap.newKeySet();

        // 1. START THE BACKGROUND DEATH CHECKER THREAD (NEW)
        Thread checkerThread = new Thread(() -> runDeathChecker(lastSeen, alive, TIMEOUT_MS));
        checkerThread.setDaemon(true);
        checkerThread.start();

        // 2. MAIN THREAD ONLY ACCEPTS HEARTBEATS
        try (ServerSocket ss = new ServerSocket(PORT)) {
            // ss.setSoTimeout() is removed
            System.out.println("Monitor listening for heartbeats on port " + PORT);

            while (true) {
                try {
                    // This call now blocks indefinitely until a heartbeat is received
                    Socket s = ss.accept();

                    // The heartbeat reception logic (remains mostly the same)
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        String line = in.readLine();
                        if (line != null) {
                            String id = line.trim();
                            if (!id.isEmpty()) {
                                long now = System.currentTimeMillis();
                                lastSeen.put(id, now);
                                if (!alive.contains(id)) {
                                    alive.add(id);
                                    System.out.println("Server " + id + " alive (heartbeat received)");
                                }
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

    // 3. THE RUN DEATH CHECKER METHOD (Copied from your second code block)
    private static void runDeathChecker(Map<String, Long> lastSeen, Set<String> alive, final int TIMEOUT_MS) {
        final int CHECK_INTERVAL = 2000;

        // Hard-coded mapping for failover logic
        final String PRIMARY_ID = "PRIMARY-1";
        final int PRIMARY_PORT = 8090;

        // Define the failover path (Backup ID -> Port)
        final Map<String, Integer> BACKUP_PORTS = MapOf(
                "BACKUP-1", 8089,
                "BACKUP-2", 8088
        );

        while (true) {
            try {
                Thread.sleep(CHECK_INTERVAL);
                long now = System.currentTimeMillis();

                boolean primaryFailed = false;

                // --- 1. DETECT DEATHS AND CHECK PRIMARY STATUS ---
                for (String id : lastSeen.keySet()) {
                    Long t = lastSeen.get(id);
                    if (t == null) continue;

                    if (alive.contains(id) && now - t > TIMEOUT_MS) {
                        alive.remove(id);

                        if (id.equals(PRIMARY_ID)) {
                            primaryFailed = true;
                        }

                        // Report Death
                        long timeLapsed = now - t;
                        System.err.println("!!! Server " + id + " is DEAD (no heartbeat for " + timeLapsed + "ms) !!!");
                    }
                }

                // --- 2. TRIGGER PROMOTION IF PRIMARY IS DOWN ---
                if (primaryFailed) {
                    System.out.println("\n*** PRIMARY SERVER FAILED. INITIATING FAILOVER ***");

                    String newPrimaryId = alive.stream()
                            .filter(BACKUP_PORTS::containsKey)
                            .findFirst()
                            .orElse(null);

                    if (newPrimaryId != null) {
                        int newPrimaryPort = BACKUP_PORTS.get(newPrimaryId);

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

    // Helper method to resolve Map.of() issue if using an older Java version
    private static <K, V> Map<K, V> MapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new ConcurrentHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}