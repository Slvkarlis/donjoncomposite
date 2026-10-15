import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game2D extends JPanel {
    private static final int TILE = 64;
    private static final int COLS = 11;
    private static final int ROWS = 8;
    private static final int HUD = 90;
    private static final int W = COLS * TILE;
    private static final int H = ROWS * TILE;
    private static final Point START = new Point(5, 6);
    private static final Point EXIT = new Point(5, 7);
    private static final int[] TORCHES = {2, 8};

    private static final Font PIXEL = new Font(Font.MONOSPACED, Font.BOLD, 14);
    private static final Font SMALL = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private static final Font BIG = new Font(Font.MONOSPACED, Font.BOLD, 34);
    private static final Color GOLD = new Color(255, 205, 70);

    private record Popup(String text, Color color, int x, int y, int born) { }

    private final GameManager game = GameManager.getInstance();
    private final Player player = game.getPlayer();
    private final Deque<Room> inside = new ArrayDeque<>(); // rooms we are in, innermost first
    private final List<Popup> popups = new ArrayList<>();
    private final Random flicker = new Random();
    private int floor = 0;
    private Point pos = new Point(START);
    private boolean facingLeft;
    private int frame;
    private int hurtFlash;
    private String message = "Arrows to move. Walk into things to fight, loot or enter.";
    private String ending;

    public Game2D() {
        setPreferredSize(new Dimension(W, H + HUD));
        setBackground(Sprites.OUTLINE);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> step(-1, 0);
                    case KeyEvent.VK_RIGHT -> step(1, 0);
                    case KeyEvent.VK_UP -> step(0, -1);
                    case KeyEvent.VK_DOWN -> step(0, 1);
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                    default -> { }
                }
                repaint();
            }
        });
        new Timer(100, e -> {
            frame++;
            if (hurtFlash > 0) hurtFlash--;
            popups.removeIf(p -> frame - p.born() > 14);
            repaint();
        }).start();
    }

    // ---------------------------------------------------------------- game logic

    private Room current() {
        return inside.isEmpty() ? (Room) game.getRoot().getChildren().get(floor) : inside.peek();
    }

    // where the i-th child of a room is drawn
    private static Point slot(int i) {
        return new Point(1 + (i % 5) * 2, 1 + (i / 5) * 2);
    }

    private static boolean isWall(int x, int y) {
        return x == 0 || y == 0 || x == COLS - 1 || y == ROWS - 1;
    }

    private void step(int dx, int dy) {
        if (ending != null) return;
        if (dx != 0) facingLeft = dx < 0;
        Point next = new Point(pos.x + dx, pos.y + dy);

        List<DungeonElement> items = current().getChildren();
        for (int i = 0; i < items.size(); i++) {
            if (slot(i).equals(next)) {
                bump(items.get(i), items, next);
                return;
            }
        }
        if (next.equals(EXIT)) {
            if (inside.isEmpty()) message = "The stairs are sealed. Pick a door first.";
            else leave();
            return;
        }
        if (!isWall(next.x, next.y)) pos = next;
    }

    private void bump(DungeonElement target, List<DungeonElement> items, Point at) {
        if (target instanceof Room) {
            inside.push((Room) target);
            pos = new Point(START);
            message = "You enter " + current().getName() + ".";
            return;
        }
        int hpBefore = player.getHealth();
        double goldBefore = player.getMoney();
        game.interact(target, items);
        message = game.takeLog().replace(System.lineSeparator(), "  ");

        int gold = (int) (player.getMoney() - goldBefore);
        int lost = hpBefore - player.getHealth();
        if (gold > 0) popups.add(new Popup("+" + gold + "g", GOLD, at.x * TILE + 10, at.y * TILE, frame));
        if (lost > 0) {
            popups.add(new Popup("-" + lost, new Color(255, 70, 60), pos.x * TILE + 18, pos.y * TILE, frame));
            hurtFlash = 3;
        }
        if (!player.isAlive()) ending = "YOU DIED ON FLOOR " + (floor + 1);
    }

    private void leave() {
        inside.pop();
        pos = new Point(START);
        if (!inside.isEmpty()) {
            message = "Back in " + current().getName() + ".";
            return;
        }
        if (floor == GameManager.FLOORS - 1) { // stay on the last floor so current() remains valid
            ending = "YOU ESCAPED!";
            return;
        }
        floor++;
        player.heal(GameManager.REST_HEAL);
        popups.add(new Popup("+" + GameManager.REST_HEAL + " HP", new Color(110, 230, 110),
                pos.x * TILE + 4, pos.y * TILE, frame));
        message = "You rest on the stairs (+" + GameManager.REST_HEAL + " HP). Pick a door.";
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        List<DungeonElement> items = current().getChildren();

        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++)
                if (isWall(x, y)) wall(g, x, y); else floorTile(g, x, y);
        stairs(g, inside.isEmpty());
        for (int t : TORCHES) torch(g, t);

        String nearby = "";
        for (int i = 0; i < items.size(); i++) {
            Point p = slot(i);
            element(g, items.get(i), p);
            if (Math.abs(p.x - pos.x) + Math.abs(p.y - pos.y) == 1) nearby = items.get(i).describe();
        }

        int bob = (frame / 3) % 2;
        shadow(g, pos.x * TILE, pos.y * TILE);
        Sprites.draw(g, Sprites.HERO, Map.of(
                'S', new Color(190, 200, 215), 'F', new Color(240, 190, 150),
                'B', new Color(60, 110, 210), 'b', new Color(35, 65, 140),
                'Y', GOLD, 'n', new Color(100, 60, 30)),
                pos.x * TILE, pos.y * TILE - bob, 4, facingLeft);

        g.setFont(PIXEL);
        for (Popup p : popups) text(g, p.text(), p.x(), p.y() - (frame - p.born()) * 3, p.color());

        lighting(g);
        if (hurtFlash > 0) {
            g.setColor(new Color(200, 0, 0, 30 * hurtFlash));
            g.fillRect(0, 0, W, H);
        }

        g.setFont(PIXEL);
        String title = inside.isEmpty() ? "FLOOR " + (floor + 1) + " - CHOOSE A DOOR"
                : current().getName().toUpperCase();
        text(g, title, (W - g.getFontMetrics().stringWidth(title)) / 2, 38, GOLD);

        hud(g, nearby);
        if (ending != null) ending(g);
    }

    private static int noise(int x, int y, int seed) {
        int h = x * 73856093 ^ y * 19349663 ^ seed * 83492791;
        return (h ^ (h >>> 13)) & 15;
    }

    private void floorTile(Graphics2D g, int x, int y) {
        int px = x * TILE, py = y * TILE, v = noise(x, y, floor) - 8;
        g.setColor(new Color(62 + v, 56 + v, 66 + v));
        g.fillRect(px, py, TILE, TILE);
        // four stone slabs per tile
        for (int i = 0; i < 4; i++) {
            int sx = px + (i % 2) * 32, sy = py + (i / 2) * 32, w = noise(x * 2 + i, y, 7) / 4;
            g.setColor(new Color(74 + v + w * 3, 67 + v + w * 3, 78 + v + w * 3));
            g.fillRect(sx + 2, sy + 2, 28, 28);
            g.setColor(new Color(90 + v, 83 + v, 95 + v));
            g.fillRect(sx + 2, sy + 2, 28, 2);
        }
        if (noise(x, y, floor + 3) == 0) { // moss
            g.setColor(new Color(60, 100, 50));
            g.fillRect(px + 30, py + 28, 6, 4);
            g.fillRect(px + 33, py + 32, 4, 4);
        }
    }

    private static void wall(Graphics2D g, int x, int y) {
        int px = x * TILE, py = y * TILE;
        g.setColor(new Color(40, 34, 46));
        g.fillRect(px, py, TILE, TILE);
        for (int row = 0; row < 4; row++) {
            int offset = (row % 2) * 16;
            for (int b = -1; b < 2; b++) {
                int bx = px + offset + b * 32, v = noise(x * 3 + b, y * 4 + row, 1) * 2;
                g.setColor(new Color(78 + v, 66 + v, 82 + v));
                g.fillRect(Math.max(bx + 2, px), py + row * 16 + 2, Math.min(bx + 30, px + TILE) - Math.max(bx + 2, px), 12);
            }
        }
        if (y == 0) {
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRect(px, py + TILE - 8, TILE, 8);
        }
    }

    private void stairs(Graphics2D g, boolean sealed) {
        int px = EXIT.x * TILE, py = EXIT.y * TILE;
        for (int i = 0; i < 5; i++) {
            int c = 70 - i * 13;
            g.setColor(new Color(c + 10, c, c + 15));
            g.fillRect(px + 4 + i * 2, py + 4 + i * 11, TILE - 8 - i * 4, 11);
        }
        if (sealed) {
            g.setColor(new Color(120, 120, 130));
            g.setStroke(new BasicStroke(4));
            for (int i = 0; i < 4; i++) g.drawLine(px + 12 + i * 13, py + 4, px + 12 + i * 13, py + TILE - 4);
            g.setStroke(new BasicStroke(1));
        }
    }

    private void torch(Graphics2D g, int x) {
        int cx = x * TILE + TILE / 2, cy = TILE / 2;
        float r = 90 + flicker.nextInt(12);
        g.setPaint(new RadialGradientPaint(cx, cy, r, new float[]{0f, 1f},
                new Color[]{new Color(255, 150, 50, 70), new Color(255, 150, 50, 0)}));
        g.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        g.setColor(new Color(90, 55, 30));
        g.fillRect(cx - 3, cy, 6, 18);
        int f = flicker.nextInt(4);
        g.setColor(new Color(255, 120, 30));
        g.fillOval(cx - 7, cy - 16 - f, 14, 18 + f);
        g.setColor(new Color(255, 230, 120));
        g.fillOval(cx - 4, cy - 8 - f / 2, 8, 10);
    }

    private static void shadow(Graphics2D g, int x, int y) {
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x + 12, y + 52, 40, 10);
    }

    private void element(Graphics2D g, DungeonElement e, Point p) {
        int x = p.x * TILE, y = p.y * TILE;
        String d = e.describe();

        if (e instanceof Room room) {
            boolean vault = room.getName().equals("Vault");
            Sprites.draw(g, Sprites.DOOR, vault
                    ? Map.of('N', new Color(110, 115, 128), 'n', new Color(65, 68, 78), 'Y', GOLD)
                    : Map.of('N', new Color(135, 85, 45), 'n', new Color(85, 52, 28), 'Y', GOLD),
                    x, y, 4, false);
            g.setFont(SMALL);
            String name = room.getName();
            text(g, name, x + (TILE - g.getFontMetrics().stringWidth(name)) / 2, y + TILE + 14, Color.WHITE);
            return;
        }

        shadow(g, x, y);
        if (e.countMonsters() > 0) {
            boolean awake = e.threat() > 0;
            boolean boss = d.startsWith("BOSS");
            Color[] c = monsterColors(d);
            if (d.contains("Elite")) {
                g.setColor(new Color(255, 210, 60, 70 + (frame % 6) * 15));
                g.fillOval(x + 2, y + 2, TILE - 4, TILE - 4);
            }
            int bob = awake ? (frame / 2 + p.x) % 2 * 2 : 0;
            Sprites.draw(g, Sprites.MONSTER, Map.of(
                    'M', c[0], 'm', c[1], 'W', Color.WHITE,
                    'E', awake ? (boss ? GOLD : new Color(255, 50, 40)) : c[1],
                    'H', boss ? new Color(235, 225, 200) : new Color(0, 0, 0, 0)),
                    x, y - bob, 4, false);
            g.setFont(SMALL);
            if (!awake) text(g, (frame / 5) % 2 == 0 ? "z" : "Z", x + 50, y + 8 - (frame / 5) % 2 * 4, new Color(170, 200, 255));
        } else if (e.getTreasureValue() > 0) {
            Color[] c = d.startsWith("Cursed") ? new Color[]{new Color(110, 50, 140), new Color(70, 30, 90), new Color(170, 230, 80)}
                    : d.startsWith("Enchanted") ? new Color[]{new Color(40, 115, 160), new Color(25, 70, 105), new Color(230, 240, 255)}
                    : new Color[]{new Color(150, 95, 45), new Color(100, 60, 30), GOLD};
            if (!d.startsWith("Cursed") && frame % 20 < 2) { // sparkle
                g.setColor(Color.WHITE);
                g.fillRect(x + 44, y + 18, 4, 4);
            }
            Sprites.draw(g, Sprites.CHEST, Map.of('C', c[0], 'c', c[1], 'Y', c[2]), x, y, 4, false);
            if (d.contains("sealed")) {
                g.setColor(new Color(150, 150, 160));
                g.setStroke(new BasicStroke(3));
                g.drawLine(x + 6, y + 24, x + 58, y + 56);
                g.drawLine(x + 58, y + 24, x + 6, y + 56);
                g.setStroke(new BasicStroke(1));
            }
            g.setFont(SMALL);
            String value = (int) e.getTreasureValue() + "g";
            text(g, value, x + (TILE - g.getFontMetrics().stringWidth(value)) / 2, y + TILE + 14, GOLD);
        } else if (d.contains("defeated")) {
            Sprites.draw(g, Sprites.SKULL, Map.of('W', new Color(220, 215, 195)), x, y, 4, false);
        } else {
            Sprites.draw(g, Sprites.CHEST_OPEN, Map.of('C', new Color(95, 70, 50), 'c', new Color(65, 48, 35),
                    'Y', new Color(120, 110, 90)), x, y, 4, false);
        }
    }

    private static Color[] monsterColors(String d) {
        if (d.startsWith("BOSS")) return new Color[]{new Color(190, 40, 50), new Color(115, 20, 30)};
        if (d.contains("Skeleton")) return new Color[]{new Color(225, 220, 200), new Color(150, 145, 130)};
        if (d.contains("Orc")) return new Color[]{new Color(70, 125, 60), new Color(40, 80, 35)};
        if (d.contains("Troll")) return new Color[]{new Color(110, 135, 155), new Color(70, 85, 105)};
        if (d.contains("Wraith")) return new Color[]{new Color(165, 145, 210), new Color(100, 85, 145)};
        if (d.contains("Ogre")) return new Color[]{new Color(200, 160, 110), new Color(140, 105, 70)};
        return new Color[]{new Color(100, 180, 70), new Color(55, 115, 40)}; // goblin
    }

    // darkness everywhere except around the hero
    private void lighting(Graphics2D g) {
        float cx = pos.x * TILE + TILE / 2f, cy = pos.y * TILE + TILE / 2f;
        g.setPaint(new RadialGradientPaint(cx, cy, 420, new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 70), new Color(0, 0, 0, 190)}));
        g.fillRect(0, 0, W, H);
    }

    private void hud(Graphics2D g, String nearby) {
        g.setColor(new Color(22, 17, 26));
        g.fillRect(0, H, W, HUD);
        g.setColor(new Color(130, 95, 45));
        g.fillRect(0, H, W, 3);

        // health bar
        int hp = player.getHealth();
        g.setColor(new Color(60, 15, 20));
        g.fillRect(12, H + 12, 200, 20);
        g.setColor(hp > 30 ? new Color(200, 45, 50) : new Color(255, 80, 40));
        g.fillRect(12, H + 12, hp * 2, 20);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(12, H + 12, hp * 2, 6);
        g.setColor(Sprites.OUTLINE);
        g.drawRect(12, H + 12, 200, 20);
        g.setFont(PIXEL);
        text(g, "HP " + hp + "/100", 20, H + 27, Color.WHITE);

        // gold
        g.setColor(new Color(200, 140, 30));
        g.fillOval(230, H + 12, 20, 20);
        g.setColor(GOLD);
        g.fillOval(232, H + 13, 16, 16);
        text(g, String.valueOf((int) player.getMoney()), 258, H + 27, GOLD);

        if (player.isCursed()) text(g, "CURSED x0.5", 340, H + 27, new Color(190, 120, 255));
        text(g, "Esc: quit", W - 100, H + 27, new Color(120, 110, 130));

        g.setFont(SMALL);
        text(g, message, 12, H + 54, new Color(255, 225, 150));
        if (!nearby.isEmpty()) text(g, "> " + nearby, 12, H + 76, new Color(170, 170, 190));
    }

    private void ending(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, W, H);
        g.setFont(BIG);
        Color color = player.isAlive() ? GOLD : new Color(220, 50, 50);
        text(g, ending, (W - g.getFontMetrics().stringWidth(ending)) / 2, H / 2 - 20, color);
        g.setFont(PIXEL);
        String score = "Gold collected: " + (int) player.getMoney() + "      Esc to quit";
        text(g, score, (W - g.getFontMetrics().stringWidth(score)) / 2, H / 2 + 24, Color.WHITE);
    }

    private static void text(Graphics2D g, String s, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 2, y + 2);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dungeon");
            Game2D panel = new Game2D();
            frame.add(panel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
