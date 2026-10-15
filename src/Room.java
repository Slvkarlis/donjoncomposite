import java.util.ArrayList;
import java.util.List;

public class Room implements DungeonElement {
    private final String name;
    private final List<DungeonElement> children = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void add(DungeonElement e) { children.add(e); }

    public void replace(int i, DungeonElement e) { children.set(i, e); }

    public List<DungeonElement> getChildren() { return children; }

    public int countMonsters() {
        return children.stream().mapToInt(DungeonElement::countMonsters).sum();
    }

    public double getTreasureValue() {
        return children.stream().mapToDouble(DungeonElement::getTreasureValue).sum();
    }

    public int threat() {
        return children.stream().mapToInt(DungeonElement::threat).sum();
    }

    public double collect(Player player) {
        return children.stream().mapToDouble(e -> e.collect(player)).sum();
    }

    public void wakeUp() { children.forEach(DungeonElement::wakeUp); }

    public void sleep() { children.forEach(DungeonElement::sleep); }

    public String describe() {
        return "[" + name + "]";
    }

    public void display(String indent) {
        System.out.println(indent + "[" + name + "] " + (int) getTreasureValue() + " gold left");
        for (DungeonElement child : children) {
            child.display(indent + "   ");
        }
    }
}
