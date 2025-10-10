/**
 * Backup server implementation.
 * Starts as backup and can be promoted to primary.
 */
public class backup extends ServerProcess {

    public backup() {
        super("BACKUP-1");
        this.isPrimary = false; // Starts as backup
    }

    @Override
    protected void onPromotedToPrimary() {
        // This is the difference - backup needs to do something when promoted
        System.out.println("*** I WAS BACKUP, NOW I'M PRIMARY! ***");
        // Later you'll add: sync state, notify others, etc.
        this.currentServerID = "PRIMARY-1";
    }

}