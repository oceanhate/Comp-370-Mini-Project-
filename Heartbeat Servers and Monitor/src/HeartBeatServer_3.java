import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class HeartBeatServer_3 {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(333)) {
            System.out.println("Server started. Waiting for Monitor...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Monitor connected.");

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            boolean running = true;
            while (HeartBeat.heartbeatCheck(running) == 1) {
                out.println("HEARTBEAT2");
                System.out.println("Sent heartbeat to Monitor");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
