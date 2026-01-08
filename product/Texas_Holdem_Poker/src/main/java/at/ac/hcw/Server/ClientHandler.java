package at.ac.hcw.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("Willkommen! Verbindung hergestellt.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client sagt: " + message);
                out.println("Echo: " + message);
            }

        } catch (IOException e) {
            System.out.println("Client verbindung abgebrochen");
        } finally {
            System.out.println("Client hat sich getrennt.");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
