/**
* Plot2D.java
*
* Copyright (C) 2009 Cytobank, Inc. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
* Cytobank, Inc.
* 659 Oak Grove Avenue #205
* Menlo Park, CA 94025
*
* http://www.cytobank.org
*/
package facs;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.geom.Point2D;

import facs.scale.*;

/**
* <p>
* A collection of static methods for generating 2D plots. The methods return a
* <code>BufferedImage</code> object containing the plot image.
* </p>
*
* <p>
* Since it is just a collection of static methods, the class is final so that
* it cannot be extended.
* </p>
*/
public final class Plot2D {

    public static final int LINEAR_DISPLAY = 0;
    public static final int LOG_DISPLAY = 1;
    public static final int ARCSINH_DISPLAY_FLUOR = 2;
    public static final int ARCSINH_DISPLAY_CYTOF = 3;

    /**
     * The edge table for the marching squares algorithm
     */
    private static final int[] edgeTable2D = new int[] { 0x0, 0x9, 0x3, 0xa, 0x6, 0xf, 0x5, 0xc, 0xc, 0x5, 0xf, 0x6, 0xa, 0x3, 0x9, 0x0 };

    /**
     * The line table for the marching squares algorithm
     */
    private static final int[][] lineTable = new int[][] { { -1, -1, -1, -1, -1, -1, -1, -1 }, { 3, 0, -1, -1, -1, -1, -1, -1 },
            { 0, 1, -1, -1, -1, -1, -1, -1 }, { 3, 1, -1, -1, -1, -1, -1, -1 }, { 2, 1, -1, -1, -1, -1, -1, -1 }, { 3, 0, 2, 1, -1, -1, -1, -1 },
            { 2, 0, -1, -1, -1, -1, -1, -1 }, { 3, 2, -1, -1, -1, -1, -1, -1 }, { 3, 2, -1, -1, -1, -1, -1, -1 }, { 2, 0, -1, -1, -1, -1, -1, -1 },
            { 3, 2, 0, 1, -1, -1, -1, -1 }, { 2, 1, -1, -1, -1, -1, -1, -1 }, { 3, 1, -1, -1, -1, -1, -1, -1 }, { 0, 1, -1, -1, -1, -1, -1, -1 },
            { 3, 0, -1, -1, -1, -1, -1, -1 }, { -1, -1, -1, -1, -1, -1, -1, -1 } };

    /**
    * <p>
    * A private constructor to suppress the default constructor so the class
    * cannot be instantiated.
    * </p>
    */
    private Plot2D() {    }

