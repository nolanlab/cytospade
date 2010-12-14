/**
 * Representation.java
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

// TODO This needs to be cleaned up since there are
// TODO deferences between this version and the version (formerly) found in smallComp.

// Import the Serializable class
import java.io.Serializable;

// Import the util package
import java.util.*;

// Import the classes for the URLEncoder
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>
 * An object containings the settings for a representation of the figures.
 * </p>
 *
 * <p>
 * Since the object is serialized, we can no longer support many of the
 * convenience methods that support encoding different objects such as the
 * <code>facs.PopulationReference</code> object. Instead, the encode methods
 * only take basic parameters as input and it is up to the client to supply the
 * appropriate information. It means it's a little more work for the client and
 * a little more logic for the client, but that is the price of serialization.
 * </p>
 *
 * <p>
 * The representation does not maintain the ID of its containing experiment.
 * This is the only thing that keeps it from completely encoding all of the
 * information needed by the plot service. This was a conscious decision to
 * avoid maintaining the same piece of information in multiple places, thereby
 * creating the need to synchronize it. Even as such, the representation still
 * saves the client from needing to do a large amount of the encoding work.
 * </p>
 */
public final class Representation implements Serializable {
    /**
     * The version number of the class for serialization
     */
    private static final long serialVersionUID = 4649L;

    /**
     * Plot type constant flags
     */

    /**
     * The constant flag for the test pattern (default plot)
     */
    public static final int TEST_PATTERN = -1;

    /**
     * The constant flag for the histogram of the x-axis
     */
    public static final int HISTOGRAM_X = 1;

    /**
     * The constant flag for the histogram of the y-axis
     */
    public static final int HISTOGRAM_Y = 2;

    /**
     * The constant flag for the dot plot
     */
    public static final int DOT_PLOT = 11;

    /**
     * The constant flag for the density dot plot
     */
    public static final int DENSITY_DOT_PLOT = 12;

    /**
     * The constant flag for the shadow plot
     */
    public static final int SHADOW_PLOT = 13;

    /**
     * The constant flag for the contour plot
     */
    public static final int CONTOUR_PLOT = 14;

    /**
     * The constant flag for the shaded contour plot
     */
    public static final int SHADED_CONTOUR_PLOT = 15;

    /**
     * The constant flag for the density plot
     */
    public static final int DENSITY_PLOT = 16;

    /**
     * The constant flag for the histogram overlay
     */
    public static final int HISTOGRAM_OVERLAY = 101;

    /**
     * Statistic type constant flags
     */

    /**
     * The constant flag for no statistic
     */
    public static final int NO_STATISTIC = -1;

    /**
     * The constant flag for the mean
     */
    public static final int MEAN = 1;

    /**
     * The constant flag for the median
     */
    public static final int MEDIAN = 2;

    /**
     * The constant flag for the standard deviation
     */
    public static final int STANDARD_DEVIATION = 3;

    /**
     * The constant flag for the variance
     */
    public static final int VARIANCE = 4;

    /**
     * The constant flag for the minimum
     */
    public static final int MINIMUM = 5;

    /**
     * The constant flag for the maximum
     */
    public static final int MAXIMUM = 6;

    /**
     * The constant flag for the percent
     */
    public static final int PERCENT = 8;

    /**
     * The constant flag for the event count
     */
    public static final int EVENT_COUNT = 9;

    /**
     * The constant flag for the channel range
     */
    public static final int CHANNEL_RANGE = 10;

    /**
     * The constant flag for the geometric mean
     */
    public static final int GEOMETRIC_MEAN = 11;

    /**
     * The constant flag for the 1D statistics summary of the x-axis
     */
    public static final int STATISTIC_1D_X = 101;

    /**
     * The constant flag for the 1D statistics summary of the y-axis
     */
    public static final int STATISTIC_1D_Y = 102;

    /**
     * The constant flag for the 2D statistics summary
     */
    public static final int STATISTIC_2D = 103;

    /**
     * Color set constant flags
     */

    /**
     * The constant flag for the default color set
     */
    public static final int DEFAULT_COLOR_SET = -1;

    /**
     * The constant flag for the en fuego (heat) color set
     */
    public static final int EN_FUEGO = 1;

    /**
     * The constant flag for the chill out (cool) color set
     */
    public static final int CHILL_OUT = 2;

    /**
     * The constant flag for the rainbow color set
     */
    public static final int RAINBOW = 3;

    /**
     * The constant flag for the greyscale color set
     */
    public static final int GREYSCALE = 4;

    /**
     * The constant flag for the pale fire color set
     */
    public static final int PALE_FIRE = 5;

    /**
     * The constant flag for the green to red color set
     */
    public static final int GREEN_TO_RED = 6;

    /**
     * The constant flag for the blue to yellow color set
     */
    public static final int BLUE_TO_YELLOW = 7;

    /**
     * The constant flag for the ryan1 color set
     */
    public static final int RYAN1 = 101;

    /**
     * The constant flag for the william1 color set
     */
    public static final int WILLIAM1 = 102;

    /**
     * The constant flag for the nikesh1 color set
     */
    public static final int NIKESH1 = 103;

    /**
     * The constant flag for the mark1 color set
     */
    public static final int MARK1 = 104;

    /**
     * The constant flag for the peter1 color set
     */
    public static final int PETER1 = 105;

    /**
     * Region IDs
     */

    /**
     * The ID of the universal region
     */
    public static final int ALL_REGIONS = -3;

    /**
     * The ID of the ungated region
     */
    public static final int UNGATED = -2;

    /**
     * The ID of the gate set region
     */
    public static final int GATE_SET = -1;

    /**
     * <p>
     * A public static nested class to encode the region parameters for a
     * region.
     * </p>
     *
     * <p>
     * The class is very similar to the <code>PlotRegion</code> class, but it is
     * different in that the <code>Region</code> is designed so that the client
     * can set the boolean flags and have the corresponding values be encoded as
     * region parameters rather than vice versa.
     * </p>
     */
    public static final class Region implements Comparable, Serializable {
        /**
         * The ID of the region
         */
        private final int id;

        /**
         * The boolean flag indicating whether to show the region
         */
        private boolean showP;

        /**
         * The boolean flag indicating whether to draw the region
         */
        private boolean drawP;

        /**
         * The boolean flag indicating whether to show the label of the region
         */
        private boolean showLabelP;

        /**
         * The boolean flag indicating whether to show the number of events in
         * the region
         */
        private boolean showEventCountP;

        /**
         * The boolean flag indicating whether to show the mean of the region
         */
        private boolean showMeanP;

        /**
         * The boolean flag indicating whether to show the median of the region
         */
        private boolean showMedianP;

        /**
         * The boolean flag indicating whether to show the percent of the region
         */
        private boolean showPercentP;

        /**
         * <p>
         * A constructor for <code>Region</code>.
         * </p>
         *
         * @param id
         * int ID of the region.
         */
        private Region(int id) {
            // Set the ID of the region
            this.id = id;

            /**
             * Initialize all the boolean flags to false
             */
            this.showP = false;
            this.drawP = false;
            this.showLabelP = false;
            this.showEventCountP = false;
            this.showMeanP = false;
            this.showMedianP = false;
            this.showPercentP = false;
        }

