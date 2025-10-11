import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HeartBeatServer {
    public static void main(String[] args) {
        int port = 12344; // must match the client port

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Heartbeat Server listening on port " + port);

            // Accept one client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                String heartbeat;
                while ((heartbeat = reader.readLine()) != null) {
                    System.out.println("Received from client: " + heartbeat);

                    if (heartbeat.equals("1")) {
                        writer.write("OK - Heartbeat received");
                    } else {
                        writer.write("ERROR - Heartbeat stopped");
                        writer.newLine();
                        writer.flush();
                        break; // stop if heartbeat = 0
                    }

                    writer.newLine();
                    writer.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
