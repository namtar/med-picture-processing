package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.ImageHelper;
import de.htw.berlin.student.gespic.utils.IntArrayTwoDimensionWrapper;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/**
 * Class to determine the lowest, highest and middle variance of the leukocytes.
 *
 * @author by Matthias Drummer on 10.12.2014
 * @See: http://imagej.nih.gov/ij/docs/index.html
 */
public class Zyto_Variance implements PlugInFilter {

    private int width;
    private int height;

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

        width = colorProcessor.getWidth();
        height = colorProcessor.getHeight();

//        ImagePlus outputImage = NewImage.createRGBImage("Output Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        ImagePlus outputImage = NewImage.createByteImage("Output Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        Rectangle roi = ip.getRoi();

        int[] originalPixels = (int[]) colorProcessor.getPixels();
//        int[] targetPixels = (int[]) outputImage.getProcessor().getPixels();
        byte[] blueSourcePixels = (byte[]) outputImage.getProcessor().getPixels();
        IntArrayTwoDimensionWrapper wrapper = new IntArrayTwoDimensionWrapper(colorProcessor.getWidth(), colorProcessor.getHeight(), null, originalPixels);

        for (int y = (int) roi.getY(); y < roi.getY() + roi.getHeight(); y++) {
            for (int x = (int) roi.getX(); x < roi.getX() + roi.getWidth(); x++) {

                int pixel = wrapper.getOriginalImagePixel(x, y);
                // endian stuff
                int red = ((pixel & 0xff0000) >> 16);
                int green = (byte) ((pixel & 0x00ff00) >> 8);
                int blue = (byte) (pixel & 0x0000ff);

//                System.out.println(red);

                if (red > 140) {
//                    wrapper.setOutputImagePixel(x, y, -1); // -1 does the trick to fill white
                    blueSourcePixels[wrapper.transformCoordinate(x, y)] = -1;
                } else {
//                    wrapper.setOutputImagePixel(x, y, blue);
                    blueSourcePixels[wrapper.transformCoordinate(x, y)] = (byte) blue;
                }
            }
        }

        outputImage.show();
        outputImage.updateAndDraw();

        ClosingHelper closingHelper = new ClosingHelper(blueSourcePixels, colorProcessor.getWidth(), colorProcessor.getHeight());
        ImagePlus dilatatedImage = NewImage.createByteImage("Dilatated Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        byte[] closedPixels = closingHelper.doClosing();
        dilatatedImage.getProcessor().setPixels(closedPixels);

        dilatatedImage.show();
        dilatatedImage.updateAndDraw();

        ImagePlus tracedImage = NewImage.createByteImage("Traced Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);

        byte[] tracePixels = new byte[blueSourcePixels.length];
        for (int i = 0; i < blueSourcePixels.length; i++) {
            tracePixels[i] = blueSourcePixels[i];
        }

        ContourTracer contourTracer = new ContourTracer(tracePixels, closedPixels, colorProcessor.getWidth(), colorProcessor.getHeight());
        tracedImage.getProcessor().setPixels(contourTracer.startTracing());

        tracedImage.show();
        tracedImage.updateAndDraw();

//        Vector<Polygon> polygons = contourTracer.getPolygonsVector();
//        Map<Polygon, List<Integer>> polygonValues = new HashMap<Polygon, List<Integer>>();
//        for (int y = 0; y < colorProcessor.getHeight(); y++) {
//            for (int x = 0; x < colorProcessor.getWidth(); x++) {
//                int pixel = ImageHelper.getEndianPixelForCoordinate(blueSourcePixels, x, y, colorProcessor.getWidth());
//                for (int i = 0; i < polygons.size(); i++) {
//                    Polygon polygon = polygons.elementAt(i);
//                    if (polygon.contains(x, y)) {
//                        if (!polygonValues.containsKey(polygon)) {
//                            polygonValues.put(polygon, new ArrayList<Integer>());
//                        }
//                        polygonValues.get(polygon).add(pixel);
//                    }
//                }
//            }
//        }
//        for (Entry<Polygon, List<Integer>> entry : polygonValues.entrySet()) {
//            System.out.println(entry.getValue());
//        }

        Vector<Polygon> polygons = contourTracer.getPolygonsVector();
        Map<Polygon, Set<Pixel>> polygonValues = new HashMap<Polygon, Set<Pixel>>();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.elementAt(i);
            List<Pixel> values = new ArrayList<Pixel>();

            // Code from Digitale Bilverarbeitung mit ImageJ
            Stack<Pixel> stack = new Stack<Pixel>(); // Stack
            stack.push(new Pixel(polygon.xpoints[0], polygon.ypoints[0]));
            while (!stack.isEmpty()) {
                Pixel pixelCoordinate = stack.pop();
                if ((pixelCoordinate.getX() >= 0) && (pixelCoordinate.getX() < width) && (pixelCoordinate.getY() >= 0) && (pixelCoordinate.getY() < height)) {
                    int pixel = ImageHelper.getEndianPixelForCoordinate(blueSourcePixels, pixelCoordinate.getX(), pixelCoordinate.getY(), width);
                    if (pixel != Constants.BACKGROUND_VAL && !values.contains(pixelCoordinate)) {

                        pixelCoordinate.setValue(pixel);
                        values.add(pixelCoordinate);
                        stack.push(new Pixel(pixelCoordinate.getX() + 1, pixelCoordinate.getY()));
                        stack.push(new Pixel(pixelCoordinate.getX(), pixelCoordinate.getY() + 1));
                        stack.push(new Pixel(pixelCoordinate.getX(), pixelCoordinate.getY() - 1));
                        stack.push(new Pixel(pixelCoordinate.getX() - 1, pixelCoordinate.getY()));
                    }
                }
            }
            polygonValues.put(polygon, new HashSet<Pixel>(values));
        }

//            collectValues(polygon.xpoints[0], polygon.ypoints[0], colorProcessor.getWidth(), colorProcessor.getHeight(), values, blueSourcePixels);
//            polygonValues.put(polygon, new HashSet<Pixel>(values));


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

//    private void collectValues(int x, int y, int width, int height, List<Pixel> valueSet, byte[] pixels) {
//
//        if (x < 0 || x == width || y < 0 || y == height) {
//            return;
//        }
//
//        int pixel = ImageHelper.getEndianPixelForCoordinate(pixels, x, y, width);
//        if (pixel != Constants.BACKGROUND_VAL) {
//            valueSet.add(new Pixel(x, y, pixel));
//            collectValues(x + 1, y, width, height, valueSet, pixels);
//            collectValues(x - 1, y, width, height, valueSet, pixels);
//            collectValues(x, y + 1, width, height, valueSet, pixels);
//            collectValues(x, y - 1, width, height, valueSet, pixels);
//        }
//    }

    public static void main(String[] args) {

        new ImageJ();

        URL url = Zyto_Variance.class.getClassLoader().getResource("pic/zyto.jpg");
//        URL url = Zyto_Variance.class.getClassLoader().getResource("pic/Inet1.jpg");

        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImagePlus imagePlus = new ImagePlus("Zyto Original", image);
        if (imagePlus != null) {
            imagePlus.show();
        }
//        imagePlus.setRoi(545, 454, 55, 55);

        IJ.runPlugIn(Zyto_Variance.class.getName(), "");
    }
}
