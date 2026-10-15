public interface DungeonElement {
    String describe();

    void display(String indent);

    int countMonsters();

    double getTreasureValue();

    // damage dealt to whoever touches this element right now
    int threat();

    // takes the money out of the element and returns it
    double collect(Player player);

    void wakeUp();

    void sleep();
}
