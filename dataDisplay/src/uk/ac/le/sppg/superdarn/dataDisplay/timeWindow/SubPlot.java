/*
 * Created on 10-Jun-2004
 */
package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import uk.ac.le.sppg.superdarn.colour.Legend;

/**
 * @author Nigel Wade
 */
class SubPlot  {

    class PlotImage {
        Image image;
        Graphics2D graphics;
        Legend legend;

        public PlotImage(int width, int height, Color colour, AffineTransform transform, Legend legend) {
            
            image = parent.createImage(width, height);
            graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(TimePlot.backgroundColour);
            graphics.fillRect(0, 0, width, height);
            graphics.setTransform(transform);

            this.legend = legend;

        }
    }

    final int width;
    final int height;
    final TimeWindow parent;

    HashMap<PlotSubParameter, PlotImage> plotImages;
    double startRange;
    double endRange;
    long startTime;
    long endTime;
    AffineTransform transform;

    PlotImage noiseImage;
    PlotImage freqImage;

    public SubPlot(  ArrayList<PlotSubParameter> plotWhat, TimeWindow parent,  int IMAGE_WIDTH, int IMAGE_HEIGHT,
        int FREQ_NOISE_IMAGE_HEIGHT,   double r1,   double r2,  long t1,  long t2) {

//System.out.println("create new subplot "+new Date(t1)+" "+new Date(t2));
        this.height = IMAGE_HEIGHT;
        this.width = IMAGE_WIDTH;
        this.parent = parent;

        plotImages = new HashMap<PlotSubParameter, PlotImage>();

        startRange = r1;
        endRange = r2;

        startTime = t1;
        endTime = t2;

        // create a transform to flip the vertical axis
        AffineTransform transform = new AffineTransform(1., 0., 0., -1., 0., (double) IMAGE_HEIGHT);

        // scale to the axis ranges.
        transform.scale((double) (IMAGE_WIDTH) / (t2 - t1), (double) (IMAGE_HEIGHT) / (r2 - r1));

        // translate the origin
        // the time axis is relative to the start of sub-plot 
        //  (the accuracy of the Graphics2D plotting is not sufficient
        //   to plot small time increments relative to the very large
        //   Java Date values.)
        transform.translate(0.0, -r1);

        for( PlotSubParameter what : plotWhat ) {
//        for (Iterator i = plotWhat.iterator(); i.hasNext();) {
//            String what = (String) i.next();
            PlotImage newImage =
                new PlotImage(IMAGE_WIDTH, IMAGE_HEIGHT, TimePlot.backgroundColour, transform, parent.getLegend(what.type));
            //System.out.println(parent);
            plotImages.put(what, newImage);
            //System.out.println("Created subplot for "+what);
        }

        // now the noise image
        noiseImage = new PlotImage(IMAGE_WIDTH, FREQ_NOISE_IMAGE_HEIGHT, TimePlot.backgroundColour, transform, null);

        // and the same for the freq image
        freqImage = new PlotImage(IMAGE_WIDTH, FREQ_NOISE_IMAGE_HEIGHT, TimePlot.backgroundColour, transform, null);

    }

    void dispose() {
        noiseImage.graphics.dispose();
        freqImage.graphics.dispose();

        for( PlotImage image : plotImages.values() ) {
//        for (Iterator i = plotImages.values().iterator(); i.hasNext();) {
//            PlotImage image = (PlotImage) i.next();
            image.graphics.dispose();
        }
    }

    synchronized Image getImage(PlotSubParameter parameter, boolean gs) {

        // needs optimization to improve speed, this is called
        // many times for each paintComponent.

        PlotImage plotImage;
        
        synchronized( plotImages ) {
            plotImage = plotImages.get(parameter);
        }
        
        if (plotImage == null) {
            return null;
        }

        return plotImage.image;

    }

    synchronized Image getImage(PlotSubParameter parameter) {
        PlotImage plotImage;
        synchronized( plotImages ) {
            plotImage = plotImages.get(parameter);
        }

        //System.out.println("getImage: asked for "+parameter+": got image "+plotImage);
        if (plotImage == null) {
            return null;
        }

        return plotImage.image;

    }

    synchronized PlotImage addParameter(PlotSubParameter what) {
        return addParameter(what, false);
    }

    synchronized PlotImage addParameter(PlotSubParameter parameter, boolean gs) {

        if (plotImages.isEmpty()) {
            return null;
        } else if (plotImages.containsKey(parameter)) {
            return plotImages.get(parameter);
        }

        Iterator i = plotImages.values().iterator();
        PlotImage firstImage = (PlotImage) i.next();
        AffineTransform transform = firstImage.graphics.getTransform();

        PlotImage newImage =
            new PlotImage(width, height, TimePlot.backgroundColour, transform, parent.getLegend(parameter.type));
        plotImages.put(parameter, newImage);


        return newImage;
    }

}
