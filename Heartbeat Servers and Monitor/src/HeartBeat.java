public class HeartBeat {
    public static int heartbeatCheck (boolean check) {
        if (!check) {
            System.out.println("User has shut-off heartbeat");
            return 0;
    }

    try {
        Thread.sleep(3000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return 0;
    }

    return 1;
    }
}
