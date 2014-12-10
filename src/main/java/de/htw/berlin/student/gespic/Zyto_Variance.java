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
}
