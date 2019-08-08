package com.coolioasjulio.uniquecolors;

public class Neighbor {
    private int row, col;
    private VarColor seed;

    public Neighbor(int row, int col, VarColor seed) {
        this.row = row;
        this.col = col;
        this.seed = seed;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public VarColor getSeed() {
        return seed;
    }
}
