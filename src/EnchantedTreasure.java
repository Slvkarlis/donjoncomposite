public class EnchantedTreasure extends ElementDecorator {
    private final DungeonElement boss;

    public EnchantedTreasure(DungeonElement wrapped, DungeonElement boss) {
        super(wrapped);
        this.boss = boss;
    }

    private boolean sealed() { return boss.countMonsters() > 0; }

    @Override
    public double getTreasureValue() { return wrapped.getTreasureValue() * 2; }

    @Override
    public double collect(Player player) { return sealed() ? 0 : wrapped.collect(player) * 2; }

    @Override
    public String describe() {
        return "Enchanted " + wrapped.describe() + " x2" + (sealed() ? " - sealed by the boss" : "");
    }
}
