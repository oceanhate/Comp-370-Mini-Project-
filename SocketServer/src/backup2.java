/**
 * Backup server implementation.
 * Starts as backup and can be promoted to primary.
 */
public class backup2 extends ServerProcess {
    public backup2(int port) {
        super(port);
        this.isPrimary = false;
    }

    @Override
    protected void onPromotedToPrimary() {
        // This is the difference - backup needs to log the promotion
        System.out.println("*** Port " + this.serverPort + " WAS BACKUP, NOW I'M PRIMARY! ***");
        // DO NOT change the port or ID; the Monitor tracks its status change
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java backup2 <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        new backup2(port).process();
    }
}