/**
* PlotUtilities.java
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

// Import the Abstract Window Toolkit package for drawing
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;

// Import the utility package
import java.util.*;

// Import the scale package
import facs.scale.*;


/**
* <p>
* A collection of public static helper methods for use in plotting.
* </p>
*
* <p>
* In particular, the methods draw the axes and render the plot regions.
* </p>
*
* <p>
* The class includes a number of convenience methods for drawing scale ticks
* and scale labels in an attempt to abstract as much of the actual drawing as
* possible from the drawing of the axes. This was done so that as scales are
* added, it would not be too hard to add a new scale for them.
* </p>
*/
final class PlotUtilities {
    /**
* The length of a major scale tick
*/
    private static final int MAJOR_TICK_LENGTH = 7;

    /**
* The length of a minor scale tick
*/
    private static final int MINOR_TICK_LENGTH = 4;

    /**
* The width of a color when drawing the scale of the color gradient
*/
    private static final int COLOR_WIDTH = 2;

    /**
* The height of a color when drawing the scale of the color gradient
*/
    private static final int COLOR_HEIGHT = 20;

    /**
* The number of levels of the color gradient to draw
*/
    private static final int COLOR_GRADIENT_LEVEL_COUNT = 50;

    /**
* The width of the gradient based on the bucket count and width
*/
    private static final double COLOR_GRADIENT_WIDTH = COLOR_GRADIENT_LEVEL_COUNT * COLOR_WIDTH - 1;

    /**
* The color of a region that is a negative compensation gate
*/
    private static final Color NEG_GATE_FILL_COLOR = new Color(1.0f, 0.25f, 0.25f, 0.1f);

    /**
* The color of a region that is a positive compensation gate
*/
    private static final Color POS_GATE_FILL_COLOR = new Color(0.25f, 1.0f, 0.25f, 0.1f);

    /**
* <p>
* A private constructor to suppress the default constructor so the class
* cannot be instantiated.
* </p>
*/
    private PlotUtilities() {
    }

