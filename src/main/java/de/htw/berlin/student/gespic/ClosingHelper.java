package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.ByteArrayTwoDimensionWrapper;
import de.htw.berlin.student.gespic.utils.ImageHelper;
import de.htw.berlin.student.gespic.utils.StructureElement;

/**
 * Helper that does a closing on the given image.
 *
 * @author by Matthias Drummer on 14.12.2014
 * @author Simon Gyimah
 */
public class ClosingHelper {

    private final static int BACKGROUND_VAL = 255;
    private final static int FOREGROUND_VAL = 0;

    private byte[] originalImagePixels;
    private int width;
    private int height;

    /**
     * Constructor.
     *
     * @param originalImage the original gray scale image to be processed
     */
    public ClosingHelper(byte[] originalImage, int width, int height) {
        this.originalImagePixels = originalImage;
        this.width = width;
        this.height = height;
    }

    public byte[] doClosing() {


        byte[] finalPixels = new byte[originalImagePixels.length];

        ByteArrayTwoDimensionWrapper wrapper = new ByteArrayTwoDimensionWrapper(width, height, finalPixels, originalImagePixels);
        StructureElement structureElement3x3 = new StructureElement(3); // we want a 3x3 matrix structure element
        StructureElement structureElement7x7 = new StructureElement(7);

        // close gaps
//        byte[] dilatatedPixels = doDilatation(originalImagePixels, width, height, structureElement3x3);
//        dilatatedPixels = doErosion(dilatatedPixels, width, height, structureElement3x3);

        // open to eliminate the basic noise
        byte[] dilatatedPixels = doErosion(originalImagePixels, width, height, structureElement3x3);
        dilatatedPixels = doDilatation(dilatatedPixels, width, height, structureElement3x3);

        // finally erode image to eleminate all remaining unnecessary pixels.
        // Note that the image only is used to detect object locations but is not used to trace the borders
        dilatatedPixels = doErosion(dilatatedPixels, width, height, structureElement7x7);


        for (int i = 0; i < dilatatedPixels.length; i++) {
            finalPixels[i] = dilatatedPixels[i];
        }

        return finalPixels;
    }

    private byte[] doDilatation(byte[] pixels, int width, int height, StructureElement structureElement) {

        byte[] dilatatedPixels = new byte[originalImagePixels.length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelOfInterest = ImageHelper.doEndian(pixels[ImageHelper.transformCoordinate(x, y, width)]);
                dilatatedPixels[ImageHelper.transformCoordinate(x, y, width)] = (byte) pixelOfInterest;

                if (pixelOfInterest != BACKGROUND_VAL) {

                    for (int row = 0; row < structureElement.getSize(); row++) {
                        for (int col = 0; col < structureElement.getSize(); col++) {
                            if (row == structureElement.getCenterCoordinate() && col == structureElement.getCenterCoordinate()) {
                                continue;
                            }
                            if (ImageHelper.canAccess(x, y, col, row, width, height, structureElement)) {
                                int offsetCol = x + (col - structureElement.getCenterCoordinate());
                                int offsetRow = y + (row - structureElement.getCenterCoordinate());

                                dilatatedPixels[ImageHelper.transformCoordinate(offsetCol, offsetRow, width)] = FOREGROUND_VAL;
                            }
                        }
                    }
                }
            }
        }
        return dilatatedPixels;
    }

    private byte[] doErosion(byte[] pixels, int width, int height, StructureElement structureElement) {

        byte[] erodedPixels = new byte[pixels.length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int pixelOfInterest = ImageHelper.doEndian(pixels[ImageHelper.transformCoordinate(x, y, width)]);
                erodedPixels[ImageHelper.transformCoordinate(x, y, width)] = (byte) pixelOfInterest;
                if (pixelOfInterest == FOREGROUND_VAL) {
                    boolean wasAlreadyEroded = false;
                    for (int row = 0; row < structureElement.getSize(); row++) {
                        for (int col = 0; col < structureElement.getSize(); col++) {
                            if (row == structureElement.getCenterCoordinate() && col == structureElement.getCenterCoordinate()) {
                                continue;
                            }
                            if (ImageHelper.canAccess(x, y, col, row, width, height, structureElement)) {
                                int offsetCol = x + (col - structureElement.getCenterCoordinate());
                                int offsetRow = y + (row - structureElement.getCenterCoordinate());

                                if (!wasAlreadyEroded) {
                                    if (ImageHelper.doEndian(pixels[ImageHelper.transformCoordinate(offsetCol, offsetRow, width)]) == BACKGROUND_VAL) {
                                        erodedPixels[ImageHelper.transformCoordinate(x, y, width)] = (byte) BACKGROUND_VAL;
                                        wasAlreadyEroded = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return erodedPixels;
    }
}
