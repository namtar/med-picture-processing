package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.ImageHelper;
import de.htw.berlin.student.gespic.utils.Tuple;

import java.awt.Polygon;
import java.util.Vector;

/**
 * Class that does the contour tracing on a gray scale image.
 *
 * @author by Matthias Drummer on 14.12.2014
 * @author Simon Gyimah
 */
public class ContourTracer {

    private Vector<Polygon> polyvec;

    private byte[] bluePixels;
    private byte[] objectPixels;

    private int width;
    private int height;

    private ViewDirection actualViewDirection;

    /**
     * Constructor.
     *
     * @param bluePixels   a byte array that contains the images blue channel pixels
     * @param objectPixels a byte array used to determine the objects
     * @param width        the width of the images
     * @param height       the height of the images
     */
    public ContourTracer(byte[] bluePixels, byte[] objectPixels, int width, int height) {

        if (bluePixels.length != objectPixels.length) {
            throw new IllegalArgumentException("Both image sizes must match.");
        }

        this.bluePixels = bluePixels;
        this.objectPixels = objectPixels;
        this.width = width;
        this.height = height;

        this.polyvec = new Vector<Polygon>();
    }

    public byte[] startTracing() {

        // iterate over objectPixels and start tracing on blue pixels for every found object pixel.

        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelOfInterest = ImageHelper.getEndianPixelForCoordinate(objectPixels, x, y, width);
                if (pixelOfInterest == Constants.FOREGROUND_VAL) {
                    boolean isBorder = false;
                    int borderX = x;
                    int borderY = y;
                    while (!isBorder) {

                        if ((borderX - 1) < 0 || ImageHelper.getEndianPixelForCoordinate(bluePixels, borderX - 1, borderY, width) == Constants.BACKGROUND_VAL) {
                            isBorder = true;
                        } else {
                            borderX = borderX - 1;
                        }
                    }

//                    if(isBorder) {
//                        System.out.println("y = " + borderY + ", x= " + borderX + " contains: " + checkcontains(borderX, borderY));
//                    }
                    if (!checkcontains(borderX, borderY)) {
                        traceContour(borderX, borderY);
//                        System.out.println("Co: x " + borderX + ", y " + borderY);
//                        count++;
                    }
//                    traceContour(borderX, borderY);
                }
            }
        }
