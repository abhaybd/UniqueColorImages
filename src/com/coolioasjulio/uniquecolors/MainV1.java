package com.coolioasjulio.uniquecolors;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainV1 {

    public static void main(String[] args) {
        int bits = 21;
        VarColor.setNumBits(bits);
        int size = (int) Math.ceil(Math.pow(2, bits / 2.0));
        MainV1 pane = new MainV1(size, size);
        long start = System.currentTimeMillis();
        pane.generate();
        long dt = System.currentTimeMillis() - start;
        System.out.println("\nElapsed time: " + dt / 1000.0);
        pane.trySaveAs(String.format("output-%d.png", bits));
    }

    private int width, height;
    private VarColor[][] colorPane;
    private Set<VarColor> usedColors;
    private List<Neighbor> neighbors = new LinkedList<>();
    private Set<Coord> visitedTiles;
    private Random r;

    public MainV1(int width, int height) {
        this.width = width;
        this.height = height;

        colorPane = new VarColor[height][width];
        visitedTiles = new HashSet<>();
        usedColors = new HashSet<>();
        r = new Random();

        int row = height / 2;
        int col = width / 2;
        System.out.printf("Start: row=%d,col=%d\n", row, col);
        visitedTiles.add(new Coord(row, col));
        VarColor color = VarColor.randomColor();
        colorPane[row][col] = color;
        usedColors.add(color);
        neighbors.addAll(getNeighbors(row, col, color));
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
            Neighbor n = neighbors.remove(0);
            if (visitedTiles.contains(new Coord(n.getRow(), n.getCol()))) {
                continue;
            }
            VarColor c = getColors(n);
            if (c != null) {
                placed++;
                colorPane[n.getRow()][n.getCol()] = c;
                usedColors.add(c);
                visitedTiles.add(new Coord(n.getRow(), n.getCol()));
                List<Neighbor> neighbors = getNeighbors(n.getRow(), n.getCol(), c);
                neighbors.removeIf(e -> visitedTiles.contains(new Coord(e.getRow(), e.getCol())));
                neighbors.removeAll(this.neighbors);
                if (!neighbors.isEmpty()) {
                    this.neighbors.addAll(neighbors);
                    Collections.shuffle(this.neighbors);
                }
            }
        }
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

    private VarColor getColors(Neighbor n) {
        List<VarColor> list = new ArrayList<>(Arrays.asList(n.getSeed().getNeighbors()));
        list.removeAll(usedColors);
        return list.isEmpty() ? null : list.get(r.nextInt(list.size()));
    }
}
