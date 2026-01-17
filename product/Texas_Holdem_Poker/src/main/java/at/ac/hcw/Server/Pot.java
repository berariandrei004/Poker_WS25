package at.ac.hcw.Server;

import java.util.ArrayList;

public class Pot {
    private int index;
    private int money;
    private ArrayList<Player> players;

    public Pot(int index) {
        this.index = index;
        this.money = 0;
        this.players = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void raisePot(int money) {
        if (money > 0) {
            this.money += money;
        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player p) {
        if (!players.contains(p)) {
            players.add(p);
        }
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    public int getHighestBet() {
        int highest = 0;
        for (Player p : players) {
            if (p.getBet() > highest) {
                highest = p.getBet();
            }
        }
        return highest;
    }
}
