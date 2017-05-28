package com.uberv.joystick;

public class Position {

    private int horizontal;
    private int vertical;

    public Position() {
    }

    public Position(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    @Override
    public String toString() {
        return "Position{" +
                "horizontal=" + horizontal +
                ", vertical=" + vertical +
                '}';
    }
}
