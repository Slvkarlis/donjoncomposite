public class BossMonster extends ElementDecorator {
    public BossMonster(DungeonElement wrapped) {
        super(wrapped);
    }

    @Override
    public int threat() { return wrapped.threat() * 3; }

    @Override
    public double getTreasureValue() { return wrapped.getTreasureValue() * 2; }

    @Override
    public double collect(Player player) { return wrapped.collect(player) * 2; }

    @Override
    public String describe() { return "BOSS " + wrapped.describe(); }
}
