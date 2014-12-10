package de.htw.berlin.student.gespic;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Class to determine the lowest, highest and middle variance of the leukocytes.
 *
 * @author by Matthias Drummer on 10.12.2014
 * @See: http://imagej.nih.gov/ij/docs/index.html
 */
public class Zyto_Variance implements PlugInFilter {


    @Override
    public int setup(String arg, ImagePlus imp) {
        return 0;
    }

    @Override
    public void run(ImageProcessor ip) {

        // TODO: Diese Klasse hier soll so gut wie keine Business Logik enthalten. Diese muss in separaten Klassen umgesetzt werden.
        // Aufgabe dieser Klasse ist es den Workflow umzusetzen.

        // Steps to do.....

        // 1st. Eliminate unneeded objects.

        // 2nd. find contours of objects

        // 3rd. Optional.

        // 4th. we have the coordinates of the objects. get color variances from the original image

        // 5th. create output image and paint borders of objects with different colors to show the correct processing.
    }
}
