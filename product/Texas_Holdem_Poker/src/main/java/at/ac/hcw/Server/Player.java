package at.ac.hcw.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Player {
    private String name;
    private int budget;
    private int bet;       // Aktueller Einsatz in dieser Wettrunde
    private int totalBet;  // Gesamteinsatz in der Hand (für Side-Pots relevant)
    private Card[] hand = new Card[7];     // Index 0-1: Handkarten, 2-6: Board
    private Card[] endHand = new Card[5];  // Die beste 5er Kombination
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

    public Card[] getEndHand() {
        return endHand;
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
        Arrays.fill(endHand, null);
    }

    public Card[] sortHand() {
        Card[] sortedHand = new Card[5];
        // Kopiere endHand sicherheitshalber, um null-Fehler zu vermeiden
        if (endHand[0] == null) return sortedHand;

        System.arraycopy(endHand, 0, sortedHand, 0, endHand.length);

        for (int i = 0; i < sortedHand.length; i++) {
            for (int j = i+1; j < sortedHand.length; j++) {
                if (sortedHand[i] != null && sortedHand[j] != null) {
                    if (sortedHand[i].getNum() < sortedHand[j].getNum()) {
                        Card tmp = sortedHand[i];
                        sortedHand[j] = sortedHand[i];
                        sortedHand[i] = tmp;
                    }
                }
            }
        }
        return sortedHand;
    }

    // --- DEINE ORIGINALE GEWINN-LOGIK ---

    public int getPoints() {
        ArrayList<Card> tempCards = new ArrayList<>();
        for (Card c : hand) {
            if (c != null) tempCards.add(c);
        }
        Collections.sort(tempCards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return Integer.compare(o2.getNum(), o1.getNum()); // o2 - o1 für descending
            }
        });
        Card[] sortedHand = new Card[7];
        for(int i=0; i<tempCards.size(); i++) {
            sortedHand[i] = tempCards.get(i);
        }
        this.hand = sortedHand;
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
                                                endHand[0] = hand[i]; // Ass
                                                endHand[1] = hand[j]; // König
                                                endHand[2] = hand[k]; // Dame
                                                endHand[3] = hand[l]; // Bube
                                                endHand[4] = hand[m]; // 10
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
                                                    endHand[0] = hand[i];
                                                    for (int n = 1; n < endHand.length; n++) {
                                                        endHand[n] = new Card(hand[j].getSuit(), hand[j].getNum() + n);
                                                    }
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
                                                    for (int n = 0; n < endHand.length; n++) {
                                                        endHand[n] = new Card(hand[i].getSuit(), hand[i].getNum() + n);
                                                    }
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
                    for (int k = 0; k < 4; k++) {
                        endHand[k] = new Card(k, hand[i].getNum());
                    }
                    Card fifth = new Card(0,0);
                    for (int l = 0; l < hand.length; l++) {
                        Card max =  new Card(0,0);
                        if (hand[l] != null && hand[l].getNum() > max.getNum() && hand[l].getNum() != hand[i].getNum()) {
                            max = hand[l];
                        }
                    }
                    endHand[4] = fifth;
                    return 7;
                }
            }
        }

        //Full House
        for (int i = 0; i < hand.length - 2; i++) {
            if (hand[i] != null && hand[i+1] != null && hand[i+2] != null) {
                if (hand[i].getNum() == hand[i+1].getNum() && hand[i].getNum() == hand[i+2].getNum()) {
                    // Drilling gefunden bei i. Suche jetzt Paar.
                    for (int j = 0; j < hand.length - 1; j++) {
                        // Paar darf nicht Teil des Drillings sein
                        if (j == i || j == i+1 || j == i+2) continue;
                        if (j+1 == i || j+1 == i+1 || j+1 == i+2) continue;

                        if (hand[j] != null && hand[j+1] != null && hand[j].getNum() == hand[j+1].getNum()) {
                            // Full House gefunden!
                            endHand[0] = hand[i];
                            endHand[1] = hand[i+1];
                            endHand[2] = hand[i+2];
                            endHand[3] = hand[j];
                            endHand[4] = hand[j+1];
                            return 6;
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
                    // Logic for flush sorting
                    if (count == 6) {
                        int min = 12;
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k] != null && hand[k].getNum() < min) {
                                min = hand[k].getNum();
                            }
                        }
                        for (int l = 0; l < endHand.length; l++) {
                            for (int m = 0; m < hand.length; m++) {
                                if (hand[m] != null && hand[m].getSuit() == hand[i].getSuit() && hand[m].getNum() != min) {
                                    endHand[l] = hand[m];
                                    break;
                                }
                            }
                        }
                    }
                    else if (count == 7) {
                        int min1 = 12;
                        int min2 = 11;
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k] != null && hand[k].getNum() < min1) {
                                min1 = hand[k].getNum();
                            }
                        }
                        for (int l = 0; l < hand.length; l++) {
                            if (hand[l] != null && hand[l].getNum() < min2 && hand[l].getNum() != min1) {
                                min2 = hand[l].getNum();
                            }
                        }
                        for (int m = 0; m < endHand.length; m++) {
                            for (int n = 0; n < hand.length; n++) {
                                if (hand[n] != null && hand[n].getSuit() == hand[i].getSuit() && hand[n].getNum() != min1 && hand[n].getNum() != min2) {
                                    endHand[m] = hand[n];
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < endHand.length; k++) {
                            for (int l = 0; l < hand.length; l++) {
                                if (hand[l] != null && hand[l].getSuit() == hand[i].getSuit()) {
                                    endHand[k] = hand[l];
                                    break;
                                }
                            }
                        }
                    }
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
                                                    endHand[0] = hand[i];
                                                    for (int n = 1; n < endHand.length; n++) {
                                                        endHand[n] = new Card(hand[i].getSuit(), hand[i].getNum() + n);
                                                    }
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
                                                    for (int n = 0; n < endHand.length; n++) {
                                                        endHand[n] = new Card(hand[i].getSuit(), hand[i].getNum() + n);
                                                    }
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
                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < hand.length; l++) {
                            if (hand[l] != null && hand[l].getNum() == hand[i].getNum()){
                                endHand[k] = hand[l];
                                break;
                            }
                        }
                    }
                    Card card4 = new Card(0,0);
                    Card card5 = new Card(0,0);
                    for (int k = 0; k < hand.length; k++) {
                        if (hand[k] != null && hand[k].getNum() > card4.getNum() && hand[k].getNum() != hand[i].getNum()) {
                            card4 = hand[k];
                        }
                        if (hand[k] != null && hand[k].getNum() > card5.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != card4.getNum()) {
                            card5 = hand[k];
                        }
                    }
                    endHand[3] = card4;
                    endHand[4] = card5;
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
                                    endHand[0] = hand[i];
                                    endHand[1] = hand[j];
                                    endHand[2] = hand[k];
                                    endHand[3] = hand[l];
                                    Card card5 = new Card(0,0);
                                    for (int m = 0; m < hand.length; m++) {
                                        if (hand[m] != null && hand[m].getNum() > card5.getNum() && card5.getNum() != hand[i].getNum() && card5.getNum() != hand[k].getNum()) {
                                            card5 = hand[m];
                                        }
                                    }
                                    endHand[4] = card5;
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
                    endHand[0] = hand[i];
                    endHand[1] = hand[j];
                    for (int k = 0; k < hand.length; k++) {
                        Card card3 = new Card(0,0);
                        if (hand[k] != null && hand[k].getNum() > card3.getNum() && hand[k].getNum() != hand[i].getNum()) {
                            card3 = hand[k];
                        }
                        endHand[2] = card3;
                    }
                    for (int k = 0; k < hand.length; k++) {
                        Card card4 = new Card(0,0);
                        if (hand[k] != null && hand[k].getNum() > card4.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != endHand[2].getNum()) {
                            card4 = hand[k];
                        }
                        endHand[3] = card4;
                    }
                    for (int k = 0; k < hand.length; k++) {
                        Card card5 = new Card(0,0);
                        if (hand[k] != null && hand[k].getNum() > card5.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != endHand[2].getNum() && hand[k].getNum() != endHand[3].getNum()) {
                            card5 = hand[k];
                        }
                        endHand[4] = card5;
                    }
                    return 1;
                }
            }
        }
        // 1. Alle verfügbaren Karten in ein temporäres Array kopieren
        Card[] allCards = new Card[7];
        int cardCount = 0;
        for(Card c : hand) {
            if(c != null) {
                allCards[cardCount++] = c;
            }
        }

        // 2. Sortieren (Bubble Sort, absteigend nach num)
        for(int i = 0; i < cardCount; i++) {
            for(int j = i + 1; j < cardCount; j++) {
                if(allCards[j].getNum() > allCards[i].getNum()) {
                    Card temp = allCards[i];
                    allCards[i] = allCards[j];
                    allCards[j] = temp;
                }
            }
        }

        // 3. Die Top 5 in endHand speichern
        for(int k = 0; k < 5; k++) {
            if (k < cardCount) {
                endHand[k] = allCards[k];
            }
        }

        return 0;
    }
}