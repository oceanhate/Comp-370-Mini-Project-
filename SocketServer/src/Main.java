import java.util.Scanner;

/**
 * The Main class is the entry point for the server application.
 */
public class Main {
    // Define static ports for clarity and easier management.
    public static final int PRIMARY_PORT = 8090;
    public static final int BACKUP_PORT = 8089;
    public static final int BACKUP_PORT2 = 8088;
    /**
     * The main method creates and starts the primary and backup servers, each in its own thread.
     * It also starts the monitor to watch over the primary server.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        var primaryServer = new primary();
        var backupServer = new backup();
        var backupServer2 = new backup2();

        // Announce that the servers are about to start.
        System.out.println("Server processes starting...");

        // Start the primary server. The process() method handles its own threading.
        primaryServer.process(PRIMARY_PORT);
        System.out.println("Primary server started on port " + PRIMARY_PORT);

        // Start the backup server. The process() method handles its own threading.
        backupServer.process(BACKUP_PORT);
        System.out.println("Backup server started on port " + BACKUP_PORT);

        backupServer2.process(BACKUP_PORT2);
        System.out.println("Backup server 2 started on port " + BACKUP_PORT2);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter a command (stop primary / stop backup1 / stop backup2 / stop all / exit / reboot): \n");
                String command = scanner.nextLine().trim().toLowerCase();

                switch (command) {
                    case "stop primary" -> {
                        primaryServer.stop();
                        System.out.println("Primary server stopped");
                    }//comands to stop the server
                    case "stop backup1" -> {
                        backupServer.stop();
                        System.out.println("Backup server1 stopped");
                    }
                    case "stop backup2" -> {
                        backupServer2.stop();
                        System.out.println("Backup server2 stopped");
                    }
                    case "stop all" -> {
                        primaryServer.stop();
                        backupServer.stop();
                        backupServer2.stop();
                        System.out.println("All servers stopped.");
                    }

                    case "reboot" -> {
                        System.out.println("Rebooting all servers...");
                        primaryServer.stop();
                        backupServer.stop();
                        backupServer2.stop();

                        // Give time for sockets to close
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                        primaryServer = new primary();
                        backupServer = new backup();
                        backupServer2 = new backup2();

                        primaryServer.process(PRIMARY_PORT);
                        backupServer.process(BACKUP_PORT);
                        backupServer2.process(BACKUP_PORT2);
                        System.out.println("All servers rebooted successfully.");
                    }

                    case "exit" -> {
                        System.out.println("Shutting down...");
                        primaryServer.stop();
                        backupServer.stop();
                        backupServer2.stop();
                        System.exit(0);

                    }
                }
            }
        }


    }
}