        /**
         * <p>
         * Returns the ID of the region.
         * </p>
         *
         * @return int ID of the region.
         */
        public int getID() {
            return id;
        }

        /**
         * <p>
         * Returns whether this region is equal to the object in the
         * <code>Object</code> object obj.
         * </p>
         *
         * <p>
         * Comparison is performed by comparing the IDs of the two regions if
         * the <code>Object</code> object obj is a
         * <code>Representation.Region</code> object.
         * </p>
         *
         * @param obj
         * <code>Object</code> object to the reference object with
         * which to compare.
         * @return boolean flag indicating whether this region is equal to the
         * object in the <code>Object</code> object obj.
         */
        public boolean equals(Object obj) {
            if (obj == null) {
                // If the object is null, then return false.
                return false;
            }

            if (obj instanceof Region) {
                // If the object is a region, then cast it to a region.
                Region region = (Region) obj;

                // Return whether the IDs of the regions are equal
                return (id == region.id);
            } else {
                // Otherise, the object is not a region, so return false.
                return false;
            }
        }

        /**
         * <p>
         * Returns the ID of the region as the hash code value for the object.
         * </p>
         *
         * @return int ID of the region as the hash code value for the object.
         */
        public int hashCode() {
            return getID();
        }

        /**
         * <p>
         * Returns whether to show the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the region.
         */
        public boolean isShown() {
            return showP;
        }

        /**
         * <p>
         * Sets whether to show the region to showP.
         * </p>
         *
         * @param showP
         * boolean flag indicating whether to show the region.
         */
        public void setShown(boolean showP) {
            this.showP = showP;
        }

        /**
         * <p>
         * Returns whether to draw the region.
         * </p>
         *
         * @return boolean flag indicating whether to draw the region.
         */
        public boolean isDrawn() {
            return drawP;
        }

        /**
         * <p>
         * Sets whether to draw the region to drawP.
         * </p>
         *
         * @param drawP
         * boolean flag indicating whether to draw the region.
         */
        public void setDrawn(boolean drawP) {
            this.drawP = drawP;
        }

        /**
         * <p>
         * Returns whether to show the label of the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the label of the
         * region.
         */
        public boolean isLabelShown() {
            return showLabelP;
        }

        /**
         * <p>
         * Sets whether to show the label of the region to showLabelP.
         * </p>
         *
         * @param showLabelP
         * boolean flag indicating whether to show the label of the
         * region.
         */
        public void setLabelShown(boolean showLabelP) {
            this.showLabelP = showLabelP;
        }

        /**
         * <p>
         * Returns whether to show the number of events in the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the number of events
         * in the region.
         */
        public boolean isEventCountShown() {
            return showEventCountP;
        }

        /**
         * <p>
         * Sets whether to show the number of events in the region to
         * showEventCountP.
         * </p>
         *
         * @param showEventCountP
         * boolean flag indicating whether to show the number of
         * events in the region.
         */
        public void setEventCountShown(boolean showEventCountP) {
            this.showEventCountP = showEventCountP;
        }

        /**
         * <p>
         * Returns whether to show the mean of the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the mean of the
         * region.
         */
        public boolean isMeanShown() {
            return showMeanP;
        }

        /**
         * <p>
         * Sets whether to show the mean of the region to showMeanP.
         * </p>
         *
         * @param showMeanP
         * boolean flag indicating whether to show the mean of the
         * region.
         */
        public void setMeanShown(boolean showMeanP) {
            this.showMeanP = showMeanP;
        }

        /**
         * <p>
         * Returns whether to show the median of the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the median of the
         * region.
         */
        public boolean isMedianShown() {
            return showMedianP;
        }

        /**
         * <p>
         * Sets whether to show the median of the region to showMedianP.
         * </p>
         *
         * @param showMedianP
         * boolean flag indicating whether to show the median of the
         * region.
         */
        public void setMedianShown(boolean showMedianP) {
            this.showMedianP = showMedianP;
        }

        /**
         * <p>
         * Returns whether to show the percent of the region.
         * </p>
         *
         * @return boolean flag indicating whether to show the percent of the
         * region.
         */
        public boolean isPercentShown() {
            return showPercentP;
        }

        /**
         * <p>
         * Sets whether to show the percent of the region to showPercentP.
         * </p>
         *
         * @param showPercentP
         * boolean flag indicating whether to show the percent of the
         * region.
         */
        public void setPercentShown(boolean showPercentP) {
            this.showPercentP = showPercentP;
        }

        /**
         * <p>
         * Returns the encoded region parameters of the region.
         * </p>
         *
         * @return <code>String</code> encoded region parameters of the region.
         */
        public String encode() {
            // Create a StringBuffer with which to encode the region parameters
            StringBuffer parameters = new StringBuffer();

            /**
             * Encode the region parameters
             */

            if (showP) {
                // If the region should be shown, then encode the other region
                // parameters.

                if (drawP) {
                    // If the region should be drawn, then encode draw the
                    // region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=region");
                }

                if (showLabelP) {
                    // If the label of the region should be shown, then encode
                    // show the label of the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=label");
                }

                if (showEventCountP) {
                    // If the number of events in the region should be shown,
                    // then encode show the number of events in the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=eventCount");
                }

                if (showMeanP) {
                    // If the mean of the region should be shown, then encode
                    // show the mean of the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=mean");
                }

                if (showMedianP) {
                    // If the median of the region should be shown, then encode
                    // show the median of the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=median");
                }

                if (showPercentP) {
                    // If the percent of the region should be shown, then encode
                    // show the percent of the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=percent");
                }
            } else {
                // Otherwise, the region should not be shown, so encode do not
                // show the region.
                parameters.append("&region");
                parameters.append(id);
                parameters.append("=none");
            }

            // Return the String representation of the StringBuffer
            return parameters.toString();
        }

        /**
         * Comparable interface
         */

        /**
         * <p>
         * Returns the comparison of this region with the object in the
         * <code>Object</code> object obj.
         * </p>
         *
         * <p>
         * This method is the <code>Representation.Region</code> implementation
         * of the abstract method in the <code>Comparable</code> interface to
         * allow regions to be sorted by ID.
         * </p>
         *
         * <p>
         * Comparison is performed by comparing the IDs of the two regions if
         * the <code>Object</code> object obj is a <code>Region</code> object.
         * </p>
         *
         * <p>
         * The ordering is consistent with equals.
         * </p>
         *
         * @param obj
         * <code>Object</code> object to the object with which to
         * compare.
         * @return int result of the comparison.
         */
        public int compareTo(Object obj) {
            if (obj == null) {
                // If the object is null, then throw a null pointer exception.
                throw new NullPointerException("The object is null.");
            }

            if (obj instanceof Region) {
                // If the object is a region, then cast it to a region.
                Region region = (Region) obj;

                if (id < region.id) {
                    // If the ID of this region is less than the ID of the other
                    // region, then this region precedes the other region, so
                    // return -1.
                    return -1;
                } else if (id == region.id) {
                    // If the IDs of the regions are equal, then the regions are
                    // equal, so then return 0.
                    return 0;
                } else {
                    // Otherwise, the ID of this region is greater than the ID
                    // of the other region, so this region follows the other
                    // region, so return 1.
                    return 1;
                }
            } else {
                // Otherwise, the object is not a region, so throw a class cast
                // exception.
                throw new ClassCastException("The object is not a region.");
            }
        }
    }

