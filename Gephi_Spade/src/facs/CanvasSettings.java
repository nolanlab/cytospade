/**
* CanvasSettings.java
*
*
* Cytobank (TM) is server and client software for web-based management, analysis,
* and sharing of flow cytometry data.
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

// Import the util package
import java.util.*;

// Import the scale package
import facs.scale.*;

/**
* <p>
* A class to contain the parameters and settings of the graphical portion of a
* plot. This class essentially re-interprets a subset of the Illustration
* parameters and turns them into settings needed to make a plot image in a
* plotting function (e.g. Plot2D). Illustration lays out a general vision of a
* plot and CanvasSettings takes care of the details.
* </p>
*
* <p>
* Where Illustration (or Representation before it) is a big bag of settings,
* CanvasSettings is a more specific interpretation of those settings for an
* individual image containing a plot, its scales, and the canvas on which it is
* drawn.
* </p>
*
* <p>
* The following diagram depicts the location of the different paddings and
* measurements. It is a useful reference when trying to understand what is
* going on.
* </p>
*
* <pre>
* &lt;---------------------- Canvas Width ----------------------&gt;
* +--+----------------------------------------------------------+--+ &circ;
* | | Top Padding | | |
* +--+----------------------------------------------------------+--+ |
* | | &lt;- Plot Width -&gt; | | |
* | | +--+------------------+--+ | | |
* | | | | Top Padding | | | | |
* | | +--+------------------+--+ &circ; |R | |
* | L| | L| |R | | |i | |
* | e| | t| |t | | |g | |
* | f| | .| |. | | |h | |
* | t| | | | | | |t |
* | | | P| |P | | | Canvas Height
* | P| | a| Plot |a | Plot Height |P |
* | a| | d| |d | |a | |
* | d| | d| |d | | |d | |
* | d| | i| |i | | |d | |
* | i| | n| |n | | |i | |
* | n| | g| |g | | |n | |
* | g| +--+------------------+--+ v |g | |
* | | | | Bottom Padding | | | | |
* | | +--+------------------+--+ | | |
* | | | | |
* | | | | |
* +--+----------------------------------------------------------+--+ |
* | | Bottom Padding | | |
* +--+----------------------------------------------------------+--+ v
* </pre>
*
* <p>
* As much as possible, I have tried to remove the parts of the canvas settings
* that deal with aggregates since that unnecessarily complicates the class (and
* exert no control over the aggregates any way). Some constants are still
* included in the <code>CanvasSettings</code> class, but that is about as far
* as the it is involved in drawing the aggregates.
* </p>
*
* <p>
* Eventually, the histogram overlay settings will be consolidated into the
* canvas settings, but that is still a ways off.
* </p>
*/
public final class CanvasSettings {
    /**
* The size of the border in each direction of the plot
*/
    public static final int BORDER_SIZE = 2;

    /**
* The size of each heatmap cell
*/
    public static final int HEATMAP_SIZE = 25;

    /**
* The size of the small buffer at the top of histograms so there is room
* above the peaks
*/
    public static final int HIST_BUFFER = 15;

    /**
* The width of the statistic iframe
*/
    public static final int STATISTIC_IFRAME_WIDTH = 450;

    /**
* The height of the statistic iframe
*/
    public static final int STATISTIC_IFRAME_HEIGHT = 250;

    /**
* The length of the tick mark
*/
    public static final int STROKE_LENGTH = 6;

    /**
* The height of the pseudo-z-axis label and scale
*/
    public static final int PSEUDO_Z_HEIGHT = 20;

    /**
* The fonts
*/
    public static final Font ARIAL_FONT_LARGE = new Font("Arial", Font.PLAIN, 14);
    public static final Font ARIAL_FONT_MEDIUM = new Font("Arial", Font.PLAIN, 12);
    public static final Font ARIAL_FONT = new Font("Arial", Font.PLAIN, 11);
    public static final Font ARIAL_FONT_SMALL = new Font("Arial", Font.PLAIN, 10);
    public static final Font ARIAL_FONT_TINY = new Font("Arial", Font.PLAIN, 8);

    /**
* The font to use for region labels in the plots
*/
    public static final Font REGION_LABEL_FONT = new Font("Arial", Font.PLAIN, 9);

    /**
* The font to use for critical labels
*/
    public static final Font CRITICAL_LABEL_FONT = new Font("Arial", Font.PLAIN, 17);

    /**
* Formatters for formatting doubles and ints
*/

    /**
* A public formatter for formatting doubles
*/
    public static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.00");

    /**
* A public formatter for formatting ints
*/
    public static final DecimalFormat INT_FORMAT = new DecimalFormat("#,###,###,###,###");

    /**
* ***********************************************************************
* ***********************************************************************
* Constant Color Sets
*
* Constant arrays of Colors used to compare populations when plotting.
*
* If you are a developer, feel free to add in your own color set. Here are
* some guidelines:
*
* - White and Black are reserved colors. Do not add white or black (or
* colors too close to white and black) to your color set.
*
* - Generally, hot colors (red, yellow, orange, pink) should indicate
* up/high and cool colors (green, blue, dark purple) should inidicate
* down/low.
*
* - It is good to balance a wide dynamic range (contrasting intensities and
* colors) with intuitive readability (it's obvious which color means
* up/high vs. down/low).
*
* - Monotone scales print well in black and white (it's easy to tell what
* is up/high and down/low, even without the color) which may be something
* to consider when designing your scale.
*
* - Make sure to say if your scale is designed to be used in a particular
* way (e.g. designed to be used with pseudo-3D plots).
*
* - In the comments, specify a short name for the colorset that can be used
* in the user interface. If you're feeling inspired, explain your
* influences and logic.
*
* - If it's your first color set, sign your work by making the name of the
* colorset your name.
*
* ***********************************************************************
* ***********************************************************************
*/

    /**
* The default 1D color set for histograms Black to yellow
*/
    private static final Color[] DEFAULT_COLOR_SET_1D = { new Color(0, 0, 0), new Color(204, 24, 0) };

    /**
* The greyscale (black and white) color set
*/
    private static final Color[] GREYSCALE = { new Color(0, 0, 0), new Color(255, 255, 255) };

    /**
* Firey colors that Jonathan likes to use for density dot plots. purple,
* red, orange, and yellow.
*/
    private static final Color[] EN_FUEGO = { new Color(50, 0, 80), new Color(75, 0, 80), new Color(102, 0, 0), new Color(150, 0, 0), new Color(204, 0, 0),
            new Color(255, 67, 0), new Color(255, 102, 0), new Color(255, 186, 51), new Color(255, 217, 51), new Color(255, 255, 51), new Color(255, 255, 150) };

    /**
* Firey colors that work well for pseudo-3D plots where you want to
* emphasize only significant changes.
*/
    private static final Color[] HETEROGENEITY_ON_FIRE = { new Color(0, 0, 0), new Color(25, 0, 40), new Color(50, 0, 80), new Color(75, 0, 80),
            new Color(102, 0, 0), new Color(102, 0, 0), new Color(150, 0, 0), new Color(204, 0, 0), new Color(255, 67, 0), new Color(255, 102, 0),
            new Color(255, 186, 51), new Color(255, 217, 51), new Color(255, 255, 51), new Color(255, 255, 150) };

    /**
* Blue, black, yellow
*
* Industry standard for phospho-flow and protein arrays
*
*/
    private static final Color[] BLUE_TO_YELLOW = { new Color(150, 150, 255), new Color(0, 0, 0), new Color(255, 255, 150) };

    /**
* Green, black, red
*
* Industry standard for DNA/RNA microarrays and CGH arrays
*
*/
    private static final Color[] GREEN_TO_RED = { new Color(0, 150, 0), new Color(0, 0, 0), new Color(200, 0, 0) };

