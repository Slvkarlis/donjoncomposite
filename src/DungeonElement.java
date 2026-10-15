public interface DungeonElement {
    String describe();

    void display(String indent);

    int countMonsters();

    double getTreasureValue();

    int threat();

    double collect(Player player);

    void wakeUp();

    void sleep();
}
