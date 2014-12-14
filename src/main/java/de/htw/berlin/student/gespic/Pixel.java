package de.htw.berlin.student.gespic;

/**
 * Represents a pixels with its coordinates and value.
 *
 * @author by Matthias Drummer on 14.12.2014#
 * @author Simon Gyimah
 */
public class Pixel {

    private int x;
    private int y;
    private Integer value;

    /**
     * Constructor.
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param value the value of the pixel
     */
    public Pixel(int x, int y, Integer value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Pixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pixel pixel = (Pixel) o;

        if (x != pixel.x) return false;
        if (y != pixel.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "x=" + x +
                ", y=" + y +
                ", value=" + value +
                '}';
    }
}
