package de.htw.berlin.student.gespic.utils;

/**
 * A wrapper class to access easily a one dimensional array.<br>
 * This is a better solution than to transform the one dimensional array to a two dimension and duplicating therefore the data in the ram,
 * but there is still the whole picture loaded to the ram instead of loading and processing chunks.
 * <p/>
 * Created by Matthias Drummer on 22.10.14.
 */
public class IntArrayTwoDimensionWrapper {

    private int dimensionX;
    private int dimensionY;

    private int[] originalArray;
    private int[] outputImageArray;

    /**
     * Constructor.
     *
     * @param dimensionX       the width of the image
     * @param dimensionY       the height of the image
     * @param outputImageArray an array that contains all bytes of the output image.
     */
    public IntArrayTwoDimensionWrapper(int dimensionX, int dimensionY, int[] outputImageArray, int[] originalArray) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.outputImageArray = outputImageArray;
        this.originalArray = originalArray;
    }

    public void setOutputImagePixel(int x, int y, int pixel) {
        int arrayLocation = (y * dimensionX) + x;
        outputImageArray[arrayLocation] = pixel;
    }

    public void setOutputImagePixel(int index, int pixel) {
        outputImageArray[index] = pixel;
    }

    public int getOutputImagePixel(int x, int y) {
        int arrayLocation = (y * dimensionX) + x;
        return outputImageArray[arrayLocation];
    }

//    public int getOutputImageEndianPixel(int x, int y) {
//        int arrayLocation = (y * dimensionX) + x;
//        int pixel = outputImageArray[arrayLocation] & 0xff;
//        return pixel;
//    }

    public int getOriginalImagePixel(int x, int y) {
        int arraLocation = (y * dimensionX) + x;
        return originalArray[arraLocation];
    }

//    public int getOriginalImageEndianPixel(int x, int y) {
//        int arrayLocation = (y * dimensionX) + x;
//        int pixel = originalArray[arrayLocation] & 0xff;
//        return pixel;
//    }

    public int[] getOutputImageArray() {
        return outputImageArray;
    }

    public int getImageWidth() {
        return dimensionX;
    }

    public int getImageHeight() {
        return dimensionY;
    }

    public int transformCoordinate(int x, int y) {
        return (y * dimensionX) + x;
    }
}
