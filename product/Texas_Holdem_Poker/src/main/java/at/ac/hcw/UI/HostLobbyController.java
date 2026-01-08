package at.ac.hcw.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class HostLobbyController {
    private PokerClient client;
    private Process serverProcess;
    @FXML private Spinner<Integer> playerCountSpinner;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;
    @FXML
    private void onLobbyCloseClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
    @FXML
    private void onStartGameClicked() throws IOException {
        App.getSceneController().switchToPokerTable();
    }
    @FXML
    private void onStartServerClicked() throws IOException {
        int maxClients = playerCountSpinner.getValue();

        ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-cp",
                System.getProperty("java.class.path"), // 1. Uses the current project's classpath
                "at.ac.hcw.Server.MainPokerServer",    // 2. Fully qualified class name
                String.valueOf(maxClients)             // 3. Arguments
        );

        builder.inheritIO(); // Shows server logs in your current console
        serverProcess = builder.start();
        App.getSceneController().setServerProcess(serverProcess);

        String ipv4 = null;
        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address) {
                        ipv4 = addr.getHostAddress();
                        System.out.println("Host Ip Address: " + ipv4);
                        break;
                    }
                }
                if (ipv4 != null) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
