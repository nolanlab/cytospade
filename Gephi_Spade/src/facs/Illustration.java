/**
 * Illustration.java
 *
 * The Illustration class stores the settings to visualize a illustration
 *
 * Butchered into a local class (removed XML encoding, etc.) <i>ZB 11/5/2010</i>
 *
 * Illustration needs to be able to serialize and deserialize itself as both a
 * querystring and an XML file.
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

// Import the utility package
import java.util.*;

// Import the arrays
import java.util.Arrays.*;

// Use JDOM to manipulate the XML.
//import org.jdom.*;

// Import the Serializable class
import java.io.Serializable;

// Import the request so we can parse parameters
//import javax.servlet.http.HttpServletRequest;

// Import the membership package
//import facs.membership.*;

// Import the scaling package
import facs.scale.*;

// Import the io package exceptions

public class Illustration {

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Plot type constant flags
     *
     * The type of the plot for each populations , assuming it is not overridden
     * by a layoutOverride type.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the test pattern, a default plot that indicates
     * that something is wrong with plotting that population.
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
     * The constant flag for the density dot plot, which calculates the density
     * of every single dot (bin) without smoothing
     */
    public static final int DENSITY_DOT_PLOT = 12;

    /**
     * The constant flag for the shadow plot, which shades the plot according to
     * the requested contour levels (this is what you get if you do a shaded
     * contour plot without drawing in the black contour lines).
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
     * The constant flag for the density plot, which is a smoothed dot plot
     */
    public static final int DENSITY_PLOT = 16;

    /**
     * The constant flag plot type of histogram overlays
     */
    public static final int HISTOGRAM_OVERLAY_PLOT_TYPE = 101;

    /**
     * The constant flag for a pseudo-3D median plot, which plots the median of
     * the z-axis for every dot (bin) as an intensity value
     */
    public static final int THIRD_AXIS_MEDIAN_PLOT = 201;

    /**
     * The constant flag for a pseudo-3D 95th-ile plot, which plots the 95th
     * percentile of the z-axis for every dot (bin) as an intensity value
     */
    public static final int THIRD_AXIS_NINETYFIFTH_PLOT = 202;

    /**
     * A constant array of valid plot types
     *
     * If you make a new plot type, put it in here or it won't be considered
     * valid when setting the plot type with setPlotType()
     */
    public static final int[] VALID_PLOT_TYPES = new int[] { HISTOGRAM_X, HISTOGRAM_Y, DOT_PLOT, DENSITY_DOT_PLOT, SHADOW_PLOT, CONTOUR_PLOT,
            SHADED_CONTOUR_PLOT, DENSITY_PLOT, THIRD_AXIS_MEDIAN_PLOT, THIRD_AXIS_NINETYFIFTH_PLOT, HISTOGRAM_OVERLAY_PLOT_TYPE };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Plot size constant flags
     *
     * By convention plot sizes are usually factors of 1,024. This originally
     * was determined by the binning of early flow cytometry data, which was log
     * amplified and then binned into 1,024 bins. Plotting the data on a scale
     * that was not a factor of 1,024 caused striping or discontinuities due to
     * rounding.
     *
     * Since then, data have been stored as raw (linear scale) values ranging
     * from various negative numbers to 262,144 and displayed on scales that are
     * different from the linear storage scale (e.g. arcsinh, biexp). It no
     * longer matters whether the plot sizes are multiples of 1,024, but the
     * convention persists because these sizes cover the range well, match what
     * people are used to seeing, and make it easy to keep the size of a set of
     * plots uniform.
     *
     * Note that Cytobank bins the data to the size of the plot, which is not
     * necessarily how other flow cytometry programs do it.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the nano plot (32px)
     */
    public static final int NANO = 32;

    /**
     * The constant flag for the micro plot (64px)
     */
    public static final int MICRO = 64;

    /**
     * The constant flag for the tiny plot (96px)
     */
    public static final int TINY = 96;

    /**
     * The constant flag for the small plot (128px)
     */
    public static final int SMALL = 128;

    /**
     * The constant flag for the regular plot (256px)
     */
    public static final int REGULAR = 256;

    /**
     * The constant flag for the large plot (512px)
     */
    public static final int LARGE = 512;

    /**
     * The constant flag for the huge plot (1024px)
     */
    public static final int HUGE = 1024;

    /**
     * A constant array of valid plot sizes
     *
     * If you make a new plot size, put it in here or it won't be considered
     * valid when setting the plot size with setPlotSize()
     */
    public static final int[] VALID_PLOT_SIZES = new int[] { NANO, MICRO, TINY, SMALL, REGULAR, LARGE, HUGE };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Variable channel constant flags
     *
     * Constant flags used to refer to channels when seeing which channel is
     * variable
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag indicating that we can't find a variable channel
     */
    public static final int VARY_NONE = -1;

    /**
     * The constant flag indicating the x channel is variable
     */
    public static final int VARY_X = 0;

    /**
     * The constant flag indicating the y channel is variable
     */
    public static final int VARY_Y = 1;

    /**
     * The constant flag indicating the z channel is variable
     */
    public static final int VARY_Z = 2;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Edit status constant flags
     *
     * The edit status is used to control who can view or edit an Illustration.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant no edit status flag
     */
    public static final int EDIT_NO_STATUS = -1;

    /**
     * The constant creator only edit status flag
     */
    public static final int EDIT_BY_CREATOR = 1;

    /**
     * The constant coauthors only edit status flag
     *
     * Anyone with full control of the experiment can edit the figure
     */
    public static final int EDIT_BY_COAUTHORS = 2;

    /**
     * The constant lab only edit status flag
     */
    public static final int EDIT_BY_LAB = 3;

    /**
     * The constant public edit status flag
     */
    public static final int EDIT_BY_PUBLIC = 4;

    /**
     * A constant array of valid edit flags
     */
    public static final int[] VALID_EDIT_STATUS = new int[] { EDIT_NO_STATUS, EDIT_BY_CREATOR, EDIT_BY_COAUTHORS, EDIT_BY_LAB, EDIT_BY_PUBLIC };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * View status constant flags
     *
     * The view status is used to control who can view an illustration.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant no view status flag
     */
    public static final int VIEW_NO_STATUS = -1;

    /**
     * The constant creator only view status flag
     */
    public static final int VIEW_BY_CREATOR = 1;

    /**
     * The constant coauthors only view status flag
     *
     * Anyone with full control of the experiment can view the figure
     */
    public static final int VIEW_BY_COAUTHORS = 2;

    /**
     * The constant lab only view status flag
     */
    public static final int VIEW_BY_LAB = 3;

    /**
     * The constant public view status flag
     */
    public static final int VIEW_BY_PUBLIC = 4;

    /**
     * A constant array of valid view flags
     */
    public static final int[] VALID_VIEW_STATUS = new int[] { VIEW_NO_STATUS, VIEW_BY_CREATOR, VIEW_BY_COAUTHORS, VIEW_BY_LAB, VIEW_BY_PUBLIC };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Citation Format constant flags
     *
     * The citation indicates who did the work and when and is usually shown in
     * an unobtrusive way (e.g. a small footer on the bottom right).
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for no citation
     */
    public static final int NO_CITATION = -1;

    /**
     * The constant flag to cite just the date
     */
    public static final int CITE_DATE = 1;

    /**
     * The constant flag to cite just the PR
     */
    public static final int CITE_PR = 2;

    /**
     * The constant flag to cite the PR and PI
     */
    public static final int CITE_PRPI = 3;

    /**
     * The constant flag to cite all authors
     */
    public static final int CITE_ALL = 4;

    /**
     * The constant flag to cite the PR and date
     */
    public static final int CITE_PR_DATE = 5;

    /**
     * The constant flag to cite the PR, PI, and date
     */
    public static final int CITE_PRPI_DATE = 6;

    /**
     * The constant flag to cite all authors and date
     */
    public static final int CITE_ALL_DATE = 7;

    /**
     * The constant flag to cite the PI
     */
    public static final int CITE_PI = 8;

    /**
     * The constant flag to cite PI and date
     */
    public static final int CITE_PI_DATE = 9;

    /**
     * A constant array of valid citation formats
     */
    public static final int[] VALID_CITATION_TYPES = new int[] { CITE_DATE, CITE_PR, CITE_PRPI, CITE_ALL, CITE_PR_DATE, CITE_PRPI_DATE, CITE_ALL_DATE, CITE_PI,
            CITE_PI_DATE };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Statistic type constant flags
     *
     * The type of statistic used to compare populations in a illustration.
     *
     * ***********************************************************************
     * ***********************************************************************
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
     * A constant array of valid statistic types
     */
    public static final int[] VALID_STATISTICS = new int[] { MEAN, MEDIAN, STANDARD_DEVIATION, VARIANCE, MINIMUM, MAXIMUM, PERCENT, EVENT_COUNT, CHANNEL_RANGE,
            GEOMETRIC_MEAN, STATISTIC_1D_X, STATISTIC_1D_Y, STATISTIC_2D };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Color set constant flags
     *
     * Constant flags that are used to indicate to the CanvasSettings which
     * color set Color[] to use.
     *
     * If you are a developer, feel free to add in your own color set in the
     * CanvasSettings.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

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
     * The constant flag for the jonathan1 color set
     */
    public static final int JONATHAN1 = EN_FUEGO;

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
     * The constant flag for the green color set
     */
    public static final int GREEN = 201;

    /**
     * The constant flag for the red color set
     */
    public static final int RED = 202;

    /**
     * The constant flag for the yellow color set
     */
    public static final int YELLOW = 203;

    /**
     * The constant flag for the blue color set
     */
    public static final int BLUE = 204;

    /**
     * The constant flag for the fuscia color set
     */
    public static final int FUSCIA = 205;

    /**
     * The constant flag for the cyan color set
     */
    public static final int CYAN = 206;

    /**
     * The constant flag for the heterogeneity on fire color set
     */
    public static final int HETEROGENEITY_ON_FIRE = 301;

    /**
     * A constant array of valid plot color set flags
     */
    public static final int[] VALID_COLOR_SETS = new int[] { EN_FUEGO, CHILL_OUT, RAINBOW, GREYSCALE, PALE_FIRE, GREEN_TO_RED, BLUE_TO_YELLOW, JONATHAN1,
            RYAN1, WILLIAM1, NIKESH1, PETER1, MARK1, GREEN, RED, YELLOW, BLUE, FUSCIA, CYAN, HETEROGENEITY_ON_FIRE };

    /**
     * A constant array of valid gradient color set flags
     */
    public static final int[] VALID_GRADIENT_COLOR_SETS = new int[] { EN_FUEGO };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Menu style constant flags
     *
     * Constant flags that are used to indicate to the illustration controls
     * what style of menu the user wants to see (verbose, compact, etc.).
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the menu style for newbs
     */
    public static final int NOOB = 0;

    /**
     * The constant flag for the menu style for the tutorial
     */
    public static final int TUTORIAL = 1;

    /**
     * The constant flag for the menu style for the standard
     */
    public static final int STANDARD_DIMENSIONS = 2;

    /**
     * The constant flag for the menu style for the standard
     */
    public static final int STANDARD_SCALES = 3;

    /**
     * The constant flag for the menu style for the compact
     */
    public static final int COMPACT = 4;

    /**
     * The constant flag for the menu style for both the dimensions and the
     * scales
     */
    public static final int STANDARD_BOTH = 5;

    /**
     * A constant array of valid menu style flags
     */
    public static final int[] VALID_MENU_STYLES = new int[] { NOOB, TUTORIAL, STANDARD_DIMENSIONS, STANDARD_SCALES, COMPACT, STANDARD_BOTH };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Illustration Page constant flags
     *
     * Constants indicating a set of pages within cytobank that are using
     * Illustration.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the main illustration page, the default.
     */
    public static final int ILLUSTRATION_PAGE = -1;

    /**
     * The constant flag for single plot page
     */
    public static final int SINGLE_PLOT_PAGE = 1;

    /**
     * The constant flag for single plot page
     */
    public static final int COMPENSATION_PAGE = 1;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Reserved Region constant flags
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the ID of the "universal" region
     */
    public static final int ALL_REGIONS = -3;

    /**
     * The constant flag for the ID of the "ungated" region
     */
    public static final int UNGATED = -2;

    /**
     * The ID of the gate set region
     */
    public static final int GATE_SET = -1;

    /**
     * The constant "universal" region
     */
    public static final Region UNIVERSAL_REGION = new Region(ALL_REGIONS);

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Reserved GateSet constant flags
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * Reserved ID number of the gating illustration
     *
     * This could be an illustration used by gating to preserve state. Or it
     * could languish unused. Either way, we have an ID set aside.
     */
    public static final int GATING_ILLUSTRATION_ID = 0;

    /**
     * Reserved ID number of the working illustration
     *
     * The working illustration is what the user sees if they do not load an
     * existing illustration. If they leave and come back, the state of the
     * illustration is persisted in the working illustration.
     */
    public static final int WORKING_ILLUSTRATION_ID = 1;

    /**
     * Reserved ID number indicating that all illustrations should be wiped out
     *
     * This number indicates that all the existing illustrations should be
     * wiped. No illustration should ever be given this ID. In the illustration
     * JSP controls, asking to delete the illustration with this ID will delete
     * all of the illustrations and start over from scratch, which can be useful
     * when testing and when the state of saved illustrations goes awry.
     */
    public static final int WIPE_ALL_ILLUSTRATIONS_ID = -999;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * layoutOverride type constants, must differ from plot types
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant flag for the layoutOverride type
     */
    public static final int NONE = -1;

    /**
     * The constant flag to override plots as heatmap squares that will have a
     * view-through to another plot
     *
     * Heatmaps include only one or two illustration dimensions
     */
    public static final int HEATMAP = 1;

    /**
     * The constant flag to override plots as heatmap squares that will have a
     * view-through to another plot and also plot out the statistics used to
     * make the heatmap in a table
     */
    public static final int HEATMAP_WITH_STATISTICS = 2;

    /**
     * The constant flag to override plots as histogram overlays
     */
    public static final int HISTOGRAM_OVERLAY = 3;

    /**
     * The constant flag to override plots as histogram overlays
     */
    public static final int HISTOGRAM_OVERLAY_WITH_STATISTICS = 4;

    /**
     * The constant flag to override plots as a solid block of heatmap squares
     * with no view through
     */
    public static final int HEATMAP_ONE_IMAGE = 5;

    /**
     * The constant flag to override plots as a solid block of heatmap squares
     * with no view through and also plot out the statistics used to make the
     * heatmap in a table
     */
    public static final int HEATMAP_ONE_IMAGE_WITH_STATISTICS = 6;

    /**
     * A constant array of valid plot color set flags
     */
    public static final int[] VALID_LAYOUT_OVERRIDE_TYPES = new int[] { HEATMAP, HEATMAP_WITH_STATISTICS, HISTOGRAM_OVERLAY, HISTOGRAM_OVERLAY_WITH_STATISTICS,
            HEATMAP_ONE_IMAGE, HEATMAP_ONE_IMAGE_WITH_STATISTICS };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Other constants
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The constant ID indicating that an illustration is the default way of
     * viewing a panel
     */
    public static final int PANEL_DEFAULT_REPRESENTATION_ID = -2;

    /**
     * The constant string path to the small placeholder image
     */
    public static final String placeholder_small_imagepath = "bitmaps/flow_tube_small.jpg";

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Constant default values
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * Default illustration name
     */
    public static final String DEFAULT_NAME = "Illustration ";

    /**
     * Default plot type
     */
    public static final int DEFAULT_PLOT_TYPE = DENSITY_DOT_PLOT;

    /**
     * Default black canvas background setting to off
     */
    public static final boolean DEFAULT_BACKGROUND_FLAG = false;

    /**
     * Default black plot background setting to off
     */
    public static final boolean DEFAULT_PLOT_BACKGROUND_FLAG = false;

    /**
     * Default to not show scale numbers on every plot
     */
    public static final boolean DEFAULT_SHOW_SCALE_LABELS_FLAG = false;

    /**
     * Default the stat type to none
     */
    public static final int DEFAULT_STAT_TYPE = NO_STATISTIC;

    /**
     * Default to en fuego
     */
    public static final int DEFAULT_COLOR_SET = EN_FUEGO;

    /**
     * Default to show annotations
     */
    public static final boolean DEFAULT_ANNOTATION_FLAG = true;

    /**
     * Default to show scale ticks on every plot
     */
    public static final boolean DEFAULT_SHOW_SCALE_TICKS_FLAG = true;

    /**
     * Default to show axis labels on every plot
     */
    public static final boolean DEFAULT_SHOW_AXIS_LABELS_FLAG = true;

    /**
     * Default to show power labels on every plot
     */
    public static final boolean DEFAULT_LONG_LABELS_FLAG = false;

    /**
     * Default to smoothing of 1.0d
     */
    public static final double DEFAULT_SMOOTHING = 1.0d;

    /**
     * Default to aspectRatio 1.0d
     */
    public static final double DEFAULT_ASPECT_RATIO = 1.0d;

    /**
     * Default to contourPercent of 10.0d
     */
    public static final double DEFAULT_CONTOUR_PERCENT = 10.0d;

    /**
     * Default to contourStartPercent of 10.0d
     */
    public static final double DEFAULT_CONTOUR_START_PERCENT = 10.0d;

    /**
     * Default to not use placeholders
     */
    public static final boolean DEFAULT_USE_PLACEHOLDERS_FLAG = false;

    /**
     * Default to not use print view
     */
    public static final boolean DEFAULT_USE_PRINT_VIEW_FLAG = false;

    /**
     * Default to not be a panel default illustration
     */
    public static final boolean DEFAULT_PANEL_DEFAULT_FLAG = false;

    /**
     * Default to not use print view
     */
    public static final boolean DEFAULT_SHOW_DETAILS_FLAG = false;

    /**
     * Default to show the title
     */
    public static final boolean DEFAULT_SHOW_TITLE_FLAG = true;

    /**
     * Default channel index
     *
     * The default index of all channels to be set upon creation. This is -1,
     * but each channel also has a special default (e.g. DEFAULT_X_CHANNEL) that
     * will be set if there are enough channels in the plot to support it.
     */
    public static final int DEFAULT_CHANNEL_INDEX = -1;

    /**
     * Default x channel
     */
    public static final int DEFAULT_X_CHANNEL = 1;

    /**
     * Default y channel
     */
    public static final int DEFAULT_Y_CHANNEL = 2;

    /**
     * Default z channel
     */
    public static final int DEFAULT_Z_CHANNEL = 3;

    /**
     * Default variable channel
     */
    public static final int DEFAULT_VARIABLE_CHANNEL = VARY_X;

    /**
     * Default compensation ID
     */