        /**
         *
         * @param cs Canvas settings containing illustration parameters
         * @param xChanEvents Primary events, x coordinates (always use)
         * @param yChanEvents Primary events, y coordinates (always use)
         * @param xChanAllEvents Background events (drawn in background of primary events) (set to null if no nodes selected)
         * @param yChanAllEvents Background events (drawn in background of primary events) (set to null if no nodes selected)
         * @param xChanLabel Label for x axis
         * @param yChanLabel Label for y axis
         * @param xChanMaximum Minimum value for x axis
         * @param yChanMaximum Minimum value for y axis
         * @param xDisplay Display style (linear, log, arcsinh-fluor, arcsinh-cytof)
         * @param yDisplay Display style (linear, log, arcsinh-fluor, arcsinh-cytof)
         * @return
         * @throws IOException
         */
        public static BufferedImage drawPlot(
                CanvasSettings cs,
                double[] xChanEvents, double[] yChanEvents,
                double[] xChanAllEvents, double[] yChanAllEvents,
                String xChanLabel, String yChanLabel,
                double xChanMaximum, double yChanMaximum,
                int xDisplay, int yDisplay
                ) throws IOException {
        if ((cs == null) || (!cs.is2DPlot())) {
            // If the canvas settings is null or is not a 2D plot or a 3D plot,
            // then quit.
            return null;
        }

        // Get the canvas width
        int width = cs.getCanvasWidth();

        // Get the canvas height
        int height = cs.getCanvasHeight();

        // Create a buffered image in which we will draw the plot
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get the graphics of the buffered image
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background color
        g.setColor(cs.getBackgroundColor());
        g.fillRect(0, 0, width, height);

       /**
        * Draw the plot
        */

        // Move the graphics to the start of the first plot
        g.translate(cs.getStartX(), cs.getStartY());

        // Fill in the plot area with the plot background color
        g.setColor(cs.getPlotBackgroundColor());
        g.fillRect(0, 0, cs.getPlotWidth(), cs.getPlotHeight());

        // Move the graphics back to the origin
        g.translate(-cs.getStartX(), -cs.getStartY());

        if (xChanEvents.length > 0) {
            // If the population is not null, then draw the plot.

               /**
                * Get the scale information
                */

                Scale xScale = null;
                Scale yScale = null;

                ScaleArgument xScaleArgument = null;
                ScaleArgument yScaleArgument = null;

                double xMin = 0.0d;
                double xMax = 1.0d;

                double yMin = 0.0d;
                double yMax = 1.0d;

                int type;

                // Get the number of events
                int eventCount = xChanEvents.length;
                String scaleArg = null;

                // Get the scale type flag of the default scale of the x
                // channel

                if (xDisplay == LINEAR_DISPLAY) {
                    type = Scaling.LINEAR;
                    xMin = 1.0d;
                    xMax = xChanMaximum;
                } else if (xDisplay == LOG_DISPLAY) {
                    type = Scaling.LOG;
                    xMin = 1.0d;
                    xMax = xChanMaximum;
                } else if (xDisplay == ARCSINH_DISPLAY_FLUOR) {
                    type = Scaling.ARCSINH;
                    scaleArg = "150";
                    xMin = -200.0d;
                    xMax = 260000.0d;
                } else if (xDisplay == ARCSINH_DISPLAY_CYTOF) {
                    type = Scaling.ARCSINH;
                    scaleArg = "5";
                    xMin = -20.0d;
                    xMax = 10000.0d;
                } else {
                    return null;
                }

                // Get the default scale of the x channel
                xScale = Scaling.getScale(type);
                //zb: for reasons I don't understand, this goes through
                //three classes, from integer to string to integer...
                xScaleArgument = Scaling.getScaleArgument(type, scaleArg);

                // Get the scale type flag of the default scale of the y
                // channel
                if (yDisplay == LINEAR_DISPLAY) {
                    type = Scaling.LINEAR;
                    yMin = 1.0d;
                    yMax = yChanMaximum;
                } else if (yDisplay == LOG_DISPLAY) {
                    type = Scaling.LOG;
                    yMin = 1.0d;
                    yMax = yChanMaximum;
                } else if (yDisplay == ARCSINH_DISPLAY_FLUOR) {
                    type = Scaling.ARCSINH;
                    scaleArg = "150";
                    yMin = -200.0d;
                    yMax = 260000.0d;
                } else if (yDisplay == ARCSINH_DISPLAY_CYTOF) {
                    type = Scaling.ARCSINH;
                    scaleArg = "5";
                    yMin = -20.0d;
                    yMax = 10000.0d;
                } else {
                    return null;
                }

                // Get the default scale of the y channel
                yScale = Scaling.getScale(type);

                // Get the default scale argument of the y channel
                yScaleArgument = Scaling.getScaleArgument(type, scaleArg);


                if (Double.isNaN(xMin)) {
                    xMin = 0.0d;
                }
                if (Double.isNaN(xMax)) {
                    xMax = xChanMaximum;
                }
                if (Double.isNaN(yMin)) {
                    yMin = 0.0d;
                }
                if (Double.isNaN(yMax)) {
                    yMax = yChanMaximum;
                }

                // Get the type of the plot
                int plotType = cs.getPlotType();

                int[][] bins = null;
                // get the array of bin values
                bins = facs.PopulationL.getBinValues(xScale, yScale, xScaleArgument, yScaleArgument, cs.getNumXBins(), cs.getNumYBins(), xMin, xMax, yMin, yMax, xChanEvents, yChanEvents);

                // Draw the plot
                
                // Move the graphics to the start of the first plot
                g.translate(cs.getStartX(), cs.getStartY());

                // If there are background events to show, draw them as gray dots
                if (xChanAllEvents != null) {
                    int[][] bkgbins = facs.PopulationL.getBinValues(xScale, yScale, xScaleArgument, yScaleArgument, cs.getNumXBins(), cs.getNumYBins(), xMin, xMax, yMin, yMax, xChanAllEvents, yChanAllEvents);
                    //drawDots(bkgbins, g, Color.LIGHT_GRAY, cs.getPlotHeight());
                    double[][] bkgDensityValues = facs.PopulationL.getTriweightKernelDensityValues(bkgbins, cs.getSmoothing() * 2.5d);
                    double[][] bkgSortedDensityValues = facs.PopulationL.sortDensityValues(bkgDensityValues);
                    drawBkgContours(bkgbins, xChanAllEvents.length, bkgDensityValues, bkgSortedDensityValues, g, cs);
                }

                if (plotType == Representation.DOT_PLOT) {
                    // If the type of the plot is a dot plot, then draw a dot
                    // plot.
                    drawDots(bins, g, cs.getOutlierColor(), cs.getPlotHeight());
                } else if (plotType == Representation.DENSITY_DOT_PLOT) {
                    // If the type of the plot is a density dot plot, then draw
                    // a density dot plot.
                    double[][] sortedBinValues = facs.PopulationL.sortBinValues(bins);
                    drawDensityDots(bins, eventCount, sortedBinValues, g, cs);
                } else if (plotType == Representation.SHADOW_PLOT) {
                    // If the type of the plot is a shadow plot, then draw a
                    // shadow plot.
                    double[][] densityValues = facs.PopulationL.getTriweightKernelDensityValues(bins, cs.getSmoothing() * 2.5d);
                    double[][] sortedDensityValues =  facs.PopulationL.sortDensityValues(densityValues);
                    drawDensityDots(bins, eventCount, sortedDensityValues, g, cs);
                } else if (plotType == Representation.DENSITY_PLOT) {
                    // If the type of the plot is a density plot, then draw a
                    // density plot.
                    double[][] densityValues = facs.PopulationL.getTriweightKernelDensityValues(bins, cs.getSmoothing() * 0.5d);
                    double[][] sortedDensityValues = facs.PopulationL.sortDensityValues(densityValues);
                    drawDensityDots(bins, eventCount, sortedDensityValues, g, cs);
                } else {
                    // Otherwise, draw a contour plot, shaded or not.
                    double[][] densityValues = facs.PopulationL.getTriweightKernelDensityValues(bins, cs.getSmoothing() * 2.5d);
                    double[][] sortedDensityValues = facs.PopulationL.sortDensityValues(densityValues);
                    drawContours(bins, eventCount, densityValues, sortedDensityValues, g, cs, (plotType == Representation.SHADED_CONTOUR_PLOT));
                }

                // Draw the axes
                PlotUtilities.drawXAxis(g, xChanLabel, xScale, xScaleArgument, xMin, xMax, cs);
                PlotUtilities.drawYAxis(g, yChanLabel, yScale, yScaleArgument, yMin, yMax, cs);

                // Move the graphics back to the origin
                g.translate(-cs.getStartX(), -cs.getStartY());
        } //fi population is not null
        

        if (cs.drawAnnotation()) {
            // If the plot should be drawn with annotations, then draw a box and
            // a moat around the plot area.

            // Move the graphics to the start of the first plot
            g.translate(cs.getStartX(), cs.getStartY());

            // Draw a box around the plot area one pixel removed from the plot
            // area
            g.setColor(cs.getLineColor());
            g.drawRect(-CanvasSettings.BORDER_SIZE, -CanvasSettings.BORDER_SIZE, cs.getPlotWidth() + CanvasSettings.BORDER_SIZE + CanvasSettings.BORDER_SIZE
                    - 1, cs.getPlotHeight() + CanvasSettings.BORDER_SIZE + CanvasSettings.BORDER_SIZE - 1);

            // Move the graphics back to the origin
            g.translate(-cs.getStartX(), -cs.getStartY());
        }

        // Dispose of the graphics
        g.dispose();

        // Return the buffered image
        return image;
    }

