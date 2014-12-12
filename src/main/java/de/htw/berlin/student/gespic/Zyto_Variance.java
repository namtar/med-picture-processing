package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.ByteArrayTwoDimensionWrapper;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;

/**
 * Class to determine the lowest, highest and middle variance of the leukocytes.
 *
 * @author by Matthias Drummer on 10.12.2014
 * @See: http://imagej.nih.gov/ij/docs/index.html
 */
public class Zyto_Variance implements PlugInFilter {


    @Override
    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about")) {
            showAbout();
            return DONE;
        }
        // does rgb and does no changes to the original image, so that no undo is required.
        return DOES_RGB + NO_CHANGES;
    }

    /**
     * About Message zu diesem Plug-In.
     */
    void showAbout() {
        IJ.showMessage("Zyto Variance", "Plugin zur Ermittlung der Zyto Varianzen.");
    }

    @Override
    public void run(ImageProcessor ip) {

        // TODO: Diese Klasse hier soll so gut wie keine Business Logik enthalten. Diese muss in separaten Klassen umgesetzt werden.
        // Aufgabe dieser Klasse ist es den Workflow umzusetzen.
        ColorProcessor colorProcessor = (ColorProcessor) ip;
        ImagePlus outputImage = NewImage.createRGBImage("Output Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        Rectangle roi = ip.getRoi();

        int[] originalPixels = (int[]) colorProcessor.getPixels();
        int[] targetPixels = (int[]) outputImage.getProcessor().getPixels();
        ByteArrayTwoDimensionWrapper wrapper = new ByteArrayTwoDimensionWrapper(colorProcessor.getWidth(), colorProcessor.getHeight(), targetPixels, originalPixels);

        for (int y = (int) roi.getY(); y < roi.getY() + roi.getHeight(); y++) {
            for (int x = (int) roi.getX(); x < roi.getX() + roi.getWidth(); x++) {

                int pixel = wrapper.getOriginalImagePixel(x, y);
                // endian stuff
                int red = ((pixel & 0xff0000) >> 16);
                int green = (byte) ((pixel & 0x00ff00) >> 8);
                int blue = (byte) (pixel & 0x0000ff);

//                System.out.println(red);

                if (red > 140) {
                    wrapper.setOutputImagePixel(x, y, -1); // -1 does the trick to fill white
                } else {
                    wrapper.setOutputImagePixel(x, y, pixel);
                }
            }
        }

        outputImage.show();
        outputImage.updateAndDraw();

        // Steps to do.....

        // 1st. Eliminate unneeded objects. (everythin what is not between the color range for blue)
        // as the book says....
        /**
         * Die meisten Farbbilder sind mit jeweils einer Komponente für die Primärfarben
         * Rot, Grün und Blau (RGB) kodiert, typischerweise mit 8 Bits
         * pro Komponente. Jedes Pixel eines solchen Farbbilds besteht daher aus
         * 3 × 8 = 24 Bits und der Wertebereich jeder Farbkomponente ist wiederum
         * [0 . . . 255].
         */
        // hilfreich um die Farbkanäle zu extrahieren
        // http://crazybiocomputing.blogspot.de/2011/11/exploring-colors-and-grays-with-imagej.html
        // http://imagejdocu.tudor.lu/doku.php?id=plugin:start
        // sieht gut aus. aus http://www.mecourse.com/landinig/software/software.html ThresholdColor
        // c=ip.getPixel(x,y);
        // r = ((c&0xff0000)>>16);//R
        // g = ((c&0x00ff00)>>8);//G
        // b = ( c&0x0000ff); //B


        // 2nd. find contours of objects

        // 3rd. Optional.

        // 4th. we have the coordinates of the objects. get color variances from the original image

        // 5th. create output image and paint borders of objects with different colors to show the correct processing.
    }

    public static void main(String[] args) {

        new ImageJ();

        URL url = Zyto_Variance.class.getClassLoader().getResource("pic/zyto.jpg");

        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImagePlus imagePlus = new ImagePlus("Zyto Original", image);
        if (imagePlus != null) {
            imagePlus.show();
        }
//        imagePlus.setRoi(511, 119, 20, 20);

        IJ.runPlugIn(Zyto_Variance.class.getName(), "");
    }
}
