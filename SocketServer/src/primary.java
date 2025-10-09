/**
 * Primary server implementation.
 * Starts as primary and handles client requests.
 */
public class primary extends ServerProcess {

    public primary() {
        // Correct Java syntax: call the superclass constructor with the server ID
        super("PRIMARY-1");
        this.isPrimary = true; // Starts as primary
    }

    @Override
    protected void onPromotedToPrimary() {
        // Already primary, do nothing
        System.out.println("I'm already the primary server");
    }

}