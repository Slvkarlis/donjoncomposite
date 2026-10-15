public class SleepingState implements MonsterState {
    public int count() { return 1; }

    public String label() { return "sleeping"; }

    public int attack(Monster m) { return 0; }

    public void wakeUp(Monster m) { m.setState(new AwakeState()); }

    public void sleep(Monster m) { }

    public void defeat(Monster m) { m.setState(new DefeatedState()); }
}