    /**
* Rainbow color scale
*
* Popular among flow cytometry users. This scale has a wide dynamic range,
* but it can be confusing to interpret which colors are high and low. It
* also loses information when printed in black and white.
*
*/
    private static final Color[] RAINBOW = { new Color(0, 0, 255), new Color(0, 255, 255), new Color(0, 255, 0), new Color(255, 255, 0), new Color(255, 0, 0) };

    /**
* Cool color scale. Blue, green
*/
    private static final Color[] CHILL_OUT = { new Color(66, 7, 101), new Color(52, 23, 126), new Color(0, 20, 239), new Color(42, 151, 132),
            new Color(37, 239, 67), new Color(60, 239, 150) };

    /**
* The default color set for 2D plots will be EN_FUEGO.
*/
    private static final Color[] DEFAULT_COLOR_SET_2D = EN_FUEGO;

    /**
* A simple color set that Ryan likes to use for colored contour plots.
* (Red, orange, yellow.)
*/
    private static final Color[] RYANS_SIMPLE_COLOR_SET = { new Color(210, 0, 0), new Color(255, 162, 0), new Color(255, 240, 0) };

    /**
* Jonathan
*/
    private static final Color[] JONATHANS_SIMPLE_COLOR_SET = EN_FUEGO;

    /**
* William - a la glace
*/
    private static final Color[] WILLIAMS_SIMPLE_COLOR_SET = {
            // new Color(255, 255, 255), white and black are reserved colors and
            // should not be included in color sets, except in special cases
            new Color(230, 255, 255), new Color(230, 255, 175), new Color(205, 255, 175), new Color(180, 255, 175), new Color(153, 255, 255),
            new Color(105, 255, 255), new Color(51, 255, 255), new Color(51, 231, 255), new Color(0, 188, 255), new Color(0, 153, 255), new Color(0, 69, 204),
            new Color(0, 38, 204), new Color(0, 0, 204), new Color(0, 0, 105) };

    /**
* Jonathan's remix of William's colorset
*/
    private static final Color[] PALE_FIRE = { new Color(0, 0, 105), new Color(0, 0, 204), new Color(0, 38, 204), new Color(0, 69, 204),
            new Color(0, 153, 255), new Color(0, 188, 255), new Color(51, 231, 255), new Color(51, 255, 255), new Color(105, 255, 255),
            new Color(153, 255, 255), new Color(180, 255, 175), new Color(205, 255, 175), new Color(230, 255, 175), new Color(230, 255, 255) };

    /**
* Nikesh
*/
    private static final Color[] NIKESHS_SIMPLE_COLOR_SET = { new Color(0, 0, 153), new Color(255, 240, 0) };

    /**
* Mark
*/
    private static final Color[] MARKS_SIMPLE_COLOR_SET = { new Color(0, 0, 0) };

    /**
* Peter
*/
    private static final Color[] PETERS_SIMPLE_COLOR_SET = { new Color(0, 0, 0) };

    /**
* Single color sets
*/
    private static final Color[] GREEN_COLOR_SET = { new Color(0, 0, 0), new Color(0, 255, 0) };
    private static final Color[] RED_COLOR_SET = { new Color(0, 0, 0), new Color(255, 0, 0) };
    private static final Color[] BLUE_COLOR_SET = { new Color(0, 0, 0), new Color(0, 0, 255) };
    private static final Color[] YELLOW_COLOR_SET = { new Color(0, 0, 0), new Color(255, 255, 0) };
    private static final Color[] FUSCIA_COLOR_SET = { new Color(0, 0, 0), new Color(255, 0, 255) };
    private static final Color[] CYAN_COLOR_SET = { new Color(0, 0, 0), new Color(0, 255, 255) };

    /**
* Instance variables - this is where the class starts
*/

    /**
* The size of the flow data portion of the graphic (as opposed to the size
* of the canvas)
*/

    /**
* The width of the plot
*/
    private int plotWidth;

    /**
* The height of the plot
*/
    private int plotHeight;

    /**
* The size of the canvas (aka the size of the whole image)
*/

    /**
* The width of the canvas
*/
    private int canvasWidth;

    /**
* The height of the canvas
*/
    private int canvasHeight;

    /**
* The amount of padding in each direction (These correspond to the padding
* regions where text, hash marks, and labels can be added.) See the diagram
* above for more information.
*/

    /**
* The amount of padding at the bottom of each plot
*/
    private int bottomPad;

    /**
* The amount of padding at the top of each plot
*/
    private int topPad;

    /**
* The amount of padding to the left of each plot
*/
    private int leftPad;

    /**
* The amount of padding to the right of each plot
*/
    private int rightPad;

    /**
* The amount of padding at the bottom of the canvas
*/
    private int canvasBottomPad;

    /**
* The amount of padding at the top of the canvas
*/
    private int canvasTopPad;

    /**
* The amount of padding to the left of the canvas
*/
    private int canvasLeftPad;

    /**
* The amount of padding to the right of the canvas
*/
    private int canvasRightPad;

    /**
* The color set (array of colors)
*/
    private Color[] colorSet;

    /**
* The number of bins on the x-axis
*/
    private int numXBins;

    /**
* The number of bins on the y-axis
*/
    private int numYBins;

    /**
* The number of rows in the canvas
*/
    private int rowCount;

    /**
     * The size of the dots (for outliers esp)
     */
    private int dotSize;
    /**
* The number of columns in the canvas
*/
    private int columnCount;

    /**
* The horizontal spacing between plots
*/
    private int hSpacing;

    /**
* The vertical spacing between plots
*/
    private int vSpacing;

    /**
* Plot parameters
*/

    /**
* The number of channels
*/
    private int channelCount;

    /**
* The x channel
*/
    private int xChannel;

    /**
* The y channel
*/
    private int yChannel;

    /**
* The type of the plot
*/
    private int plotType;

    /**
* The boolean flag indicating whether to use white text on a black
* background
*/
    private boolean blackBackgroundP;

    /**
* The boolean flag indicating whether to draw a plot with annotations
*/
    private boolean annotationP;

    /**
* The boolean flag indicating whether to draw the scale labels
*/
    private boolean scaleLabelP;

    /**
* The boolean flag indicating whether to draw the scale ticks
*/
    private boolean scaleTickP;

    /**
* The boolean flag indicating whether to draw the axis labels
*/
    private boolean axisLabelP;

    /**
* The boolean flag indicating whether to use long labels
*/
    private boolean longLabelP;

    /**
* The smoothing level of the smoothing function to use
*/
    private double smoothing;

    /**
* The aspect ratio of the histogram
*/
    private double aspectRatio;

    /**
* The percentage in each level of the contour plot
*/
    private double contourPercent;

    /**
* The starting percentage of the contour plot
*/
    private double contourStartPercent;

    /**
* Population type parameters
*/

    /**
* The type of the population
*/
    private int populationType;

    /**
* The number of events to get from the flow file
*/
    private int eventCount;

    /**
* Scale parameters
*/

    /**
* The array of scale type flags
*/
    private int[] scaleFlags;

    /**
* The array of scale argument strings
*/
    private String[] scaleArguments;

    /**
* The array of channel minimums
*/
    private double[] minimums;

    /**
* The array of channel maximums
*/
    private double[] maximums;

    /**
* Region parameters
*/

    /**
* The list of regions
*/
    private ArrayList<PlotRegion> regions;

