package at.ac.hcw.Server;

import java.util.*;

public class Game {
    private int smallBlind;
    private int bigBlind;
    private Player[] players;
    public ArrayList<Pot> pots;
    private int potIndex = 0;
    private State state = State.WAITING_FOR_PLAYERS;
    private int currentPlayerIndex;
    private final List<ClientHandler> listeners = new ArrayList<>();
    private int dealerIndex = 0;
    private Card[] deck = new Card[52];
    private Random random = new Random();

    public void addListener(ClientHandler handler) {
        listeners.add(handler);
    }

    public void removeListener(ClientHandler handler) {
        listeners.remove(handler);
    }

    public Game(int smallBlind, int bigBlind, Player[] players) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.players = players;
        this.pots = new ArrayList<>();
    }


    public int getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.length) {
            return null;
        }
        return players[currentPlayerIndex];
    }

    public void addPlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                break;
            }
        }
    }

    private int countActivePlayers() {
        int count = 0;
        for (Player p : players) {
            if (p != null && !p.hasFolded()) {
                count++;
            }
        }
        return count;
    }

    public Card[] createDeck() {
        Card[] deck = new Card[52];
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 12; j++) {
                deck[i * 13 + j] = new Card(i, j);
            }
        }
        return deck;
    }

    public int compareEndHands(Player a, Player b) {

        Card[] ha = a.getEndHand();
        Card[] hb = b.getEndHand();

        for (int i = 0; i < ha.length; i++) {
            if (ha[i].getNum() > hb[i].getNum()) return 1;
            if (ha[i].getNum() < hb[i].getNum()) return -1;
        }

        return 0;
    }

    public Player[] getWinners(ArrayList<Player> players) {

        int maxPoints = 0;
        for (Player player : players) {
            int pts = player.getPoints();
            if (pts > maxPoints) {
                maxPoints = pts;
            }
        }

        ArrayList<Player> possWinners = new ArrayList<>();
        for (Player player : players) {
            if (player.getPoints() == maxPoints) {
                player.sortHand();
                possWinners.add(player);
            }
        }

        ArrayList<Player> winners = new ArrayList<>();
        winners.add(possWinners.get(0));

        for (int i = 1; i < possWinners.size(); i++) {
            Player challenger = possWinners.get(i);
            Player currentWinner = winners.get(0);

            int cmp = compareEndHands(challenger, currentWinner);

            if (cmp > 0) {
                winners.clear();
                winners.add(challenger);
            } else if (cmp == 0) {
                winners.add(challenger);
            }
        }

        return winners.toArray(new Player[0]);
    }

    public Card setCard(Card[] deck, Random random) {
        int i = random.nextInt(52);
        while (deck[i] == null) {
            i = random.nextInt(52);
        }
        Card ret = deck[i];
        deck[i] = null;
        return ret;
    }

    private boolean bettingRoundFinished() {

        Pot mainPot = pots.get(0);
        int highestBet = mainPot.getHighestBet();

        for (Player p : mainPot.getPlayers()) {
            if (!p.hasFolded()
                    && !p.isAllin()
                    && p.getBet() != highestBet) {
                return false;
            }
        }
        return true;
    }

    public void buildSidePots() {

        pots.clear();

        List<Player> possWinners = new ArrayList<>();

        for (Player p : players) {
            if (!p.hasFolded() && p.getTotalBet() > 0) {
                possWinners.add(p);
            }
        }

        possWinners.sort(Comparator.comparingInt(Player::getTotalBet));

        int prevBet = 0;

        while (!possWinners.isEmpty()) {

            Player first = possWinners.get(0);
            int lowestAmount = first.getTotalBet();
            int potAmount = (lowestAmount - prevBet) * possWinners.size();

            Pot pot = new Pot(potIndex++);
            pot.setMoney(potAmount);

            for (Player p : possWinners) {
                pot.addPlayer(p);
            }

            pots.add(pot);

            prevBet = lowestAmount;

            possWinners.removeIf(p -> p.getTotalBet() == lowestAmount);
        }
    }

    public synchronized String handleCommand(Player player, String command) {

        if (pots.isEmpty()) {
            return "ERROR No active round";
        }

        Pot mainPot = pots.get(0);
        String[] parts = command.split(" ");
        String cmd = parts[0];
        String response;

        if (player.hasFolded()) {
            return "ERROR You already folded";
        }

        switch (cmd) {

            case "CALL": {
                if (parts.length != 2) return "ERROR CALL <amount>";

                int toCall = Integer.parseInt(parts[1]);
                int diff = toCall - player.getBet();
                if (diff < 0) return "ERROR Invalid call";

                player.call(diff);
                mainPot.raisePot(diff);

                response = "CALL " + diff;
                break;
            }

            case "RAISE": {
                if (parts.length != 2) return "ERROR RAISE <amount>";

                int raiseAmount = Integer.parseInt(parts[1]);
                if (raiseAmount <= 0) return "ERROR Invalid raise";

                int diffToCall = mainPot.getHighestBet() - player.getBet();
                if (diffToCall > 0) {
                    player.call(diffToCall);
                    mainPot.raisePot(diffToCall);
                }

                player.raise(raiseAmount);
                mainPot.raisePot(raiseAmount);

                response = "RAISE " + raiseAmount;
                break;
            }

            case "CHECK": {
                if (player.getBet() < mainPot.getHighestBet()) {
                    return "ERROR Cannot check";
                }
                player.check();
                response = "CHECK";
                break;
            }

            case "FOLD": {
                player.fold();
                mainPot.removePlayer(player);
                response = "FOLD";
                break;
            }

            case "ALLIN": {
                int amount = player.allIn();
                mainPot.raisePot(amount);
                response = "ALLIN " + amount;
                break;
            }

            default:
                return "ERROR Unknown command";
        }

        currentPlayerIndex = getNextActivePlayer();

        if (currentPlayerIndex == -1) {
            advanceState();
            return response;
        }

        if (countActivePlayers() == 1) {
            showdown();
            return "WIN_BY_FOLD";
        }

        if (bettingRoundFinished()) {
            advanceState();
        } else {
            broadcastGameState();
        }

        return response;
    }

    private void sendPrivateCardsToOwner(Player player, Card c1, Card c2) {
        for (ClientHandler ch : listeners) {
            if (ch.getPlayer() == player) {
                ch.sendPrivateCards(c1, c2);
                return;
            }
        }
    }

    public synchronized void startNewRound() {

        pots.clear();
        potIndex = 0;

        Pot mainPot = new Pot(potIndex++);
        pots.add(mainPot);

        deck = createDeck();

        for (Player p : players) {
            if (p != null) {
                p.resetForNewRound();
                mainPot.addPlayer(p);
            }
        }

        //Karten
        for (Player p : players) {
            if (p != null) {
                Card c1 = setCard(deck, random);
                Card c2 = setCard(deck, random);

                p.receiveCard(c1, 0);
                p.receiveCard(c2, 1);

                sendPrivateCardsToOwner(p, c1, c2);
            }
        }

        int sbIndex = (dealerIndex + 1) % players.length;
        int bbIndex = (dealerIndex + 2) % players.length;

        currentPlayerIndex = (dealerIndex + 3) % players.length;

        Player sb = players[sbIndex];
        Player bb = players[bbIndex];

        sb.raise(smallBlind);
        mainPot.raisePot(smallBlind);

        bb.raise(bigBlind);
        mainPot.raisePot(bigBlind);

        currentPlayerIndex = (bbIndex + 1) % players.length;
    }

    public synchronized void broadcastGameState() {
        String stateMessage = buildGameStateMessage();

        for (ClientHandler handler : listeners) {
            handler.sendMessage(stateMessage);
        }
    }

    private String buildGameStateMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("GAME_STATE ");
        sb.append("state=").append(state);
        sb.append(" pot=").append(pots.get(0).getMoney());
        sb.append(" highestBet=").append(pots.get(0).getHighestBet());
        if (currentPlayerIndex >= 0 && players[currentPlayerIndex] != null) {
            sb.append(" currentPlayer=").append(players[currentPlayerIndex].getName());
        }
        else {
            sb.append(" currentPlayer=NONE");
        }

        return sb.toString();
    }

    public void dealFlop() {

        Card c1 = setCard(deck, random);
        Card c2 = setCard(deck, random);
        Card c3 = setCard(deck, random);

        for (Player p : players) {
            if (p != null) {
                p.receiveCard(c1, 2);
                p.receiveCard(c2, 3);
                p.receiveCard(c3, 4);
            }
        }
        resetPlayerBets();
        currentPlayerIndex = getNextActivePlayer();
        setState(State.FLOP_BETTING);
        broadcast("FLOP " + c1 + " " + c2 + " " + c3);
    }

    public void dealTurn() {

        Card c4 = setCard(deck, random);

        for (Player p : players) {
            if (p != null) {
                p.receiveCard(c4, 5);
            }
        }
        resetPlayerBets();
        currentPlayerIndex = getNextActivePlayer();
        setState(State.TURN_BETTING);
        broadcast("TURN " + c4);
    }

    public void dealRiver() {

        Card c5 = setCard(deck, random);

        for (Player p : players) {
            if (p != null) {
                p.receiveCard(c5, 6);
            }
        }

        resetPlayerBets();
        currentPlayerIndex = getNextActivePlayer();

        setState(State.RIVER_BETTING);
        broadcast("RIVER " + c5);
    }

    public void advanceState() {
        switch (state) {
            case PREFLOP_BETTING -> dealFlop();
            case FLOP_BETTING    -> dealTurn();
            case TURN_BETTING    -> dealRiver();
            case RIVER_BETTING   -> showdown();
        }
    }

    public void showdown() {

        setState(State.SHOWDOWN);

        buildSidePots();

        for (Pot pot : pots) {
            Player[] winners = getWinners(pot.getPlayers());
            int win = pot.getMoney() / winners.length;

            for (Player p : winners) {
                p.winPot(win);
            }
        }
        dealerIndex = (dealerIndex + 1) % players.length;
        startNewRound();
        setState(State.PREFLOP_BETTING);
    }

    public void resetPlayerBets() {
        for (Player player : players) {
            if (player != null) {
                player.setBet(0);
            }
        }
    }

    public int getFirstPlayer() {
        int index = (dealerIndex + 1) % players.length;

        for (int i = 0; i < players.length; i++) {

            Player p = players[index];

            if (p != null && !p.hasFolded() && !p.isAllin()) {
                return index;
            }

            index = (index + 1) % players.length;
        }

        return -1;
    }

    public int getNextActivePlayer() {

        int index = (currentPlayerIndex + 1) % players.length;

        for (int i = 0; i < players.length; i++) {
            Player p = players[index];

            if (p != null && !p.hasFolded() && !p.isAllin()) {
                return index;
            }

            index = (index + 1) % players.length;
        }

        // Niemand mehr handlungsfähig
        return -1;
    }

    public void setState(State newState) {
        this.state = newState;

        if (newState == State.PREFLOP_BETTING) {
            onNewRound();
        }

        broadcastGameState();
    }

    private void onNewRound() {
        System.out.println("NEW_ROUND");

        for (ClientHandler client : listeners) {
            client.sendMessage("NEW_ROUND");
        }
    }

    public synchronized void startGame() {
        startNewRound();
        setState(State.PREFLOP_BETTING);
    }

    private void broadcast(String msg) {
        for (ClientHandler ch : listeners) {
            ch.sendMessage(msg);
        }
    }
}