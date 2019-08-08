package com.coolioasjulio.uniquecolors;

import java.util.Objects;

public class Coord {
    private int row, col;

    public Coord(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coord coord = (Coord) o;
        return row == coord.row &&
                col == coord.col;
    }

    public int hashCode() {
        return Objects.hash(row, col);
    }
}