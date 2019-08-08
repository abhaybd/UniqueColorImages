package com.coolioasjulio.uniquecolors;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class MainV1 {

    private static final boolean FILL_GAPS = true;

    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            System.out.println("How many bits of color?");
            int bits = in.nextInt();
            VarColor.setNumBits(bits);
            int size = (int) Math.ceil(Math.pow(2, bits / 2.0));
            MainV1 pane = new MainV1(size, size);
            long start = System.currentTimeMillis();
            pane.generate();
            long dt = System.currentTimeMillis() - start;
            System.out.println("\nElapsed time: " + dt / 1000.0);
            pane.trySaveAs(String.format("output-%d.png", bits));
        }
    }

    private int width, height;
    private VarColor[][] colorPane;
    private Set<VarColor> usedColors;
    private List<Neighbor> neighbors = new ArrayList<>();
    private Random r;

    public MainV1(int width, int height) {
        this.width = width;
        this.height = height;

        colorPane = new VarColor[height][width];
        usedColors = new HashSet<>();
        r = new Random();

        int row = height / 2;
        int col = width / 2;
        System.out.printf("Start: row=%d,col=%d\n", row, col);
        VarColor color = VarColor.randomColor();
        colorPane[row][col] = color;
        usedColors.add(color);
        neighbors.addAll(getNeighbors(row, col, color));
    }

    private boolean visitedTile(int row, int col) {
        return colorPane[row][col] != null;
    }

    private List<Neighbor> getNeighbors(int row, int col, VarColor color) {
        List<Neighbor> neighbors = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < height && c >= 0 && c < width) {
                    neighbors.add(new Neighbor(r, c, color));
                }
            }
        }
        return neighbors;
    }

    public void generate() {
        long numIters = 0;
        long placed = 0;
        while (!neighbors.isEmpty()) {
            numIters++;
            if (numIters % 1000 == 0) {
                System.out.printf("\rIter: % 7d, placed: % 6d", numIters, placed);
            }
            Neighbor n = neighbors.remove(neighbors.size() - 1);
            if (visitedTile(n.getRow(), n.getCol())) {
                System.err.println("Already visited neighbor! This should not happen.");
                continue;
            }
            VarColor c = getColor(n);
            if (c != null) {
                placed++;
                colorPane[n.getRow()][n.getCol()] = c;
                usedColors.add(c);
                List<Neighbor> localNeighbors = getNeighbors(n.getRow(), n.getCol(), c);
                localNeighbors.removeIf(e -> visitedTile(e.getRow(), e.getCol()));
                localNeighbors.removeAll(neighbors);

                if (!localNeighbors.isEmpty()) {
                    neighbors.addAll(localNeighbors);
                    Collections.shuffle(neighbors);
                }
            }
        }
        if (FILL_GAPS) {
            fillGaps();
        }
    }

    private void fillGaps() {
        trySaveAs("pre.png");
        HashSet<VarColor> allColorSet = new HashSet<>();
        for (int r = 0; r <= VarColor.getMask(); r++) {
            for (int g = 0; g <= VarColor.getMask(); g++) {
                for (int b = 0; b <= VarColor.getMask(); b++) {
                    allColorSet.add(new VarColor(r, g, b));
                }
            }
        }
        List<VarColor> remainingColors = allColorSet.parallelStream().filter(e -> !usedColors.contains(e)).collect(Collectors.toList());
        Collections.shuffle(remainingColors);
        List<Coord> coordinates = new ArrayList<>(width * height - usedColors.size());
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (colorPane[row][col] == null) {
                    coordinates.add(new Coord(row, col));
                }
            }
        }
        System.out.println("\nColors: " + remainingColors.size());
        System.out.println("Coords: " + coordinates.size());

        for (VarColor color : remainingColors) {
            Coord coord = coordinates.parallelStream().min(Comparator.comparing(e -> findDist(color, e))).orElseThrow(IllegalStateException::new);
            colorPane[coord.getRow()][coord.getCol()] = color;
            coordinates.remove(coord);
        }
        trySaveAs("post.png");
    }

    private double findDist(VarColor color, Coord coord) {
        List<Neighbor> neighbors = getNeighbors(coord.getRow(), coord.getCol(), null);
        neighbors.removeIf(n -> !visitedTile(n.getRow(), n.getCol()));
        double minDist = Double.POSITIVE_INFINITY;
        for (Neighbor n : neighbors) {
            VarColor nColor = colorPane[n.getRow()][n.getCol()];
            double dist = Math.pow(nColor.getR() - color.getR(), 2) + Math.pow(nColor.getG() - color.getG(), 2) + Math.pow(nColor.getB() - color.getB(), 2);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
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
                VarColor c = colorPane[row][col];
                Color color = c == null ? Color.BLACK : c.toAwt();
                img.setRGB(col, row, color.getRGB()); // this is (x,y), not (row,col)
            }
        }
        return img;
    }

    private VarColor getColor(Neighbor n) {
        List<VarColor> list = new ArrayList<>(Arrays.asList(n.getSeed().getNeighbors()));
        list.removeAll(usedColors);
        return list.isEmpty() ? null : list.get(r.nextInt(list.size()));
    }
}