    /**
* <p>
* A constructor for <code>CanvasSettings</code>.
* </p>
*
* <p>
* The constructor should not be called directly since it is quite easy to
* create invalid states for the canvas settings.
* </p>
*
* @param numXBins
* int number of bins on the x-axis.
* @param numYBins
* int number of bins on the y-axis.
* @param hSpacing
* int horizontal spacing between plots
* @param vSpacing
* int vertical spacing between plots.
* @param channelCount
* int number of channels in the canvas settings.
* @param xChannel
* int x channel.
* @param yChannel
* int y channel.
* @param plotType
* int constant flag of the type of the plot.
* @param colorSet
* int constant flag of the color set.
* @param blackBackgroundP
* boolean flag indicating whether to use white text on a black
* background.
* @param annotationP
* boolean flag indicating whether to draw a plot with
* annotations.
* @param scaleLabelP
* boolean flag indicating whether to draw the scale labels.
* @param scaleTickP
* boolean flag indicating whether to draw the scale ticks.
* @param axisLabelP
* boolean flag indicating whether to draw the axis labels.
* @param longLabelP
* boolean flag indicating whether to use long labels.
* @param axisBins
* int number of axis bins on each axis.
* @param smoothing
* double smoothing level of the smoothing function to use.
* @param aspectRatio
* double aspect ratio of the histogram.
* @param contourPercent
* double percentage in each level of the contour plot.
* @param contourStartPercent
* double starting percentage of the contour plot.
* @param populationType
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
*/
    private CanvasSettings(int numXBins, int numYBins, int hSpacing, int vSpacing, int channelCount, int xChannel, int yChannel, int plotType, int colorSet,
            boolean blackBackgroundP, boolean annotationP, boolean scaleLabelP, boolean scaleTickP, boolean axisLabelP, boolean longLabelP, int axisBins,
            double smoothing, double aspectRatio, double contourPercent, double contourStartPercent, int populationType, int eventCount, int dotSize) {
        // Set the number of bins on the x-axis
        this.numXBins = numXBins;

        // Set the number of bins on the y-axis
        this.numYBins = numYBins;

        // Set the size of the dots
        this.dotSize = dotSize;

        /**
* Note: For now, we are just going to make each plot the same size as
* the number of axis bins on each axis. If we were to try to draw the
* image to be whatever size was passed, it would look weird when the
* size is not a multiple of the number of bins. One fix would be to
* always draw the image at its true size (same size as the number of
* axis bins on each axis), and then rescale it to whatever size it
* needs to be AFTER the drawing is finished. This is potentially quite
* expensive computationally.
*/
        plotWidth = numXBins;
        plotHeight = numYBins;

        // Set the horizontal spacing between plots
        this.hSpacing = hSpacing;

        // Set the vertical spacing between plots
        this.vSpacing = vSpacing;

        if (channelCount <= 0) {
            // If the number of channels is less than or equal to 0, then set
            // the number of channels to 0.
            this.channelCount = 0;
        } else {
            // Otherwise, the number of channels is greater than 0, so set the
            // number of channels to it.
            this.channelCount = channelCount;
        }

        /**
* Plot parameters
*/

        // Set the x channel
        this.xChannel = xChannel;

        // Set the y channel
        this.yChannel = yChannel;

        // Set the type of the plot
        this.plotType = plotType;

        if (is1DPlot()) {
            // If the plot is an 1D plot, then set the color set to the default
            // 1D color set.
            this.colorSet = DEFAULT_COLOR_SET_1D;

            if (plotType == Illustration.HISTOGRAM_X) {
                // If the plot is a histogram of the x channel, then recalculate
                // the height of the plot using the aspect ratio.
                plotHeight = (int) ((double) numYBins * aspectRatio);
            } else if (plotType == Illustration.HISTOGRAM_Y) {
                // If the plot is a histogram of the y channel, then recalculate
                // the width of the plot using the aspect ratio.
                plotWidth = (int) ((double) numXBins * aspectRatio);
            }
        } else {
            // Otherwise, the plot is not an 1D plot, so set the color set.
            setColorSet(colorSet);
        }

        // Set whether to use white text on a black background to false
        this.blackBackgroundP = blackBackgroundP;

        // Set whether to draw a plot with annotations
        this.annotationP = annotationP;

        // Set whether to draw the scale labels
        this.scaleLabelP = scaleLabelP;

        // Set whether to draw the scale ticks
        this.scaleTickP = scaleTickP;

        // Set whether to draw the axis labels
        this.axisLabelP = axisLabelP;

        // Set whether to use long labels
        this.longLabelP = longLabelP;

        // Set the smoothing level of the smoothing function to use
        this.smoothing = smoothing;

        // Set the aspect ratio of the histogram
        this.aspectRatio = aspectRatio;

        // Set the percentage in each level of the contour plot
        this.contourPercent = contourPercent;

        // Set the starting percentage of the contour plot
        this.contourStartPercent = contourStartPercent;

        /**
* Population type parameters
*/

        // Set the type of the population
        this.populationType = populationType;

        // Set the number of events to get from the flow file
        this.eventCount = eventCount;

        /**
* Scale parameters
*/

        // Allocate the array of scale type flags
        scaleFlags = new int[this.channelCount];

        // Allocate the array of scale argument strings
        scaleArguments = new String[this.channelCount];

        // Allocate the array of channel minimums
        minimums = new double[this.channelCount];

        // Allocate the array of channel maximums
        maximums = new double[this.channelCount];

        // Loop through the channels
        for (int i = 0; i < this.channelCount; i++) {
            // Initialize the scale type flag to 1
            scaleFlags[i] = 1;

            // Initialize the channel minimum to Double.NaN
            minimums[i] = Double.NaN;

            // Initialize the channel maximum to Double.NaN
            maximums[i] = Double.NaN;
        }

        /**
* Region parameters
*/

        // Initialize the list of regions
        regions = new ArrayList<PlotRegion>();

        // Initialize the number of rows to 1
        rowCount = 1;

        // Initialize the number of columns to 1
        columnCount = 1;

        // Calculate the padding using one row and one column
        calculatePadding(rowCount, columnCount);
    }