//        for (int i = 0; i < polyvec.size(); i++) {
//            Polygon polygon = polyvec.elementAt(i);
//            System.out.println(polygon.npoints + ", Co: " + polygon.getBounds());
//        }

        return bluePixels;
    }

    private void traceContour(int x, int y) {

//        int rightCount = 0; // if rightCount + 1 == 4
        actualViewDirection = ViewDirection.EAST;
        Polygon currentPolygon = new Polygon();
        Tuple<Integer, Integer> actualPixelPosition = new Tuple<Integer, Integer>(Integer.valueOf(x), Integer.valueOf(y));
        do {
            int pixel = ImageHelper.doEndian(bluePixels[ImageHelper.transformCoordinate(actualPixelPosition.getX(), actualPixelPosition.getY(), width)]);
            if (pixel == Constants.BACKGROUND_VAL) {
                Turn toTurn = Turn.RIGHT;
//                if (rightCount + 1 == 4) {
//                    toTurn = Turn.LEFT;
//                }
                bluePixels[ImageHelper.transformCoordinate(actualPixelPosition.getX(), actualPixelPosition.getY(), width)] = Constants.BACKGROUND_VAL.byteValue();
                actualPixelPosition = coordinateDirectionTranscoder(toTurn, actualPixelPosition.getX(), actualPixelPosition.getY());
//                actualViewDirection = calculateViewDirectionByTurn(actualViewDirection, toTurn);
//                rightCount++;
            } else {
//                rightCount = 0;
                currentPolygon.addPoint(actualPixelPosition.getX(), actualPixelPosition.getY());
                bluePixels[ImageHelper.transformCoordinate(actualPixelPosition.getX(), actualPixelPosition.getY(), width)] = Constants.FOREGROUND_VAL.byteValue();
                actualPixelPosition = coordinateDirectionTranscoder(Turn.LEFT, actualPixelPosition.getX(), actualPixelPosition.getY());
//                actualViewDirection = calculateViewDirectionByTurn(actualViewDirection, Turn.LEFT);
            }

        } while (actualPixelPosition.getX().intValue() != x || actualPixelPosition.getY().intValue() != y);

        if(currentPolygon.npoints == 1914) {
            System.out.println(actualPixelPosition.toString());
        }
        polyvec.add(currentPolygon);
    }

    private ViewDirection calculateViewDirectionByTurn(Turn turn) {
        ViewDirection newViewDirection = null;
        if (turn == Turn.LEFT) {
            newViewDirection = ViewDirection.valueOf(actualViewDirection.getLeft().toUpperCase());
        } else {
            newViewDirection = ViewDirection.valueOf(actualViewDirection.getRight().toUpperCase());
        }
        return newViewDirection;
    }

    private Tuple<Integer, Integer> coordinateDirectionTranscoder(Turn turn, int actualX, int actualY) {

        Integer newX = null;
        Integer newY = null;

        Turn performedTurn = null;

        switch (actualViewDirection) {
            case NORTH:
                newY = actualY;
                if (turn == Turn.LEFT) {
                    newX = actualX - 1;
                    performedTurn = turn;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newX = actualX;
                        newY = actualY - 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newY = actualY;
                            newX = actualX + 1;
                            performedTurn = Turn.RIGHT;
                        }
                    }

                } else {
                    newX = actualX + 1;
                    performedTurn = Turn.RIGHT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newX = actualX;
                        newY = actualY - 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newY = actualY;
                            newX = actualX - 1;
                            performedTurn = Turn.LEFT;
                        }
                    }
                }

                break;
            case EAST:
                newX = actualX;
                if (turn == Turn.LEFT) {
                    newY = actualY - 1;
                    performedTurn = Turn.LEFT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newY = actualY;
                        newX = actualX + 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newX = actualX;
                            newY = newY + 1;
                            performedTurn = Turn.RIGHT;
                        }
                    }
                } else {
                    newY = actualY + 1;
                    performedTurn = Turn.RIGHT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newY = actualY;
                        newX = actualX + 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newX = actualX;
                            newY = actualY - 1;
                            performedTurn = Turn.LEFT;
                        }
                    }
                }
                break;
            case SOUTH:
                newY = actualY;
                if (turn == Turn.LEFT) {
                    newX = actualX + 1;
                    performedTurn = Turn.LEFT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newX = actualX;
                        newY = actualY + 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newY = actualY;
                            newX = actualX - 1;
                            performedTurn = Turn.RIGHT;
                        }
                    }
                } else {
                    newX = actualX - 1;
                    performedTurn = Turn.RIGHT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newX = actualX;
                        newY = actualY + 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newY = actualY;
                            newX = actualX + 1;
                            performedTurn = Turn.LEFT;
                        }
                    }
                }
                break;
            case WEST:
                newX = actualX;
                if (turn == Turn.LEFT) {
                    newY = actualY + 1;
                    performedTurn = Turn.LEFT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newY = actualY;
                        newX = actualX - 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newX = actualX;
                            newY = actualY - 1;
                            performedTurn = Turn.RIGHT;
                        }
                    }
                } else {
                    newY = actualY - 1;
                    performedTurn = Turn.RIGHT;
                    if (ImageHelper.checkBorder(newX, newY, width, height)) {
                        newY = actualY;
                        newX = actualX - 1;
                        performedTurn = Turn.NO;
                        if (ImageHelper.checkBorder(newX, newY, width, height)) {
                            newX = actualX;
                            newY = actualY + 1;
                            performedTurn = Turn.LEFT;
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Given Value not supported: " + this);
        }

        if (performedTurn != Turn.NO) {
            actualViewDirection = calculateViewDirectionByTurn(performedTurn);
        }

        return new Tuple<Integer, Integer>(newX, newY);
    }

    /**
     * Checks, if a point is inside one of the Objects.
     *
     * @param x The x image coordinate of the point to be examined.
     * @param y the y image coordinate of the point to be examined.
     * @return inside True, if the point lies inside an object, false otherwise.
     */
    public boolean checkcontains(int x, int y) //checks all Polygons
    {
        for (int i = 0; i < this.polyvec.size(); i++) {
            Polygon poly = (Polygon) this.polyvec.elementAt(i);
            if (poly.contains(x, y)) {
                return true;
            }

            for (int j = 0; j < poly.npoints; j++) {
                if (x == poly.xpoints[j] && y == poly.ypoints[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private enum Turn {
        LEFT,
        RIGHT,
        NO;
    }

    private enum ViewDirection {
        NORTH("west", "east"),
        EAST("north", "south"),
        SOUTH("east", "west"),
        WEST("south", "north");

        private String left;
        private String right;

        private ViewDirection(String left, String right) {
            this.left = left;
            this.right = right;
        }

        public String getLeft() {
            return left;
        }

        public String getRight() {
            return right;
        }
    }
}
