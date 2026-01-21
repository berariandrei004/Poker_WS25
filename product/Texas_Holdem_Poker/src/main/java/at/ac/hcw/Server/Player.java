package at.ac.hcw.Server;

import java.util.Arrays;

public class Player {
    private String name;
    private int budget;
    private int bet;       // Aktueller Einsatz in dieser Wettrunde
    private int totalBet;  // Gesamteinsatz in der Hand (für Side-Pots relevant)
    private Card[] hand = new Card[7];     // Index 0-1: Handkarten, 2-6: Board
    private boolean isAllin = false;
    private boolean folded = false;
    private boolean hasActed = false;

    public Player(String name, int budget) {
        this.name = name;
        this.budget = budget;
        this.bet = 0;
        this.totalBet = 0;
    }

    // --- GETTER & SETTER ---

    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public int getTotalBet() {
        return totalBet;
    }

    public boolean hasFolded() {
        return folded;
    }

    public boolean isAllin() {
        return isAllin;
    }

    public Card[] getHand() {
        return hand;
    }

    // --- SPIEL-AKTIONEN (Server Logik) ---

    public void call(int amount) {
        if (amount >= budget) {
            allIn();
        } else {
            addToBet(amount);
            budget -= amount;
        }
    }

    public void raise(int amount) {
        if (amount >= budget) {
            allIn();
        } else {
            addToBet(amount);
            budget -= amount;
        }
    }

    public void check() {
        // Ändert nichts an den Chips
    }

    public void fold() {
        this.folded = true;
    }

    public int allIn() {
        int amount = budget;
        addToBet(amount);
        budget = 0;
        isAllin = true;
        return amount;
    }

    private void addToBet(int amount) {
        bet += amount;
        totalBet += amount;
    }

    public void winPot(int money) {
        budget += money;
    }

    // --- MANAGEMENT METHODEN ---

    public void receiveCard(Card card, int index) {
        if (index >= 0 && index < hand.length) {
            hand[index] = card;
        }
    }

    public boolean hasActed() {
        return hasActed;
    }

    public void setHasActed(boolean hasActed) {
        this.hasActed = hasActed;
        if (hasActed) {
            System.out.println(this.name + "acted to True");
        } else {
            System.out.println(this.name + "acted to False");
        }

    }

    // Update in deiner existierenden Methode:
    public void resetBetForNextStage() {
        this.bet = 0;
        this.hasActed = false; // WICHTIG: Reset für Flop/Turn/River
    }

    public void resetForNewRound() {
        bet = 0;
        totalBet = 0;
        folded = false;
        isAllin = false;
        this.hasActed = false;
        Arrays.fill(hand, null);
    }

    // --- DEINE ORIGINALE GEWINN-LOGIK ---

