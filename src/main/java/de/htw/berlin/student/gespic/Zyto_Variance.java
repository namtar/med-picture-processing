package de.htw.berlin.student.gespic;

import de.htw.berlin.student.gespic.utils.ImageHelper;
import de.htw.berlin.student.gespic.utils.IntArrayTwoDimensionWrapper;
import ij.IJ;
import ij.IJEventListener;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.text.NumberFormat;
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

        ColorProcessor colorProcessor = (ColorProcessor) ip;

        width = colorProcessor.getWidth();
        height = colorProcessor.getHeight();

        ImagePlus mixedColorImage = NewImage.createByteImage("Mixed Color Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        byte[] mixedColorPixels = (byte[]) mixedColorImage.getProcessor().getPixels();
        Rectangle roi = ip.getRoi();

        int[] originalPixels = (int[]) colorProcessor.getPixels();
        IntArrayTwoDimensionWrapper wrapper = new IntArrayTwoDimensionWrapper(colorProcessor.getWidth(), colorProcessor.getHeight(), null, originalPixels);

        for (int y = (int) roi.getY(); y < roi.getY() + roi.getHeight(); y++) {
            for (int x = (int) roi.getX(); x < roi.getX() + roi.getWidth(); x++) {

                int pixel = wrapper.getOriginalImagePixel(x, y);
                // endian stuff
                int red = ((pixel & 0xff0000) >> 16);
                int green = ((pixel & 0x00ff00) >> 8);
                int blue = (pixel & 0x0000ff);

                //                System.out.println(red);

                //				0.8 * blue + 0.2 * red
                //				double channelVal = (0.8 * blue) + (0.1 * red);
                // calculate the grey value by using the proportion of the brightness
                double channelVal = (blue / ((0.33 * blue) + (0.33 * red) + (0.33 * green) / 3) * 100);
                if (channelVal < 140) {
                    channelVal = Constants.BACKGROUND_VAL;
                }

                mixedColorPixels[wrapper.transformCoordinate(x, y)] = (byte) channelVal;
            }
        }
        
        mixedColorImage.show();
        mixedColorImage.updateAndDraw();

        ClosingHelper closingHelper = new ClosingHelper(mixedColorPixels, colorProcessor.getWidth(), colorProcessor.getHeight());
        ImagePlus dilatatedImage = NewImage.createByteImage("Cleared Up Object Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);
        byte[] closedPixels = closingHelper.doClosing();
        dilatatedImage.getProcessor().setPixels(closedPixels);

        dilatatedImage.show();
        dilatatedImage.updateAndDraw();

        ImagePlus tracedImage = NewImage.createByteImage("Traced Image", colorProcessor.getWidth(), colorProcessor.getHeight(), 1, NewImage.FILL_WHITE);

        byte[] tracePixels = new byte[mixedColorPixels.length];
        for (int i = 0; i < mixedColorPixels.length; i++) {
            tracePixels[i] = mixedColorPixels[i];
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
                    //					int pixel = ImageHelper.getEndianPixelForCoordinate(blueSourcePixels, pixelCoordinate.getX(), pixelCoordinate.getY(), width);
                    int pixel = ImageHelper.getEndianPixelForCoordinate(mixedColorPixels, pixelCoordinate.getX(), pixelCoordinate.getY(), width);

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

        calculateAndOutputVariances(polygonValues);
    }

    private void calculateAndOutputVariances(Map<Polygon, Set<Pixel>> polygonValues) {
        ResultsTable r = new ResultsTable();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        int count =0;




        int polyCounter = 1;
        for (Entry<Polygon, Set<Pixel>> entry : polygonValues.entrySet()) {

            // determine middle value
            double sumGrey = 0;
            for (Pixel pixel : entry.getValue()) {
                sumGrey += pixel.getValue();
            }

            double middleValue = sumGrey / entry.getValue().size();

            double lowestVariance = 255d;
            double highestVariance = 0d;
            double varianceSum = 0;
            // determine lowest, highest, middlest variance
            for (Pixel pixel : entry.getValue()) {

                double diff = middleValue - pixel.getValue();
                diff = Math.abs(diff);

                if (diff < lowestVariance) {
                    lowestVariance = diff;
                }
                if (diff > highestVariance) {
                    highestVariance = diff;
                }

                varianceSum += diff;
            }

            double middlestVariance = varianceSum / entry.getValue().size();

//            StringBuilder stringBuilder = new StringBuilder("Variance for Polygon: ");
//            stringBuilder.append(polyCounter);
//            if (entry.getKey().npoints > 0) {
//                stringBuilder.append(" Start Coordinate: X: ");
//                stringBuilder.append(entry.getKey().xpoints[0]);
//                stringBuilder.append(" Y: ");
//                stringBuilder.append(entry.getKey().ypoints[0]);
//            }
//            stringBuilder.append(" Middlevalue: ");
//            stringBuilder.append(middleValue);
//            stringBuilder.append(" Lowest Variance: ");
//            stringBuilder.append(lowestVariance);
//            stringBuilder.append(" Middlest Variance: ");
//            stringBuilder.append(middlestVariance);
//            stringBuilder.append(" Highest Variance: ");
//            stringBuilder.append(highestVariance);


            //Output result Table

            r.incrementCounter();
            r.setValue("x",count,nf.format(entry.getKey().xpoints[0]));
            r.setValue("y",count,nf.format(entry.getKey().ypoints[0]));
            r.setValue("Meiddlevalue",count,middleValue);
            r.setValue("Lowest Variance",count,lowestVariance);
            r.setValue("Middlest Variance",count,middlestVariance);
            r.setValue("Highest Variance",count,highestVariance);


            count++;
            // Outup to Log
            //IJ.log(stringBuilder.toString());




            //Console Output
            //System.out.println(stringBuilder.toString());

            polyCounter++;
        }
        r.show("Result");
    }


    public static void main(String[] args) {

        new ImageJ();

        URL url = Zyto_Variance.class.getClassLoader().getResource("pic/zyto.jpg");
//		 URL url = Zyto_Variance.class.getClassLoader().getResource("pic/Inet1.jpg");

        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImagePlus imagePlus = new ImagePlus("Zyto Original", image);
        if (imagePlus != null) {
            imagePlus.show();
        }
        //        imagePlus.setRoi(545, 454, 55, 55);

        IJ.runPlugIn(Zyto_Variance.class.getName(), "");
    }
}
