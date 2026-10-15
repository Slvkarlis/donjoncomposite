public class Treasure implements DungeonElement {
    private final String name;
    private double value;

    public Treasure(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public int countMonsters() { return 0; }

    public double getTreasureValue() { return value; }

    public int threat() { return 0; }

    public double collect(Player player) {
        double taken = value;
        value = 0;
        return taken;
    }

    public void wakeUp() { }

    public void sleep() { }

    public String describe() {
        return value > 0 ? name + " (" + (int) value + " gold)" : name + " (taken)";
    }

    public void display(String indent) {
        System.out.println(indent + describe());
    }
}