    /**
* <p>
* Draws the x-axis using the graphics in the <code>Graphics2D</code> object
* g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param axisLabel
* <code>String</code> label of the x-axis.
* @param scale
* <code>Scale</code> object to the scale of the x-axis.
* @param scaleArgument
* <code>ScaleArgument</code> object to the scale argument of the
* x-axis.
* @param min
* double channel minimum of the x-axis.
* @param max
* double channel maximum of the x-axis.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
*/
    public static void drawXAxis(Graphics2D g, String axisLabel, Scale scale, ScaleArgument scaleArgument, double min, double max, CanvasSettings cs) {
        if ((g == null) || (scale == null) || (cs == null) || Double.isNaN(min) || Double.isNaN(max)) {
            // If the graphics, the scale, or the canvas settings is null or the
            // channel minimum or the channel maximum is not a number, then
            // quit.
            return;
        }

        if (!cs.drawAnnotation()) {
            // If the plot should not be drawn with annotations, then quit.
            return;
        }

        // Get whether to draw the scale labels
        boolean drawLabelsP = cs.drawScaleLabel();

        // Get whether to draw the scale ticks
        boolean drawTicksP = cs.drawScaleTick();

        // Get the width of the plot
        int plotWidth = cs.getPlotWidth();

        // Get the height of the plot
        int plotHeight = cs.getPlotHeight();

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        /**
* Draw the x-axis label
*/

        if (cs.drawAxisLabel() && (axisLabel != null) && (axisLabel.length() > 0)) {
            // If the axis labels are drawn and the axis label is not null or
            // not empty, then draw the x-axis label.
            if (plotWidth < 128) {
                // If the width of the plot is less than 200 pixels, then use
                // the small font.
                g.setFont(CanvasSettings.ARIAL_FONT);
            } else {
                // Otherwise, the width of the plot is greater than 200 pixels,
                // then use the normal font.

                // Set the font size for this critical label
                g.setFont(CanvasSettings.CRITICAL_LABEL_FONT);
            }

            // Get the font metrics of the current font
            FontMetrics metrics = g.getFontMetrics();

            // Get the height of the current font
            int fontHeight = metrics.getHeight();

            // Get the width of the label in the current font
            int fontWidth = metrics.stringWidth(axisLabel);
            /*
* // Initialize the x-axis label offset to the height of the font
* int axisLabelOffset = fontHeight;
*
* if(drawLabelsP || drawTicksP) { // If the scale labels or the
* scale ticks are drawn, then add some offset for the scale ticks.
* axisLabelOffset += CanvasSettings.STROKE_LENGTH + 2;
*
* if(drawLabelsP) { // If the scale labels are drawn, then add some
* offset for the scale labels. axisLabelOffset += 12; } }
*
* // Draw the x-axis label g.drawString(axisLabel, (plotWidth -
* fontWidth) / 2, plotHeight + axisLabelOffset);
*/

            /*
* Debug lines for testing calculations
*
* g.drawString("CH: "+cs.getCanvasHeight(), 10, 10);
* g.drawString("Offset: "+cs.getPlotVertSpacing(), 10, 25);
* g.drawString("PlotHeight: "+cs.getPlotHeight(), 10, 40);
* g.drawString("Left Pad: "+cs.getLeftPad(), 10, 55);
* g.drawString("Right Pad: "+cs.getRightPad(), 10, 70);
* g.drawString("Top Pad: "+cs.getTopPad(), 10, 85);
* g.drawString("Bottom Pad: "+cs.getBottomPad(), 10, 100);
* g.drawString("Canvas Top Pad: "+cs.getCanvasTopPad(), 10, 115);
* g.drawString("Canvas Bottom Pad: "+cs.getCanvasBottomPad(), 10,
* 130); g.drawString("Canvas Left Pad: "+cs.getCanvasLeftPad(), 10,
* 145); g.drawString("Canvas Right Pad: "+cs.getCanvasRightPad(),
* 10, 160);
*/

            // Draw the x-axis label as close as possible to the edge of the
            // plot (4 pixels from the edge, to be exact)
            g.drawString(axisLabel, (plotWidth - fontWidth) / 2, plotHeight + cs.getBottomPad() + CanvasSettings.BORDER_SIZE - 4);
        }

        /**
* Draw the x-axis scale
*/

        if (min > max) {
            // If the channel minimum of the x-axis is greater than the channel
            // maximum of the x-axis, then quit.
            return;
        }

        if (drawLabelsP || drawTicksP) {
            // If the scale labels or the scale ticks are drawn, then draw the
            // x-axis scale.

            // Set the font of the scale labels to the tiny font
            g.setFont(CanvasSettings.ARIAL_FONT_TINY);

            // Get whether to show the scale labels in power notation
            boolean powerNotationP = (!cs.useLongLabel());

            /**
* Calculate the scale factor used to bin based on the range of the
* channel (Assumes plotWidth is equal to the number of bins)
*/
            double scaledMin, scaleFactor;

            if (scaleArgument == null) {
                // If the scale argument is null, then use the version of
                // getValue() without the scale argument.
                scaledMin = scale.getValue(min);
                scaleFactor = (double) plotWidth / (scale.getValue(max) - scaledMin);
            } else {
                // Otherwise, the scale argument is not null, so use the version
                // of getValue() with the scale argument.
                scaledMin = scale.getValue(min, scaleArgument);
                scaleFactor = (double) plotWidth / (scale.getValue(max, scaleArgument) - scaledMin);
            }

            // Calculate the y-coordinate of the scale ticks
            int scaleY = plotHeight + CanvasSettings.BORDER_SIZE;

            // Calculate the y-coordinate of the scale labels
            int labelY = scaleY + MAJOR_TICK_LENGTH;

            // Set the maximum bin as the last bin
            int maxBin = plotWidth - 1;

            double value, base;
            int bin;

            if (powerNotationP || (scale instanceof LnScale) || (scale instanceof LogScale)) {
                // If the scale labels should be shown in power notation or the
                // scale of the x-axis is a variant of the logarithm scale, then
                // draw the log axis.
                if (max > 0.0d) {
                    // If the channel maximum is positive, then draw the
                    // positive log axis.

                    /**
* Draw the positive log axis
*/

                    // Initialize the starting decade to 0
                    int startDecade = 0;

                    if (min > 1.0d) {
                        // If the channel minimum is greater than 1.0d, then
                        // find the largest decade that is below the channel
                        // minimum.
                        startDecade = (int) Math.floor(Math.log10(min));

                        if (startDecade < 0) {
                            // If the largest decade that is below the channel
                            // minimum is less than 0, then set the starting
                            // decade to 0.
                            startDecade = 0;
                        }
                    }

                    // Find the smallest decade that is above the channel
                    // maximum
                    int endDecade = (int) Math.ceil(Math.log10(max));

                    // Loop through the decades
                    for (int i = startDecade; i <= endDecade; i++) {
                        // Calculate the base corresponding to the current
                        // decade
                        base = getDecade(i);

                        if (base > max) {
                            // If the base is greater than the channel maximum,
                            // then break the loop.
                            break;
                        }

                        // Loop through the ticks for the current decade
                        for (int j = 1; j < 10; j++) {
                            // Calculate the value corresponding to the current
                            // tick
                            value = base * (double) j;

                            if (value < min) {
                                // If the value is less than the channel
                                // minimum, then skip the current decade.
                                continue;
                            } else if (value > max) {
                                // If the value is greater than the channel
                                // maximum, then break the loop.
                                break;
                            }

                            /**
* Calculate the bin value
*/

                            if (scaleArgument == null) {
                                // If the scale argument is null, then use the
                                // version of getValue() without the scale
                                // argument.
                                bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                            } else {
                                // Otherwise, the scale argument is not null, so
                                // use the version of getValue() with the scale
                                // argument.
                                bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                            }

                            if (bin < 0) {
                                // If the calculated bin is less than the first
                                // bin, then set the bin to the first bin.
                                bin = 0;
                            } else if (bin > maxBin) {
                                // If the calculated bin is greater than the
                                // maximum bin, then set the bin to the maximum
                                // bin.
                                bin = maxBin;
                            }

                            if (j == 1) {
                                // If the current scale tick is the first in the
                                // decade, then draw a major scale tick.
                                drawXMajorScaleTick(g, bin, scaleY);

                                if (drawLabelsP) {
                                    // If the scale labels are drawn, then draw
                                    // the scale label.
                                    if (powerNotationP) {
                                        // If the scale labels should be shown
                                        // in power notation, then draw the
                                        // current value as a power of 10 as the
                                        // scale label.
                                        drawXPowerScaleLabel(g, 10, i, bin, labelY);
                                    } else {
                                        // Otherwise, simply draw the current
                                        // value as the scale label.
                                        drawXScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), bin, labelY);
                                    }
                                }
                            } else if (i < endDecade) {
                                // If the current decade is not the last decade,
                                // then draw a minor scale tick.
                                drawXMinorScaleTick(g, bin, scaleY);
                            }
                        }
                    }
                }

                if (min < 0.0d) {
                    // If the channel minimum is negative, then draw the
                    // negative log axis.

                    /**
* Draw the negative log axis
*/

                    // Initialize the starting decade to 0
                    int startDecade = 0;

                    if (max < -1.0d) {
                        // If the channel maximum is less than -1.0d, then find
                        // the smallest decade that is above the channel
                        // maximum.
                        startDecade = (int) Math.floor(Math.log10(Math.abs(max)));

                        if (startDecade < 0) {
                            // If the smallest decade that is above the channel
                            // maximum is less than 0, then set the starting
                            // decade to 0.
                            startDecade = 0;
                        }
                    }

                    // Find the largest decade that is below the channel minimum
                    int endDecade = (int) Math.ceil(Math.log10(Math.abs(min)));

                    // Loop through the decades
                    for (int i = startDecade; i <= endDecade; i++) {
                        // Calculate the base corresponding to the current
                        // decade
                        base = getDecade(i) * -1.0d;

                        if (base < min) {
                            // If the base is less than the channel minimum,
                            // then break the loop.
                            break;
                        }

                        // Loop through the ticks for the current decade
                        for (int j = 1; j < 10; j++) {
                            // Calculate the value corresponding to the current
                            // tick
                            value = base * (double) j;

                            if (value > max) {
                                // If the value is greater than the channel
                                // maximum, then skip the current decade.
                                continue;
                            } else if (value < min) {
                                // If the value is less than the channel
                                // minimum, then break the loop.
                                break;
                            }

                            /**
* Calculate the bin value
*/

                            if (scaleArgument == null) {
                                // If the scale argument is null, then use the
                                // version of getValue() without the scale
                                // argument.
                                bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                            } else {
                                // Otherwise, the scale argument is not null, so
                                // use the version of getValue() with the scale
                                // argument.
                                bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                            }

                            if (bin < 0) {
                                // If the calculated bin is less than the first
                                // bin, then set the bin to the first bin.
                                bin = 0;
                            } else if (bin > maxBin) {
                                // If the calculated bin is greater than the
                                // maximum bin, then set the bin to the maximum
                                // bin.
                                bin = maxBin;
                            }

                            if (j == 1) {
                                // If the current scale tick is the first in the
                                // decade, then draw a major scale tick.
                                drawXMajorScaleTick(g, bin, scaleY);

                                if (drawLabelsP) {
                                    // If the scale labels are drawn, then draw
                                    // the scale label.
                                    if (powerNotationP) {
                                        // If the scale labels should be shown
                                        // in power notation, then draw the
                                        // current value as a power of 10 as the
                                        // scale label.
                                        drawXPowerScaleLabel(g, -10, i, bin, labelY);
                                    } else {
                                        // Otherwise, simply draw the current
                                        // value as the scale label.
                                        drawXScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), bin, labelY);
                                    }
                                }
                            } else if (i < endDecade) {
                                // If the current decade is not the last decade,
                                // then draw a minor scale tick.
                                drawXMinorScaleTick(g, bin, scaleY);
                            }
                        }
                    }
                }
            } else {
                // Otherwise, the scale labels are not in power notation, so
                // draw a number of somewhat linear scale ticks.

                // Calculate the range
                double range = max - min;

                // Set the number of scale ticks to divide the range
                int numTicks = 8;

                // Loop through the number of scale ticks
                for (int i = 0; i <= numTicks; i++) {
                    // Calculate the value corresponding to the current scale
                    // tick
                    value = min + (double) i / (double) numTicks * range;

                    /**
* Calculate the bin value
*/

                    if (scaleArgument == null) {
                        // If the scale argument is null, then use the version
                        // of getValue() without the scale argument.
                        bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                    } else {
                        // Otherwise, the scale argument is not null, so use the
                        // version of getValue() with the scale argument.
                        bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                    }

                    if (bin < 0) {
                        // If the calculated bin is less than the first bin,
                        // then set the bin to the first bin.
                        bin = 0;
                    } else if (bin > maxBin) {
                        // If the calculated bin is greater than the maximum
                        // bin, then set the bin to the maximum bin.
                        bin = maxBin;
                    }

                    if ((i % 2) == 1) {
                        // If the current scale tick is an odd scale tick, then
                        // draw a minor scale tick.
                        drawXMinorScaleTick(g, bin, scaleY);
                    } else {
                        // Otherwise, the current scale tick is an even scale
                        // tick, so draw a major scale tick.
                        drawXMajorScaleTick(g, bin, scaleY);

                        if (drawLabelsP) {
                            // If the scale labels are drawn, then draw the
                            // scale label.
                            drawXScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), bin, labelY);
                        }
                    }
                }
            }
        }
    }

    /**
* <p>
* Draws the y-axis using the graphics in the <code>Graphics2D</code> object
* g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param axisLabel
* <code>String</code> label of the y-axis.
* @param scale
* <code>Scale</code> object to the scale of the y-axis.
* @param scaleArgument
* <code>ScaleArgument</code> object to the scale argument of the
* y-axis.
* @param min
* double channel minimum of the y-axis.
* @param max
* double channel maximum of the y-axis.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
*/
    public static void drawYAxis(Graphics2D g, String axisLabel, Scale scale, ScaleArgument scaleArgument, double min, double max, CanvasSettings cs) {
        if ((g == null) || (scale == null) || (cs == null) || Double.isNaN(min) || Double.isNaN(max)) {
            // If the graphics, the scale, or the canvas settings is null or the
            // channel minimum or the channel maximum is not a number, then
            // quit.
            return;
        }

        if (!cs.drawAnnotation()) {
            // If the plot should not be drawn with annotations, then quit.
            return;
        }

        // Get whether to draw the scale labels
        boolean drawLabelsP = cs.drawScaleLabel();

        // Get whether to draw the scale ticks
        boolean drawTicksP = cs.drawScaleTick();

        // Get the width of the plot
        int plotWidth = cs.getPlotWidth();

        // Get the height of the plot
        int plotHeight = cs.getPlotHeight();

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        /**
* Draw the y-axis label
*/

        if (cs.drawAxisLabel() && (axisLabel != null) && (axisLabel.length() > 0)) {
            // If the axis labels are drawn and the axis label is not null or
            // not empty, then draw the x-axis label.
            if (plotWidth < 128) {
                // If the width of the plot is less than 200 pixels, then use
                // the small font.
                g.setFont(CanvasSettings.ARIAL_FONT);
            } else {
                // Otherwise, the width of the plot is greater than 200 pixels,
                // then use the normal font.

                // Set the font size for this critical label
                g.setFont(CanvasSettings.CRITICAL_LABEL_FONT);
            }

            // Get the font metrics of the current font
            FontMetrics metrics = g.getFontMetrics();

            // Get the height of the current font
            int fontHeight = metrics.getHeight();

            // Get the width of the label in the current font
            int fontWidth = metrics.stringWidth(axisLabel);
            /*
* // Initialize the x-axis label offset to 0 int axisLabelOffset =
* 0;
*
* if(drawLabelsP || drawTicksP) { // If the scale labels or the
* scale ticks are drawn, then add some offset for the scale ticks.
* axisLabelOffset -= CanvasSettings.STROKE_LENGTH - 2;
*
* if(drawLabelsP) { // If the scale labels are drawn, then add some
* offset for the scale labels. axisLabelOffset -= 14; } }
*/
            // Rotate the axes by 90 degrees
            g.rotate(-Math.PI / 2);

            // Draw the y-axis label
            // g.drawString(axisLabel, -(plotHeight + fontWidth) / 2,
            // axisLabelOffset);

            // Draw the y-axis label almost in the middle of the leftPadding, w/
            // 2 pixels extra space between it and the left edge
            g.drawString(axisLabel, -(plotHeight + fontWidth) / 2, -cs.getLeftPad() + (fontHeight / 2) + 2);

            // Rotate the axes back
            g.rotate(Math.PI / 2);
        }

        /**
* Draw the y-axis scale
*/

        if (min > max) {
            // If the channel minimum of the x-axis is greater than the channel
            // maximum of the x-axis, then quit.
            return;
        }

        if (drawLabelsP || drawTicksP) {
            // If the scale labels or the scale ticks are drawn, then draw the
            // x-axis scale.

            // Set the font of the scale labels to the tiny font
            g.setFont(CanvasSettings.ARIAL_FONT_TINY);

            // Get whether to show the scale labels in power notation
            boolean powerNotationP = (!cs.useLongLabel());

            /**
* Calculate the scale factor used to bin based on the range of the
* channel (Assumes plotHeight is equal to the number of bins)
*/
            double scaledMin, scaleFactor;

            if (scaleArgument == null) {
                // If the scale argument is null, then use the version of
                // getValue() without the scale argument.
                scaledMin = scale.getValue(min);
                scaleFactor = (double) plotHeight / (scale.getValue(max) - scaledMin);
            } else {
                // Otherwise, the scale argument is not null, so use the version
                // of getValue() with the scale argument.
                scaledMin = scale.getValue(min, scaleArgument);
                scaleFactor = (double) plotHeight / (scale.getValue(max, scaleArgument) - scaledMin);
            }

            // Calculate the x-coordinate of the scale ticks
            int scaleX = -CanvasSettings.BORDER_SIZE;

            // Calculate the x-coordinate of the scale labels
            int labelX = scaleX - MAJOR_TICK_LENGTH - 4;

            // Set the maximum bin as the last bin
            int maxBin = plotHeight - 1;

            // Calculate the y-coordinate of the first bin (the height of the
            // plot minus 1)
            int height = plotHeight - 1;

            double value, base;
            int bin, y;

            if (powerNotationP || (scale instanceof LnScale) || (scale instanceof LogScale)) {
                // If the scale labels should be shown in power notation or the
                // scale of the x-axis is a variant of the logarithm scale, then
                // draw the log axis.
                if (max > 0.0d) {
                    // If the channel maximum is positive, then draw the
                    // positive log axis.

                    /**
* Draw the positive log axis
*/

                    // Initialize the starting decade to 0
                    int startDecade = 0;

                    if (min > 1.0d) {
                        // If the channel minimum is greater than 1.0d, then
                        // find the largest decade that is below the channel
                        // minimum.
                        startDecade = (int) Math.floor(Math.log10(min));

                        if (startDecade < 0) {
                            // If the largest decade that is below the channel
                            // minimum is less than 0, then set the starting
                            // decade to 0.
                            startDecade = 0;
                        }
                    }

                    // Find the smallest decade that is above the channel
                    // maximum
                    int endDecade = (int) Math.ceil(Math.log10(max));

                    // Loop through the decades
                    for (int i = startDecade; i <= endDecade; i++) {
                        // Calculate the base corresponding to the current
                        // decade
                        base = getDecade(i);

                        if (base > max) {
                            // If the base is greater than the channel maximum,
                            // then break the loop.
                            break;
                        }

                        // Loop through the ticks for the current decade
                        for (int j = 1; j < 10; j++) {
                            // Calculate the value corresponding to the current
                            // tick
                            value = base * (double) j;

                            if (value < min) {
                                // If the value is less than the channel
                                // minimum, then skip the current decade.
                                continue;
                            } else if (value > max) {
                                // If the value is greater than the channel
                                // maximum, then break the loop.
                                break;
                            }

                            /**
* Calculate the bin value
*/

                            if (scaleArgument == null) {
                                // If the scale argument is null, then use the
                                // version of getValue() without the scale
                                // argument.
                                bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                            } else {
                                // Otherwise, the scale argument is not null, so
                                // use the version of getValue() with the scale
                                // argument.
                                bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                            }

                            if (bin < 0) {
                                // If the calculated bin is less than the first
                                // bin, then set the bin to the first bin.
                                bin = 0;
                            } else if (bin > maxBin) {
                                // If the calculated bin is greater than the
                                // maximum bin, then set the bin to the maximum
                                // bin.
                                bin = maxBin;
                            }

                            // Calculate the y-coordinate of the scale tick and
                            // the scale label using the current bin value
                            y = height - bin;

                            if (j == 1) {
                                // If the current scale tick is the first in the
                                // decade, then draw a major scale tick.
                                drawYMajorScaleTick(g, scaleX, y);

                                if (drawLabelsP) {
                                    // If the scale labels are drawn, then draw
                                    // the scale label.
                                    if (powerNotationP) {
                                        // If the scale labels should be shown
                                        // in power notation, then draw the
                                        // current value as a power of 10 as the
                                        // scale label.
                                        drawYPowerScaleLabel(g, 10, i, labelX, y);
                                    } else {
                                        // Otherwise, simply draw the current
                                        // value as the scale label.
                                        drawYScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), labelX, y);
                                    }
                                }
                            } else if (i < endDecade) {
                                // If the current decade is not the last decade,
                                // then draw a minor scale tick.
                                drawYMinorScaleTick(g, scaleX, y);
                            }
                        }
                    }
                }

                if (min < 0.0d) {
                    // If the channel minimum is negative, then draw the
                    // negative log axis.

                    /**
* Draw the negative log axis
*/

                    // Initialize the starting decade to 0
                    int startDecade = 0;

                    if (max < -1.0d) {
                        // If the channel maximum is less than -1.0d, then find
                        // the smallest decade that is above the channel
                        // maximum.
                        startDecade = (int) Math.floor(Math.log10(Math.abs(max)));

                        if (startDecade < 0) {
                            // If the smallest decade that is above the channel
                            // maximum is less than 0, then set the starting
                            // decade to 0.
                            startDecade = 0;
                        }
                    }

                    // Find the largest decade that is below the channel minimum
                    int endDecade = (int) Math.ceil(Math.log10(Math.abs(min)));

                    // Loop through the decades
                    for (int i = startDecade; i <= endDecade; i++) {
                        // Calculate the base corresponding to the current
                        // decade
                        base = getDecade(i) * -1.0d;

                        if (base < min) {
                            // If the base is less than the channel minimum,
                            // then break the loop.
                            break;
                        }

                        // Loop through the ticks for the current decade
                        for (int j = 1; j < 10; j++) {
                            // Calculate the value corresponding to the current
                            // tick
                            value = base * (double) j;

                            if (value > max) {
                                // If the value is greater than the channel
                                // maximum, then skip the current decade.
                                continue;
                            } else if (value < min) {
                                // If the value is less than the channel
                                // minimum, then break the loop.
                                break;
                            }

                            /**
* Calculate the bin value
*/

                            if (scaleArgument == null) {
                                // If the scale argument is null, then use the
                                // version of getValue() without the scale
                                // argument.
                                bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                            } else {
                                // Otherwise, the scale argument is not null, so
                                // use the version of getValue() with the scale
                                // argument.
                                bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                            }

                            if (bin < 0) {
                                // If the calculated bin is less than the first
                                // bin, then set the bin to the first bin.
                                bin = 0;
                            } else if (bin > maxBin) {
                                // If the calculated bin is greater than the
                                // maximum bin, then set the bin to the maximum
                                // bin.
                                bin = maxBin;
                            }

                            // Calculate the y-coordinate of the scale tick and
                            // the scale label using the current bin value
                            y = height - bin;

                            if (j == 1) {
                                // If the current scale tick is the first in the
                                // decade, then draw a major scale tick.
                                drawYMajorScaleTick(g, scaleX, y);

                                if (drawLabelsP) {
                                    // If the scale labels are drawn, then draw
                                    // the scale label.
                                    if (powerNotationP) {
                                        // If the scale labels should be shown
                                        // in power notation, then draw the
                                        // current value as a power of 10 as the
                                        // scale label.
                                        drawYPowerScaleLabel(g, 10, i, labelX, y);
                                    } else {
                                        // Otherwise, simply draw the current
                                        // value as the scale label.
                                        drawYScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), labelX, y);
                                    }
                                }
                            } else if (i < endDecade) {
                                // If the current decade is not the last decade,
                                // then draw a minor scale tick.
                                drawYMinorScaleTick(g, scaleX, y);
                            }
                        }
                    }
                }
            } else {
                // Otherwise, the scale labels are not in power notation, so
                // draw a number of somewhat linear scale ticks.

                // Calculate the range
                double range = max - min;

                // Set the number of scale ticks to divide the range
                int numTicks = 8;

                // Loop through the number of scale ticks
                for (int i = 0; i <= numTicks; i++) {
                    // Calculate the value corresponding to the current scale
                    // tick
                    value = min + (double) i / (double) numTicks * range;

                    /**
* Calculate the bin value
*/

                    if (scaleArgument == null) {
                        // If the scale argument is null, then use the version
                        // of getValue() without the scale argument.
                        bin = (int) ((scale.getValue(value) - scaledMin) * scaleFactor);
                    } else {
                        // Otherwise, the scale argument is not null, so use the
                        // version of getValue() with the scale argument.
                        bin = (int) ((scale.getValue(value, scaleArgument) - scaledMin) * scaleFactor);
                    }

                    if (bin < 0) {
                        // If the calculated bin is less than the first bin,
                        // then set the bin to the first bin.
                        bin = 0;
                    } else if (bin > maxBin) {
                        // If the calculated bin is greater than the maximum
                        // bin, then set the bin to the maximum bin.
                        bin = maxBin;
                    }

                    // Calculate the y-coordinate of the scale tick and the
                    // scale label using the current bin value
                    y = height - bin;

                    if ((i % 2) == 1) {
                        // If the current scale tick is an odd scale tick, then
                        // draw a minor scale tick.
                        drawYMinorScaleTick(g, scaleX, y);
                    } else {
                        // Otherwise, the current scale tick is an even scale
                        // tick, so draw a major scale tick.
                        drawYMajorScaleTick(g, scaleX, y);

                        if (drawLabelsP) {
                            // If the scale labels are drawn, then draw the
                            // scale label.
                            drawYScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), labelX, y);
                        }
                    }
                }
            }
        }
    }

    /**
* <p>
* Draws the pseudo-z-axis using the graphics in the <code>Graphics2D</code>
* object g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param axisLabel
* <code>String</code> label of the z-axis.
* @param scale
* <code>Scale</code> object to the scale of the z-axis.
* @param scaleArgument
* <code>ScaleArgument</code> object to the scale argument of the
* z-axis.
* @param min
* double channel minimum of the z-axis.
* @param max
* double channel maximum of the z-axis.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
*/
    public static void drawPseudoZAxis(Graphics2D g, String axisLabel, Scale scale, ScaleArgument scaleArgument, double min, double max, CanvasSettings cs) {
        if ((g == null) || (scale == null) || (cs == null) || Double.isNaN(min) || Double.isNaN(max)) {
            // If the graphics, the scale, or the canvas settings is null or the
            // channel minimum or the channel maximum is not a number, then
            // quit.
            return;
        }

        if (!cs.drawAnnotation()) {
            // If the plot should not be drawn with annotations, then quit.
            return;
        }

        // Get whether to draw the scale labels
        boolean drawLabelsP = cs.drawScaleLabel();

        // Get whether to draw the scale ticks
        boolean drawTicksP = cs.drawScaleTick();

        // Get the width of the plot
        int plotWidth = cs.getPlotWidth();

        // Get the height of the plot
        int plotHeight = cs.getPlotHeight();

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        /**
* Draw the y-axis label
*/

        if (cs.drawAxisLabel() && (axisLabel != null) && (axisLabel.length() > 0)) {
            // If the axis labels are drawn and the axis label is not null or
            // not empty, then draw the x-axis label.
            if (plotWidth < 128) {
                // If the width of the plot is less than 200 pixels, then use
                // the small font.
                g.setFont(CanvasSettings.ARIAL_FONT);
            } else {
                // Otherwise, the width of the plot is greater than 200 pixels,
                // then use the normal font.

                // Set the font size for this critical label
                g.setFont(CanvasSettings.CRITICAL_LABEL_FONT);
            }

            // Get the font metrics of the current font
            FontMetrics metrics = g.getFontMetrics();

            // Get the height of the current font
            int fontHeight = metrics.getHeight();

            String zLabel = "z: ";
            if (cs.getPlotType() == Illustration.THIRD_AXIS_MEDIAN_PLOT) {
                zLabel += "MFI ";
            } else if (cs.getPlotType() == Illustration.THIRD_AXIS_NINETYFIFTH_PLOT) {
                zLabel += "95th ";
            }
            zLabel += axisLabel;

            // Get the width of the label in the current font
            int fontWidth = metrics.stringWidth(zLabel);

            // Draw the z-axis label at the top of the plot, centered
            g.drawString(zLabel, (plotWidth / 2) - (fontWidth / 2), -CanvasSettings.PSEUDO_Z_HEIGHT / 2);
        }

        /**
* Draw the a-axis scale
*/

        /*
* Commented out for now
*
*
*
* if(min > max) { // If the channel minimum of the x-axis is greater
* than the channel maximum of the x-axis, then quit. return; }
*
* if(drawLabelsP || drawTicksP) { // If the scale labels or the scale
* ticks are drawn, then draw the axis scale.
*
* // Set the font of the scale labels to the tiny font
* g.setFont(CanvasSettings.ARIAL_FONT_TINY);
*
* // Get whether to show the scale labels in power notation boolean
* powerNotationP = (!cs.useLongLabel());
*
*
* // Calculate the scale factor used to bin based on the range of the
* channel // (Assumes plotHeight is equal to the number of bins) double
* scaledMin, scaleFactor;
*
* if(scaleArgument == null) { // If the scale argument is null, then
* use the version of getValue() without the scale argument. scaledMin =
* scale.getValue(min); scaleFactor = (double)plotHeight /
* (scale.getValue(max) - scaledMin); } else { // Otherwise, the scale
* argument is not null, so use the version of getValue() with the scale
* argument. scaledMin = scale.getValue(min, scaleArgument); scaleFactor
* = (double)plotHeight / (scale.getValue(max, scaleArgument) -
* scaledMin); }
*
*
* // Calculate the x-coordinate of the scale ticks int scaleX =
* -CanvasSettings.BORDER_SIZE;
*
* // Calculate the x-coordinate of the scale labels int labelX = scaleX
* - MAJOR_TICK_LENGTH - 4;
*
* // Set the maximum bin as the last bin int maxBin = plotHeight - 1;
*
* // Calculate the y-coordinate of the first bin (the height of the
* plot minus 1) int height = plotHeight - 1;
*
* double value, base; int bin, y;
*
* if(powerNotationP || (scale instanceof LnScale) || (scale instanceof
* LogScale)) { // If the scale labels should be shown in power notation
* or the scale of the x-axis is a variant of the logarithm scale, then
* draw the log axis. if(max > 0.0d) { // If the channel maximum is
* positive, then draw the positive log axis.
*
*
* // Draw the positive log axis
*
* // Initialize the starting decade to 0 int startDecade = 0;
*
* if(min > 1.0d) { // If the channel minimum is greater than 1.0d, then
* find the largest decade that is below the channel minimum.
* startDecade = (int)Math.floor(Math.log10(min));
*
* if(startDecade < 0) { // If the largest decade that is below the
* channel minimum is less than 0, then set the starting decade to 0.
* startDecade = 0; } }
*
* // Find the smallest decade that is above the channel maximum int
* endDecade = (int)Math.ceil(Math.log10(max));
*
* // Loop through the decades for(int i = startDecade; i <= endDecade;
* i++) { // Calculate the base corresponding to the current decade base
* = getDecade(i);
*
* if(base > max) { // If the base is greater than the channel maximum,
* then break the loop. break; }
*
* // Loop through the ticks for the current decade for(int j = 1; j <
* 10; j++) { // Calculate the value corresponding to the current tick
* value = base (double)j;
*
* if(value < min) { // If the value is less than the channel minimum,
* then skip the current decade. continue; } else if(value > max) { //
* If the value is greater than the channel maximum, then break the
* loop. break; }
*
*
* // Calculate the bin value
*
* if(scaleArgument == null) { // If the scale argument is null, then
* use the version of getValue() without the scale argument. bin =
* (int)((scale.getValue(value) - scaledMin) scaleFactor); } else { //
* Otherwise, the scale argument is not null, so use the version of
* getValue() with the scale argument. bin =
* (int)((scale.getValue(value, scaleArgument) - scaledMin)
* scaleFactor); }
*
* if(bin < 0) { // If the calculated bin is less than the first bin,
* then set the bin to the first bin. bin = 0; } else if(bin > maxBin) {
* // If the calculated bin is greater than the maximum bin, then set
* the bin to the maximum bin. bin = maxBin; }
*
*
* // Calculate the y-coordinate of the scale tick and the scale label
* using the current bin value y = height - bin;
*
* if(j == 1) { // If the current scale tick is the first in the decade,
* then draw a major scale tick. drawYMajorScaleTick(g, scaleX, y);
*
* if(drawLabelsP) { // If the scale labels are drawn, then draw the
* scale label. if(powerNotationP) { // If the scale labels should be
* shown in power notation, then draw the current value as a power of 10
* as the scale label. drawYPowerScaleLabel(g, 10, i, labelX, y); } else
* { // Otherwise, simply draw the current value as the scale label.
* drawYScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), labelX,
* y); } } } else if(i < endDecade) { // If the current decade is not
* the last decade, then draw a minor scale tick. drawYMinorScaleTick(g,
* scaleX, y); } } } }
*
* if(min < 0.0d) { // If the channel minimum is negative, then draw the
* negative log axis.
*
*
* // Draw the negative log axis
*
* // Initialize the starting decade to 0 int startDecade = 0;
*
* if(max < -1.0d) { // If the channel maximum is less than -1.0d, then
* find the smallest decade that is above the channel maximum.
* startDecade = (int)Math.floor(Math.log10(Math.abs(max)));
*
* if(startDecade < 0) { // If the smallest decade that is above the
* channel maximum is less than 0, then set the starting decade to 0.
* startDecade = 0; } }
*
* // Find the largest decade that is below the channel minimum int
* endDecade = (int)Math.ceil(Math.log10(Math.abs(min)));
*
* // Loop through the decades for(int i = startDecade; i <= endDecade;
* i++) { // Calculate the base corresponding to the current decade base
* = getDecade(i) -1.0d;
*
* if(base < min) { // If the base is less than the channel minimum,
* then break the loop. break; }
*
* // Loop through the ticks for the current decade for(int j = 1; j <
* 10; j++) { // Calculate the value corresponding to the current tick
* value = base (double)j;
*
* if(value > max) { // If the value is greater than the channel
* maximum, then skip the current decade. continue; } else if(value <
* min) { // If the value is less than the channel minimum, then break
* the loop. break; }
*
*
* // Calculate the bin value
*
* if(scaleArgument == null) { // If the scale argument is null, then
* use the version of getValue() without the scale argument. bin =
* (int)((scale.getValue(value) - scaledMin) scaleFactor); } else { //
* Otherwise, the scale argument is not null, so use the version of
* getValue() with the scale argument. bin =
* (int)((scale.getValue(value, scaleArgument) - scaledMin)
* scaleFactor); }
*
* if(bin < 0) { // If the calculated bin is less than the first bin,
* then set the bin to the first bin. bin = 0; } else if(bin > maxBin) {
* // If the calculated bin is greater than the maximum bin, then set
* the bin to the maximum bin. bin = maxBin; }
*
*
* // Calculate the y-coordinate of the scale tick and the scale label
* using the current bin value y = height - bin;
*
* if(j == 1) { // If the current scale tick is the first in the decade,
* then draw a major scale tick. drawYMajorScaleTick(g, scaleX, y);
*
* if(drawLabelsP) { // If the scale labels are drawn, then draw the
* scale label. if(powerNotationP) { // If the scale labels should be
* shown in power notation, then draw the current value as a power of 10
* as the scale label. drawYPowerScaleLabel(g, 10, i, labelX, y); } else
* { // Otherwise, simply draw the current value as the scale label.
* drawYScaleLabel(g, CanvasSettings.INT_FORMAT.format(value), labelX,
* y); } } } else if(i < endDecade) { // If the current decade is not
* the last decade, then draw a minor scale tick. drawYMinorScaleTick(g,
* scaleX, y); } } } } } else { // Otherwise, the scale labels are not
* in power notation, so draw a number of somewhat linear scale ticks.
*
* // Calculate the range double range = max - min;
*
* // Set the number of scale ticks to divide the range int numTicks =
* 8;
*
* // Loop through the number of scale ticks for(int i = 0; i <=
* numTicks; i++) { // Calculate the value corresponding to the current
* scale tick value = min + (double)i / (double)numTicks range;
*
*
* // Calculate the bin value
*
* if(scaleArgument == null) { // If the scale argument is null, then
* use the version of getValue() without the scale argument. bin =
* (int)((scale.getValue(value) - scaledMin) scaleFactor); } else { //
* Otherwise, the scale argument is not null, so use the version of
* getValue() with the scale argument. bin =
* (int)((scale.getValue(value, scaleArgument) - scaledMin)
* scaleFactor); }
*
* if(bin < 0) { // If the calculated bin is less than the first bin,
* then set the bin to the first bin. bin = 0; } else if(bin > maxBin) {
* // If the calculated bin is greater than the maximum bin, then set
* the bin to the maximum bin. bin = maxBin; }
*
*
* // Calculate the y-coordinate of the scale tick and the scale label
* using the current bin value y = height - bin;
*
* if((i % 2) == 1) { // If the current scale tick is an odd scale tick,
* then draw a minor scale tick. drawYMinorScaleTick(g, scaleX, y); }
* else { // Otherwise, the current scale tick is an even scale tick, so
* draw a major scale tick. drawYMajorScaleTick(g, scaleX, y);
*
* if(drawLabelsP) { // If the scale labels are drawn, then draw the
* scale label. drawYScaleLabel(g,
* CanvasSettings.INT_FORMAT.format(value), labelX, y); } } } } }
*/
    }

    /**
* <p>
* Draws a count axis for the x-axis using the graphics in the
* <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param max
* int maximum count on the count axis.
* @param maxX
* int x-coordinate of the maximum count.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
*/
    public static void drawXCountAxis(Graphics2D g, int max, int maxX, CanvasSettings cs) {
        if ((g == null) || (cs == null) || (!cs.drawAnnotation())) {
            // If the graphics or the canvas settings is null or the plot should
            // not be drawn with annotations, then quit.
            return;
        }

        // Get the width of the plot
        int plotWidth = cs.getPlotWidth();

        // Get the height of the plot
        int plotHeight = cs.getPlotHeight();

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        /**
* Draw the "Counts" label
*/

        // Set the label to "Counts"
        String label = "Counts";

        if (cs.drawAxisLabel()) {
            // If the axis labels are drawn, then draw the "Counts" label.
            if (plotWidth < 200) {
                // If the width of the plot is less than 200 pixels, then use
                // the small font.
                g.setFont(CanvasSettings.ARIAL_FONT_SMALL);
            } else {
                // Otherwise, the width of the plot is greater than 200 pixels,
                // then use the normal font.
                g.setFont(CanvasSettings.ARIAL_FONT);
            }

            // Get the font metrics of the current font
            FontMetrics metrics = g.getFontMetrics();

            // Get the width of the label in the current font
            int fontWidth = metrics.stringWidth(label);

            // Draw the "Counts" label as close as possible to the edge of the
            // plot (4 pixels from the edge, to be exact)
            g.drawString(label, (plotWidth - fontWidth) / 2, plotHeight + cs.getBottomPad() - 4);
        }

        /**
* Draw the "Counts" scale
*/

        if (cs.drawScaleTick()) {
            // If the scale ticks are drawn, then draw the scale ticks for 0 and
            // the max.

            // Calculate the y-coordinate of the scale ticks
            int scaleY = plotHeight + CanvasSettings.BORDER_SIZE;

            // Draw the major scale tick for 0
            drawXMajorScaleTick(g, 0, scaleY);

            // Draw the major scale tick for max
            drawXMajorScaleTick(g, maxX, scaleY);
        }

        if (cs.drawScaleLabel()) {
            // If the scale labels are drawn, then draw the scale labels for 0
            // and the max.

            // Set the font of the scale labels to the tiny font
            g.setFont(CanvasSettings.ARIAL_FONT_TINY);

            // Calculate the y-coordinate of the scale labels
            int labelY = plotHeight + CanvasSettings.BORDER_SIZE + MAJOR_TICK_LENGTH;

            // Draw the scale label for 0
            drawXScaleLabel(g, Integer.toString(0), 0, labelY);

            // Draw the scale label for max
            drawXScaleLabel(g, Integer.toString(max), maxX, labelY);
        }
    }

    /**
* <p>
* Draws a count axis for the y-axis using the graphics in the
* <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param max
* int maximum count on the count axis.
* @param maxY
* int y-coordinate of the maximum count.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
*/
    public static void drawYCountAxis(Graphics2D g, int max, int maxY, CanvasSettings cs) {
        if ((g == null) || (cs == null) || (!cs.drawAnnotation())) {
            // If the graphics or the canvas settings is null or the plot should
            // not be drawn with annotations, then quit.
            return;
        }

        // Get the width of the plot
        int plotWidth = cs.getPlotWidth();

        // Get the height of the plot
        int plotHeight = cs.getPlotHeight();

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        /**
* Draw the "Counts" label
*/

        // Set the label to "Counts"
        String label = "Counts";

        if (cs.drawAxisLabel()) {
            // If the axis labels are drawn, then draw the "Counts" label.
            if (plotWidth < 200) {
                // If the width of the plot is less than 200 pixels, then use
                // the small font.
                g.setFont(CanvasSettings.ARIAL_FONT_SMALL);
            } else {
                // Otherwise, the width of the plot is greater than 200 pixels,
                // then use the normal font.
                g.setFont(CanvasSettings.ARIAL_FONT);
            }

            // Get the font metrics of the current font
            FontMetrics metrics = g.getFontMetrics();

            // Get the height of the current font
            int fontHeight = metrics.getHeight();

            // Get the width of the label in the current font
            int fontWidth = metrics.stringWidth(label);

            // Rotate the axes by 90 degrees
            g.rotate(-Math.PI / 2);

            // Draw the "Counts" label as close as possible to the edge of the
            // plot (4 pixels from the edge, to be exact)
            g.drawString(label, -(plotHeight + fontWidth) / 2, -cs.getLeftPad() + fontHeight + 4);

            // Rotate the axes back
            g.rotate(Math.PI / 2);
        }

        /**
* Draw the "Counts" scale
*/

        if (cs.drawScaleTick()) {
            // If the scale ticks are drawn, then draw the scale ticks for 0 and
            // the max.

            // Calculate the x-coordinate of the scale ticks
            int scaleX = -CanvasSettings.BORDER_SIZE;

            // Draw the major scale tick for 0
            drawYMajorScaleTick(g, scaleX, plotHeight);

            // Draw the major scale tick for max
            drawYMajorScaleTick(g, scaleX, maxY);
        }

        if (cs.drawScaleLabel()) {
            // If the scale labels are drawn, then draw the scale labels for 0
            // and the max.

            // Set the font of the scale labels to the tiny font
            g.setFont(CanvasSettings.ARIAL_FONT_TINY);

            // Calculate the x-coordinate of the scale labels
            int labelX = -MAJOR_TICK_LENGTH - CanvasSettings.BORDER_SIZE - 4;

            // Draw the scale label for 0
            drawYScaleLabel(g, Integer.toString(0), labelX, plotHeight);

            // Draw the scale label for max
            drawYScaleLabel(g, Integer.toString(max), labelX, maxY);
        }
    }

    /**
* <p>
* Returns the value of 10 taken to the corresponding power in power.
* </p>
*
* @param power
* int power to which to take the value of 10.
* @return double value of 10 taken to the corresponding power in power.
*/
    public static double getDecade(int power) {
        if (power == 0) {
            // If the power is equal to 0, then return 1.0d.
            return 1.0d;
        } else {
            // Otherwise, the power is not equal to 0, so calculate the value of
            // 10 taken to the power.

            // Initialize the decade to 1.0d
            double decade = 1.0d;

            if (power < 0) {
                // If the power is less than 0, then divide the decade by 10.0d
                // Math.abs(power) number of times.

                // Get the absolute value of the power
                power = Math.abs(power);

                // Divide the decade by 10.0d Math.abs(power) number of times
                for (int i = 0; i < power; i++) {
                    decade /= 10.0d;
                }
            } else {
                // Otherwise, the power is greater than 0, so multiply the
                // decade by 10.0d power number of times.
                for (int i = 0; i < power; i++) {
                    decade *= 10.0d;
                }
            }

            // Return the decade
            return decade;
        }
    }

    /**
* Scale tick methods
*/

    /**
* <p>
* Draws a major scale tick for the x-axis from (x, y) down using the
* graphics in the <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param x
* int starting x-coordinate of the scale tick.
* @param y
* int starting y-coordinate of the scale tick.
*/
    private static void drawXMajorScaleTick(Graphics g, int x, int y) {
        drawScaleTick(g, x, y, false, MAJOR_TICK_LENGTH);
    }

    /**
* <p>
* Draws a major scale tick for the y-axis from (x, y) to the left using the
* graphics in the <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param x
* int starting x-coordinate of the scale tick.
* @param y
* int starting y-coordinate of the scale tick.
*/
    private static void drawYMajorScaleTick(Graphics g, int x, int y) {
        drawScaleTick(g, x, y, true, MAJOR_TICK_LENGTH);
    }

    /**
* <p>
* Draws a minor scale tick for the x-axis from (x, y) down using the
* graphics in the <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param x
* int starting x-coordinate of the scale tick.
* @param y
* int starting y-coordinate of the scale tick.
*/
    private static void drawXMinorScaleTick(Graphics g, int x, int y) {
        drawScaleTick(g, x, y, false, MINOR_TICK_LENGTH);
    }

    /**
* <p>
* Draws a minor scale tick for the y-axis from (x, y) to the left using the
* graphics in the <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param x
* int starting x-coordinate of the scale tick.
* @param y
* int starting y-coordinate of the scale tick.
*/
    private static void drawYMinorScaleTick(Graphics g, int x, int y) {
        drawScaleTick(g, x, y, true, MINOR_TICK_LENGTH);
    }

    /**
* <p>
* Draws a scale tick of length length from (x, y) to the left if
* horizontalP is true or down if horizontalP is false using the graphics in
* the <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param x
* int starting x-coordinate of the scale tick.
* @param y
* int starting y-coordinate of the scale tick.
* @param yAxisP
* boolean flag indicating whether the scale tick is for the
* y-axis.
* @param length
* int length of the scale tick.
*/
    private static void drawScaleTick(Graphics g, int x, int y, boolean yAxisP, int length) {
        if (g == null) {
            // If the graphics is null, then quit.
            return;
        }

        if (yAxisP) {
            // If the scale tick is for the y-axis, then draw a horizontal scale
            // tick extending to the left from (x, y).
            g.drawLine(x, y, x - length, y);
        } else {
            // Otherwise, the scale tick is for the x-axis, so draw a vertical
            // scale tick extending down from (x, y).
            g.drawLine(x, y, x, y + length);
        }
    }

    /**
* Scale label methods
*/

    /**
* <p>
* Draws the scale label in the <code>String</code> label centered around
* the x-coordinate x and with the top at the y-coordinate y using the
* graphics in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param label
* <code>String</code> label of the scale label.
* @param x
* int center x-coordinate of the scale label.
* @param y
* int top y-coordinate of the scale label.
*/
    private static void drawXScaleLabel(Graphics g, String label, int x, int y) {
        drawScaleLabel(g, label, x, y, false);
    }

    /**
* <p>
* Draws the scale label in the <code>String</code> label centered around
* the y-coordinate y and with the right at the x-coordinate x using the
* graphics in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param label
* <code>String</code> label of the scale label.
* @param x
* int right x-coordinate of the scale label.
* @param y
* int center y-coordinate of the scale label.
*/
    private static void drawYScaleLabel(Graphics g, String label, int x, int y) {
        drawScaleLabel(g, label, x, y, true);
    }

    /**
* <p>
* Draws the scale label in the <code>String</code> label using the graphics
* in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param label
* <code>String</code> label of the scale label.
* @param x
* int x-coordinate of the scale label.
* @param y
* int y-coordinate of the scale label.
* @param yAxisP
* boolean flag indicating whether the scale label is for the
* y-axis.
*/
    private static void drawScaleLabel(Graphics g, String label, int x, int y, boolean yAxisP) {
        if ((g == null) || (label == null) || (label.length() <= 0)) {
            // If the graphics is null or the label is null or empty, then quit.
            return;
        }

        // Get the font metrics of the current font
        FontMetrics metrics = g.getFontMetrics();

        // Get the height of the current font
        int fontHeight = metrics.getHeight();

        // Get the width of the label in the current font
        int fontWidth = metrics.stringWidth(label);

        if (yAxisP) {
            // If the scale label is for the y-axis, then draw the label
            // centered around the y-coordinate with the right at the
            // x-coordinate.
            g.drawString(label, x - fontWidth, y + (fontHeight / 2));
        } else {
            // Otherwise, the scale label is for the x-axis, so draw the label
            // centered around the x-coordinate with the top at the
            // y-coordinate.
            g.drawString(label, x - (fontWidth / 2), y + fontHeight);
        }
    }

    /**
* <p>
* Draws a scale label of the form of the base base take to the power of
* power with the power displayed as a superscript of the base centered
* around the x-coordinate x and with the top at the y-coordinate y using
* the graphics in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param base
* int base.
* @param power
* int power.
* @param x
* int center x-coordinate of the scale label.
* @param y
* int top y-coordinate of the scale label.
*/
    private static void drawXPowerScaleLabel(Graphics g, int base, int power, int x, int y) {
        drawPowerScaleLabel(g, base, power, x, y, false);
    }

    /**
* <p>
* Draws a scale label of the form of the base base take to the power of
* power with the power displayed as a superscript of the base centered
* around the y-coordinate y and with the right at the x-coordinate x using
* the graphics in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param base
* int base.
* @param power
* int power.
* @param x
* int right x-coordinate of the scale label.
* @param y
* int center y-coordinate of the scale label.
*/
    private static void drawYPowerScaleLabel(Graphics g, int base, int power, int x, int y) {
        drawPowerScaleLabel(g, base, power, x, y, true);
    }

    /**
* <p>
* Draws a scale label of the form of the base base take to the power of
* power with the power displayed as a superscript of the base using the
* graphics in the <code>Graphics</code> object g.
* </p>
*
* <p>
* The scale label is drawn using the current font in the graphics in the
* <code>Graphics</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics</code> object to the graphics.
* @param base
* int base.
* @param power
* int power.
* @param x
* int x-coordinate of the scale label.
* @param y
* int y-coordinate of the scale label.
* @param yAxisP
* boolean flag indicating whether the scale label is for the
* y-axis.
*/
    private static void drawPowerScaleLabel(Graphics g, int base, int power, int x, int y, boolean yAxisP) {
        if (g == null) {
            // If the graphics is null, then quit.
            return;
        }

        // Get the string for the base
        String baseString = Integer.toString(base);

        // Get the string for the power
        String powerString = Integer.toString(power);

        // Get the font metrics of the current font
        FontMetrics metrics = g.getFontMetrics();

        // Get the height of the current font
        int fontHeight = metrics.getHeight();

        // Get the width of the base string in the current font
        int baseWidth = metrics.stringWidth(baseString);

        // Get the width of the power string in the current font
        int powerWidth = metrics.stringWidth(powerString);

        if (yAxisP) {
            // If the scale label is for the y-axis, then draw the base and
            // power strings centered around the y-coordinate with the right at
            // the x-coordinate.
            g.drawString(baseString, x - (baseWidth + powerWidth), y + (int) ((double) fontHeight * 0.75d));
            g.drawString(powerString, x - powerWidth, y + (int) ((double) fontHeight * 0.25d));
        } else {
            // Otherwise, the scale label is for the x-axis, so draw the base
            // and power strings centered around the x-coordinate with the top
            // at the y-coordinate
            g.drawString(baseString, x - ((baseWidth + powerWidth) / 2), y + fontHeight + fontHeight / 2);
            g.drawString(powerString, x - ((baseWidth + powerWidth) / 2) + baseWidth, y + fontHeight);
        }
    }

    /**
* Region methods
*/

    /**
* <p>
* Draws the regions in the canvas settings in the
* <code>CanvasSettings</code> object cs using the graphics in the
* <code>Graphics2D</code> object g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param pop
* <code>Population</code> object to the population.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
* @throws IOException
*/
//    public static void drawRegions(Graphics2D g, Population pop, CanvasSettings cs) throws IOException {
//        if ((g == null) || (pop == null) || (cs == null)) {
//            // If the graphics, the population, or the canvas settings is null,
//            // then quit.
//            return;
//        }
//
//        if (!cs.drawAnnotation()) {
//            // If the plot should not be drawn with annotations, then quit.
//            return;
//        }
//
//        // Get the array of regions from the canvas settings
//        PlotRegion[] regions = cs.getRegions();
//
//        if ((regions == null) || (regions.length <= 0)) {
//            // If the array of regions is null or empty, then quit.
//            return;
//        }
//
//        /**
//         * Draw the regions
//         */
//
//        // Translate the origin to the upper left corner of the canvas
//        g.translate(-cs.getStartX(), -cs.getStartY());
//
//        int id;
//
//        // Loop through the array of regions
//        for (int i = 0; i < regions.length; i++) {
//            // Get the ID of the current region
//            id = regions[i].getID();
//
//            if (regions[i].isShown() && regions[i].isDrawn()) {
//                // If the current region is shown and drawn, then check if the
//                // ID of the current region is valid.
//                if ((id == Representation.ALL_REGIONS) || (id == Representation.GATE_SET)) {
//                    // If the ID of the current region corresponds to the
//                    // universal region or the active gate set region, then draw
//                    // the appropriate gates.
//
//                    // Get the x channel of the plot
//                    int xChannel = cs.getXChannel();
//
//                    // Get the y channel of the plot
//                    int yChannel = cs.getYChannel();
//
//                    if (id == Representation.ALL_REGIONS) {
//                        // If the ID of the current region corresponds to the
//                        // universal region, then get the array of gates for the
//                        // population on the current channels.
//                        gates = Gate.filter(exp.getGates(filename), xChannel, yChannel);
//                    } else if (id == Representation.GATE_SET) {
//                        // If the ID of the current region corresponds to the
//                        // active gate set region, then get the array of active
//                        // gates of the population on the current channels.
//                        gates = Gate.filter(pop.getGates(), xChannel, yChannel);
//                    }
//
//                } else if (id == Representation.UNGATED) {
//                    // If the ID of the current region corresponds to the
//                    // ungated region, then do nothing.
//                } else if (id > 0) {
//                    // If the ID of the current region is a valid gate ID, then
//                    // draw the region defined by the gate.
//
//                    // Get the gate corresponding to the ID of the current
//                    // region
//                    gate = gateMap.get(Integer.valueOf(id));
//
//                    // Draw the current region
//                    drawRegion(g, gate, regions[i], cs, pop);
//                }
//            }
//        }
//
//        // Translate the origin back to the upper left corner of the first plot
//        g.translate(cs.getStartX(), cs.getStartY());
//    }

    /**
* <p>
* Draws the region in the plot defined by the gate in the <code>Gate</code>
* object gate and described by the region in the <code>PlotRegion</code>
* object region using the graphics in the <code>java.awt.Graphics2D</code>
* object g.
* </p>
*
* @param g
* <code>java.awt.Graphics2D</code> object to the graphics.
* @param gate
* <code>Gate</code> object to the gate defining the region in
* the plot.
* @param region
* <code>PlotRegion</code> object to the region describing the
* region in the plot.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings.
* @param pop
* <code>Population</code> object to the population on which the
* gate is gating.
* @throws IOException
*/
//    private static void drawRegion(Graphics2D g, Gate gate, PlotRegion region, CanvasSettings cs, Population pop) throws IOException {
//        if ((g == null) || (gate == null) || (region == null) || (cs == null) || (gate instanceof Quadrant) || (gate instanceof Split) || (!region.isShown())
//                || (!region.isDrawn())) {
//            // If the graphics, the gate, the region, or the canvas settings is
//            // null, the gate is a quadrant gate or a split gate, or the region
//            // should not be shown or should not be drawn, then quit.
//            return;
//        }
//
//        // Don't need to draw a region if Population's Compensation ID doesn't match the gate's Compensation ID
//        if ( (pop != null) && (gate.getCompensationID() != pop.getCompensationID()) ){
//
//          // EDGE CASE where File Compensation was used, but there's NO compensation matrix in the fcs file in this case the region should show the gate.
//          if( !((pop.getCompensation() == null) && (gate.getCompensationID() == Compensation.USE_FILE_COMPENSATION) && (Population.getFileCompensation(pop.getFlowFile()) == null)) ) {
//              return;
//          }
//        }
//
//        // Get whether the plot is an 1D plot
//        boolean plot1DP = cs.is1DPlot();
//
//        // Get whether the plot is an 2D plot
//        boolean plot2DP = cs.is2DPlot();
//
//        if ((plot1DP && (!(gate instanceof Gate1D))) || (plot2DP && (!(gate instanceof Gate2D)))) {
//            // If the plot is an 1D plot and the gate is not an 1D gate
//            // or if the plot is a 2D plot and the gate is not a 2D gate, then
//            // quit.
//            return;
//        }
//
//        // Get the x channel of the plot
//        int xChannel = cs.getXChannel();
//
//        // Get the y channel of the plot
//        int yChannel = cs.getYChannel();
//
//        // Initialize whether the gate is flipped to false
//        boolean flippedP = false;
//
//        /**
//* Get the scale information
//*/
//
//        // Get the scale type flag of the scale of the x channel
//        int xScaleFlag = cs.getScaleFlag(xChannel);
//
//        // Get the scale type flag of the scale of the y channel
//        int yScaleFlag = cs.getScaleFlag(yChannel);
//
//        // Get the channel minimum of the x channel
//        double xMin = cs.getMinimum(xChannel);
//
//        // Get the channel maximum of the x channel
//        double xMax = cs.getMaximum(xChannel);
//
//        // Get the channel minimum of the y channel
//        double yMin = cs.getMinimum(yChannel);
//
//        // Get the channel maximum of the y channel
//        double yMax = cs.getMaximum(yChannel);
//
//        if (plot1DP) {
//            // If the plot is an 1D plot, then check the channels of the gate.
//            if ((gate.getXChannel() == xChannel) && (gate.getYChannel() == -1)) {
//                // If the x channel of the gate is equal to the x channel of the
//                // plot, then the gate is not flipped.
//                if ((xChannel < 0) || (xChannel >= pop.getChannelCount())) {
//                    // If the x channel is invalid, then quit.
//                    return;
//                }
//
//                // Set whether the gate is flipped to false
//                flippedP = false;
//
//                if ((gate.getXScaleFlag() != xScaleFlag) || (Double.isNaN(gate.getXMinimum()) && (!Double.isNaN(xMin)))
//                        || (Double.isNaN(gate.getXMaximum()) && (!Double.isNaN(xMax))) || ((!Double.isNaN(gate.getXMinimum())) && (gate.getXMinimum() != xMin))
//                        || ((!Double.isNaN(gate.getXMaximum())) && (gate.getXMaximum() != xMax))) {
//                    // If the scale type flag of the scale of the x channel of
//                    // the gate is not equal to the scale type flag of the scale
//                    // of the x channel of the plot, then quit.
//                    return;
//                }
//
//                if (xScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the x channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getXScaleArgumentString() != null) && (!gate.getXScaleArgumentString().equals(cs.getScaleArgumentString(xChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the x channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the x channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//            } else if ((cs.getPlotType() == Representation.HISTOGRAM_Y) && (gate.getXChannel() == yChannel) && (gate.getYChannel() == -1)) {
//                // If the type of the plot is a histogram of the y-axis and the
//                // x channel of the gate is equal to the y channel of the plot,
//                // then the gate is flipped.
//                if ((yChannel < 0) || (yChannel >= pop.getChannelCount())) {
//                    // If the y channel is invalid, then quit.
//                    return;
//                }
//
//                // Set whether the gate is flipped to true
//                flippedP = true;
//
//                if ((gate.getXScaleFlag() != yScaleFlag) || (Double.isNaN(gate.getXMinimum()) && (!Double.isNaN(yMin)))
//                        || (Double.isNaN(gate.getXMaximum()) && (!Double.isNaN(yMax))) || ((!Double.isNaN(gate.getXMinimum())) && (gate.getXMinimum() != yMin))
//                        || ((!Double.isNaN(gate.getXMaximum())) && (gate.getXMaximum() != yMax))) {
//                    // If the scale type flag of the scale of the x channel of
//                    // the gate is not equal to the scale type flag of the scale
//                    // of the x channel of the plot, then quit.
//                    return;
//                }
//
//                if (yScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the y channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getXScaleArgumentString() != null) && (!gate.getXScaleArgumentString().equals(cs.getScaleArgumentString(yChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the x channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the y channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//            } else {
//                // Otherwise, the gate is invalid, so quit.
//                return;
//            }
//        }
//
//        if (plot2DP) {
//            // If the plot is a 2D plot, then check the channels of the gate.
//            if ((xChannel < 0) || (xChannel >= pop.getChannelCount()) || (yChannel < 0) || (yChannel >= pop.getChannelCount())) {
//                // If the x channel or the y channel is invalid, then quit.
//                return;
//            }
//
//            if ((gate.getXChannel() == xChannel) && (gate.getYChannel() == yChannel)) {
//                // If the x channel and the y channel of the gate are equal to
//                // the x channel and the y channel of the plot, then the gate is
//                // not flipped.
//                flippedP = false;
//
//                if ((gate.getXScaleFlag() != xScaleFlag) || (gate.getYScaleFlag() != yScaleFlag) || (Double.isNaN(gate.getXMinimum()) && (!Double.isNaN(xMin)))
//                        || (Double.isNaN(gate.getYMinimum()) && (!Double.isNaN(yMin))) || (Double.isNaN(gate.getXMaximum()) && (!Double.isNaN(xMax)))
//                        || (Double.isNaN(gate.getYMaximum()) && (!Double.isNaN(yMax))) || ((!Double.isNaN(gate.getXMinimum())) && (gate.getXMinimum() != xMin))
//                        || ((!Double.isNaN(gate.getYMinimum())) && (gate.getYMinimum() != yMin))
//                        || ((!Double.isNaN(gate.getXMaximum())) && (gate.getXMaximum() != xMax))
//                        || ((!Double.isNaN(gate.getYMaximum())) && (gate.getYMaximum() != yMax))) {
//                    // If the scale type flag of the scale of the x channel of
//                    // the gate is not equal to the scale type flag of the scale
//                    // of the x channel of the plot,
//                    // or if the scale type flag of the scale of the y channel
//                    // of the gate is not equal to the scale type flag of the
//                    // scale of the y channel of the plot,
//                    // then quit.
//                    return;
//                }
//
//                if (xScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the x channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getXScaleArgumentString() != null) && (!gate.getXScaleArgumentString().equals(cs.getScaleArgumentString(xChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the x channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the x channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//
//                if (yScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the y channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getYScaleArgumentString() != null) && (!gate.getYScaleArgumentString().equals(cs.getScaleArgumentString(yChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the y channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the y channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//            } else if ((gate.getXChannel() == yChannel) && (gate.getYChannel() == xChannel)) {
//                // If the x channel and the y channel of the gate are equal to
//                // the y channel and the x channel of the plot, then the gate is
//                // flipped.
//                flippedP = true;
//
//                if ((gate.getXScaleFlag() != yScaleFlag) || (gate.getYScaleFlag() != xScaleFlag) || (Double.isNaN(gate.getXMinimum()) && (!Double.isNaN(yMin)))
//                        || (Double.isNaN(gate.getYMinimum()) && (!Double.isNaN(xMin))) || (Double.isNaN(gate.getXMaximum()) && (!Double.isNaN(yMax)))
//                        || (Double.isNaN(gate.getYMaximum()) && (!Double.isNaN(xMax))) || ((!Double.isNaN(gate.getXMinimum())) && (gate.getXMinimum() != yMin))
//                        || ((!Double.isNaN(gate.getYMinimum())) && (gate.getYMinimum() != xMin))
//                        || ((!Double.isNaN(gate.getXMaximum())) && (gate.getXMaximum() != yMax))
//                        || ((!Double.isNaN(gate.getYMaximum())) && (gate.getYMaximum() != xMax))) {
//                    // If the scale type flag of the scale of the x channel of
//                    // the gate is not equal to the scale type flag of the scale
//                    // of the y channel of the plot,
//                    // or if the scale type flag of the scale of the y channel
//                    // of the gate is not equal to the scale type flag of the
//                    // scale of the x channel of the plot,
//                    // then quit.
//                    return;
//                }
//
//                if (xScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the x channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getXScaleArgumentString() != null) && (!gate.getXScaleArgumentString().equals(cs.getScaleArgumentString(yChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the x channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the y channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//
//                if (yScaleFlag == Scaling.ARCSINH) {
//                    // If the scale of the y channel of the plot is the arcsinh
//                    // scale, then check the scale argument string.
//                    if ((gate.getYScaleArgumentString() != null) && (!gate.getYScaleArgumentString().equals(cs.getScaleArgumentString(xChannel)))) {
//                        // If the scale argument string of the scale argument of
//                        // the scale of the y channel of the gate is not null
//                        // and not equal to the scale argument string of the
//                        // scale argument of the scale of the x channel of the
//                        // plot, then quit.
//                        return;
//                    }
//                }
//            } else {
//                // Otherwise, the gate is invalid, so quit.
//                return;
//            }
//        }
//
//        /**
//* Draw the gate
//*/
//
//        // Get the width of the plot
//        int plotWidth = cs.getPlotWidth();
//
//        // Get the left x-coordinate of the current plot
//        int leftX = cs.getStartX();
//
//        // Get the bottom y-coordinate of the current plot
//        int bottomY = cs.getBottomY(0, 0);
//
//        // Get the number of axis bins on the x-axis of the plot on which the
//        // gate was drawn
//        int axisBins = gate.getXBins();
//
//        if (axisBins <= 0) {
//            // If the number of axis bins on the x-axis of the plot on which the
//            // gate was drawn is less than or equal to 0, then quit.
//            return;
//        }
//
//        // Calculate the scale factor between the plot on which the gate was
//        // drawn and the current plot
//        double scaleFactor = (double) plotWidth / (double) axisBins;
//
//        // Save the current transformation
//        AffineTransform origTransform = g.getTransform();
//
//        // Move the origin to the bottom left corner of the plot
//        g.translate(leftX, bottomY);
//
//        // Scale the coordinate system to the size of the plot on which the gate
//        // was drawn
//        g.scale(scaleFactor, -scaleFactor);
//
//        if (flippedP) {
//            // If the region is flipped, then rotate the coordinate axes.
//            g.rotate(Math.PI / 2);
//            g.scale(1.0d, -1.0d);
//        }
//
//        // Draw the gate
//        if (gate.isNegative()) {
//            gate.draw(g, true, Gate.NORMAL_COLOR, Gate.NORMAL_COLOR, NEG_GATE_FILL_COLOR);
//        } else if (gate.isPositive()) {
//            gate.draw(g, true, Gate.NORMAL_COLOR, Gate.NORMAL_COLOR, POS_GATE_FILL_COLOR);
//        } else {
//            gate.draw(g);
//        }
//
//        // Restore the original transformation
//        g.setTransform(origTransform);
//
//        /**
//* Draw the label
//*/
//
//        // Create a new StringBuffer with which we will build the label
//        StringBuffer labelBuffer = new StringBuffer();
//
//        if (region.isLabelShown()) {
//            // If the label is shown, then append the name of the gate to the
//            // label of the region.
//            labelBuffer.append(gate.getName());
//            labelBuffer.append(" ");
//        }
//
//        if (pop != null) {
//            // If the population is not null, then get the subpopulation for the
//            // other information for the label of the region.
//
//            // Initialize the channel to the x channel of the gate
//            int channel = gate.getXChannel();
//
//            if (flippedP) {
//                // If the gate is flipped, then set the channel to the y channel
//                // of the gate.
//                channel = gate.getYChannel();
//            }
//
//            // Gate the population on the gate to get the subpopulation
//            Population subPop = pop.gate(gate);
//
//            if (subPop != null) {
//                // If the subpopulation is not null, then get the other
//                // information for the label of the region.
//                if (region.isPercentShown()) {
//                    // If the percent is shown, then append the percent in the
//                    // region defined by the current gate to the label of the
//                    // region.
//                    if (labelBuffer.length() > 0) {
//                        // If the label is not empty, then prepend a comma.
//                        labelBuffer.append(", ");
//                    }
//
//                    // Append the percent in the region defined by the current
//                    // gate to the label of the region
//                    labelBuffer.append(CanvasSettings.DOUBLE_FORMAT.format(subPop.getPercent()));
//                    labelBuffer.append("% ");
//                }
//
//                if (region.isEventCountShown()) {
//                    // If the event count is shown, then append the event count
//                    // in the region defined by the current gate to the label of
//                    // the region.
//                    if (labelBuffer.length() > 0) {
//                        // If the label is not empty, then prepend a comma.
//                        labelBuffer.append(", ");
//                    }
//
//                    // Append the event count in the region defined by the
//                    // current gate to the label of the region
//                    labelBuffer.append(CanvasSettings.INT_FORMAT.format(subPop.getEventCount()));
//                }
//
//                if (region.isMedianShown()) {
//                    // If the median is shown, then append the median in the
//                    // region defined by the current gate to the label of the
//                    // region.
//                    if (labelBuffer.length() > 0) {
//                        // If the label is not empty, then prepend a comma.
//                        labelBuffer.append(", ");
//                    }
//
//                    // Append the median in the region defined by the current
//                    // gate to the label of the region
//                    labelBuffer.append(CanvasSettings.DOUBLE_FORMAT.format(subPop.getMedian(channel)));
//                }
//
//                if (region.isMeanShown()) {
//                    // If the mean is shown, then append the mean in the region
//                    // defined by the current gate to the label of the region.
//                    if (labelBuffer.length() > 0) {
//                        // If the label is not empty, then prepend a comma.
//                        labelBuffer.append(", ");
//                    }
//
//                    // Append the mean in the region defined by the current gate
//                    // to the label of the region
//                    labelBuffer.append(CanvasSettings.DOUBLE_FORMAT.format(subPop.getMean(channel)));
//                }
//            }
//            if (subPop != null)
//             subPop.close();
//        }
//
//        // Get the String representation of the StringBuffer
//        String label = labelBuffer.toString();
//
//        if ((label != null) && (label.length() > 0)) {
//            // If the label of the region is not null and not empty, then draw
//            // it.
//
//            // Save the current font
//            Font origFont = g.getFont();
//
//            // Set the font for the label
//            g.setFont(CanvasSettings.REGION_LABEL_FONT);
//            if (plotWidth > 128)
//                g.setFont(CanvasSettings.CRITICAL_LABEL_FONT);
//
//            // Get the font metrics of the current font
//            FontMetrics metrics = g.getFontMetrics();
//
//            // Get the height of the current font
//            int fontHeight = metrics.getHeight();
//
//            // Get the width of the label in the current font
//            int fontWidth = metrics.stringWidth(label);
//
//            /**
//* Calculate the coordinates of the label so that it is centered at
//* the gate label coordinates --- Note: drawString() takes the
//* bottom left corner of the string as the coordinates whereas
//* fillRect() takes top left corner of the rectangle.
//*/
//            int labelX, labelY;
//
//            if (flippedP) {
//                // If the region is flipped, then use the gate label
//                // y-coordinate to calculate the label x-coordinate and vice
//                // versa.
//
//                // The x-coordinate of the label is the left x-coordinate of the
//                // plot plus the amount the label is on the y-axis (since the
//                // region is flipped)
//                // minus half the width of the label
//                labelX = leftX + (int) ((double) gate.getLabelY() * scaleFactor) - (fontWidth / 2);
//
//                // The y-coordinate of the label is the bottom y-coordinate of
//                // the plot minus the amount the label is on the x-axis (since
//                // the region is flipped)
//                // plus half the height of the label (since we are in user
//                // coordinates and the origin is in the upper left corner)
//                labelY = bottomY - (int) ((double) gate.getLabelX() * scaleFactor) + (fontHeight / 2);
//            } else {
//                // Otherwise, the region is not flipped, so use the gate label
//                // coordinates as they are.
//
//                // The x-coordinate of the label is the left x-coordinate of the
//                // plot plus the amount the label is on the x-axis
//                // minus half the width of the label
//                labelX = leftX + (int) ((double) gate.getLabelX() * scaleFactor) - (fontWidth / 2);
//
//                // The y-coordinate of the label is the bottom y-coordinate of
//                // the plot minus the amount the label is on the y-axis
//                // plus half the height of the label (since we are in user
//                // coordinates and the origin is in the upper left corner)
//                labelY = bottomY - (int) ((double) gate.getLabelY() * scaleFactor) + (fontHeight / 2);
//            }
//            if (labelX < leftX + 2 || (labelX < plotWidth / 2 && gate.getType() == Quad.TYPE))
//                labelX = leftX + 1;
//            if (labelX > leftX + plotWidth - fontWidth - 2 || (labelX > plotWidth / 2 && gate.getType() == Quad.TYPE))
//                labelX = leftX + plotWidth - fontWidth + 1;
//            if (labelY > bottomY - 2 || (labelY > plotWidth / 2 && gate.getType() == Quad.TYPE))
//                labelY = bottomY - 2;
//            if (labelY < bottomY - plotWidth + fontHeight - 2 || (labelY < plotWidth / 2 && gate.getType() == Quad.TYPE))
//                labelY = bottomY - plotWidth + fontHeight - 1;
//
//            // Draw a white rectangle behind the label
//            g.setColor(Color.white);
//            g.fillRect(labelX - 1, labelY - fontHeight, fontWidth + 2, fontHeight + 2);
//
//            // Draw the label
//            g.setColor(Color.blue);
//            g.drawString(label, labelX, labelY);
//
//            // Restore the original font
//            g.setFont(origFont);
//        }
//    }

    /**
* Histogram overlay labels methods
*/

    /**
* <p>
* Returns the labels of the histogram overlay in the <code>String</code>
* array labels.
* </p>
*
* @param labels
* <code>String</code> array of labels of the histogram overlay.
* @param cs
* <code>CanvasSettings</code> object to the canvas settings of
* the plot.
* @return <code>BufferedImage</code> object to the labels of the histogram
* overlay.
*/
    public static BufferedImage drawHistogramOverlayLabels(String[] labels, CanvasSettings cs) {
        if ((labels == null) || (cs == null) || (cs.getPlotType() != Representation.HISTOGRAM_OVERLAY)) {
            // If the array of labels or the canvas settings is null or if the
            // type of the plot is not a histogram overlay, then quit.
            return null;
        }

        // Set the number of rows to the length of the array of labels
        cs.setRowCount(labels.length);

        // Find the longest string and set the image width using it
        int maxLength = 0;

        // Loop through the array of labels
        for (int i = 0; i < labels.length; i++) {
            if ((labels[i] != null) && (labels[i].length() > maxLength)) {
                // If the current label is not null and its length is greater
                // than the maximum length, then update the maximum length.
                maxLength = labels[i].length();
            }
        }

        int fontWidth = 12;

        // Calculate the width of the image = 14 * the maximum length of a label
        // This is a bit of a hack based on the size of the CRITICAL_LABEL_FONT
        int imageWidth = fontWidth * maxLength;

        // Create a buffered image in which we will draw the plot
        BufferedImage image = new BufferedImage(imageWidth, cs.getCanvasHeight(), BufferedImage.TYPE_INT_RGB);

        // Get the Graphics object to the buffered image
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background color
        g.setColor(cs.getBackgroundColor());
        g.fillRect(0, 0, imageWidth, cs.getCanvasHeight());

        // Set the font size for this critical label
        g.setFont(CanvasSettings.CRITICAL_LABEL_FONT);

        // Get the font metrics of the current font
        FontMetrics metrics = g.getFontMetrics();

        // Get the height of the current font
        int fontHeight = metrics.getHeight();

        /**
* Draw the labels of the histogram overlay
*/

        // Set the color to the text color
        g.setColor(cs.getTextColor());

        int topY;

        // Loop through the array of labels
        for (int i = 0; i < labels.length; i++) {
            if ((labels[i] != null) && (labels[i].length() > 0)) {
                // If the current label is not null and not empty, then draw it.

                // Get the the bottom y-coordinate of the current histogram
                // The label should be just above the axis of a histogram
                topY = cs.getBottomY(i, 0) - fontHeight + 3;

                // Calculate the right x-coordinate of the labels of the
                // histogram overlay
                int labelX = imageWidth - 2;

                // Move the graphics into position for the current label
                g.translate(0, topY);

                // Draw the current label
                drawYScaleLabel(g, labels[i], labelX, 0);

                // Move the graphics back to the origin
                g.translate(0, -topY);
            }
        }

        // Dispose of the graphics
        g.dispose();

        // Return the buffered image
        return image;
    }

    /**
* Color gradient methods
*/

    /**
* <p>
* Returns the scale of the color gradient in the <code>ColorGradient</code>
* object gradient.
* </p>
*
* @param gradient
* <code>ColorGradient</code> object to the color gradient.
* @return <code>BufferedImage</code> object to the scale of the color
* gradient in the <code>ColorGradient</code> object gradient.
*/
    public static BufferedImage drawColorGradient(ColorGradient gradient) {
        return drawColorGradient(gradient, COLOR_HEIGHT);

    }


    /**
* <p>
* Returns the scale of the color gradient in the <code>ColorGradient</code>
* object gradient.
* </p>
*
* @param gradient
* <code>ColorGradient</code> object to the color gradient.
* @param barHeight
* int height of each bar of color in the scale of the color
* gradient.
* @return <code>BufferedImage</code> object to the scale of the color
* gradient in the <code>ColorGradient</code> object gradient.
*/
    public static BufferedImage drawColorGradient(ColorGradient gradient, int barHeight) {
        if (gradient == null) {
            return null;
        }

        // Set whether the color gradient is a bidirectional color gradient
        boolean bidirectionalP = (gradient instanceof BidirectionalColorGradient);

        int xOffset = 30;
        int yOffset = 1;

        // Calculate the number of levels
        int levelCount = COLOR_GRADIENT_LEVEL_COUNT;

        if (bidirectionalP) {
            // If the color gradient is a bidirectional color gradient, then add
            // COLOR_GRADIENT_LEVEL_COUNT - 1 to the number of levels.
            // We subtract one since the two color gradients overlap.
            levelCount += COLOR_GRADIENT_LEVEL_COUNT - 1;
        }

        // Create the scale image
        int scaleImageWidth = levelCount * COLOR_WIDTH + 2 * xOffset;
        int scaleImageHeight = barHeight + 30;
        BufferedImage image = new BufferedImage(scaleImageWidth, scaleImageHeight, BufferedImage.TYPE_INT_RGB);

        // Get the graphics of the buffered image
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set the background color to white
        g.setColor(Color.white);
        g.fillRect(0, 0, scaleImageWidth, scaleImageHeight);

        // Draw the scale colors

        // Get the minimum value of the color gradient
        double gradientMin = gradient.getMinimum();
        double gradientMax = gradient.getMaximum();

        // Calculate the increment between levels of the color gradient
        double increment = Math.abs(gradientMax - gradientMin) / (double) (levelCount - 1);

        // Loop through the levels of the color gradient
        for (int i = 0; i < levelCount; i++) {
            g.setColor(gradient.getColor(gradientMin + ((double) i * increment)));
            g.fillRect(xOffset + i * COLOR_WIDTH, yOffset, COLOR_WIDTH, barHeight);
        }

        // Draw a black border around the scale of the color gradient
        g.setColor(Color.black);
        g.drawRect(xOffset - 1, 0, levelCount * COLOR_WIDTH + 1, barHeight + 1);

        // Draw the scale labels
        if (!Double.isNaN(gradientMin) && !Double.isNaN(gradientMin)) {

            int minX = xOffset;
            int maxX = xOffset + levelCount * COLOR_WIDTH - 1;

            int tickY = yOffset + barHeight + 1;
            int labelY = yOffset + barHeight + 1 + MINOR_TICK_LENGTH;

            // Draw the scale label of the minimum value of the color gradient
            drawXMinorScaleTick(g, minX, tickY);
            drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format(gradientMin), minX, labelY);

            // Draw the scale label of the maximum value of the color gradient
            drawXMinorScaleTick(g, maxX, tickY);
            drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format(gradientMax), maxX, labelY);

            if (bidirectionalP) {
                // If the color gradient is a bidirectional color gradient, then draw the labels of the three middle values.

                // Get the inflection and calculate the middle (minor tick) values in between each half of the scale
                double inflection = ((BidirectionalColorGradient) gradient).getInflection();
                double minMinorScaleTickValue = middleValue(gradientMin, inflection);
                double maxMinorScaleTickValue = middleValue(inflection, gradientMax);

                // Figure out the X coords in the image to position the three values at
                int inflectionXCoord = relativeXCoordinate(inflection, gradientMin, gradientMax, xOffset);
                int minMinorScaleXCoord = relativeXCoordinate(minMinorScaleTickValue, gradientMin, gradientMax, xOffset);
                int maxMinorScaleXCoord = relativeXCoordinate(maxMinorScaleTickValue, gradientMin, gradientMax, xOffset);

                // Draw the scale label of the value of the inflection point of the color gradient
                drawXMinorScaleTick(g, inflectionXCoord, tickY);
                drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format(inflection), inflectionXCoord, labelY);

                // Draw the scale label of the middle value between the minimum value and the value of the inflection point
                drawXMinorScaleTick(g, minMinorScaleXCoord, tickY);
                drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format(minMinorScaleTickValue), minMinorScaleXCoord, labelY);

                // Draw the scale label of the middle value between the middle value between the value of the inflection point and the maximum value
                drawXMinorScaleTick(g, maxMinorScaleXCoord, tickY);
                drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format(maxMinorScaleTickValue), maxMinorScaleXCoord, labelY);

            } else {
                // Otherwise, the color gradient is a unidirectional color gradient, so draw the label of the middle value.

                // Calculate the x-coordinate of the line for the middle value of the color gradient
                int middleX = xOffset + (COLOR_GRADIENT_LEVEL_COUNT * COLOR_WIDTH) / 2;

                // Draw the scale label of the middle value of the color gradient
                drawXMinorScaleTick(g, middleX, tickY);
                drawXScaleLabel(g, CanvasSettings.DOUBLE_FORMAT.format((gradientMin + gradientMax) / 2.0d), middleX, labelY);
            }
        }

        g.dispose();

        return image;
    }


    /**
* Calculates the X coordinate for the value in a scale image for a given value
* and the min/max range of possible values. Done by calculating the fractional
* distance from the minimum to the value and converting to pixels with the
* addition of an offset value (the scale bar is offset from the left edge of the imagee)
*
* This function will work for all positive and negative values of value,min,max as long
* as they're in logical order min <= value <= max. If the inputs don't follow that
* results will be not be valid.
* @param value
* @param min
* @param max
* @param offset
* @return int
*/
    public static int relativeXCoordinate(double value, double min, double max, double offset){

      // Convert the ranges from the minimum to both the input value and max to absolute ranges
      // and calculate the fractional distance from the min -> value and then convert to pixels
      // by multiplying by the width in pixels and add the offset in (the scale bar sits 'offset'
      // pixels from the left side of the image) and round off to an integer for the drawing code
      double relativePosition = ((range(value, min) / range(min,max)) * COLOR_GRADIENT_WIDTH * 2.0d);
      return (int)Math.round(relativePosition + offset);

    }


    /**
* Calculates the range of any two real numbers.
* Accepts input values in any order
* @param value1
* @param value2
* @return double
*/
    public static double range(double value1, double value2) {

      if (value1 != value2) {

        double min = Math.min(value1, value2);
        double max = Math.max(value1, value2);

        if((min < 0) && (max <= 0)){
          return Math.abs(min) - Math.abs(max);
        } else if((min >= 0) && (max > 0)){
          return Math.abs(max) - Math.abs(min);
        } else {
          return Math.abs(min) + Math.abs(max);
        }

      }

      // If value1 == value2 range is zero
      return 0;
    }


    /**
* Calculates the exact value in between the two input values. Accepts the values
* in either order and should work for all real numbers
* @param valueOne double
* @param valueTwo double
* @return double
*/
    public static double middleValue(double valueOne, double valueTwo) {

      if (valueOne < valueTwo) {
        return (valueTwo - range(valueOne,valueTwo) / 2.0d);
      }
      else if(valueOne > valueTwo) {
        return (valueOne - range(valueOne,valueTwo) / 2.0d);
      }

      // Just return the first value if they're equal
      return valueOne;

    }


}