/**
 * Backup server implementation.
 * Starts as backup and can be promoted to primary.
 */
public class backup extends ServerProcess {
    public backup(int port) {
        super(port);
        this.isPrimary = false;
    }

    @Override
    protected void onPromotedToPrimary() {
        // This is the difference - backup needs to log the promotion
        System.out.println("*** Port " + this.serverPort + " WAS BACKUP, NOW I'M PRIMARY! ***");
        // DO NOT change the port or ID; the Monitor tracks its status change
    }

    // You will need a main method to run this instance (example below)
    /*
    public static void main(String[] args) {
        new backup2().process();
    }
    */
}