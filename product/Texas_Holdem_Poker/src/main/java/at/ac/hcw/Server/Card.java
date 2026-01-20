package at.ac.hcw.Server;

public class Card implements Comparable<Card>{
    private int suit;
    private int num;

    public Card(int suit, int num) {
        this.suit = suit;
        this.num = num;
    }

    public int getSuit() {
        return suit;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public int compareTo(Card other) {
        return Integer.compare(this.num, other.num);
    }

    @Override
    public String toString() {
        String[] suits = {"diamonds", "hearts", "spades", "clubs"};
        String s = suits[suit];

        // Client erwartet: 2_of_clubs, jack_of_hearts, ace_of_spades
        if (num == 12) return "ace_of_" + s;
        if (num == 11) return "king_of_" + s;
        if (num == 10) return "queen_of_" + s;
        if (num == 9)  return "jack_of_" + s;

        // num 0 ist die "2", num 8 ist die "10"
        return (num + 2) + "_of_" + s;
    }
}
