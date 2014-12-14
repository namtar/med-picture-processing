package de.htw.berlin.student.gespic.utils;

/**
 * Helper class that provides subsidiary methods.
 * <p/>
 * Created by matthias.drummer on 05.11.14.
 */
public final class ImageHelper {

    private ImageHelper() {
    }

    public static boolean canAccess(int choordX, int choordY, int matrixX, int matrixY, int imageWidth, int imageHeight, StructureElement structureElement) {

        // translate matrixX
        int matrixXTrans = matrixX - structureElement.getCenterCoordinate();
        int matrixYTrans = matrixY - structureElement.getCenterCoordinate();

        // translate matrixY
        if ((choordX + matrixXTrans) < 0 || (choordX + matrixXTrans) > (imageWidth - 1)) {
            return false;
        }
        if ((choordY + matrixYTrans) < 0 || (choordY + matrixYTrans) > (imageHeight - 1)) {
            return false;
        }

        return true;
    }

    public static boolean checkBorder(int x, int y, int width, int height) {

        if (x < 0 || y < 0 || x == width || y == width) {
            return true;
        }
        return false;
    }

    public static int normalize(int value) {
        if (value > 255) {
            value = 255;
        } else if (value < 0) {
            value = 0;
        }
        return value;
    }

    public static int doEndian(byte value) {
        return value & 0xff;
    }

    public static int transformCoordinate(int x, int y, int width) {
        return (y * width) + x;
    }

    public static int getEndianPixelForCoordinate(byte[] pixels, int x, int y, int width) {
        return doEndian(pixels[transformCoordinate(x, y, width)]);
    }
}
