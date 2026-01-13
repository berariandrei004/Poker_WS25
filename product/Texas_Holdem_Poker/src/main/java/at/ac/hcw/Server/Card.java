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
            stringSuit = "Karo";
        }
        else if (suit == 1) {
            stringSuit = "Herz";
        }
        else if (suit == 2) {
            stringSuit = "Pik";
        }
        else {
            stringSuit = "Kreuz";
        }

        if (this.num == 12) {
            return (stringSuit + " Ass");
        }
        else if (num == 11) {
            return (stringSuit + " König");
        }
        else if (num == 10) {
            return (stringSuit + " Dame");
        }
        else if (num == 9) {
            return (stringSuit + " Bube");
        }
        else {
            return (stringSuit + (num + 2));
        }
    }
}
