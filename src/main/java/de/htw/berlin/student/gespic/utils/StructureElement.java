package de.htw.berlin.student.gespic.utils;

/**
 * A structure element for image operations. The structure element must be symetric and the values must be odd.
 *
 * @author by Matthias Drummer on 14.12.2014
 * @author Simon Gyimah
 */
public class StructureElement {

    private int size;

    /**
     * Constructor.
     *
     * @param size the size of the structure element
     */
    public StructureElement(int size) {

        if ((size % 2) == 0) {
            throw new IllegalArgumentException("The size of the structure element must be odd.");
        }

        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public int getCenterCoordinate() {
        return (size - 1) / 2;
    }


    @Override
    public String toString() {
        return "StructureElement{" +
                "size=" + size +
                ", centerCoordinate=" + getCenterCoordinate() +
                '}';
    }
}
