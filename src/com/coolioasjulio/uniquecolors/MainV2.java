package com.coolioasjulio.uniquecolors;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.stream.DoubleStream;

public class MainV2 {

    private static final boolean AVG_COST = false;

    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            System.out.println("How many bits of color?");
            int bits = in.nextInt();
            MainV2 pane = new MainV2(1 << (bits / 3));
            long start = System.currentTimeMillis();
            pane.generate();
            long dt = System.currentTimeMillis() - start;
            System.out.println("\nElapsed time: " + dt / 1000.0);
            pane.trySaveAs(String.format("output_v2-%d.png", bits));
        }
    }

    private int width, height;
    private Color[][] colorPane;
    private List<Color> allColors;
    private ListIterator<Color> colorIter;
    private List<Coord> edges;

    public MainV2(int numColors) {
        width = height = (int) Math.ceil(Math.pow(numColors, 1.5));

        colorPane = new Color[height][width];
        edges = new LinkedList<>();
        allColors = new ArrayList<>((int) Math.pow(numColors, 3));

        System.out.print("Creating random colors...");
        float inc = 1f / numColors;
        for (int r = 0; r < numColors; r++) {
            for (int g = 0; g < numColors; g++) {
                for (int b = 0; b < numColors; b++) {
                    allColors.add(new Color(r * inc + inc / 2, g * inc + inc / 2, b * inc + inc / 2));
                }
            }
        }
        Collections.shuffle(allColors);
        colorIter = allColors.listIterator();
        System.out.println("Done!");

        int row = height / 2;
        int col = width / 2;
        Color color = colorIter.next();
        colorPane[row][col] = color;
        edges.addAll(getNeighbors(row, col));
    }

    public void generate() {
        long i = 1;
        double numColors = allColors.size();
        while (colorIter.hasNext()) {
            System.out.printf("\r%.2f%%", 100.0 * (i++) / numColors);
            Color color = colorIter.next();

            Coord coord = edges.parallelStream().min(Comparator.comparing(e -> findDist(color, e))).orElseThrow(IllegalStateException::new);
            colorPane[coord.getRow()][coord.getCol()] = color;
            edges.remove(coord);
            List<Coord> neighbors = getNeighbors(coord.getRow(), coord.getCol());
            neighbors.removeIf(e -> visitedTile(e.getRow(), e.getCol()));
            neighbors.removeAll(edges);
            edges.addAll(neighbors);
        }
    }

    private boolean visitedTile(int row, int col) {
        return colorPane[row][col] != null;
    }

    private double findDist(Color color, Coord coord) {
        List<Coord> coords = getNeighbors(coord.getRow(), coord.getCol());
        coords.removeIf(n -> !visitedTile(n.getRow(), n.getCol()));
        if (coords.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        DoubleStream ds = coords.stream()
                .map(c -> colorPane[c.getRow()][c.getCol()])
                .mapToDouble(e -> Math.pow(e.getRed() - color.getRed(), 2) + Math.pow(e.getGreen() - color.getGreen(), 2) + Math.pow(e.getBlue() - color.getBlue(), 2));
        return AVG_COST ? ds.average().orElseThrow(IllegalStateException::new) : ds.min().orElseThrow(IllegalStateException::new);
    }

    private List<Coord> getNeighbors(int row, int col) {
        List<Coord> neighbors = new ArrayList<>(8);
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < height && c >= 0 && c < width) {
                    neighbors.add(new Coord(r, c));
                }
            }
        }
        return neighbors;
    }

    public void trySaveAs(String path) {
        try {
            saveAs(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAs(String path) throws IOException {
        ImageIO.write(createImage(), "png", new File(path));
    }

    public BufferedImage createImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color c = colorPane[row][col];
                c = c == null ? Color.BLACK : c;
                img.setRGB(col, row, c.getRGB()); // this is (x,y), not (row,col)
            }
        }
        return img;
    }
}
