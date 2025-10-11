import java.io.*;
import java.net.Socket;

public class HeartBeatClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12344);
             BufferedReader buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            while (true) {
                int heartBeat = HeartBeat.heartbeatCheck(true);

                buffWriter.write(String.valueOf(heartBeat));
                buffWriter.newLine();
                buffWriter.flush();


                String response = buffReader.readLine();
                System.out.println("Server: " + response);


                if (heartBeat == 0) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("I/O error Occured: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