//    public static final int DEFAULT_COMPENSATION_ID = Compensation.USE_FILE_COMPENSATION;

    /**
     * Default channel count; if this doesn't get reset by setScalesAndChannels,
     * then the plot will not print. Which is to say, if we don't find a real
     * live flow file, the default channel count of -1 will mean we won't get a
     * plot.
     */
    public static final int DEFAULT_CHANNEL_COUNT = -1;

    /**
     * Default gate set IDs
     */
    public static final ArrayList DEFAULT_GATE_SET_IDS = new ArrayList<Integer>() {
        {
            add(UNGATED);
        }
    };

    /**
     * Default regions arraylist
     */
    public static final ArrayList DEFAULT_REGIONS = new ArrayList<Region>() {
        {
            add(UNIVERSAL_REGION);
        }
    };

    /**
     * Default scale flag
     */
    public static final int DEFAULT_SCALE_FLAG = Scaling.ARCSINH;

    /**
     * Default scale argument
     */
    public static final String DEFAULT_SCALE_ARGUMENT = "200";

    /**
     * Default scale minimum
     */
    public static final double DEFAULT_SCALE_MINIMUM = -200.0d;

    /**
     * Default scale maximum
     */
    public static final double DEFAULT_SCALE_MAXIMUM = 262144.0d;

    /**
     * Default population type
     */
    public static final int DEFAULT_POPULATION_TYPE = -1;

    /**
     * Default population cutoff
     */
    public static final int DEFAULT_POPULATION_CUTOFF = -1;

    /**
     * Default event count
     */
    public static final int DEFAULT_EVENT_COUNT = -1;

    /**
     * Default citation format
     */
    public static final int DEFAULT_CITATION = CITE_PRPI_DATE;

    /**
     * Default edit status
     */
    public static final int DEFAULT_EDIT_STATUS = EDIT_BY_CREATOR;

    /**
     * Default view status
     */
    public static final int DEFAULT_VIEW_STATUS = VIEW_BY_COAUTHORS;

    /**
     * Default creatorID
     */
    public static final int DEFAULT_CREATOR_ID = -1;

    /**
     * Default menu style
     */
    public static final int DEFAULT_MENU_STYLE = STANDARD_DIMENSIONS;

    /**
     * Default plot size
     */
    public static final int DEFAULT_PLOT_SIZE = SMALL;

    /**
     * Default creation query string
     */
    public static final String DEFAULT_CREATION_QUERY_STRING = null;

    /**
     * Default keystone filename
     */
    public static final String DEFAULT_KEYSTONE_FILENAME = "";

    /**
     * Default layoutOverride type
     */
    public static final int DEFAULT_LAYOUT_OVERRIDE = NONE;

    /**
     * Default statistic
     */
    public static final int DEFAULT_STATISTIC = PopulationGrid.ARCSINH_MEDIAN;

    /**
     * Default equation
     */
    public static final int DEFAULT_EQUATION = PopulationGrid.DIFFERENCE;

    /**
     * Default formula
     */
    public static final String DEFAULT_FORMULA = null;

    /**
     * Default control
     */
    public static final int DEFAULT_CONTROL = PopulationGrid.CELL_1_1;

    /**
     * Default control row
     */
    public static final int DEFAULT_CONTROL_ROW = -1;

    /**
     * Default control column
     */
    public static final int DEFAULT_CONTROL_COLUMN = -1;

    /**
     * Default range
     */
    public static final double DEFAULT_RANGE = Double.NaN;

    /**
     * Default row
     */
    public static final IllustrationDimension DEFAULT_ROW = null;

    /**
     * Default column
     */
    public static final IllustrationDimension DEFAULT_COLUMN = null;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Constant parameter names
     *
     * These are the names used for XML nodes and querysting parameters.
     *
     * JSP and other java objects should refer to these values rather than hard
     * coding a particular String to make it easier to change these values.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The name of this object
     */
    public static final String ILLUSTRATION_PARAM_NAME = "illustration";

    /**
     * The name of the id parameter
     */
    public static final String ID_PARAM_NAME = "illustrationID";

    /**
     * The name of the creator experiment ID parameter
     */
    public static final String CREATOR_EXPERIMENT_ID_PARAM_NAME = "experimentID";

    /**
     * The name of the name parameter
     */
    public static final String NAME_PARAM_NAME = "illustrationName";

    /**
     * The name of the plot typeparameter
     */
    public static final String PLOT_TYPE_PARAM_NAME = "plotType";

    /**
     * The name of the XML lookup flag parameter
     */
    public static final String FROM_XML_PARAM_NAME = "fromXML";

    /**
     * The name of the black canvs background flag parameter
     */
    public static final String BLACK_BACKGROUND_PARAM_NAME = "blackBackground";

    /**
     * The name of the black plot background flag parameter
     */
    public static final String BLACK_PLOT_BACKGROUND_PARAM_NAME = "blackPlotBackground";

    /**
     * The name of the x channel parameter
     */
    public static final String X_CHANNEL_PARAM_NAME = "xChannel";

    /**
     * The name of the y channel parameter
     */
    public static final String Y_CHANNEL_PARAM_NAME = "yChannel";

    /**
     * The name of the z channel parameter
     */
    public static final String Z_CHANNEL_PARAM_NAME = "zChannel";

    /**
     * The name of the variable channel parameter
     */
    public static final String VARIABLE_CHANNEL_PARAM_NAME = "variableChannel";

    /**
     * The name of the compensation ID parameter
     */
    public static final String COMPENSATION_ID_PARAM_NAME = "compensationID";

    /**
     * The name of the channel count parameter
     */
    public static final String CHANNEL_COUNT_PARAM_NAME = "channelCount";

    /**
     * The name of the gateSetIDs parameter
     */
    public static final String GATE_SET_IDS_PARAM_NAME = "gateSetID";

    /**
     * The name of the scale flag parameter
     */
    public static final String SCALE_FLAG_PARAM_NAME = "scale";

    /**
     * The name of the scale argument parameter
     */
    public static final String SCALE_ARGUMENT_PARAM_NAME = "scaleArg";

    /**
     * The name of the scale minimum parameter
     */
    public static final String SCALE_MINIMUM_PARAM_NAME = "min";

    /**
     * The name of the scale maximum parameter
     */
    public static final String SCALE_MAXIMUM_PARAM_NAME = "max";

    /**
     * The name of the population type parameter
     */
    public static final String POPULATION_TYPE_PARAM_NAME = "populationType";

    /**
     * The name of the population cutoff parameter
     */
    public static final String POPULATION_CUTOFF_PARAM_NAME = "layoutPopulationCutoff";

    /**
     * The name of the event count parameter
     */
    public static final String EVENT_COUNT_PARAM_NAME = "eventCount";

    /**
     * The name of the panelDefault parameter
     */
    public static final String PANEL_DEFAULT_PARAM_NAME = "panelDefault";

    /**
     * The name of the citationFormat parameter
     */
    public static final String CITATION_FORMAT_PARAM_NAME = "citationFormat";

    /**
     * The name of the creatorID parameter
     */
    public static final String CREATOR_ID_PARAM_NAME = "creatorID";

    /**
     * The name of the editStatus parameter
     */
    public static final String EDIT_STATUS_PARAM_NAME = "editStatus";

    /**
     * The name of the viewStatus parameter
     */
    public static final String VIEW_STATUS_PARAM_NAME = "viewStatus";

    /**
     * The name of the menuStyle parameter
     */
    public static final String MENU_STYLE_PARAM_NAME = "menuStyle";

    /**
     * The name of the plot size parameter
     */
    public static final String PLOT_SIZE_PARAM_NAME = "axisBins";

    /**
     * The name of the save flag parameter, which indicates to the POST servlet
     * to save and finish the illustration
     */
    public static final String SAVE_FLAG_PARAM_NAME = "saveFlag";

    /**
     * The name of the delete flag parameter, which indicates to the POST
     * servlet to delete an illustration
     */
    public static final String DELETE_FLAG_PARAM_NAME = "deleteFlag";

    /**
     * The name of the delete all flag parameter, which indicates to the POST
     * servlet to delete all illustrations
     */
    public static final String DELETE_ALL_FLAG_PARAM_NAME = "deleteAllFlag";

    /**
     * The name of the show labels flag parameter
     */
    public static final String SHOW_SCALE_LABELS_PARAM_NAME = "scaleLabel";

    /**
     * The name of the stat type parameter
     */
    public static final String STAT_TYPE_PARAM_NAME = "statType";

    /**
     * The name of the color set parameter
     */
    public static final String COLOR_SET_PARAM_NAME = "colorSet";

    /**
     * The name of the annotation flag parameter
     */
    public static final String ANNOTATION_PARAM_NAME = "annotation";

    /**
     * The name of the show scale ticks flag parameter
     */
    public static final String SHOW_SCALE_TICKS_PARAM_NAME = "scaleTick";

    /**
     * The name of the show axis labels flag parameter
     */
    public static final String SHOW_AXIS_LABELS_PARAM_NAME = "axisLabel";

    /**
     * The name of the long scale labels flag parameter
     */
    public static final String LONG_LABELS_PARAM_NAME = "longLabel";

    /**
     * The name of the smoothing parameter
     */
    public static final String SMOOTHING_PARAM_NAME = "smoothing";

    /**
     * The name of the aspect ratio parameter
     */
    public static final String ASPECT_RATIO_PARAM_NAME = "aspectRatio";

    /**
     * The name of the contour percent parameter
     */
    public static final String CONTOUR_PERCENT_PARAM_NAME = "contourPercent";

    /**
     * The name of the contour start percent parameter
     */
    public static final String CONTOUR_START_PERCENT_PARAM_NAME = "contourStartPercent";

    /**
     * The name of the use placeholders flag parameter
     */
    public static final String USE_PLACEHOLDERS_PARAM_NAME = "usePlaceholders";

    /**
     * The name of the use in printview flag parameter
     */
    public static final String USE_PRINT_VIEW_PARAM_NAME = "usePrintView";

    /**
     * The name of the illustration panel default flag
     */
    public static final String SHOW_PANEL_DEFAULT_NAME = "panelDefault";

    /**
     * The name of the illustration show details flag
     */
    public static final String SHOW_DETAILS_PARAM_NAME = "showDetails";

    /**
     * The name of the illustration show title flag
     */
    public static final String SHOW_TITLE_PARAM_NAME = "showTitle";

    /**
     * The name of the creation query string parameter
     */
    public static final String CREATION_QUERY_STRING_PARAM_NAME = "creationQueryString";

    /**
     * The name of the keystone file name parameter
     */
    public static final String KEYSTONE_FILENAME_PARAM_NAME = "keystoneFilename";

    /**
     * The name of the active illustration dimensions array parameter
     */
    public static final String ACTIVE_DIMENSIONS_PARAM_NAME = "activeDimensions";

    /**
     * The name of the XML node that holds all the settings for an illustration
     * dimension. Each illustration dimension will be encoded as this, and all
     * of them will be placed in an XML node named by
     * ACTIVE_DIMENSIONS_PARAM_NAME
     */
    public static final String ILLUSTRATION_DIMENSION_PARAM_NAME = "illustrationDimension";

    /**
     * Prefix that goes before every illustration element variable in the query
     * string illustration
     */
    public static final String DIMENSION_PREFIX = "illD";

    /**
     * Prefix that goes before every region variable in the query string
     * illustration
     */
    public static final String REGION_PREFIX = "region";

    /**
     * The type of layoutOverride
     */
    public static final String LAYOUT_OVERRIDE_PARAM_NAME = "layoutOverride";

    /**
     * The statistic used to compare populations
     */
    public static final String STATISTIC_PARAM_NAME = "statistic";

    /**
     * The equation used to compare populations
     */
    public static final String EQUATION_PARAM_NAME = "equation";

    /**
     * The formula used to compare populations
     */
    public static final String FORMULA_PARAM_NAME = "formula";

    /**
     * The control population
     */
    public static final String CONTROL_PARAM_NAME = "control";

    /**
     * The controol row index
     */
    public static final String CONTROL_ROW_PARAM_NAME = "controlRow";

    /**
     * The control column index
     */
    public static final String CONTROL_COLUMN_PARAM_NAME = "controlColumn";

    /**
     * The range for the scale
     */
    public static final String RANGE_PARAM_NAME = "range";

    /**
     * The row illustration dimension
     */
    public static final String ROW_PARAM_NAME = "row";

    /**
     * The column illustration dimension
     */
    public static final String COLUMN_PARAM_NAME = "column";

    /**
     * Legacy XML nodes from the old object
     */
    public static final String OVERLAY_PARAM_NAME = "overlay";
    public static final String HEATMAP_PARAM_NAME = "heatmap";

    /**
     * ***********************************************************************
     * ***********************************************************************
     * IllustrationDimension
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * An Illustration dimension, a mask and Illustration-specific settings for
     * a FigureDimension.
     * </p>
     *
     * <p>
     * This is designed to allow users to edit the view of a FigureDimension in
     * one Illustration without affecting other Illustrations that display the
     * same FigureDimension.
     * </p>
     *
     */
    public class IllustrationDimension implements Comparable<IllustrationDimension> {

        /**
         * Constants - used to name the XML nodes for illustrationDimension
         */
        public static final String ID_PARAM_NAME = "ID";
        public static final String TARGET_FIG_DIM_ID_PARAM_NAME = "TargetFigDimID";
        public static final String ORDER_PARAM_NAME = "Order";
        public static final String ORIENTATION_PARAM_NAME = "Orientation";
        public static final String INDICES_PARAM_NAME = "Indices";
        public static final String INDEX_PARAM_NAME = "Index";

        /**
         * Public Constants
         */

        // ORIENTATION CONSTANTS
        public static final int STACKED = 0;
        public static final int SIDE_BY_SIDE = 1;
        public static final int PAGES = 2;
        public static final int OVERLAY = 3;

        // -1 is an invalid ID, this needs to be set at creation
        public static final int DEFAULT_ID = -1;

        // -1 is an invalid target ID, this needs to be set at creation
        public static final int DEFAULT_TARGET_FIGURE_DIMENSION_ID = -1;

        // Default to stacked
        public static final int DEFAULT_ORIENTATION = STACKED;

        // Default to be the first dimension
        public static final int DEFAULT_ORDER = 1;

        /**
         * The ID of the illustration dimension (unique)
         */
        private int id;

        /**
         * The ID of the figure dimension this illustration dimension points to
         * (not necessarily unique)
         */
        private int targetFigureDimensionID;

        /**
         * The order index of the illustration dimension
         */
        private int order;

        /**
         * The integer constant indicating whether the illustration dimention is
         * pages, stacked, or side by side
         */
        private int orientation;

        /**
         * The array of indices of the selected elements in the illustration
         * dimension
         */
        private int[] indices;

        /**
         * <p>
         * Creates an illustration dimension with the id set to id and
         * everything else to defaults.
         * </p>
         *
         * @param id
         * int ID of the figure dimension of the illustration
         * dimension.
         */
        public IllustrationDimension(int id) {

            // Set the id of illustration dimension
            this.id = id;

            this.order = getNextOrderValue();

            // Set the targetFigureDimensionID of illustration dimension
            this.targetFigureDimensionID = DEFAULT_TARGET_FIGURE_DIMENSION_ID;

            // Set whether the illustration dimention is stacked, side by side,
            // or pages
            this.orientation = DEFAULT_ORIENTATION;

            // Set the array of indices
            setIndices(indices);

        }

        /**
         * <p>
         * Creates an illustration dimension with the id set to id, the
         * targetIllustrationDimensionID of the illustration dimension set to
         * targetFigureDimensionID, the orientation set to orientation, and the
         * indicies to indices.
         * </p>
         *
         * <p>
         * The order index of the illustration dimension is set to -1.
         * </p>
         *
         * @param id
         * int ID of the figure dimension of the illustration
         * dimension.
         * @param targetFigureDimensionID
         * int ID of the figure dimension of the illustration
         * dimension.
         * @param orientation
         * integer constant indicating the orientation of the
         * illustration.
         * @param indices
         * int array of indices of the selected elements in the
         * illustration dimension.
         */
        public IllustrationDimension(int id, int targetFigureDimensionID, int orientation, int[] indices) {
            this(id, targetFigureDimensionID, DEFAULT_ORDER, orientation, indices);
        }

        /**
         * <p>
         * Creates an illustration dimension with the id set to id, the
         * targetIllustrationDimensionID of the illustration dimension set to
         * targetFigureDimensionID, the order index of the illustration
         * dimension set to order, the orientation to orientation, and the
         * indices set to indices.
         * </p>
         *
         * @param id
         * int ID of the figure dimension of the illustration
         * dimension.
         * @param targetFigureDimensionID
         * int ID of the illustration dimension.
         * @param order
         * int order index of the illustration dimension.
         * @param orientation
         * integer constant indicating the orientation of the
         * illustration.
         * @param indices
         * int array of indices of the selected elements in the
         * illustration dimension.
         */
        public IllustrationDimension(int id, int targetFigureDimensionID, int order, int orientation, int[] indices) {
            // Set the id of illustration dimension
            this.id = id;

            // Set the targetFigureDimensionID of illustration dimension
            this.targetFigureDimensionID = targetFigureDimensionID;

            // Set the order index of the illustration dimension
            this.order = order;

            // Set whether the illustration dimention is stacked, side by side,
            // or pages
            this.orientation = orientation;

            // Set the array of indices
            setIndices(indices);
        }

        /**
         * <p>
         * Sets the optional parameters of the Illustration Dimension to their
         * default values.
         * </p>
         */
        private void setToDefaultValues() {

        }

        /**
         * <p>
         * Returns the ID of the illustration dimension.
         * </p>
         *
         * @return int ID of the illustration dimension.
         */
        public int getID() {
            return id;
        }

        /**
         * <p>
         * Returns the ID of the illustration dimension's figure dimension.
         * </p>
         *
         * @return int ID of the illustration dimension's figure dimension.
         */
        public int getTargetFigureDimensionID() {
            return targetFigureDimensionID;
        }

        /**
         * <p>
         * Returns the illustration dimension's figure dimension, plucked from
         * an array of figure dimensions.
         * </p>
         *
         * @param figureDimensions
         * <code>FigureDimension[]</code> an array of figure
         * dimensions.</p>
         *
         * @return <code>FigureDimension</code> the IllustrationDimension's
         * target figure dimension.</p>
         */
//        public FigureDimension getTargetFigureDimension(FigureDimension[] figureDimensions) {
//
//            // create a targetFigureDimension set to be returned
//            FigureDimension targetFigureDimension = null;
//
//            if (figureDimensions != null && figureDimensions.length > 0 && this != null) {
//                // if we got a non-null figureDimensions array
//                for (int i = 0; i < figureDimensions.length; i++) {
//                    // For each figure dimension in the array
//
//                    if (figureDimensions[i] != null) {
//
//                        if (figureDimensions[i].getID() == this.getTargetFigureDimensionID()) {
//                            // If the passed ID matches the target ID,
//                            // then return it
//
//                            targetFigureDimension = (FigureDimension) figureDimensions[i];
//                            break;
//
//                        }
//                    }
//                }
//            }
//
//            return targetFigureDimension;
//        }

        /**
         * <p>
         * Returns the order index of the illustration dimension.
         * </p>
         *
         * @return int order index of the illustration dimension.
         */
        public int getOrder() {
            return order;
        }

        /**
         * <p>
         * Returns whether this is the column dimension.
         * </p>
         *
         * @return boolean flag indicating whether the dimension is the column
         * dimension.
         */
        public boolean isColumn() {
            boolean isColumn = false;
            // If this is the second active dimension, then it is the row
            // dimension
            // then this is a table in the sense that this function tests
            if (getOrder() == 0)
                isColumn = true;
            return isColumn;
        }

        /**
         * <p>
         * Returns whether this is the row dimension.
         * </p>
         *
         * @return boolean flag indicating whether the figure dimension is the
         * row dimension.
         */
        public boolean isRow() {
            boolean isRow = false;
            // If this is the second active dimension, then it is the row
            // dimension
            // then this is a table in the sense that this function tests
            if (getOrder() == 1)
                isRow = true;
            return isRow;
        }

        /**
         * <p>
         * Returns whether the figure dimension describes a table. Legacy
         * function.
         * </p>
         *
         * @return boolean flag indicating whether the dimension describes a
         * table.
         */
        public boolean isTable() {
            boolean isTable = false;
            // If the orientation is anything other than a page,
            // then this is a table in the sense that this function tests
            if (getOrientation() == STACKED || getOrientation() == SIDE_BY_SIDE || getOrientation() == OVERLAY)
                isTable = true;
            return isTable;
        }

        /**
         * <p>
         * Returns whether the illustration dimension is stacked, side by side,
         * or pages.
         * </p>
         *
         * @return integer constant indicating whether the illustration
         * dimension is stacked, side by side, or pages.
         */
        public int getOrientation() {
            return orientation;
        }

        /**
         * <p>
         * Returns whether the passed index is active.
         * </p>
         *
         * @param indexToCheck
         * int index to determine whether it is in the list of active
         * indices
         *
         * @return boolean flag indicating whether the passed index is in the
         * list of active indices
         */
        public boolean isActive(int indexToCheck) {
            boolean isActiveP = false;

            for (int j = 0; j < indices.length; j++) {
                if (indexToCheck == indices[j])
                    isActiveP = true;
            }

            return isActiveP;
        }

        /**
         * <p>
         * Returns the number of selected elements in the illustration
         * dimension.
         * </p>
         *
         * @return int number of selected elements in the illustration
         * dimension.
         */
        public int getIndexCount() {
            if (indices == null) {
                // If the array of indices of the selected elements in the
                // illustration dimension is null, then return 0.
                return 0;
            } else {
                // Otherwise, the array of indices of the selected elements in
                // the illustration dimension is not null, so return its length.
                return indices.length;
            }
        }

        /**
         * <p>
         * Returns the array of indices of the selected elements in the
         * illustration dimension.
         * </p>
         *
         * @return int array of indices of the selected elements in the
         * illustration dimension.
         */
        public int[] getIndices() {
            return indices;
        }

        /**
         * <p>
         * Sets the array of indices of the selected elements in the
         * illustration dimension to indices.
         * </p>
         *
         * @param indices
         * int array of indices of the selected elements in the
         * illustration dimension.
         */
        private void setIndices(int[] indices) {
            if (indices == null) {
                // If the array of indices is null, then set the array of
                // indices to an empty int array.
                this.indices = new int[0];
            } else {
                // Otherwise, the array of indices is not null, so copy the
                // array of indices.

                // Allocate the array of indices
                this.indices = new int[indices.length];

                // Copy the array of indices
                System.arraycopy(indices, 0, this.indices, 0, indices.length);

                // Sort the array of indices
                Arrays.sort(this.indices);
            }
        }

        /**
         * Comparable interface
         */

        /**
         * <p>
         * Returns the comparison of this illustration dimension with the passed
         * IllustrationDimension
         *
         * <p>
         * The method is the <code>IllustrationDimension</code> implementation
         * of the abstract method in the
         * <code>Comparable&lt;IllustrationDimension&gt;</code> interface to
         * allow sorting of <code>IllustrationDimension</code> objects by the
         * order index.
         * </p>
         *
         * @param dimension
         * <code>IllustrationDimension</code> object to the
         * illustration dimension with which to compare.
         * @return int result of the comparison.
         */
        public int compareTo(IllustrationDimension dimension) {
            if (dimension == null) {
                // If the illustration dimension with which to compare is null,
                // then return -1.
                return -1;
            }

            // Get the order index of this illustration dimension
            Integer thisOrder = Integer.valueOf(getOrder());

            // Get the order index of the other illustration dimension
            Integer otherOrder = Integer.valueOf(dimension.getOrder());

            // Return the comparison of the order indices of the illustration
            // dimensions
            return thisOrder.compareTo(otherOrder);
        }

        /**
         * <p>
         * Returns the parameters of the illustration dimension encoded as HTML
         * formatted text.
         * </p>
         *
         * @return <code>String</code> parameters of the illustration dimension
         * encoded as HTML formatted text.
         */
//        public String getDetails() {
//            // Create a StringBuffer with which to encode the parameters
//            StringBuffer parameters = new StringBuffer();
//
//            // The first one added does not begin with an ampersand
//            // Add the ID
//            parameters.append("<td>" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ID_PARAM_NAME + "</td><td>" + JSPlib.encode(this.id + "")
//                    + "</td></tr><tr>");
//            // Add the targetFigureDimensionID
//            parameters.append("<td>" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + TARGET_FIG_DIM_ID_PARAM_NAME + "</td><td>"
//                    + JSPlib.encode(this.targetFigureDimensionID + "") + "</td></tr><tr>");
//            // Add the order
//            parameters.append("<td>" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ORDER_PARAM_NAME + "</td><td>" + JSPlib.encode(this.order + "")
//                    + "</td></tr><tr>");
//            // Add the orientation
//            parameters.append("<td>" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ORIENTATION_PARAM_NAME + "</td><td>"
//                    + JSPlib.encode(this.orientation + "") + "</td></tr><tr>");
//            // Add the indicies
//            StringBuffer indicesParameter = new StringBuffer();
//            indicesParameter.append("<td>" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + INDICES_PARAM_NAME + "</td><td>");
//            for (int i = 0; i < this.indices.length; i++) {
//                indicesParameter.append(JSPlib.encode(this.indices[i] + ""));
//                if (i != this.indices.length - 1)
//                    indicesParameter.append(",");
//            }
//            parameters.append(indicesParameter.toString() + "</td></tr><tr>");
//
//            return parameters.toString();
//        }

        /**
         * Quesrystring methods
         */

        /**
         * <p>
         * Returns the parameters of the illustration dimension encoded as a URL
         * querystring (serializes the Illustration Dimension as a querystring).
         * </p>
         *
         * @return <code>String</code> parameters of the illustration dimension
         * encoded as a URL querystring.
         */
//        public String getQueryString() {
//            // Create a StringBuffer with which to encode the parameters
//            StringBuffer parameters = new StringBuffer();
//
//            // The first one added does not begin with an ampersand
//            // Add the ID
//            parameters.append(DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ID_PARAM_NAME + "=" + JSPlib.encode(this.id + ""));
//            // Add the targetFigureDimensionID
//            parameters.append("&" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + TARGET_FIG_DIM_ID_PARAM_NAME + "="
//                    + JSPlib.encode(this.targetFigureDimensionID + ""));
//            // Add the order
//            parameters.append("&" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ORDER_PARAM_NAME + "=" + JSPlib.encode(this.order + ""));
//            // Add the orientation
//            parameters.append("&" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + ORIENTATION_PARAM_NAME + "=" + JSPlib.encode(this.orientation + ""));
//            // Add the indicies
//            StringBuffer indicesParameter = new StringBuffer();
//            indicesParameter.append("&" + DIMENSION_PREFIX + JSPlib.encode(this.id + "") + INDICES_PARAM_NAME + "=");
//            for (int i = 0; i < this.indices.length; i++) {
//                indicesParameter.append(JSPlib.encode(this.indices[i] + ""));
//                if (i != this.indices.length - 1)
//                    indicesParameter.append(",");
//            }
//            parameters.append(indicesParameter.toString());
//
//            return parameters.toString();
//        }

        /**
         * <p>
         * Sets the parameters of the illustration dimension using a URL
         * querystring (deserializes an illustration dimension from a
         * querystring).
         *
         * Searches the querystring for parameters named the same as the
         * constant parameter names of an illustration dimension with the same
         * id of the one you are calling this on. When if finds a parameter, it
         * will update the value of the illustration dimension to the value on
         * the querystring.
         *
         * If a given parameter is not found it uses the default.
         * </p>
         *
         * @param id
         * <code>int</code> ID of the illustration dimension to be
         * decoded
         * @param queryString
         * <code>String</code> parameters of the illustration
         * dimension encoded as a URL querystring.
         */
//        public IllustrationDimension setFromQueryString(int id, String queryString) {
//
//            IllustrationDimension returnDimension = null;
//
//            int targetFigureDimensionID = DEFAULT_TARGET_FIGURE_DIMENSION_ID;
//            int order = DEFAULT_ORDER;
//            int orientation = DEFAULT_ORIENTATION;
//            int[] indices = null;
//
//            // Check to make sure querystring is a valid query string
//            if (queryString != null) {
//
//                // Look for each parameter and, if it exists, parse it
//
//                // IllustrationDimension targetFigureDimensionID
//                if (queryString.contains(DIMENSION_PREFIX + JSPlib.encode(id + "") + TARGET_FIG_DIM_ID_PARAM_NAME)) {
//                    targetFigureDimensionID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, DIMENSION_PREFIX + JSPlib.encode(id + "")
//                            + TARGET_FIG_DIM_ID_PARAM_NAME)), targetFigureDimensionID);
//                }
//
//                // IllustrationDimension order
//                if (queryString.contains(DIMENSION_PREFIX + JSPlib.encode(id + "") + ORDER_PARAM_NAME)) {
//                    order = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, DIMENSION_PREFIX + JSPlib.encode(id + "") + ORDER_PARAM_NAME)),
//                            order);
//                }
//
//                // IllustrationDimension orientation
//                if (queryString.contains(DIMENSION_PREFIX + JSPlib.encode(id + "") + ORIENTATION_PARAM_NAME)) {
//                    orientation = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, DIMENSION_PREFIX + JSPlib.encode(id + "")
//                            + ORIENTATION_PARAM_NAME)), orientation);
//                }
//
//                // IllustrationDimension indicies
//                if (queryString.contains(DIMENSION_PREFIX + JSPlib.encode(id + "") + INDICES_PARAM_NAME)) {
//                    // Get the comma-delimited list of indicies
//                    String indiceslist = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, DIMENSION_PREFIX + JSPlib.encode(id + "")
//                            + INDICES_PARAM_NAME)), "");
//                    // Set the indicies array from the values inbetween the
//                    // commas
//                    String[] indicesArray = indiceslist.split(",");
//
//                    indices = new int[indicesArray.length];
//
//                    for (int i = 0; i < indicesArray.length; i++) {
//                        indices[i] = JSPlib.checkStatus(indicesArray[i], -1);
//                    }
//
//                }
//            }
//
//            returnDimension = new IllustrationDimension(id, targetFigureDimensionID, order, orientation, indices);
//
//            return returnDimension;
//        }

        /**
         * XML methods
         */

        /**
         * <p>
         * Returns the <code>org.jdom.Element</code> object corresponding to the
         * illustration dimension.
         * </p>
         *
         * @return <code>org.jdom.Element</code> object corresponding to the
         * illustration dimension.
         */
//        public Element getElement() {
//            // Create the illustration dimension element
//            Element element = new Element(ILLUSTRATION_DIMENSION_PARAM_NAME);
//
//            // Encode the ID of the illustration dimension
//            Element idElement = new Element(ID_PARAM_NAME);
//            idElement.addContent(Integer.toString(id));
//            element.addContent(idElement);
//
//            // Encode the targetFigureDimensionID of the illustration dimension
//            Element targetFigureDimensionIDElement = new Element(TARGET_FIG_DIM_ID_PARAM_NAME);
//            targetFigureDimensionIDElement.addContent(Integer.toString(targetFigureDimensionID));
//            element.addContent(targetFigureDimensionIDElement);
//
//            // Encode the order index of the illustration dimension
//            Element orderElement = new Element(ORDER_PARAM_NAME);
//            orderElement.addContent(Integer.toString(order));
//            element.addContent(orderElement);
//
//            // Encode the illustration dimension orientation
//            Element tableElement = new Element(ORIENTATION_PARAM_NAME);
//            tableElement.addContent(Integer.toString(orientation));
//            element.addContent(tableElement);
//
//            // Create the indices element to hold all the indices
//            Element indicesElement = new Element(INDICES_PARAM_NAME);
//
//            if (indices != null) {
//                // If the array of indices of the selected elements in the
//                // illustration dimension is not null, then encode it.
//                Element indexElement;
//
//                // Loop through the array of indices
//                for (int i = 0; i < indices.length; i++) {
//                    indexElement = new Element(INDEX_PARAM_NAME);
//                    indexElement.addContent(Integer.toString(indices[i]));
//                    indicesElement.addContent(indexElement);
//                }
//            }
//
//            // Add the indices element to the element
//            element.addContent(indicesElement);
//
//            // Return the element we created
//            return element;
//        }

        /**
         * <p>
         * Sets an illustration dimension based on an element.
         * </p>
         *
         * @param element
         * <code>org.jdom.Element</code> object to the XML element.
         */
//        public IllustrationDimension setFromElement(Element element) {
//
//            IllustrationDimension returnDimension = null;
//
//            int id = DEFAULT_ID;
//            int targetFigureDimensionID = DEFAULT_TARGET_FIGURE_DIMENSION_ID;
//            int order = DEFAULT_ORDER;
//            int orientation = DEFAULT_ORIENTATION;
//
//            if (element != null) {
//
//                // Parse the id
//                if (element.getChild(ID_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    id = JSPlib.checkStatus(element.getChild(ID_PARAM_NAME).getText(), DEFAULT_ID);
//                }
//
//                // Parse the targetFigureDimensionID
//                if (element.getChild(TARGET_FIG_DIM_ID_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    targetFigureDimensionID = JSPlib.checkStatus(element.getChild(TARGET_FIG_DIM_ID_PARAM_NAME).getText(), DEFAULT_TARGET_FIGURE_DIMENSION_ID);
//                }
//
//                // Parse the order
//                if (element.getChild(ORDER_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    order = JSPlib.checkStatus(element.getChild(ORDER_PARAM_NAME).getText(), DEFAULT_ORDER);
//                }
//
//                // Parse the orientation
//                if (element.getChild(ORIENTATION_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    orientation = JSPlib.checkStatus(element.getChild(ORIENTATION_PARAM_NAME).getText(), DEFAULT_ORIENTATION);
//                }
//
//                /**
//                 * Parse the array of indices of the selected elements in the
//                 * illustration dimension
//                 */
//                int[] indices = null;
//
//                // Get the indices element
//                Element indicesElement = element.getChild(INDICES_PARAM_NAME);
//
//                if (indicesElement != null) {
//                    // If the indices element exists, then parse the array of
//                    // indices of the selected elements in the illustration
//                    // dimension.
//
//                    // Get all the indices
//                    List elementList = indicesElement.getChildren();
//                    Iterator iterator = elementList.iterator();
//
//                    // Allocate the array of indices
//                    indices = new int[elementList.size()];
//
//                    // Initialize the index to 0
//                    int index = 0;
//
//                    Element indexElement;
//
//                    while (iterator.hasNext()) {
//                        // Get the index element
//                        indexElement = (Element) iterator.next();
//                        indices[index] = JSPlib.checkStatus(indexElement.getText(), -1);
//
//                        // Increment the index
//                        index++;
//                    }
//
//                }
//
//                returnDimension = new IllustrationDimension(id, targetFigureDimensionID, order, orientation, indices);
//            }
//            return returnDimension;
//
//        }

        // end IllustrationDimension class
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Region
     * ***********************************************************************
     * ***********************************************************************
     */

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
         * Default showP flag
         */
        public static final boolean DEFAULT_SHOW = true;

        /**
         * Default shapeP flag
         */
        public static final boolean DEFAULT_SHAPE = true;

        /**
         * Default labelP flag
         */
        public static final boolean DEFAULT_LABEL = false;

        /**
         * Default eventcountP flag
         */
        public static final boolean DEFAULT_EVENT_COUNT = false;

        /**
         * Default percentP flag
         */
        public static final boolean DEFAULT_PERCENT = true;

        public static final String ID_PARAM_NAME = "id";
        public static final String SHOW_PARAM_NAME = "shown";
        public static final String SHAPE_PARAM_NAME = "shape";
        public static final String LABEL_PARAM_NAME = "label";
        public static final String EVENT_COUNT_PARAM_NAME = "eventcount";
        public static final String PERCENT_PARAM_NAME = "percent";

        // -1 is an invalid ID, this needs to be set at creation
        public static final int DEFAULT_ID = ALL_REGIONS;

        /**
         * The ID of the region
         */
        private final int id;

        /**
         * The boolean flag indicating whether to show the region
         */
        private boolean showP;

        /**
         * The boolean flag indicating whether to draw the region shape
         */
        private boolean shapeP;

        /**
         * The boolean flag indicating whether to show the label of the region
         */
        private boolean labelP;

        /**
         * The boolean flag indicating whether to show the number of events in
         * the region
         */
        private boolean eventCountP;

        /**
         * The boolean flag indicating whether to show the percent of the region
         */
        private boolean percentP;

        /**
         * <p>
         * A constructor for <code>Region</code>.
         * </p>
         *
         * @param id
         * int ID of the region.
         */
        public Region(int id) {
            // Set the ID of the region
            this.id = id;

            /**
             * Initialize all the boolean flags
             */
            this.showP = DEFAULT_SHOW;
            this.shapeP = DEFAULT_SHAPE;
            this.labelP = DEFAULT_LABEL;
            this.eventCountP = DEFAULT_EVENT_COUNT;
            this.percentP = DEFAULT_PERCENT;
        }

        /**
         * <p>
         * A full constructor for <code>Region</code>.
         * </p>
         *
         * @param id
         * int ID of the region.
         * @param showP
         * boolean flag indicating whether or not to show any of the
         * region
         * @param shapeP
         * boolean flag indicating whether or not to show the region
         * shape
         * @param labelP
         * boolean flag indicating whether or not to show the region
         * name label
         * @param eventCountP
         * boolean flag indicating whether or not to show the event
         * count in the region
         * @param percentP
         * boolean flag indicating whether or not to show the percent
         * of the parent events in the region
         */
        public Region(int id, boolean showP, boolean shapeP, boolean labelP, boolean eventCountP, boolean percentP) {
            // Set the ID of the region
            this.id = id;
            this.showP = showP;
            this.shapeP = shapeP;
            this.labelP = labelP;
            this.eventCountP = eventCountP;
            this.percentP = percentP;
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
         * <code>Illustration.Region</code> object.
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
         * Returns whether to draw the region shape.
         * </p>
         *
         * @return boolean flag indicating whether to draw the region shape.
         */
        public boolean isShapeShown() {
            return shapeP;
        }

        /**
         * <p>
         * Sets whether to draw the region to drawP.
         * </p>
         *
         * @param shapeP
         * boolean flag indicating whether to draw the region.
         */
        public void setShapeShown(boolean shapeP) {
            this.shapeP = shapeP;
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
            return labelP;
        }

        /**
         * <p>
         * Sets whether to show the label of the region to showLabelP.
         * </p>
         *
         * @param labelP
         * boolean flag indicating whether to show the label of the
         * region.
         */
        public void setLabelShown(boolean labelP) {
            this.labelP = labelP;
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
            return eventCountP;
        }

        /**
         * <p>
         * Sets whether to show the number of events in the region to
         * eventCountP.
         * </p>
         *
         * @param eventCountP
         * boolean flag indicating whether to show the number of
         * events in the region.
         */
        public void setEventCountShown(boolean eventCountP) {
            this.eventCountP = eventCountP;
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
            return percentP;
        }

        /**
         * <p>
         * Sets whether to show the percent of the region to percentP.
         * </p>
         *
         * @param percentP
         * boolean flag indicating whether to show the percent of the
         * region.
         */
        public void setPercentShown(boolean percentP) {
            this.percentP = percentP;
        }

        /**
         * <p>
         * Returns the parameters of the region encoded as a URL querystring in
         * the format needed for plotting (serializes the Region as a
         * querystring).
         * </p>
         *
         * @return <code>String</code> parameters of the region encoded as a URL
         * querystring.
         */
        public String getPlotSettingsQueryString() {
            StringBuffer parameters = new StringBuffer();

            /**
             * Encode the region parameters
             */

            if (showP) {
                // If the region should be shown, then encode the other region
                // parameters.

                if (shapeP) {
                    // If the region should be drawn, then encode draw the
                    // region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=region");
                }

                if (labelP) {
                    // If the label of the region should be shown, then encode
                    // show the label of the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=label");
                }

                if (eventCountP) {
                    // If the number of events in the region should be shown,
                    // then encode show the number of events in the region.
                    parameters.append("&region");
                    parameters.append(id);
                    parameters.append("=eventCount");
                }

                if (percentP) {
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
         * <p>
         * Returns the parameters of the region encoded as a URL querystring
         * (serializes the Region as a querystring).
         * </p>
         *
         * @return <code>String</code> parameters of the region encoded as a URL
         * querystring.
         */
//        public String getQueryString() {
//            // Create a StringBuffer with which to encode the parameters
//            StringBuffer parameters = new StringBuffer();
//
//            parameters.append(REGION_PREFIX + JSPlib.encode(this.id + "") + ID_PARAM_NAME + "=" + JSPlib.encode(this.id + ""));
//
//            // Add show
//            parameters.append("&" + REGION_PREFIX + JSPlib.encode(this.id + "") + SHOW_PARAM_NAME + "=" + JSPlib.encode(this.showP + ""));
//
//            if (shapeP) {
//                // Add shape
//                parameters.append("&" + REGION_PREFIX + JSPlib.encode(this.id + "") + SHAPE_PARAM_NAME + "=" + JSPlib.encode(this.shapeP + ""));
//            }
//            if (labelP) {
//                // Add label
//                parameters.append("&" + REGION_PREFIX + JSPlib.encode(this.id + "") + LABEL_PARAM_NAME + "=" + JSPlib.encode(this.labelP + ""));
//            }
//            if (percentP) {
//                // Add percent
//                parameters.append("&" + REGION_PREFIX + JSPlib.encode(this.id + "") + PERCENT_PARAM_NAME + "=" + JSPlib.encode(this.percentP + ""));
//            }
//            if (eventCountP) {
//                // Add eventcount
//                parameters.append("&" + REGION_PREFIX + JSPlib.encode(this.id + "") + EVENT_COUNT_PARAM_NAME + "=" + JSPlib.encode(this.eventCountP + ""));
//            }
//
//            return parameters.toString();
//        }

        /**
         * <p>
         * Returns the encoded region parameters of the region.
         * </p>
         *
         * @return <code>String</code> encoded region parameters of the region.
         */
//        public String encode() {
//            return this.getQueryString();
//        }

        /**
         * <p>
         * Sets the parameters of the region using a URL querystring
         * (deserializes a Region from a querystring).
         *
         * Searches the querystring for parameters named the same as the
         * constant parameter names of a region with the same id of the one you
         * are calling this on. When if finds a parameter, it will update the
         * value of the Region to the value on the querystring.
         *
         * If a given parameter is not found it uses the default.
         * </p>
         *
         * @param id
         * <code>int</code> ID of the region to be decoded
         * @param queryString
         * <code>String</code> parameters of the illustration
         * dimension encoded as a URL querystring.
         */
//        public Region setFromQueryString(int id, String queryString) {
//
//            Region returnRegion = null;
//
//            // Set defaults
//            boolean showP = DEFAULT_SHOW;
//            boolean shapeP = DEFAULT_SHAPE;
//            boolean labelP = DEFAULT_LABEL;
//            boolean eventCountP = DEFAULT_EVENT_COUNT;
//            boolean percentP = DEFAULT_PERCENT;
//
//            // Check to make sure querystring is a valid query string
//            if (queryString != null) {
//
//                // Double check that the ID is on the QS
//                if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + ID_PARAM_NAME)) {
//
//                    // Look for each parameter and parse it
//
//                    // showP
//                    if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + SHOW_PARAM_NAME)) {
//                        showP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, REGION_PREFIX + JSPlib.encode(id + "") + SHOW_PARAM_NAME)),
//                                showP);
//                    } else {
//                        showP = false;
//                    }
//
//                    // shapeP
//                    if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + SHAPE_PARAM_NAME)) {
//                        shapeP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, REGION_PREFIX + JSPlib.encode(id + "") + SHAPE_PARAM_NAME)),
//                                shapeP);
//                    } else {
//                        shapeP = false;
//                    }
//
//                    // labelP
//                    if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + LABEL_PARAM_NAME)) {
//                        labelP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, REGION_PREFIX + JSPlib.encode(id + "") + LABEL_PARAM_NAME)),
//                                labelP);
//                    } else {
//                        labelP = false;
//                    }
//
//                    // eventCountP
//                    if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + EVENT_COUNT_PARAM_NAME)) {
//                        eventCountP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, REGION_PREFIX + JSPlib.encode(id + "")
//                                + EVENT_COUNT_PARAM_NAME)), eventCountP);
//                    } else {
//                        eventCountP = false;
//                    }
//
//                    // percentP
//                    if (queryString.contains(REGION_PREFIX + JSPlib.encode(id + "") + PERCENT_PARAM_NAME)) {
//                        percentP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, REGION_PREFIX + JSPlib.encode(id + "")
//                                + PERCENT_PARAM_NAME)), percentP);
//                    } else {
//                        percentP = false;
//                    }
//                }
//
//                returnRegion = new Region(id, showP, shapeP, labelP, eventCountP, percentP);
//            }
//
//            return returnRegion;
//        }

        /**
         * XML methods
         */

        /**
         * <p>
         * Returns the <code>org.jdom.Element</code> object corresponding to the
         * Region.
         * </p>
         *
         * @return <code>org.jdom.Element</code> object corresponding to the
         * region
         */
//        public Element getElement() {
//            // Create the illustration dimension element
//            Element regionElement = new Element(REGION_PREFIX + Integer.toString(id));
//
//            // Encode the ID
//            regionElement.addContent(XMLLog.encodeElement(ID_PARAM_NAME, Integer.toString(id)));
//
//            // show
//            regionElement.addContent(XMLLog.encodeElement(SHOW_PARAM_NAME, Boolean.toString(showP)));
//
//            // shape
//            regionElement.addContent(XMLLog.encodeElement(SHAPE_PARAM_NAME, Boolean.toString(shapeP)));
//
//            // label
//            regionElement.addContent(XMLLog.encodeElement(LABEL_PARAM_NAME, Boolean.toString(labelP)));
//
//            // eventCount
//            regionElement.addContent(XMLLog.encodeElement(EVENT_COUNT_PARAM_NAME, Boolean.toString(eventCountP)));
//
//            // percent
//            regionElement.addContent(XMLLog.encodeElement(PERCENT_PARAM_NAME, Boolean.toString(percentP)));
//
//            // Return the element we created
//            return regionElement;
//        }

        /**
         * <p>
         * Sets a region based on an element.
         * </p>
         *
         * @param element
         * <code>org.jdom.Element</code> object to the XML element.
         */
//        public Region setFromElement(Element element) {
//
//            Region returnRegion = null;
//
//            // Set defaults
//            int id = DEFAULT_ID;
//            boolean showP = DEFAULT_SHOW;
//            boolean shapeP = DEFAULT_SHAPE;
//            boolean labelP = DEFAULT_LABEL;
//            boolean eventCountP = DEFAULT_EVENT_COUNT;
//            boolean percentP = DEFAULT_PERCENT;
//
//            if (element != null) {
//
//                // Parse the id
//                if (element.getChild(ID_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    id = JSPlib.checkStatus(element.getChild(ID_PARAM_NAME).getText(), DEFAULT_ID);
//                }
//
//                // show
//                if (element.getChild(SHOW_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    showP = JSPlib.checkStatus(element.getChild(SHOW_PARAM_NAME).getText(), DEFAULT_SHOW);
//                }
//
//                // shape
//                if (element.getChild(SHAPE_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    shapeP = JSPlib.checkStatus(element.getChild(SHAPE_PARAM_NAME).getText(), DEFAULT_SHAPE);
//                }
//
//                // label
//                if (element.getChild(LABEL_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    labelP = JSPlib.checkStatus(element.getChild(LABEL_PARAM_NAME).getText(), DEFAULT_LABEL);
//                }
//
//                // eventCount
//                if (element.getChild(EVENT_COUNT_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    eventCountP = JSPlib.checkStatus(element.getChild(EVENT_COUNT_PARAM_NAME).getText(), DEFAULT_EVENT_COUNT);
//                }
//
//                // percent
//                if (element.getChild(PERCENT_PARAM_NAME) != null) {
//                    // If there is a matching element
//                    percentP = JSPlib.checkStatus(element.getChild(PERCENT_PARAM_NAME).getText(), DEFAULT_PERCENT);
//                }
//
//                returnRegion = new Region(id, showP, shapeP, labelP, eventCountP, percentP);
//
//            }
//
//            return returnRegion;
//
//        }

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
         * This method is the <code>Illustration.Region</code> implementation of
         * the abstract method in the <code>Comparable</code> interface to allow
         * regions to be sorted by ID.
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
     * ***********************************************************************
     * ***********************************************************************
     * Illustration Parameters
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * The ID of the illustration
     */
    private int id;

    /**
     * The ID of the experiment that created this illustration
     */
    private int creatorExperimentID;

    /**
     * The name or title of the illustration
     */
    private String name;

    /**
     * The filename of the keystone file
     */
    private String keystoneFilename;

    /**
     * The type of the plot
     */
    private int plotType;

    /**
     * The size of the plot
     */
    private int plotSize;

    /**
     * The x channel
     */
    private int xChannel;

    /**
     * The y channel
     */
    private int yChannel;

    /**
     * The z channel
     */
    private int zChannel;

    /**
     * The variable channel
     */
    private int variableChannel;

    /**
     * The compensation id
     */
    private int compensationID;

    /**
     * The number of channels in the illustration
     */
    private int channelCount;

    /**
     * The list of gate set IDs
     */
    private ArrayList gateSetIDs;

    /**
     * The list of regions
     */
    private ArrayList regions;

    /**
     * The scale flags
     */
    private int[] scaleFlags;

    /**
     * The scale arguments
     */
    private String[] scaleArguments;

    /**
     * The scale minimums
     */
    private double[] minimums;

    /**
     * The scale maximums
     */
    private double[] maximums;

    /**
     * The population type; used to get some or all of the events in a
     * population in order or randomly
     */
    private int populationType;

    /**
     * The population cutoff; used to ignore populations with few events
     */
    private int populationCutoff;

    /**
     * The event count; used to set how many events to get from a population
     */
    private int eventCount;

    private int dotSize;

    /**
     * The citation format
     */
    private int citationFormat;

    /**
     * The creatorID
     */
    private int creatorID;

    /**
     * The edit status (a flag for who can edit the illustration)
     */
    private int editStatus;

    /**
     * The view status (a flag for who can view the illustration)
     */
    private int viewStatus;

    /**
     * The menu style
     */
    private int menuStyle;

    /**
     * The boolean flag indicating whether to use white text on a black
     * background for the canvas
     */
    private boolean blackBackgroundP;

    /**
     * The boolean flag indicating whether to use a black background on the plot
     */
    private boolean blackPlotBackgroundP;

    /**
     * The boolean flag indicating whether to use place holders for the plots
     */
    private boolean usePlaceholdersP;

    /**
     * The boolean flag indicating whether to use print view
     */
    private boolean usePrintViewP;

    /**
     * The boolean flag indicating whether to show illustration details
     */
    private boolean showDetailsP;

    /**
     * The boolean flag indicating whether this is a default illustration for a
     * panel set
     */
    private boolean panelDefaultP;

    /**
     * The boolean flag indicating whether to show illustration title
     */
    private boolean showTitleP;

    /**
     * The boolean flag indicating whether to label scales on every plot
     */
    private boolean showScaleLabelsP;

    /**
     * The type of statistic
     */
    private int statType;

    /**
     * The color set
     */
    private int colorSet;

    /**
     * <p>
     * An internal flag, not saved, that indicates whether the channels and
     * scales have been properly set. This is false unless the channelCount is
     * greater than zero and the lengths of the arrays containing the scale
     * flags, arguments, minimums, and maximums are all the same as the
     * channelCount.
     * </p>
     *
     * <p>
     * This flag is used to quickly check whether it is safe to iterate through
     * the scale arrays using the number of channels.
     * </p>
     */
    private boolean channelsAndScalesAreValid;

    /**
     * The annotation boolean flag
     */
    private boolean annotationP;

    /**
     * The boolean flag indicating whether to label scale ticks on every plot
     */
    private boolean showScaleTicksP;

    /**
     * The boolean flag indicating whether to label axes on every plot
     */
    private boolean showAxisLabelsP;

    /**
     * The boolean flag indicating whether to use long labels or power labels
     */
    private boolean longLabelsP;

    /**
     * The smoothing factor double
     */
    private double smoothing;

    /**
     * The double aspect ratio of the plot (height / width)
     */
    private double aspectRatio;

    /**
     * The double indicating percent of events per contour
     */
    private double contourPercent;

    /**
     * The double indicating the percent of events at which to start drawing
     * contours
     */
    private double contourStartPercent;

    /**
     * The list of illustration dimensions
     */
    private ArrayList<IllustrationDimension> dimensions;

    /**
     * The queryString used to create the illustration
     *
     * This is used for development and testing, so there is no accessor
     * function for the creation query string. Instead, you should use
     * getQueryString(). This field will probably be removed or commented out
     * once the class stabilizes.
     */
    private String creationQueryString;

    /**
     * The constant flag of the type of the layoutOverride
     */
    private int layoutOverride;

    /**
     * The constant flag of the statistic of the illustration
     */
    private int statistic;

    /**
     * The constant flag of the equation of the illustration
     */
    private int equation;

    /**
     * The formula of the equation of the illustration
     */
    private String formula;

    /**
     * The constant flag of the control of the illustration
     */
    private int control;

    /**
     * The index of the control row of the illustration
     */
    private int controlRow;

    /**
     * The index of the control column of the illustration
     */
    private int controlColumn;

    /**
     * The dynamic range of the illustration
     */
    private double range;

    /**
     * The row illustration dimension
     */
    private IllustrationDimension row;

    /**
     * The column illustration dimension
     */
    private IllustrationDimension column;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Constructors
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * A constructor for <code>Illustration</code> that takes an experiment id
     * and the name of a keystone flow cytometry file.
     *
     * Any new illustration is a user-specific working illustration until
     * finished and saved by Experiment.
     * </p>
     *
     * @param creatorExperimentID
     * <code>int</code> the ID of the experiment to which this
     * Illustration belongs.
     * @param keystoneFilename
     * <code>String</code> the name of the keystone file.
     * @param creatorID
     * <code>int</code> the ID of the user creating the illustration.
     */
//    public Illustration(int creatorExperimentID, String keystoneFilename, int creatorID) {
//
//        // Initialize the Illustration by setting the defaults
//        setToDefaultValues();
//
//        // Set the ID of the experiment that created this illustration
//        this.creatorExperimentID = creatorExperimentID;
//
//        // Set the ID to be a working illustration ID
//        // This is only changed by Experiment when the user finishes and saves
//        // their personal working illustration
//        this.id = WORKING_ILLUSTRATION_ID;
//
//        // Set the ID of the user that created this illustration
//        this.creatorID = creatorID;
//
//        // Set the keystone filename
//        setKeystoneFilename(keystoneFilename);
//
//        // Set the scales and channels based on the keystone filename and
//        // experiment
//        setScalesAndChannels();
//    }

    /**
     * <p>
     * A constructor for <code>Illustration</code> that takes a string that is
     * assumed to be a querystring formatted Illustration. Any parameters found
     * on this querystring are translated into Illustration parameters. Other
     * Illustration values are set to their defaults.
     *
     * This constructor makes a new <code>Illustration</code> with a new ID.
     * </p>
     *
     * @param queryString
     * <code>String</code> a queryString formatted Illustration.
     */
//    public Illustration(String queryString) {
//        // Set the Illustration defaults
//        setToDefaultValues();
//
//        // Look on the presumed querystring for parameter values
//        setFromQueryString(queryString);
//    }

    /**
     * <p>
     * A constructor for <code>Illustration</code> that takes an Element. Any
     * parameters in the Element are translated into Illustration parameters.
     * Other Illustration values are set to their defaults.
     *
     * This constructor makes a new <code>Illustration</code> object and gets
     * the ID from the Element. If there is no ID in the Element, it will assign
     * it a new ID.
     * </p>
     *
     * @param element
     * <code>Element</code> an Element that will be parsed to yield
     * the Illustration parameters.
     */
//    public Illustration(Element element) {
//        // Set the Illustration defaults
//        setToDefaultValues();
//
//        // Look on the element for parameter values
//        setFromElement(element);
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Helper Functions
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Sets the optional parameters of the Illustration to their default values.
     * </p>
     */
    private void setToDefaultValues() {

        // Set the name of the illustration to a working illustration name
        this.name = "Illustration";

        // Set the plot type of the illustration to the default
        this.plotType = DEFAULT_PLOT_TYPE;

        // set the black canvas background flag to default
        this.blackBackgroundP = DEFAULT_BACKGROUND_FLAG;

        // set the black plot background flag to default
        this.blackPlotBackgroundP = DEFAULT_PLOT_BACKGROUND_FLAG;

        // set the x channel to default
        this.xChannel = DEFAULT_CHANNEL_INDEX;

        // set the y channel to default
        this.yChannel = DEFAULT_CHANNEL_INDEX;

        // set the z channel to default
        this.zChannel = DEFAULT_CHANNEL_INDEX;

        // set the plot size to default
        this.plotSize = DEFAULT_PLOT_SIZE;

        // set the variable channel to the default
        this.variableChannel = DEFAULT_VARIABLE_CHANNEL;

        // set the compensation ID to default
//      this.compensationID = DEFAULT_COMPENSATION_ID;

        // set the channel count to the default
        this.channelCount = DEFAULT_CHANNEL_COUNT;

        // set the gateSetIDs to default
        this.gateSetIDs = DEFAULT_GATE_SET_IDS;

        // set the regions to default
        this.regions = DEFAULT_REGIONS;

        // set the populationType to default
        this.populationType = DEFAULT_POPULATION_TYPE;

        // set the populationCutoff to default
        this.populationCutoff = DEFAULT_POPULATION_CUTOFF;

        // set the eventCount to default
        this.eventCount = DEFAULT_EVENT_COUNT;

        // set the citationFormat to default
        this.citationFormat = DEFAULT_CITATION;

        // set the editStatus to default
        this.editStatus = DEFAULT_EDIT_STATUS;

        // set the viewStatus to default
        this.viewStatus = DEFAULT_VIEW_STATUS;

        // set the menuStyle to default
        this.menuStyle = DEFAULT_MENU_STYLE;

        // set the use placeholders flag to default
        this.usePlaceholdersP = DEFAULT_USE_PLACEHOLDERS_FLAG;

        // set the show labels to default
        this.showScaleLabelsP = DEFAULT_SHOW_SCALE_LABELS_FLAG;

        // set the stat type to default
        this.statType = DEFAULT_STAT_TYPE;

        // set the color set to default
        this.colorSet = DEFAULT_COLOR_SET;

        // set the annotation to default
        this.annotationP = DEFAULT_ANNOTATION_FLAG;

        // set the scale Ticks to default
        this.showScaleTicksP = DEFAULT_SHOW_SCALE_TICKS_FLAG;

        // set the axis labels to default
        this.showAxisLabelsP = DEFAULT_SHOW_AXIS_LABELS_FLAG;

        // set the long labels to default
        this.longLabelsP = DEFAULT_LONG_LABELS_FLAG;

        // set the smoothing to default
        this.smoothing = DEFAULT_SMOOTHING;

        // set the aspect ratio to default
        this.aspectRatio = DEFAULT_ASPECT_RATIO;

        // set the contour percent to default
        this.contourPercent = DEFAULT_CONTOUR_PERCENT;

        // set the contour start percent to default
        this.contourStartPercent = DEFAULT_CONTOUR_START_PERCENT;

        // set the use in print view flag to default
        this.usePrintViewP = DEFAULT_USE_PRINT_VIEW_FLAG;

        // set the panel default flag to default
        this.panelDefaultP = DEFAULT_PANEL_DEFAULT_FLAG;

        // set the show details flag to default
        this.showDetailsP = DEFAULT_SHOW_DETAILS_FLAG;

        // set the show title flag to default
        this.showTitleP = DEFAULT_SHOW_TITLE_FLAG;

        // set the dimensions to a new arraylist
        this.dimensions = new ArrayList<IllustrationDimension>();

        // set the creation query string to default
        this.creationQueryString = DEFAULT_CREATION_QUERY_STRING;

        this.layoutOverride = DEFAULT_LAYOUT_OVERRIDE;

        this.statistic = DEFAULT_STATISTIC;

        this.equation = DEFAULT_EQUATION;

        this.formula = DEFAULT_FORMULA;

        this.control = DEFAULT_CONTROL;

        this.controlRow = DEFAULT_CONTROL_ROW;

        this.controlColumn = DEFAULT_CONTROL_COLUMN;

        this.range = DEFAULT_RANGE;

        this.row = DEFAULT_ROW;

        this.column = DEFAULT_COLUMN;

    }

    /**
     * <p>
     * Returns the next available ID for an Illustration.
     * </p>
     *
     * @return <code>int</code> the value of the illustration id.
     */
//    public int getNextID() {
//        // Go through an array of illustrations, getting the illustration ids,
//        // and find the next one.
//
//        // The gating and working illustration IDs are reserved, so by
//        // definition
//        // there is no lower ID than those; take the greater of the two as the
//        // current maxID (i.e. if the working is greater, take it, otherwise
//        // take
//        // the gating).
//        int maxID = (WORKING_ILLUSTRATION_ID > GATING_ILLUSTRATION_ID) ? WORKING_ILLUSTRATION_ID : GATING_ILLUSTRATION_ID;
//
//        // Get the experiment
//        Experiment experiment = Experiment.getExperimentWithID(this.creatorExperimentID);
//
//        if (experiment != null) {
//            return experiment.getNextIllustrationID();
//        } else {
//            // Return whatever we have at the end of this + 1, which should be a
//            // new
//            // illustration ID
//            return (maxID + 1);
//        }
//
//    }

    /**
     * <p>
     * Returns the next available ID for an IllustrationDimension.
     * </p>
     *
     * @param illustrationDimensions
     * <code>IllustrationDimension[]</code> an array of illustration
     * dimensions
     *
     * @return <code>int</code> the value of the illustration dimension id.
     */
    public int getNextDimensionID(IllustrationDimension[] illustrationDimensions) {
        // Go through an array of illustration dimensions, getting the
        // illustration ids,
        // and find the next one.

        int maxID = 0;

        if (illustrationDimensions != null && illustrationDimensions.length > 0) {
            // If we found illustrationDimensions, look for the highest ID
            for (int i = 0; i < illustrationDimensions.length; i++) {
                if (illustrationDimensions[i] != null) {
                    if (illustrationDimensions[i].getID() > maxID) {
                        // If we find an illustration with an ID bigger than the
                        // current
                        // maxID, then update the maxID
                        maxID = illustrationDimensions[i].getID();
                    }
                }
            }
        }

        // Return whatever we have at the end of this + 1, which should be a new
        // illustration dimension ID
        return (maxID + 1);

    }

    /**
     * <p>
     * Returns the next highest order in an array of illustration dimensions.
     * </p>
     *
     * <p>
     * Note that the passed dimensions must be sorted already in the current
     * implementation.
     * </p>
     *
     * @return <code>int</code> the value of the next highest order.
     */
    public int getNextOrderValue() {

        int nextOrderValue = 0;

        IllustrationDimension[] illustrationDimensions = getActiveDimensions();

        if (illustrationDimensions != null && illustrationDimensions.length > 0) {
            // If we got a valid set of dimensions
            for (int i = 0; i < illustrationDimensions.length; i++) {
                if (nextOrderValue <= illustrationDimensions[i].getOrder()) {
                    nextOrderValue = illustrationDimensions[i].getOrder() + 1;
                }
            }
        }

        return nextOrderValue;

    }

    /**
     * <p>
     * Returns an ordered array of the active IllustrationDimension objects.
     * </p>
     *
     * @return <code>IllustrationDimension[]</code> an ordered array of
     * IllustrationDimension objects corresponding to the active
     * illustration dimensions.
     */
    public IllustrationDimension[] getActiveDimensions() {
        return toArray();
    }

    /**
     * <p>
     * Returns an ordered array of the active FigureDimension IDs.
     * </p>
     *
     * @return <code>int[]</code> an ordered array of ints corresponding to the
     * active figure dimension IDs.
     */
    public int[] getActiveFigureDimensionIDs() {
        IllustrationDimension[] activeDimensions = getActiveDimensions();

        int[] activeIDs = null;

        if (activeDimensions != null && activeDimensions.length > 0) {
            activeIDs = new int[activeDimensions.length];
            for (int k = 0; k < activeDimensions.length; k++) {
                activeIDs[k] = activeDimensions[k].getTargetFigureDimensionID();
            }
        }
        return activeIDs;
    }

    /**
     * <p>
     * Returns an array of the fixed FigureDimensions.
     * </p>
     *
     * @return <code>int[]</code> an array of ints corresponding to the fixed
     * figure dimensions.
     */
    public int[] getFixedFigureDimensionIDs() {
        IllustrationDimension[] activeDimensions = getActiveDimensions();

        int[] activeIDs = null;

        if (activeDimensions != null && activeDimensions.length > 0) {
            activeIDs = new int[activeDimensions.length];
            for (int k = 0; k < activeDimensions.length; k++) {
                activeIDs[k] = activeDimensions[k].getTargetFigureDimensionID();
            }
        }
        return activeIDs;
    }

    /**
     * <p>
     * Returns whether this illustration is equal to the object in the
     * <code>Object</code> object obj.
     * </p>
     *
     * <p>
     * Comparison is performed by comparing the IDs of the two illustrations if
     * the <code>Object</code> object obj is a <code>Illustration</code> object.
     * </p>
     *
     * @param obj
     * <code>Object</code> object to the reference object with which
     * to compare.
     * @return boolean flag indicating whether this illustration is equal to the
     * object in the <code>Object</code> object obj.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            // If the object is null, then return false.
            return false;
        }

        if (obj instanceof Illustration) {
            // If the object is a illustration, then cast it to a illustration.
            Illustration ill = (Illustration) obj;

            // Return whether the IDs are equal
            return (this.getID() == ill.getID());
        } else {
            // Otherwise, the object is not a illustration, so return false.
            return false;
        }
    }

    /**
     * <p>
     * Returns the ID of the illustration as the hash code value for the object.
     * </p>
     *
     * @return int ID of the illustration.
     */
    public int hashCode() {
        return getID();
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Gate set helpers
     * ***********************************************************************
     * ***********************************************************************
     */

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
     * @return true if the illustration changed as a result of the call.
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
     * @return true if the illustration changed as a result of the call.
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
     * illustration ungated.
     * </p>
     */
    public void clearGateSetIDs() {
        gateSetIDs.clear();
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Channel helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the number of channels in the illustration.
     * </p>
     *
     * @return int number of channels in the illustration.
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * <p>
     * Sets the number of channels in the illustration to channelCount.
     * </p>
     *
     * <p>
     * The method only resizes the number of channels in the illustration if the
     * number of channels is greater than the number of channels in the
     * illustration.
     * </p>
     *
     * @param channelCount
     * int number of channels.
     */
    public void setChannelCount(int channelCount) {
        if (channelCount <= this.channelCount) {
            // If the number of channels is less than or equal to the number of
            // channels in the illustration, then quit.
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

        // Loop through the channels and set to the defaults
        for (int i = 0; i < channelCount; i++) {
            // Initialize the scale type flag to 1
            scaleFlags[i] = DEFAULT_SCALE_FLAG;

            // Initialize the channel minimum
            scaleArguments[i] = DEFAULT_SCALE_ARGUMENT;

            // Initialize the channel minimum
            minimums[i] = DEFAULT_SCALE_MINIMUM;

            // Initialize the channel maximum
            maximums[i] = DEFAULT_SCALE_MAXIMUM;
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

        // Set the number of channels in the illustration to the number of
        // channels
        this.channelCount = channelCount;
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Scale helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Sets the channel names and scale parameters based on the current keystone
     * file.
     * </p>
     */
//    public void setScalesAndChannels() {
//        // Start out by setting the channel count to zero
//        // We'll then try and get a flow file and update the channel count and
//        // scales
//        this.channelCount = 0;
//
//        // Initialize an fcsFile
//        fcsFile file = null;
//
//        // If we have a keystone filename, they try to get it
//        if (keystoneFilename != null && keystoneFilename != "") {
//
//            // Get the experiment
//            Experiment currentExp = Experiment.getExperimentWithID(creatorExperimentID);
//
//            // If we have both an experiment and a keystone file name
//            // Try to get an fcsFile based on the keystone file name and
//            // experiment
//            if ((currentExp != null) && (keystoneFilename != null)) {
//                // If the experiment and the filename are not null, then get the
//                // flow file.
//                try {
//                    // Try to get the fcsFile object for the flow file with
//                    // filename filename
//                    // Make sure not to get the events, since we don't need them
//                    file = currentExp.getFile(keystoneFilename, false);
//                } catch (FileNotFoundException fnfe) {
//                    // If a FileNotFoundException occurred, then set the flow
//                    // file to null.
//                    file = null;
//                } catch (IOException ioe) {
//                    // If an IOException occurred, then set the flow file to
//                    // null.
//                    file = null;
//                }
//            }
//
//            // If we found a file, then get the number of channels
//            if (file != null) {
//                // Get the array of channels of the flow file via the experiment
//                // (so we get updates from any panel sets)
//                String[] channels = currentExp.getChannels(file);
//
//                // Set the number of channels in the flow file to the length of
//                // the array of channels
//                if (channels != null)
//                    this.channelCount = channels.length;
//            }
//
//            if (this.channelCount <= 0) {
//                // If the number of channels is less than or equal to 0, then
//                // set the number of channels to 0.
//                this.channelCount = 0;
//            }
//
//        } else {
//            // Otherwise, if we don't have a keystone file name, set the channel
//            // count to zero
//            channelCount = 0;
//        }
//
//        /**
//         * Set the scale parameters
//         */
//
//        // If we found some channels, then set the scales
//        if (this.channelCount > 0) {
//            // Allocate the array of scale type flags
//            scaleFlags = new int[this.channelCount];
//
//            // Allocate the array of scale argument strings
//            scaleArguments = new String[this.channelCount];
//
//            // Allocate the array of channel minimums
//            minimums = new double[this.channelCount];
//
//            // Allocate the array of channel maximums
//            maximums = new double[this.channelCount];
//
//            // Loop through the channels
//            for (int i = 0; i < this.channelCount; i++) {
//
//                // If we have a file, get the channel from the file, otherwise
//                // go with the defaults
//
//                if (file != null) {
//                    // Get the scale type flag of the default scale of the
//                    // current channel
//                    scaleFlags[i] = ScaleDefaults.getDefaultScaleFlag(file.isLog(i), file.isDisplayLog(i));
//
//                    // Get the scale argument string of the scale argument of
//                    // the default scale of the current channel
//                    scaleArguments[i] = ScaleDefaults.getDefaultScaleArgumentString(scaleFlags[i]);
//
//                    // Get the channel minimum of the default scale of the
//                    // current channel
//                    minimums[i] = ScaleDefaults.getDefaultChannelMinimum(scaleFlags[i]);
//
//                    // Get the linear range of the data
//                    maximums[i] = file.getScaleRange(i);
//                } else {
//                    // Go with the defaults
//
//                    // Initialize the scale type flag to 1
//                    scaleFlags[i] = DEFAULT_SCALE_FLAG;
//
//                    // Initialize the scale type flag to 1
//                    scaleArguments[i] = DEFAULT_SCALE_ARGUMENT;
//
//                    // Initialize the channel minimum to the default
//                    minimums[i] = DEFAULT_SCALE_MINIMUM;
//
//                    // Initialize the channel maximum to the default
//                    maximums[i] = DEFAULT_SCALE_MAXIMUM;
//                }
//            }
//
//            // If we found some channels, then give better defaults for x, y,
//            // and z
//            if (this.channelCount > 3) {
//                this.xChannel = DEFAULT_X_CHANNEL;
//                this.yChannel = DEFAULT_Y_CHANNEL;
//                this.zChannel = DEFAULT_Z_CHANNEL;
//            }
//        } else {
//            // We didn't find any channels, so zero out the channels and scales
//            this.channelCount = 0;
//
//            // Allocate the array of scale type flags
//            scaleFlags = new int[this.channelCount];
//
//            // Allocate the array of scale argument strings
//            scaleArguments = new String[this.channelCount];
//
//            // Allocate the array of channel minimums
//            minimums = new double[this.channelCount];
//
//            // Allocate the array of channel maximums
//            maximums = new double[this.channelCount];
//        }
//
//    }

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
            // If the index of the channel is invalid, then return the default.
            return DEFAULT_SCALE_FLAG;
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
            // If the index of the channel is invalid, then return the default.
            return DEFAULT_SCALE_ARGUMENT;
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
            // If the index of the channel is invalid, then return the default
            return DEFAULT_SCALE_MINIMUM;
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
            // If the index of the channel is invalid, then return the default
            // max.
            return DEFAULT_SCALE_MAXIMUM;
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

    public boolean channelsAndScalesAreValid() {
        // Set to default
        boolean areValid = false;

        // Check to see...
        if (channelCount > 0 && scaleFlags != null && scaleArguments != null && minimums != null && maximums != null && scaleFlags.length == channelCount
                && scaleArguments.length == channelCount && minimums.length == channelCount && maximums.length == channelCount) {
            areValid = true;
        }

        return areValid;
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Accessors and Mutators
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the ID of the illustration.
     * </p>
     *
     * @return <code>int</code> the value of the illustration id.
     */
    public int getID() {
        return id;
    }

    /**
     * <p>
     * Returns the ID of the experiment that created the illustration.
     * </p>
     *
     * @return <code>int</code> the value of the creator experiment id.
     */
    public int getCreatorExperimentID() {
        return creatorExperimentID;
    }

    /**
     * <p>
     * Returns the name of the illustration.
     * </p>
     *
     * @return <code>String</code> name of the illustration.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Overrides the toString method to return the name of the illustration.
     * </p>
     *
     * @return <code>String</code> name of the illustration.
     */
    public String toString() {
        return getName();
    }

    /**
     * <p>
     * Sets the creator experiment id of the illustration. Use with caution.
     * </p>
     *
     * @param creatorID
     * <code>int</code> ID of the illustration creator.</p>
     */
    public void setCreatorExperimentID(int creatorID) {
        this.creatorExperimentID = creatorID;
    }

    /**
     * <p>
     * Sets the creator id of the illustration (ID of the user who created the
     * experiment). Use with caution.
     * </p>
     *
     * @param creatorID
     * <code>int</code> ID of the illustration creator.</p>
     */
    public void setCreatorID(int creatorID) {
        this.creatorID = creatorID;
    }

    /**
     * <p>
     * Sets the id of the illustration. Use with caution.
     * </p>
     *
     * @param id
     * <code>int</code> ID of the illustration.</p>
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * <p>
     * Sets the name of the illustration.
     * </p>
     *
     * @param name
     * <code>String</code> name of the illustration.</p>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     * Gets the name of the keystone file, the file used to initiate the
     * illustration.
     * </p>
     *
     * @return <code>String</code> name of the keystone file.</p>
     */
    public String getKeystoneFilename() {
        return keystoneFilename;
    }

    /**
     * <p>
     * Function to set the keystone file name, the file used to initiate the
     * illustration.
     * </p>
     *
     * @param name
     * <code>String</code> name of the keystone file.</p>
     */
    public void setKeystoneFilename(String name) {
        this.keystoneFilename = name;
    }

    /**
     * <p>
     * Returns the filename of the flow file used to create the illustration
     * (aka the keystone file).
     * </p>
     *
     * @return <code>String</code> filename of the flow file used to create the
     * illustration.
     */
    public String getFilename() {
        return getKeystoneFilename();
    }

    /**
     * <p>
     * Sets the filename of the flow file used to create the illustration to
     * filename (aka the keystone file).
     * </p>
     *
     * @param filename
     * <code>String</code> filename of the flow file used to create
     * the illustration.
     */
    public void setFilename(String filename) {
        setKeystoneFilename(filename);
    }

    /**
     * <p>
     * Returns the constant flag of the type of the plot.
     * </p>
     *
     * @return int constant flag of the type of the plot.
     */
    public int getPlotType() {
        // To be legacy compatible, this should return a special value for
        // histogram overlays
        if (isHistogramOverlay()) {
            return HISTOGRAM_OVERLAY_PLOT_TYPE;
        } else {
            return plotType;
        }
    }

    /**
     * <p>
     * Returns whether the plot is a pseudo 3D plot.
     * </p>
     *
     * @return boolean flag indicating whether the plot is a pseudo 3D plot.
     */
    public boolean isPseudo3DPlot() {
        return ((plotType == THIRD_AXIS_MEDIAN_PLOT) || (plotType == THIRD_AXIS_NINETYFIFTH_PLOT));
    }

    /**
     * <p>
     * Returns whether the plot is a 1D plot.
     * </p>
     *
     * @return boolean flag indicating whether the plot is a 1D plot.
     */
    public boolean is1DPlot() {
        return ((plotType == HISTOGRAM_X) || (plotType == HISTOGRAM_Y) || (plotType == HISTOGRAM_OVERLAY_PLOT_TYPE) || (isOneImageHeatmap()));
    }

    /**
     * <p>
     * Sets the type of the plot to the type indicated by the constant flag of
     * the type of the plot type.
     * </p>
     *
     * @param type
     * <code>int</code> constant flag of the type of the plot.
     */
    public void setPlotType(int type) {
        // Sort the array of valid plot types
        Arrays.sort(VALID_PLOT_TYPES);

        // Check the array of valid plot types for the passed type
        if (Arrays.binarySearch(VALID_PLOT_TYPES, type) > -1) {
            // If the passed type is found in the array of valid plot types,
            // then
            // Set the type of the plot based on the constant flag
            this.plotType = type;
        } else {
            // Otherwise, set the plot type to the test pattern as a warning
            this.plotType = TEST_PATTERN;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the size of the plot.
     * </p>
     *
     * @return int constant flag of the size of the plot.
     */
    public int getPlotSize() {
        return plotSize;
    }

    /**
     * <p>
     * Sets the size of the plot to the type indicated by the constant flag of
     * the type of the plot size.
     * </p>
     *
     * @param size
     * <code>int</code> constant flag of the size of the plot.
     */
    public void setPlotSize(int size) {
        // Sort the array of valid plot types
        Arrays.sort(VALID_PLOT_SIZES);

        // Check the array of valid plot types for the passed type
        if (Arrays.binarySearch(VALID_PLOT_SIZES, size) > -1) {
            // If the passed size type is found in the array of valid plot
            // sizes, then
            // Set the size of the plot
            this.plotSize = size;
        } else {
            // Otherwise, set the plot size to the default
            this.plotSize = DEFAULT_PLOT_SIZE;
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
        // Sort the array of valid statistics
        Arrays.sort(VALID_STATISTICS);

        // Check the array of valid statistics for the passed type
        if (Arrays.binarySearch(VALID_STATISTICS, type) > -1) {
            // If the passed type is found in the array of valid statistics,
            // then
            // Set the type of the statistic based on the type
            this.statType = type;
        } else {
            // Otherwise, set the statistic to the default
            this.statType = DEFAULT_STAT_TYPE;
        }
    }

    /**
     * <p>
     * Returns the x channel of the plot.
     * </p>
     *
     * @return int x channel of the plot.
     */
    public int getXChannel() {
        return xChannel;
    }

    /**
     * <p>
     * Sets the x channel of the Illustration.
     * </p>
     *
     * @param channel
     * <code>int</code> x channel of the illustration.</p>
     */
    public void setXChannel(int channel) {
        if ((channel >= -1) && (channel < this.channelCount)) {
            // If the index of the x channel is valid, then set the x channel to
            // channel.
            this.xChannel = channel;
        }
    }

    /**
     * <p>
     * Returns the y channel of the plot.
     * </p>
     *
     * @return int y channel of the plot.
     */
    public int getYChannel() {
        return yChannel;
    }

    /**
     * <p>
     * Returns the file mapping for the keystone file, plucked from a list of
     * file mappings.
     * </p>
     *
     * @return FileMapping the filemapping of the keystone file
     */
//    public FileMapping getKeystoneFileMapping(FileMapping[] fileMappings) {
//
//        FileMapping foundFileMapping = null;
//
//        // If we're passed nothing, return nothing
//        if (fileMappings != null && fileMappings.length > 0) {
//            // Look for the keystone file mapping
//            for (int k = 0; k < fileMappings.length; k++) {
//                // Look for the name of the keystone file
//                if (fileMappings[k] != null && fileMappings[k].getName().equals(getKeystoneFilename())) {
//                    // If we find the name of the keystone file, then return the
//                    // file mapping
//                    foundFileMapping = fileMappings[k];
//                    break;
//                }
//            }
//        }
//
//        return foundFileMapping;
//
//    }

    /**
     * <p>
     * Returns the membership sets for the keystone file, plucked from a list of
     * membership sets.
     * </p>
     *
     * @return MembershipSet[] an array of membership sets mapped to the
     * keystone file
     */
//    public MembershipSet[] getKeystoneMembershipSets(FileMapping[] fileMappings) {
//
//        MembershipSet[] keystoneMembershipSets = null;
//
//        // If we're passed nothing, do nothing
//        if (fileMappings != null && fileMappings.length > 0) {
//
//            // Get the keystone file mapping
//            FileMapping keystoneFileMapping = getKeystoneFileMapping(fileMappings);
//
//            // If there is a keystone filemapping
//            // then convert it to membership sets and return them
//            if (keystoneFileMapping != null) {
//                keystoneMembershipSets = keystoneFileMapping.toArray();
//            }
//
//        }
//
//        return keystoneMembershipSets;
//
//    }

    /**
     * <p>
     * Returns any panel sets associated with the keystone file, plucked from an
     * existing array of membership sets.
     * </p>
     *
     * @return PanelSet[] an array of PanelSet objects associated with the
     * keystone file.
     */
//    public PanelSet[] getKeystonePanelSets(FileMapping[] fileMappings) {
//
//        PanelSet[] foundPanelSets = null;
//
//        // Create a list to hold the found panel sets
//        ArrayList<PanelSet> panelSetList = new ArrayList<PanelSet>();
//
//        // If we're passed nothing, do nothing
//        if (fileMappings != null && fileMappings.length > 0) {
//
//            // Get the membership sets that are associated with the keystone
//            // file
//            MembershipSet[] keystoneMembershipSets = getKeystoneMembershipSets(fileMappings);
//
//            // If we find some keystone membership sets in the passed array
//            if (keystoneMembershipSets != null && keystoneMembershipSets.length > 0) {
//
//                for (int j = 0; j < keystoneMembershipSets.length; j++) {
//                    // If the current membershipSet is a PanelSet
//                    // Then add it to the list
//                    if (keystoneMembershipSets[j] != null && keystoneMembershipSets[j].getType().equals("Panel Set")) {
//                        // Cast the current membership as a Panel Set and add it
//                        // to the list
//                        PanelSet currentPanelSet = (PanelSet) keystoneMembershipSets[j];
//                        panelSetList.add(currentPanelSet);
//                    }
//                }
//            }
//
//            // If we found some panel sets, then convert them to the array that
//            // we will return
//            if (panelSetList != null && panelSetList.size() > 0) {
//                // Allocate an array to hold the panel sets
//                foundPanelSets = new PanelSet[panelSetList.size()];
//
//                // Copy the panel sets to the array
//                panelSetList.toArray(foundPanelSets);
//            }
//        }
//
//        return foundPanelSets;
//    }

//    public PanelChannel getKeystoneYPanelChannel(FileMapping[] fileMappings, MembershipSet[] membershipSets) {
//        return (getKeystonePanelChannel(fileMappings, membershipSets, getYChannel()));
//    }
//
//    public PanelChannel getKeystoneXPanelChannel(FileMapping[] fileMappings, MembershipSet[] membershipSets) {
//        return (getKeystonePanelChannel(fileMappings, membershipSets, getXChannel()));
//    }
//
//    public PanelChannel getKeystoneZPanelChannel(FileMapping[] fileMappings, MembershipSet[] membershipSets) {
//        return (getKeystonePanelChannel(fileMappings, membershipSets, getZChannel()));
//    }

    /**
     * <p>
     * Finds the PanelChannel object matching the keystone file's passed channel
     * index.
     * </p>
     *
     * @return PanelChannel the PanelChannel of the keystone file's Y channel.
     */
//    public PanelChannel getKeystonePanelChannel(FileMapping[] fileMappings, MembershipSet[] membershipSets, int channel) {
//        PanelChannel foundPanelChannel = null;
//
//        // If we're passed nothing, do nothing
//        if (fileMappings != null && fileMappings.length > 0 && membershipSets != null && membershipSets.length > 0) {
//            // Check that the channel is valid
//            if (channel > -1 && channel < channelCount) {
//
//                PanelSet[] keystonePanelSets = getKeystonePanelSets(fileMappings);
//
//                // If we found some panel sets
//                if (keystonePanelSets != null && keystonePanelSets.length > 0) {
//                    PanelSet firstKeystonePanelSet = keystonePanelSets[0];
//
//                    // Look for the matching Panel Channel in the membership set
//                    // array
//                    for (int j = 0; j < membershipSets.length; j++) {
//                        if (membershipSets[j] != null && membershipSets[j].getType() != null && membershipSets[j].getType().equals("Panel Channel")) {
//                            PanelChannel currentPanelChannel = (PanelChannel) membershipSets[j];
//
//                            // Found a panel channel, now check it against the
//                            // Panel Set and channel
//                            if (currentPanelChannel.getPanelID() == firstKeystonePanelSet.getID() && currentPanelChannel.getChannel() == channel) {
//                                // Wow... we found it! Assign it to
//                                // foundPanelChannel and get out of this loop
//                                foundPanelChannel = currentPanelChannel;
//                                break;
//
//                            }
//
//                        }
//
//                    }
//
//                }
//            }
//        }
//
//        return foundPanelChannel;
//    }

    /**
     * <p>
     * Returns the z channel of the plot.
     * </p>
     *
     * @return int z channel of the plot.
     */
    public int getZChannel() {
        return zChannel;
    }

    /**
     * <p>
     * Sets the y channel of the Illustration.
     * </p>
     *
     * @param channel
     * <code>int</code> y channel of the illustration.</p>
     */
    public void setYChannel(int channel) {
        if ((channel >= -1) && (channel < this.channelCount)) {
            // If the index of the y channel is valid, then set the y channel to
            // channel.
            this.yChannel = channel;
        }
    }

    /**
     * <p>
     * Sets the z channel of the Illustration.
     * </p>
     *
     * @param channel
     * <code>int</code> z channel of the illustration.</p>
     */
    public void setZChannel(int channel) {
        if ((channel >= -1) && (channel < this.channelCount)) {
            // If the index of the z channel is valid, then set the z channel to
            // channel.
            this.zChannel = channel;
        }
    }

    /**
     * <p>
     * Sets the int flag of the variable channel.
     * </p>
     *
     * This looks at the plot type, layout override and whether channels are are
     * variable dimension in the illustration dimensions to determine which
     * channel (x, y, z) should be the one shown in a standard plot or
     * overridden in a dynamic layout.
     *
     * @param figureDimensions
     * <code>FigureDimension[]</code> array of figure dimensions.</p>
     */
//    public int getVariableChannel(FigureDimension[] figureDimensions) {
//        int variableChannel = VARY_NONE;
//
//        if (channelsAreVariable(figureDimensions)) {
//            variableChannel = getVariableChannel();
//        } else {
//            // If channels are not variable, then look at the number of
//            // dimensions to
//            // figure out the variable channel
//            setVariableChannelToDefault();
//        }
//
//        checkVariableChannelBoundaries();
//
//        return variableChannel;
//    }

    /**
     * <p>
     * Sets the variable channel of the illustration directly.
     * </p>
     */
    public void setVariableChannelToDefault() {
        if (is1DPlot()) {
            this.variableChannel = VARY_X;
        } else if (isPseudo3DPlot()) {
            this.variableChannel = VARY_Z;
        } else {
            this.variableChannel = VARY_Y;
        }
        checkVariableChannelBoundaries();
    }

    /**
     * <p>
     * Sets the variable channel of the illustration directly.
     * </p>
     */
    public void checkVariableChannelBoundaries() {
        // Check the boundaries and reset if needed
        if (is1DPlot()) {
            if (this.variableChannel != VARY_X)
                this.variableChannel = VARY_X;
        } else if (isPseudo3DPlot()) {
            if (this.variableChannel > VARY_Z)
                this.variableChannel = VARY_Z;
        } else {
            if (this.variableChannel > VARY_Y)
                this.variableChannel = VARY_Y;
        }

        if (this.variableChannel < 0)
            this.variableChannel = VARY_X;
    }

    /**
     * <p>
     * Sets the variable channel of the illustration directly.
     * </p>
     *
     * @param value
     * <code>int</code> variable channel illustration.</p>
     */
    public void setVariableChannel(int value) {
        if (value > 0) {
            this.variableChannel = value;
        } else {
            setVariableChannelToDefault();
        }
        checkVariableChannelBoundaries();
    }

    /**
     * <p>
     * Returns the int flag of the variable channel.
     * </p>
     *
     * @return int variable channel flag
     */
    public int getVariableChannel() {
        checkVariableChannelBoundaries();
        return this.variableChannel;
    }

    /**
     * <p>
     * Returns the compensation id of the illustration.
     * </p>
     *
     * @return int compensationID of the illustration
     */
    public int getCompensationID() {
        return compensationID;
    }

    /**
     * <p>
     * Sets the compensationID of the illustration.
     * </p>
     *
     * @param value
     * <code>int</code> compensationID of the illustration.</p>
     */
    public void setCompensationID(int value) {
        this.compensationID = value;
    }

    /**
     * <p>
     * Returns the citation format of the illustration.
     * </p>
     *
     * @return int citation format of the illustration
     */
    public int getCitationFormat() {
        return citationFormat;
    }

    /**
     * <p>
     * Returns the id of the illustration creator.
     * </p>
     *
     * @return int creator id of the illustration
     */
    public int getCreatorID() {
        return creatorID;
    }

    /**
     * <p>
     * Returns the view status of the illustration.
     * </p>
     *
     * @return int view status of the illustration
     */
    public int getViewStatus() {
        return viewStatus;
    }

    /**
     * <p>
     * Sets the view status to that indicated by the passed viewStatus flag.
     * </p>
     *
     * @param viewStatus
     * int constant flag of the viewStatus.
     */
    public void setViewStatus(int viewStatus) {
        // Sort the array of valid view statuses
        Arrays.sort(VALID_VIEW_STATUS);

        // Check the array of valid statistics for the passed type
        if (Arrays.binarySearch(VALID_VIEW_STATUS, viewStatus) > -1) {
            // If the passed type is found in the array, then
            // Set the type based on the passed type
            this.viewStatus = viewStatus;
        } else {
            // Otherwise, set it to the default
            this.viewStatus = DEFAULT_VIEW_STATUS;
        }
    }

    /**
     * <p>
     * Sets the edit status to that indicated by the passed editStatus flag.
     * </p>
     *
     * @param editStatus
     * int constant flag of the editStatus.
     */
    public void setEditStatus(int editStatus) {
        // Sort the array of valid edit statuses
        Arrays.sort(VALID_EDIT_STATUS);

        // Check the array of valid statistics for the passed type
        if (Arrays.binarySearch(VALID_EDIT_STATUS, editStatus) > -1) {
            // If the passed type is found in the array, then
            // Set the type based on the passed type
            this.editStatus = editStatus;
        } else {
            // Otherwise, set it to the default
            this.editStatus = DEFAULT_EDIT_STATUS;
        }
    }

    /**
     * <p>
     * Returns the editStatus of the illustration.
     * </p>
     *
     * @return int editStatus of the illustration
     */
    public int getEditStatus() {
        return editStatus;
    }

    /**
     * <p>
     * Returns the menu style of the illustration.
     * </p>
     *
     * @return int menu style of the illustration
     */
    public int getMenuStyle() {
        return menuStyle;
    }

    /**
     * <p>
     * Returns whether to use white text on a black background. Legacy function.
     * </p>
     *
     * @return boolean flag indicating whether to use white text on a black
     * background.
     */
    public boolean useBlackBackground() {
        return this.blackBackgroundP;
    }

    /**
     * <p>
     * Sets the blackBackgroundP boolean of the Illustration.
     * </p>
     *
     * @param blackBackgroundP
     * <code>boolean</code> backBackgroundP flag of the
     * illustration.</p>
     */
    public void setBlackBackground(boolean blackBackgroundP) {
        this.blackBackgroundP = blackBackgroundP;
    }

    /**
     * <p>
     * Returns the value of the blackPlotBackgroundP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the blackPlotBackgroundP flag.
     */
    public boolean useBlackPlotBackground() {
        return this.blackPlotBackgroundP;
    }

    /**
     * <p>
     * Sets the blackPlotBackgroundP boolean of the Illustration.
     * </p>
     *
     * @param blackPlotBackgroundP
     * <code>boolean</code> blackPlotBackgroundP flag of the
     * illustration.</p>
     */
    public void setBlackPlotBackground(boolean blackPlotBackgroundP) {
        this.blackPlotBackgroundP = blackPlotBackgroundP;
    }

    /**
     * <p>
     * Sets the usePlaceholdersP boolean of the Illustration.
     * </p>
     *
     * @param value
     * <code>boolean</code> usePlaceholdersPP flag of the
     * illustration.</p>
     */
    public void setUsePlaceholders(boolean value) {
        this.usePlaceholdersP = value;
    }

    /**
     * <p>
     * Returns the value of the usePlaceholdersP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the usePlaceholdersP flag.
     */
    public boolean usePlaceholders() {
        return this.usePlaceholdersP;
    }

    /**
     * <p>
     * Returns the path of the placerholder image. This is an alternative to the
     * more costly version where a querystring for a placeholder is requested
     * and sent to the plotServlet.
     * </p>
     *
     * @return <code>String</code> the path to the placeholder image.
     */
    public String getPlaceholderImagepath() {
        return this.placeholder_small_imagepath;
    }

    /**
     * <p>
     * Returns the value of the stat type.
     * </p>
     *
     * @return <code>int</code> the value of the stat type.
     */
    public int getStatType() {
        return this.statType;
    }

    /**
     * <p>
     * Returns the value of the color set.
     * </p>
     *
     * @return <code>int</code> the value of the color set.
     */
    public int getColorSet() {
        return this.colorSet;
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
        // Sort the array of valid color sets
        Arrays.sort(VALID_COLOR_SETS);

        // Check the array of valid statistics for the passed type
        if (Arrays.binarySearch(VALID_COLOR_SETS, colorSet) > -1) {
            // If the passed type is found in the array of valid color sets,
            // then
            // Set the type of the color set based on the type
            this.colorSet = colorSet;
        } else {
            // Otherwise, set the statistic to the default
            this.colorSet = DEFAULT_COLOR_SET;
        }
    }

    /**
     * <p>
     * Checks that the color set works for gradient visualizations, like
     * heatmaps, and resets it if not.
     * </p>
     */
    public void checkGradientColorSet() {
        // Sort the array of valid color sets
        Arrays.sort(VALID_GRADIENT_COLOR_SETS);

        // Check the array of valid statistics for the passed type
        if (Arrays.binarySearch(VALID_GRADIENT_COLOR_SETS, this.colorSet) > -1) {
            // If the passed type is found in the array of valid color sets,
            // then
            // do nothing, we're ok
        } else {
            // Otherwise, set to the default color set, which should work for
            // gradients
            this.colorSet = DEFAULT_COLOR_SET;
        }
    }

    /**
     * <p>
     * Returns whether there are colors used in the illustration.
     * </p>
     *
     * @return <code>boolean</code> indicating whether colors are used.
     */
    public boolean isUncolored() {
        boolean isUncolored = false;
        if (plotType == HISTOGRAM_X || plotType == HISTOGRAM_Y || plotType == DOT_PLOT || plotType == CONTOUR_PLOT) {
            isUncolored = true;
        }
        return isUncolored;
    }

    /**
     * <p>
     * Returns the value of the annotation flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the annotationP flag.
     */
    public boolean drawAnnotation() {
        return this.annotationP;
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
     * Returns the value of the showScaleTicksP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the showScaleTicksP flag.
     */
    public boolean drawScaleTick() {
        return this.showScaleTicksP;
    }

    /**
     * <p>
     * Sets whether to draw the scale ticks to scaleTickP.
     * </p>
     *
     * @param showScaleTicksP
     * boolean flag indicating whether to draw the scale ticks.
     */
    public void setScaleTick(boolean showScaleTicksP) {
        this.showScaleTicksP = showScaleTicksP;
    }

    /**
     * <p>
     * Returns the value of the showScaleLabelsP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the showScaleLabelsP flag.
     */
    public boolean drawScaleLabel() {
        return this.showScaleLabelsP;
    }

    /**
     * <p>
     * Sets the showScaleLabelsP boolean of the Illustration, which flags
     * whether to show the numerical labels of the scales.
     * </p>
     *
     * @param value
     * <code>boolean</code> showScaleLabelsP flag of the
     * illustration.</p>
     */
    public void setScaleLabel(boolean value) {
        this.showScaleLabelsP = value;
    }

    /**
     * <p>
     * Returns the value of the showAxisLabelsP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the showAxisLabelsP flag.
     */
    public boolean drawAxisLabel() {
        return this.showAxisLabelsP;
    }

    /**
     * <p>
     * Sets whether to draw the axis labels to showAxisLabelsP.
     * </p>
     *
     * @param showAxisLabelsP
     * boolean flag indicating whether to draw the axis labels.
     */
    public void setAxisLabel(boolean showAxisLabelsP) {
        this.showAxisLabelsP = showAxisLabelsP;
    }

    /**
     * <p>
     * Returns the value of the longLabelsP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the longLabelsP flag.
     */
    public boolean useLongLabel() {
        return this.longLabelsP;
    }

    /**
     * <p>
     * Sets whether to use long labels to longLabelsP.
     * </p>
     *
     * @param longLabelsP
     * boolean flag indicating whether to use long labels.
     */
    public void setLongLabel(boolean longLabelsP) {
        this.longLabelsP = longLabelsP;
    }

    /**
     * <p>
     * Returns the number of axis bins on each axis.
     * </p>
     *
     * @return int number of axis bins on each axis.
     */
    public int getAxisBins() {
        return this.plotSize;
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
            this.plotSize = bins;
        }
    }

    /**
     * <p>
     * Returns the value of the smoothing.
     * </p>
     *
     * @return <code>double</code> the value of the smoothing.
     */
    public double getSmoothing() {
        return this.smoothing;
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
     * Returns the value of the aspectRatio.
     * </p>
     *
     * @return <code>boolean</code> the value of the aspectRatio.
     */
    public double getAspectRatio() {
        return this.aspectRatio;
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
     * Returns the value of the contourPercent.
     * </p>
     *
     * @return <code>boolean</code> the value of the contourPercent.
     */
    public double getContourPercent() {
        return this.contourPercent;
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
     * Returns the value of the contourStartPercent.
     * </p>
     *
     * @return <code>boolean</code> the value of the contourStartPercent.
     */
    public double getContourStartPercent() {
        return this.contourStartPercent;
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
     * <p>
     * Returns the value of the usePrintviewP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the usePrintviewP flag.
     */
    public boolean usePrintView() {
        return this.usePrintViewP;
    }

    /**
     * <p>
     * Sets the usePrintViewP boolean of the Illustration.
     * </p>
     *
     * @param value
     * <code>boolean</code> usePrintViewP flag of the
     * illustration.</p>
     */
    public void setUsePrintView(boolean value) {
        this.usePrintViewP = value;
    }

    /**
     * <p>
     * Returns the value of the panelDefaultP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the panelDefaultP flag.
     */
    public boolean isPanelDefaultRepresentation() {
        return this.panelDefaultP;
    }

    /**
     * <p>
     * Sets whether this is the default illustration for a panel set to
     * panelDefaultP.
     * </p>
     *
     * @param panelDefaultP
     * boolean flag indicating whether this is the default
     * illustration for a panel set.
     */
    public void setPanelDefaultRepresentation(boolean panelDefaultP) {
        this.panelDefaultP = panelDefaultP;
    }

    /**
     * <p>
     * Returns the value of the showDetailsP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the showDetailsP flag.
     */
    public boolean showDetails() {
        return this.showDetailsP;
    }

    /**
     * <p>
     * Returns the value of the showTitleP flag.
     * </p>
     *
     * @return <code>boolean</code> the value of the showTitleP flag.
     */
    public boolean showTitle() {
        return this.showTitleP;
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
     * Returns the constant flag of the cutoff of the population.
     * </p>
     *
     * @return int cutoff of the population.
     */
    public int getPopulationCutoff() {
        return populationCutoff;
    }

    /**
     * <p>
     * Sets the cutoff of the population to that indicated by the constant flag
     * of the cutoff of the population.
     * </p>
     *
     * @param cutoff
     * int cutoff of the population.
     */
    public void setPopulationCutoff(int cutoff) {
        populationCutoff = cutoff;
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
     * Returns the size of the dots to plot
     */
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
     * <p>
     * Returns the constant flag of the layoutOverride.
     * </p>
     *
     * @return int constant flag of the layoutOverride.
     */
    public int getLayoutOverride() {
        return layoutOverride;
    }

    /**
     * <p>
     * Sets the layoutOverride to the type indicated by the constant flag of the
     * type.
     * </p>
     *
     * @param type
     * <code>int</code> constant flag of the type of layoutOverride.
     */
    public void setLayoutOverride(int type) {
        // Sort the array of valid types
        Arrays.sort(VALID_LAYOUT_OVERRIDE_TYPES);

        // Check the array of valid types for the passed type
        if (Arrays.binarySearch(VALID_LAYOUT_OVERRIDE_TYPES, type) > -1) {
            // If the passed type is found in the array of valid types, then
            // Set the type based on the constant flag
            this.layoutOverride = type;
        } else {
            // Otherwise, set the type to the default
            this.layoutOverride = DEFAULT_LAYOUT_OVERRIDE;
        }

        if (layoutOverride == HISTOGRAM_OVERLAY_WITH_STATISTICS || layoutOverride == HISTOGRAM_OVERLAY) {
            this.plotType = HISTOGRAM_OVERLAY_PLOT_TYPE;
        }

    }

    /**
     * <p>
     * Returns the constant flag of the layoutOverride. Legacy code.
     * </p>
     *
     * @return int constant flag of the layoutOverride.
     */
    public int getType() {
        return getLayoutOverride();
    }

    /**
     * <p>
     * Sets the layoutOverride to the type indicated by the constant flag of the
     * type. Legacy code.
     * </p>
     *
     * @param type
     * <code>int</code> constant flag of the type of layoutOverride.
     */
    public void setType(int type) {
        setLayoutOverride(type);
    }

    /**
     * <p>
     * Returns whether the layoutOverride is a one image heatmap.
     * </p>
     *
     * @return boolean flag indicating whether this is a one image heatmap.
     */
    public boolean isOneImageHeatmap() {
        if (layoutOverride == HEATMAP_ONE_IMAGE || layoutOverride == HEATMAP_ONE_IMAGE_WITH_STATISTICS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Returns whether the layoutOverride is a heatmap with view through.
     * </p>
     *
     * @return boolean flag indicating whether this is a heatmap with view
     * through.
     */
    public boolean isHeatmapWithViewthrough() {
        if (layoutOverride == HEATMAP || layoutOverride == HEATMAP_WITH_STATISTICS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Returns whether the layoutOverride is any type of heatmap.
     * </p>
     *
     * @return boolean flag indicating whether this is a heatmap of any type.
     */
    public boolean isHeatmap() {
        if (isHeatmapWithViewthrough() || isOneImageHeatmap()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Returns whether stats are being used for heatmaps or histogram overlays.
     * </p>
     *
     * @return boolean flag indicating whether stats are being used by a
     * heatmap.
     */
    public boolean getUsesStats() {
        if (layoutOverride == HEATMAP_ONE_IMAGE_WITH_STATISTICS || layoutOverride == HEATMAP_WITH_STATISTICS
                || layoutOverride == HISTOGRAM_OVERLAY_WITH_STATISTICS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Returns whether the layoutOverride is a histogram overlay type.
     * </p>
     *
     * @return boolean flag indicating whether the figure dimension in the
     * illustration is using a histogram overlay.
     */
    public boolean isHistogramOverlay() {
        if (layoutOverride == HISTOGRAM_OVERLAY || layoutOverride == HISTOGRAM_OVERLAY_WITH_STATISTICS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Statistics and controls
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the constant flag of the statistic type.
     * </p>
     *
     * @return int constant flag of the statistic type.
     */
    public int getStatistic() {
        return statistic;
    }

    /**
     * <p>
     * Sets the statistic to that indicated by the constant statistic.
     * </p>
     *
     * @param statistic
     * <code>int</code> constant type of statistic.
     */
    public void setStatistic(int statistic) {
        // Sort the array of valid types
        Arrays.sort(VALID_STATISTICS);

        // Check the array of valid types for the passed type
        if (Arrays.binarySearch(VALID_STATISTICS, statistic) > -1) {
            // If the passed type is found in the array of valid statistics,
            // then
            // Set the type based on the constant flag
            this.statistic = statistic;
        } else {
            // Otherwise, set the type to the default
            this.statistic = DEFAULT_STATISTIC;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the equation.
     * </p>
     *
     * @return int constant flag of the equation.
     */
    public int getEquation() {
        return equation;
    }

    /**
     * <p>
     * Sets the equation to that indicated by the constant equation.
     * </p>
     *
     * @param equation
     * <code>int</code> constant type of equation.
     */
    public void setEquation(int equation) {
        // Sort the array of valid types
        Arrays.sort(PopulationGrid.VALID_EQUATIONS);

        // Check the array of valid types for the passed type
        if (Arrays.binarySearch(PopulationGrid.VALID_EQUATIONS, equation) > -1) {
            // If the passed type is found in the array of valid equations, then
            // Set the type based on the constant flag
            this.equation = equation;
        } else {
            // Otherwise, set the type to the default
            this.equation = DEFAULT_EQUATION;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the control to use when calculating.
     * </p>
     *
     * @return int constant flag of the control.
     */
    public int getControl() {
        return control;
    }

    /**
     * <p>
     * Sets the control flag to that indicated by the constant control.
     * </p>
     *
     * @param control
     * <code>int</code> constant type of control.
     */
    public void setControl(int control) {
        // Sort the array of valid types
        Arrays.sort(PopulationGrid.VALID_CONTROLS);

        // Check the array of valid types for the passed type
        if (Arrays.binarySearch(PopulationGrid.VALID_CONTROLS, control) > -1) {
            // If the passed type is found in the array of valid types, then
            // Set the type based on the constant flag
            this.control = control;
        } else {
            // Otherwise, set the type to the default
            this.control = DEFAULT_CONTROL;
        }
    }

    /**
     * <p>
     * Returns the index of the control row of the illustration or -1 if it is
     * not set.
     * </p>
     *
     * @return int index of the control row of the illustration or -1 if it is
     * not set.
     */
    public int getControlRow() {
        return controlRow;
    }

    /**
     * <p>
     * Sets the index of the control row of the illustration to row.
     * </p>
     *
     * @param row
     * int index of the control row of the illustration.
     */
    public void setControlRow(int row) {
        controlRow = row;
    }

    /**
     * <p>
     * Returns the index of the control column of the illustration or -1 if it
     * is not set.
     * </p>
     *
     * @return int index of the control column of the illustration or -1 if it
     * is not set.
     */
    public int getControlColumn() {
        return controlColumn;
    }

    /**
     * <p>
     * Sets the index of the control column of the illustration to column.
     * </p>
     *
     * @param column
     * int index of the control column of the illustration.
     */
    public void setControlColumn(int column) {
        controlColumn = column;
    }

    /**
     * <p>
     * Returns the formula of the equation of the illustration.
     * </p>
     *
     * @return <code>String</code> formula of the equation of the illustration.
     */
    public String getFormula() {
        return formula;
    }

    /**
     * <p>
     * Sets the formula of the equation of the illustration to formula.
     * </p>
     *
     * @param formula
     * <code>String</code> formula of the equation of the
     * illustration.
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * <p>
     * Returns the dynamic range of the illustration.
     * </p>
     *
     * @return double dynamic range of the illustration.
     */
    public double getRange() {
        return range;
    }

    /**
     * <p>
     * Sets the dynamic range of the illustration to the absolute value of the
     * value in range.
     * </p>
     *
     * @param range
     * double dynamic range of the illustration.
     */
    public void setRange(double range) {
        this.range = Math.abs(range);
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Illustration dimension helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the ID of the row illustration dimension.
     * </p>
     *
     * @return int ID of the row illustration dimension.
     */
    public int getRowID() {
        if (row == null) {
            // If the row illustration dimension is null, then return -1.
            return -1;
        } else {
            // Otherwise, the row illustration dimension is not null
            // so return the ID of the row illustration dimension.
            return row.getID();
        }
    }

    /**
     * <p>
     * Returns the illustration dimension for the row, which is the second
     * dimension in the order index.
     * </p>
     *
     * @return <code>IllustrationDimension</code> object to the illustration
     * dimension for the row.
     */
    public IllustrationDimension getRow() {
        if (dimensions.size() > 1) {
            // If there are more than one illustration dimension in the list of
            // illustration dimensions, then sort them
            // by order index.
            Collections.sort(dimensions);

            // Return the second member, index 1
            return dimensions.get(1);
        } else {
            // Otherwise return null, there is no row
            return null;
        }
    }

    /**
     * <p>
     * Sets the illustration dimension for the row to row.
     * </p>
     *
     * @param row
     * <code>IllustrationDimension</code> object of the illustration
     * dimension for the row.
     */
    public void setRow(IllustrationDimension row) {
        this.row = row;
    }

    /**
     * <p>
     * Returns the number of rows in the illustration.
     * </p>
     *
     * <p>
     * The number of rows returned by this method is an approximation of the
     * actual number of rows in the final illustration calculated from the
     * number of selected indices in the row illustration dimension. If the
     * underlying figure dimension changed so that any of the indices were no
     * longer valid, then the number of rows returned by this method will be
     * different than the actual number of rows in the final illustration. In
     * any case, the number of rows returned by this method is an upper bound on
     * the actual number of rows in the final illustration as invalid indices
     * are ignored.
     * </p>
     *
     * @return int number of rows in the illustration.
     */
    public int getRowCount() {
        if (row == null) {
            // If the row illustration dimension is null, then return 0.
            return 0;
        } else {
            // Otherwise, the row illustration dimension is not null, so return
            // the number of indices in the row illustration dimension.
            return row.getIndexCount();
        }
    }

    /**
     * <p>
     * Returns the ID of the column figure dimension.
     * </p>
     *
     * @return int ID of the column figure dimension.
     */
    public int getColumnID() {
        if (column == null) {
            // If the column illustration dimension is null, then return -1.
            return -1;
        } else {
            // Otherwise, the column illustration dimension is not null, so
            // return the ID of the column illustration dimension.
            return column.getID();
        }
    }

    /**
     * <p>
     * Returns the illustration dimension for the column, the first dimension.
     * </p>
     *
     * @return <code>IllustrationDimension</code> object of the illustration
     * dimension for the column.
     */
    public IllustrationDimension getColumn() {
        if (dimensions.size() > 0) {
            // If there are more than one illustration dimension in the list of
            // illustration dimensions, then sort them
            // by order index.
            Collections.sort(dimensions);

            // Return the first member, index 0
            return dimensions.get(0);
        } else {
            // Otherwise return null, there is no column
            return null;
        }
    }

    /**
     * <p>
     * Sets the illustration dimension for the column to column.
     * </p>
     *
     * @param column
     * <code>IllustrationDimension</code> object of the illustration
     * dimension for the column.
     */
    public void setColumn(IllustrationDimension column) {
        this.column = column;
    }

    /**
     * <p>
     * Returns the number of columns in the illustration.
     * </p>
     *
     * <p>
     * The number of columns returned by this method is an approximation of the
     * actual number of columns in the final illustration calculated from the
     * number of selected indices in the column illustration dimension. If the
     * underlying figure dimension changed so that any of the indices were no
     * longer valid, then the number of columns returned by this method will be
     * different than the actual number of columns in the final illustration. In
     * any case, the number of columns returned by this method is an upper bound
     * on the actual number of columns in the final illustration as invalid
     * indices are ignored.
     * </p>
     *
     * @return int number of columns in the illustration.
     */
    public int getColumnCount() {
        if (column == null) {
            // If the column illustration dimension is null, then return 0.
            return 0;
        } else {
            // Otherwise, the column illustration dimension is not null, so
            // return the number of indices in the column illustration
            // dimension.
            return column.getIndexCount();
        }
    }

    /**
     * <p>
     * Adds the illustration dimension in the
     * <code>Illustration.IllustrationDimension</code> object dimension to the
     * list of illustration dimensions.
     * </p>
     *
     * @param dimension
     * <code>Illustration.IllustrationDimension</code> object to add
     * to the list of illustration dimensions.
     */
    public boolean add(IllustrationDimension dimension) {
        if (dimension == null) {
            // If the illustration dimension is null, then quit.
            return false;
        } else {
            // Otherwise, the illustration dimension is not null, so add it to
            // the list of illustration dimensions.
            return dimensions.add(dimension);
        }
    }

    /**
     * <p>
     * Clears the list of illustration dimensions in the illustration.
     * </p>
     */
    public void clear() {
        dimensions.clear();
    }

    /**
     * <p>
     * Returns the array of illustration dimensions in the illustration.
     * </p>
     *
     * <p>
     * The illustration dimensions correspond to the remaining illustration
     * dimensions in the illustration that are not ignored.
     * </p>
     *
     * @return <code>IllustrationDimension</code> array of illustration
     * dimensions in the ilustration.
     */
    public IllustrationDimension[] toArray() {
        if (dimensions.size() > 1) {
            // If there are more than one illustration dimension in the list of
            // illustration dimensions, then sort them
            // by order index.
            Collections.sort(dimensions);
        }

        // Allocate an array to hold all the illustration dimensions
        IllustrationDimension[] dimensionsArray = new IllustrationDimension[dimensions.size()];

        // Copy the list of illustration dimensions into the array of
        // illustration dimensions
        dimensions.toArray(dimensionsArray);

        // Return the array of illustrations dimensions
        return dimensionsArray;
    }

    /**
     * <p>
     * Returns a hash map from the ID of the illustration dimension to the
     * <code>Illustration.IllustrationDimension</code> object.
     * </p>
     *
     * @return <code>HashMap</code> of <code>Integer</code> to
     * <code>Illustration.IllustrationDimension</code> mapping the ID fo
     * the illustration dimension to the
     * <code>Illustration.IllustrationDimension</code> object.
     */
    public HashMap<Integer, IllustrationDimension> getIllustrationDimensionMap() {
        // A hash map of the ID of the figure dimension to the illustration
        // dimension
        HashMap<Integer, Illustration.IllustrationDimension> dimMap = new HashMap<Integer, Illustration.IllustrationDimension>();

        // Get the array of all the illustration dimensions
        IllustrationDimension[] dims = toArray();

        // Convert the array of illustration dimensions into the hash map
        for (int i = 0; i < dims.length; i++) {
            // Put the illustration dimension into the hash map using the ID of
            // the figure dimension as the key
            dimMap.put(Integer.valueOf(dims[i].getID()), dims[i]);
        }

        // Return the hash map
        return dimMap;
    }

    /**
     * <p>
     * Returns whether the figure dimension in the <code>FigureDimension</code>
     * object dim is fixed (included in the illustration, but set to one value).
     * </p>
     *
     * @param dim
     * <code>FigureDimension</code> object to test.
     * @return boolean flag indicating whether the figure dimension in the
     * <code>FigureDimension</code> object dim is inactive (ignored by
     * the illustration).
     */
//    public boolean isFixed(FigureDimension dim) {
//        if (dim == null) {
//            // If the figure dimension is null, then return true, the dimension
//            // is fixed.
//            return true;
//        }
//
//        /**
//         * Check if the figure dimension is one of the figure dimensions not
//         * active and not ignored by the illustration which means it will be
//         * included as just one value (that matching the keystone file)
//         */
//
//        // Get the array of all the illustration dimensions
//        IllustrationDimension[] dims = getActiveDimensions();
//
//        // Loop through the array of all the active illustration dimensions
//        for (int i = 0; i < dims.length; i++) {
//            if (dim.getID() == dims[i].getTargetFigureDimensionID()) {
//                // If the ID of the target figure dimension is the ID of the
//                // active figure dimension, then return false, the dimension is
//                // not ignored.
//                return false;
//            }
//        }
//
//        // At this point, the ID of the figure dimension is not one of the
//        // active dimensions.
//        // So, return true, the dimension is fixed.
//        return true;
//    }

    /**
     * <p>
     * Returns the number of tables in the illustration.
     * </p>
     *
     * <p>
     * The number of tables returned by this method is an approximation of the
     * actual number of tables in the final illustration calculated from the
     * number of selected indices in the illustration dimensions. If the
     * underlying figure dimensions changed so that any of the indices were no
     * longer valid, then the number of tables returned by this method will be
     * different than the actual number of tables in the final illustration. In
     * any case, the number of tables returned by this method is an upper bound
     * on the actual number of tables in the final illustration as invalid
     * indices are ignored.
     * </p>
     *
     * @return int number of tables in the illustration.
     */
    public int getTableCount() {
        return getCountHelper(true);
    }

    /**
     * <p>
     * Returns the number of pages in the illustration.
     * </p>
     *
     * <p>
     * The number of pages returned by this method is an approximation of the
     * actual number of pages in the final illustration calculated from the
     * number of selected indices in the illustration dimensions. If the
     * underlying figure dimensions changed so that any of the indices were no
     * longer valid, then the number of pages returned by this method will be
     * different than the actual number of pages in the final illustration. In
     * any case, the number of pages returned by this method is an upper bound
     * on the actual number of pages in the final illustration as invalid
     * indices are ignored.
     * </p>
     *
     * @return int number of pages in the illustration.
     */
    public int getPageCount() {
        return getCountHelper(false);
    }

    /**
     * <p>
     * Returns the number of tables in the illustration if tableP is true or the
     * number of pages in the illustration if tableP is false.
     * </p>
     *
     * <p>
     * The count returned by this method is an approximation of the actual count
     * in the final illustration calculated from the number of selected indices
     * in the illustration dimensions. If the underlying figure dimensions
     * changed so that any of the indices were no longer valid, then the count
     * returned by this method will be different than the actual count in the
     * final illustration. In any case, the count returned by this method is an
     * upper bound on the actual count in the final illustration as invalid
     * indices are ignored.
     * </p>
     *
     * @param tableP
     * boolean flag indicating whether to return the number of tables
     * in the illustration.
     * @return int number of tables in the illustration if tableP is true or the
     * number of pages in the illustration if tableP is false.
     */
    private int getCountHelper(boolean tableP) {
        // Get the array of illustration dimensions
        IllustrationDimension[] dims = toArray();

        // Initialize the count to 1
        int count = 1;

        // Loop through the array of illustration dimensions
        for (int i = 0; i < dims.length; i++) {
            if ((tableP && dims[i].isTable()) || (!tableP && !dims[i].isTable())) {
                // If the number of tables is being counted and the current
                // illustration dimension describes a table or
                // if the number of pages is being counted and the current
                // illustration dimension describes a page (does not describe a
                // table), then update the count.
                count *= dims[i].getIndexCount();
            }
        }

        // Return the count
        return count;
    }

    /**
     * <p>
     * Whether the illustration has any active illustration dimensions.
     * </p>
     *
     * @return boolean a flag indicating whether this illustration has any
     * active dimensions
     */
    public boolean hasActiveDimensions() {
        // Get the array of illustration dimensions
        IllustrationDimension[] dims = toArray();

        if (dims != null && dims.length > 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Figure dimension helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns a boolean flag indicating whether one of the active dimensions
     * includes channels.
     * </p>
     *
     * @param figureDimensions
     * <code>FigureDimension[]</code> an array of
     * FigureDimensions.</p>
     *
     * @return <code>boolean</code> whether channels are variable (active) in
     * one of the illustration dimensions.
     */
//    public boolean channelsAreVariable(FigureDimension[] figureDimensions) {
//
//        // Initialize a variable to return if we don't find any channels in the
//        // active dimensions
//        boolean channelsAreVariableP = false;
//
//        // get the active illustration dimensions
//        IllustrationDimension[] activeDimensions = toArray();
//
//        if (activeDimensions != null && activeDimensions.length > 0) {
//            // For each active dimension, get its target figure dimension
//            for (int i = 0; i < activeDimensions.length; i++) {
//                FigureDimension activeFigureDimension = activeDimensions[i].getTargetFigureDimension(figureDimensions);
//
//                // If we found a figure dimension
//                if (activeFigureDimension != null) {
//
//                    // Get an array of the selected membershipSets of this
//                    // figuredimension
//                    MembershipSet[] activeMembershipSets = getSelectedMembershipSets(activeFigureDimension, activeDimensions[i]);
//
//                    if (activeMembershipSets != null && activeMembershipSets.length > 0) {
//                        // For each of the membership sets, see whether it is a
//                        // panel channel
//                        for (int j = 0; j < activeMembershipSets.length; j++) {
//
//                            // If we found a panel channel, return true and get
//                            // out of here
//                            if (activeMembershipSets[j] != null && activeMembershipSets[j].getType() == "Panel Channel") {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // If we get here, return the default, false
//        return channelsAreVariableP;
//    }

    /**
     * <p>
     * Returns a boolean flag indicating whether one of the active dimensions
     * includes channels. This more expensive version of the function looks up
     * the membership sets for the lazy user.
     * </p>
     *
     * @return <code>boolean</code> whether channels are variable (active) in
     * one of the illustration dimensions.
     */
//    public boolean channelsAreVariable() {
//
//        // Get the experiment
//        Experiment currentExp = Experiment.getExperimentWithID(this.creatorExperimentID);
//
//        // Get the membership of the current experiment
//        Membership membership = currentExp.getMembership();
//
//        // Get the array of membership sets
//        MembershipSet[] membershipSetsArray = null;
//
//        if (membership != null) {
//            membershipSetsArray = membership.getMembershipSets();
//        }
//
//        // Get all the figure dimensions
//        FigureDimension[] figureDimensions = null;
//
//        if (membershipSetsArray != null && this != null) {
//            figureDimensions = this.getFigureDimensions(membershipSetsArray);
//        }
//
//        return channelsAreVariable(figureDimensions);
//    }

    /**
     * <p>
     * Returns the array of figure dimensions corresponding to the figure
     * dimensions in the list of passed membership sets.
     * </p>
     *
     * @param membershipSetsArray
     * <code>MembershipSet[]</code> an array of membership sets.</p>
     *
     * @return <code>FigureDimension[]</code> array of figure dimensions in the
     * membership set list.
     */
//    public FigureDimension[] getFigureDimensions(MembershipSet[] membershipSetsArray) {
//
//        // Count the number of figure dimensions
//        int totalFigureDimensions = 0;
//        if (membershipSetsArray != null) {
//            for (int k = 0; k < membershipSetsArray.length; k++) {
//                if (membershipSetsArray[k].getType().equals("Figure Dimension")) {
//                    totalFigureDimensions++;
//                }
//            }
//        }
//
//        // Create the list of figure dimensions
//        int dimensionCounter = 0;
//        FigureDimension[] figureDimensions = null;
//        if (membershipSetsArray != null) {
//            figureDimensions = new FigureDimension[totalFigureDimensions];
//            for (int k = 0; k < membershipSetsArray.length; k++) {
//                if (membershipSetsArray[k].getType().equals("Figure Dimension")) {
//                    figureDimensions[dimensionCounter] = (FigureDimension) membershipSetsArray[k];
//                    dimensionCounter++;
//                }
//            }
//        }
//
//        // Return the array of figure dimensions
//        return figureDimensions;
//    }

    /**
     * <p>
     * Returns the array of figure dimensions corresponding to the active
     * illustration dimensions in the illustration.
     * </p>
     *
     * @param figureDimensions
     * <code>FigureDimension[]</code> an array of figure
     * dimensions.</p>
     *
     * @return <code>FigureDimension[]</code> array of figure dimensions active
     * in the ilustration.
     */
//    public FigureDimension[] getActiveFigureDimensions(FigureDimension[] figureDimensions) {
//        if (dimensions.size() > 1) {
//            // If there are more than one illustration dimension in the list of
//            // illustration dimensions, then sort them
//            // by order index.
//            Collections.sort(dimensions);
//        }
//
//        // Allocate an array to hold all the illustration dimensions
//        IllustrationDimension[] dimensionsArray = new IllustrationDimension[dimensions.size()];
//
//        // Copy the list of illustration dimensions into the array of
//        // illustration dimensions
//        dimensions.toArray(dimensionsArray);
//
//        // Allocate an array to hold all the figure dimensions
//        FigureDimension[] activeFigureDimensions = null;
//
//        if (dimensionsArray != null && dimensionsArray.length > 0) {
//            // If there are active dimensions, get their corresponding figure
//            // dimensions into activeFigureDimensions
//
//            activeFigureDimensions = new FigureDimension[dimensionsArray.length];
//
//            for (int k = 0; k < dimensionsArray.length; k++) {
//                if (dimensionsArray[k] != null && dimensionsArray[k].getID() > -1) {
//                    // If there really is an illustration dimension in the
//                    // array, then get its figure dimension and add it to the
//                    // array
//                    activeFigureDimensions[k] = dimensionsArray[k].getTargetFigureDimension(figureDimensions);
//                }
//            }
//        }
//
//        // Return the array of figure dimensions
//        return activeFigureDimensions;
//    }

    /**
     * <p>
     * Returns the array of figure dimensions corresponding to the fixed
     * illustration dimensions in the illustration.
     * </p>
     *
     * @param figureDimensions
     * <code>FigureDimension[]</code> an array of figure
     * dimensions.</p>
     *
     * @return <code>FigureDimension[]</code> array of figure dimensions active
     * in the ilustration.
     */
//    public FigureDimension[] getFixedFigureDimensions(FigureDimension[] figureDimensions) {
//
//        // Make a list of the fixed dimensions
//        int fixedDimensionCount = 0;
//
//        for (int k = 0; k < figureDimensions.length; k++) {
//            if (this.isFixed(figureDimensions[k])) {
//                fixedDimensionCount++;
//            }
//        }
//
//        FigureDimension[] fixedFigureDimensions = new FigureDimension[fixedDimensionCount];
//        int dimensionCounter = 0;
//        for (int k = 0; k < figureDimensions.length; k++) {
//            if (this.isFixed(figureDimensions[k])) {
//                fixedFigureDimensions[dimensionCounter] = figureDimensions[k];
//                dimensionCounter++;
//            }
//        }
//
//        // Return the array of fixed figure dimensions
//        return fixedFigureDimensions;
//    }

    /**
     * <p>
     * Returns the figure dimension associated with a membership set, plucked
     * from an array of figure dimensions.
     * </p>
     *
     * @param membershipSet
     * <code>MembershipSet</code> the membership set whose figure
     * dimension we want to find.</p>
     * @param figureDimensions
     * <code>FigureDimension[]</code> an array of figure
     * dimensions.</p>
     *
     * @return <code>FigureDimension</code> the figure dimension associated with
     * membershipSet.</p>
     */
//    public FigureDimension getTargetFigureDimension(MembershipSet membershipSet, FigureDimension[] figureDimensions) {
//
//        // create a targetFigureDimension set to be returned
//        FigureDimension targetFigureDimension = null;
//
//        if (figureDimensions != null && figureDimensions.length > 0) {
//            // if we got a non-null figureDimensions array
//            for (int i = 0; i < figureDimensions.length; i++) {
//                // For each figure dimension in the array
//
//                if (figureDimensions[i] != null && figureDimensions[i].contains(membershipSet)) {
//                    // If the figure dimension contaisn the ID of the current
//                    // membership set,
//                    // then return it
//
//                    targetFigureDimension = (FigureDimension) figureDimensions[i];
//                    break;
//
//                }
//            }
//        }
//
//        return targetFigureDimension;
//    }

    /**
     * <p>
     * Returns the array of selected membership sets in the figure dimension in
     * the <code>FigureDimension</code> object figureDim based on the
     * illustration dimension in the
     * <code>Illustration.IllustrationDimension</code> object illDim.
     * </p>
     *
     * @param figureDim
     * <code>FigureDimension</code> object to the figure dimension
     * whose selected membership sets to return.
     * @param illDim
     * <code>Illustration.IllustrationDimension</code> object to the
     * illustration dimension containing the selected indices of the
     * figure dimension.
     * @return <code>MembershipSet</code> array of selected membership sets in
     * the figure dimension.
     */
//    public static MembershipSet[] getSelectedMembershipSets(FigureDimension figureDim, Illustration.IllustrationDimension illDim) {
//        if ((figureDim == null) || (illDim == null) || (figureDim.getID() != illDim.getTargetFigureDimensionID())) {
//            // If the figure dimension or the illustration dimension is null or
//            // the ID of the figure dimension is not equal to the ID of the
//            // illustration dimension, then quit.
//            return new MembershipSet[0];
//        }
//
//        // Get the array of membership sets in the figure dimension
//        MembershipSet[] sets = figureDim.toArray();
//
//        // Get the array of selected indices in the illustration dimension
//        int[] indices = illDim.getIndices();
//
//        // Create a list to hold the selected membership sets
//        ArrayList<MembershipSet> setList = new ArrayList<MembershipSet>(indices.length);
//
//        if (indices != null) {
//            // If the array of selected indices in the illustration dimension is
//            // not null, then get the selected membership sets.
//            int index;
//
//            // Loop through the array of selected indices in the illustration
//            // dimension
//            for (int i = 0; i < indices.length; i++) {
//                // Get the current index
//                index = indices[i];
//
//                if ((index >= 0) && (index < sets.length)) {
//                    // If the current index is a valid index in the array of
//                    // membership sets in the figure dimension, then add the
//                    // membership set to the list of selected membership sets.
//                    setList.add(sets[index]);
//                }
//            }
//        }
//
//        // Allocate the array of selected membership sets
//        MembershipSet[] selectedSets = new MembershipSet[setList.size()];
//
//        // Copy the list of selected membership sets into the array of selected
//        // membership sets
//        setList.toArray(selectedSets);
//
//        // Return the array of selected membership sets
//        return selectedSets;
//    }

    /**
     * <p>
     * Returns the parameters of a passed figure dimension encoded as a URL
     * querystring for an illustration dimension (serializes the FigureDimension
     * as either the existing or as a new illustration dimension querystring).
     * </p>
     *
     * @return <code>String</code> parameters of the figure dimension encoded as
     * an illustration dimension URL querystring.
     */
//    public String getDimensionQueryString(FigureDimension figureDimension) {
//
//        String queryString = "";
//
//        // Create an IllustrationDimension and populate it
//        // int id, int targetFigureDimensionID, int order, int orientation,
//        // int[] indices
//
//        if (figureDimension != null) {
//            IllustrationDimension[] currentDimensions = getActiveDimensions();
//
//            // Look for the figure dimension's ID in the active dimensions
//            for (int i = 0; i < currentDimensions.length; i++) {
//                // If the figureDimension is active, return the querystring for
//                // that illustration dimension
//                if (currentDimensions[i].getTargetFigureDimensionID() == figureDimension.getID()) {
//                    return currentDimensions[i].getQueryString();
//                }
//            }
//
//            // If we didn't find it in the active dimensions, then create a new
//            // one
//
//            int nextID = getNextDimensionID(currentDimensions);
//            int[] allIndices = getAllIndices(figureDimension);
//            IllustrationDimension currentDimension = new IllustrationDimension(nextID, figureDimension.getID(), getNextOrderValue(),
//                    IllustrationDimension.DEFAULT_ORIENTATION, allIndices);
//            queryString += currentDimension.getQueryString();
//        }
//
//        return queryString;
//    }

    /**
     * <p>
     * Returns the all the indices of a figure dimension.
     * </p>
     *
     * @return <code>int[]</code> an array of all the indices of a figure
     * dimension.
     */
//    public int[] getAllIndices(FigureDimension figureDimension) {
//
//        int[] allIndices = null;
//
//        // Set an index counter, interate through the figure dimension members,
//        // and add the index counter to the array
//        int indexCounter = 0;
//        if (figureDimension != null) {
//            // Get the membership sets associated with this figure dimension
//            // using the toArray method,
//            // which is part of the BagSet superclass from which FigureDimension
//            // is subclassed
//            MembershipSet[] membershipSets = figureDimension.toArray();
//
//            if (membershipSets != null && membershipSets.length > 0) {
//                allIndices = new int[membershipSets.length];
//
//                // Set to the current index
//                for (int j = 0; j < membershipSets.length; j++) {
//                    allIndices[j] = j;
//                }
//            }
//
//        }
//
//        return allIndices;
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Color Set helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the parameters of the color gradient as indicated by the
     * illustration in the <code>Illustration</code> object illustration for the
     * population grid in the <code>PopulationGrid</code> object grid.
     * </p>
     *
     * @param illustration
     * <code>Illustration</code> object to the illustration.
     * @param grid
     * <code>PopulationGrid</code> object to the population grid.
     * @return <code>String</code> parameters of the color gradient for the
     * population grid in the <code>PopulationGrid</code> object grid.
     * @throws IOException
     */
//    public static String getColorGradientParameters(Illustration illustration, PopulationGrid grid) throws IOException {
//        // Create a new StringBuffer with which we will build the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        parameters.append("&colorSet=");
//        parameters.append(illustration.getColorSet());
//
//        /**
//         * Encode the parameters of the color gradient
//         */
//
//        // Get the control of the population grid
//        int control = grid.getControl();
//
//        // Get the minimum value of the population grid
//        double minimum = grid.getMinimum();
//
//        // Get the value of the inflection point of the population grid
//        double inflection = grid.getInflection();
//
//        // Get the maximum value of the population grid
//        double maximum = grid.getMaximum();
//
//        // Get the range of the population grid
//        double range = grid.getRange();
//
//        // Get the dynamic range of the illustration
//        double dynamicRange = illustration.getRange();
//
//        if (grid.isUnidirectional()) {
//            // If the range of the population grid is unidirectional, then the
//            // color gradient is unidirectional.
//            if (grid.isMaximum()) {
//                // If the control of the population grid is one of the maximums,
//                // then the color gradient is the maximum unidirectional color
//                // gradient.
//                parameters.append("&isMaximum=true");
//
//                if (Double.isNaN(dynamicRange)) {
//                    // If the dynamic range of the illustration is not a number,
//                    // then use the minimum and the maximum values of the
//                    // population grid.
//                    parameters.append("&min=");
//                    parameters.append(minimum);
//                    parameters.append("&max=");
//                    parameters.append(maximum);
//                } else {
//                    // Otherwise, the dynamic range of the illustration is a
//                    // number, so use it.
//                    parameters.append("&min=");
//                    parameters.append(maximum - dynamicRange);
//                    parameters.append("&max=");
//                    parameters.append(maximum);
//                }
//            } else {
//                // Otherwise, the color gradient is the minimum unidirectional
//                // color gradient.
//                if (Double.isNaN(dynamicRange)) {
//                    // If the dynamic range of the illustration is not a number,
//                    // then use the minimum and the maximum values of the
//                    // population grid.
//                    parameters.append("&min=");
//                    parameters.append(minimum);
//                    parameters.append("&max=");
//                    parameters.append(maximum);
//                } else {
//                    // Otherwise, the dynamic range of the illustration is a
//                    // number, so use it.
//                    parameters.append("&min=");
//                    parameters.append(minimum);
//                    parameters.append("&max=");
//                    parameters.append(minimum + dynamicRange);
//                }
//            }
//        } else {
//            // Otherwise, the range of the population grid is bidirectional, so
//            // the color gradient is bidirectional.
//            if (Double.isNaN(dynamicRange)) {
//                // If the dynamic range of the illustration is not a number,
//                // then use the minimum and the maximum values of the population
//                // grid.
//                parameters.append("&min=");
//                parameters.append(inflection - range);
//                parameters.append("&inflection=");
//                parameters.append(inflection);
//                parameters.append("&max=");
//                parameters.append(inflection + range);
//            } else {
//                // Otherwise, the dynamic range of the illustration is a number,
//                // so use it.
//                parameters.append("&min=");
//                parameters.append(inflection - dynamicRange);
//                parameters.append("&inflection=");
//                parameters.append(inflection);
//                parameters.append("&max=");
//                parameters.append(inflection + dynamicRange);
//            }
//        }
//
//        // Return the String representation of the StringBuffer
//        return parameters.toString();
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Region helpers
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Creates a new region with ID id and adds it to the illustration. The
     * resulting region is returned or null is returned if the ID is invalid.
     * </p>
     *
     * @param id
     * int ID of the region to add.
     * @return <code>Illustration.Region</code> object to the region with ID id
     * or null if the ID is invalid.
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
     * Removes the region with ID id from the illustration.
     * </p>
     *
     * @param id
     * int ID of the region to remove.
     * @return true if the illustration changed as a result of the call.
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
     * @return <code>Illustration.Region</code> object to the region with ID id
     * or null if the ID is invalid or the region does not exist.
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
     * Return the array of regions in the illustration.
     * </p>
     *
     * @return <code>Illustration.Region</code> array of regions in the
     * illustration.
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
     * Clears the regions in the illustration.
     * </p>
     */
    public void clearRegions() {
        regions.clear();
    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Legacy support for 'getRepresentation' factory methods
     * ***********************************************************************
     * ***********************************************************************
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
    public Representation getRepresentation(int id, String name, int channelCount, String filename, int compensationID, int[] gateSetIDs, int xChannel,
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
        rep.setPlotType(getPlotType());

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
    public Representation getRepresentation(int id, String name, int channelCount, String filename, int compensationID, int[] gateSetIDs, int xChannel,
            int yChannel, int plotType, int statType, int colorSet, boolean blackBackgroundP, boolean annotationP, boolean scaleLabelP, boolean scaleTickP,
            boolean axisLabelP, boolean longLabelP, int axisBins, double smoothing, double aspectRatio, double contourPercent, double contourStartPercent,
            int populationType, int eventCount, int dotSize) {

        // Create the Representation object
        Representation rep = Representation.getRepresentation(id, name, channelCount, filename, compensationID, gateSetIDs, xChannel, yChannel, getPlotType(),
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
     * ***********************************************************************
     * ***********************************************************************
     * Getting plot parameters
     *
     * Many different ways of asking for the plot parameters, some of which
     * override the parameters encoded in the illustration.
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the plot settings parameters of a placeholder encoded as a
     * querystring. This is a specialized version of the plot parameters that
     * tells the plotter to just print out a placeholder of some sort.
     * </p>
     *
     * <p>
     * The encoded parameter does not start with a '?' or a '&' so it is
     * suitable to be concatenated into an URL as the only parameters or as
     * additional parameters. It was designed this way so the client would have
     * the option to use the parameters in either capacity.
     * </p>
     *
     * @return <code>String</code> plot setting parameters of a placeholder
     * illustration encoded as a URL querystring.
     */
    public String getPlaceholderQueryString() {
        // Create a StringBuffer with which to encode the parameters
        StringBuffer parameters = new StringBuffer();

        // Add a mode tag that tells the plotServlet what to do
        parameters.append("mode=" + USE_PLACEHOLDERS_PARAM_NAME);

        return parameters.toString();
    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * illustration.
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
     * parameters of the illustration.
     */
//    public String getParameters() {
//        return getParameters(xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * illustration using the channel in xChannel as the x channel and the
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
     * parameters of the illustration.
     */
//    public String getParameters(int xChannel, int yChannel) {
//        return getParameters(xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * illustration using the channel in xChannel as the x channel and the
     * channel in yChannel as the y channel. Also the z channel as the zChannel
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
     * @param zChannel
     * int z channel.
     * @return <code>String</code> string encoding the plot parameters,
     * population type parameters, the scale parameters, and the region
     * parameters of the illustration.
     */
//    public String getParameters(int xChannel, int yChannel, int zChannel) {
//        return getParameters(xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the plot parameters, population type
     * parameters, the scale parameters, and the region parameters of the
     * illustration using the channel in xChannel as the x channel and the
     * channel in yChannel as the y channel. Also the z channel as the zChannel.
     * The type of the population indicated by the constant flag populationType
     * and the number of events to get from the flow file eventCount are used as
     * the population type parameters.
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
     * @param zChannel
     * int z channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the plot parameters,
     * population type parameters, the scale parameters, and the region
     * parameters of the illustration.
     */
//    public String getParameters(int xChannel, int yChannel, int zChannel, int populationType, int eventCount) {
//        if (populationType <= 0) {
//            // If the type of the population is invalid, then use the type of
//            // the population of the illustration.
//            populationType = this.populationType;
//            eventCount = this.eventCount;
//        }
//
//        if ((xChannel < -1) || (xChannel >= channelCount)) {
//            // If the x channel is invalid, then use the x channel of the
//            // illustration.
//            xChannel = this.xChannel;
//        }
//
//        if ((yChannel < -1) || (yChannel >= channelCount)) {
//            // If the y channel is invalid, then use the y channel of the
//            // illustration.
//            yChannel = this.yChannel;
//        }
//
//        if ((zChannel < -1) || (zChannel >= channelCount)) {
//            // If the z channel is invalid, then use the z channel of the
//            // illustration.
//            yChannel = this.yChannel;
//        }
//
//        return getPlotSettingsQueryString(xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the illustration.
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
     * Since the illustration does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @return <code>String</code> string encoding the illustration.
     */
//    public String encode() {
//        return encode(keystoneFilename, compensationID, getGateSetIDs(), xChannel, yChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by the ID
     * in compensationID, the array of gate set IDs replaced by the IDs in
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
     * Since the illustration does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param keystoneFilename
     * <code>String</code> filename of the keystone flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @return <code>String</code> string encoding the illustration with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
//    public String encode(String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel) {
//        return encode(keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by the ID
     * in compensationID, the array of gate set IDs replaced by the IDs in
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
     * Since the illustration does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param keystoneFilename
     * <code>String</code> filename of the keystone flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param zChannel
     * int z channel.
     * @return <code>String</code> string encoding the illustration with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
//    public String encode(String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int zChannel) {
//        return encode(keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by the ID
     * in compensationID, the array of gate set IDs replaced by the IDs in
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
     * Since the illustration does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param keystoneFilename
     * <code>String</code> filename of the keystone flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param zChannel
     * int z channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the illustration with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
//    public String encode(String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int zChannel, int populationType,
//            int eventCount) {
//        // Create a StringBuffer with which to encode the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // Encode the filename
//        parameters.append("filename=");
//        parameters.append(JSPlib.encode(keystoneFilename));
//
//        // Encode the ID of the compensation
//        parameters.append("&compensationID=");
//        parameters.append(compensationID);
//
//        if (gateSetIDs != null) {
//            // If the array of gate set IDs is not null, then encode them.
//
//            // Loop through the array of gate set IDs
//            for (int i = 0; i < gateSetIDs.length; i++) {
//                parameters.append("&gateSetID=");
//                parameters.append(gateSetIDs[i]);
//            }
//        }
//
//        // Encode the illustration parameters
//        parameters.append("&");
//        parameters.append(getParameters(xChannel, yChannel, zChannel, populationType, eventCount));
//
//        // Return the String illustration of the StringBuffer
//        return parameters.toString();
//    }

    /**
     * <p>
     * Returns a string encoding the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by the ID
     * in compensationID, the array of gate set IDs replaced by the IDs in
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
     * Since the illustration does not maintain the ID of the experiment, it
     * does not encode the ID of the experiment.
     * </p>
     *
     * @param keystoneFilename
     * <code>String</code> filename of the keystone flow file.
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
     * @return <code>String</code> string encoding the illustration with the
     * filename replaced by the filename in filename, the ID of the
     * compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
     */
//    public String encode(String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int populationType, int eventCount) {
//        return encode(keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * illustration with the filename replaced by the filename in filename, the
     * ID of the compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
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
     * @param keystoneFilename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs.
     */
//    public String encode(int experimentID, String keystoneFilename, int compensationID, int[] gateSetIDs) {
//        return encode(experimentID, keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * illustration with the filename replaced by the filename in filename, the
     * ID of the compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
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
     * @param keystoneFilename
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
     * experimentID and the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs.
     */
//    public String encode(int experimentID, String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel) {
//        return encode(experimentID, keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * illustration with the filename replaced by the filename in filename, the
     * ID of the compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs.
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
     * @param keystoneFilename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param zChannel
     * int z channel.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs.
     */
//    public String encode(int experimentID, String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int zChannel) {
//        return encode(experimentID, keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * illustration with the filename replaced by the filename in filename, the
     * ID of the compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs. The type of the
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
     * @param keystoneFilename
     * <code>String</code> filename of the flow file.
     * @param compensationID
     * int ID of the compensation.
     * @param gateSetIDs
     * int array of gate set IDs.
     * @param xChannel
     * int x channel.
     * @param yChannel
     * int y channel.
     * @param zChannel
     * int z channel.
     * @param populationType
     * int constant flag of the type of the population.
     * @param eventCount
     * int number of events to get from the flow file.
     * @return <code>String</code> string encoding the ID of the experiment
     * experimentID and the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs. The type of the population indicated by
     * the constant flag populationType and the number of events to get
     * from the flow file eventCount are used as the population type
     * parameters.
     */
//    public String encode(int experimentID, String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int zChannel,
//            int populationType, int eventCount) {
//        // Create a StringBuffer with which to encode the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // Encode the ID of the experiment
//        parameters.append("experimentID=");
//        parameters.append(experimentID);
//
//        // Encode the illustration
//        parameters.append("&");
//        parameters.append(encode(keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, zChannel, populationType, eventCount));
//
//        // Return the String illustration of the StringBuffer
//        return parameters.toString();
//    }

    /**
     * <p>
     * Returns a string encoding the ID of the experiment experimentID and the
     * illustration with the filename replaced by the filename in filename, the
     * ID of the compensation replaced by the ID in compensationID, the array of
     * gate set IDs replaced by the IDs in gateSetIDs. The type of the
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
     * @param keystoneFilename
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
     * experimentID and the illustration with the filename replaced by
     * the filename in filename, the ID of the compensation replaced by
     * the ID in compensationID, the array of gate set IDs replaced by
     * the IDs in gateSetIDs. The type of the population indicated by
     * the constant flag populationType and the number of events to get
     * from the flow file eventCount are used as the population type
     * parameters.
     */
//    public String encode(int experimentID, String keystoneFilename, int compensationID, int[] gateSetIDs, int xChannel, int yChannel, int populationType,
//            int eventCount) {
//        return encode(experimentID, keystoneFilename, compensationID, gateSetIDs, xChannel, yChannel, zChannel, populationType, eventCount);
//    }

    /**
     * <p>
     * Returns the plot settings parameters of the illustration encoded as a URL
     * querystring (serializes the Illustration plot parameters as a
     * querystring).
     * </p>
     *
     * <p>
     * This function is a bridge between Illustration and the plotting
     * functions, such as <code>plotServlet</code>, <code>Plot2D</code>, and
     * <code>CanvasSettings</code>. This function should be updated to format
     * the plot settings query string based on the conventions requred by those
     * objects.
     * </p>
     *
     * <p>
     * This function does not encode all of an illustration, just those settings
     * needed in the graphical portion of a plot. For example, the title of the
     * illustration and the citation are not part of the graphics, but the scale
     * labels and canvas background are.
     * </p>
     *
     * @return <code>String</code> plot setting parameters of the illustration
     * encoded as a URL querystring.
     */
//    public String getPlotSettingsQueryString() {
//        return getPlotSettingsQueryString(this.xChannel, this.yChannel, this.zChannel, this.populationType, this.eventCount);
//    }

    /**
     * <p>
     * Returns the plot settings parameters of the illustration encoded as a URL
     * querystring (serializes the Illustration plot parameters as a
     * querystring).
     * </p>
     *
     * <p>
     * This function is a bridge between Illustration and the plotting
     * functions, such as <code>plotServlet</code>, <code>Plot2D</code>, and
     * <code>CanvasSettings</code>. This function should be updated to format
     * the plot settings query string based on the conventions requred by those
     * objects.
     * </p>
     *
     * <p>
     * This function does not encode all of an illustration, just those settings
     * needed in the graphical portion of a plot. For example, the title of the
     * illustration and the citation are not part of the graphics, but the scale
     * labels and canvas background are.
     * </p>
     *
     * @return <code>String</code> plot setting parameters of the illustration
     * encoded as a URL querystring.
     */
//    public String getPlotSettingsQueryString(int xChannel, int yChannel, int zChannel, int populationType, int eventCount) {
//        // Create a StringBuffer with which to encode the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // The first one added does not begin with an ampersand
//        // Add the plot type
//        parameters.append(PLOT_TYPE_PARAM_NAME + "=" + JSPlib.encode(getPlotType() + ""));
//
//        // Add the plot size
//        parameters.append("&" + PLOT_SIZE_PARAM_NAME + "=" + JSPlib.encode(this.plotSize + ""));
//
//        // Add the xChannel
//        parameters.append("&" + X_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(xChannel + ""));
//
//        // Add the yChannel
//        parameters.append("&" + Y_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(yChannel + ""));
//
//        // Add the zChannel
//        parameters.append("&" + Z_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(zChannel + ""));
//
//        // Add the zChannel
//        parameters.append("&" + VARIABLE_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(variableChannel + ""));
//
//        // Add the event count
//        parameters.append("&" + EVENT_COUNT_PARAM_NAME + "=" + JSPlib.encode(eventCount + ""));
//
//        // Add the population type
//        parameters.append("&" + POPULATION_TYPE_PARAM_NAME + "=" + JSPlib.encode(populationType + ""));
//
//        // Add the black background flag
//        parameters.append("&" + BLACK_BACKGROUND_PARAM_NAME + "=" + JSPlib.encode(this.blackBackgroundP + ""));
//
//        // Add the black plot background flag
//        parameters.append("&" + BLACK_PLOT_BACKGROUND_PARAM_NAME + "=" + JSPlib.encode(this.blackPlotBackgroundP + ""));
//
//        // Add the use place holders flag
//        parameters.append("&" + USE_PLACEHOLDERS_PARAM_NAME + "=" + JSPlib.encode(this.usePlaceholdersP + ""));
//
//        // Add the compensation id
//        parameters.append("&" + COMPENSATION_ID_PARAM_NAME + "=" + JSPlib.encode(this.compensationID + ""));
//
//        // Add the event count
//        parameters.append("&" + EVENT_COUNT_PARAM_NAME + "=" + JSPlib.encode(this.eventCount + ""));
//
//        // Add the channel count
//        parameters.append("&" + CHANNEL_COUNT_PARAM_NAME + "=" + JSPlib.encode(this.channelCount + ""));
//
//        // If there are channels and the scales match them,
//        // Add the channel and scale settings
//        if (channelsAndScalesAreValid()) {
//            for (int i = 0; i < channelCount; i++) {
//                // Add the scale flags
//                parameters.append("&" + SCALE_FLAG_PARAM_NAME + i + "=" + JSPlib.encode(this.scaleFlags[i] + ""));
//
//                // Add the scale arguments
//                parameters.append("&" + SCALE_ARGUMENT_PARAM_NAME + i + "=" + JSPlib.encode(this.scaleArguments[i] + ""));
//
//                // Add the scale minimums
//                parameters.append("&" + SCALE_MINIMUM_PARAM_NAME + i + "=" + JSPlib.encode(this.minimums[i] + ""));
//
//                // Add the scale maximums
//                parameters.append("&" + SCALE_MAXIMUM_PARAM_NAME + i + "=" + JSPlib.encode(this.maximums[i] + ""));
//            }
//        }
//
//        // Add the gate set IDs
//        int[] gateSetIDArray = getGateSetIDs();
//        for (int i = 0; i < gateSetIDArray.length; i++) {
//            // Add the gate set ids
//            parameters.append("&" + GATE_SET_IDS_PARAM_NAME + "=" + JSPlib.encode(gateSetIDArray[i] + ""));
//        }
//
//        // Add the show labels flag
//        parameters.append("&" + SHOW_SCALE_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.showScaleLabelsP + ""));
//
//        // Add the stat type
//        parameters.append("&" + STAT_TYPE_PARAM_NAME + "=" + JSPlib.encode(this.statType + ""));
//
//        // Add the color set
//        parameters.append("&" + COLOR_SET_PARAM_NAME + "=" + JSPlib.encode(this.colorSet + ""));
//
//        // Add the annotation
//        parameters.append("&" + ANNOTATION_PARAM_NAME + "=" + JSPlib.encode(this.annotationP + ""));
//
//        // Add the show scale ticks flag
//        parameters.append("&" + SHOW_SCALE_TICKS_PARAM_NAME + "=" + JSPlib.encode(this.showScaleTicksP + ""));
//
//        // Add the show axis labels flag
//        parameters.append("&" + SHOW_AXIS_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.showAxisLabelsP + ""));
//
//        // Add the long label flag
//        parameters.append("&" + LONG_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.longLabelsP + ""));
//
//        // Add the smoothing
//        parameters.append("&" + SMOOTHING_PARAM_NAME + "=" + JSPlib.encode(this.smoothing + ""));
//
//        // Add the aspect ratio
//        parameters.append("&" + ASPECT_RATIO_PARAM_NAME + "=" + JSPlib.encode(this.aspectRatio + ""));
//
//        // Add the contour percent
//        parameters.append("&" + CONTOUR_PERCENT_PARAM_NAME + "=" + JSPlib.encode(this.contourPercent + ""));
//
//        // Add the contour start percent
//        parameters.append("&" + CONTOUR_START_PERCENT_PARAM_NAME + "=" + JSPlib.encode(this.contourStartPercent + ""));
//
//        /**
//         * Encode the region parameters
//         */
//
//        if (annotationP) {
//            // If the plot should be drawn with annotations, then encode the
//            // region parameters.
//
//            // Get the array of regions
//            Region[] regionArray = getRegions();
//
//            // Loop through the array of regions
//            for (int i = 0; i < regionArray.length; i++) {
//                // Encode the current region and append it to the parameters
//                parameters.append("&" + regionArray[i].getPlotSettingsQueryString());
//            }
//        }
//
//        // Add the layout override
//        parameters.append("&" + LAYOUT_OVERRIDE_PARAM_NAME + "=" + JSPlib.encode(this.layoutOverride + ""));
//
//        // Add the statistic
//        parameters.append("&" + STATISTIC_PARAM_NAME + "=" + JSPlib.encode(this.statistic + ""));
//
//        // Add the equation
//        parameters.append("&" + EQUATION_PARAM_NAME + "=" + JSPlib.encode(this.equation + ""));
//
//        // Add the formula
//        parameters.append("&" + FORMULA_PARAM_NAME + "=" + JSPlib.encode(this.formula + ""));
//
//        // Add the control
//        parameters.append("&" + CONTROL_PARAM_NAME + "=" + JSPlib.encode(this.control + ""));
//
//        // Add the controlRow
//        parameters.append("&" + CONTROL_ROW_PARAM_NAME + "=" + JSPlib.encode(this.controlRow + ""));
//
//        // Add the controlColumn
//        parameters.append("&" + CONTROL_COLUMN_PARAM_NAME + "=" + JSPlib.encode(this.controlColumn + ""));
//
//        // Add the range
//        parameters.append("&" + RANGE_PARAM_NAME + "=" + JSPlib.encode(this.range + ""));
//
//        return parameters.toString();
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Querystring Serialization / Deserialization
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the parameters of the illustration encoded as a URL querystring
     * (serializes the Illustration as a querystring).
     * </p>
     *
     * @return <code>String</code> parameters of an illustration encoded using
     * URL querystring conventions.
     */
//    public String getQueryString() {
//        // Create a StringBuffer with which to encode the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // The first one added does not begin with an ampersand
//        // Add the id
//        parameters.append(ID_PARAM_NAME + "=" + JSPlib.encode(this.id + ""));
//
//        // Add the keystone filename
//        parameters.append("&" + KEYSTONE_FILENAME_PARAM_NAME + "=" + JSPlib.encode(this.keystoneFilename));
//
//        // Add the creator experiment ID
//        parameters.append("&" + CREATOR_EXPERIMENT_ID_PARAM_NAME + "=" + JSPlib.encode(this.creatorExperimentID + ""));
//
//        // Add the plot size
//        parameters.append("&" + PLOT_SIZE_PARAM_NAME + "=" + JSPlib.encode(this.plotSize + ""));
//
//        // Add the creator ID
//        parameters.append("&" + CREATOR_ID_PARAM_NAME + "=" + JSPlib.encode(this.creatorID + ""));
//
//        // Add the name
//        parameters.append("&" + NAME_PARAM_NAME + "=" + JSPlib.encode(this.name));
//
//        // Add the plot type
//        parameters.append("&" + PLOT_TYPE_PARAM_NAME + "=" + JSPlib.encode(this.getPlotType() + ""));
//
//        // Add the xChannel
//        parameters.append("&" + X_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(this.xChannel + ""));
//
//        // Add the yChannel
//        parameters.append("&" + Y_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(this.yChannel + ""));
//
//        // Add the zChannel
//        parameters.append("&" + Z_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(this.zChannel + ""));
//
//        // Add the variableChannel
//        parameters.append("&" + VARIABLE_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(this.variableChannel + ""));
//
//        // Add the black background flag
//        parameters.append("&" + BLACK_BACKGROUND_PARAM_NAME + "=" + JSPlib.encode(this.blackBackgroundP + ""));
//
//        // Add the black plot background flag
//        parameters.append("&" + BLACK_PLOT_BACKGROUND_PARAM_NAME + "=" + JSPlib.encode(this.blackPlotBackgroundP + ""));
//
//        // Add the use place holders flag
//        parameters.append("&" + USE_PLACEHOLDERS_PARAM_NAME + "=" + JSPlib.encode(this.usePlaceholdersP + ""));
//
//        // Add the compensation id
//        parameters.append("&" + COMPENSATION_ID_PARAM_NAME + "=" + JSPlib.encode(this.compensationID + ""));
//
//        // Add the population cutoff
//        parameters.append("&" + POPULATION_CUTOFF_PARAM_NAME + "=" + JSPlib.encode(this.populationCutoff + ""));
//
//        // Add the event count
//        parameters.append("&" + EVENT_COUNT_PARAM_NAME + "=" + JSPlib.encode(this.eventCount + ""));
//
//        // Add the population type
//        parameters.append("&" + POPULATION_TYPE_PARAM_NAME + "=" + JSPlib.encode(this.populationType + ""));
//
//        // Add the channel count
//        parameters.append("&" + CHANNEL_COUNT_PARAM_NAME + "=" + JSPlib.encode(this.channelCount + ""));
//
//        // If there are channels and the scales match them,
//        // Add the channel and scale settings
//        if (channelsAndScalesAreValid()) {
//            for (int i = 0; i < channelCount; i++) {
//                // Add the scale flags
//                parameters.append("&" + SCALE_FLAG_PARAM_NAME + i + "=" + JSPlib.encode(this.scaleFlags[i] + ""));
//
//                // Add the scale arguments
//                parameters.append("&" + SCALE_ARGUMENT_PARAM_NAME + i + "=" + JSPlib.encode(this.scaleArguments[i] + ""));
//
//                // Add the scale minimums
//                parameters.append("&" + SCALE_MINIMUM_PARAM_NAME + i + "=" + JSPlib.encode(this.minimums[i] + ""));
//
//                // Add the scale maximums
//                parameters.append("&" + SCALE_MAXIMUM_PARAM_NAME + i + "=" + JSPlib.encode(this.maximums[i] + ""));
//            }
//        }
//
//        // Add the gate set IDs
//        int[] gateSetIDArray = getGateSetIDs();
//        for (int i = 0; i < gateSetIDArray.length; i++) {
//            // Add the gate set ids
//            parameters.append("&" + GATE_SET_IDS_PARAM_NAME + JSPlib.encode(gateSetIDArray[i] + "") + "=" + JSPlib.encode(gateSetIDArray[i] + ""));
//        }
//
//        // Add the citation format
//        parameters.append("&" + CITATION_FORMAT_PARAM_NAME + "=" + JSPlib.encode(this.citationFormat + ""));
//
//        // Add the view Status
//        parameters.append("&" + VIEW_STATUS_PARAM_NAME + "=" + JSPlib.encode(this.viewStatus + ""));
//
//        // Add the edit Status
//        parameters.append("&" + EDIT_STATUS_PARAM_NAME + "=" + JSPlib.encode(this.editStatus + ""));
//
//        // Add the menu style
//        parameters.append("&" + MENU_STYLE_PARAM_NAME + "=" + JSPlib.encode(this.menuStyle + ""));
//
//        // Add the show labels flag
//        parameters.append("&" + SHOW_SCALE_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.showScaleLabelsP + ""));
//
//        // Add the state type
//        parameters.append("&" + STAT_TYPE_PARAM_NAME + "=" + JSPlib.encode(this.statType + ""));
//
//        // Add the color set
//        parameters.append("&" + COLOR_SET_PARAM_NAME + "=" + JSPlib.encode(this.colorSet + ""));
//
//        // Add the annotation flag
//        parameters.append("&" + ANNOTATION_PARAM_NAME + "=" + JSPlib.encode(this.annotationP + ""));
//
//        // Add the scale ticks flag
//        parameters.append("&" + SHOW_SCALE_TICKS_PARAM_NAME + "=" + JSPlib.encode(this.showScaleTicksP + ""));
//
//        // Add the show axis labels flag
//        parameters.append("&" + SHOW_AXIS_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.showAxisLabelsP + ""));
//
//        // Add the long labels flag
//        parameters.append("&" + LONG_LABELS_PARAM_NAME + "=" + JSPlib.encode(this.longLabelsP + ""));
//
//        // Add the smoothing
//        parameters.append("&" + SMOOTHING_PARAM_NAME + "=" + JSPlib.encode(this.smoothing + ""));
//
//        // Add the aspect ratio
//        parameters.append("&" + ASPECT_RATIO_PARAM_NAME + "=" + JSPlib.encode(this.aspectRatio + ""));
//
//        // Add the contour percent
//        parameters.append("&" + CONTOUR_PERCENT_PARAM_NAME + "=" + JSPlib.encode(this.contourPercent + ""));
//
//        // Add the contour start percent
//        parameters.append("&" + CONTOUR_START_PERCENT_PARAM_NAME + "=" + JSPlib.encode(this.contourStartPercent + ""));
//
//        // Add the use in print view flag
//        parameters.append("&" + USE_PRINT_VIEW_PARAM_NAME + "=" + JSPlib.encode(this.usePrintViewP + ""));
//
//        // Add the panel default flag
//        parameters.append("&" + PANEL_DEFAULT_PARAM_NAME + "=" + JSPlib.encode(this.panelDefaultP + ""));
//
//        // Add the show details flag
//        parameters.append("&" + SHOW_DETAILS_PARAM_NAME + "=" + JSPlib.encode(this.showDetailsP + ""));
//
//        // Add the show title flag
//        parameters.append("&" + SHOW_TITLE_PARAM_NAME + "=" + JSPlib.encode(this.showTitleP + ""));
//
//        // Add the dimensions
//        // Get the array of illustration dimensions
//        // This is only getting one
//        IllustrationDimension[] dimensionsArray = getActiveDimensions();
//        // Loop through the array of illustrations dimensions encoding each
//        for (int i = 0; i < dimensionsArray.length; i++) {
//            parameters.append("&" + dimensionsArray[i].getQueryString());
//        }
//
//        // Add the regions
//        Region[] regionArray = getRegions();
//        for (int i = 0; i < regionArray.length; i++) {
//            parameters.append("&" + regionArray[i].getQueryString());
//        }
//
//        // Add the layout override
//        parameters.append("&" + LAYOUT_OVERRIDE_PARAM_NAME + "=" + JSPlib.encode(this.layoutOverride + ""));
//
//        // Add the statistic
//        parameters.append("&" + STATISTIC_PARAM_NAME + "=" + JSPlib.encode(this.statistic + ""));
//
//        // Add the equation
//        parameters.append("&" + EQUATION_PARAM_NAME + "=" + JSPlib.encode(this.equation + ""));
//
//        // Add the formula
//        parameters.append("&" + FORMULA_PARAM_NAME + "=" + JSPlib.encode(this.formula + ""));
//
//        // Add the control
//        parameters.append("&" + CONTROL_PARAM_NAME + "=" + JSPlib.encode(this.control + ""));
//
//        // Add the controlRow
//        parameters.append("&" + CONTROL_ROW_PARAM_NAME + "=" + JSPlib.encode(this.controlRow + ""));
//
//        // Add the controlColumn
//        parameters.append("&" + CONTROL_COLUMN_PARAM_NAME + "=" + JSPlib.encode(this.controlColumn + ""));
//
//        // Add the range
//        parameters.append("&" + RANGE_PARAM_NAME + "=" + JSPlib.encode(this.range + ""));
//
//        return parameters.toString();
//    }

    /**
     * <p>
     * Sets the parameters of the illustration using a URL querystring
     * (deserializes an illustration from a querystring).
     * </p>
     *
     * <p>
     * Searches the querystring for parameters named the same as the constant
     * parameter names of an illustration. When if finds a parameter, it will
     * update the value of the illustration to the value on the querystring.
     * </p>
     *
     * <p>
     * If a given parameter is not found, the current value is left unchanged.
     * If an error occurs while parsing a parameter, the current value is left
     * unchanged.
     * </p>
     *
     * <p>
     * Any updates to the Illustration.getQueryString() function should be
     * mirrored here.
     * </p>
     *
     * @param queryString
     * <code>String</code> parameters of an illustration encoded
     * using URL querystring conventions.
     */
//    public void setFromQueryString(String queryString) {
//
//        // Check to make sure querystring is a valid query string
//        if (queryString != null) {
//
//            // Set the creation query string equal to the query string passed in
//            this.creationQueryString = JSPlib.checkStatus(queryString, DEFAULT_CREATION_QUERY_STRING);
//
//            // Look for each parameter and, if it exists, parse it
//
//            // ID
//            if (queryString.contains(ID_PARAM_NAME)) {
//                this.id = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, ID_PARAM_NAME)), this.id);
//            }
//
//            // creatorID
//            if (queryString.contains(CREATOR_ID_PARAM_NAME)) {
//                this.creatorID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CREATOR_ID_PARAM_NAME)), this.creatorID);
//            }
//
//            // Illustration name
//            if (queryString.contains(NAME_PARAM_NAME)) {
//                this.name = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, NAME_PARAM_NAME)), this.name);
//            }
//
//            // Creator Experiment ID
//            if (queryString.contains(CREATOR_EXPERIMENT_ID_PARAM_NAME)) {
//                this.creatorExperimentID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CREATOR_EXPERIMENT_ID_PARAM_NAME)),
//                        this.creatorExperimentID);
//            }
//
//            // Keystone file
//            if (queryString.contains(KEYSTONE_FILENAME_PARAM_NAME)) {
//                setKeystoneFilename(JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, KEYSTONE_FILENAME_PARAM_NAME)), this.keystoneFilename));
//            }
//
//            // Channel count
//            if (queryString.contains(CHANNEL_COUNT_PARAM_NAME)) {
//                this.channelCount = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CHANNEL_COUNT_PARAM_NAME)), this.channelCount);
//            }
//
//            if (this.channelCount > 0) {
//
//                // At this point we are just looking for parameters and entering
//                // the
//                // default if we don't find one. This won't work if there are
//                // not
//                // valid parameters, so we need to check to see whether the
//                // number of
//                // parameters found was right, at least. This check could be
//                // improved
//                // further, but the basic idea is to fire off
//                // setScalesAndChannels() if
//                // anything looks funny about the querystring. It would be
//                // better to
//                // lose scale customization but set the scales properly for the
//                // file
//                // than to go with the (potentially inappropriate) default
//                // values we
//                // have picked in illustration. Which is to say,
//                // setScalesAndChannels
//                // is much better than the defaults in Illustration, since it
//                // looks at
//                // the flow file.
//                //
//                // So, check to see if there are the right number of each
//                // parameter
//                // If not, fire off setScalesAndChannels
//
//                // Initialize the scales to the size of the channels and then
//                // look to
//                // see if we find similar sized arrays on the querystring
//                scaleFlags = new int[this.channelCount];
//                scaleArguments = new String[this.channelCount];
//                minimums = new double[this.channelCount];
//                maximums = new double[this.channelCount];
//
//                boolean foundAllScaleParameters = true;
//
//                if (JSPlib.getParameterArray(queryString, SCALE_FLAG_PARAM_NAME + "[0-9]*?").length != scaleFlags.length)
//                    foundAllScaleParameters = false;
//                if (JSPlib.getParameterArray(queryString, SCALE_ARGUMENT_PARAM_NAME + "[0-9]*?").length != scaleArguments.length)
//                    foundAllScaleParameters = false;
//                if (JSPlib.getParameterArray(queryString, SCALE_MINIMUM_PARAM_NAME + "[0-9]*?").length != minimums.length)
//                    foundAllScaleParameters = false;
//                if (JSPlib.getParameterArray(queryString, SCALE_MAXIMUM_PARAM_NAME + "[0-9]*?").length != maximums.length)
//                    foundAllScaleParameters = false;
//
//                if (foundAllScaleParameters) {
//                    // Look for each of the scale parameters corresponding to
//                    // each channel
//                    for (int i = 0; i < this.channelCount; i++) {
//                        scaleFlags[i] = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SCALE_FLAG_PARAM_NAME + i)), DEFAULT_SCALE_FLAG);
//                        scaleArguments[i] = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SCALE_ARGUMENT_PARAM_NAME + i)),
//                                DEFAULT_SCALE_ARGUMENT);
//                        minimums[i] = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SCALE_MINIMUM_PARAM_NAME + i)), DEFAULT_SCALE_MINIMUM);
//                        maximums[i] = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SCALE_MAXIMUM_PARAM_NAME + i)), DEFAULT_SCALE_MAXIMUM);
//                    }
//                } else {
//                    // If we didn't find all the parameters, set the scales and
//                    // channels
//                    // to the defaults from the file
//                    setScalesAndChannels();
//                }
//
//            }
//
//            // Check to see whether the channel and scales are OK, and if not,
//            // re-initialize the channels and scales using the keystone file
//            // name
//            if (!channelsAndScalesAreValid())
//                setScalesAndChannels();
//
//            // ********* Do keystone file, channels, and scales before doing x,
//            // y, z channel ************ //
//            // This is so that these values are not overwritten by
//            // setScalesAndChannels()
//
//            // Plot type
//            if (queryString.contains(PLOT_TYPE_PARAM_NAME)) {
//                this.plotType = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, PLOT_TYPE_PARAM_NAME)), getPlotType());
//            }
//
//            // Plot size
//            if (queryString.contains(PLOT_SIZE_PARAM_NAME)) {
//                this.plotSize = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, PLOT_SIZE_PARAM_NAME)), this.plotSize);
//            }
//
//            // X channel
//            if (queryString.contains(X_CHANNEL_PARAM_NAME)) {
//                this.xChannel = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, X_CHANNEL_PARAM_NAME)), this.xChannel);
//            }
//
//            // Y channel
//            if (queryString.contains(Y_CHANNEL_PARAM_NAME)) {
//                this.yChannel = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, Y_CHANNEL_PARAM_NAME)), this.yChannel);
//            }
//
//            // Z channel
//            if (queryString.contains(Z_CHANNEL_PARAM_NAME)) {
//                this.zChannel = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, Z_CHANNEL_PARAM_NAME)), this.zChannel);
//            }
//
//            // variableChannel
//            if (queryString.contains(VARIABLE_CHANNEL_PARAM_NAME)) {
//                this.variableChannel = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, VARIABLE_CHANNEL_PARAM_NAME)), this.variableChannel);
//            }
//
//            // Compensation ID
//            if (queryString.contains(COMPENSATION_ID_PARAM_NAME)) {
//                this.compensationID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, COMPENSATION_ID_PARAM_NAME)), this.compensationID);
//            }
//
//            // population type
//            if (queryString.contains(POPULATION_TYPE_PARAM_NAME)) {
//                this.populationType = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, POPULATION_TYPE_PARAM_NAME)), this.populationType);
//            }
//
//            // population cutoff
//            if (queryString.contains(POPULATION_CUTOFF_PARAM_NAME)) {
//                this.populationCutoff = JSPlib
//                        .checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, POPULATION_CUTOFF_PARAM_NAME)), this.populationCutoff);
//            }
//
//            // Event count
//            if (queryString.contains(EVENT_COUNT_PARAM_NAME)) {
//                this.eventCount = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, EVENT_COUNT_PARAM_NAME)), this.eventCount);
//            }
//
//            // Menu style
//            if (queryString.contains(MENU_STYLE_PARAM_NAME)) {
//                this.menuStyle = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, MENU_STYLE_PARAM_NAME)), this.menuStyle);
//            }
//
//            // Black canvas background
//            if (queryString.contains(BLACK_BACKGROUND_PARAM_NAME)) {
//                this.blackBackgroundP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, BLACK_BACKGROUND_PARAM_NAME)), this.blackBackgroundP);
//            }
//
//            // Black plot background
//            if (queryString.contains(BLACK_PLOT_BACKGROUND_PARAM_NAME)) {
//                this.blackPlotBackgroundP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, BLACK_PLOT_BACKGROUND_PARAM_NAME)),
//                        this.blackPlotBackgroundP);
//            }
//
//            // Use placeholders
//            if (queryString.contains(USE_PLACEHOLDERS_PARAM_NAME)) {
//                this.usePlaceholdersP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, USE_PLACEHOLDERS_PARAM_NAME)), this.usePlaceholdersP);
//            }
//
//            // Show labels
//            if (queryString.contains(SHOW_SCALE_LABELS_PARAM_NAME)) {
//                this.showScaleLabelsP = JSPlib
//                        .checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SHOW_SCALE_LABELS_PARAM_NAME)), this.showScaleLabelsP);
//            }
//
//            // Show scale ticks
//            if (queryString.contains(SHOW_SCALE_TICKS_PARAM_NAME)) {
//                this.showScaleTicksP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SHOW_SCALE_TICKS_PARAM_NAME)), this.showScaleTicksP);
//            }
//
//            // Stat type
//            if (queryString.contains(STAT_TYPE_PARAM_NAME)) {
//                this.statType = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, STAT_TYPE_PARAM_NAME)), this.statType);
//            }
//
//            // color set
//            if (queryString.contains(COLOR_SET_PARAM_NAME)) {
//                this.colorSet = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, COLOR_SET_PARAM_NAME)), this.colorSet);
//            }
//
//            // annotation
//            if (queryString.contains(ANNOTATION_PARAM_NAME)) {
//                this.annotationP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, ANNOTATION_PARAM_NAME)), this.annotationP);
//            }
//
//            // axis labels
//            if (queryString.contains(SHOW_AXIS_LABELS_PARAM_NAME)) {
//                this.showAxisLabelsP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SHOW_AXIS_LABELS_PARAM_NAME)), this.showAxisLabelsP);
//            }
//
//            // long labels
//            if (queryString.contains(LONG_LABELS_PARAM_NAME)) {
//                this.longLabelsP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, LONG_LABELS_PARAM_NAME)), this.longLabelsP);
//            }
//
//            // smoothing
//            if (queryString.contains(SMOOTHING_PARAM_NAME)) {
//                this.smoothing = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SMOOTHING_PARAM_NAME)), this.smoothing);
//            }
//
//            // aspectRatio
//            if (queryString.contains(ASPECT_RATIO_PARAM_NAME)) {
//                this.aspectRatio = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, ASPECT_RATIO_PARAM_NAME)), this.aspectRatio);
//            }
//
//            // contour Percent
//            if (queryString.contains(CONTOUR_PERCENT_PARAM_NAME)) {
//                this.contourPercent = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CONTOUR_PERCENT_PARAM_NAME)), this.contourPercent);
//            }
//
//            // contour start percent
//            if (queryString.contains(CONTOUR_START_PERCENT_PARAM_NAME)) {
//                this.contourStartPercent = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CONTOUR_START_PERCENT_PARAM_NAME)),
//                        this.contourStartPercent);
//            }
//
//            // Use printview
//            if (queryString.contains(USE_PRINT_VIEW_PARAM_NAME)) {
//                this.usePrintViewP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, USE_PRINT_VIEW_PARAM_NAME)), this.usePrintViewP);
//            }
//
//            // Panel default
//            if (queryString.contains(PANEL_DEFAULT_PARAM_NAME)) {
//                this.panelDefaultP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, PANEL_DEFAULT_PARAM_NAME)), this.panelDefaultP);
//            }
//
//            // Show details
//            if (queryString.contains(SHOW_DETAILS_PARAM_NAME)) {
//                this.showDetailsP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SHOW_DETAILS_PARAM_NAME)), this.showDetailsP);
//            }
//
//            // Show title
//            if (queryString.contains(SHOW_TITLE_PARAM_NAME)) {
//                this.showTitleP = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, SHOW_TITLE_PARAM_NAME)), this.showTitleP);
//            }
//
//            // layoutOverride
//            if (queryString.contains(LAYOUT_OVERRIDE_PARAM_NAME)) {
//                this.layoutOverride = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, LAYOUT_OVERRIDE_PARAM_NAME)), this.layoutOverride);
//            }
//            // statistic
//            if (queryString.contains(STATISTIC_PARAM_NAME)) {
//                this.statistic = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, STATISTIC_PARAM_NAME)), this.statistic);
//            }
//            // equation
//            if (queryString.contains(EQUATION_PARAM_NAME)) {
//                this.equation = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, EQUATION_PARAM_NAME)), this.equation);
//            }
//            // formula
//            if (queryString.contains(FORMULA_PARAM_NAME)) {
//                this.formula = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, FORMULA_PARAM_NAME)), this.formula);
//            }
//            // control
//            if (queryString.contains(CONTROL_PARAM_NAME)) {
//                this.control = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CONTROL_PARAM_NAME)), this.control);
//            }
//            // controlRow
//            if (queryString.contains(CONTROL_ROW_PARAM_NAME)) {
//                this.controlRow = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CONTROL_ROW_PARAM_NAME)), this.controlRow);
//            }
//            // controlColumn
//            if (queryString.contains(CONTROL_COLUMN_PARAM_NAME)) {
//                this.controlColumn = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, CONTROL_COLUMN_PARAM_NAME)), this.controlColumn);
//            }
//            // range
//            if (queryString.contains(RANGE_PARAM_NAME)) {
//                this.range = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, RANGE_PARAM_NAME)), this.range);
//            }
//
//            // Gate set IDs, need to parse out an array of parameters
//            if (queryString.contains(GATE_SET_IDS_PARAM_NAME)) {
//                // Get the array of gate set IDs
//                String[] gateSetIDStrings = JSPlib.getParameterArray(queryString, GATE_SET_IDS_PARAM_NAME + "[0-9]*?");
//
//                if (gateSetIDStrings == null) {
//                    // If the array of gate set IDs is null, then create an
//                    // empty array of gate set IDs.
//                    gateSetIDs = DEFAULT_GATE_SET_IDS;
//                } else {
//                    // Otherwise, the array of gate set IDs is not null, then
//                    // parse the gate set IDs.
//                    gateSetIDs = new ArrayList<Integer>();
//
//                    // Loop through the array of gate set IDs parsing each
//                    for (int i = 0; i < gateSetIDStrings.length; i++) {
//                        gateSetIDs.add(JSPlib.checkStatus(gateSetIDStrings[i], UNGATED));
//                    }
//                }
//            }
//
//            // Match the dimension prefix, plus any number of numbers, then ID
//            int[] activeDimensionIDs = JSPlib.checkStatus(JSPlib.getParameterArray(queryString, DIMENSION_PREFIX + "[0-9]*?"
//                    + IllustrationDimension.ID_PARAM_NAME), new int[0], -1);
//            if (activeDimensionIDs != null && activeDimensionIDs.length > 0) {
//                this.dimensions.clear();
//                for (int i = 0; i < activeDimensionIDs.length; i++) {
//
//                    IllustrationDimension newDimension = new IllustrationDimension(activeDimensionIDs[i]);
//                    newDimension = newDimension.setFromQueryString(activeDimensionIDs[i], queryString);
//                    this.dimensions.add(newDimension);
//                }
//
//                // Sort the dimensions by order
//                Collections.sort(dimensions);
//
//            }
//
//            // Match the region prefix, plus any number of numbers, then ID
//            // Make sure to allow for the minus sign, since these IDs can go
//            // negative
//            int[] activeRegionIDs = JSPlib.checkStatus(JSPlib.getParameterArray(queryString, REGION_PREFIX + "[\\-]*+[0-9]*?" + Region.ID_PARAM_NAME),
//                    new int[0], ALL_REGIONS);
//
//            if (activeRegionIDs != null && activeRegionIDs.length > 0) {
//                this.regions.clear();
//                for (int i = 0; i < activeRegionIDs.length; i++) {
//
//                    Region newRegion = new Region(activeRegionIDs[i]);
//                    newRegion = newRegion.setFromQueryString(activeRegionIDs[i], queryString);
//                    this.regions.add(newRegion);
//                }
//
//            }
//
//        }
//
//        // Make certain to sort the dimensions
//        if (hasActiveDimensions()) {
//
//            // Sort the dimensions by order
//            Collections.sort(dimensions);
//
//            // Activate a row and column, if there is one
//            if (dimensions.size() > 0) {
//                setColumn(dimensions.get(0));
//            }
//
//            if (dimensions.size() > 1) {
//                setRow(dimensions.get(1));
//            }
//        }
//
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * XML Serialization / Deserialization
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns an <code>org.jdom.Element</code> object corresponding to the
     * illustration.
     * </p>
     *
     * <p>
     * Any updates to this function should be mirrored in
     * Illustration.setFromElement()
     * </p>
     *
     * @return <code>org.jdom.Element</code> XML element object encoding the
     * current state of the illustration.
     */

//    public Element getElement() {
//
//        // Create the illustration element
//        Element illustrationElement = new Element(ILLUSTRATION_PARAM_NAME);
//
//        // Encode the parameters the Illustration and add them to the
//        // illustration element
//        illustrationElement.addContent(XMLLog.encodeElement(ID_PARAM_NAME, Integer.toString(id)));
//
//        if (name != null) {
//            // If the name of the Illustration is not null, then encode it.
//            illustrationElement.addContent(XMLLog.encodeElement(NAME_PARAM_NAME, name));
//        }
//
//        illustrationElement.addContent(XMLLog.encodeElement(CREATOR_EXPERIMENT_ID_PARAM_NAME, Integer.toString(creatorExperimentID)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CREATOR_ID_PARAM_NAME, Integer.toString(creatorID)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(PLOT_TYPE_PARAM_NAME, Integer.toString(plotType)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(PLOT_SIZE_PARAM_NAME, Integer.toString(plotSize)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(X_CHANNEL_PARAM_NAME, Integer.toString(xChannel)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(Y_CHANNEL_PARAM_NAME, Integer.toString(yChannel)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(Z_CHANNEL_PARAM_NAME, Integer.toString(zChannel)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(VARIABLE_CHANNEL_PARAM_NAME, Integer.toString(variableChannel)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(POPULATION_TYPE_PARAM_NAME, Integer.toString(populationType)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(POPULATION_CUTOFF_PARAM_NAME, Integer.toString(populationCutoff)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(EVENT_COUNT_PARAM_NAME, Integer.toString(eventCount)));
//
//        // Create the gateSet IDs element to hold the gate set IDs
//        Element gateSetIDsElement = new Element(GATE_SET_IDS_PARAM_NAME);
//
//        // Add the gate set IDs
//        int[] gateSetIDArray = getGateSetIDs();
//        for (int i = 0; i < gateSetIDArray.length; i++) {
//            // Add the gate set ids
//            gateSetIDsElement.addContent(XMLLog.encodeElement(GATE_SET_IDS_PARAM_NAME + Integer.toString(gateSetIDArray[i]), Integer
//                    .toString(gateSetIDArray[i])));
//        }
//
//        // Add the gateSet IDs element to the element
//        illustrationElement.addContent(gateSetIDsElement);
//
//        // Create the scale element to hold the scale parameters
//        Element scaleElement = new Element(SCALE_FLAG_PARAM_NAME);
//
//        // Check to see whether the channel and scales are OK,
//        if (channelsAndScalesAreValid()) {
//            // Add the scale and channel parameters
//            for (int i = 0; i < channelCount; i++) {
//                // Create the current scale element to hold the parameters for
//                // this scale
//                Element currentScaleElement = new Element(SCALE_FLAG_PARAM_NAME + i);
//
//                // Add the gate set ids
//                currentScaleElement.addContent(XMLLog.encodeElement(SCALE_FLAG_PARAM_NAME + Integer.toString(i), Integer.toString(scaleFlags[i])));
//                currentScaleElement.addContent(XMLLog.encodeElement(SCALE_ARGUMENT_PARAM_NAME + Integer.toString(i), scaleArguments[i]));
//                currentScaleElement.addContent(XMLLog.encodeElement(SCALE_MINIMUM_PARAM_NAME + Integer.toString(i), Double.toString(minimums[i])));
//                currentScaleElement.addContent(XMLLog.encodeElement(SCALE_MAXIMUM_PARAM_NAME + Integer.toString(i), Double.toString(maximums[i])));
//
//                // Add the current scale element to the scale element
//                scaleElement.addContent(currentScaleElement);
//
//            }
//        }
//
//        // Add the scale element to the illustration element
//        illustrationElement.addContent(scaleElement);
//
//        illustrationElement.addContent(XMLLog.encodeElement(COMPENSATION_ID_PARAM_NAME, Integer.toString(compensationID)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CHANNEL_COUNT_PARAM_NAME, Integer.toString(channelCount)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(MENU_STYLE_PARAM_NAME, Integer.toString(menuStyle)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(BLACK_BACKGROUND_PARAM_NAME, Boolean.toString(blackBackgroundP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(BLACK_PLOT_BACKGROUND_PARAM_NAME, Boolean.toString(blackPlotBackgroundP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(USE_PLACEHOLDERS_PARAM_NAME, Boolean.toString(usePlaceholdersP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SHOW_SCALE_LABELS_PARAM_NAME, Boolean.toString(showScaleLabelsP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(STAT_TYPE_PARAM_NAME, Integer.toString(statType)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(COLOR_SET_PARAM_NAME, Integer.toString(colorSet)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(ANNOTATION_PARAM_NAME, Boolean.toString(annotationP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SHOW_SCALE_TICKS_PARAM_NAME, Boolean.toString(showScaleTicksP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SHOW_AXIS_LABELS_PARAM_NAME, Boolean.toString(showAxisLabelsP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(LONG_LABELS_PARAM_NAME, Boolean.toString(longLabelsP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SMOOTHING_PARAM_NAME, Double.toString(smoothing)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(ASPECT_RATIO_PARAM_NAME, Double.toString(aspectRatio)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CONTOUR_PERCENT_PARAM_NAME, Double.toString(contourPercent)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CONTOUR_START_PERCENT_PARAM_NAME, Double.toString(contourStartPercent)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(USE_PRINT_VIEW_PARAM_NAME, Boolean.toString(usePrintViewP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SHOW_DETAILS_PARAM_NAME, Boolean.toString(showDetailsP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(PANEL_DEFAULT_PARAM_NAME, Boolean.toString(panelDefaultP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(SHOW_TITLE_PARAM_NAME, Boolean.toString(showTitleP)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(LAYOUT_OVERRIDE_PARAM_NAME, Integer.toString(layoutOverride)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(STATISTIC_PARAM_NAME, Integer.toString(statistic)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(EQUATION_PARAM_NAME, Integer.toString(equation)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(FORMULA_PARAM_NAME, formula));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CONTROL_PARAM_NAME, Integer.toString(control)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CONTROL_ROW_PARAM_NAME, Integer.toString(controlRow)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(CONTROL_COLUMN_PARAM_NAME, Integer.toString(controlColumn)));
//
//        illustrationElement.addContent(XMLLog.encodeElement(RANGE_PARAM_NAME, Double.toString(range)));
//
//        // Save the creation query string in XML -- uncomment this for testing
//        // illustrationElement.addContent(XMLLog.encodeElement(CREATION_QUERY_STRING_PARAM_NAME,
//        // creationQueryString));
//
//        if (keystoneFilename != null) {
//            illustrationElement.addContent(XMLLog.encodeElement(KEYSTONE_FILENAME_PARAM_NAME, keystoneFilename));
//        }
//
//        /**
//         * Encode the illustration dimensions
//         */
//
//        // Get the array of illustration dimensions
//        IllustrationDimension[] dimensionsArray = getActiveDimensions();
//
//        // Create the dimensions element to hold the illustration dimensions
//        Element dimensionsElement = new Element(ACTIVE_DIMENSIONS_PARAM_NAME);
//
//        // Loop through the array of illustrations dimensions encoding each
//        for (int i = 0; i < dimensionsArray.length; i++) {
//            dimensionsElement.addContent(dimensionsArray[i].getElement());
//        }
//
//        // Add the dimensions element to the element
//        illustrationElement.addContent(dimensionsElement);
//
//        /**
//         * Encode the regions
//         */
//
//        // Get the array of regions
//        Region[] regionArray = getRegions();
//
//        // Create the regions element to hold the regions
//        Element allRegionsElement = new Element(REGION_PREFIX);
//
//        // Loop through the array of regions encoding each
//        for (int i = 0; i < regionArray.length; i++) {
//            allRegionsElement.addContent(regionArray[i].getElement());
//        }
//
//        // Add the dimensions element to the element
//        illustrationElement.addContent(allRegionsElement);
//
//        // return final element
//        return illustrationElement;
//
//    }

    /**
     * <p>
     * Sets the parameters of the illustration using an XML
     * <code>org.jdom.Element</code> encoding of an illustration.
     * </p>
     *
     * <p>
     * Any updates to Illustration.getElement() should be mirrored here.
     * </p>
     *
     * @param element
     * <code>org.jdom.Element</code> XML element object encoding an
     * illustration.
     */
//    public void setFromElement(Element element) {
//        if (element != null) {
//            // If there is an element, look for parameters and, if they exist,
//            // parse them
//            // Default to the current value if nothing is found
//
//            // Parse the id
//            if (element.getChild(ID_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.id = JSPlib.checkStatus(element.getChild(ID_PARAM_NAME).getText(), this.id);
//            }
//
//            // Parse the creatorID
//            if (element.getChild(CREATOR_ID_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.creatorID = JSPlib.checkStatus(element.getChild(CREATOR_ID_PARAM_NAME).getText(), this.creatorID);
//            }
//
//            // Parse the creator experiment id
//            if (element.getChild(CREATOR_EXPERIMENT_ID_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.creatorExperimentID = JSPlib.checkStatus(element.getChild(CREATOR_EXPERIMENT_ID_PARAM_NAME).getText(), this.creatorExperimentID);
//            }
//
//            // Parse the channel count
//            if (element.getChild(CHANNEL_COUNT_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.channelCount = JSPlib.checkStatus(element.getChild(CHANNEL_COUNT_PARAM_NAME).getText(), this.channelCount);
//            }
//
//            // Parse the keystone filename
//            if (element.getChild(KEYSTONE_FILENAME_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.keystoneFilename = JSPlib.checkStatus(element.getChild(KEYSTONE_FILENAME_PARAM_NAME).getText(), this.keystoneFilename);
//            }
//
//            // If there are channels, try to get the scales
//            if (this.channelCount > 0) {
//
//                if (element.getChild(SCALE_FLAG_PARAM_NAME) != null) {
//                    // Initialize the scale settings
//                    this.scaleFlags = new int[channelCount];
//                    this.scaleArguments = new String[channelCount];
//                    this.minimums = new double[channelCount];
//                    this.maximums = new double[channelCount];
//
//                    // Get the scale element
//                    Element scaleElement = element.getChild(SCALE_FLAG_PARAM_NAME);
//
//                    for (int i = 0; i < channelCount; i++) {
//
//                        // Look for a scale element named according to the
//                        // channel index
//                        if (scaleElement.getChild(SCALE_FLAG_PARAM_NAME + i) != null) {
//                            // If the scale element for this channel exists,
//                            // Get it and look for each child
//                            Element currentChannelScaleElement = scaleElement.getChild(SCALE_FLAG_PARAM_NAME + i);
//
//                            scaleFlags[i] = JSPlib.checkStatus(currentChannelScaleElement.getChild(SCALE_FLAG_PARAM_NAME + i).getText(), DEFAULT_SCALE_FLAG);
//                            scaleArguments[i] = JSPlib.checkStatus(currentChannelScaleElement.getChild(SCALE_ARGUMENT_PARAM_NAME + i).getText(),
//                                    DEFAULT_SCALE_ARGUMENT);
//                            minimums[i] = JSPlib
//                                    .checkStatus(currentChannelScaleElement.getChild(SCALE_MINIMUM_PARAM_NAME + i).getText(), DEFAULT_SCALE_MINIMUM);
//                            maximums[i] = JSPlib
//                                    .checkStatus(currentChannelScaleElement.getChild(SCALE_MAXIMUM_PARAM_NAME + i).getText(), DEFAULT_SCALE_MAXIMUM);
//
//                        }
//
//                    }
//                }
//
//            }
//
//            // Check to see whether the channel and scales are OK, and if not,
//            // re-initialize the channels and scales using the keystone file
//            // name
//            if (!channelsAndScalesAreValid())
//                setScalesAndChannels();
//
//            // ********* Do keystone file, channels, and scales before doing x,
//            // y, z channel ************ //
//            // This is so that these values are not overwritten by
//            // setScalesAndChannels()
//
//            // Parse the name
//            if (element.getChild(NAME_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.name = JSPlib.checkStatus(element.getChild(NAME_PARAM_NAME).getText(), this.name);
//            }
//
//            // Parse the plotType
//            if (element.getChild(PLOT_TYPE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.plotType = JSPlib.checkStatus(element.getChild(PLOT_TYPE_PARAM_NAME).getText(), getPlotType());
//            }
//
//            // Parse the plotSize
//            if (element.getChild(PLOT_SIZE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.plotSize = JSPlib.checkStatus(element.getChild(PLOT_SIZE_PARAM_NAME).getText(), this.plotSize);
//            }
//
//            // Parse the x channel
//            if (element.getChild(X_CHANNEL_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.xChannel = JSPlib.checkStatus(element.getChild(X_CHANNEL_PARAM_NAME).getText(), this.xChannel);
//            }
//
//            // Parse the y channel
//            if (element.getChild(Y_CHANNEL_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.yChannel = JSPlib.checkStatus(element.getChild(Y_CHANNEL_PARAM_NAME).getText(), this.yChannel);
//            }
//
//            // Parse the z channel
//            if (element.getChild(Z_CHANNEL_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.zChannel = JSPlib.checkStatus(element.getChild(Z_CHANNEL_PARAM_NAME).getText(), this.zChannel);
//            }
//
//            // Parse the variable channel
//            if (element.getChild(VARIABLE_CHANNEL_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.variableChannel = JSPlib.checkStatus(element.getChild(VARIABLE_CHANNEL_PARAM_NAME).getText(), this.variableChannel);
//            }
//
//            // Parse the compensation id
//            if (element.getChild(COMPENSATION_ID_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.compensationID = JSPlib.checkStatus(element.getChild(COMPENSATION_ID_PARAM_NAME).getText(), this.compensationID);
//            }
//
//            if (element.getChild(GATE_SET_IDS_PARAM_NAME) != null) {
//                // Parse the gate set ids
//                this.gateSetIDs = new ArrayList<Integer>();
//
//                // Get the gateSetIDs element
//                Element gateSetIDsElement = element.getChild(GATE_SET_IDS_PARAM_NAME);
//
//                if (gateSetIDsElement.getChildren() != null) {
//                    List gateSetList = gateSetIDsElement.getChildren();
//
//                    if (gateSetList != null) {
//
//                        Iterator iterator = gateSetList.iterator();
//
//                        while (iterator.hasNext()) {
//                            // Get the gateSetID element
//                            Element gateSetIDElement = (Element) iterator.next();
//
//                            if (gateSetIDElement != null && gateSetIDElement.getText() != null) {
//                                int gateSetID = JSPlib.checkStatus(gateSetIDElement.getText(), UNGATED);
//                                gateSetIDs.add(gateSetID);
//                            }
//
//                        }
//                    }
//                }
//            }
//
//            // Parse the menu Style
//            if (element.getChild(CITATION_FORMAT_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.citationFormat = JSPlib.checkStatus(element.getChild(CITATION_FORMAT_PARAM_NAME).getText(), this.citationFormat);
//            }
//
//            // Parse the viewStatus
//            if (element.getChild(VIEW_STATUS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.viewStatus = JSPlib.checkStatus(element.getChild(VIEW_STATUS_PARAM_NAME).getText(), this.viewStatus);
//            }
//
//            // Parse the editStatus
//            if (element.getChild(EDIT_STATUS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.editStatus = JSPlib.checkStatus(element.getChild(EDIT_STATUS_PARAM_NAME).getText(), this.editStatus);
//            }
//
//            // Parse the menu Style
//            if (element.getChild(MENU_STYLE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.menuStyle = JSPlib.checkStatus(element.getChild(MENU_STYLE_PARAM_NAME).getText(), this.menuStyle);
//            }
//
//            // Parse the blackBooleanP
//            if (element.getChild(BLACK_BACKGROUND_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.blackBackgroundP = JSPlib.checkStatus(element.getChild(BLACK_BACKGROUND_PARAM_NAME).getText(), this.blackBackgroundP);
//            }
//
//            // Parse the blackPlotBooleanP
//            if (element.getChild(BLACK_PLOT_BACKGROUND_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.blackPlotBackgroundP = JSPlib.checkStatus(element.getChild(BLACK_PLOT_BACKGROUND_PARAM_NAME).getText(), this.blackPlotBackgroundP);
//            }
//
//            // Parse the showScaleTicksP
//            if (element.getChild(SHOW_SCALE_TICKS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.showScaleTicksP = JSPlib.checkStatus(element.getChild(SHOW_SCALE_TICKS_PARAM_NAME).getText(), this.showScaleTicksP);
//            }
//
//            // Parse the showScaleLabelsP
//            if (element.getChild(SHOW_SCALE_LABELS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.showScaleLabelsP = JSPlib.checkStatus(element.getChild(SHOW_SCALE_LABELS_PARAM_NAME).getText(), this.showScaleLabelsP);
//            }
//
//            // Parse the stat type
//            if (element.getChild(STAT_TYPE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.statType = JSPlib.checkStatus(element.getChild(STAT_TYPE_PARAM_NAME).getText(), this.statType);
//            }
//
//            // Parse the color set
//            if (element.getChild(COLOR_SET_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.colorSet = JSPlib.checkStatus(element.getChild(COLOR_SET_PARAM_NAME).getText(), this.colorSet);
//            }
//
//            // Parse the annotation
//            if (element.getChild(ANNOTATION_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.annotationP = JSPlib.checkStatus(element.getChild(ANNOTATION_PARAM_NAME).getText(), this.annotationP);
//            }
//
//            // Parse the axis labels
//            if (element.getChild(SHOW_AXIS_LABELS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.showAxisLabelsP = JSPlib.checkStatus(element.getChild(SHOW_AXIS_LABELS_PARAM_NAME).getText(), this.showAxisLabelsP);
//            }
//
//            // Parse the long labels flag
//            if (element.getChild(LONG_LABELS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.longLabelsP = JSPlib.checkStatus(element.getChild(LONG_LABELS_PARAM_NAME).getText(), this.longLabelsP);
//            }
//
//            // Parse the smoothing
//            if (element.getChild(SMOOTHING_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.smoothing = JSPlib.checkStatus(element.getChild(SMOOTHING_PARAM_NAME).getText(), this.smoothing);
//            }
//
//            // Parse the aspectRatio
//            if (element.getChild(ASPECT_RATIO_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.aspectRatio = JSPlib.checkStatus(element.getChild(ASPECT_RATIO_PARAM_NAME).getText(), this.aspectRatio);
//            }
//
//            // Parse the contourPercent
//            if (element.getChild(CONTOUR_PERCENT_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.contourPercent = JSPlib.checkStatus(element.getChild(CONTOUR_PERCENT_PARAM_NAME).getText(), this.contourPercent);
//            }
//
//            // Parse the contour start Percent
//            if (element.getChild(CONTOUR_START_PERCENT_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.contourStartPercent = JSPlib.checkStatus(element.getChild(CONTOUR_START_PERCENT_PARAM_NAME).getText(), this.contourStartPercent);
//            }
//
//            // Parse the usePlaceholdersP
//            if (element.getChild(USE_PLACEHOLDERS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.usePlaceholdersP = JSPlib.checkStatus(element.getChild(USE_PLACEHOLDERS_PARAM_NAME).getText(), this.usePlaceholdersP);
//            }
//
//            // Parse the usePrintViewP
//            if (element.getChild(USE_PRINT_VIEW_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.usePrintViewP = JSPlib.checkStatus(element.getChild(USE_PRINT_VIEW_PARAM_NAME).getText(), this.usePrintViewP);
//            }
//
//            // Parse the panelDefaultP
//            if (element.getChild(PANEL_DEFAULT_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.panelDefaultP = JSPlib.checkStatus(element.getChild(PANEL_DEFAULT_PARAM_NAME).getText(), this.panelDefaultP);
//            }
//
//            // Parse the showDetailsP
//            if (element.getChild(SHOW_DETAILS_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.showDetailsP = JSPlib.checkStatus(element.getChild(SHOW_DETAILS_PARAM_NAME).getText(), this.showDetailsP);
//            }
//
//            // Parse the showTitleP
//            if (element.getChild(SHOW_TITLE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.showTitleP = JSPlib.checkStatus(element.getChild(SHOW_TITLE_PARAM_NAME).getText(), this.showTitleP);
//            }
//
//            // Parse the layoutOverride
//            if (element.getChild(LAYOUT_OVERRIDE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.layoutOverride = JSPlib.checkStatus(element.getChild(LAYOUT_OVERRIDE_PARAM_NAME).getText(), this.layoutOverride);
//            }
//
//            // Parse the statistic
//            if (element.getChild(STATISTIC_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.statistic = JSPlib.checkStatus(element.getChild(STATISTIC_PARAM_NAME).getText(), this.statistic);
//            }
//
//            // Parse the equation
//            if (element.getChild(EQUATION_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.equation = JSPlib.checkStatus(element.getChild(EQUATION_PARAM_NAME).getText(), this.equation);
//            }
//
//            // Parse the formula
//            if (element.getChild(FORMULA_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.formula = JSPlib.checkStatus(element.getChild(FORMULA_PARAM_NAME).getText(), this.formula);
//            }
//
//            // Parse the control
//            if (element.getChild(CONTROL_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.control = JSPlib.checkStatus(element.getChild(CONTROL_PARAM_NAME).getText(), this.control);
//            }
//
//            // Parse the controlRow
//            if (element.getChild(CONTROL_ROW_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.controlRow = JSPlib.checkStatus(element.getChild(CONTROL_ROW_PARAM_NAME).getText(), this.controlRow);
//            }
//
//            // Parse the controlColumn
//            if (element.getChild(CONTROL_COLUMN_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.controlColumn = JSPlib.checkStatus(element.getChild(CONTROL_COLUMN_PARAM_NAME).getText(), this.controlColumn);
//            }
//
//            // Parse the range
//            if (element.getChild(RANGE_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.range = JSPlib.checkStatus(element.getChild(RANGE_PARAM_NAME).getText(), this.range);
//            }
//
//            // Parse the creationQueryString
//            if (element.getChild(CREATION_QUERY_STRING_PARAM_NAME) != null) {
//                // If there is a matching element
//                this.creationQueryString = JSPlib.checkStatus(element.getChild(CREATION_QUERY_STRING_PARAM_NAME).getText(), this.creationQueryString);
//            }
//
//            /**
//             * Parse the illustration dimensions
//             */
//
//            // Create the list of the remaining illustration dimensions
//            this.dimensions = new ArrayList<IllustrationDimension>();
//
//            // Get the dimensions element
//            Element dimensionsElement = element.getChild(ACTIVE_DIMENSIONS_PARAM_NAME);
//
//            if (dimensionsElement != null) {
//                // If the dimensions element exists, then parse the illustration
//                // dimensions.
//
//                // Clear out the illustration dimensions
//                dimensions.clear();
//
//                // Get all the illustration dimensions
//                List dimensionsList = dimensionsElement.getChildren();
//                Iterator iterator = dimensionsList.iterator();
//
//                Element dimensionElement;
//
//                while (iterator.hasNext()) {
//                    // Get the dimension element
//                    dimensionElement = (Element) iterator.next();
//
//                    // Initialize a new dimension
//                    IllustrationDimension dim = new IllustrationDimension(-1);
//
//                    // Get the illustration dimension from the dimension element
//                    dim = dim.setFromElement(dimensionElement);
//
//                    if (dim != null) {
//                        // If the illustration dimension is not null, then add
//                        // it to the list of remaining ilustration dimensions.
//                        dimensions.add(dim);
//                    }
//                }
//
//            }
//
//            /**
//             * Parse the regions
//             */
//
//            // Create the list of the remaining regions
//            this.regions = new ArrayList<Region>();
//
//            // Get the regions element
//            Element allRegionsElement = element.getChild(REGION_PREFIX);
//
//            if (allRegionsElement != null) {
//                // If the regions element exists, then parse the regions
//
//                // Get all the regions
//                List regionsList = allRegionsElement.getChildren();
//                Iterator iterator = regionsList.iterator();
//
//                Element regionElement;
//
//                // Clear out the regions
//                regions.clear();
//
//                while (iterator.hasNext()) {
//                    // Get the region element
//                    regionElement = (Element) iterator.next();
//
//                    // Initialize a new region
//                    Region currentRegion = new Region(-1);
//
//                    // Get the illustration dimension from the dimension element
//                    currentRegion = currentRegion.setFromElement(regionElement);
//
//                    if (currentRegion != null) {
//                        // If the region is not null, then add it to the list of
//                        // regions
//                        regions.add(currentRegion);
//                    }
//                }
//
//            }
//
//        }
//
//        if (hasActiveDimensions()) {
//
//            // Sort the dimensions by order
//            Collections.sort(dimensions);
//
//            // Activate a row and column, if there is one
//            if (dimensions.size() > 0) {
//                setColumn(dimensions.get(0));
//            }
//
//            if (dimensions.size() > 1) {
//                setRow(dimensions.get(1));
//            }
//        }
//
//    }

    /**
     * ***********************************************************************
     * ***********************************************************************
     * HTML Output functions
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * <p>
     * Returns the parameters of the illustration encoded as a string in html
     * format. This is useful for printing out the details of the illustration
     * in a webpage.
     * </p>
     *
     * @return <code>String</code> parameters of the illustration encoded as an
     * HTML formatted string.
     */
//    public String getDetails() {
//        // Create a StringBuffer with which to encode the details
//        StringBuffer details = new StringBuffer();
//
//        // Start out a table
//        details.append("<table class=\"SmallTable\" width=\"500\"><tr>");
//
//        // Add the id
//        details.append("<td>" + ID_PARAM_NAME + "</td><td>" + (this.id + "") + "</td></tr><tr>");
//
//        // Add the creator experiment ID
//        details.append("<td>" + CREATOR_EXPERIMENT_ID_PARAM_NAME + "</td><td>" + (this.creatorExperimentID + "") + "</td></tr><tr>");
//
//        // Add the name ... aka the title
//        details.append("<td>" + NAME_PARAM_NAME + "</td><td>" + (this.name) + "</td></tr><tr>");
//
//        // Add whether the title is shown
//        details.append("<td>" + SHOW_TITLE_PARAM_NAME + "</td><td>" + (this.showTitleP) + "</td></tr><tr>");
//
//        // Add the plot type
//        details.append("<td>" + PLOT_TYPE_PARAM_NAME + "</td><td>" + (this.plotType + "") + "</td></tr><tr>");
//
//        // Add the plot size
//        details.append("<td>" + PLOT_SIZE_PARAM_NAME + "</td><td>" + (this.plotSize + "") + "</td></tr><tr>");
//
//        // Add the xChannel
//        details.append("<td>" + X_CHANNEL_PARAM_NAME + "</td><td>" + (this.xChannel + "") + "</td></tr><tr>");
//
//        // Add the yChannel
//        details.append("<td>" + Y_CHANNEL_PARAM_NAME + "</td><td>" + (this.yChannel + "") + "</td></tr><tr>");
//
//        // Add the zChannel
//        details.append("<td>" + Z_CHANNEL_PARAM_NAME + "</td><td>" + (this.zChannel + "") + "</td></tr><tr>");
//
//        // Add the variableChannel
//        details.append("<td>" + VARIABLE_CHANNEL_PARAM_NAME + "</td><td>" + (this.variableChannel + "") + "</td></tr><tr>");
//
//        // Add the black canvas background flag
//        details.append("<td>" + BLACK_BACKGROUND_PARAM_NAME + "</td><td>" + (this.blackBackgroundP + "") + "</td></tr><tr>");
//
//        // Add the black plot background flag
//        details.append("<td>" + BLACK_PLOT_BACKGROUND_PARAM_NAME + "</td><td>" + (this.blackPlotBackgroundP + "") + "</td></tr><tr>");
//
//        // Add the use place holders flag
//        details.append("<td>" + USE_PLACEHOLDERS_PARAM_NAME + "</td><td>" + (this.usePlaceholdersP + "") + "</td></tr><tr>");
//
//        // Add the compensation id
//        details.append("<td>" + COMPENSATION_ID_PARAM_NAME + "</td><td>" + (this.compensationID + "") + "</td></tr><tr>");
//
//        // Add the channel count
//        details.append("<td>" + CHANNEL_COUNT_PARAM_NAME + "</td><td>" + (this.channelCount + "") + "</td></tr><tr>");
//
//        // Add the population type
//        details.append("<td>" + POPULATION_TYPE_PARAM_NAME + "</td><td>" + (this.populationType + "") + "</td></tr><tr>");
//
//        // Add the population cutoff
//        details.append("<td>" + POPULATION_CUTOFF_PARAM_NAME + "</td><td>" + (this.populationCutoff + "") + "</td></tr><tr>");
//
//        // Add the event count
//        details.append("<td>" + EVENT_COUNT_PARAM_NAME + "</td><td>" + (this.eventCount + "") + "</td></tr><tr>");
//
//        // If there are channels and the scales match them, then print out the
//        // scales
//        if (channelsAndScalesAreValid()) {
//            // Add the scale settings
//            for (int i = 0; i < channelCount; i++) {
//                // Add the scale flags
//                details.append("<td>" + SCALE_FLAG_PARAM_NAME + channelCount + "</td><td>" + (this.scaleFlags[i] + "") + "</td></tr><tr>");
//
//                // Add the scale arguments
//                details.append("<td>" + SCALE_ARGUMENT_PARAM_NAME + channelCount + "</td><td>" + (this.scaleArguments[i] + "") + "</td></tr><tr>");
//
//                // Add the scale minimums
//                details.append("<td>" + SCALE_MINIMUM_PARAM_NAME + channelCount + "</td><td>" + (this.minimums[i] + "") + "</td></tr><tr>");
//
//                // Add the scale maximums
//                details.append("<td>" + SCALE_MAXIMUM_PARAM_NAME + channelCount + "</td><td>" + (this.maximums[i] + "") + "</td></tr><tr>");
//            }
//        }
//
//        // Add the regions
//        Region[] regionArray = getRegions();
//        details.append("<td>" + REGION_PREFIX + "</td><td>");
//        for (int i = 0; i < regionArray.length; i++) {
//            details.append(regionArray[i].getID() + " ");
//            details.append(regionArray[i].isPercentShown() + ", ");
//        }
//        details.append("</td></tr><tr>");
//
//        // Add the gate set IDs
//        int[] gateSetIDArray = getGateSetIDs();
//        details.append("<td>" + GATE_SET_IDS_PARAM_NAME + "</td><td>");
//        for (int i = 0; i < gateSetIDArray.length; i++) {
//            details.append(gateSetIDArray[i] + ", ");
//        }
//        details.append("</td></tr><tr>");
//
//        // Add the citation format
//        details.append("<td>" + CITATION_FORMAT_PARAM_NAME + "</td><td>" + (this.citationFormat + "") + "</td></tr><tr>");
//
//        // Add the editStatus
//        details.append("<td>" + EDIT_STATUS_PARAM_NAME + "</td><td>" + (this.editStatus + "") + "</td></tr><tr>");
//
//        // Add the viewStatus
//        details.append("<td>" + VIEW_STATUS_PARAM_NAME + "</td><td>" + (this.viewStatus + "") + "</td></tr><tr>");
//
//        // Add the creatorID
//        details.append("<td>" + CREATOR_ID_PARAM_NAME + "</td><td>" + (this.creatorID + "") + "</td></tr><tr>");
//
//        // Add the menu style
//        details.append("<td>" + MENU_STYLE_PARAM_NAME + "</td><td>" + (this.menuStyle + "") + "</td></tr><tr>");
//
//        // Add the show labels flag
//        details.append("<td>" + SHOW_SCALE_TICKS_PARAM_NAME + "</td><td>" + (this.showScaleTicksP + "") + "</td></tr><tr>");
//
//        // Add the show labels flag
//        details.append("<td>" + SHOW_SCALE_LABELS_PARAM_NAME + "</td><td>" + (this.showScaleLabelsP + "") + "</td></tr><tr>");
//
//        // Add the state type
//        details.append("<td>" + STAT_TYPE_PARAM_NAME + "</td><td>" + (this.statType + "") + "</td></tr><tr>");
//
//        // Add the color set
//        details.append("<td>" + COLOR_SET_PARAM_NAME + "</td><td>" + (this.colorSet + "") + "</td></tr><tr>");
//
//        // Add the annotation flag
//        details.append("<td>" + ANNOTATION_PARAM_NAME + "</td><td>" + (this.annotationP + "") + "</td></tr><tr>");
//
//        // Add the show axis labels flag
//        details.append("<td>" + SHOW_AXIS_LABELS_PARAM_NAME + "</td><td>" + (this.showAxisLabelsP + "") + "</td></tr><tr>");
//
//        // Add the long labels flag
//        details.append("<td>" + LONG_LABELS_PARAM_NAME + "</td><td>" + (this.longLabelsP + "") + "</td></tr><tr>");
//
//        // Add the smoothing
//        details.append("<td>" + SMOOTHING_PARAM_NAME + "</td><td>" + (this.smoothing + "") + "</td></tr><tr>");
//
//        // Add the aspect ratio
//        details.append("<td>" + ASPECT_RATIO_PARAM_NAME + "</td><td>" + (this.aspectRatio + "") + "</td></tr><tr>");
//
//        // Add the contour percent
//        details.append("<td>" + CONTOUR_PERCENT_PARAM_NAME + "</td><td>" + (this.contourPercent + "") + "</td></tr><tr>");
//
//        // Add the contour start percent
//        details.append("<td>" + CONTOUR_START_PERCENT_PARAM_NAME + "</td><td>" + (this.contourStartPercent + "") + "</td></tr><tr>");
//
//        // Add the use in print view flag
//        details.append("<td>" + USE_PRINT_VIEW_PARAM_NAME + "</td><td>" + (this.usePrintViewP + "") + "</td></tr><tr>");
//
//        // Add the panel default flag
//        details.append("<td>" + PANEL_DEFAULT_PARAM_NAME + "</td><td>" + (this.panelDefaultP + "") + "</td></tr><tr>");
//
//        // Add the show details flag
//        details.append("<td>" + SHOW_DETAILS_PARAM_NAME + "</td><td>" + (this.showDetailsP + "") + "</td></tr><tr>");
//
//        // Add the keystone filename
//        details.append("<td>" + KEYSTONE_FILENAME_PARAM_NAME + "</td><td>" + (this.keystoneFilename) + "</td></tr><tr>");
//
//        // Add the layoutOverride
//        details.append("<td>" + LAYOUT_OVERRIDE_PARAM_NAME + "</td><td>" + (this.layoutOverride) + "</td></tr><tr>");
//
//        // Add the statistic
//        details.append("<td>" + STATISTIC_PARAM_NAME + "</td><td>" + (this.statistic) + "</td></tr><tr>");
//
//        // Add the equation
//        details.append("<td>" + EQUATION_PARAM_NAME + "</td><td>" + (this.equation) + "</td></tr><tr>");
//
//        // Add the formula
//        details.append("<td>" + FORMULA_PARAM_NAME + "</td><td>" + (this.formula) + "</td></tr><tr>");
//
//        // Add the control
//        details.append("<td>" + CONTROL_PARAM_NAME + "</td><td>" + (this.control) + "</td></tr><tr>");
//
//        // Add the control row
//        details.append("<td>" + CONTROL_ROW_PARAM_NAME + "</td><td>" + (this.controlRow) + "</td></tr><tr>");
//
//        // Add the controlColumn
//        details.append("<td>" + CONTROL_COLUMN_PARAM_NAME + "</td><td>" + (this.controlColumn) + "</td></tr><tr>");
//
//        // Add the range
//        details.append("<td>" + RANGE_PARAM_NAME + "</td><td>" + (this.range) + "</td></tr><tr>");
//
//        if (this.getRow() != null) {
//            // Add the row
//            details.append("<td>" + ROW_PARAM_NAME + "</td><td>" + (this.getRow().getID()) + "</td></tr><tr>");
//        }
//
//        if (this.getColumn() != null) {
//            // Add the column
//            details.append("<td>" + COLUMN_PARAM_NAME + "</td><td>" + (this.getColumn().getID()) + "</td></tr><tr>");
//        }
//
//        // Add the dimensions
//        // Get the array of illustration dimensions
//        IllustrationDimension[] dimensionsArray = getActiveDimensions();
//        // Loop through the array of illustrations dimensions encoding each
//        for (int i = 0; i < dimensionsArray.length; i++) {
//            details.append(dimensionsArray[i].getDetails());
//        }
//
//        if (this.creationQueryString != null && this.creationQueryString != DEFAULT_CREATION_QUERY_STRING) {
//            // Add the creation query string, if it exists
//            // Break up this string every 200 characters so it does not screw up
//            // the tables
//            details.append("<td>" + CREATION_QUERY_STRING_PARAM_NAME + "</td><td><font class=\"monospace\">" + JSPlib.addBreaks(this.creationQueryString, 80)
//                    + "</font></td></tr><tr>");
//        }
//
//        // Finish off the table
//        details.append("</tr></table>");
//
//        return details.toString();
//    }

    // end class
}