    /**
* <p>
* Calculates the padding based on the canvas settings.
* </p>
*
* <p>
* The method originally used groupACount and groupBCount, which made no
* sense if you didn't know which group was group A and which was group B.
* In the new scheme, think of multiple plots as being arranged in a table
* in the canvas. rowCount is the number of rows in that table and the
* columnCount is the number of columns in that table. It actually makes
* very little difference since groupACount is the same as rowCount and
* groupBCount is the same as columnCount; however, rowCount and columnCount
* are simply much easier to remember and to figure out.
* </p>
*
* <p>
* If the number of rows is less than or equal to 0, then it is set to 1. If
* the number of columns is less than or equal to 0, then it is set to 1.
* This way, the canvas will always have at least one plot.
* </p>
*
* @param rowCount
* int number of rows in the canvas.
* @param columnCount
* int number of columns in the canvas.
*/
    private void calculatePadding(int rowCount, int columnCount) {
        if (rowCount <= 0) {
            // If the number of rows is less than or equal to 0, then set it to
            // 1.
            rowCount = 1;
        }

        if (columnCount <= 0) {
            // If the number of columns is less than or equal to 0, then set it
            // to 1.
            columnCount = 1;
        }

        /**
* Initialize all the padding to 0
*/
        bottomPad = 0;
        topPad = 0;
        leftPad = 0;
        rightPad = 0;
        canvasBottomPad = 0;
        canvasTopPad = 0;
        canvasLeftPad = 0;
        canvasRightPad = 0;

        if (annotationP) {
            // If the plot should be drawn with annotations, then add some
            // padding for the annotations.
            bottomPad = BORDER_SIZE;
            topPad = BORDER_SIZE;
            leftPad = BORDER_SIZE;
            rightPad = BORDER_SIZE;

            canvasBottomPad = 3;
            canvasTopPad = 3;
            canvasLeftPad = 3;
            canvasRightPad = 3;

            if (scaleLabelP) {
                // If the scale labels should be drawn, then add some more
                // padding.
                if (plotType == Illustration.HISTOGRAM_Y) {
                    // If the plot is a histogram of the y channel, then add 4
                    // to the bottom padding.
                    bottomPad += 4;
                } else {
                    // Otherwise, the plot is not a histogram of the y channel,
                    // so add 14 to the bottom padding.
                    // And add a little padding on the right
                    bottomPad += 14;
                    rightPad += 3;
                }

                if (plotType == Representation.HISTOGRAM_OVERLAY) {
                    // If the plot is a histogram overlay, then add 3 to the
                    // canvas left padding.
                    canvasLeftPad += 3;
                }

                if ((plotType == Representation.HISTOGRAM_X) || (plotType == Representation.HISTOGRAM_OVERLAY)) {
                    // If the plot is a standard histogram (not flipped, so
                    // histogram of the x channel or a histogram overlay), then
                    // add 4 to the left padding.
                    leftPad += 4;
                } else {
                    // Otherwise, the plot is not a standard histogram, so add
                    // 18 to the left padding.
                    leftPad += 18;
                }

                if (!scaleTickP) {
                    // If the scale ticks should not be drawn, then add some
                    // more padding.
                    bottomPad += STROKE_LENGTH;
                    leftPad += STROKE_LENGTH;
                }
            }

            if (scaleTickP) {
                // If the scale ticks should be drawn, then add some more
                // padding.
                bottomPad += STROKE_LENGTH;
                leftPad += STROKE_LENGTH;
            }

            if (axisLabelP) {
                // If the axis labels should be drawn, then add some more
                // padding.
                if (plotType != Illustration.HISTOGRAM_Y) {
                    // If the plot is not a histogram of the y channel, then add
                    // 22 to the bottom padding.
                    bottomPad += 22;
                }

                if ((plotType != Representation.HISTOGRAM_X) && (plotType != Representation.HISTOGRAM_OVERLAY)) {
                    // If the plot is not a histogram of the x channel and is
                    // not a histogram overlay, then add 20 to the left padding.
                    leftPad += 20;
                }

                if (isPseudo3DPlot()) {
                    canvasTopPad += PSEUDO_Z_HEIGHT;
                }
            }
        }

        if (plotType == Representation.HISTOGRAM_OVERLAY) {
            // If the plot is a histogram overlay, then calculate the size of
            // the canvas differently.
            canvasWidth = canvasLeftPad + leftPad + plotWidth + rightPad + canvasRightPad;
            canvasHeight = canvasTopPad + topPad + plotHeight + bottomPad + vSpacing * (rowCount - 1) + canvasBottomPad;
        } else {
            // Otherwise, the plot is not a histogram overlay, so calculate the
            // size of the canvas using the number of rows and the number of
            // columns.
            canvasWidth = canvasLeftPad + ((leftPad + plotWidth + rightPad + hSpacing) * columnCount) - hSpacing + canvasRightPad;
            canvasHeight = canvasTopPad + ((topPad + plotHeight + bottomPad + vSpacing) * rowCount) - vSpacing + canvasBottomPad;
        }

    }

    /**
* <p>
* Sets the color set to the color set indicated by the constant flag
* colorSet.
* </p>
*
* @param colorSet
* int constant flag of the color set.
*/
    public void setColorSet(int colorSet) {
        // Set the color set based on the constant flag
        switch (colorSet) {
        case Illustration.EN_FUEGO:
            this.colorSet = EN_FUEGO;
            return;
        case Illustration.HETEROGENEITY_ON_FIRE:
            this.colorSet = HETEROGENEITY_ON_FIRE;
            return;
        case Illustration.CHILL_OUT:
            this.colorSet = CHILL_OUT;
            return;
        case Illustration.RAINBOW:
            this.colorSet = RAINBOW;
            return;
        case Illustration.GREYSCALE:
            this.colorSet = GREYSCALE;
            return;
        case Illustration.PALE_FIRE:
            this.colorSet = PALE_FIRE;
            return;
        case Illustration.GREEN_TO_RED:
            this.colorSet = GREEN_TO_RED;
            return;
        case Illustration.BLUE_TO_YELLOW:
            this.colorSet = BLUE_TO_YELLOW;
            return;
        case Illustration.RYAN1:
            this.colorSet = RYANS_SIMPLE_COLOR_SET;
            return;
        case Illustration.WILLIAM1:
            this.colorSet = WILLIAMS_SIMPLE_COLOR_SET;
            return;
        case Illustration.NIKESH1:
            this.colorSet = NIKESHS_SIMPLE_COLOR_SET;
            return;
        case Illustration.MARK1:
            this.colorSet = MARKS_SIMPLE_COLOR_SET;
            return;
        case Illustration.PETER1:
            this.colorSet = PETERS_SIMPLE_COLOR_SET;
            return;
        case Illustration.BLUE:
            this.colorSet = BLUE_COLOR_SET;
            return;
        case Illustration.RED:
            this.colorSet = RED_COLOR_SET;
            return;
        case Illustration.YELLOW:
            this.colorSet = YELLOW_COLOR_SET;
            return;
        case Illustration.GREEN:
            this.colorSet = GREEN_COLOR_SET;
            return;
        case Illustration.FUSCIA:
            this.colorSet = FUSCIA_COLOR_SET;
            return;
        case Illustration.CYAN:
            this.colorSet = CYAN_COLOR_SET;
            return;

            // Otherwise, set the color set to DEFAULT_COLOR_SET_2D.
        default:
            this.colorSet = DEFAULT_COLOR_SET_2D;
            return;
        }
    }

    /**
* <p>
* Returns whether the plot is an 1D plot.
* </p>
*
* @return boolean flag indicating whether the plot is an 1D plot.
*/
    public boolean is1DPlot() {
        return ((plotType == Representation.HISTOGRAM_X) || (plotType == Representation.HISTOGRAM_Y) || (plotType == Representation.HISTOGRAM_OVERLAY));
    }

    /**
* <p>
* Returns whether the plot is a pseudo 3D plot.
* </p>
*
* @return boolean flag indicating whether the plot is a pseudo 3D plot.
*/
    public boolean isPseudo3DPlot() {
        return ((plotType == Illustration.THIRD_AXIS_MEDIAN_PLOT) || (plotType == Illustration.THIRD_AXIS_NINETYFIFTH_PLOT));
    }

    /**
* <p>
* Returns whether the plot is an 2D plot.
* </p>
*
* @return boolean flag indicating whether the plot is an 2D plot.
*/
    public boolean is2DPlot() {
        return (!is1DPlot());
    }

    /**
* <p>
* Sets the number of rows to rowCount thereby forcing the canvas size to be
* recalculated.
* </p>
*
* @param rowCount
* int number of rows in the canvas.
*/
    public void setRowCount(int rowCount) {
        if (rowCount > 0) {
            // If the number of rows is greater than 0, then set the number of
            // rows.
            this.rowCount = rowCount;

            // Recalculate the canvas size
            calculatePadding(this.rowCount, columnCount);
        }
    }

    /**
* <p>
* Sets the number of columns to columnCount thereby forcing the canvas size
* to be recalculated.
* </p>
*
* @param columnCount
* int number of columns in the canvas.
*/
    public void setColumnCount(int columnCount) {
        if (columnCount > 0) {
            // If the number of columns is greater than 0, then set the number
            // of columns.
            this.columnCount = columnCount;

            // Recalculate the canvas size
            calculatePadding(rowCount, this.columnCount);
        }
    }

