public class EliteMonster extends ElementDecorator {
    public EliteMonster(DungeonElement wrapped) {
        super(wrapped);
    }

    @Override
    public int countMonsters() { return wrapped.countMonsters() * 2; }

    @Override
    public int threat() { return wrapped.threat() * 2; }

    @Override
    public double getTreasureValue() { return wrapped.getTreasureValue() * 2; }

    @Override
    public double collect(Player player) { return wrapped.collect(player) * 2; }

    @Override
    public String describe() { return "Elite " + wrapped.describe(); }
}
