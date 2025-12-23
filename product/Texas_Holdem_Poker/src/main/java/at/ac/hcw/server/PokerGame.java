package at.ac.hcw.server;

import java.util.ArrayList;
import java.util.List;

public class PokerGame {
    private int playerCount;
    private int bigBlind;
    private int smallBlind;
    private int startingCash;
    private List<ClientHandler> clients = new ArrayList<>();

    public PokerGame(int playerCount, int bigBlind, int smallBlind, int startingCash, List<ClientHandler> clients) {
        this.playerCount = playerCount;
        this.bigBlind = bigBlind;
        this.smallBlind = smallBlind;
        this.startingCash = startingCash;
        this.clients = clients;
    }

}
