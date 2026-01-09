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
    @FXML private TextField hostPlayerNameField;
    @FXML private Spinner<Integer> playerCountSpinner;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;
    @FXML
    private void onBackClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
    @FXML
    private void onStartGameClicked() throws IOException {
        App.getSceneController().switchToPokerTable();
    }
    @FXML
    private void onStartServerClicked() throws IOException {
        if (hostPlayerNameField.getText() == null || hostPlayerNameField.getText().length() < 3) {
            System.out.println("Spielername benötigt mindestens drei Zeichen!");
            return;
        }
        int maxClients = playerCountSpinner.getValue();
        String ipv4 = null;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();

            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                    ipv4 = addr.getHostAddress();
                    break;
                }
            }
            if (ipv4 != null) break;
        }

        if (ipv4 == null) {
            System.out.println("Keine gültige IPv4-Adresse gefunden!");
            return;
        }
        String joinCodeId = JoinCodeHandler.IPv4ToJoinCode(ipv4);
        ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-cp",
                System.getProperty("java.class.path"), // 1. Uses the current project's classpath
                "at.ac.hcw.Server.MainPokerServer",    // 2. Fully qualified class name
                String.valueOf(maxClients),             // 3. Arguments
                joinCodeId
        );

        builder.inheritIO(); // Shows server logs in your current console
        Process serverProcess = builder.start();
        App.getSceneController().setServerProcess(serverProcess);


        App.getSceneController().connectToServer(ipv4, 5000, hostPlayerNameField.getText());

    }

}
