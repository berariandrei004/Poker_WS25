package at.ac.hcw.Server;

import java.util.*;

public class Game {
    private int smallBlind;
    private int bigBlind;
    private Player[] players; // Array der Größe 2 erwartet
    private ArrayList<Pot> pots;
    private int currentBet;
    private int currentPlayerIndex;

    // Liste der verbundenen ClientHandler zum Senden von Nachrichten
    private final List<ClientHandler> listeners = new ArrayList<>();

    private int dealerIndex = 0;
    private Card[] deck;
    private Random random = new Random();

    // Aktuelle Board Cards speichern für Late-Joiner oder Status Updates
    private List<Card> board = new ArrayList<>();

    public Game(int smallBlind, int bigBlind, Player[] players) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.players = players;
        this.pots = new ArrayList<>();
        this.pots.add(new Pot(0));
    }

    public void addListener(ClientHandler handler) {
        listeners.add(handler);
    }

    // --- Spielablauf Steuerung ---

    public void addPlayer(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                break;
            }
        }
        // Wenn 2 Spieler da sind, könnte man automatisch starten oder warten
        if (countActivePlayers() == 2) {
            // Optional: Automatisch starten
            // startNewRound();
        }
    }

    public synchronized void startNewRound() {
        System.out.println("Server: Starte neue Runde...");

        // Reset
        pots.clear();
        pots.add(new Pot(0));
        board.clear();
        deck = createDeck();
        broadcastToAll("NEW_ROUND");

        // Spieler Reset
        for (Player p : players) {
            if (p != null) p.resetForNewRound();
        }

        // Blinds setzen (Dealer rotiert am Ende)
        int sbIndex = dealerIndex; // Im Heads-Up ist Dealer oft Small Blind preflop, aber wir machen Dealer = Button = SB
        int bbIndex = (dealerIndex + 1) % 2;

        Player sb = players[sbIndex];
        Player bb = players[bbIndex];

        // Blinds abziehen
        sb.raise(smallBlind); // Logik in Player muss angepasst werden (siehe unten)
        bb.raise(bigBlind);

        pots.get(0).raisePot(smallBlind + bigBlind);
        currentBet = bigBlind;

        // Karten austeilen
        for (Player p : players) {
            if (p == null) continue;
            Card c1 = drawCard();
            Card c2 = drawCard();
            p.receiveCard(c1, 0);
            p.receiveCard(c2, 1);

            // Nachricht an diesen Spieler: Seine Karten
            sendToPlayer(p, "HAND " + c1 + " " + c2);
            // Nachricht an den Gegner: "Gegner hat Karten bekommen"
            sendToOpponent(p, "OPPONENT_HAND");
        }

        // Wer ist dran? (Preflop: Spieler nach Big Blind -> im Heads-Up ist das der Dealer/SB)
        currentPlayerIndex = sbIndex;

        broadcastUIUpdate();
    }

    public synchronized String handleCommand(Player player, String command) {
        String[] parts = command.split(" ");
        String action = parts[0];

        Pot mainPot = pots.get(0);

        // Sicherheitscheck: Ist der Spieler dran?
        if (player != players[currentPlayerIndex]) {
            return "ERROR Not your turn";
        }

        switch (action) {
            case "FOLD":
                player.fold();
                // Gegner gewinnt sofort
                endHandByFold(getOpponent(player));
                return "FOLD_OK";

            case "CHECK":
                // Check ist nur erlaubt, wenn player.bet == currentBet
                if (player.getBet() < currentBet) {
                    // Falls es eigentlich ein Call sein müsste (Client sendet manchmal Check für beides)
                    int toCall = currentBet - player.getBet();
                    player.call(toCall);
                    mainPot.raisePot(toCall);
                } else {
                    player.check();
                }
                break;

            case "CALL": // Wird vom Client meist nicht gesendet, da Button "Check/Call" heißt
                int toCall = currentBet - player.getBet();
                player.call(toCall);
                mainPot.raisePot(toCall);
                break;

            case "RAISE":
                int amount = Integer.parseInt(parts[1]);
                // Client sendet oft absolute Betrag oder Raise-Betrag. Hier Annahme: Raise Amount on top
                // Einfache Logik:
                int callPart = currentBet - player.getBet();
                player.call(callPart);
                mainPot.raisePot(callPart);

                player.raise(amount);
                mainPot.raisePot(amount);
                currentBet = player.getBet(); // Neue High Bet
                break;

            case "ALLIN":
                int allInAmount = player.allIn();
                mainPot.raisePot(allInAmount);
                if (player.getBet() > currentBet) {
                    currentBet = player.getBet();
                }
                break;
        }

        // Nächster Spieler oder nächste Phase?
        if (isBettingRoundOver()) {
            advanceGameStage();
        } else {
            // Zug wechselt
            currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        }

        broadcastUIUpdate();
        return "OK";
    }

    // --- State Management Helpers ---

    private void broadcastUIUpdate() {
        // 1. Pot Update
        broadcastToAll("POT " + pots.get(0).getMoney());

        // 2. Chips und Active Player für JEDEN Client individuell berechnen
        for (ClientHandler ch : listeners) {
            Player hero = ch.getPlayer();
            if (hero == null) continue;

            Player opponent = getOpponent(hero);

            // Chips senden
            ch.sendMessage("CHIPS HERO " + hero.getBudget());
            if (opponent != null) {
                ch.sendMessage("CHIPS OPPONENT " + opponent.getBudget());
            }

            // Wer ist dran?
            Player current = players[currentPlayerIndex];
            if (current == hero) {
                ch.sendMessage("TURN_ACTIVE HERO");
            } else {
                ch.sendMessage("TURN_ACTIVE OPPONENT");
            }
        }
    }

    private void advanceGameStage() {
        // Reset Bets für nächste Runde (interne Logik, nicht Chips)
        for(Player p : players) p.setBet(0);
        currentBet = 0;
        currentPlayerIndex = (dealerIndex + 1) % 2; // BB fängt post-flop an (oder SB? Standard: Position) -> Hier vereinfacht

        // Je nach Anzahl Karten auf Board
        int cardsOnBoard = board.size();

        if (cardsOnBoard == 0) {
            // Flop
            Card c1 = drawCard(); Card c2 = drawCard(); Card c3 = drawCard();
            board.add(c1); board.add(c2); board.add(c3);
            broadcastToAll("FLOP " + c1 + " " + c2 + " " + c3);
        } else if (cardsOnBoard == 3) {
            // Turn
            Card c4 = drawCard();
            board.add(c4);
            broadcastToAll("TURN " + c4);
        } else if (cardsOnBoard == 4) {
            // River
            Card c5 = drawCard();
            board.add(c5);
            broadcastToAll("RIVER " + c5);
        } else if (cardsOnBoard == 5) {
            // Showdown
            performShowdown();
            return; // Showdown startet neue Runde oder beendet
        }

        broadcastUIUpdate();
    }

    private void performShowdown() {
        // Karten aufdecken
        for(ClientHandler ch : listeners) {
            Player p = ch.getPlayer();
            Player opp = getOpponent(p);
            // Sende dem Spieler die Karten des Gegners
            ch.sendMessage("SHOWDOWN " + opp.getHand()[0] + " " + opp.getHand()[1]);
        }

        // Gewinner ermitteln (Vereinfacht: High Card Logic Placeholder)
        // Hier müsstest du deine komplexe "getWinners" Logik einbinden
        // Fürs Beispiel gewinnt Player 1 immer:
        Player winner = players[0]; // TODO: Echte Logik nutzen

        broadcastToAll("WINNER " + winner.getName());

        // Pot verteilen
        winner.winPot(pots.get(0).getMoney());
        pots.get(0).setMoney(0);

        // Dealer rotieren
        dealerIndex = (dealerIndex + 1) % 2;

        // Neue Runde verzögert starten (Thread sleep bad in production, but ok here)
        new Thread(() -> {
            try { Thread.sleep(4000); } catch (Exception e){}
            startNewRound();
        }).start();
    }

    private void endHandByFold(Player winner) {
        broadcastToAll("WINNER " + winner.getName());
        winner.winPot(pots.get(0).getMoney());
        pots.get(0).setMoney(0);
        dealerIndex = (dealerIndex + 1) % 2;

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (Exception e){}
            startNewRound();
        }).start();
    }

    // --- Technical Helpers ---

    private boolean isBettingRoundOver() {
        // Sehr vereinfacht: Wenn beide Bets gleich sind und nicht 0 (oder check-check auf 0)
        // Echte Logik muss prüfen, ob jeder mindestens einmal gehandelt hat.
        // Für dieses Beispiel: Wenn Bets gleich sind und Pot > Blinds (grobe Annäherung)
        Player p1 = players[0];
        Player p2 = players[1];
        if (p1 == null || p2 == null) return false;
        if (p1.hasFolded() || p2.hasFolded()) return true;

        return p1.getBet() == p2.getBet() && p1.getBet() == currentBet; // && beide haben gehandelt (TODO)
    }

    private Player getOpponent(Player p) {
        if (players[0] == p) return players[1];
        return players[0];
    }

    private int countActivePlayers() {
        int c = 0;
        for(Player p : players) if(p!=null) c++;
        return c;
    }

    private Card drawCard() {
        while(true) {
            int r = random.nextInt(52);
            if(deck[r] != null) {
                Card c = deck[r];
                deck[r] = null;
                return c;
            }
        }
    }

    private Card[] createDeck() {
        Card[] d = new Card[52];
        for(int i=0; i<4; i++) {
            for(int j=0; j<13; j++) {
                d[i*13+j] = new Card(i, j);
            }
        }
        return d;
    }

    // Sendet Nachricht an ALLE Clients
    private void broadcastToAll(String msg) {
        for(ClientHandler ch : listeners) {
            ch.sendMessage(msg);
        }
    }

    // Sendet Nachricht nur an den Besitzer des Player-Objekts
    private void sendToPlayer(Player p, String msg) {
        for(ClientHandler ch : listeners) {
            if (ch.getPlayer() == p) {
                ch.sendMessage(msg);
            }
        }
    }

    // Sendet Nachricht nur an den Gegner des Player-Objekts
    private void sendToOpponent(Player p, String msg) {
        Player opp = getOpponent(p);
        sendToPlayer(opp, msg);
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }
}