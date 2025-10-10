/**
 * Primary server implementation.
 * Starts as primary and handles client requests.
 */
public class primary extends ServerProcess {
    public primary(int port) {
        super(port);
        this.isPrimary = true;
    }

    @Override
    protected void onPromotedToPrimary() {
        // Log the change, even if it's the default primary, for clarity
        System.out.println("I am Port " + this.serverPort + ". Confirmed as PRIMARY.");
    }

    // You will need a main method to run this instance (example below)
    /*
    public static void main(String[] args) {
        new primary().process();
    }
    */
}