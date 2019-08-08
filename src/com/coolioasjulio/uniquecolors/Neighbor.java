package com.coolioasjulio.uniquecolors;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbor neighbor = (Neighbor) o;
        return row == neighbor.row &&
                col == neighbor.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
