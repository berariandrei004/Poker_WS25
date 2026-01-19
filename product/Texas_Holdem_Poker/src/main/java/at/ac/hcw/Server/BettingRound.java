package at.ac.hcw.Server;

import java.util.List;

public class BettingRound {

    private final List<Player> players;
    private int currentPlayerIndex;

    private int currentBet;
    private int lastRaiserIndex;

    private boolean finished;

    public BettingRound(List<Player> players, int startIndex) {
        this.players = players;
        this.currentPlayerIndex = startIndex;
        this.currentBet = 0;
        this.lastRaiserIndex = startIndex;
        this.finished = false;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public boolean isFinished() {
        return finished;
    }

    public void fold(Player p) {
        p.setFolded(true);
        advanceTurn();
    }

    public void call(Player p) {
        int toCall = currentBet - p.getBet();
        p.call(toCall);
        advanceTurn();
    }

    public void raise(Player p, int newTotalBet) {
        int toAdd = newTotalBet - p.getBet();
        if (toAdd <= 0) {
            throw new IllegalArgumentException("Raise too small");
        }

        currentBet = newTotalBet;
        p.call(toAdd);
        lastRaiserIndex = currentPlayerIndex;
        advanceTurn();
    }

    private void advanceTurn() {

        int checked = 0;

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            checked++;
        } while (checked <= players.size() && players.get(currentPlayerIndex).hasFolded() ||  players.get(currentPlayerIndex).isAllin());

        if (checked > players.size() || currentPlayerIndex == lastRaiserIndex) {
            finished = true;
        }
    }
}