    /**
* Spacing methods
*/

    /**
* <p>
* Returns the width of the plot (the flow data portion of the image).
* </p>
*
* @return int width of the plot (the flow data portion of the image).
*/
    public int getPlotWidth() {
        return plotWidth;
    }

    /**
* <p>
* Returns the height of the plot (the flow data portion of the image).
* </p>
*
* @return int height of the plot (the flow data portion of the image).
*/
    public int getPlotHeight() {
        return plotHeight;
    }

    /**
* <p>
* Returns the width of the canvas.
* </p>
*
* @return int width of the canvas.
*/
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
* <p>
* Returns the height of the canvas.
* </p>
*
* @return int height of the canvas.
*/
    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getDotSize() {
        return dotSize;
    }

    /**
* <p>
* Returns the amount of padding at the bottom of each plot.
* </p>
*
* @return int amount of padding at the bottom of each plot.
*/
    public int getBottomPad() {
        return bottomPad;
    }

    /**
* <p>
* Returns the amount of padding at the top of each plot.
* </p>
*
* @return int amount of padding at the top of each plot.
*/
    public int getTopPad() {
        return topPad;
    }

    /**
* <p>
* Returns the amount of padding to the left of each plot.
* </p>
*
* @return int amount of padding to the left of each plot.
*/
    public int getLeftPad() {
        return leftPad;
    }

    /**
* <p>
* Returns the amount of padding to the right of each plot.
* </p>
*
* @return int amount of padding to the right of each plot.
*/
    public int getRightPad() {
        return rightPad;
    }

    /**
* <p>
* Returns the amount of padding at the bottom of the canvas.
* </p>
*
* @return int amount of padding at the bottom of the canvas
*/
    public int getCanvasBottomPad() {
        return canvasBottomPad;
    }

    /**
* <p>
* Returns the amount of padding at the top of the canvas.
* </p>
*
* @return int amount of padding at the top of the canvas.
*/
    public int getCanvasTopPad() {
        return canvasTopPad;
    }

    /**
* <p>
* Returns the amount of padding to the left of the canvas.
* </p>
*
* @return int amount of padding to the left of the canvas.
*/
    public int getCanvasLeftPad() {
        return canvasLeftPad;
    }

    /**
* <p>
* Returns the amount of padding to the right of the canvas.
* </p>
*
* @return int amount of padding to the right of the canvas.
*/
    public int getCanvasRightPad() {
        return canvasRightPad;
    }

    /**
* <p>
* Returns the array of colors containing the color set.
* </p>
*
* @return <code>Color</code> array of colors containing the color set.
*/
    public Color[] getColorSet() {
        return colorSet;
    }

    /**
* <p>
* Returns the number of bins on the x axis.
* </p>
*
* @return int number of bins on the x axis.
*/
    public int getNumXBins() {
        return numXBins;
    }

    /**
* <p>
* Returns the number of bins on the y axis.
* </p>
*
* @return int number of bins on the y axis.
*/
    public int getNumYBins() {
        return numYBins;
    }

    /**
* <p>
* Returns the number of bins on the z axis.
* </p>
*
* @return int number of bins on the z axis, which is the number of bins on
* the x axis, for now.
*/
    public int getNumZBins() {
        return numXBins;
    }

    /**
* <p>
* Returns the horizontal spacing between plots.
* </p>
*
* @return int horizontal spacing between plots.
*/
    public int getPlotHorizSpacing() {
        return hSpacing;
    }

    /**
* <p>
* Returns the vertical spacing between plots.
* </p>
*
* @return int vertical spacing between plots.
*/
    public int getPlotVertSpacing() {
        return vSpacing;
    }

    /**
* <p>
* Sets the number of channels in the canvas settings to channelCount.
* </p>
*
* <p>
* This is a temporary hack for histogram overlays and should not be used
* regularly as it will be replaced once histogram overlays have made the
* transition.
* </p>
*
* @param channelCount
* int number of channel in the canvas settings.
* @deprecated The method is really a hack until histogram overlays get
* their own representation.
*/
    public void setChannelCount(int channelCount) {
        if (channelCount <= 0) {
            // If the number of channels is less than or equal to 0, then set
            // the number of channels to 0.
            this.channelCount = 0;
        } else {
            // Otherwise, the number of channels is greater than 0, so set the
            // number of channels to it.
            this.channelCount = channelCount;
        }

        /**
* Scale parameters
*/

        // Allocate the array of scale type flags
        scaleFlags = new int[this.channelCount];

        // Allocate the array of scale argument strings
        scaleArguments = new String[this.channelCount];

        // Allocate the array of channel minimums
        minimums = new double[this.channelCount];

        // Allocate the array of channel maximums
        maximums = new double[this.channelCount];

        // Loop through the channels
        for (int i = 0; i < this.channelCount; i++) {
            // Initialize the scale type flag to 1
            scaleFlags[i] = 1;

            // Initialize the channel minimum to Double.NaN
            minimums[i] = Double.NaN;

            // Initialize the channel maximum to Double.NaN
            maximums[i] = Double.NaN;
        }
    }

    /**
* Plot parameter methods
*/

    /**
* <p>
* Returns the x channel.
* </p>
*
* @return int x channel.
*/
    public int getXChannel() {
        return xChannel;
    }

    /**
* <p>
* Returns the y channel.
* </p>
*
* @return int y channel.
*/
    public int getYChannel() {
        return yChannel;
    }

    /**
* <p>
* Returns the constant flag of the type of the plot.
* </p>
*
* @return int constant flag of the type of the plot.
*/
    public int getPlotType() {
        return plotType;
    }

    /**
* <p>
* Returns whether to use white text on a black background.
* </p>
*
* @return boolean flag indicating whether to use white text on a black
* background.
*/
    public boolean useBlackBackground() {
        return blackBackgroundP;
    }

    /**
* <p>
* Sets whether to use white text on a black background.
* </p>
*
*/
    public void setBlackBackgroundP(boolean blackBackgroundP) {
        this.blackBackgroundP = blackBackgroundP;
    }

    /**
* <p>
* Returns whether to draw a plot with annotations.
* </p>
*
* @return boolean flag indicating whether to draw a plot with annotations.
*/
    public boolean drawAnnotation() {
        return annotationP;
    }

    /**
* <p>
* Returns whether to draw the scale labels.
* </p>
*
* @return boolean flag indicating whether to draw the scale labels.
*/
    public boolean drawScaleLabel() {
        return scaleLabelP;
    }

    /**
* <p>
* Returns whether to draw the scale ticks.
* </p>
*
* @return boolean flag indicating whether to draw the scale ticks.
*/
    public boolean drawScaleTick() {
        return scaleTickP;
    }

    /**
* <p>
* Returns whether to draw the axis labels.
* </p>
*
* @return boolean flag indicating whether to draw the axis labels.
*/
    public boolean drawAxisLabel() {
        return axisLabelP;
    }

    /**
* <p>
* Returns whether to use long labels.
* </p>
*
* @return boolean flag indicating whether to use long labels.
*/
    public boolean useLongLabel() {
        return longLabelP;
    }

    /**
* <p>
* Returns the smoothing level of the smoothing function to use.
* </p>
*
* @return double smoothing level of the smoothing function to use.
*/
    public double getSmoothing() {
        return smoothing;
    }

    /**
* <p>
* Returns the aspect ratio of the histogram.
* </p>
*
* @return double aspect ratio of the histogram.
*/
    public double getAspectRatio() {
        return aspectRatio;
    }

    /**
* <p>
* Returns the percentage in each level of the contour plot.
* </p>
*
* @return double percentage in each level of the contour plot.
*/
    public double getContourPercent() {
        return contourPercent;
    }

