public class Player {
    private static final int MAX_HEALTH = 100;

    private int health = MAX_HEALTH;
    private double money = 0;
    private double goldFactor = 1;

    public int getHealth() { return health; }

    public double getMoney() { return money; }

    public boolean isAlive() { return health > 0; }

    public void hurt(int damage) { health = Math.max(0, health - damage); }

    public void heal(int amount) { health = Math.min(MAX_HEALTH, health + amount); }

    public void earn(double amount) { money += amount; }

    public double getGoldFactor() { return goldFactor; }

    public boolean isCursed() { return goldFactor < 1; }

    public void curse() { goldFactor = 0.5; }
}