   /**
    * <p>
    * Draws a simple dot plot of the bin values in the array of int arrays bins
    * using the graphics in the <code>Graphics</code> object g and the color in
    * the <code>java.awt.Color</code> object color.
    * </p>
    *
    * @param bins
    * array of int arrays containing the array of bin values.
    * @param g
    * <code>java.awt.Graphics</code> object with which to draw the
    * simple dot plot.
    * @param plotHeight
    * int height of the plot.
    */
    private static void drawDots(int[][] bins, Graphics g, Color color, int plotHeight) {
        if ((bins == null) || (bins.length <= 0) || (g == null)) {
            // If the array of bin values is null or empty or the graphics is
            // null, then quit.
            return;
        }

        // Calculate the y-coordinate of the first bin (the height of the plot
        // minus 1)
        int height = plotHeight - 1;

        // Set the color to color
        g.setColor(color);

        // Loop through the array of bin values
        for (int i = 0; i < bins.length; i++) {
            for (int j = 0; j < bins[i].length; j++) {
                if (bins[i][j] > 0) {
                    // If the value of the current bin is greater than 0, then
                    // draw a dot (rectangle) at the corresponding location.
                    g.fillRect(i, height - j, 1, 1);
                }
            }
        }
    }

    /**
    * drawDensityDots --- Draws a density dot plot.
    *
    * @param binValues
    * <code>int[][]</code> the binned data
    * @param numEvents
    * <code>int</code> the number of events
    * @param sortedDensityValues
    * <code>double[][]</code> the sorted density values
    * @param g
    * <code>Graphics</code> the graphics object on which to draw
    * @param cs
    * <code>CanvasSettings</code> the current canvas settings
    */
    private static void drawDensityDots(int[][] binValues, int numEvents, double[][] sortedDensityValues, Graphics g, CanvasSettings cs) {

        if ((binValues == null) || (binValues.length <= 0)) {
            // If the array of bin values is null or empty, then quit.
            return;
        }

        if ((sortedDensityValues == null) || (sortedDensityValues.length <= 0)) {
            // If the array of sorted density values is null or empty, then
            // quit.
            return;
        }

        // these are for quickly mapping a percent to a density value
        int numLevels = (int) ((100.0d - cs.getContourStartPercent()) / cs.getContourPercent()) + 1;
        int indexIncrement = (int) ((cs.getContourPercent() / 100.0) * (numEvents - 1));
        int index = (int) (cs.getContourStartPercent() / 100.0 * (numEvents - 1));
        int startK = 0;

        // we need to pass this a bunch
        int height = cs.getPlotHeight() - 1;

        // compute the colors for the plot
        ColorGradient colorgrad = new UnidirectionalColorGradient(0, numLevels - 1, cs.getColorSet());

        for (int i = 0; i < numLevels; i++) {
            for (int k = startK; k < sortedDensityValues.length; k++) {
                index -= binValues[(int) sortedDensityValues[k][1]][(int) sortedDensityValues[k][2]];
                if (index <= 0) {

                    drawColoredDots(binValues, sortedDensityValues, k, g, height, colorgrad.getColor(i));

                    // draw outliers if it's the first level
                    if (i == 0) {
                        g.setColor(cs.getOutlierColor());
                        drawOutliers(binValues, sortedDensityValues, k, g, height, cs.getDotSize());
                    }

                    // Commented out development bugchecking code
                    // g.setColor(Color.red);
                    // g.drawString("i: " + i + " NL: " + numLevels + " K: " + k
                    // + " Idx: " + index + " IdxI: "+indexIncrement + " EC: " +
                    // numEvents, -5, 20+i*10);

                    startK = k + 1;
                    break;
                }
            }

            index += indexIncrement;
        }
    }

