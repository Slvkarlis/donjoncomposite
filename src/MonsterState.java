public interface MonsterState {
    int count();

    String label();

    int attack(Monster m);

    void wakeUp(Monster m);

    void sleep(Monster m);

    void defeat(Monster m);
}