    /**
     * The ID of the representation
     */
    private final int id;

    /**
     * The name of the representation
     */
    private String name;

    /**
     * The boolean flag indicating whether the representation is the default
     * representation for a panel set
     */
    private boolean panelDefaultRepresentationP;

    /**
     * The number of channels in the representation
     */
    private int channelCount;

    /**
     * Population parameters
     */

    /**
     * The filename of the flow file used to create the representation
     */
    private String filename;

    /**
     * The ID of the compensation
     */
    private int compensationID;

    /**
     * The list of gate set IDs
     */
    private ArrayList gateSetIDs;

    /**
     * Plot parameters
     */

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
     * The type of the statistic
     */
    private int statType;

    /**
     * The color set
     */
    private int colorSet;

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
     * The number of axis bins on each axis
     */
    private int axisBins;

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

    private int dotSize;

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
    private ArrayList regions;

    /**
     * <p>
     * A full constructor for <code>Representation</code>. It should not be
     * invoked directly. Instead, the appropriate factory method should be used.
     * </p>
     *
     * @param id
     * int ID of the representation.
     * @param name
     * <code>String</code> name of the representation.
     * @param channelCount
     * int number of channels in the representation.
     */
    public Representation(int id, String name, int channelCount) {
        // Set the ID of the representation
        this.id = id;

        // Set the name of the representation
        this.name = name;

        // Initialize whether the representation is the default representation
        // for a panel set to false
        panelDefaultRepresentationP = false;

        if (channelCount <= 0) {
            // If the number of channels is less than or equal to 0, then set
            // the number of channels in the representation to 0.
            this.channelCount = 0;
        } else {
            // Otherwise, the number of channels is greater than 0, so set the
            // number of channels in the representation to it.
            this.channelCount = channelCount;
        }

        /**
         * Population parameters
         */

        // Initialize the filename of the flow file used to create the
        // representation to null
        filename = null;

        // Initialize the ID of the compensation to -1
        compensationID = -1;

        // Create the list of gate set IDs
        gateSetIDs = new ArrayList();

        /**
         * Plot parameters
         */

        // Initialize the x channel to -1
        xChannel = -1;

        if (this.channelCount > 0) {
            // If the number of channels is greater than 0, then set the x
            // channel to 0.
            xChannel = 0;
        }

        // Initialize the y channel to -1
        yChannel = -1;

        if (this.channelCount > 0) {
            // If the number of channels is greater than 0, then set the y
            // channel to 0.
            yChannel = 0;
        }

        // Initialize the type of the plot to TEST_PATTERN
        plotType = TEST_PATTERN;

        // Initialize the type of the statistic to NO_STATISTIC
        statType = NO_STATISTIC;

        // Initialize the color set to DEFAULT_COLOR_SET
        colorSet = DEFAULT_COLOR_SET;

        // Initialize whether to use white text on a black background to false
        blackBackgroundP = false;

        // Initialize whether to draw a plot with annotations to false
        annotationP = false;

        // Initialize whether to draw the scale labels to false
        scaleLabelP = false;

        // Initialize whether to draw the scale ticks to false
        scaleTickP = false;

        // Initialize whether to draw the axis labels to false
        axisLabelP = false;

        // Initialize whether to use long labels to false
        longLabelP = false;

        // Initialize the number of axis bins on each axis to 256
        axisBins = 256;

        // Initialize the smoothing level of the smoothing function to use to
        // 1.0d
        smoothing = 1.0d;

        // Initialize the aspect ratio of the histogram to 1.0d
        aspectRatio = 1.0d;

        // Initialize the percentage in each level of the contour plot to 10.0d
        contourPercent = 10.0d;

        // Initialize the starting percentage of the contour plot to 10.0d
        contourStartPercent = 10.0d;

        /**
         * Population type parameters
         */

        // Initialize the type of the population to -1
        populationType = -1;

        // Initialize the number of events to get form the flow file to 10000
        eventCount = 10000;

        dotSize = 1;

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

        // Create the list of regions
        regions = new ArrayList();
    }

    /**
     * <p>
     * Returns the ID of the representation.
     * </p>
     *
     * @return int ID of the representation.
     */
    public int getID() {
        return id;
    }

    /**
     * <p>
     * Returns the name of the representation.
     * </p>
     *
     * @return <code>String</code> name of the representation.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Overrides the toString method to return the name of the representation.
     * </p>
     *
     * @return <code>String</code> name of the representation.
     */
    public String toString() {
        return getName();
    }

    /**
     * <p>
     * Sets the name of the representation to name.
     * </p>
     *
     * @param name
     * <code>String</code> name of the representation.</p>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     * Returns whether this representation is equal to the object in the
     * <code>Object</code> object obj.
     * </p>
     *
     * <p>
     * Comparison is performed by comparing the IDs of the two representations
     * if the <code>Object</code> object obj is a <code>Representation</code>
     * object.
     * </p>
     *
     * @param obj
     * <code>Object</code> object to the reference object with which
     * to compare.
     * @return boolean flag indicating whether this representation is equal to
     * the object in the <code>Object</code> object obj.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            // If the object is null, then return false.
            return false;
        }

        if (obj instanceof Representation) {
            // If the object is a representation, then cast it to a
            // representation.
            Representation rep = (Representation) obj;

            // Return whether the IDs are equal
            return (id == rep.id);
        } else {
            // Otherwise, the object is not a representation, so return false.
            return false;
        }
    }

    /**
     * <p>
     * Returns the ID of the representation as the hash code value for the
     * object.
     * </p>
     *
     * @return int ID of the representation.
     */
    public int hashCode() {
        return getID();
    }

    /**
     * <p>
     * Returns whether the representation is the default representation for a
     * panel set.
     * </p>
     *
     * @return boolean flag indicating whether the representation is the default
     * representation for a panel set.
     */
    public boolean isPanelDefaultRepresentation() {
        return panelDefaultRepresentationP;
    }

    /**
     * <p>
     * Sets whether the representation is the default representation for a panel
     * set to panelDefaultRepresentationP.
     * </p>
     *
     * @param panelDefaultRepresentationP
     * boolean flag indicating whether the representation is the
     * default representation for a panel set.
     */
    public void setPanelDefaultRepresentation(boolean panelDefaultRepresentationP) {
        this.panelDefaultRepresentationP = panelDefaultRepresentationP;
    }