    private static void drawColoredDots(int[][] binValues, double[][] sortedDensityValues, int firstIndex, Graphics g, int height, Color color) {
        if ((binValues == null) || (binValues.length <= 0)) {
            // If the array of bin values is null or empty, then quit.
            return;
        }

        if ((sortedDensityValues == null) || (sortedDensityValues.length <= 0)) {
            // If the array of sorted density values is null or empty, then
            // quit.
            return;
        }

        g.setColor(color);
        for (int i = firstIndex; i < sortedDensityValues.length; i++) {
            if (binValues[(int) sortedDensityValues[i][1]][(int) sortedDensityValues[i][2]] != 0) {
                g.fillRect((int) (sortedDensityValues[i][1]), (int) (height - sortedDensityValues[i][2]), 1, 1);
            }
        }
    }

    private static void drawOutliers(int[][] binValues, double[][] sortedDensityValues, int lastIndex, Graphics g, int height, int lw) {
        if ((binValues == null) || (binValues.length <= 0)) {
            // If the array of bin values is null or empty, then quit.
            return;
        }

        if ((sortedDensityValues == null) || (sortedDensityValues.length <= 0)) {
            // If the array of sorted density values is null or empty, then
            // quit.
            return;
        }

        for (int i = 0; i < lastIndex; i++) {
            if (binValues[(int) sortedDensityValues[i][1]][(int) sortedDensityValues[i][2]] != 0) {
                g.fillOval((int) (sortedDensityValues[i][1]), (int) (height - sortedDensityValues[i][2]), lw, lw);
                //g.fillRect((int) (sortedDensityValues[i][1]), (int) (height - sortedDensityValues[i][2]), 1, 1);
            }
        }
    }

