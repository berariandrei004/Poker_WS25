package at.ac.hcw.Server;

import java.util.*;

public class Game {
    private int smallBlind;
    private int bigBlind;
    private Player[] players;
    public ArrayList<Pot> pots;
    private int potIndex = 0;

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
            if (p != null && !p.hasFolded()) count++;
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

    public void playRound(int currentBet, int roundStart, Scanner scanner) {
        int i = roundStart;
        Pot mainPot = pots.get(0);
        int lastRaiser = -1;

        while (countActivePlayers() > 1) {

            Player p = players[i];

            if (p != null && mainPot.getPlayers().contains(p)) {

                int action = p.askAction(currentBet, scanner);

                if (action < 0) {
                    mainPot.removePlayer(p);
                }
                else {
                    int diff = action - p.getBet();
                    p.setBet(action);
                    mainPot.raisePot(diff);
                    if (action > currentBet) {
                        currentBet = action;
                        lastRaiser = i;
                    }
                }
            }

            i = (i + 1) % players.length;

            if (lastRaiser == i || mainPot.getPlayers().size() <= 1) {
                break;
            }
        }
    }

    public void buildSidePots() {

        pots.clear();
        potIndex = 0;

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

    public void playGame() {
        Random random = new Random();
        int start = random.nextInt(players.length);
        Scanner scanner = new Scanner(System.in);

        boolean continueGame = true;
        while (continueGame) {
            Card[] deck = createDeck();
            Pot mainPot = new Pot(0);
            pots.add(mainPot);
            for (Player player : players) {
                mainPot.addPlayer(player);
            }

            for (Player player : players) {
                player.receiveCard(setCard(deck, random), 0);
                player.receiveCard(setCard(deck, random), 1);
                System.out.println(player.getHand()[0]);
                System.out.println(player.getHand()[1]);
            }

            players[start].raise(smallBlind);
            players[start].setBet(smallBlind);
            mainPot.raisePot(smallBlind);

            int bbPlayer = (start + 1) % players.length;
            players[bbPlayer].setBet(bigBlind);
            players[bbPlayer].raise(bigBlind);

            mainPot.raisePot(bigBlind);

            int roundStart = (bbPlayer+1)%players.length;

            playRound(bigBlind,roundStart,scanner);

            Card FlopFirst = setCard(deck,random);
            Card FlopSecond = setCard(deck,random);
            Card FlopThird = setCard(deck,random);

            System.out.println(FlopFirst);
            System.out.println(FlopSecond);
            System.out.println(FlopThird);

            for (Player player : players) {
                if (player != null) {
                    player.receiveCard(FlopFirst, 2);
                    player.receiveCard(FlopSecond, 3);
                    player.receiveCard(FlopThird, 4);
                }
            }

            playRound(0,roundStart,scanner);

            Card Turn = setCard(deck,random);

            System.out.println(Turn);

            for (Player player : players) {
                if (player != null) {
                    player.receiveCard(Turn, 5);
                }
            }

            playRound(0,roundStart,scanner);

            Card River = setCard(deck,random);

            System.out.println(River);

            for (Player player : players) {
                if (player != null) {
                    player.receiveCard(River, 6);
                }
            }

            playRound(0,roundStart,scanner);
            buildSidePots();

            for (Pot pot : pots) {

                Player[] potWinners = getWinners(pot.getPlayers());
                int personalWin = pot.getMoney() / potWinners.length;

                for (Player p : potWinners) {
                    p.winPot(personalWin);
                }
            }

            for (Player player: players) {
                player.resetForNewRound();
            }

            deck = createDeck();

            System.out.println("Do you want to continue the game?");
            continueGame = scanner.nextBoolean();
        }
    }
}