    /**
* <p>
* Returns the starting percentage of the contour plot.
* </p>
*
* @return double starting percentage of the contour plot.
*/
    public double getContourStartPercent() {
        return contourStartPercent;
    }

    /**
* Population type parameter methods
*/

    /**
* <p>
* Returns the constant flag of the type of the population.
* </p>
*
* @return int constant flag of the type of the population.
*/
    public int getPopulationType() {
        return populationType;
    }

    /**
* <p>
* Returns the number of events to get from the flow file.
* </p>
*
* @return int number of events to get from the flow file.
*/
    public int getEventCount() {
        return eventCount;
    }

    /**
* Scale parameter methods
*/

    /**
* <p>
* Returns the scale of the channel indicated by the channel index channel
* or null if the channel or the scale is invalid.
* </p>
*
* @param channel
* int index of the channel whose scale to return.
* @return <code>facs.scale.Scale</code> object to the scale of the channel
* indicated by the channel index channel or null if the channel or
* the scale is invalid.
*/
    public Scale getScale(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return null.
            return null;
        } else {
            // Otherwise, the index of the channel is valid, so return the scale
            // of the channel.
            return Scaling.getScale(scaleFlags[channel]);
        }
    }

    /**
* <p>
* Returns the scale type flag of the scale of the channel indicated by the
* channel index channel or 1 if the channel is invalid.
* </p>
*
* @param channel
* int index of the channel whose scale type flag to return.
* @return int scale type flag of the scale of the channel indicated by the
* channel index channel or 1 if the channel is invalid.
*/
    public int getScaleFlag(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return 1.
            return 1;
        } else {
            // Otherwise, the index of the channel is valid, so return the scale
            // type flag of the scale of the channel.
            return scaleFlags[channel];
        }
    }

    /**
* <p>
* Sets the scale type flag of the scale of the channel indicated by the
* channel index channel to flag.
* </p>
*
* @param channel
* int index of the channel whose scale type flag to set.
* @param flag
* int scale type flag of the scale of the channel indicated by
* the channel index channel.
*/
    public void setScaleFlag(int channel, int flag) {
        if ((channel >= 0) && (channel < channelCount)) {
            // If the index of the channel is valid, then set the scale type
            // flag of the scale of the channel to flag.
            scaleFlags[channel] = flag;
        }
    }

    /**
* <p>
* Returns the scale argument of the scale of the channel indicated by the
* channel index channel or null if the channel is invalid.
* </p>
*
* @param channel
* int index of the channel whose scale argument to return.
* @return <code>facs.scale.ScaleArgument</code> object to the scale
* argument of the scale of the channel indicated by the channel
* index channel or null if the channel is invalid.
*/
    public ScaleArgument getScaleArgument(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return null.
            return null;
        } else {
            // Otherwise, the index of the channel is valid, so return the scale
            // argument of the scale of the channel.
            return Scaling.getScaleArgument(scaleFlags[channel], scaleArguments[channel]);
        }
    }

    /**
* <p>
* Returns the scale argument string of the scale argument of the scale of
* the channel indicated by the channel index channel or null if the channel
* is invalid.
* </p>
*
* @param channel
* int index of the channel whose scale argument string to
* return.
* @return <code>String</code> scale argument string of the scale argument
* of the scale of the channel indicated by the channel index
* channel or null if the channel is invalid.
*/
    public String getScaleArgumentString(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return null.
            return null;
        } else {
            // Otherwise, the index of the channel is valid, so return the scale
            // argument string of the scale argument of the scale of the
            // channel.
            return scaleArguments[channel];
        }
    }

    /**
* <p>
* Sets the scale argument string of the scale argument of the scale of the
* channel indicated by the channel index channel to arg.
* </p>
*
* @param channel
* int index of the channel whose scale argument string to set.
* @param arg
* <cod>String</code> scale argument string of the scale argument
* of the scale of the channel indicated by the channel index
* channel.
*/
    public void setScaleArgumentString(int channel, String arg) {
        if ((channel >= 0) && (channel < channelCount)) {
            // If the index of the channel is valid, then set the scale argument
            // string of the scale argument of the scale of the channel to arg.
            scaleArguments[channel] = arg;
        }
    }

    /**
* <p>
* Returns the channel minimum of the channel indicated by the channel index
* channel or Double.NaN if the channel is invalid.
* </p>
*
* @param channel
* int index of the channel whose channel minimum to return.
* @return double channel minimum of the channel indicated by the channel
* index channel or Double.NaN if the channel is invalid.
*/
    public double getMinimum(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return Double.NaN.
            return Double.NaN;
        } else {
            // Otherwise, the index of the channel is valid, so return the
            // channel minimum of the channel.
            return minimums[channel];
        }
    }

    /**
* <p>
* Sets the channel minimum of the channel indicated by the channel index
* channel to min.
* </p>
*
* @param channel
* int index of the channel whose channel minimum to set.
* @param min
* double channel minimum of the channel indicated by the channel
* index channel.
*/
    public void setMinimum(int channel, double min) {
        if ((channel >= 0) && (channel < channelCount)) {
            // If the index of the channel is valid, then set the channel
            // minimum of the channel to min.
            minimums[channel] = min;
        }
    }

    /**
* <p>
* Returns the channel maximum of the channel indicated by the channel index
* channel or Double.NaN if the channel is invalid.
* </p>
*
* @param channel
* int index of the channel whose channel maximum to return.
* @return double channel maximum of the channel indicated by the channel
* index channel or Double.NaN if the channel is invalid.
*/
    public double getMaximum(int channel) {
        if ((channel < 0) || (channel >= channelCount)) {
            // If the index of the channel is invalid, then return Double.NaN.
            return Double.NaN;
        } else {
            // Otherwise, the index of the channel is valid, so return the
            // channel maximum of the channel.
            return maximums[channel];
        }
    }

    /**
* <p>
* Sets the channel maximum of the channel indicated by the channel index
* channel to max.
* </p>
*
* @param channel
* int index of the channel whose channel maximum to set.
* @param max
* double channel maximum of the channel indicated by the channel
* index channel.
*/
    public void setMaximum(int channel, double max) {
        if ((channel >= 0) && (channel < channelCount)) {
            // If the index of the channel is valid, then set the channel
            // maximum of the channel to max.
            maximums[channel] = max;
        }
    }


    /**
* <p>
* Sets the index value of the X Channel member
* </p>
*/
    public void setXChannel(int channel) {
        xChannel = channel;
    }

    /**
* <p>
* Sets the index value of the yChannel member
* </p>
*/
    public void setYChannel(int channel) {
        yChannel = channel;
    }



    /**
* Region parameter methods
*/

    /**
* <p>
* Adds the region in the <code>PlotRegion</code> object region to the
* canvas settings.
* </p>
*
* @param region
* <code>PlotRegion</code> object to the region to add to the
* canvas settings.
* @return true if the canvas settings changed as a result of the call.
*/
    public boolean addRegion(PlotRegion region) {
        if (region == null) {
            // If the region is null, then quit.
            return false;
        }

        // Add the region to the list of regions
        return regions.add(region);
    }

    /**
* <p>
* Return the array of regions in the canvas settings.
* </p>
*
* @return <code>PlotRegion</code> array of regions in the canvas settings.
*/
    public PlotRegion[] getRegions() {
        // Allocate an array to hold all the regions
        PlotRegion[] regionArray = new PlotRegion[regions.size()];

        // Copy the list of regions into the array of regions
        regions.toArray(regionArray);

        // Return the array of regions
        return regionArray;
    }

    /**
* Color methods --- The following methods return the appropriate color to
* used based on the canvas settings, which is based on the corresponding
* parameter.
*/

    /**
* <p>
* Returns the color with which the text should be rendered.
* </p>
*
* @return <code>java.awt.Color</code> object to the color with which the
* text should be rendered.
*/
    public Color getTextColor() {
        if (blackBackgroundP) {
            // If the plot should be white text on a black background, then
            // return Color.white.
            return Color.white;
        } else {
            // Otherwise, the plot should not be white text on a black
            // background, so return Color.black.
            return Color.black;
        }
    }

    /**
* <p>
* Returns the color with which the lines should be rendered.
* </p>
*
* @return <code>java.awt.Color</code> object to the color with which the
* lines should be rendered.
*/
    public Color getLineColor() {
        if (blackBackgroundP) {
            // If the plot should be white text on a black background, then
            // return Color.white.
            return Color.white;
        } else {
            // Otherwise, the plot should not be white text on a black
            // background, so return Color.black.
            return Color.black;
        }
    }

    /**
* <p>
* Returns the color with which the outliers should be rendered.
* </p>
*
* @return <code>java.awt.Color</code> object to the color with which the
* outliers should be rendered.
*/
    public Color getOutlierColor() {
        if (getPlotBackgroundColor() == Color.black) {
            // If the plot should be white outliers on a black background, then
            // return Color.white.
            return Color.white;
        } else {
            // Otherwise, the plot should not be white outliers on a black
            // background, so return Color.black.
            return Color.black;
        }
    }

    /**
* <p>
* Returns the color of the background.
* </p>
*
* @return <code>java.awt.Color</code> object to the color of the
* background.
*/
    public Color getBackgroundColor() {
        if (blackBackgroundP) {
            // If the plot should be white text on a black background, then
            // return Color.black.
            return Color.black;
        } else {
            // Otherwise, the plot should not be white text on a black
            // background, so return Color.white.
            return Color.white;
        }
    }

    /**
* <p>
* Returns the color of the plot background, the area behind the events.
* </p>
*
* @return <code>java.awt.Color</code> object to the color of the
* background.
*/
    public Color getPlotBackgroundColor() {
        if (blackBackgroundP) {
            // If the plot should be white text on a black background, then
            // return Color.white.
            return Color.black;
        } else {
            // Otherwise, the plot should not be white text on a black
            // background, so return Color.black.
            return Color.white;
        }
    }

    /**
* <p>
* Returns the color of unshaded contours.
* </p>
*
* @return <code>java.awt.Color</code> object to the color of standard
* (unshaded) contours.
*/
    public Color getContourColor() {
        if (blackBackgroundP) {
            // If the plot should be white text on a black background, then
            // return Color.white.
            return Color.black;
        } else {
            // Otherwise, the plot should not be white text on a black
            // background, so return Color.black.
            return Color.white;
        }
    }

    /**
* Position calculation methods --- The following methods calculate the
* positions based on the padding and the row and column indices.
*
* I believe this could save quite a bit of heap room by calculating and
* consuming the values immediately instead of storing them for the
* long-run.
*
* Especially considering how often people seem to like to rewrite these
* methods. RTFM!!! Oh right, there is no manual until I write it. Argh.
*/

    /**
* <p>
* Returns the left x-coordinate of the first plot, that is, the plot in
* position (0, 0).
* </p>
*
* @return int left x-coordinate of the first plot.
*/
    public int getStartX() {
        return getLeftX(0, 0);
    }

    /**
* <p>
* Returns the left x-coordinate of the plot for the plot in position (row,
* column).
* </p>
*
* <p>
* Syntactic sugar for the getLeftX method.
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int left x-coordinate of the plot.
*/
    public int getStartX(int row, int column) {
        return getLeftX(row, column);
    }

    /**
* <p>
* Returns the left x-coordinate of the plot for the plot in position (row,
* column).
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int left x-coordinate of the plot.
*/
    public int getLeftX(int row, int column) {
        // The left x-coordinate is calculated by traversing from the left of
        // the canvas, right by column number of plots, and then right by
        // leftPad.
        return (canvasLeftPad + ((leftPad + plotWidth + rightPad + hSpacing) * column) + leftPad);
    }

    /**
* <p>
* Returns the right x-coordinate of the plot for the plot in position (row,
* column).
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int right x-coordinate of the plot.
*/
    public int getRightX(int row, int column) {
        return (getLeftX(row, column) + plotWidth);
    }

    /**
* <p>
* Returns the top y-coordinate of the first plot, that is, the plot in
* position (0, 0).
* </p>
*
* @return int top y-coordinate of the first plot.
*/
    public int getStartY() {
        return getTopY(0, 0);
    }

    /**
* <p>
* Returns the top y-coordinate of the plot for the plot in position (row,
* column).
* </p>
*
* <p>
* Syntactic sugar for the getTopY method.
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int top y-coordinate of the plot.
*/
    public int getStartY(int row, int column) {
        return getTopY(row, column);
    }

    /**
* <p>
* Returns the top y-coordinate of the plot for the plot in position (row,
* column).
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int top y-coordinate of the plot.
*/
    public int getTopY(int row, int column) {
        if (plotType == Representation.HISTOGRAM_OVERLAY) {
            // If the plot type is a histogram overlay, then return the special
            // calculation for the top y-coordinate of the plot.
            return (canvasTopPad + topPad + (vSpacing * row));
        } else {
            // Otherwise, the plot type is not a histogram overlay, so calculate
            // the top y-coordinate of the plot.
            // The top y-coordinate is calculated by traversing from the top of
            // the canvas, down by row number of plots, and then down by topPad.
            return (canvasTopPad + ((topPad + plotHeight + bottomPad + vSpacing) * row) + topPad);
        }
    }

    /**
* <p>
* Returns the bottom y-coordinate of the plot for the plot in position
* (row, column).
* </p>
*
* @param row
* int row index of the plot.
* @param column
* int column index of the plot.
* @return int bottom y-coordinate of the plot.
*/
    public int getBottomY(int row, int column) {
        return (getTopY(row, column) + plotHeight);
    }

    /**
* Factory methods
*/

    /**
* <p>
* Returns a canvas settings created based on the input parameters.
* </p>
*
* @param hSpacing
* int horizontal spacing between plots
* @param vSpacing
* int vertical spacing between plots.
* @param channelCount
* int number of channels in the canvas settings.
* @param xChannel
* int x channel.
* @param yChannel
* int y channel.
* @param plotType
* int constant flag of the type of the plot.
* @param colorSet
* int constant flag of the color set.
* @param blackBackgroundP
* boolean flag indicating whether to use white text on a black
* background.
* @param annotationP
* boolean flag indicating whether to draw a plot with
* annotations.
* @param scaleLabelP
* boolean flag indicating whether to draw the scale labels.
* @param scaleTickP
* boolean flag indicating whether to draw the scale ticks.
* @param axisLabelP
* boolean flag indicating whether to draw the axis labels.
* @param longLabelP
* boolean flag indicating whether to use long labels.
* @param axisBins
* int number of axis bins on each axis.
* @param smoothing
* double smoothing level of the smoothing function to use.
* @param aspectRatio
* double aspect ratio of the histogram.
* @param contourPercent
* double percentage in each level of the contour plot.
* @param contourStartPercent
* double starting percentage of the contour plot.
* @param populationType
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
* @return <code>CanvasSettings</code> object to the representation created
* based on the input parameters.
*/
    public static CanvasSettings getCanvasSettings(int hSpacing, int vSpacing, int channelCount, int xChannel, int yChannel, int plotType, int colorSet,
            boolean blackBackgroundP, boolean annotationP, boolean scaleLabelP, boolean scaleTickP, boolean axisLabelP, boolean longLabelP, int axisBins,
            double smoothing, double aspectRatio, double contourPercent, double contourStartPercent, int populationType, int eventCount, int dotSize) {

        // Create the CanvasSettings object and return it
        return new CanvasSettings(axisBins, axisBins, hSpacing, vSpacing, channelCount, xChannel, yChannel, plotType, colorSet, blackBackgroundP, annotationP,
                scaleLabelP, scaleTickP, axisLabelP, longLabelP, axisBins, smoothing, aspectRatio, contourPercent, contourStartPercent, populationType,
                eventCount, dotSize);
    }

    /**
* <p>
* Returns a canvas settings created based on the representation in the
* <code>Representation</code> object representation or null if the canvas
* settings cannot be created.
* </p>
*
* @param representation
* <code>Representation</code> object to the representation.
* @return <code>CanvasSettings</code> to the canvas settings created based
* on the representation in the <code>Representation</code> object
* representation or null if the canvas settings cannot be created.
*/
    public static CanvasSettings getCanvasSettings(Representation representation) {
        return getCanvasSettings(representation, 0, 0);
    }

    /**
* <p>
* Returns a canvas settings created based on the representation in the
* <code>Representation</code> object representation with the horizontal
* spacing between plots set to hSpacing and the vertical spacing between
* plots set to vSpacing or null if the canvas settings cannot be created.
* </p>
*
* @param representation
* <code>Representation</code> object to the representation.
* @param hSpacing
* int horizontal spacing between plots
* @param vSpacing
* int vertical spacing between plots.
* @return <code>CanvasSettings</code> to the canvas settings created based
* on the representation in the <code>Representation</code> object
* representation or null if the canvas settings cannot be created.
*/
    public static CanvasSettings getCanvasSettings(Representation representation, int hSpacing, int vSpacing) {
        if (representation == null) {
            // If the representation is null, then quit.
            return null;
        }

        // Create the canvas settings
        CanvasSettings cs = CanvasSettings.getCanvasSettings(hSpacing, vSpacing, representation.getChannelCount(), representation.getXChannel(), representation
                .getYChannel(), representation.getPlotType(), representation.getColorSet(), representation.useBlackBackground(), representation
                .drawAnnotation(), representation.drawScaleLabel(), representation.drawScaleTick(), representation.drawAxisLabel(), representation
                .useLongLabel(), representation.getAxisBins(), representation.getSmoothing(), representation.getAspectRatio(), representation
                .getContourPercent(), representation.getContourStartPercent(), representation.getPopulationType(), representation.getEventCount(), representation.getDotSize());

        /**
* Set the scale parameters
*/

        // Get the number of channels in the representation
        int channelCount = representation.getChannelCount();

        // Loop through the channels
        for (int i = 0; i < channelCount; i++) {
            cs.setScaleFlag(i, representation.getScaleFlag(i));
            cs.setScaleArgumentString(i, representation.getScaleArgumentString(i));
            cs.setMinimum(i, representation.getMinimum(i));
            cs.setMaximum(i, representation.getMaximum(i));
        }

        // Return the canvas settings
        return cs;
    }

    /**
* <p>
* Returns a canvas settings created based on the illustration in the
* <code>Illustration</code> object illustration or null if the canvas
* settings cannot be created.
* </p>
*
* @param illustration
* <code>Illustration</code> object to the illustration.
* @return <code>CanvasSettings</code> to the canvas settings created based
* on the illustration in the <code>Illustration</code> object
* illustration or null if the canvas settings cannot be created.
*/
    public static CanvasSettings getCanvasSettings(Illustration illustration) {
        return getCanvasSettings(illustration, 0, 0);
    }

    /**
* <p>
* Returns a canvas settings created based on the illustration in the
* <code>Illustration</code> object illustration with the horizontal spacing
* between plots set to hSpacing and the vertical spacing between plots set
* to vSpacing or null if the canvas settings cannot be created.
* </p>
*
* @param illustration
* <code>Illustration</code> object to the illustration.
* @param hSpacing
* int horizontal spacing between plots
* @param vSpacing
* int vertical spacing between plots.
* @return <code>CanvasSettings</code> to the canvas settings created based
* on the illustration in the <code>Illustration</code> object
* illustration or null if the canvas settings cannot be created.
*/
    public static CanvasSettings getCanvasSettings(Illustration illustration, int hSpacing, int vSpacing) {
        if (illustration == null) {
            // If the illustration is null, then quit.
            return null;
        }

        // Create the canvas settings
        CanvasSettings cs = CanvasSettings.getCanvasSettings(hSpacing, vSpacing, illustration.getChannelCount(), illustration.getXChannel(), illustration
                .getYChannel(), illustration.getPlotType(), illustration.getColorSet(), illustration.useBlackBackground(), illustration.drawAnnotation(),
                illustration.drawScaleLabel(), illustration.drawScaleTick(), illustration.drawAxisLabel(), illustration.useLongLabel(), illustration
                        .getAxisBins(), illustration.getSmoothing(), illustration.getAspectRatio(), illustration.getContourPercent(), illustration
                        .getContourStartPercent(), illustration.getPopulationType(), illustration.getEventCount(), illustration.getDotSize());

        /**
* Set the scale parameters
*/

        // Get the number of channels in the illustration
        int channelCount = illustration.getChannelCount();

        // Loop through the channels
        for (int i = 0; i < channelCount; i++) {
            cs.setScaleFlag(i, illustration.getScaleFlag(i));
            cs.setScaleArgumentString(i, illustration.getScaleArgumentString(i));
            cs.setMinimum(i, illustration.getMinimum(i));
            cs.setMaximum(i, illustration.getMaximum(i));
        }

        // Return the canvas settings
        return cs;
    }

    /**
* Static helper methods
*/

    /**
* <p>
* Returns a 256 pixel by 256 pixel test pattern of the default 2D color
* set.
* </p>
*
* @return <code>BufferedImage</code> object to the 256 pixel by 256 pixel
* test pattern of the default 2D color set.
*/
    public static BufferedImage getTestPattern() {
        return getTestPattern(256, 256, DEFAULT_COLOR_SET_2D);
    }

    /**
* <p>
* Returns the width by height test pattern of the colors in the
* <code>java.awt.Color</code> array of colors colorSet.
* </p>
*
* <p>
* The colors are drawn as rectangles that span the width of the test
* pattern.
* </p>
*
* @param width
* int width of the test pattern.
* @param height
* int height of the test pattern.
* @param colorSet
* <code>java.awt.Color</code> array of colors to use to draw the
* test pattern.
* @return <code>BufferedImage</code> object to the width by height test
* pattern of the colors in the <code>java.awt.Color</code> array of
* colors colorSet.
*/
    public static BufferedImage getTestPattern(int width, int height, Color[] colorSet) {
        if ((width <= 0) || (height <= 0)) {
            // If the width or the height of the test pattern is less than or
            // equal to 0, then return an empty image.
            return new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
        }

        // Create a buffered image in which we will draw the plot
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get the Graphics object to the buffered image
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background color to white
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        /**
* Draw the color set
*/

        if ((colorSet != null) && (colorSet.length > 0)) {
            // If the array of colors is not null and not empty, then draw the
            // test pattern.

            // Divide the height by the number of colors to show
            int multiplier = height / colorSet.length;

            // Loop through the array of colors drawing each as a bar
            for (int i = 0; i < colorSet.length; i++) {
                // Set the color to the current color in the array of colors
                g.setColor(colorSet[i]);

                // Draw a bar with the current color
                g.fillRect(0, i * multiplier, width, multiplier);
            }
        }

        // Dispose of the graphics
        g.dispose();

        // Return the buffered image
        return image;
    }
}

