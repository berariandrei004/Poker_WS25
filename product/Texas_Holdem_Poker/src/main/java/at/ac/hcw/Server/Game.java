package at.ac.hcw.Server;

public class Game {
    private int smallBlind;
    private int bigBlind;
    private Player[] players;

    public Game(int smallBlind, int bigBlind, Player[] players) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.players = players;
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
            if (!players[i].equals(player)) {
                players[i] = player;
                break;
            }
        }
    }

    public Card[] createDeck() {
        Card[] deck = new Card[52];
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 12; j++) {
                deck[i*13+j] = new Card(i,j);
            }
        }
        return deck;
    }

    public int compare(Player a, Player b) {
        int num = a.getPoints();

        if (num == 9) {
            int aHigh = 0;
            int bHigh = 0;
            for (int i = 0; i < 7; i++) {
                for (int j = 12; j >= 5; j--) {
                    if (a.getHand()[i].getNum() == j) {

                    }
                }
            }
        }

        return 0;
    }
}
