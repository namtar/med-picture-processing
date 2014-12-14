package de.htw.berlin.student.gespic.utils;

/**
 * A tuple to wrap to values.
 * <p/>
 * Created by matthias.drummer on 26.11.14.
 */
public class Tuple<X, Y> {

    private X x;
    private Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public void setX(X x) {
        this.x = x;
    }

    public Y getY() {
        return y;
    }

    public void setY(Y y) {
        this.y = y;
    }


    @Override
    public String toString() {
        return "Tuple{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
