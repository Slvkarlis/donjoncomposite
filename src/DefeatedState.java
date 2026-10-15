public class DefeatedState implements MonsterState {
    public int count() { return 0; }

    public String label() { return "defeated"; }

    public int attack(Monster m) { return 0; }

    public void wakeUp(Monster m) { }

    public void sleep(Monster m) { }

    public void defeat(Monster m) { }
}
