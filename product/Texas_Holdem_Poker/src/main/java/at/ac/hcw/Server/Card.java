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
        String stringSuit;
        if (suit == 0) {
            stringSuit = "diamonds";
        }
        else if (suit == 1) {
            stringSuit = "hearts";
        }
        else if (suit == 2) {
            stringSuit = "spades";
        }
        else {
            stringSuit = "clubs";
        }

        if (this.num == 12) {
            return ("ace_of_" + stringSuit);
        }
        else if (num == 11) {
            return ("king_of_" + stringSuit);
        }
        else if (num == 10) {
            return ("queen_of_" + stringSuit);
        }
        else if (num == 9) {
            return ("jack_of_" + stringSuit);
        }
        else {
            return ((num+2) + "_of_" + stringSuit);
        }
    }
}
