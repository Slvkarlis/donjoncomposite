public class CursedTreasure extends ElementDecorator {
    public CursedTreasure(DungeonElement wrapped) {
        super(wrapped);
    }

    @Override
    public double collect(Player player) {
        double taken = wrapped.collect(player);
        if (taken > 0) player.curse();
        return taken;
    }

    @Override
    public String describe() { return "Cursed " + wrapped.describe() + " - halves all later gold"; }
}