    /**
     * <p>
     * Returns the number of channels in the representation.
     * </p>
     *
     * @return int number of channels in the representation.
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * <p>
     * Sets the number of channels in the representation to channelCount.
     * </p>
     *
     * <p>
     * The method only resizes the number of channels in the representation if
     * the number of channels is greater than the number of channels in the
     * representation.
     * </p>
     *
     * @param channelCount
     * int number of channels.
     */
    public void setChannelCount(int channelCount) {
        if (channelCount <= this.channelCount) {
            // If the number of channels is less than or equal to the number of
            // channels in the representation, then quit.
            return;
        }

        /**
         * Save the scale parameters
         */

        // Allocate the array of scale type flags
        int[] scaleFlagArray = new int[this.channelCount];

        // Copy the array of scale type flags
        System.arraycopy(scaleFlags, 0, scaleFlagArray, 0, this.channelCount);

        // Allocate the array of scale argument strings
        String[] scaleArgumentArray = new String[this.channelCount];

        // Copy the array of scale argument strings
        System.arraycopy(scaleArguments, 0, scaleArgumentArray, 0, this.channelCount);

        // Allocate the array of channel minimums
        double[] minimumArray = new double[this.channelCount];

        // Copy the array of channel minimums
        System.arraycopy(minimums, 0, minimumArray, 0, this.channelCount);

        // Allocate the array of channel maximums
        double[] maximumArray = new double[this.channelCount];

        // Copy the array of channel maximums
        System.arraycopy(maximums, 0, maximumArray, 0, this.channelCount);

        /**
         * Resize the scale parameters to the number of channels
         */

        // Allocate the array of scale type flags
        scaleFlags = new int[channelCount];

        // Allocate the array of scale argument strings
        scaleArguments = new String[channelCount];

        // Allocate the array of channel minimums
        minimums = new double[channelCount];

        // Allocate the array of channel maximums
        maximums = new double[channelCount];

        // Loop through the channels
        for (int i = 0; i < channelCount; i++) {
            // Initialize the scale type flag to 1
            scaleFlags[i] = 1;

            // Initialize the channel minimum to Double.NaN
            minimums[i] = Double.NaN;

            // Initialize the channel maximum to Double.NaN
            maximums[i] = Double.NaN;
        }

        /**
         * Restore the saved scale parameters
         */

        // Copy the array of scale type flags
        System.arraycopy(scaleFlagArray, 0, scaleFlags, 0, this.channelCount);

        // Copy the array of scale argument strings
        System.arraycopy(scaleArgumentArray, 0, scaleArguments, 0, this.channelCount);

        // Copy the array of channel minimums
        System.arraycopy(minimumArray, 0, minimums, 0, this.channelCount);

        // Copy the array of channel maximums
        System.arraycopy(maximumArray, 0, maximums, 0, this.channelCount);

        // Set the number of channels in the representation to the number of
        // channels
        this.channelCount = channelCount;
    }

    /**
     * Population parameter methods
     */

    /**
     * <p>
     * Returns the filename of the flow file used to create the representation.
     * </p>
     *
     * @return <code>String</code> filename of the flow file used to create the
     * representation.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * <p>
     * Sets the filename of the flow file used to create the representation to
     * filename.
     * </p>
     *
     * @param filename
     * <code>String</code> filename of the flow file used to create
     * the representation.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * <p>
     * Returns the ID of the compensation.
     * </p>
     *
     * @return int ID of the compensation.
     */
    public int getCompensationID() {
        return compensationID;
    }

    /**
     * <p>
     * Sets the ID of the compensation to id.
     * </p>
     *
     * @param id
     * int ID of the compensation.
     */
    public void setCompensationID(int id) {
        compensationID = id;
    }

    /**
     * <p>
     * Returns the array of gate set IDs.
     * </p>
     *
     * @return int array of gate set IDs.
     */
    public int[] getGateSetIDs() {
        // Get the number of gate set IDs in the list of gate set IDs
        final int size = gateSetIDs.size();

        // Allocate an array to hold all the gate set IDs
        int[] idArray = new int[size];

        Object obj;
        Integer id;

        // Copy the list of gate set IDs into the array of gate set IDs
        for (int i = 0; i < size; i++) {
            // Initialize the current gate set ID in the array of gate set IDs
            // to -1
            idArray[i] = -1;

            // Get the current object
            obj = gateSetIDs.get(i);

            if (obj instanceof Integer) {
                // If the current gate set ID is an Integer (and it should
                // always be), then get the current gate set ID.
                id = (Integer) obj;

                // Set the current gate set ID in the array of gate set IDs to
                // the current gate set ID
                idArray[i] = id.intValue();
            }
        }

        // Return the array of gate set IDs
        return idArray;
    }

    /**
     * <p>
     * Returns whether the ID id is in the list of gate set IDs.
     * </p>
     *
     * @param id
     * int ID to test.
     * @return boolean flag indicating whether the ID id is in the list of gate
     * set IDs.
     */
    public boolean containsGateSetID(int id) {
        if (id < 1) {
            // If the ID is invalid, then quit.
            return false;
        }

        // Create an Integer object of the ID
        Integer newID = new Integer(id);

        // Return whether the ID is in the list of gate set IDs
        return gateSetIDs.contains(newID);
    }

    /**
     * <p>
     * Adds the ID id to the list of gate set IDs.
     * </p>
     *
     * @param id
     * int ID to add to the list of gate set IDs.
     * @return true if the representation changed as a result of the call.
     */
    public boolean addGateSetID(int id) {
        if (id < 1) {
            // If the ID is invalid, then quit.
            return false;
        }

        // Create an Integer object of the ID
        Integer newID = new Integer(id);

        if (gateSetIDs.contains(newID)) {
            // If the ID is already in the list of gate set IDs, then quit.
            return false;
        } else {
            // Otherwise, the ID is not in the list of gate set IDs, so add it
            // to the list of gate set IDs.
            return gateSetIDs.add(newID);
        }
    }

    /**
     * <p>
     * Removes the ID id from the list of gate set IDs.
     * </p>
     *
     * @param id
     * int ID to remove from the list of gate set IDs.
     * @return true if the representation changed as a result of the call.
     */
    public boolean removeGateSetID(int id) {
        if (id < 1) {
            // If the ID is invalid, then quit.
            return false;
        }

        // Create an Integer object of the ID
        Integer newID = new Integer(id);

        // Remove the ID from the list of gate set IDs
        return gateSetIDs.remove(newID);
    }

