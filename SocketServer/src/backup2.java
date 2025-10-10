/**
 * Backup server implementation.
 * Starts as backup and can be promoted to primary.
 */
public class backup2 extends ServerProcess {

    public backup2() {
        super("BACKUP-2");
        this.isPrimary = false; // Starts as backup
    }

    @Override
    protected void onPromotedToPrimary() {
        // This is the difference - backup needs to do something when promoted
        System.out.println("*** I WAS BACKUP, NOW I'M PRIMARY! ***");
        this.currentServerID = "PRIMARY-1";
    }
}