        /**
    * Draws a BACKGROUND contour dot plot.
    *
    * @param binValues
    * <code>int[][]</code> the binned data
    * @param numEvents
    * int number of events
    * @param densityValues
    * <code>double[][]</code> the density values
    * @param sortedDensityValues
    * <code>double[][]</code> the sorted density values
    * @param g
    * <code>Graphics</code> the graphics object on which to draw
    * @param cs
    * <code>CanvasSettings</code> the current canvas settings
    * @param shaded
    * <code>boolean</code> indicates whether the levels are shaded
    * or not
    */
    private static void drawBkgContours(int[][] binValues, int numEvents, double[][] densityValues, double[][] sortedDensityValues, Graphics g, CanvasSettings cs) {
        if ((binValues == null) || (binValues.length <= 0)) {
            return;
        }
        if ((densityValues == null) || (densityValues.length <= 0)) {
            return;
        }
        if ((sortedDensityValues == null) || (sortedDensityValues.length <= 0)) {
            return;
        }

        // these are for quickly mapping a percent to a density value
        int numLevels = (int) ((100.0d - cs.getContourStartPercent()) / cs.getContourPercent()) + 1;
        int indexIncrement = (int) ((cs.getContourPercent() / 100.0) * (numEvents - 1));
        int index = (int) (cs.getContourStartPercent() / 100.0 * (numEvents - 1));
        int startK = 0;

        // we have to pass this a bunch of times
        int height = cs.getPlotHeight() - 1;

        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < numLevels; i++) {
            for (int k = startK; k < sortedDensityValues.length; k++) {
                index -= binValues[(int) sortedDensityValues[k][1]][(int) sortedDensityValues[k][2]];
                if (index <= 0) {
                        // fill in the level with the contour color
                        drawColoredLevel(binValues, sortedDensityValues, k, g, height, cs.getContourColor());

                        // Reset the color to the line color
                        g.setColor(Color.LIGHT_GRAY);

                    // draw the contour line
                    drawContourLevel(densityValues, sortedDensityValues[k][0], g, height);

                    //Never draw outliers for background
                    // draw outliers if it's the first level
//                    if (i == 0) {
//                        drawOutliers(binValues, sortedDensityValues, k, g, height, cs.getDotSize());
//                    }

                    startK = k + 1;
                    break;
                }
            }

            index += indexIncrement;
        }
    }


    /**
    * Draws a contour dot plot.
    *
    * @param binValues
    * <code>int[][]</code> the binned data
    * @param numEvents
    * int number of events
    * @param densityValues
    * <code>double[][]</code> the density values
    * @param sortedDensityValues
    * <code>double[][]</code> the sorted density values
    * @param g
    * <code>Graphics</code> the graphics object on which to draw
    * @param cs
    * <code>CanvasSettings</code> the current canvas settings
    * @param shaded
    * <code>boolean</code> indicates whether the levels are shaded
    * or not
    */
    private static void drawContours(int[][] binValues, int numEvents, double[][] densityValues, double[][] sortedDensityValues, Graphics g, CanvasSettings cs,
            boolean shaded) {
        if ((binValues == null) || (binValues.length <= 0)) {
            // If the array of bin values is null or empty, then quit.
            return;
        }

        if ((densityValues == null) || (densityValues.length <= 0)) {
            // If the array of density values is null or empty, then quit.
            return;
        }

        if ((sortedDensityValues == null) || (sortedDensityValues.length <= 0)) {
            // If the array of sorted density values is null or empty, then
            // quit.
            return;
        }

        // these are for quickly mapping a percent to a density value
        int numLevels = (int) ((100.0d - cs.getContourStartPercent()) / cs.getContourPercent()) + 1;
        int indexIncrement = (int) ((cs.getContourPercent() / 100.0) * (numEvents - 1));
        int index = (int) (cs.getContourStartPercent() / 100.0 * (numEvents - 1));
        int startK = 0;

        // we have to pass this a bunch of times
        int height = cs.getPlotHeight() - 1;

        // if the contours are shaded, compute their colors
        ColorGradient colorgrad = null;
        if (shaded) {
            colorgrad = new UnidirectionalColorGradient(0, numLevels - 1, cs.getColorSet());
        }

        g.setColor(cs.getOutlierColor());
        for (int i = 0; i < numLevels; i++) {
            for (int k = startK; k < sortedDensityValues.length; k++) {
                index -= binValues[(int) sortedDensityValues[k][1]][(int) sortedDensityValues[k][2]];
                if (index <= 0) {
                    // fill in the level if appropriate
                    if (shaded) {
                        drawColoredLevel(binValues, sortedDensityValues, k, g, height, colorgrad.getColor(i));

                        // Reset the color to the line color
                        // g.setColor(cs.getOutlierColor());
                    } else {
                        // fill in the level with the contour color
                        drawColoredLevel(binValues, sortedDensityValues, k, g, height, cs.getContourColor());

                        // Reset the color to the line color
                        g.setColor(cs.getOutlierColor());
                    }

                    // draw the contour line
                    drawContourLevel(densityValues, sortedDensityValues[k][0], g, height);

                    // draw outliers if it's the first level
                    if (i == 0) {
                        drawOutliers(binValues, sortedDensityValues, k, g, height, cs.getDotSize());
                    }

                    startK = k + 1;
                    break;
                }
            }

            index += indexIncrement;
        }
    }

    /**
    * <p>
    * Draws a contour level?
    * </p>
    */
    private static void drawContourLevel(double[][] densityValues, double h, Graphics g, int height) {
        for (int i = 0; i < densityValues.length - 1; i++)
            for (int j = 0; j < densityValues[i].length - 1; j++)
                marchSquare(densityValues, i, j, h, g, height);
    }

    /**
    * <p>
    * Performs the marching square algorithm?
    * </p>
    */
    private static void marchSquare(double[][] densityValues, int i, int j, double h, Graphics g, int height) {
        int index = 0;

        if (densityValues[i][j] < h)
            index |= 1;

        if (densityValues[i + 1][j] < h)
            index |= 2;

        if (densityValues[i + 1][j + 1] < h)
            index |= 4;

        if (densityValues[i][j + 1] < h)
            index |= 8;

        if (index == 0)
            return;

        int edges = edgeTable2D[index];

        Point2D.Double[] lineIntersections = new Point2D.Double[4];

        if ((edges & 1) != 0)
            lineIntersections[0] = linearInterpolate(h, densityValues[i][j], densityValues[i + 1][j], i, j, i + 1, j);

        if ((edges & 2) != 0)
            lineIntersections[1] = linearInterpolate(h, densityValues[i + 1][j], densityValues[i + 1][j + 1], i + 1, j, i + 1, j + 1);

        if ((edges & 4) != 0)
            lineIntersections[2] = linearInterpolate(h, densityValues[i + 1][j + 1], densityValues[i][j + 1], i + 1, j + 1, i, j + 1);

        if ((edges & 8) != 0)
            lineIntersections[3] = linearInterpolate(h, densityValues[i][j + 1], densityValues[i][j], i, j + 1, i, j);

        // draw the lines
        for (int t = 0; lineTable[index][t] != -1; t += 2) {
            g.drawLine((int) Math.round(lineIntersections[lineTable[index][t]].x), (int) Math.round(height - lineIntersections[lineTable[index][t]].y),
                    (int) Math.round(lineIntersections[lineTable[index][t + 1]].x), (int) Math.round(height - lineIntersections[lineTable[index][t + 1]].y));
        }
    }

    private static Point2D.Double linearInterpolate(double h, double fa, double fb, double ax, double ay, double bx, double by) {
        if (fa == fb)
            return (new Point2D.Double(ax, ay));

        double s = (h - fa) / (fb - fa);
        return (new Point2D.Double(ax + s * (bx - ax), ay + s * (by - ay)));
    }

    private static void drawColoredLevel(int[][] binValues, double[][] sortedDensityValues, int firstIndex, Graphics g, int height, Color color) {
        g.setColor(color);
        for (int i = firstIndex; i < sortedDensityValues.length; i++) {
            g.fillRect((int) (sortedDensityValues[i][1]), (int) (height - sortedDensityValues[i][2]), 1, 1);
        }
    }

    /**
    * Heatmap methods
    */

    /**
    * <p>
    * Returns the heatmap of the grid of values in the array of double arrays
    * values using the colors in the color gradient in the
    * <code>ColorGradient</code> object gradient.
    * </p>
    *
    * <p>
    * The tricky part about generating heatmaps is that rows correspond to the
    * y-direction whereas columns correspond to the x-direction, so the two for
    * loops through the grid of values and the actual image traversal go in
    * different directions.
    * </p>
    *
    * @param values
    * array of double arrays containing the grid of values.
    * @param gradient
    * <code>ColorGradient</code> object to the color gradient.
    * @return <code>BufferedImage</code> object to the heatmap of the grid of
    * values in the array of double arrays values using the colors in
    * the color gradient in the <code>ColorGradient</code> object
    * gradient.
    */
    public static BufferedImage drawHeatmap(double[][] values, ColorGradient gradient) {
        if ((values == null) || (gradient == null) || (values.length <= 0) || (values[0].length <= 0)) {
            // If the grid of values or the color gradient is null or the grid
            // of values is empty, then quit.
            return null;
        }

        // Get the number of rows in the grid of values
        int rowCount = values.length;

        // Get the number of columns in the grid of values
        int columnCount = values[0].length;

        // Get the size of each heatmap cell
        int size = CanvasSettings.HEATMAP_SIZE;

        // Calculate the width of the plot based on the number of columns
        int width = columnCount * size;

        // Calculate the height of the plot based on the number of rows
        int height = rowCount * size;

        // Create a buffered image in which we will draw the heatmap
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get the graphics of the buffered image
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background color to white
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        /**
        * Draw the heatmap of the grid of values
        */
        int x, y;

        // Loop through the rows
        for (int i = 0; i < rowCount; i++) {
            // Loop through the columns
            for (int j = 0; j < columnCount; j++) {

                // Calculate the upper left x-coordinate of the heatmap cell
                x = j * size;

                // Calculate the upper left y-coordinate of the heatmap cell
                y = i * size;

                if (Double.isNaN(values[i][j])) {
                    // If the current value is not a number, then draw a red
                    // diagonal line through the heatmap cell from the upper
                    // left to the lower right.
                    g.setColor(Color.red);
                    g.drawLine(x, y, x + size, y + size);
                } else {
                    // Otherwise, the current value is a number, so draw the
                    // heatmap cell for the current value with the appropriate
                    // color.
                    g.setColor(gradient.getColor(values[i][j]));
                    g.fillRect(x, y, size, size);
                }
            }
        }

        /**
        * Draw the border lines of the heatmap cells, as necessary
        */

        if ((rowCount > 1) || (columnCount > 1)) {
            // If the number of rows or the number of columns is greater than 1,
            // then the heatmap has more than one cell, so draw the border
            // lines.

            // Set the color to black
            g.setColor(Color.black);

            // Draw the lines between heatmap cell rows
            for (int i = 1; i < rowCount; i++) {
                // Calculate the y-coordinate of the line
                y = i * size;

                // Draw the current line
                g.drawLine(0, y, width - 1, y);
            }

            // Draw the lines between heatmap cell columns
            for (int i = 1; i < columnCount; i++) {
                // Calculate the x-coordinate of the line
                x = i * size;

                // Draw the current line
                g.drawLine(x, 0, x, height - 1);
            }

            // Draw a rectangle around the entire heatmap
            g.drawRect(0, 0, width - 1, height - 1);
        }

        // Dispose of the graphics
        g.dispose();

        // Return the buffered image
        return image;
    }

    /**
    * Testing methods
    */

    /**
    * <p>
    * A private static method to test drawing a heatmap.
    * </p>
    *
    * <p>
    * It generates a heatmap whose first two rows should look similar as they
    * are only off by 1.0. The same is true with the last two rows. The first
    * two rows and the last two rows are the reverse of each other.
    * </p>
    *
    * @param filename
    * <code>String</code> filename of the file to which to write the
    * heatmap.
    */
    private static void testHeatmap(String filename) {
        // Create a dummy grid of values
        double[][] values = new double[][] { { 0.0d, 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d, 7.0d, 8.0d, 9.0d },
                { 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d, 7.0d, 8.0d, 9.0d, 10.0d }, { 9.0d, 8.0d, 7.0d, 6.0d, 5.0d, 4.0d, 3.0d, 2.0d, 1.0d, 0.0d },
                { 10.0d, 9.0d, 8.0d, 7.0d, 6.0d, 5.0d, 4.0d, 3.0d, 2.0d, 1.0d } };

        /*
        * double[][] values = new double[][] { {246.6, 249.0, 249.7, 224.5,
        * 246.6, 234.8, 231.9, 245.0}, {243.7, 243.2, 245.2, 220.5, 243.7,
        * 230.6, 229.0, 239.4} };
        */

        // Draw the heatmap
        BufferedImage image = drawHeatmap(values, new UnidirectionalColorGradient(0.0d, 10.0d, Color.black, Color.white));

        // Initialize the file to null
        File file = null;

        if ((filename == null) || (filename.length() <= 0)) {
            // If the filename is null or empty, then set the file to
            // "heatmap.png".
            file = new File("heatmap.png");
        } else {
            // Otherwise, the filename is not null and not empty, so set the
            // file to the file with filename.
            file = new File(filename);
        }

        try {
            // Try to write the heatmap out to the file
            ImageIO.write(image, "png", file);
        } catch (IOException ioe) {
            // If an IOException occurred, then print it out.
            System.err.println(ioe.toString());
        }
    }

    /**
    * <p>
    * A private static method to test drawing the labels of the histogram
    * overlay.
    * </p>
    *
    * @param filename
    * <code>String</code> filename of the file to which to write the
    * labels of the histogram overlay.
    */
    private static void testHistogramOverlayLabels(String filename) {
        // Create a dummy array of labels
        String[] labels = { "Label 1", "Label 2", "Label 3", "Label 4", "Label 5" };

        // Create a dummy canvas settings
        CanvasSettings cs = CanvasSettings.getCanvasSettings(0, 32, 0, 0, 1, Representation.HISTOGRAM_OVERLAY, Representation.EN_FUEGO, false, true, true,
                true, true, false, 128, 1.0d, 1.0d, 10.0d, 10.0d, -1, 10000, 1);

        // Draw the labels of the histogram overlay
        BufferedImage image = PlotUtilities.drawHistogramOverlayLabels(labels, cs);

        // Initialize the file to null
        File file = null;

        if ((filename == null) || (filename.length() <= 0)) {
            // If the filename is null or empty, then set the file to
            // "labels.png".
            file = new File("labels.png");
        } else {
            // Otherwise, the filename is not null and not empty, so set the
            // file to the file with filename.
            file = new File(filename);
        }

        try {
            // Try to write the heatmap out to the file
            ImageIO.write(image, "png", file);
        } catch (IOException ioe) {
            // If an IOException occurred, then print it out.
            System.err.println(ioe.toString());
        }
    }

    /**
    * <p>
    * A private static method to test drawing the scale of a color gradient.
    * </p>
    *
    * @param filename
    * <code>String</code> filename of the file to which to write the
    * scale.
    */
    private static void testColorGradient(String filename) {
        // Create a dummy gradient for testing
        // ColorGradient gradient = new UnidirectionalColorGradient(0.0d, 10.0d,
        // Color.black, Color.white);
        // ColorGradient gradient =
        // UnidirectionalColorGradient.getUnidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT,
        // false, Double.NaN, Double.NaN);
        // ColorGradient gradient =
        // UnidirectionalColorGradient.getUnidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT,
        // false, 5.0d, 15.0d);
        // ColorGradient gradient =
        // UnidirectionalColorGradient.getUnidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT,
        // true, 5.0d, 15.0d);
        ColorGradient gradient = BidirectionalColorGradient.getBidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT, 5.0d, 10.0d, 15.0d);

        // Draw the scale of the color gradient
        BufferedImage image = PlotUtilities.drawColorGradient(gradient);

        // Initialize the file to null
        File file = null;

        if ((filename == null) || (filename.length() <= 0)) {
            // If the filename is null or empty, then set the file to
            // "heatmap.png".
            file = new File("gradient.png");
        } else {
            // Otherwise, the filename is not null and not empty, so set the
            // file to the file with filename.
            file = new File(filename);
        }

        try {
            // Try to write the heatmap out to the file
            ImageIO.write(image, "png", file);
        } catch (IOException ioe) {
            // If an IOException occurred, then print it out.
            System.err.println(ioe.toString());
        }
    }

    /**
    * <p>
    * A main method to test the class.
    * </p>
    *
    * @param args
    * <code>String</code> array of arguments at the command prompt.
    */
    public static void main(String[] args) {
        if (args.length > 0) {
            // If the array of arguments is not empty, then use the first
            // argument as the filename of the heatmap.
            // testHeatmap(args[0]);
            testHistogramOverlayLabels(args[0]);
            // testColorGradient(args[0]);
        } else {
            // Otherwise, the array of arguments is empty, so use null as the
            // filename of the heatmap.
            // testHeatmap(null);
            testHistogramOverlayLabels(null);
            // testColorGradient(null);
        }
    }
}