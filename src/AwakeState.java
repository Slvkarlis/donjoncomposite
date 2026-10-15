public class AwakeState implements MonsterState {
    public int count() { return 1; }

    public String label() { return "awake"; }

    public int attack(Monster m) { return m.getDangerLevel() * 4; }

    public void wakeUp(Monster m) { }

    public void sleep(Monster m) { m.setState(new SleepingState()); }

    public void defeat(Monster m) { m.setState(new DefeatedState()); }
}