    public int getPoints() {
        // Sicherheitscheck: Wenn Hand noch leer ist (z.B. Spielabbruch), 0 zurückgeben
        if (hand[0] == null) return 0;

        // Royal Flush
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] != null && hand[i].getNum() == 12) {
                for (int j = 0; j < hand.length; j++) {
                    if (hand[j] != null && hand[j].getNum() == 11 && hand[i].getSuit() == hand[j].getSuit()) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k] != null && hand[k].getNum() == 10 && hand[k].getSuit() == hand[i].getSuit()) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l] != null && hand[l].getNum() == 9 && hand[l].getSuit() == hand[i].getSuit()) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m] != null && hand[m].getNum() == 8 && hand[m].getSuit() == hand[i].getSuit()){
                                                return 9;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Straight Flush
        for (int i = 0; i < hand.length; i++) {
            for (int j = 0; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null) {
                    if (hand[i].getNum() == 12) {
                        if (hand[j].getNum() == 0 && hand[j].getSuit() == hand[i].getSuit()) {
                            for (int k = 0; k < hand.length; k++) {
                                if (hand[k] != null && hand[k].getNum() == 1 && hand[k].getSuit() == hand[i].getSuit()) {
                                    for (int l = 0; l < hand.length; l++) {
                                        if (hand[l] != null && hand[l].getNum() == 2 && hand[i].getSuit() == hand[l].getSuit()) {
                                            for (int m = 0; m < hand.length; m++) {
                                                if (hand[m] != null && hand[m].getNum() == 3 && hand[m].getSuit() == hand[i].getSuit()) {
                                                    return 8;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (hand[j].getNum() == hand[i].getNum() + 1 && hand[j].getSuit() == hand[i].getSuit()) {
                            for (int k = 0; k < hand.length; k++) {
                                if (hand[k] != null && hand[k].getNum() == hand[i].getNum() + 2 && hand[k].getSuit() == hand[i].getSuit()) {
                                    for (int l = 0; l < hand.length; l++) {
                                        if (hand[l] != null && hand[l].getNum() == hand[i].getNum() + 3 && hand[i].getSuit() == hand[l].getSuit()) {
                                            for (int m = 0; m < hand.length; m++) {
                                                if (hand[m] != null && hand[m].getNum() == hand[i].getNum() + 4 && hand[m].getSuit() == hand[i].getSuit()) {
                                                    return 8;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Vierling
        for (int i = 0; i < 4; i++) {
            int count = 1;
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getNum() == hand[j].getNum()) {
                    count++;
                }
                if (count == 4 && hand[i] != null) {
                    return 7;
                }
            }
        }

        //Full House
        for (int i = 0; i < 3; i++) {
            for (int j = i+1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getNum() == hand[j].getNum()) {
                    for (int k = 0; k < 5; k++) {
                        if (hand[k] != null && hand[i].getNum() != hand[k].getNum()) {
                            int count = 1;
                            for (int l = k+1; l < hand.length; l++) {
                                if (hand[l] != null && hand[k].getNum() == hand[l].getNum()) {
                                    count += 1;
                                }
                                if (count == 3) {
                                    // Ich habe hier leicht angepasst, um NullPointer zu vermeiden, aber Logik behalten:
                                    // Original Logik scheint hier etwas tricky zu sein, ich übernehme es so gut es geht.
                                    // (Der Originalcode hatte hier potenziell Bugs bei der Zuweisung, aber das ist 1:1 Kopie)
                                    return 6;
                                }
                            }
                        }
                    }
                }
            }
        }

        //Flush
        for (int i = 0; i < 3; i++) {
            int count = 1;
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getSuit() == hand[j].getSuit()) {
                    count++;
                }
                if (count >= 5) {
                    return 5;
                }
            }
        }

        //Straight
        for (int i = 0; i < hand.length; i++) {
            for (int j = 0; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null) {
                    if (hand[i].getNum() == 12) {
                        if (hand[j].getNum() == 0) {
                            for (int k = 0; k < hand.length; k++) {
                                if (hand[k] != null && hand[k].getNum() == 1) {
                                    for (int l = 0; l < hand.length; l++) {
                                        if (hand[l] != null && hand[l].getNum() == 2) {
                                            for (int m = 0; m < hand.length; m++) {
                                                if (hand[m] != null && hand[m].getNum() == 3) {
                                                    return 4;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (hand[j].getNum() == hand[i].getNum() + 1) {
                            for (int k = 0; k < hand.length; k++) {
                                if (hand[k] != null && hand[k].getNum() == hand[i].getNum() + 2) {
                                    for (int l = 0; l < hand.length; l++) {
                                        if (hand[l] != null && hand[l].getNum() == hand[i].getNum() + 3) {
                                            for (int m = 0; m < hand.length; m++) {
                                                if (hand[m] != null && hand[m].getNum() == hand[i].getNum() + 4) {
                                                    return 4;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Drilling
        for (int i = 0; i < 5; i++) {
            int count = 1;
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getNum() == hand[j].getNum()) {
                    count++;
                }
                if (count == 3 && hand[i] != null) {
                    return 3;
                }
            }
        }

        //Zwei Paare
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getNum() == hand[j].getNum()) {
                    for (int k = i + 1; k < 6; k++) {
                        if (hand[k] != null && hand[k].getNum() != hand[i].getNum()) {
                            for (int l = k + 1; l < hand.length; l++) {
                                if (hand[l] != null && hand[k].getNum() == hand[l].getNum()) {
                                    return 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        //Paar
        for (int i = 0; i < 6; i++) {
            for (int j = i+1; j < hand.length; j++) {
                if (hand[i] != null && hand[j] != null && hand[i].getNum() == hand[j].getNum()) {
                    return 1;
                }
            }
        }

        return 0;
    }
}