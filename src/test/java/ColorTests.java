import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * @author by Matthias Drummer on 12.12.2014
 */
public class ColorTests {

    public static void main(String[] args) {

        ImagePlus ip = NewImage.createRGBImage("Test", 100, 100, 1, NewImage.FILL_BLACK);

        ImageProcessor imageProcessor = ip.getProcessor();
        ColorProcessor colorProcessor;
        if (imageProcessor instanceof ColorProcessor) {
            colorProcessor = (ColorProcessor) imageProcessor;
        } else {
            throw new IllegalStateException("For a rgb image there must be a color processor present.");
        }

        int[] pixels = (int[]) colorProcessor.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            // set blue
            byte blue = (byte) 255;
            byte red = 115;
            byte green = 0;

//            red = 0;

            int val = 0xff000000 | ((red & 0xff) << 16) | ((green & 0xff) << 8) | blue & 0xff;
            System.out.println(val);
            pixels[i] = val;

        }

        ip.show();
        ip.updateAndDraw();

    }
}
