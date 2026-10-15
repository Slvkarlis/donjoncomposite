import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public final class GameManager {
    static final int FLOORS = 5;
    static final int REST_HEAL = 10;
    private static final double WAKE_CHANCE = 0.35;
    private static final String[] MONSTERS = {"Goblin", "Skeleton", "Orc", "Troll", "Wraith", "Ogre"};
    private static final String[] TREASURES = {"Coin pouch", "Silver chalice", "Gem", "Gold idol", "Old crown"};

    private static GameManager instance;

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();
    private final Player player = new Player();
    private final StringBuilder log = new StringBuilder();
    private final Room root;

    private GameManager() {
        root = buildDungeon();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public Room getRoot() { return root; }

    public Player getPlayer() { return player; }

    // returns everything said since the last call and clears it
    public String takeLog() {
        String text = log.toString().trim();
        log.setLength(0);
        return text;
    }

    public void run() {
        double total = root.getTreasureValue();
        double inChosenRooms = 0;
        double bestPath = 0;
        int cleared = 0;
        List<String> report = new ArrayList<>();

        say("=== DUNGEON ===");
        say("Cross " + FLOORS + " floors alive, one room per floor. Grab all the gold you can.");

        try {
            for (DungeonElement floorElement : root.getChildren()) {
                Room floor = (Room) floorElement;
                List<DungeonElement> doors = floor.getChildren();
                double bestHere = doors.stream().mapToDouble(DungeonElement::getTreasureValue).max().orElse(0);

                refresh();
                System.out.println("--- " + floor.getName() + " ---  HP " + player.getHealth()
                        + " | gold " + (int) player.getMoney());
                list(doors);
                Room room = (Room) doors.get(pick("Which room do you enter? ", doors.size()));

                double available = room.getTreasureValue();
                double before = player.getMoney();
                explore(room);
                double gained = player.getMoney() - before;

                inChosenRooms += available;
                bestPath += bestHere;
                report.add(String.format("%-9s %-14s %9.0f %9.0f %9.0f",
                        floor.getName(), room.getName(), gained, available, bestHere));

                if (!player.isAlive()) break;
                cleared++;
                player.heal(REST_HEAL);
            }
        } catch (NoSuchElementException e) {
            say("Input closed, leaving the dungeon.");
        }

        refresh();
        System.out.println(cleared == FLOORS ? "*** YOU ESCAPED THE DUNGEON ***"
                : player.isAlive() ? "*** YOU GAVE UP ON FLOOR " + (cleared + 1) + " ***"
                : "*** YOU DIED ON FLOOR " + (cleared + 1) + " ***");
        System.out.println("\nWhat was left behind:");
        root.display("");
        System.out.println(String.format("%n%-9s %-14s %9s %9s %9s", "Floor", "Room", "Got", "In room", "Best room"));
        report.forEach(System.out::println);
        System.out.println(String.format("%nYour gold               : %.0f", player.getMoney()));
        System.out.println(String.format("In the rooms you entered: %.0f (%.0f%% taken)",
                inChosenRooms, percent(player.getMoney(), inChosenRooms)));
        System.out.println(String.format("Best rooms on your floors: %.0f (%.0f%% taken)",
                bestPath, percent(player.getMoney(), bestPath)));
        System.out.println(String.format("Whole dungeon           : %.0f (%.0f%% taken)",
                total, percent(player.getMoney(), total)));
    }

    private void explore(Room room) {
        List<DungeonElement> items = room.getChildren();
        while (player.isAlive()) {
            refresh();
            System.out.println(room.getName() + " | HP " + player.getHealth() + " | gold "
                    + (int) player.getMoney() + (player.isCursed() ? " | CURSED x0.5" : ""));
            list(items);
            String input = ask("Number = fight/take/enter, L = leave: ");

            if (input.equals("l")) return;

            int index = parse(input) - 1;
            if (index < 0 || index >= items.size()) continue;
            DungeonElement target = items.get(index);

            if (target instanceof Room) {
                explore((Room) target);
                continue;
            }
            interact(target, items);
        }
    }

    // fight or loot one element of a room, then let the room react
    public void interact(DungeonElement target, List<DungeonElement> items) {
        if (target.countMonsters() == 0 && target.getTreasureValue() == 0) {
            say("Nothing left there.");
            return;
        }

        int damage = target.threat();
        double factor = player.getGoldFactor();
        double gain = target.collect(player) * factor;
        player.hurt(damage);
        player.earn(gain);
        if (gain == 0 && damage == 0) {
            say("It will not budge. Defeat the boss first.");
        } else {
            say("+" + (int) gain + " gold" + (damage > 0 ? ", -" + damage + " HP" : ""));
        }
        if (factor == 1 && player.isCursed()) {
            say("You are CURSED: all gold from now on is halved.");
        }

        strikeBack(items);
        makeNoise(items);
    }

    private void strikeBack(List<DungeonElement> items) {
        int damage = 0;
        for (DungeonElement e : items) {
            if (!(e instanceof Room)) damage += e.threat();
        }
        if (damage > 0) {
            player.hurt(damage);
            say("The awake monsters hit you: -" + damage + " HP");
        }
    }

    private void makeNoise(List<DungeonElement> items) {
        for (DungeonElement e : items) {
            if (e instanceof Room || random.nextDouble() >= WAKE_CHANCE) continue;
            int before = e.threat();
            e.wakeUp();
            if (e.threat() > before) say("The noise wakes: " + e.describe());
        }
    }

    private void say(String message) {
        log.append(message).append(System.lineSeparator());
    }

    private void refresh() {
        clearScreen();
        if (log.length() > 0) System.out.println(log);
        log.setLength(0);
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println();
        }
    }

    private void list(List<DungeonElement> items) {
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + items.get(i).describe());
        }
    }

    private int pick(String prompt, int max) {
        while (true) {
            int choice = parse(ask(prompt));
            if (choice >= 1 && choice <= max) return choice - 1;
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim().toLowerCase();
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double percent(double part, double whole) {
        return whole == 0 ? 0 : part * 100 / whole;
    }

    private Room buildDungeon() {
        Room dungeon = new Room("Dungeon");
        for (int f = 1; f <= FLOORS; f++) {
            Room floor = new Room("Floor " + f);
            int rooms = 2 + random.nextInt(2);
            for (int r = 0; r < rooms; r++) {
                Room room = buildRoom("Room " + f + (char) ('A' + r), f * 2);
                if (random.nextDouble() < 0.4) {
                    Room vault = new Room("Vault");
                    DungeonElement boss = new BossMonster(
                            new Monster("Guardian", Math.min(5, 2 + f * 2 / 3), new AwakeState()));
                    vault.add(boss);
                    vault.add(new EnchantedTreasure(new Treasure("Relic", 60 + f * 20), boss));
                    room.add(vault);
                }
                floor.add(room);
            }
            dungeon.add(floor);
        }
        return dungeon;
    }

    private Room buildRoom(String name, int floor) {
        Room room = new Room(name);
        int maxDanger = Math.min(5, 1 + floor / 2);

        int monsters = random.nextInt(4);
        for (int i = 0; i < monsters; i++) {
            MonsterState state = random.nextBoolean() ? new AwakeState() : new SleepingState();
            DungeonElement m = new Monster(MONSTERS[random.nextInt(MONSTERS.length)],
                    1 + random.nextInt(maxDanger), state);
            room.add(random.nextDouble() < 0.2 ? new EliteMonster(m) : m);
        }

        int treasures = 1 + random.nextInt(3);
        for (int i = 0; i < treasures; i++) {
            DungeonElement t = new Treasure(TREASURES[random.nextInt(TREASURES.length)],
                    20 + random.nextInt(40 + floor * 15));
            room.add(random.nextDouble() < 0.2 ? new CursedTreasure(t) : t);
        }
        return room;
    }
}
