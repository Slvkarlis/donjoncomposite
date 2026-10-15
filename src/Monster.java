public class Monster implements DungeonElement {
    private static final int BOUNTY_PER_LEVEL = 20;

    private final String name;
    private final int dangerLevel;
    private MonsterState state;

    public Monster(String name, int dangerLevel, MonsterState state) {
        this.name = name;
        this.dangerLevel = dangerLevel;
        this.state = state;
    }

    public int getDangerLevel() { return dangerLevel; }

    public void setState(MonsterState state) { this.state = state; }

    public void wakeUp() { state.wakeUp(this); }

    public void sleep() { state.sleep(this); }

    public void defeat() { state.defeat(this); }

    public int countMonsters() { return state.count(); }

    public double getTreasureValue() { return state.count() * dangerLevel * BOUNTY_PER_LEVEL; }

    public int threat() { return state.attack(this); }

    public double collect(Player player) {
        double bounty = getTreasureValue();
        defeat();
        return bounty;
    }

    public String describe() {
        return name + " (danger " + dangerLevel + ", " + state.label() + ")";
    }

    public void display(String indent) {
        System.out.println(indent + describe());
    }
}