    /**
     * <p>
     * Clears the list of gate set IDs.
     * </p>
     *
     * <p>
     * The method has the effect of making the population referenced by the
     * representation ungated.
     * </p>
     */
    public void clearGateSetIDs() {
        gateSetIDs.clear();
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
     * Sets the x channel to the channel indicated by the channel index channel.
     * </p>
     *
     * @param channel
     * int index of the x channel.
     */
    public void setXChannel(int channel) {
        if ((channel >= -1) && (channel < channelCount)) {
            // If the index of the x channel is valid, then set the x channel to
            // channel.
            this.xChannel = channel;
        }
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
     * Sets the y channel to the channel indicated by the channel index channel.
     * </p>
     *
     * @param channel
     * int index of the y channel.
     */
    public void setYChannel(int channel) {
        if ((channel >= -1) && (channel < channelCount)) {
            // If the index of the y channel is valid, then set the y channel to
            // channel.
            this.yChannel = channel;
        }
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
     * Sets the type of the plot to the type indicated by the constant flag of
     * the type of the plot type.
     * </p>
     *
     * @param type
     * int constant flag of the type of the plot.
     */
    public void setPlotType(int type) {
        // Set the type of the plot based on the constant flag
        switch (type) {
        case HISTOGRAM_X:
            this.plotType = HISTOGRAM_X;
            return;
        case HISTOGRAM_Y:
            this.plotType = HISTOGRAM_Y;
            return;
        case DOT_PLOT:
            this.plotType = DOT_PLOT;
            return;
        case DENSITY_DOT_PLOT:
            this.plotType = DENSITY_DOT_PLOT;
            return;
        case SHADOW_PLOT:
            this.plotType = SHADOW_PLOT;
            return;
        case CONTOUR_PLOT:
            this.plotType = CONTOUR_PLOT;
            return;
        case SHADED_CONTOUR_PLOT:
            this.plotType = SHADED_CONTOUR_PLOT;
            return;
        case DENSITY_PLOT:
            this.plotType = DENSITY_PLOT;
            return;
        case HISTOGRAM_OVERLAY:
            this.plotType = HISTOGRAM_OVERLAY;
            return;

            // Otherwise, set the type of the plot to TEST_PATTERN.
        default:
            this.plotType = TEST_PATTERN;
            return;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the type of the statistic.
     * </p>
     *
     * @return int constant flag of the type of the statistic.
     */
    public int getStatisticType() {
        return statType;
    }

    /**
     * <p>
     * Sets the type of the statistic to the type indicated by the constant flag
     * of the type of the statistic.
     * </p>
     *
     * @param type
     * int constant flag of the type of the statistic.
     */
    public void setStatisticType(int type) {
        // Set the type of the statistic based on the constant flag
        switch (type) {
        case MEAN:
            this.statType = MEAN;
            return;
        case MEDIAN:
            this.statType = MEDIAN;
            return;
        case STANDARD_DEVIATION:
            this.statType = STANDARD_DEVIATION;
            return;
        case VARIANCE:
            this.statType = VARIANCE;
            return;
        case MINIMUM:
            this.statType = MINIMUM;
            return;
        case MAXIMUM:
            this.statType = MAXIMUM;
            return;
        case PERCENT:
            this.statType = PERCENT;
            return;
        case EVENT_COUNT:
            this.statType = EVENT_COUNT;
            return;
        case CHANNEL_RANGE:
            this.statType = CHANNEL_RANGE;
            return;
        case GEOMETRIC_MEAN:
            this.statType = GEOMETRIC_MEAN;
            return;
        case STATISTIC_1D_X:
            this.statType = STATISTIC_1D_X;
            return;
        case STATISTIC_1D_Y:
            this.statType = STATISTIC_1D_Y;
            return;
        case STATISTIC_2D:
            this.statType = STATISTIC_2D;
            return;

            // Otherwise, set the type of the statistic to NO_STATISTIC.
        default:
            this.statType = NO_STATISTIC;
            return;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the color set.
     * </p>
     *
     * @return int constant flag of the color set.
     */
    public int getColorSet() {
        return colorSet;
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
        case EN_FUEGO:
            this.colorSet = EN_FUEGO;
            return;
        case CHILL_OUT:
            this.colorSet = CHILL_OUT;
            return;
        case RAINBOW:
            this.colorSet = RAINBOW;
            return;
        case GREYSCALE:
            this.colorSet = GREYSCALE;
            return;
        case PALE_FIRE:
            this.colorSet = PALE_FIRE;
            return;
        case GREEN_TO_RED:
            this.colorSet = GREEN_TO_RED;
            return;
        case BLUE_TO_YELLOW:
            this.colorSet = BLUE_TO_YELLOW;
            return;
        case RYAN1:
            this.colorSet = RYAN1;
            return;
        case WILLIAM1:
            this.colorSet = WILLIAM1;
            return;
        case NIKESH1:
            this.colorSet = NIKESH1;
            return;
        case MARK1:
            this.colorSet = MARK1;
            return;
        case PETER1:
            this.colorSet = PETER1;
            return;

            // Otherwise, set the color set to DEFAULT_COLORSET.
        default:
            this.colorSet = DEFAULT_COLOR_SET;
            return;
        }
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
     * Sets whether to use white text on a black background to blackBackgroundP.
     * </p>
     *
     * @param blackBackgroundP
     * boolean flag indicating whether to use white text on a black
     * background.
     */
    public void setBlackBackground(boolean blackBackgroundP) {
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
     * Sets whether to draw a plot with annotations to annotationP.
     * </p>
     *
     * @param annotationP
     * boolean flag indicating whether to draw a plot with
     * annotations.
     */
    public void setAnnotation(boolean annotationP) {
        this.annotationP = annotationP;
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
     * Sets whether to draw the scale labels to scaleLabelP.
     * </p>
     *
     * @param scaleLabelP
     * boolean flag indicating whether to draw the scale labels.
     */
    public void setScaleLabel(boolean scaleLabelP) {
        this.scaleLabelP = scaleLabelP;
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
     * Sets whether to draw the scale ticks to scaleTickP.
     * </p>
     *
     * @param scaleTickP
     * boolean flag indicating whether to draw the scale ticks.
     */
    public void setScaleTick(boolean scaleTickP) {
        this.scaleTickP = scaleTickP;
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
     * Sets whether to draw the axis labels to axisLabelP.
     * </p>
     *
     * @param axisLabelP
     * boolean flag indicating whether to draw the axis labels.
     */
    public void setAxisLabel(boolean axisLabelP) {
        this.axisLabelP = axisLabelP;
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
     * Sets whether to use long labels to longLabelP.
     * </p>
     *
     * @param longLabelP
     * boolean flag indicating whether to use long labels.
     */
    public void setLongLabel(boolean longLabelP) {
        this.longLabelP = longLabelP;
    }

    /**
     * <p>
     * Returns the number of axis bins on each axis.
     * </p>
     *
     * @return int number of axis bins on each axis.
     */
    public int getAxisBins() {
        return axisBins;
    }

    /**
     * <p>
     * Sets the number of axis bins on each axis to bins.
     * </p>
     *
     * @param bins
     * int number of axis bins on each axis.
     */
    public void setAxisBins(int bins) {
        if (bins >= 0) {
            // If the number of axis bins on each axis is greater than or equal
            // to 0, then set the number of axis bins on each axis.
            axisBins = bins;
        }
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
     * Sets the smoothing level of the smoothing function to use to smoothing.
     * </p>
     *
     * @param smoothing
     * double smoothing level of the smoothing function to use.
     */
    public void setSmoothing(double smoothing) {
        this.smoothing = smoothing;
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
     * Sets the aspect ratio of the histogram to aspectRatio.
     * </p>
     *
     * @param aspectRatio
     * double aspect ratio of the histogram.
     */
    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
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
     * Sets the percentage in each level of the contour plot to contourPercent.
     * </p>
     *
     * @param contourPercent
     * double percentage in each level of the contour plot.
     */
    public void setContourPercent(double contourPercent) {
        this.contourPercent = contourPercent;
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
     * <p>
     * Sets the starting percentage of the contour plot to contourStartPercent.
     * </p>
     *
     * @param contourStartPercent
     * double starting percentage of the contour plot.
     */
    public void setContourStartPercent(double contourStartPercent) {
        this.contourStartPercent = contourStartPercent;
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
     * Sets the constant flag of the type of the population to the type
     * indicated by the constant flag of the type of the population.
     * </p>
     *
     * @param type
     * int constant flag of the type of the population.
     */
    public void setPopulationType(int type) {
        populationType = type;
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

    public int getDotSize() {
        return dotSize;
    }

    public void setDotSize(int dotSize) {
        this.dotSize = dotSize;
    }

    /**
     * <p>
     * Sets the number of events to get from the flow file to eventCount.
     * </p>
     *
     * @param eventCount
     * int number of events to get from the flow file.
     */
    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    /**
     * Scale parameter methods
     */

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
     * Region parameter methods
     */

    /**
     * <p>
     * Creates a new region with ID id and adds it to the representation. The
     * resulting region is returned or null is returned if the ID is invalid.
     * </p>
     *
     * @param id
     * int ID of the region to add.
     * @return <code>Representation.Region</code> object to the region with ID
     * id or null if the ID is invalid.
     */
    public Region addRegion(int id) {
        if (id < ALL_REGIONS) {
            // If the ID of the region is invalid, then quit.
            return null;
        }

        // Get the region with ID id
        Region region = getRegion(id);

        if (region == null) {
            // If the region is null, then the region with ID id does not exist,
            // so create it.
            region = new Region(id);

            // Add the region to the list of regions
            regions.add(region);

            if (regions.size() > 1) {
                // If there are more than one region in the list of regions,
                // then sort it.
                Collections.sort(regions);
            }
        }

        // Return the region
        return region;
    }

    /**
     * <p>
     * Removes the region with ID id from the representation.
     * </p>
     *
     * @param id
     * int ID of the region to remove.
     * @return true if the representation changed as a result of the call.
     */
    public boolean removeRegion(int id) {
        if (id < ALL_REGIONS) {
            // If the ID of the region is invalid, then quit.
            return false;
        }

        /**
         * Look for the region with ID id in the list of regions
         */
        Object object;
        Region region;

        // Loop through the list of regions
        for (int i = 0, n = regions.size(); i < n; i++) {
            // Get the current object in the list of regions
            object = regions.get(i);

            if (object instanceof Region) {
                // If the current object in the list of regions is a region,
                // then cast it to a region.
                region = (Region) object;

                if (region.getID() == id) {
                    // If the ID of the current region is equal to the ID of the
                    // region, then we have found the region, so return it.
                    return regions.remove(region);
                }
            }
        }

        // At this point, the region with ID id was not found in the list of
        // regions, so return false.
        return false;
    }

    /**
     * <p>
     * Returns the region with ID id or null if the ID is invalid or the region
     * does not exist.
     * </p>
     *
     * @param id
     * int ID of the region.
     * @return <code>Representation.Region</code> object to the region with ID
     * id or null if the ID is invalid or the region does not exist.
     */
    public Region getRegion(int id) {
        if (id < ALL_REGIONS) {
            // If the ID of the region is invalid, then quit.
            return null;
        }

        /**
         * Look for the region with ID id in the list of regions
         */
        Object object;
        Region region;

        // Loop through the list of regions
        for (int i = 0, n = regions.size(); i < n; i++) {
            // Get the current object in the list of regions
            object = regions.get(i);

            if (object instanceof Region) {
                // If the current object in the list of regions is a region,
                // then cast it to a region.
                region = (Region) object;

                if (region.getID() == id) {
                    // If the ID of the current region is equal to the ID of the
                    // region, then we have found the region, so return it.
                    return region;
                }
            }
        }

        // At this point, the region with ID id was not found in the list of
        // regions, so return null.
        return null;
    }

    /**
     * <p>
     * Return the array of regions in the representation.
     * </p>
     *
     * @return <code>Representation.Region</code> array of regions in the
     * representation.
     */
    public Region[] getRegions() {
        // Allocate an array to hold all the regions
        Region[] regionArray = new Region[regions.size()];

        // Copy the list of regions into the array of regions
        regions.toArray(regionArray);

        // Return the array of regions
        return regionArray;
    }

    /**
     * <p>
     * Clears the regions in the representation.
     * </p>
     */
    public void clearRegions() {
        regions.clear();
    }

    /**
     * Encode methods
     */

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * representation.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @return <code>String</code> string encoding the plot parameters,
     * population type parameters, the scale parameters, and the region
     * parameters of the representation.
     */
    public String getParameters() {
        return getParameters(xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * representation using the channel in xChannel as the x channel and the
     * channel in yChannel as the y channel.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @return <code>String</code> string encoding the plot parameters,
     * population type parameters, the scale parameters, and the region
     * parameters of the representation.
     */
    public String getParameters(int xChannel, int yChannel) {
        return getParameters(xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * representation using the channel in xChannel as the x channel and the
     * channel in yChannel as the y channel. The type of the population
     * indicated by the constant flag populationType and the number of events to
     * get from the flow file eventCount are used as the population type
     * parameters.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the plot parameters,
     * population type parameters, the scale parameters, and the region
     * parameters of the representation.
     */
    public String getParameters(int xChannel, int yChannel, int populationType, int eventCount) {
        if (populationType <= 0) {
            // If the type of the population is invalid, then use the type of
            // the population of the representation.
            populationType = this.populationType;
            eventCount = this.eventCount;
        }

        if ((xChannel < -1) || (xChannel >= channelCount)) {
            // If the x channel is invalid, then use the x channel of the
            // representation.
            xChannel = this.xChannel;
        }

        if ((yChannel < -1) || (yChannel >= channelCount)) {
            // If the y channel is invalid, then use the y channel of the
            // representation.
            yChannel = this.yChannel;
        }

        /**
         * Encode the parameters
         */

        // Create a StringBuffer with which to encode the parameters
        StringBuffer parameters = new StringBuffer();

        /**
         * Encode the channels
         */

        // Encode the x channel
        parameters.append("xChannel=");
        parameters.append(xChannel);

        // Encode the y channel
        parameters.append("&yChannel=");
        parameters.append(yChannel);

        /**
         * Encode the plot parameters
         */

        // Encode the type of the plot
        parameters.append("&plotType=");

        if (yChannel == -1) {
            // If the y channel is -1, then encode the histogram of the x-axis
            // as the type of the plot.
            parameters.append(HISTOGRAM_X);
        } else if (xChannel == -1) {
            // If the x channel is -1, then encode the histogram of the y-axis
            // as the type of the plot.
            parameters.append(HISTOGRAM_Y);
        } else {
            // Otherwise, the x channel and the y channel are not -1, so just
            // encode the type of the plot.
            parameters.append(plotType);
        }

        // Encode the type of the statistic
        parameters.append("&statType=");
        parameters.append(statType);

        // Encode the color set
        parameters.append("&colorSet=");
        parameters.append(colorSet);

        // Encode whether to use white text on a black background
        parameters.append("&blackBackground=");
        parameters.append(blackBackgroundP);

        // Encode whether to draw a plot with annotations
        parameters.append("&annotation=");
        parameters.append(annotationP);

        if (annotationP) {
            // If the plot should be drawn with annotations, then encode the
            // other boolean flags.

            // Encode whether to draw the scale labels
            parameters.append("&scaleLabel=");
            parameters.append(scaleLabelP);

            // Encode whether to draw the scale ticks
            parameters.append("&scaleTick=");
            parameters.append(scaleTickP);

            // Encode whether to draw the axis labels
            parameters.append("&axisLabel=");
            parameters.append(axisLabelP);

            // Encode whether to use long labels
            parameters.append("&longLabel=");
            parameters.append(longLabelP);
        }

        // Encode the number of axis bins on each axis
        parameters.append("&axisBins=");
        parameters.append(axisBins);

        // Encode the smoothing level of the smoothing function to use
        parameters.append("&smoothing=");
        parameters.append(smoothing);

        // Encode the aspect ratio of the histogram
        parameters.append("&aspectRatio=");
        parameters.append(aspectRatio);

        // Encode the percentage in each level of the contour plot
        parameters.append("&contourPercent=");
        parameters.append(contourPercent);

        // Encode the starting percentage of the contour plot
        parameters.append("&contourStartPercent=");
        parameters.append(contourStartPercent);

        /**
         * Encode the population type parameters
         */

        if (populationType > 0) {
            // If the type of the population is valid, then encode it and the
            // number of events.
            parameters.append("&populationType=");
            parameters.append(populationType);

            parameters.append("&eventCount=");
            parameters.append(eventCount);
        }

        /**
         * Encode the scale parameters
         */

        // Encode the number of channels
        parameters.append("&channelCount=");
        parameters.append(channelCount);

        // Loop through the channels
        for (int i = 0; i < channelCount; i++) {
            // Encode the scale type flag of the scale of the current channel
            parameters.append("&scale");
            parameters.append(i);
            parameters.append("=");
            parameters.append(scaleFlags[i]);

            if (scaleArguments[i] != null) {
                // If the scale argument string of the scale argument of the
                // scale of the current channel is not null, then encode it.
                parameters.append("&scaleArg");
                parameters.append(i);
                parameters.append("=");
                parameters.append(encode(scaleArguments[i]));
            }

            if (!Double.isNaN(minimums[i])) {
                // If the channel minimum of the current channel is a number,
                // then encode it.
                parameters.append("&min");
                parameters.append(i);
                parameters.append("=");
                parameters.append(minimums[i]);
            }

            if (!Double.isNaN(maximums[i])) {
                // If the channel maximum of the current channel is a number,
                // then encode it.
                parameters.append("&max");
                parameters.append(i);
                parameters.append("=");
                parameters.append(maximums[i]);
            }
        }

        /**
         * Encode the region parameters
         */

        if (annotationP) {
            // If the plot should be drawn with annotations, then encode the
            // region parameters.

            // Get the array of regions
            Region[] regionArray = getRegions();

            // Loop through the array of regions
            for (int i = 0; i < regionArray.length; i++) {
                // Encode the current region and append it to the parameters
                parameters.append(regionArray[i].encode());
            }
        }

        // Return the String representation of the StringBuffer
        return parameters.toString();
    }

    /**
     * <p>
     * Returns a string encoding the representation.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * <p>
     * Since the representation does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @return <code>String</code> string encoding the representation.
     */
    public String encode() {
        return encode(filename, compensationID, getGateSetIDs(), xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the representation with the filename replaced
     * by the filename in filename, the ID of the compensation replaced by the
     * ID in compensationID, the array of gate set IDs replaced by the IDs in
     * gateSetIDs.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * <p>
     * Since the representation does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param filename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @return <code>String</code> string encoding the representation with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
    public String encode(String filename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel) {
        return encode(filename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the representation with the filename replaced
     * by the filename in filename, the ID of the compensation replaced by the
     * ID in compensationID, the array of gate set IDs replaced by the IDs in
     * gateSetIDs. The type of the population indicated by the constant flag
     * populationType and the number of events to get from the flow file
     * eventCount are used as the population type parameters.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * <p>
     * Since the representation does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param filename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the representation with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
    public String encode(String filename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int populationType, int eventCount) {
        // Create a StringBuffer with which to encode the parameters
        StringBuffer parameters = new StringBuffer();

        // Encode the filename
        parameters.append("filename=");
        parameters.append(encode(filename));

        // Encode the ID of the compensation
        parameters.append("&compensationID=");
        parameters.append(compensationID);

        if (gateSetIDs != null) {
            // If the array of gate set IDs is not null, then encode them.

            // Loop through the array of gate set IDs
            for (int i = 0; i < gateSetIDs.length; i++) {
                parameters.append("&gateSetID=");
                parameters.append(gateSetIDs[i]);
            }
        }

        // Encode the representation parameters
        parameters.append("&");
        parameters.append(getParameters(xChannel, yChannel, populationType, eventCount));

        // Return the String representation of the StringBuffer
        return parameters.toString();
    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * representation with the filename replaced by the filename in filename,
     * the ID of the compensation replaced by the ID in compensationID, the
     * array of gate set IDs replaced by the IDs in gateSetIDs.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @param experimentID
     * int ID of the experiment.
     * @param filename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the representation with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs.
     */
    public String encode(int experimentID, String filename, int compensationID, int[] gateSetIDs) {
        return encode(experimentID, filename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * representation with the filename replaced by the filename in filename,
     * the ID of the compensation replaced by the ID in compensationID, the
     * array of gate set IDs replaced by the IDs in gateSetIDs.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @param experimentID
     * int ID of the experiment.
     * @param filename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the representation with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs.
     */
    public String encode(int experimentID, String filename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel) {
        return encode(experimentID, filename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * representation with the filename replaced by the filename in filename,
     * the ID of the compensation replaced by the ID in compensationID, the
     * array of gate set IDs replaced by the IDs in gateSetIDs. The type of the
     * population indicated by the constant flag populationType and the number
     * of events to get from the flow file eventCount are used as the population
     * type parameters.
     * </p>
     *
     * <p>
     * The encoded parameters do not start with a '?' or a '&' so it is suitable
     * to be concatenated into an URL as the only parameters or as additional
     * parameters. It was designed this way so the client would have the option
     * to use the parameters in either capacity.
     * </p>
     *
     * @param experimentID
     * int ID of the experiment.
     * @param filename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the representation with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs. The type of the population indicated by
     * the constant flag populationType and the number of events to get
     * from the flow file eventCount are used as the population type
     * parameters.
     */
    public String encode(int experimentID, String filename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int populationType, int eventCount) {
        // Create a StringBuffer with which to encode the parameters
        StringBuffer parameters = new StringBuffer();

        // Encode the ID of the experiment
        parameters.append("experimentID=");
        parameters.append(experimentID);

        // Encode the representation
        parameters.append("&");
        parameters.append(encode(filename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount));

        // Return the String representation of the StringBuffer
        return parameters.toString();
    }

    /**
     * Factory methods
     */

    /**
     * <p>
     * Returns a representation created based on the input parameters.
     * </p>
     *
     * <p>
     * The factory method may be overkill for something like a representation
     * since it is only a bag of settings, all of which can be seen with
     * accessors and changed with mutators, but by funneling the creation of
     * representation through the factory method, it makes it easier to change
     * later when that is no longer the case.
     * </p>
     *
     * @param id
     * int ID of the representation.
     * @param name
     * <code>String</code> name of the representation.
     * @param channelCount
     * int number of channels.
     * @param filename
     * <code>String</code> filename of the flow file used to create
     * the representation.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param plotType
     * int constant flag of the type of the plot.
     * @param statType
     * int constant flag of the type of the statistic.
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
     * @return <code>Representation</code> object to the representation created
     * based on the input parameters.
     */
    public static Representation getRepresentation(int id, String name, int channelCount, String filename, int compensationID, int[] gateSetIDs, int xChannel,
            int yChannel, int plotType, int statType, int colorSet, boolean blackBackgroundP, boolean annotationP, boolean scaleLabelP, boolean scaleTickP,
            boolean axisLabelP, boolean longLabelP, int axisBins, double smoothing, double aspectRatio, double contourPercent, double contourStartPercent) {

        // Create the Representation object
        Representation rep = new Representation(id, name, channelCount);

        // Set the filename
        rep.setFilename(filename);

        // Set the ID of the compensation
        rep.setCompensationID(compensationID);

        if (gateSetIDs != null) {
            // If the array of gate set IDs is not null, then add them.

            // Loop through the array of gate set IDs
            for (int i = 0; i < gateSetIDs.length; i++) {
                rep.addGateSetID(gateSetIDs[i]);
            }
        }

        // Set the x channel
        rep.setXChannel(xChannel);

        // Set the y channel
        rep.setYChannel(yChannel);

        // Set the type of the plot
        rep.setPlotType(plotType);

        // Set the type of the statistic
        rep.setStatisticType(statType);

        // Set the color set
        rep.setColorSet(colorSet);

        // Set whether to use white text on a black background
        rep.setBlackBackground(blackBackgroundP);

        // Set whether to draw a plot with annotations
        rep.setAnnotation(annotationP);

        // Set whether to draw the scale labels
        rep.setScaleLabel(scaleLabelP);

        // Set whether to draw the scale ticks
        rep.setScaleTick(scaleTickP);

        // Set whether to draw the axis labels
        rep.setAxisLabel(axisLabelP);

        // Set whether to use long labels
        rep.setLongLabel(longLabelP);

        // Set the number of axis bins on each axis
        rep.setAxisBins(axisBins);

        // Set the smoothing level of the smoothing function to use
        rep.setSmoothing(smoothing);

        // Set the aspect ratio of the histogram
        rep.setAspectRatio(aspectRatio);

        // Set the percentage in each level of the contour plot
        rep.setContourPercent(contourPercent);

        // Set the starting percentage of the contour plot
        rep.setContourStartPercent(contourStartPercent);

        // Return the Representation object
        return rep;
    }

    /**
     * <p>
     * Returns a representation created based on the input parameters.
     * </p>
     *
     * <p>
     * The factory method may be overkill for something like a representation
     * since it is only a bag of settings, all of which can be seen with
     * accessors and changed with mutators, but by funneling the creation of
     * representation through the factory method, it makes it easier to change
     * later when that is no longer the case.
     * </p>
     *
     * @param id
     * int ID of the representation.
     * @param name
     * <code>String</code> name of the representation.
     * @param channelCount
     * int number of channels.
     * @param filename
     * <code>String</code> filename of the flow file used to create
     * the representation.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param plotType
     * int constant flag of the type of the plot.
     * @param statType
     * int constant flag of the type of the statistic.
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
     * @return <code>Representation</code> object to the representation created
     * based on the input parameters.
     */
    public static Representation getRepresentation(int id, String name, int channelCount, String filename, int compensationID, int[] gateSetIDs, int xChannel,
            int yChannel, int plotType, int statType, int colorSet, boolean blackBackgroundP, boolean annotationP, boolean scaleLabelP, boolean scaleTickP,
            boolean axisLabelP, boolean longLabelP, int axisBins, double smoothing, double aspectRatio, double contourPercent, double contourStartPercent,
            int populationType, int eventCount) {

        // Create the Representation object
        Representation rep = Representation.getRepresentation(id, name, channelCount, filename, compensationID, gateSetIDs, xChannel, yChannel, plotType,
                statType, colorSet, blackBackgroundP, annotationP, scaleLabelP, scaleTickP, axisLabelP, longLabelP, axisBins, smoothing, aspectRatio,
                contourPercent, contourStartPercent);

        // Set the type of the population
        rep.setPopulationType(populationType);

        // Set number of events to get from the flow file
        rep.setEventCount(eventCount);

        // Return the Representation object
        return rep;
    }

    /**
     * Static helper methods
     */

    /**
     * <p>
     * Returns the string in the <code>String</code> str encoded using "UTF-8"
     * encoding.
     * </p>
     *
     * <p>
     * This method is a convenience method for encoding strings over URLs.
     * </p>
     *
     * <p>
     * This method removes the need to remember the exact encoding to use for
     * URLs and simplifies the encoding process because it is a common mistake
     * to forget to specify the "UTF-8" encoding when using the URLEncoder,
     * which calls the deprecated version of the encode method that uses the
     * platform's default encoding. This may cause problems later.
     * </p>
     *
     * <p>
     * If a <code>java.io.UnsupportedEncodingException</code> occurred because
     * "UTF-8" encoding is not supported, then the method returns null. This is
     * a bigger problem since "UTF-8" should be supported according to the W3C.
     * </p>
     *
     * @param str
     * <code>String</code> string to encode using "UTF-8" encoding.
     * @return <code>String</code> string in the <code>String</code> str encoded
     * using "UTF-8" encoding..
     */
    private static String encode(String str) {
        if (str == null) {
            // If the string is null, then return null.
            return null;
        }

        try {
            // Try to encode the string using "UTF-8" encoding
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            // If an UnsupportedEncodingException occurred, then return null.
            return null;
        }
    }

    /**
     * Testing methods
     */

    /**
     * <p>
     * A main method to test the class.
     * </p>
     *
     * @param args
     * <code>String</code> array of arguments at the command prompt.
     */
    public static void main(String[] args) {
        // Create a representation for testing
        Representation rep = new Representation(1, "Representation 1", 8);

        // Print out the encoded representation
        System.out.println(rep.encode());

        // Set the ID of the compensation
        rep.setCompensationID(3);

        rep.setScaleArgumentString(0, "Zero");
        rep.setScaleArgumentString(1, "One");
        rep.setScaleArgumentString(2, "Two");
        rep.setScaleArgumentString(3, "blah");
        rep.setMinimum(6, 10.0d);

        // Print out the encoded representation
        System.out.println(rep.encode());

        System.out.println("Scale parameters before resizing");
        System.out.println("---");

        // Loop through the channels
        for (int i = 0; i < 8; i++) {
            System.out.println("Channel " + i + ": ");
            System.out.println("\tScale flag: " + rep.getScaleFlag(i));
            System.out.println("\tScale argument string: " + rep.getScaleArgumentString(i));
            System.out.println("\tMinimum: " + rep.getMinimum(i));
            System.out.println("\tMaximum: " + rep.getMaximum(i));
        }

        // Set the number of channels in the representation to 11
        rep.setChannelCount(11);

        rep.setScaleFlag(9, 3);
        rep.setScaleArgumentString(9, "Nine");
        rep.setScaleArgumentString(10, "Ten");

        System.out.println("Scale parameters after resizing");
        System.out.println("---");

        // Loop through the channels
        for (int i = 0; i < 11; i++) {
            System.out.println("Channel " + i + ": ");
            System.out.println("\tScale flag: " + rep.getScaleFlag(i));
            System.out.println("\tScale argument string: " + rep.getScaleArgumentString(i));
            System.out.println("\tMinimum: " + rep.getMinimum(i));
            System.out.println("\tMaximum: " + rep.getMaximum(i));
        }
    }
}