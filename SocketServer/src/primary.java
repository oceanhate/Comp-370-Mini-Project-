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

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java primary <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        new primary(port).process();
    }
}