import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;

// 16x16 pixel-art sprites; each letter is looked up in a palette, '.' is transparent
public final class Sprites {
    public static final Color OUTLINE = new Color(20, 16, 24);

    public static final String[] HERO = {
            ".....KKKKKK.....",
            "....KSSSSSSK....",
            "....KSSSSSSK....",
            "....KKKKKKKK....",
            "....KFKFFKFK....",
            "....KFFFFFFK..K.",
            ".....KFFFFK..KSK",
            "...KKBBBBBBKKKSK",
            "..KBBBBYYBBBBKSK",
            "..KBKBBYYBBKBKSK",
            "..KFKBBBBBBKFKK.",
            "....KbbbbbbK....",
            "....KbbKKbbK....",
            "....KbbKKbbK....",
            "....KnnKKnnK....",
            "....KKK..KKK....",
    };

    // H = horns, only drawn for bosses
    public static final String[] MONSTER = {
            "..H..........H..",
            "..HH........HH..",
            "...HKKKKKKKKH...",
            "..KMMMMMMMMMMK..",
            ".KMMMMMMMMMMMMK.",
            "KmMMEEMMMMEEMMmK",
            "KmMMEKMMMMEKMMmK",
            ".KMMMMMMMMMMMMK.",
            ".KMMMWKWWKWMMMK.",
            "..KMMKKKKKKMMK..",
            "...KMMMMMMMMK...",
            "..KMKMMMMMMKMK..",
            ".KMK.KmmmmK.KMK.",
            ".KK..KmKKmK..KK.",
            ".....KmK.KmK....",
            "....KKK...KKK...",
    };

    public static final String[] SKULL = {
            "................",
            "................",
            "................",
            "................",
            ".....KKKKKK.....",
            "....KWWWWWWK....",
            "...KWWWWWWWWK...",
            "...KWKKWWKKWK...",
            "...KWKKWWKKWK...",
            "...KWWWKKWWWK...",
            "....KWWWWWWK....",
            ".....KWKKWK.....",
            "..KK..KKKK..KK..",
            "..KWKK....KKWK..",
            "...KWWWWWWWWK...",
            "..KWKK....KKWK..",
    };

    public static final String[] CHEST = {
            "................",
            "................",
            "................",
            "................",
            "................",
            "..KKKKKKKKKKKK..",
            ".KCCCCCCCCCCCCK.",
            ".KCYCCCCCCCCYCK.",
            ".KcYccccccccYcK.",
            ".KKKKKKYYKKKKKK.",
            ".KCYCCKYYKCCYCK.",
            ".KCYCCCYYCCCYCK.",
            ".KCYCCCCCCCCYCK.",
            ".KcYccccccccYcK.",
            ".KKKKKKKKKKKKKK.",
            "................",
    };

    public static final String[] CHEST_OPEN = {
            "................",
            "................",
            "................",
            "..KKKKKKKKKKKK..",
            ".KCCCCCCCCCCCCK.",
            ".KcYccccccccYcK.",
            ".KKKKKKKKKKKKKK.",
            ".KKKKKKKKKKKKKK.",
            ".KCYCCCCCCCCYCK.",
            ".KCYCCCCCCCCYCK.",
            ".KcYccccccccYcK.",
            ".KKKKKKKKKKKKKK.",
            "................",
            "................",
            "................",
            "................",
    };

    public static final String[] DOOR = {
            "....KKKKKKKK....",
            "...KnNNNNNNnK...",
            "..KnNNnNNnNNnK..",
            "..KNNNnNNnNNNK..",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNYNK.",
            ".KNNNNnNNnNNYNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KNNNNnNNnNNNNK.",
            ".KKKKKKKKKKKKKK.",
    };

    private Sprites() { }

    public static void draw(Graphics g, String[] art, Map<Character, Color> palette,
                            int x, int y, int scale, boolean flip) {
        for (int row = 0; row < art.length; row++) {
            for (int col = 0; col < art[row].length(); col++) {
                char c = art[row].charAt(col);
                Color color = c == 'K' ? OUTLINE : palette.get(c);
                if (color == null) continue;
                int px = flip ? art[row].length() - 1 - col : col;
                g.setColor(color);
                g.fillRect(x + px * scale, y + row * scale, scale, scale);
            }
        }
    }
}
