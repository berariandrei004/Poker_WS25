package at.ac.hcw.Server;

import java.util.Arrays;
import java.util.Scanner;

public class Player {
    private String name;
    private int budget;
    private int bet;
    private int totalBet;
    private Card[] hand = new Card[7];
    private Card[] endHand = new Card[5];
    private boolean isAllin = false;
    private boolean folded = false;

    public Player(String name, int budget) {
        this.name = name;
        this.budget = budget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public void setTotalBet(int totalBet) {
        this.totalBet = totalBet;
    }

    public void setAllin(boolean allin) {
        isAllin = allin;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public Card[] getHand() {
        return hand;
    }

    public Card[] getEndHand() {
        return endHand;
    }

    public void winPot(int money) {
        budget += money;
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

    public void addToBet(int amount) {
        bet += amount;
        totalBet += amount;
    }

    public void resetForNewRound() {
        bet = 0;
        totalBet = 0;
        folded = false;
        isAllin = false;
        Arrays.fill(hand, null);
        Arrays.fill(endHand, null);
    }

    public Card[] sortHand() {
        Card[] sortedHand = new Card[5];
        for (int i = 0; i < sortedHand.length; i++) {
            sortedHand[i] = endHand[i];
        }
        for (int i = 0; i < sortedHand.length; i++) {
            for (int j = i+1; j < sortedHand.length; j++) {
                if (sortedHand[i].getNum() < sortedHand[j].getNum()) {
                    Card tmp = sortedHand[i];
                    sortedHand[i] = sortedHand[j];
                    sortedHand[i] = tmp;
                }
            }
        }
        return sortedHand;
    }

    public void check() {
    }

    public void call(int amount) {
        if (amount >= budget) {
            addToBet(budget);
            budget = 0;
            isAllin = true;
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

    public void fold() {
        folded = true;
    }

    public int allIn() {
        int amount = budget;
        addToBet(amount);
        budget = 0;
        isAllin = true;
        return amount;
    }

    public void receiveCard(Card card, int index) {
        hand[index] = card;
    }

    public int getPoints() {

        // Royal Flush
        for (int i = 0; i < hand.length; i++) {
            if (hand[i].getNum() == 12) {
                for (int j = 0; j < hand.length; j++) {
                    if (hand[j].getNum() == 11 && hand[i].getSuit() == hand[j].getSuit()) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() == 10 && hand[k].getSuit() == hand[i].getSuit()) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l].getNum() == 9 && hand[l].getSuit() == hand[i].getSuit()) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m].getNum() == 8 && hand[m].getSuit() == hand[i].getSuit()){
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
                if (hand[i].getNum() == 12) {
                    if (hand[j].getNum() == 0 && hand[j].getSuit() == hand[i].getSuit()) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() == 1 && hand[k].getSuit() == hand[i].getSuit()) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l].getNum() == 2 && hand[i].getSuit() == hand[l].getSuit()) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m].getNum() == 3 && hand[m].getSuit() == hand[i].getSuit()) {
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
                }
                else {
                    if (hand[j].getNum() == hand[i].getNum() + 1 && hand[j].getSuit() == hand[i].getSuit()) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() == hand[i].getNum() + 2 && hand[k].getSuit() == hand[i].getSuit()) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l].getNum() == hand[i].getNum() + 3 && hand[i].getSuit() == hand[l].getSuit()) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m].getNum() == hand[i].getNum() + 4 && hand[m].getSuit() == hand[i].getSuit()) {
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

        //Vierling
        for (int i = 0; i < 4; i++) {
            int count = 1;
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i].getNum() == hand[j].getNum()) {
                    count++;
                }
                if (count == 4) {
                    for (int k = 0; k < 4; k++) {
                        endHand[k] = new Card(k, hand[i].getNum());
                    }
                    Card fifth = new Card(0,0);
                    for (int l = 0; l < hand.length; l++) {
                        Card max =  new Card(0,0);
                        if (hand[l].getNum() > max.getNum() && hand[l].getNum() != hand[i].getNum()) {
                            max = hand[l];
                        }
                    }
                    endHand[4] = fifth;
                    return 7;
                }
            }
        }

        //Full House
        for (int i = 0; i < 3; i++) {
            for (int j = i+1; j < hand.length; j++) {
                if (hand[i].getNum() == hand[j].getNum()) {
                    for (int k = 0; k < 5; k++) {
                        if (hand[i].getNum() != hand[k].getNum()) {
                            int count = 1;
                            for (int l = k+1; l < hand.length; l++) {
                                if (hand[i].getNum() == hand[l].getNum()) {
                                    count += 1;
                                }
                                if (count == 3) {
                                    for (int m = 0; m < 3; m++) {
                                        for (int n = 0; n < 7; n++) {
                                            if (hand[n].getNum() == hand[i].getNum()) {
                                                endHand[n] = hand[i];
                                            }
                                        }
                                    }
                                    for (int m = 3; m < 5; m++) {
                                        for (int n = 0; n < 7; n++) {
                                            if (hand[n].getNum() == hand[j].getNum()) {
                                                endHand[n] = hand[j];
                                            }
                                        }
                                    }
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
                if (hand[i].getSuit() == hand[j].getSuit()) {
                    count++;
                }
                if (count >= 5) {
                    if (count == 6) {
                        int min = 12;
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() < min) {
                                min = hand[k].getNum();
                            }
                        }
                        for (int l = 0; l < endHand.length; l++) {
                            for (int m = 0; m < hand.length; m++) {
                                if (hand[m].getSuit() == hand[i].getSuit() && hand[m].getNum() != min) {
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
                            if (hand[k].getNum() < min1) {
                                min1 = hand[k].getNum();
                            }
                        }
                        for (int l = 0; l < hand.length; l++) {
                            if (hand[l].getNum() < min2 && hand[l].getNum() != min1) {
                                min2 = hand[l].getNum();
                            }
                        }
                        for (int m = 0; m < endHand.length; m++) {
                            for (int n = 0; n < hand.length; n++) {
                                if (hand[n].getSuit() == hand[i].getSuit() && hand[n].getNum() != min1 && hand[n].getNum() != min2) {
                                    endHand[m] = hand[n];
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < endHand.length; k++) {
                            for (int l = 0; l < hand.length; l++) {
                                if (hand[l].getSuit() == hand[i].getSuit()) {
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
                if (hand[i].getNum() == 12) {
                    if (hand[j].getNum() == 0) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() == 1) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l].getNum() == 2) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m].getNum() == 3) {
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
                }
                else {
                    if (hand[j].getNum() == hand[i].getNum() + 1) {
                        for (int k = 0; k < hand.length; k++) {
                            if (hand[k].getNum() == hand[i].getNum() + 2) {
                                for (int l = 0; l < hand.length; l++) {
                                    if (hand[l].getNum() == hand[i].getNum() + 3) {
                                        for (int m = 0; m < hand.length; m++) {
                                            if (hand[m].getNum() == hand[i].getNum() + 4) {
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

        //Drilling
        for (int i = 0; i < 5; i++) {
            int count = 1;
            for (int j = i + 1; j < hand.length; j++) {
                if (hand[i].getNum() == hand[j].getNum()) {
                    count++;
                }
                if (count == 3) {
                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < hand.length; l++) {
                            if (hand[l].getNum() == hand[i].getNum()){
                                endHand[k] = hand[l];
                                break;
                            }
                        }
                    }
                    Card card4 = new Card(0,0);
                    Card card5 = new Card(0,0);
                    for (int k = 0; k < hand.length; k++) {
                        if (hand[k].getNum() > card4.getNum() && hand[k].getNum() != hand[i].getNum()) {
                            card4 = hand[k];
                        }
                        if (hand[k].getNum() > card5.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != card4.getNum()) {
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
                if (hand[i].getNum() == hand[j].getNum()) {
                    for (int k = i + 1; k < 6; k++) {
                        if (hand[k].getNum() != hand[i].getNum()) {
                            for (int l = k + 1; l < hand.length; l++) {
                                if (hand[k].getNum() == hand[l].getNum()) {
                                    endHand[0] = hand[i];
                                    endHand[1] = hand[j];
                                    endHand[2] = hand[k];
                                    endHand[3] = hand[l];
                                    Card card5 = new Card(0,0);
                                    for (int m = 0; m < hand.length; m++) {
                                        if (hand[m].getNum() > card5.getNum() && card5.getNum() != hand[i].getNum() && card5.getNum() != hand[k].getNum()) {
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
                if (hand[i].getNum() == hand[j].getNum()) {
                    endHand[0] = hand[i];
                    endHand[1] = hand[j];
                    for (int k = 0; k < hand.length; k++) {
                        Card card3 = new Card(0,0);
                        if (hand[k].getNum() > card3.getNum() && hand[k].getNum() != hand[i].getNum()) {
                            card3 = hand[k];
                        }
                        endHand[2] = card3;
                    }
                    for (int k = 0; k < hand.length; k++) {
                        Card card4 = new Card(0,0);
                        if (hand[k].getNum() > card4.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != endHand[2].getNum()) {
                            card4 = hand[k];
                        }
                        endHand[3] = card4;
                    }
                    for (int k = 0; k < hand.length; k++) {
                        Card card5 = new Card(0,0);
                        if (hand[k].getNum() > card5.getNum() && hand[k].getNum() != hand[i].getNum() && hand[k].getNum() != endHand[2].getNum() && hand[k].getNum() != endHand[3].getNum()) {
                            card5 = hand[k];
                        }
                        endHand[4] = card5;
                    }
                    return 1;
                }
            }
        }

        return 0;
    }
}
