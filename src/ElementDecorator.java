public abstract class ElementDecorator implements DungeonElement {
    protected final DungeonElement wrapped;

    protected ElementDecorator(DungeonElement wrapped) {
        this.wrapped = wrapped;
    }

    public String describe() { return wrapped.describe(); }

    public void display(String indent) {
        System.out.println(indent + describe());
    }

    public int countMonsters() { return wrapped.countMonsters(); }

    public double getTreasureValue() { return wrapped.getTreasureValue(); }

    public int threat() { return wrapped.threat(); }

    public double collect(Player player) { return wrapped.collect(player); }

    public void wakeUp() { wrapped.wakeUp(); }

    public void sleep() { wrapped.sleep(); }
}
