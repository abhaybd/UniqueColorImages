package com.coolioasjulio.uniquecolors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VarColor {

    public static VarColor randomColor() {
        Random rand = new Random();
        int r = rand.nextInt(getMask() + 1) & 0xFF;
        int g = rand.nextInt(getMask() + 1) & 0xFF;
        int b = rand.nextInt(getMask() + 1) & 0xFF;
        return new VarColor(r, g, b);
    }

    private static int numBits = 15;

    public static void setNumBits(int numBits) {
        if (numBits % 3 != 0 || numBits >= 32) {
            throw new IllegalArgumentException("No.");
        }
        VarColor.numBits = numBits;
    }

    private static int getMask() {
        return (1 << numBits / 3) - 1;
    }

    private int r, g, b;

    public VarColor(short rgb) {
        this((rgb >> numBits / 3 * 2) & getMask(), (rgb >> numBits / 3) & getMask(), rgb & getMask());
    }

    public VarColor(int r, int g, int b) {
        if (!validValue(r) || !validValue(g) || !validValue(b)) {
            throw new IllegalArgumentException("Invalid color value!");
        }
        this.r = r;
        this.g = g;
        this.b = b;
    }

    private boolean validValue(int b) {
        return b >= 0 && b <= getMask();
    }

    public int getR() {
        return g;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public Color toAwt() {
        float max = getMask();
        return new Color(r / max, g / max, b / max);
    }

    public VarColor[] getNeighbors() {
        List<VarColor> neighbors = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dg = -1; dg <= 1; dg++) {
                for (int db = -1; db <= 1; db++) {
                    if (dr == 0 && dg == 0 && db == 0) {
                        continue;
                    }
                    int red = r + dr;
                    int green = g + dg;
                    int blue = b + db;
                    if (validValue(red) && validValue(green) && validValue(blue)) {
                        neighbors.add(new VarColor(r + dr, g + dg, b + db));
                    }
                }
            }
        }
        return neighbors.toArray(new VarColor[0]);
    }

    public VarColor randomNeighbor() {
        Random rand = new Random();
        int dr, dg, db;
        do {
            dr = rand.nextInt(3) - 1;
            dg = rand.nextInt(3) - 1;
            db = rand.nextInt(3) - 1;
        } while (dr == 0 && dg == 0 && db == 0);
        try {
            return new VarColor(r + dr, g + dg, b + db);
        } catch (IllegalArgumentException e) {
            return randomNeighbor(); // try again
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof VarColor)) {
            return false;
        }
        VarColor c = (VarColor) o;
        return c.r == r && c.g == g && c.b == b;
    }

    public int hashCode() {
        return (r << numBits / 3 * 2) | (g << numBits / 3) | b;
    }
}
