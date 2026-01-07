package at.ac.hcw;

import java.util.*;

public class Deck {

    private final List<String> cards = new ArrayList<>();
    private int index = 0;

    public Deck() {
        String[] suits = {"clubs", "diamonds", "hearts", "spades"};
        String[] ranks = {
                "2","3","4","5","6","7","8","9","10",
                "jack","queen","king","ace"
        };

        for (String s : suits) {
            for (String r : ranks) {
                cards.add(r + "_of_" + s + ".jpg");
            }
        }

        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
        index = 0;
    }

    public String draw() {
        if (index >= cards.size()) throw new IllegalStateException("Deck is empty");
        return cards.get(index++);
    }
}