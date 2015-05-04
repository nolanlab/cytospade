/**
 * PopulationGrid.java
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

// Import the prefuse data package
import java.io.IOException;

//import prefuse.data.*;
//import prefuse.data.util.TableIterator;

// Import the membership package
//import facs.membership.*;

// Import the scale package
import facs.scale.*;

/**
 * <p>
 * A three-dimensional grid of population references. It is used to aggregate
 * populations in two dimensions.
 * </p>
 *
 * <p>
 * It may seem odd to the careful reader that I called the class a0
 * three-dimensional grid of population references when it is used to aggregate
 * populations in two dimensions. Well, that is simply because the intersection
 * used to find the population in each position of the two-dimensional grid may
 * not be unique. Rather than only take the first population in these cases, the
 * population grid maintains all of them in a series of layers.
 * </p>
 *
 * <p>
 * Please see the {@link facs.PopulationReference} class for more information.
 * </p>
 *
 * <p>
 * The population grid maintains the control of the grid internally. The
 * information is kept in three parts. The type of the control is indicated by
 * the control flag. The position of the control in the grid is indicated by the
 * indices of the control row and the control column. A valid control row and an
 * invalid control column (-1), for example, would indicate that the indicated
 * row should be used as the control for the population grid and vice versa. To
 * indicate a particular cell in the population grid is the control, the
 * appropriate cell based control type must be indicated by the control flag and
 * the control row and the control column should be set to the row and the
 * column of the cell, respectively.
 * </p>
 *
 * <p>
 * The population grid also maintains the statistic and the equation to use
 * internally. The idea is that this would avoid having to pass an extra piece
 * of information to indicate the type of statistic to return when the
 * population grid is passed as often in the case in OOP.
 * </p>
 *
 * <p>
 * Of course, the fact that the population grid maintains the layers throws a
 * huge wrinkle in the way of calculating the statistics. Since the only
 * completely valid layer in the grid of population references is the first, all
 * the statistics are based on that first layer. That means, the table minimum,
 * the table maximum, the row minimum, the row maximum, the column minimum, and
 * the column maximum are all calculated based on that first layer. It is a bit
 * odd, but it is better than splitting the layers into different population
 * grids like we have been doing prior to the inclusion of layers in the
 * population grid.
 * </p>
 *
 * <p>
 * As an additional speed up, the population grid also maintains the density
 * values of the population based on a canvas settings if the canvas settings of
 * the population grid is not null. This was introduced so that when the
 * population grid is used to calculate histogram overlays, the population would
 * not be retrieved twice, incurring the extra overhead costs. This breaks the
 * compartmentalization idea of OOP, but most speed ups do.
 * </p>
 */
public final class PopulationGrid {
    /**
     * Statistic constant flags --- The following flags are used to indicate the
     * type of statistic to calculate.
     */

    /**
     * No statistic specified
     */
    public static final int NO_STATISTIC = -1;

    /**
     * Calculate the mean
     */
    public static final int MEAN = 1;

    /**
     * Calculate the median
     */
    public static final int MEDIAN = 2;

    /**
     * Calculate the standard deviation
     */
    public static final int STANDARD_DEVIATION = 3;

    /**
     * Calculate the variance
     */
    public static final int VARIANCE = 4;

    /**
     * Calculate the minimum
     */
    public static final int MINIMUM = 5;

    /**
     * Calculate the maximum
     */
    public static final int MAXIMUM = 6;

    /**
     * Calculate the file index
     */
    public static final int FILE_INDEX = 7;

    /**
     * Calculate the percent
     */
    public static final int PERCENT = 8;

    /**
     * Calculate the event count
     */
    public static final int EVENT_COUNT = 9;

    /**
     * Calculate the channel range
     */
    public static final int CHANNEL_RANGE = 10;

    /**
     * Calculate the geometric mean
     */
    public static final int GEOMETRIC_MEAN = 11;

    /**
     * Calculate the 95th Percentile
     */
    public static final int NINETYFIFTH_PERCENTILE = 12;

    /**
     * Calculate the arcsinh median
     */
    public static final int ARCSINH_MEDIAN = 13;

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Equation constant flags
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * Equation constant flags --- The following flags are used to indicate the
     * type of equation to use on the statistics.
     *
     * To add a new equation, update: - PopulationGrid (add the constant flag
     * and code for the new equation and add the constant flag to the list of
     * valid equations) - illustrationControls.jsp - illustration.jsp
     *
     * Legacy: To add a new equation, one updated: - PopulationGrid (add the
     * constant flag and code for the new equation) - cytobank_viewLayout.jsp
     * (add controls) - layoutUI.js (update the "equation boundary testing") -
     * Layout.java (update setEquation function)
     *
     */

    /**
     * No equation specified
     */
    public static final int NO_EQUATION = -1;

    /**
     * The ratio equation - calculate the ratios
     */
    public static final int RATIO = 1;

    /**
     * The log ratio equation - calculate the log ratios
     */
    public static final int LOG_RATIO = 2;

    /**
     * The fold ratio equation
     */
    public static final int FOLD = 3;

    /**
     * The custom equation - perform the calculation in the formula of the
     * equation
     */
    public static final int CUSTOM = 4;

    /**
     * The log2 ratio equation - calculate the log ratios
     */
    public static final int LOG2_RATIO = 5;

    /**
     * The difference between the two numbers -- straight out subtraction
     */
    public static final int DIFFERENCE = 6;

    /**
     * A constant array of valid equation types
     */
    public static final int[] VALID_EQUATIONS = new int[] { RATIO, LOG_RATIO, FOLD, CUSTOM, LOG2_RATIO, DIFFERENCE };

    /**
     * ***********************************************************************
     * ***********************************************************************
     * Control constant flags
     *
     * ***********************************************************************
     * ***********************************************************************
     */

    /**
     * Control constant flags --- The following flags are used to indicate the
     * type of control to use when calculating ratios and log ratios.
     */

    /**
     * No control specified
     */
    public static final int NO_CONTROL = -1;

    /**
     * Use the cell in the table specified by (control row, control column)
     */
    public static final int BY_CELL = 0;

    /**
     * Use the row in the table specified as the control row
     */
    public static final int BY_ROW = 1;

    /**
     * Use the column in the table specified as the control column
     */
    public static final int BY_COLUMN = 2;

    /**
     * Use the table minimum as the control
     */
    public static final int TABLE_MIN = 3;

    /**
     * Use the table maximum as the control
     */
    public static final int TABLE_MAX = 4;

    /**
     * Use the row minimum as the control
     */
    public static final int ROW_MIN = 5;

    /**
     * Use the row maximum as the control
     */
    public static final int ROW_MAX = 6;

    /**
     * Use the column minimum as the control
     */
    public static final int COLUMN_MIN = 7;

    /**
     * Use the column maximum as the control
     */
    public static final int COLUMN_MAX = 8;

    /**
     * Use the first row as the control
     *
     * This is a hardcoded case of BY_ROW
     */
    public static final int ROW_1 = 9;

    /**
     * Use the first column as the control
     *
     * This is a hardcoded case of BY_COLUMN
     */
    public static final int COLUMN_1 = 10;

    /**
     * Use the first cell as the control
     *
     * This is a hardcoded case of BY_CELL
     */
    public static final int CELL_1_1 = 11;

    /**
     * A constant array of valid control types
     */
    public static final int[] VALID_CONTROLS = new int[] { BY_CELL, BY_ROW, BY_COLUMN, TABLE_MIN, TABLE_MAX, ROW_MIN, ROW_MAX, COLUMN_MIN, COLUMN_MAX, ROW_1,
            COLUMN_1, CELL_1_1 };

    /**
     * The column names of the prefuse table
     */

    /**
     * The name of the layer index column
     */
    private static final String LAYER_NAME = "Layer";

    /**
     * The name of the row index column
     */
    private static final String ROW_NAME = "Row";

    /**
     * The name of the column index column
     */
    private static final String COLUMN_NAME = "Column";

    /**
     * The name of the channel index column
     */
    private static final String CHANNEL_NAME = "Channel";

    /**
     * The name of the mean column
     */
    private static final String MEAN_NAME = "Mean";

    /**
     * The name of the median column
     */
    private static final String MEDIAN_NAME = "Median";

    /**
     * The name of the standard deviation column
     */
    private static final String STANDARD_DEVIATION_NAME = "SD";

    /**
     * The name of the variance column
     */
    private static final String VARIANCE_NAME = "Variance";

    /**
     * The name of the minimum column
     */
    private static final String MINIMUM_NAME = "Minimum";

    /**
     * The name of the maximum column
     */
    private static final String MAXIMUM_NAME = "Maximum";

    /**
     * The name of the percent column
     */
    private static final String PERCENT_NAME = "Percent";

    /**
     * The name of the event count column
     */
    private static final String EVENT_COUNT_NAME = "EventCount";

    /**
     * The name of the channel range column
     */
    private static final String CHANNEL_RANGE_NAME = "ChannelRange";

    /**
     * The name of the geometric mean column
     */
    private static final String GEOMETRIC_MEAN_NAME = "GeometricMean";

    /**
     * The name of the value derived column
     */
    private static final String VALUE_NAME = "Value";

    /**
     * The name of the 95th percentile column
     */
    private static final String NINETYFIFTH_PERCENTILE_NAME = "95th Percentile";

    /**
     * The name of the arcsinh median column
     */
    private static final String ARCSINH_MEDIAN_NAME = "ArcsinhMedian";

    /**
     * The default value for a null grid position
     */
    private static final double DEFAULT_VALUE = Double.NaN;

    /**
     * Instance variables
     */

    /**
     * The number of layers in the population grid
     */
    private final int layerCount;

    /**
     * The number of rows in the population grid
     */
    private final int rowCount;

    /**
     * The number of columns in the population grid
     */
    private final int columnCount;

    /**
     * The array of row labels
     */
    private String[] rowLabels;

    /**
     * The array of column labels
     */
    private String[] columnLabels;

    /**
     * The grid of populations (ok, population references)
     */
    private PopulationReference[][][] grid;

    /**
     * The constant flag of the statistic of the population grid
     */
    private int statistic;

    /**
     * The constant flag of the equation of the population grid
     */
    private int equation;

    /**
     * The population size cutoff; do not show populations with fewer events
     * than this number Values less than 1 are interpreted as "no cutoff"
     */
    private int populationCutoff;

    /**
     * The formula of the equation of the population grid
     */
    private String formula;

    /**
     * The constant flag of the control of the population grid
     */
    private int control;

    /**
     * The index of the control row of the population grid
     */
    private int controlRow;

    /**
     * The index of the control column of the population grid
     */
    private int controlColumn;

    /**
     * The boolean flag indicating whether the values of the population grid
     * need to be calculated
     */
    private boolean dirtyP;

    /**
     * The grid of values of the population grid
     */
    private double[][][] values;

    /**
     * The canvas settings of the population grid
     */
    private CanvasSettings cs;

    /**
     * The grid of triweight kernel density values of the population grid
     */
    private double[][][][] densities;

    /**
     * <p>
     * A protected full constructor for <code>PopulationGrid</code>.
     * </p>
     *
     * <p>
     * A population grid should only be created from its factory method.
     * </p>
     *
     * @param grid
     * array of arrays of <code>PopulationReference</code> arrays
     * containing the grid of population references.
     * @param rowLabels
     * <code>String</code> array of row labels.
     * @param columnLabels
     * <code>String</code> array of column labels.
     * @param layerCount
     * int number of layers in the grid of population references.
     * @param rowCount
     * int number of rows in the grid of population references.
     * @param columnCount
     * int number of columns in the grid of population references.
     */
    private PopulationGrid(PopulationReference[][][] grid, String[] rowLabels, String[] columnLabels, int layerCount, int rowCount, int columnCount) {
        // Set the number of layers to the number of layers in the grid of
        // population references
        this.layerCount = layerCount;

        // Set the number of rows to the number of rows in the grid of
        // population references
        this.rowCount = rowCount;

        // Set the number of columns to the number of columns in the grid of
        // population references
        this.columnCount = columnCount;

        /**
         * Copy the row labels
         */

        // Allocate the array of row labels
        this.rowLabels = new String[this.rowCount];

        if ((rowLabels == null) || (rowLabels.length <= 0)) {
            // If the array of row labels is null or empty, then set the row
            // labels to the empty string.
            for (int i = 0; i < this.rowCount; i++) {
                this.rowLabels[i] = "";
            }
        } else if (rowLabels.length < this.rowCount) {
            // If the length of the array of row labels is less than the number
            // of rows in the grid of population references, then copy the array
            // of row labels and set the remaining row labels to the empty
            // string.
            System.arraycopy(rowLabels, 0, this.rowLabels, 0, rowLabels.length);

            // Loop through the remaining rows
            for (int i = rowLabels.length; i < this.rowCount; i++) {
                this.rowLabels[i] = "";
            }
        } else {
            // Otherwise, the array of row labels is not null and its length is
            // greater than or equal to the number of rows in the grid of
            // population references, so copy the array of row labels.
            System.arraycopy(rowLabels, 0, this.rowLabels, 0, this.rowCount);
        }

        /**
         * Copy the column labels
         */

        // Allocate the array of column labels
        this.columnLabels = new String[this.columnCount];

        if ((columnLabels == null) || (columnLabels.length <= 0)) {
            // If the array of column labels is null or empty, then set the
            // column labels to the empty string.
            for (int i = 0; i < this.columnCount; i++) {
                this.columnLabels[i] = "";
            }
        } else if (columnLabels.length < this.columnCount) {
            // If the length of the array of column labels is less than the
            // number of columns in the grid of population references, then copy
            // the array of column labels and set the remaining column labels to
            // the empty string.
            System.arraycopy(columnLabels, 0, this.columnLabels, 0, columnLabels.length);

            // Loop through the remaining columns
            for (int i = columnLabels.length; i < this.columnCount; i++) {
                this.columnLabels[i] = "";
            }
        } else {
            // Otherwise, the array of column labels is not null and its length
            // is greater than or equal to the number of columns in the grid of
            // population references, so copy the array of column labels.
            System.arraycopy(columnLabels, 0, this.columnLabels, 0, this.columnCount);
        }

        /**
         * Create the grid of population references
         */

        // Allocate the grid of population references
        this.grid = new PopulationReference[this.layerCount][this.rowCount][this.columnCount];

        // Copy each element of the grid of population references
        for (int i = 0; i < this.layerCount; i++) {
            for (int j = 0; j < this.rowCount; j++) {
                for (int k = 0; k < this.columnCount; k++) {
                    this.grid[i][j][k] = grid[i][j][k];
                }
            }
        }

        // Initialize the constant flag of the statistic to NO_STATISTIC
        statistic = NO_STATISTIC;

        // Initialize the constant flag of the equation to NO_EQUATION
        equation = NO_EQUATION;

        // Initialize the population cutoff
        populationCutoff = -1;

        // Initialize the formula of the equation to null
        formula = null;

        // Initialize the constant flag of the control to NO_CONTROL
        control = NO_CONTROL;

        // Initialize the control row and the control column to -1
        controlRow = -1;
        controlColumn = -1;

        // Initialize whether the values of the population grid need to be
        // calculated to true
        dirtyP = true;

        // Initialize the values of the population grid to null
        values = null;

        // Initialize the canvas settings of the population grid to null
        cs = null;

        // Initialize the densities of the population grid to null
        densities = null;
    }

    /**
     * <p>
     * Returns the number of layers in the population grid.
     * </p>
     *
     * @return int number of layers in the population grid.
     */
    public int getLayerCount() {
        return layerCount;
    }

    /**
     * <p>
     * Returns the number of rows in the population grid.
     * </p>
     *
     * @return int number of rows in the population grid.
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * <p>
     * Returns the number of columns in the population grid.
     * </p>
     *
     * @return int number of columns in the population grid.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * <p>
     * Returns the array of row labels for the population grid.
     * </p>
     *
     * @return <code>String</code> array of row labels for the population grid.
     */
    public String[] getRowLabels() {
        // Allocate a copy of the array of row labels
        String[] labels = new String[rowLabels.length];

        // Copy the array of row labels
        System.arraycopy(rowLabels, 0, labels, 0, rowLabels.length);

        // Return the array of labels
        return labels;
    }

    /**
     * <p>
     * Returns the array of column labels for the population grid.
     * </p>
     *
     * @return <code>String</code> array of column labels for the population
     * grid.
     */
    public String[] getColumnLabels() {
        // Allocate a copy of the array of column labels
        String[] labels = new String[columnLabels.length];

        // Copy the array of column labels
        System.arraycopy(columnLabels, 0, labels, 0, columnLabels.length);

        // Return the array of labels
        return labels;
    }

    /**
     * <p>
     * Returns the grid of population references in the population grid.
     * </p>
     *
     * @return array of arrays of <code>PopulationReference</code> arrays
     * containing the grid of population references in the population
     * grid.
     */
    public PopulationReference[][][] getGrid() {
        return grid;
    }

    /**
     * <p>
     * Returns the layer of the grid of population references in the population
     * grid indicated by the layer index layer.
     * </p>
     *
     * @param layer
     * int index of the layer to return.
     * @return array of <code>PopulationReference</code> arrays containing the
     * layer of the grid of population references in the population grid
     * indicated by the layer index layer.
     */
    public PopulationReference[][] getLayer(int layer) {
        if ((layer < 0) || (layer >= layerCount)) {
            // If the index of the layer is invalid, then return an empty layer.
            return new PopulationReference[0][0];
        } else {
            // Otherwise, the index of the layer is valid, so return the layer
            // of the grid of population references.
            return grid[layer];
        }
    }

    /**
     * <p>
     * Returns the constant flag of the statistic of the population grid or -1
     * if it is not set.
     * </p>
     *
     * @return int constant flag of the statistic of the population grid or -1
     * if it is not set.
     */
    public int getStatistic() {
        return statistic;
    }

    /**
     * <p>
     * Sets the constant flag of the statistic of the population grid to
     * statistic.
     * </p>
     *
     * @param statistic
     * int constant flag of the statistic of the population grid.
     */
    public void setStatistic(int statistic) {
        if (statistic < -1) {
            // If the statistic is not valid, then quit.
            return;
        }

        // Set whether the values of the population grid need to be calculated
        // to true
        dirtyP = true;

        // Set the statistic based on the value of statistic
        switch (statistic) {
        case PopulationGrid.MEAN:
            this.statistic = PopulationGrid.MEAN;
            return;
        case PopulationGrid.MEDIAN:
            this.statistic = PopulationGrid.MEDIAN;
            return;
        case PopulationGrid.STANDARD_DEVIATION:
            this.statistic = PopulationGrid.STANDARD_DEVIATION;
            return;
        case PopulationGrid.VARIANCE:
            this.statistic = PopulationGrid.VARIANCE;
            return;
        case PopulationGrid.MINIMUM:
            this.statistic = PopulationGrid.MINIMUM;
            return;
        case PopulationGrid.MAXIMUM:
            this.statistic = PopulationGrid.MAXIMUM;
            return;
        case PopulationGrid.FILE_INDEX:
            this.statistic = PopulationGrid.FILE_INDEX;
            return;
        case PopulationGrid.PERCENT:
            this.statistic = PopulationGrid.PERCENT;
            return;
        case PopulationGrid.EVENT_COUNT:
            this.statistic = PopulationGrid.EVENT_COUNT;
            return;
        case PopulationGrid.CHANNEL_RANGE:
            this.statistic = PopulationGrid.CHANNEL_RANGE;
            return;
        case PopulationGrid.GEOMETRIC_MEAN:
            this.statistic = PopulationGrid.GEOMETRIC_MEAN;
            return;
        case PopulationGrid.NINETYFIFTH_PERCENTILE:
            this.statistic = PopulationGrid.NINETYFIFTH_PERCENTILE;
            return;
        case PopulationGrid.ARCSINH_MEDIAN:
            this.statistic = PopulationGrid.ARCSINH_MEDIAN;
            return;

            // Otherwise, set the statistic to NO_STATISTIC.
        default:
            this.statistic = PopulationGrid.NO_STATISTIC;
            return;
        }
    }

    /**
     * <p>
     * Returns the constant flag of the equation of the population grid or -1 if
     * it is not set.
     * </p>
     *
     * @return int constant flag of the equation of the population grid or -1 if
     * it is not set.
     */
    public int getEquation() {
        return equation;
    }

    /**
     * <p>
     * Sets the constant flag of the equation of the population grid to
     * equation.
     * </p>
     *
     * @param equation
     * int constant flag of the equation of the population grid.
     */
    public void setEquation(int equation) {
        if (equation < -1) {
            // If the equation is not valid, then quit.
            return;
        }

        // Set whether the values of the population grid need to be calculated
        // to true
        dirtyP = true;

        // Set the equation based on the value of equation
        switch (equation) {
        case PopulationGrid.RATIO:
            this.equation = PopulationGrid.RATIO;
            return;
        case PopulationGrid.DIFFERENCE:
            this.equation = PopulationGrid.DIFFERENCE;
            return;
        case PopulationGrid.LOG_RATIO:
            this.equation = PopulationGrid.LOG_RATIO;
            return;
        case PopulationGrid.LOG2_RATIO:
            this.equation = PopulationGrid.LOG2_RATIO;
            return;
        case PopulationGrid.FOLD:
            this.equation = PopulationGrid.FOLD;
            return;
        case PopulationGrid.CUSTOM:
            this.equation = PopulationGrid.CUSTOM;
            return;

            // Otherwise, set the equation to NO_EQUATION.
        default:
            this.equation = PopulationGrid.NO_EQUATION;
            return;
        }
    }

    /**
     * <p>
     * Returns the population cutoff of the population grid. Values less than 1
     * mean there is no population cutoff (use all the events).
     * </p>
     *
     * @return int population cutoff of the population grid
     */
    public int getPopulationCutoff() {
        return populationCutoff;
    }

    /**
     * <p>
     * Sets the population cutoff of the population grid.
     * </p>
     *
     * @param populationCutoff
     * <code>int</code> population cutoff of the population grid.
     */
    public void setPopulationCutoff(int populationCutoff) {
        this.populationCutoff = populationCutoff;
    }

    /**
     * <p>
     * Returns the formula of the equation of the population grid.
     * </p>
     *
     * @return <code>String</code> formula of the equation of the population
     * grid.
     */
    public String getFormula() {
        return formula;
    }

    /**
     * <p>
     * Sets the formula of the equation of the population grid to formula.
     * </p>
     *
     * @param formula
     * <code>String</code> formula of the equation of the population
     * grid.
     */
    public void setFormula(String formula) {
        this.formula = formula;

        // Set whether the values of the population grid need to be calculated
        // to true
        dirtyP = true;
    }

    /**
     * <p>
     * Returns the constant flag of the control of the population grid or -1 if
     * it is not set.
     * </p>
     *
     * @return int constant flag of the control of the population grid or -1 if
     * it is not set.
     */
    public int getControl() {
        return control;
    }

    /**
     * <p>
     * Sets the constant flag of the control of the population grid to control.
     * </p>
     *
     * @param control
     * int constant flag of the control of the population grid.
     */
    public void setControl(int control) {
        if (control < -1) {
            // If the control is not valid, then quit.
            return;
        }

        // Set whether the values of the population grid need to be calculated
        // to true
        dirtyP = true;

        // Set the control based on the value of control
        switch (control) {
        case BY_CELL:
            this.control = BY_CELL;
            return;
        case BY_ROW:
            this.control = BY_ROW;
            return;
        case BY_COLUMN:
            this.control = BY_COLUMN;
            return;
        case TABLE_MIN:
            this.control = TABLE_MIN;
            return;
        case TABLE_MAX:
            this.control = TABLE_MAX;
            return;
        case CELL_1_1:
            this.control = CELL_1_1;
            return;
        case ROW_MIN:
            this.control = ROW_MIN;
            return;
        case ROW_MAX:
            this.control = ROW_MAX;
            return;
        case ROW_1:
            this.control = ROW_1;
            return;
        case COLUMN_MIN:
            this.control = COLUMN_MIN;
            return;
        case COLUMN_MAX:
            this.control = COLUMN_MAX;
            return;
        case COLUMN_1:
            this.control = COLUMN_1;
            return;

            // Otherwise, set the control to NO_CONTROL.
        default:
            this.control = NO_CONTROL;
            controlRow = -1;
            controlColumn = -1;
            return;
        }
    }

    /**
     * <p>
     * Returns the index of the control row of the population grid or -1 if it
     * is not set.
     * </p>
     *
     * @return int index of the control row of the population grid or -1 if it
     * is not set.
     */
    public int getControlRow() {
        return controlRow;
    }

    /**
     * <p>
     * Sets the index of the control row of the population grid to row.
     * </p>
     *
     * <p>
     * Passing a row index equal to -1 would clear the control row.
     * </p>
     *
     * @param row
     * int index of the control row of the population grid.
     */
    public void setControlRow(int row) {
        if ((row >= -1) && (row < rowCount)) {
            // If the index of the row is valid, then set the control row.
            controlRow = row;

            // Set whether the values of the population grid need to be
            // calculated to true
            dirtyP = true;
        }
    }

    /**
     * <p>
     * Returns the index of the control column of the population grid or -1 if
     * it is not set.
     * </p>
     *
     * @return int index of the control column of the population grid or -1 if
     * it is not set.
     */
    public int getControlColumn() {
        return controlColumn;
    }

    /**
     * <p>
     * Sets the index of the control column of the population grid to column.
     * </p>
     *
     * <p>
     * Passing a column index equal to -1 would clear the control column.
     * </p>
     *
     * @param column
     * int index of the control column of the population grid.
     */
    public void setControlColumn(int column) {
        if ((column >= -1) && (column < columnCount)) {
            // If the index of the column is valid, then set the control column.
            controlColumn = column;

            // Set whether the values of the population grid need to be
            // calculated to true
            dirtyP = true;
        }
    }

    /**
     * <p>
     * Sets the control of the population grid to the minimum of the first layer
     * of the grid of values in the population grid if minP is true or to the
     * maximum of the first layer of the grid of values in the population grid
     * if minP is false.
     * </p>
     *
     * @param minP
     * boolean flag indicating whether to set the control to the
     * minimum of the grid of values in the population grid.
     * @throws IOException
     */
//    private void setControlHelper(boolean minP) throws IOException {
//        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
//            // If the number of layers, the number of rows, or the number of
//            // columns is less than or equal to 0, then quit.
//            return;
//        }
//
//        if (values == null) {
//            // If the grid of values is null, then calculate it.
//            calculateStatistics();
//        }
//
//        // Initialize the row and the column to the first element
//        int row = 0;
//        int column = 0;
//
//        // Loop through each element of the population grid looking for the
//        // appropriate extremum
//        for (int i = 0; i < rowCount; i++) {
//            for (int j = 0; j < columnCount; j++) {
//                if ((minP && (values[0][i][j] < values[0][row][column])) || (!minP && (values[0][i][j] > values[0][row][column]))) {
//                    // If we are looking for the minimum and the current value
//                    // is less than the current minimum or
//                    // if we are looking for the maximum and the current value
//                    // is greater than the current maximum, then update the
//                    // indices.
//                    row = i;
//                    column = j;
//                }
//            }
//        }
//
//        // Update the control row and control column to the appropriate extremum
//        setControlRow(row);
//        setControlColumn(column);
//    }

    /**
     * <p>
     * Sets the canvas settings of the population grid to the canvas settings in
     * the <code>CanvasSettings</code> object cs.
     * </p>
     *
     * @param cs
     * <code>CanvasSettings</code> object to the canvas settings.
     */
    public void setCanvasSettings(CanvasSettings cs) {
        this.cs = cs;
    }

    /**
     * Statistics methods
     */

    /**
     * <p>
     * Populates the grid of values of the population grid with the statistic
     * indicated by the constant flag of the statistic of the population grid
     * and populates the corresponding triweight kernel density values of the
     * population grid as well.
     * </p>
     *
     * <p>
     * To decide between whether to try to reuse populations when populating the
     * grid of values and the triweight kernel density values of the population
     * grid, this method first looks through the first row to see if any of the
     * population references there have the same population. If not, then this
     * method looks through the first column to see if any of the population
     * references there have the same population. If any of the population
     * references have the same population in either the first row or the first
     * column, then this method tries to reuse the population. Otherwise, this
     * method does not try to reuse the population. This seems like a reasonable
     * check because in the case where trying to reuse populations would
     * actually be beneficial, the rows or the columns should only vary in the
     * channel. In general, this would cause the first row or the first column
     * to have the same population due to the programmatic nature of layout
     * generation. This check takes quadratic time, but it is dominated by the
     * loop that populates the grid of values of the population grid, which
     * takes cubic time. It seems like a small price to pay for a potential
     * speed up.
     * </p>
     * @throws IOException
     */
//    private void calculateStatistics() throws IOException {
//        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
//            // If the number of layers, the number of rows, or the number of
//            // columns is less than or equal to 0, then create an empty grid of
//            // values.
//            values = new double[0][0][0];
//
//            if (cs != null) {
//                // If the canvas settings of the population grid is not null,
//                // then create an empty grid of triweight kernel density values.
//                densities = new double[0][0][0][0];
//            }
//
//            return;
//        }
//
//        // Allocate the grid of values
//        values = new double[layerCount][rowCount][columnCount];
//
//        if (cs != null) {
//            // If the canvas settings of the population grid is not null, then
//            // create the grid of triweight kernel density values.
//            densities = new double[layerCount][rowCount][columnCount][cs.getPlotWidth()];
//        }
//
//        // Allocate the mask indicating which values of the population grid have
//        // been set
//        boolean[][][] setMask = new boolean[layerCount][rowCount][columnCount];
//
//        // Loop through each element of the population grid
//        for (int i = 0; i < layerCount; i++) {
//            for (int j = 0; j < rowCount; j++) {
//                for (int k = 0; k < columnCount; k++) {
//                    // Initialize the current value to the default value
//                    values[i][j][k] = DEFAULT_VALUE;
//
//                    // Initialize the current element to whether the population
//                    // reference is null
//                    // This way, non-null population references will have the
//                    // mask set to false
//                    // and null population references will have the mask set to
//                    // true.
//                    setMask[i][j][k] = (grid[i][j][k] == null);
//                }
//            }
//        }
//
//        if (statistic == NO_STATISTIC) {
//            // If the statistic of the population grid is NO_STATISTIC, then
//            // quit.
//            return;
//        }
//
//        /**
//         * Decide whether to try to reuse populations
//         */
//
//        // Initialize whether to try to reuse populations to false
//        boolean reuseP = false;
//
//        // Loop through the first row
//        for (int i = 0; i < (columnCount - 1); i++) {
//            if (grid[0][0][i] != null) {
//                // If the outer population reference is not null, then check
//                // whether the same population exists in the first row.
//
//                // Loop through the remaining columns of the first row
//                for (int j = i + 1; j < columnCount; j++) {
//                    if (grid[0][0][i].isSamePopulation(grid[0][0][j])) {
//                        // If the inner population reference references the same
//                        // population as the outer population, then set whether
//                        // to try to reuse populations to true.
//                        reuseP = true;
//
//                        break;
//                    }
//                }
//
//                if (reuseP) {
//                    // If whether to try to reuse populations is true, then
//                    // break.
//                    break;
//                }
//            }
//        }
//
//        if (!reuseP) {
//            // If whether to try to reuse populations is false, then check
//            // whether the same population exists in the first column.
//
//            // Loop through the first row
//            for (int i = 0; i < (rowCount - 1); i++) {
//                if (grid[0][i][0] != null) {
//                    // If the outer population reference is not null, then check
//                    // whether the same population exists in the first column.
//
//                    // Loop through the remaining rows of the first column
//                    for (int j = i + 1; j < rowCount; j++) {
//                        if (grid[0][i][0].isSamePopulation(grid[0][j][0])) {
//                            // If the inner population reference references the
//                            // same population as the outer population, then set
//                            // whether to try to reuse populations to true.
//                            reuseP = true;
//
//                            break;
//                        }
//                    }
//
//                    if (reuseP) {
//                        // If whether to try to reuse populations is true, then
//                        // break.
//                        break;
//                    }
//                }
//            }
//        }
//
//        /**
//         * Populate the grid of values with the appropriate statistic
//         */
//        Population pop;
//        int channel, type;
//        Scale scale;
//        ScaleArgument scaleArgument;
//        double min, max;
//        fcsFile file;
//        double[] channelDensities;
//
//        // Loop through each element of the population grid
//        for (int i = 0; i < layerCount; i++) {
//            for (int j = 0; j < rowCount; j++) {
//                for (int k = 0; k < columnCount; k++) {
//                    if (statistic == FILE_INDEX) {
//                        // If the statistic of the population grid is
//                        // FILE_INDEX, then get the index of the row of the
//                        // population.
//                        values[i][j][k] = (double) j;
//                    }
//
//                    if (setMask[i][j][k]) {
//                        // If the value of the current population reference is
//                        // set, then skip it.
//                        // If whether to try to reuse populations is true, then
//                        // the value of the current population reference is set,
//                        // so skip it.
//                        // Otherwise, whether to try to reuse populations is
//                        // false, so the current population reference is null
//                        // (see initialization above), so skip it.
//                        continue;
//                    }
//
//                    if (grid[i][j][k] != null) {
//                        // If the current population reference is not null, then
//                        // get the population referenced by it.
//                        pop = grid[i][j][k].getPopulation();
//
//                        // Get the channel of the current population reference
//                        channel = grid[i][j][k].getChannel();
//
//                        if (pop != null && ((this.getPopulationCutoff() < 1) || (pop.getEventCount() > this.getPopulationCutoff()))) {
//                            // If the population referenced by the current
//                            // population reference is not null AND
//                            // If the population cutoff is less than one OR the
//                            // population's event count is greater than the
//                            // cutoff
//                            // then get the statistic
//                            if (statistic == PERCENT) {
//                                // If the statistic of the population grid is
//                                // PERCENT, then get the percent of the
//                                // population.
//                                values[i][j][k] = pop.getPercent();
//                            } else if (statistic == EVENT_COUNT) {
//                                // If the statistic of the population grid is
//                                // EVENT_COUNT, then get the number of events in
//                                // the population.
//                                values[i][j][k] = pop.getEventCount();
//                            } else if (statistic == MEAN) {
//                                // If the statistic of the population grid is
//                                // MEAN, then get the mean of the channel.
//                                values[i][j][k] = pop.getMean(channel);
//                            } else if (statistic == MEDIAN) {
//                                // If the statistic of the population grid is
//                                // MEDIAN, then get the median of the channel.
//                                values[i][j][k] = pop.getMedian(channel);
//                            } else if (statistic == STANDARD_DEVIATION) {
//                                // If the statistic of the population grid is
//                                // STANDARD_DEVIATION, then get the standard
//                                // deviation of the channel.
//                                values[i][j][k] = pop.getStandardDeviation(channel);
//                            } else if (statistic == VARIANCE) {
//                                // If the statistic of the population grid is
//                                // VARIANCE, then get the variance of the
//                                // channel.
//                                values[i][j][k] = pop.getVariance(channel);
//                            } else if (statistic == MINIMUM) {
//                                // If the statistic of the population grid is
//                                // MINIMUM, then get the minimum of the channel.
//                                values[i][j][k] = pop.getMinimum(channel);
//                            } else if (statistic == MAXIMUM) {
//                                // If the statistic of the population grid is
//                                // MAXIMUM, then get the maximum of the channel.
//                                values[i][j][k] = pop.getMaximum(channel);
//                            } else if (statistic == CHANNEL_RANGE) {
//                                // If the statistic of the population grid is
//                                // CHANNEL_RANGE, then get the channel range of
//                                // the channel.
//                                values[i][j][k] = pop.getChannelRange(channel);
//                            } else if (statistic == GEOMETRIC_MEAN) {
//                                // If the statistic of the population grid is
//                                // GEOMETRIC_MEAN, then get the geometric mean
//                                // of the channel.
//                                values[i][j][k] = pop.getGeometricMean(channel);
//                            } else if (statistic == NINETYFIFTH_PERCENTILE) {
//                                // If the statistic of the population grid is
//                                // 95th percentile then get the 95th percentile
//                                // of the channel.
//                                values[i][j][k] = pop.getNthPercentile(channel, 95);
//                            } else if (statistic == ARCSINH_MEDIAN) {
//                                // If the statistic of the population grid is
//                                // arcsinh median then get the arcsinh median of
//                                // the channel.
//
//                                // Define a scale argument string for this
//                                // channel
//                                String scaleArgString = "";
//
//                                // Try to get a scale argument from the canvas
//                                // settings
//                                // If this doesn't work, we'll just let
//                                // getArcsinhMedian worry about it
//
//                                if (cs != null) {
//                                    scaleArgument = cs.getScaleArgument(channel);
//
//                                    if (scaleArgument instanceof ArcsinhScaleArgument) {
//                                        // If the scale argument is an arcsinh
//                                        // scale argument,
//                                        // then cast it to an arcsinh scale
//                                        // argument and get the compression
//                                        // width
//                                        int compressionWidth = ((ArcsinhScaleArgument) scaleArgument).getCompressionWidth();
//
//                                        scaleArgString = compressionWidth + "";
//                                    }
//                                }
//
//                                // Get the arcsinh median and put it in the
//                                // values array
//                                values[i][j][k] = pop.getArcsinhMedian(channel, scaleArgString);
//                            }
//
//                            /**
//                             * Get the triweight kernel density values as
//                             * necessary
//                             */
//
//                            if ((cs != null) && (channel > -1)) {
//                                // If the canvas settings of the population grid
//                                // is not null and the channel is valid, then
//                                // get the triweight kernel density values.
//
//                                /**
//                                 * Get the scale information
//                                 */
//
//                                // Get the scale of the channel
//                                scale = cs.getScale(channel);
//
//                                // Get the scale argument of the channel
//                                scaleArgument = cs.getScaleArgument(channel);
//
//                                // Get the channel minimum
//                                min = cs.getMinimum(channel);
//
//                                // Get the channel maximum
//                                max = cs.getMaximum(channel);
//
//                                if (scale == null) {
//                                    // If the scale of the channel is still
//                                    // null, then get the default scale of the
//                                    // channel.
//
//                                    // Get the flow file underlying the
//                                    // population
//                                    file = pop.getFlowFile();
//
//                                    // Get the scale type flag of the default
//                                    // scale of the channel
//                                    type = ScaleDefaults.getDefaultScaleFlag(file.isLog(channel), pop.isLogDisplay(channel));
//
//                                    // Get the default scale of the channel
//                                    scale = Scaling.getScale(type);
//
//                                    // Get the default scale argument of the
//                                    // channel
//                                    scaleArgument = ScaleDefaults.getDefaultScaleArgument(type);
//
//                                    // Get the default channel minimum
//                                    min = ScaleDefaults.getDefaultChannelMinimum(type);
//
//                                    // Get the channel range to use as the
//                                    // default channel maximum
//                                    max = pop.getChannelRange(channel);
//
//                                    // Set the flow file to null
//                                    file = null;
//                                }
//
//                                // Get the triweight kernel density values
//                                channelDensities = pop.getTriweightKernelDensityValues(channel, scale, scaleArgument, cs.getPlotWidth(), min, max, 1.0d);
//
//                                if (channelDensities.length == densities[i][j][k].length) {
//                                    // If the length of the triweight kernel
//                                    // density values is equal to the densities,
//                                    // the copy it.
//                                    System.arraycopy(channelDensities, 0, densities[i][j][k], 0, channelDensities.length);
//                                }
//                            }
//
//                            // Set whether the value of the current population
//                            // reference is set to true
//                            setMask[i][j][k] = true;
//
//                            if (reuseP) {
//                                // If whether to try to reuse populations is
//                                // true, then try to reuse the population
//                                // referenced by the current population
//                                // reference.
//                                reusePopulation(grid[i][j][k], pop, setMask);
//                            }
//                        }
//
//                        /**
//                         * Set the population to null --- Since we no longer
//                         * need the population, but the population is still in
//                         * scope, we set the reference to null here in the hopes
//                         * that the garbage collector would collect the
//                         * population as it takes up quite a bit of the heap.
//                         */
//                        pop = null;
//                    }
//                }
//            }
//        }
//    }

    /**
     * <p>
     * Loops through the each element of population grid, setting the value of
     * each population reference that references the same population as the
     * population reference in the <code>PopulationReference</code> object ref.
     * The population referenced by the population reference should be passed in
     * the <code>Population</code> object pop. Failure to do so may lead to
     * incorrect results.
     * </p>
     *
     * <p>
     * This method modifies the instance variable values that contains the grid
     * of values, so it should be properly allocated before this method is
     * called.
     * </p>
     *
     * <p>
     * This is an attempt to try to maximize the use of populations created
     * while populating the grid of values and minimize the creation of
     * populations.
     * </p>
     *
     * <p>
     * This method is really a failed attempt because the strategy depends on
     * the population references in the population grid referencing the same
     * population. This case rarely occurs as far as I know. The basic case when
     * such a scenario occurs is when the channels are either the rows or the
     * columns of the population grid. Looking at the layouts, this is often not
     * the case. Since the algorithm loops through the grid of population
     * references looking for population references that reference the same
     * population, this makes a normally cubic algorithm in getting the
     * statistics a sextic (to the sixth degree?) algorithm by introducing a
     * cubic search algorithm in another cubic algorithm. Thus, in the end, I
     * think this strategy really adds more overhead than any computation it
     * saves.
     * </p>
     *
     * @param ref
     * <code>PopulationReference</code> object to the population
     * reference.
     * @param pop
     * <code>Population</code> object to the population referenced by
     * the population reference.
     * @param setMask
     * array of arrays of boolean arrays indicating whether the
     * corresponding value in the grid of values has already been
     * set.
     * @throws IOException
     */
//    private void reusePopulation(PopulationReference ref, Population pop, boolean[][][] setMask) throws IOException {
//        if ((ref == null) || (pop == null) || (setMask == null) || (setMask.length != layerCount) || (setMask[0].length != rowCount)
//                || (setMask[0][0].length != columnCount)) {
//            // If the population reference, the population, or the mask is null,
//            // or the size of the mask is wrong, then quit.
//            return;
//        }
//
//        /**
//         * Loop through the grid of population references looking for population
//         * references that reference the same population as the input population
//         * reference
//         */
//        int channel, type;
//        Scale scale;
//        ScaleArgument scaleArgument;
//        double min, max;
//        fcsFile file;
//        double[] channelDensities;
//
//        // Loop through each element of the population grid
//        for (int i = 0; i < layerCount; i++) {
//            for (int j = 0; j < rowCount; j++) {
//                for (int k = 0; k < columnCount; k++) {
//                    if ((grid[i][j][k] == null) || setMask[i][j][k] || (!ref.isSamePopulation(grid[i][j][k]))) {
//                        // If the current population reference is null, its
//                        // value is set, or it does not reference the same
//                        // population as the population reference, then skip it.
//                        continue;
//                    }
//
//                    // Get the channel of the current population reference
//                    channel = grid[i][j][k].getChannel();
//
//                    if (statistic == PERCENT) {
//                        // If the statistic of the population grid is PERCENT,
//                        // then get the percent of the population.
//                        values[i][j][k] = pop.getPercent();
//                    } else if (statistic == EVENT_COUNT) {
//                        // If the statistic of the population grid is
//                        // EVENT_COUNT, then get the number of events in the
//                        // population.
//                        values[i][j][k] = pop.getEventCount();
//                    } else if (statistic == MEAN) {
//                        // If the statistic of the population grid is MEAN, then
//                        // get the mean of the channel.
//                        values[i][j][k] = pop.getMean(channel);
//                    } else if (statistic == MEDIAN) {
//                        // If the statistic of the population grid is MEDIAN,
//                        // then get the median of the channel.
//                        values[i][j][k] = pop.getMedian(channel);
//                    } else if (statistic == STANDARD_DEVIATION) {
//                        // If the statistic of the population grid is
//                        // STANDARD_DEVIATION, then get the standard deviation
//                        // of the channel.
//                        values[i][j][k] = pop.getStandardDeviation(channel);
//                    } else if (statistic == VARIANCE) {
//                        // If the statistic of the population grid is VARIANCE,
//                        // then get the variance of the channel.
//                        values[i][j][k] = pop.getVariance(channel);
//                    } else if (statistic == MINIMUM) {
//                        // If the statistic of the population grid is MINIMUM,
//                        // then get the minimum of the channel.
//                        values[i][j][k] = pop.getMinimum(channel);
//                    } else if (statistic == MAXIMUM) {
//                        // If the statistic of the population grid is MAXIMUM,
//                        // then get the maximum of the channel.
//                        values[i][j][k] = pop.getMaximum(channel);
//                    } else if (statistic == CHANNEL_RANGE) {
//                        // If the statistic of the population grid is
//                        // CHANNEL_RANGE, then get the channel range of the
//                        // channel.
//                        values[i][j][k] = pop.getChannelRange(channel);
//                    } else if (statistic == GEOMETRIC_MEAN) {
//                        // If the statistic of the population grid is
//                        // GEOMETRIC_MEAN, then get the geometric mean of the
//                        // channel.
//                        values[i][j][k] = pop.getGeometricMean(channel);
//                    } else if (statistic == NINETYFIFTH_PERCENTILE) {
//                        // If the statistc of the population grid is
//                        // NINETYFIFTH_PERCENTILE, then get the 95th percentile
//                        // of the channel.
//                        values[i][j][k] = pop.getNthPercentile(channel, 95);
//                    } else if (statistic == ARCSINH_MEDIAN) {
//                        // If the statistic of the population grid is arcsinh
//                        // median then get the arcsinh median of the channel.
//
//                        // Define a scale argument string for this channel
//                        String scaleArgString = "";
//
//                        // Try to get a scale argument from the canvas settings
//                        // If this doesn't work, we'll just let getArcsinhMedian
//                        // worry about it
//
//                        if (cs != null) {
//                            scaleArgument = cs.getScaleArgument(channel);
//
//                            if (scaleArgument instanceof ArcsinhScaleArgument) {
//                                // If the scale argument is an arcsinh scale
//                                // argument,
//                                // then cast it to an arcsinh scale argument and
//                                // get the compression width
//                                int compressionWidth = ((ArcsinhScaleArgument) scaleArgument).getCompressionWidth();
//
//                                scaleArgString = compressionWidth + "";
//                            }
//                        }
//
//                        // Get the arcsinh median and put it in the values array
//                        values[i][j][k] = pop.getArcsinhMedian(channel, scaleArgString);
//                    }
//
//                    /**
//                     * Get the triweight kernel density values as necessary
//                     */
//
//                    if ((cs != null) && (channel > -1)) {
//                        // If the canvas settings of the population grid is not
//                        // null and the channel is valid, then get the triweight
//                        // kernel density values.
//
//                        /**
//                         * Get the scale information
//                         */
//
//                        // Get the scale of the channel
//                        scale = cs.getScale(channel);
//
//                        // Get the scale argument of the channel
//                        scaleArgument = cs.getScaleArgument(channel);
//
//                        // Get the channel minimum
//                        min = cs.getMinimum(channel);
//
//                        // Get the channel maximum
//                        max = cs.getMaximum(channel);
//
//                        if (scale == null) {
//                            // If the scale of the channel is still null, then
//                            // get the default scale of the channel.
//
//                            // Get the flow file underlying the population
//                            file = pop.getFlowFile();
//
//                            // Get the scale type flag of the default scale of
//                            // the channel
//                            type = ScaleDefaults.getDefaultScaleFlag(file.isLog(channel), pop.isLogDisplay(channel));
//
//                            // Get the default scale of the channel
//                            scale = Scaling.getScale(type);
//
//                            // Get the default scale argument of the channel
//                            scaleArgument = ScaleDefaults.getDefaultScaleArgument(type);
//
//                            // Get the default channel minimum
//                            min = ScaleDefaults.getDefaultChannelMinimum(type);
//
//                            // Get the channel range to use as the default
//                            // channel maximum
//                            max = pop.getChannelRange(channel);
//                        }
//
//                        // Get the triweight kernel density values
//                        channelDensities = pop.getTriweightKernelDensityValues(channel, scale, scaleArgument, cs.getPlotWidth(), min, max, 1.0d);
//
//                        if (channelDensities.length == densities[i][j][k].length) {
//                            // If the length of the triweight kernel density
//                            // values is equal to the densities, the copy it.
//                            System.arraycopy(channelDensities, 0, densities[i][j][k], 0, channelDensities.length);
//                        }
//                    }
//
//                    // Set whether the value of the current population reference
//                    // is set to true
//                    setMask[i][j][k] = true;
//                }
//            }
//        }
//    }

    /**
     * <p>
     * Populates the grid of values of the population grid with the value of the
     * custom equation in the formula of the equation using a prefuse table.
     * </p>
     * @throws IOException
     */
//    private void calculateCustomEquation() throws IOException {
//        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
//            // If the number of layers, the number of rows, or the number of
//            // columns is less than or equal to 0, then create an empty grid of
//            // values.
//            values = new double[0][0][0];
//
//            if (cs != null) {
//                // If the canvas settings of the population grid is not null,
//                // then create an empty grid of triweight kernel density values.
//                densities = new double[0][0][0][0];
//            }
//
//            return;
//        }
//
//        // Allocate the grid of values
//        values = new double[layerCount][rowCount][columnCount];
//
//        if (cs != null) {
//            // If the canvas settings of the population grid is not null, then
//            // create the grid of triweight kernel density values.
//            densities = new double[layerCount][rowCount][columnCount][cs.getPlotWidth()];
//        }
//
//        if ((formula == null) || (formula.length() <= 0)) {
//            // If the formula of the equation is null or empty, then create a
//            // default grid of values.
//
//            // Loop through each element of the population grid
//            for (int i = 0; i < layerCount; i++) {
//                for (int j = 0; j < rowCount; j++) {
//                    for (int k = 0; k < columnCount; k++) {
//                        // Initialize the current value to the default value
//                        values[i][j][k] = DEFAULT_VALUE;
//                    }
//                }
//            }
//
//            return;
//        }
//
//        /**
//         * Create the prefuse table
//         */
//        Table table = new Table();
//
//        // Add the layer index column
//        table.addColumn(LAYER_NAME, int.class, -1);
//
//        // Add the row index column
//        table.addColumn(ROW_NAME, int.class, -1);
//
//        // Add the column index column
//        table.addColumn(COLUMN_NAME, int.class, -1);
//
//        // Add the channel index column
//        table.addColumn(CHANNEL_NAME, int.class, -1);
//
//        // Add the mean column
//        table.addColumn(MEAN_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the median column
//        table.addColumn(MEDIAN_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the standard deviation column
//        table.addColumn(STANDARD_DEVIATION_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the variance column
//        table.addColumn(VARIANCE_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the minimum column
//        table.addColumn(MINIMUM_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the maximum column
//        table.addColumn(MAXIMUM_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the percent column
//        table.addColumn(PERCENT_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the event count column
//        table.addColumn(EVENT_COUNT_NAME, int.class, 0);
//
//        // Add the channel range column
//        table.addColumn(CHANNEL_RANGE_NAME, double.class, DEFAULT_VALUE);
//
//        // Add the geometric mean column
//        // table.addColumn(GEOMETRIC_MEAN_NAME, double.class, DEFAULT_VALUE);
//
//        int row, channel, type;
//        Population pop;
//        fcsFile file;
//        Scale scale;
//        ScaleArgument scaleArgument;
//        double min, max;
//        double[] channelDensities;
//
//        // Loop through each element of the population grid
//        for (int i = 0; i < layerCount; i++) {
//            for (int j = 0; j < rowCount; j++) {
//                for (int k = 0; k < columnCount; k++) {
//                    // Add a row to the table and get the row number
//                    row = table.addRow();
//
//                    // Set the layer index of the current row to the current
//                    // layer index
//                    table.set(row, LAYER_NAME, i);
//
//                    // Set the row index of the current row to the current row
//                    // index
//                    table.set(row, ROW_NAME, j);
//
//                    // Set the column index of the current row to the current
//                    // column index
//                    table.set(row, COLUMN_NAME, k);
//
//                    if (grid[i][j][k] != null) {
//                        // If the population reference at (i, j, k) is not null,
//                        // then get its population.
//
//                        // Get the population at (i, j, k)
//                        pop = grid[i][j][k].getPopulation();
//
//                        // Get the channel of the population reference at (i, j,
//                        // k)
//                        channel = grid[i][j][k].getChannel();
//
//                        // Set the channel index of the current row to the
//                        // channel of the population reference at (i, j, k)
//                        table.set(row, CHANNEL_NAME, channel);
//
//                        if (pop != null && ((this.getPopulationCutoff() < 1) || (pop.getEventCount() > this.getPopulationCutoff()))) {
//                            // If the population at (i, j, k) is not null AND
//                            // If the population cutoff is less than one OR the
//                            // population's event count is greater than the
//                            // cutoff
//                            // then set the columns of the current row of the
//                            // prefuse table.
//
//                            // Set the mean of the current row to the mean of
//                            // the channel of the population
//                            table.set(row, MEAN_NAME, pop.getMean(channel));
//
//                            // Set the median of the current row to the median
//                            // of the channel of the population
//                            table.set(row, MEDIAN_NAME, pop.getMedian(channel));
//
//                            // Set the standard deviation of the current row to
//                            // the standard deviation of the channel of the
//                            // population
//                            table.set(row, STANDARD_DEVIATION_NAME, pop.getStandardDeviation(channel));
//
//                            // Set the variance of the current row to the
//                            // variance of the channel of the population
//                            table.set(row, VARIANCE_NAME, pop.getVariance(channel));
//
//                            // Set the minimum of the current row to the minimum
//                            // of the channel of the population
//                            table.set(row, MINIMUM_NAME, pop.getMinimum(channel));
//
//                            // Set the maximum of the current row to the maximum
//                            // of the channel of the population
//                            table.set(row, MAXIMUM_NAME, pop.getMaximum(channel));
//
//                            // Set the percent of the current row to the percent
//                            // of the population
//                            table.set(row, PERCENT_NAME, pop.getPercent());
//
//                            // Set the event count of the current row to the
//                            // event count of the population
//                            table.set(row, EVENT_COUNT_NAME, pop.getEventCount());
//
//                            // Set the channel range of the current row to the
//                            // range of the channel of the population
//                            table.set(row, CHANNEL_RANGE_NAME, pop.getChannelRange(channel));
//
//                            // Set the geometric mean of the current row to the
//                            // geometric mean of the channel of the population
//                            // table.set(row, GEOMETRIC_MEAN_NAME,
//                            // pop.getGeometricMean(channel));
//
//                            /**
//                             * Get the triweight kernel density values as
//                             * necessary
//                             */
//
//                            if ((cs != null) && (channel > -1)) {
//                                // If the canvas settings of the population grid
//                                // is not null and the channel is valid, then
//                                // get the triweight kernel density values.
//
//                                /**
//                                 * Get the scale information
//                                 */
//
//                                // Get the scale of the channel
//                                scale = cs.getScale(channel);
//
//                                // Get the scale argument of the channel
//                                scaleArgument = cs.getScaleArgument(channel);
//
//                                // Get the channel minimum
//                                min = cs.getMinimum(channel);
//
//                                // Get the channel maximum
//                                max = cs.getMaximum(channel);
//
//                                if (scale == null) {
//                                    // If the scale of the channel is still
//                                    // null, then get the default scale of the
//                                    // channel.
//
//                                    // Get the flow file underlying the
//                                    // population
//                                    file = pop.getFlowFile();
//
//                                    // Get the scale type flag of the default
//                                    // scale of the channel
//                                    type = ScaleDefaults.getDefaultScaleFlag(file.isLog(channel), pop.isLogDisplay(channel));
//
//                                    // Get the default scale of the channel
//                                    scale = Scaling.getScale(type);
//
//                                    // Get the default scale argument of the
//                                    // channel
//                                    scaleArgument = ScaleDefaults.getDefaultScaleArgument(type);
//
//                                    // Get the default channel minimum
//                                    min = ScaleDefaults.getDefaultChannelMinimum(type);
//
//                                    // Get the channel range to use as the
//                                    // default channel maximum
//                                    max = pop.getChannelRange(channel);
//                                }
//
//                                // Get the triweight kernel density values
//                                channelDensities = pop.getTriweightKernelDensityValues(channel, scale, scaleArgument, cs.getPlotWidth(), min, max, 1.0d);
//
//                                if (channelDensities.length == densities[i][j][k].length) {
//                                    // If the length of the triweight kernel
//                                    // density values is equal to the densities,
//                                    // the copy it.
//                                    System.arraycopy(channelDensities, 0, densities[i][j][k], 0, channelDensities.length);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Add a derived column using the formula of the equation
//        table.addColumn(VALUE_NAME, formula);
//
//        /**
//         * Populate the grid of values
//         */
//
//        // Get the iterator for the prefuse table
//        TableIterator iter = table.iterator();
//
//        // Loop through each element of the population grid
//        for (int i = 0; i < layerCount; i++) {
//            for (int j = 0; j < rowCount; j++) {
//                for (int k = 0; k < columnCount; k++) {
//                    // Initialize the current value to the default value
//                    values[i][j][k] = DEFAULT_VALUE;
//
//                    if (iter.hasNext()) {
//                        // If the iterator for the prefuse table has a next
//                        // element, then get the number of the next row.
//                        row = iter.nextInt();
//
//                        // Get the value of the derived column of the next row
//                        values[i][j][k] = table.getDouble(row, VALUE_NAME);
//                    }
//                }
//            }
//        }
//    }

    /**
     * Ratios methods --- The ratios methods calculate the three types of
     * ratios: the ratio, the log ratio, and the fold ratio. The ratio of two
     * numbers is simply the first divided by the second. The log ratio is the
     * log base 10 of the ratio. The fold ratio is different from the regular
     * ratio in that the smaller of the two numbers in the ratio is always in
     * the denominator.
     *
     * I had a long debate over whether to make these methods static or leave
     * them as normal methods. After doing them the static way, I realized that
     * there is much to be gained from making the methods normal methods. Among
     * them is the fact that the number of rows and the number of columns are
     * quite useful. With static methods, it was necessary to detect the size of
     * the grid each time, which amounted to half the work (the loop is the bulk
     * of the work, but once I've written it - and it's quite straight-forward -
     * the bulk of the work is for the CPU and not myself whereas I actually
     * have to do the detection work, in terms of figuring out the logic and
     * what not). Writing these helper methods as normal methods saved quite a
     * bit on the detection work.
     *
     * Update: Ratios also now calculates the log2 ratio and the difference. So
     * it's not really that well named any more, but it seemed silly to create a
     * new function that did nearly the same thing just to do the difference.
     */

    /**
     * <p>
     * Returns the grid of ratios of the grid of values divided by the value in
     * the double control.
     * </p>
     *
     * <p>
     * This takes care of the cases where the control is BY_CELL, TABLE_MIN, or
     * TABLE_MAX, since they are all based on a single cell in the grid of
     * values.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * <p>
     * The ratios can be calculated in-situ here since the control is a single
     * value that we can store beforehand; however, we do not calculate the
     * ratios in-situ.
     * </p>
     *
     * @param control
     * double value by which to divide all the values in the grid of
     * values.
     * @return array of arrays of double arrays containing the grid of ratios.
     */
    private double[][][] getRatios(double control) {
        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
            // If the number of layers, the number of rows, or the number of
            // columns is less than or equal to 0, then return an empty grid of
            // ratios.
            return new double[0][0][0];
        }

        // Allocate the grid of ratios
        double[][][] ratios = new double[layerCount][rowCount][columnCount];

        // Loop through the layers
        for (int i = 0; i < layerCount; i++) {
            // Loop through the rows
            for (int j = 0; j < rowCount; j++) {
                // Loop through the columns
                for (int k = 0; k < columnCount; k++) {
                    if (equation == FOLD) {
                        // If the equation is FOLD, then calculate the fold
                        // ratio - 1.
                        // This is so that we do not have a color issue because
                        // there is a
                        // gap in the scale from -1 to 1; we just subtract off
                        // the one and
                        // the scale pivots around zero.
                        if (control > values[i][j][k]) {
                            // If the control is greater than the current value,
                            // then calculate the inverted ratio + 1.
                            ratios[i][j][k] = (-control / values[i][j][k]) + 1;
                        } else {
                            // Otherwise, the control is less than or equal to
                            // the current value, so calculate the ratio - 1.
                            ratios[i][j][k] = (values[i][j][k] / control) - 1;
                        }
                    } else if (equation == DIFFERENCE) {
                        // If the equation is DIFFERENCE, then take the
                        // difference
                        ratios[i][j][k] = values[i][j][k] - control;
                    } else {
                        // Otherwise, calculate the ratio.
                        ratios[i][j][k] = values[i][j][k] / control;
                    }

                    if (equation == LOG_RATIO) {
                        // If the equation is LOG_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log10(ratios[i][j][k]);
                    }
                    if (equation == LOG2_RATIO) {
                        // If the equation is LOG2_RATIO, then take the log base
                        // 2 of the ratio.
                        ratios[i][j][k] = Math.log(ratios[i][j][k]) / Math.log(2);
                    }
                }
            }
        }

        // Return the grid of ratios
        return ratios;
    }

    /**
     * <p>
     * Returns the grid of ratios of the grid of values divided by the value in
     * the cell (row, column) in the first layer.
     * </p>
     *
     * <p>
     * This takes care of the cases where the control is BY_CELL, TABLE_MIN, or
     * TABLE_MAX, since they are all based on a single cell in the grid of
     * values.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * <p>
     * The ratios can be calculated in-situ here since the control is a single
     * value that we can store beforehand; however, we do not calculate the
     * ratios in-situ.
     * </p>
     *
     * @param row
     * int row index of the control cell.
     * @param column
     * int column index of the control cell.
     * @return array of arrays of double arrays containing the grid of ratios.
     */
    private double[][][] getRatios(int row, int column) {
        if ((layerCount <= 0) || (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount)) {
            // If the number of layers is less than or equal to 0 or the row
            // index or the column index is not valid, then return an empty grid
            // of ratios.
            return new double[0][0][0];
        } else {
            // Otherwise, divide the grid of values by the control, which is the
            // value in the cell (row, column) in the first layer.
            return getRatios(values[0][row][column]);
        }
    }

    /**
     * <p>
     * Returns the grid of ratios of the grid of values divided by the row
     * indicated by the index index if rowP is true or the column indicated by
     * the index index if rowP is false.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * <p>
     * The ratios cannot be calculated in-situ here since we need the original
     * grid of values to find the controls.
     * </p>
     *
     * @param index
     * int index of the row if rowP is true or index of the column if
     * rowP is false.
     * @param rowP
     * boolean flag indicating whether index should be interpreted as
     * a row index.
     * @return array of arrays of double arrays containing the grid of ratios.
     */
    private double[][][] getRatios(int index, boolean rowP) {
        if (layerCount <= 0) {
            // If the number of layers is less than or equal to 0, then return
            // an empty grid of ratios.
            return new double[0][0][0];
        }

        if (rowP) {
            // If we are calculating the ratios by row, then check if the index
            // is a valid row index.
            if ((index < 0) || (index >= rowCount)) {
                // If the index is not a valid row index, then return an empty
                // grid of ratios.
                return new double[0][0][0];
            }
        } else {
            // Otherwise, we are calculating the ratios by column, so check if
            // the index is a valid column index.
            if ((index < 0) || (index >= columnCount)) {
                // If the index is not a valid column index, then return an
                // empty grid of ratios.
                return new double[0][0][0];
            }
        }

        // Allocate the grid of ratios
        double[][][] ratios = new double[layerCount][rowCount][columnCount];

        // Loop through the layers
        for (int i = 0; i < layerCount; i++) {
            // Loop through the rows
            for (int j = 0; j < rowCount; j++) {
                // Loop through the columns
                for (int k = 0; k < columnCount; k++) {
                    if (rowP) {
                        // If we are calculating the ratios by row, then the
                        // index is the row index.
                        if (equation == FOLD) {
                            // If the equation is FOLD, then calculate the fold
                            // ratio - 1.
                            // This is so that we do not have a color issue
                            // because there is a
                            // gap in the scale from -1 to 1; we just subtract
                            // off the one and
                            // the scale pivots around zero.
                            if (values[0][index][k] > values[i][j][k]) {
                                // If the control is greater than the current
                                // value, then calculate the inverted ratio + 1.
                                ratios[i][j][k] = (-values[0][index][k] / values[i][j][k]) + 1;
                            } else {
                                // Otherwise, the control is less than or equal
                                // to the current value, so calculate the ratio
                                // - 1.
                                ratios[i][j][k] = (values[i][j][k] / values[0][index][k]) - 1;
                            }
                        } else if (equation == DIFFERENCE) {
                            // Calculate the difference
                            ratios[i][j][k] = values[i][j][k] - values[0][index][k];
                        } else {
                            // Otherwise, calculate the ratio.
                            ratios[i][j][k] = values[i][j][k] / values[0][index][k];
                        }
                    } else {
                        // Otherwise, we are calculating the ratios by column,
                        // so the index is the column index.
                        if (equation == FOLD) {
                            // If the equation is FOLD, then calculate the fold
                            // ratio - 1.
                            // This is so that we do not have a color issue
                            // because there is a
                            // gap in the scale from -1 to 1; we just subtract
                            // off the one and
                            // the scale pivots around zero.
                            if (values[0][j][index] > values[i][j][k]) {
                                // If the control is greater than the current
                                // value, then calculate the inverted ratio + 1.
                                ratios[i][j][k] = (-values[0][j][index] / values[i][j][k]) + 1;
                            } else {
                                // Otherwise, the control is less than or equal
                                // to the current value, so calculate the ratio
                                // - 1.
                                ratios[i][j][k] = (values[i][j][k] / values[0][j][index]) - 1;
                            }
                        } else if (equation == DIFFERENCE) {
                            ratios[i][j][k] = values[i][j][k] - values[0][j][index];
                        } else {
                            // Otherwise, calculate the ratio.
                            ratios[i][j][k] = values[i][j][k] / values[0][j][index];
                        }
                    }

                    if (equation == LOG_RATIO) {
                        // If the equation is LOG_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log10(ratios[i][j][k]);
                    }
                    if (equation == LOG2_RATIO) {
                        // If the equation is LOG2_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log(ratios[i][j][k]) / Math.log(2);
                    }
                }
            }
        }

        // Return the grid of ratios
        return ratios;
    }

    /**
     * <p>
     * Returns the grid of ratios of the grid of values divided by the row
     * minimum if minP is true or the row maximum if minP is false.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * <p>
     * The ratios cannot be calculated in-situ here since we need the original
     * grid of values to find the row extremums.
     * </p>
     *
     * @param minP
     * boolean flag indicating whether to divide by the row minimum.
     * @return array of arrays of double arrays containing the grid of ratios.
     */
    private double[][][] getRatiosByRowExtremum(boolean minP) {
        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
            // If the number of layers, the number of rows, or the number of
            // columns is less than or equal to 0, then return an empty grid of
            // ratios.
            return new double[0][0][0];
        }

        // Allocate the grid of ratios
        double[][][] ratios = new double[layerCount][rowCount][columnCount];

        double control;

        // Loop through the rows
        for (int j = 0; j < rowCount; j++) {
            // Initialize the control to 1.0d
            control = 1.0d;

            if (columnCount > 0) {
                // If the population grid has columns, then find the appropriate
                // row extremum.

                if (minP) {
                    // If the control is the row minimum, then get the minimum
                    // of the current row.
                    control = PopulationGrid.getMin(values[0][j]);
                } else {
                    // Otherwise, the control is the row maximum, so get the
                    // maximum of the current row.
                    control = PopulationGrid.getMax(values[0][j]);
                }
            }

            // Loop through the layers
            for (int i = 0; i < layerCount; i++) {
                // Loop through the columns
                for (int k = 0; k < columnCount; k++) {
                    if (equation == FOLD) {
                        // If the equation is FOLD, then calculate the fold
                        // ratio - 1.
                        if (control > values[i][j][k]) {
                            // If the control is greater than the current value,
                            // then calculate the inverted ratio + 1.
                            ratios[i][j][k] = (-control / values[i][j][k]) + 1;
                        } else {
                            // Otherwise, the control is less than or equal to
                            // the current value, so calculate the ratio - 1.
                            ratios[i][j][k] = (values[i][j][k] / control) - 1;
                        }
                    } else if (equation == DIFFERENCE) {
                        ratios[i][j][k] = values[i][j][k] - control;
                    } else {
                        // Otherwise, calculate the ratio.
                        ratios[i][j][k] = values[i][j][k] / control;
                    }

                    if (equation == LOG_RATIO) {
                        // If the equation is LOG_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log10(ratios[i][j][k]);
                    }
                    if (equation == LOG2_RATIO) {
                        // If the equation is LOG2_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log(ratios[i][j][k]) / Math.log(2);
                    }
                }
            }
        }

        // Return the grid of ratios
        return ratios;
    }

    /**
     * <p>
     * Returns the grid of ratios of the grid of values divided by the column
     * minimum if minP is true or the column maximum if minP is false.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * <p>
     * The ratios cannot be calculated in-situ here since we need the original
     * grid of values to find the column extremums.
     * </p>
     *
     * @param minP
     * boolean flag indicating whether to divide by the column
     * minimum.
     * @return array of arrays of double arrays containing the grid of ratios.
     */
    private double[][][] getRatiosByColumnExtremum(boolean minP) {
        if ((layerCount <= 0) || (rowCount <= 0) || (columnCount <= 0)) {
            // If the number of layers, the number of rows, or the number of
            // columns is less than or equal to 0, then return an empty grid of
            // ratios.
            return new double[0][0][0];
        }

        // Allocate the grid of ratios
        double[][][] ratios = new double[layerCount][rowCount][columnCount];

        // Allocate an array to hold the values in a column
        double[] columnValues = new double[rowCount];

        double control;

        // Loop through the columns
        for (int k = 0; k < columnCount; k++) {
            // Initialize the control to 1.0d
            control = 1.0d;

            if (rowCount > 0) {
                // If the population grid has rows, then find the appropriate
                // column extremum.

                // Loop through the rows populating the array of values in the
                // current column
                for (int j = 0; j < rowCount; j++) {
                    columnValues[j] = values[0][j][k];
                }

                if (minP) {
                    // If the control is the column minimum, then get the
                    // minimum of the current column.
                    control = PopulationGrid.getMin(columnValues);
                } else {
                    // Otherwise, the control is the column maximum, so get the
                    // maximum of the current column.
                    control = PopulationGrid.getMax(columnValues);
                }
            }

            // Loop through the layers
            for (int i = 0; i < layerCount; i++) {
                // Loop through the rows
                for (int j = 0; j < rowCount; j++) {
                    if (equation == FOLD) {
                        // If the equation is FOLD, then calculate the fold
                        // ratio - 1.
                        if (control > values[i][j][k]) {
                            // If the control is greater than the current value,
                            // then calculate the inverted ratio + 1.
                            ratios[i][j][k] = (-control / values[i][j][k]) + 1;
                        } else {
                            // Otherwise, the control is less than or equal to
                            // the current value, so calculate the ratio - 1.
                            ratios[i][j][k] = (values[i][j][k] / control) - 1;
                        }
                    } else if (equation == DIFFERENCE) {
                        ratios[i][j][k] = values[i][j][k] - control;
                    } else {
                        // Otherwise, calculate the ratio.
                        ratios[i][j][k] = values[i][j][k] / control;
                    }

                    if (equation == LOG_RATIO) {
                        // If the equation is LOG_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log10(ratios[i][j][k]);
                    }
                    if (equation == LOG2_RATIO) {
                        // If the equation is LOG2_RATIO, then take the log base
                        // 10 of the ratio.
                        ratios[i][j][k] = Math.log(ratios[i][j][k]) / Math.log(2);
                    }
                }
            }
        }

        // Return the grid of ratios
        return ratios;
    }

    /**
     * <p>
     * Returns the grid of ratios. The ratios are the values of the population
     * grid divided by the control of the population grid.
     * </p>
     *
     * <p>
     * The ratios are log ratios if the equation is LOG_RATIO or LOG2_RATIO.
     * </p>
     *
     * @return array of arrays of double arrays containing the grid of ratios.
     * @throws IOException
     */
//    private double[][][] getRatios() throws IOException {
//        // Calculate the ratios based on the value of control
//        switch (control) {
//        case BY_CELL:
//            return getRatios(controlRow, controlColumn);
//        case BY_ROW:
//            return getRatios(controlRow, true);
//
//            // Use row 1
//        case ROW_1:
//            return getRatios(0, true);
//        case BY_COLUMN:
//            return getRatios(controlColumn, false);
//
//            // Use column 1
//        case COLUMN_1:
//            return getRatios(0, false);
//
//            // Use the first row and column, i.e. cell 1,1
//        case CELL_1_1:
//            setControlHelper(false);
//            return getRatios(0, 0);
//
//            // Set the control to the table minimum
//        case TABLE_MIN:
//            setControlHelper(true);
//            return getRatios(controlRow, controlColumn);
//
//            // Set the control to the table maximum
//        case TABLE_MAX:
//            setControlHelper(false);
//            return getRatios(controlRow, controlColumn);
//
//        case ROW_MIN:
//            return getRatiosByRowExtremum(true);
//        case ROW_MAX:
//            return getRatiosByRowExtremum(false);
//        case COLUMN_MIN:
//            return getRatiosByColumnExtremum(true);
//        case COLUMN_MAX:
//            return getRatiosByColumnExtremum(false);
//
//            // Otherwise, return an empty grid of ratios.
//        default:
//            return new double[0][0][0];
//        }
//    }

    /**
     * <p>
     * Returns the grid of values of the population grid based on the statistic,
     * the equation, the formula, and the control of the population grid.
     * </p>
     *
     * <p>
     * If the equation is RATIO, LOG_RATIO, or LOG2_RATIO, DIFFERENCE or FOLD,
     * but the control of population grid is not valid, then the values of the
     * population grid are simply the statistics of the population grid.
     * </p>
     *
     * <p>
     * The values are not calculated until this method is called.
     * </p>
     *
     * @return array of arrays of double arrays containing the grid of values of
     * the population grid based on the statistic, the equation, the
     * formula, and the control of the population grid.
     * @throws IOException
     */
//    public double[][][] getValues() throws IOException {
//        if (!dirtyP) {
//            // If the values of the population grid do not need to be
//            // calculated, then return the grid of values.
//            return values;
//        }
//
//        // Otherwise, the values of the population grid need to be calculated,
//        // so calculate them.
//
//        if (equation == CUSTOM) {
//            // If the equation is CUSTOM, then calculate the custom equation.
//            calculateCustomEquation();
//        } else {
//            // Otherwise, the equation is not the custom equation, so calculate
//            // the statistics.
//            calculateStatistics();
//
//            if (equation == NO_EQUATION) {
//                // If the equation is NO_EQUATION, then do nothing since the
//                // grid of values already have the statistics.
//            } else if ((equation == DIFFERENCE) || (equation == RATIO) || (equation == LOG_RATIO) || (equation == LOG2_RATIO) || (equation == FOLD)) {
//                // If the equation is DIFFERENCE, RATIO, LOG_RATIO, LOG2_RATIO,
//                // or FOLD, then set the grid of values to the appropriate
//                // ratios of the grid of values.
//                if (control > NO_CONTROL) {
//                    // If the control of the population grid is valid, then set
//                    // the grid of values to the appropriate ratios of the grid
//                    // of values.
//                    values = getRatios();
//                }
//            }
//
//            // If the equation is not recognized, then the grid of values is
//            // simply the statistics.
//        }
//
//        // Set whether the values of the population grid need to be calculated
//        // to false
//        dirtyP = false;
//
//        // Return the grid of values
//        return values;
//    }

    /**
     * <p>
     * Returns the layer of the grid of values of the population grid indicated
     * by the layer index layer.
     * </p>
     *
     * @param layer
     * int index of the layer to return.
     * @return array of double arrays containing the grid of values of the
     * population grid based on the statistic, the equation, the
     * formula, and the control of the population grid.
     * @throws IOException
     */
//    public double[][] getValues(int layer) throws IOException {
//        if ((layer < 0) || (layer >= layerCount)) {
//            // If the index of the layer is invalid, then return an empty layer.
//            return new double[0][0];
//        } else {
//            // Otherwise, the index of the layer is valid, so return the layer
//            // of the grid of values.
//            if (dirtyP) {
//                // If the values of the population grid need to be calculated,
//                // then calculate the grid of values.
//                getValues();
//            }
//
//            // Return the layer of the grid of values
//            return values[layer];
//        }
//    }

    /**
     * <p>
     * Returns the value of the population in the population grid at (row,
     * column) of the layer indicated by the layer index.
     * </p>
     *
     * @param layer
     * int index of the layer of the population.
     * @param row
     * int index of the row of the population.
     * @param column
     * int index of the column of the population.
     * @return double value of the population in the populationg grid at (row,
     * column) of the layer indicated by the layer index.
     * @throws IOException
     */
//    public double getValue(int layer, int row, int column) throws IOException {
//        if ((layer < 0) || (layer >= layerCount) || (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount)) {
//            // If the index of the layer, row, or column is invalid, then return
//            // Double.NaN.
//            return Double.NaN;
//        } else {
//            // Otherwise, the indices are valid, so return the value.
//            if (dirtyP) {
//                // If the values of the population grid need to be calculated,
//                // then calculate the grid of values.
//                getValues();
//            }
//
//            // Return the value of the population
//            return values[layer][row][column];
//        }
//    }

    /**
     * <p>
     * Returns the population reference of the population in the population grid
     * at (row, column) of the layer indicated by the layer index.
     * </p>
     *
     * @param layer
     * int index of the layer of the population.
     * @param row
     * int index of the row of the population.
     * @param column
     * int index of the column of the population.
     * @return double value of the population in the populationg grid at (row,
     * column) of the layer indicated by the layer index.
     */
    public PopulationReference getPopulationReference(int layer, int row, int column) {
        if ((layer < 0) || (layer >= layerCount) || (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount) || (grid == null)) {
            // If the index of the layer, row, or column is invalid or the grid
            // of populations is null, then quit.
            return null;
        } else {
            // Otherwise, the indices are valid and the grid of populations is
            // not null, so return the population reference.
            return grid[layer][row][column];
        }
    }

    /**
     * <p>
     * Returns the triweight kernel density values of the population in the
     * population grid at (row, column) of the layer indicated by the layer
     * index.
     * </p>
     *
     * @param layer
     * int index of the layer of the population.
     * @param row
     * int index of the row of the population.
     * @param column
     * int index of the column of the population.
     * @return double array of triweight kernel density values of the population
     * in the population grid at (row, column) of the layer indicated by
     * the layer index.
     */
    public double[] getDensities(int layer, int row, int column) {
        if ((layer < 0) || (layer >= layerCount) || (row < 0) || (row >= rowCount) || (column < 0) || (column >= columnCount) || (cs == null)
                || (densities == null)) {
            // If the index of the layer, row, or column is invalid or the
            // canvas settings of the population grid or the grid of triweight
            // kernel density values of the population grid is null, then return
            // an empty array of triweight kernel density values.
            return new double[0];
        } else {
            // Otherwise, the indices are valid and the canvas settings of the
            // population grid and the grid of triweight kernel density values
            // of the population grid are not null, so return the array of
            // triweight kernel density values.
            return densities[layer][row][column];
        }
    }

    /**
     * <p>
     * Returns the minimum value of the population grid.
     * </p>
     *
     * @return double minimum value of the population grid.
     * @throws IOException
     */
//    public double getMinimum() throws IOException {
//        if (dirtyP) {
//            // If the values of the population grid need to be calculated, then
//            // calculate the grid of values.
//            getValues();
//        }
//
//        // Return the minimum value of the population grid
//        return PopulationGrid.getMin(values);
//    }

    /**
     * <p>
     * Returns the maximum value of the population grid.
     * </p>
     *
     * @return double maximum value of the population grid.
     * @throws IOException
     */
//    public double getMaximum() throws IOException {
//        if (dirtyP) {
//            // If the values of the population grid need to be calculated, then
//            // calculate the grid of values.
//            getValues();
//        }
//
//        // Return the maximum value of the population grid
//        return PopulationGrid.getMax(values);
//    }

    /**
     * <p>
     * Returns the value of the inflection point of the population grid or
     * Double.NaN if the population grid does not have an inflection point.
     * </p>
     *
     * @return double value of the inflection point of the population grid or
     * Double.NaN if the population grid does not have an inflection
     * point.
     */
    public double getInflection() {
        if (isBidirectional()) {
            // If the range of the population grid is bidirectional, then figure
            // out the value of the inflection point.

            // Initialize the value of the inflection point to 1.0d
            double inflection = 0.0d;

            if ((equation == LOG_RATIO) || (equation == LOG2_RATIO)) {
                // If the equation is LOG_RATIO or LOG2_RATIO, then set the
                // value of the inflection point to 0.0d.
                inflection = 0.0d;
            } else if ((equation == FOLD) || (equation == RATIO)) {
                // If the equation is FOLD, then set the value of the inflection
                // point to 0.0d.
                // This is because we will use the FOLD - 1 so that we don't get
                // weird color artifacts
                // The other solution would be to put a gap in the scale between
                // -1 and 1
                inflection = 0.0d;
            }

            // Return the value of the inflection point
            return inflection;
        } else {
            // Otherwise, the range of the population grid is unidirectional, so
            // return Double.NaN.
            return Double.NaN;
        }
    }

    /**
     * <p>
     * Returns whether the range of the population grid is unidirectional.
     * </p>
     *
     * @return boolean flag indicating whether the range of the population grid
     * is unidirectional.
     */
    public boolean isUnidirectional() {
        return (!isBidirectional());
    }

    /**
     * <p>
     * Returns whether the range of the population grid is bidirectional.
     * </p>
     *
     * @return boolean flag indicating whether the range of the population grid
     * is bidirectional.
     */
    public boolean isBidirectional() {
        if ((equation == DIFFERENCE) || (equation == RATIO) || (equation == LOG_RATIO) || (equation == LOG2_RATIO) || (equation == FOLD)) {
            // If the equation is DIFFERENCE, RATIO, LOG_RATIO, LOG2_RATIO, or
            // FOLD, then the range of the population grid is bidirectional if
            // the control is BY_CELL, BY_ROW, or BY_COLUMN.
            if ((control == BY_CELL) || (control == BY_ROW) || (control == BY_COLUMN) || (control == ROW_1) || (control == COLUMN_1) || (control == CELL_1_1)) {
                // If the control is BY_CELL, BY_ROW, or BY_COLUMN, or a
                // hardcoded version of one of these, then the range of the
                // population grid is bidirectional.
                return true;
            } else {
                // Otherwise, the range of the population is not bidirectional.
                return false;
            }
        } else {
            // Otherwise, the range of the population is not bidirectional.
            return false;
        }
    }

    /**
     * <p>
     * Returns whether the range of the population grid is minimum
     * unidirectional.
     * </p>
     *
     * @return boolean flag indicating whether the range of the population grid
     * is minimum unidirectional.
     */
    public boolean isMinimum() {
        if (isBidirectional()) {
            // If the range of the population grid is bidirectional, then return
            // false.
            return false;
        } else {
            // Otherwise, the range of the population grid is unidirectional, so
            // the range of the population grid is minimum unidirectional if it
            // is not maximum unidirectional.
            return (!isMaximum());
        }
    }

    /**
     * <p>
     * Returns whether the range of the population grid is maximum
     * unidirectional.
     * </p>
     *
     * @return boolean flag indicating whether the range of the population grid
     * is maximum unidirectional.
     */
    public boolean isMaximum() {
        if (isBidirectional()) {
            // If the range of the population grid is bidirectional, then return
            // false.
            return false;
        } else {
            // Otherwise, the range of the population grid is unidirectional, so
            // check if the range of the population grid is maximum
            // unidirectional.
            if ((equation == DIFFERENCE) || (equation == RATIO) || (equation == LOG_RATIO) || (equation == LOG2_RATIO) || (equation == FOLD)) {
                // If the equation is DIFFERENCE, RATIO, LOG_RATIO, LOG2_RATIO,
                // or FOLD, then the range of the population grid is maximum
                // unidirectional if the control is TABLE_MAX, ROW_MAX, or
                // COLUMN_MAX.
                if ((control == TABLE_MAX) || (control == ROW_MAX) || (control == COLUMN_MAX)) {
                    // If the control is TABLE_MAX, ROW_MAX, or COLUMN_MAX, then
                    // the range of the population grid is maximum
                    // unidirectional.
                    return true;
                } else {
                    // Otherwise, the range of the population is not maximum
                    // unidirectional.
                    return false;
                }
            } else {
                // Otherwise, the range of the population is not maximum
                // unidirectional.
                return false;
            }
        }
    }

    /**
     * <p>
     * Returns the range of the population grid if it is unidirectional, or the
     * positive range of the population grid if it is bidirectional.
     * </p>
     *
     * @return double range of the population grid if the range is
     * unidirectional, or the positive range of the population grid if
     * the range is bidirectional.
     * @throws IOException
     */
//    public double getRange() throws IOException {
//        // Get the minimum value of the population grid
//        double minimum = getMinimum();
//
//        // Get the value of the inflection point of the population grid
//        double inflection = getInflection();
//
//        // Get the maximum value of the population grid
//        double maximum = getMaximum();
//
//        if (isBidirectional()) {
//            // If the range of the population grid is bidirectional, then figure
//            // out the value of the inflection point.
//
//            // Calculate the difference between the maximum of the population
//            // grid and the value of the inflection point
//            double toMax = Math.abs(maximum - inflection);
//
//            // Calculate the difference between the minimum of the population
//            // grid and the value of the inflection point
//            double toMin = Math.abs(minimum - inflection);
//
//            if (toMax > toMin) {
//                // If the difference between the maximum of the population grid
//                // and the value of the inflection point is greater than the
//                // difference between the minimum of the population grid and the
//                // value of the inflection point, then return the difference
//                // between the maximum of the population grid and the value of
//                // the inflection point.
//                return toMax;
//            } else {
//                // Otherwise, the difference between the maximum of the
//                // population grid and the value of the inflection point is less
//                // than or equal to the difference between the minimum of the
//                // population grid and the value of the inflection point, so
//                // return the difference between the minimum of the population
//                // grid and the value of the inflection point.
//                return toMin;
//            }
//        } else {
//            // Otherwise, the range of the population grid is unidirectional, so
//            // return the difference between the maximum and the minimum of the
//            // population grid.
//            return (maximum - minimum);
//        }
//    }

    /**
     * Factory methods
     */

    /**
     * <p>
     * Returns a population grid created by filtering the population table in
     * the <code>PopulationTable</code> object table with the row membership
     * sets in the <code>MembershipSet</code> array of membership sets rows and
     * the column membership sets in the <code>MembershipSet</code> array of
     * membership sets columns.
     * </p>
     *
     * <p>
     * There was an implementation decision over what to return if the
     * population table is null. Theoretically, the population grid can cope
     * with such a case, but it seemed more useful to return null. The same is
     * true if the number of layers in the population grid is equal to 0. Thus,
     * for both of these cases, the factory method returns null.
     * </p>
     *
     * @param table
     * <code>PopulationTable</code> object to the population table
     * containing the populations.
     * @param rows
     * <code>MembershipSet</code> array of row membership sets.
     * @param columns
     * <code>MembershipSet</code> array of column membership sets.
     * @return <code>PopulationGrid</code> object to the population grid created
     * by filtering the population table in the
     * <code>PopulationTable</code> object table with the row membership
     * sets in the <code>MembershipSet</code> array of membership sets
     * rows and the column membership sets in the
     * <code>MembershipSet</code> array of membership sets columns.
     */
//    public static PopulationGrid getPopulationGrid(PopulationTable table, MembershipSet[] rows, MembershipSet[] columns) {
//        return getPopulationGrid(table, rows, columns, -1, 0);
//    }

    /**
     * <p>
     * Returns a population grid created by filtering the population table in
     * the <code>PopulationTable</code> object table with the row membership
     * sets in the <code>MembershipSet</code> array of membership sets rows and
     * the column membership sets in the <code>MembershipSet</code> array of
     * membership sets columns.
     * </p>
     *
     * <p>
     * There was an implementation decision over what to return if the
     * population table is null. Theoretically, the population grid can cope
     * with such a case, but it seemed more useful to return null. The same is
     * true if the number of layers in the population grid is equal to 0. Thus,
     * for both of these cases, the factory method returns null.
     * </p>
     *
     * @param table
     * <code>PopulationTable</code> object to the population table
     * containing the populations.
     * @param rows
     * <code>MembershipSet</code> array of row membership sets.
     * @param columns
     * <code>MembershipSet</code> array of column membership sets.
     * @param type
     * int constant flag of the type of the population.
     * @param numEvents
     * int number of events to get from the flow file.
     * @return <code>PopulationGrid</code> object to the population grid created
     * by filtering the population table in the
     * <code>PopulationTable</code> object table with the row membership
     * sets in the <code>MembershipSet</code> array of membership sets
     * rows and the column membership sets in the
     * <code>MembershipSet</code> array of membership sets columns.
     */
//    public static PopulationGrid getPopulationGrid(PopulationTable table, MembershipSet[] rows, MembershipSet[] columns, int type, int numEvents) {
//        if (table == null) {
//            // If the population table is null, then quit.
//            return null;
//        }
//
//        /**
//         * Figure out the number of layers, the number of rows, and the number
//         * of columns
//         */
//
//        // Get the number of layers in the population grid
//        int layerCount = PopulationGrid.getLayerCount(table, rows, columns);
//
//        if (layerCount <= 0) {
//            // If the number of layers is equal to 0, then quit.
//            return null;
//        }
//
//        // Initialize the number of rows to 1
//        int rowCount = 1;
//
//        String[] rowLabels;
//
//        if ((rows == null) || (rows.length <= 0)) {
//            // If the array of row membership sets is null or empty, then create
//            // a default array of row labels.
//            rowLabels = new String[1];
//            rowLabels[0] = "";
//        } else {
//            // Otherwise, the array of row membership sets is not null and not
//            // empty, so set the number of rows to its length.
//            rowCount = rows.length;
//
//            // Allocate the array of row labels
//            rowLabels = new String[rowCount];
//
//            // Loop through the rows
//            for (int i = 0; i < rowCount; i++) {
//                rowLabels[i] = rows[i].getName();
//            }
//        }
//
//        // Initialize the number of columns to 1
//        int columnCount = 1;
//
//        String[] columnLabels;
//
//        if ((columns == null) || (columns.length <= 0)) {
//            // If the array of column membership sets is null or empty, then
//            // create a default array of column labels.
//            columnLabels = new String[1];
//            columnLabels[0] = "";
//        } else {
//            // Otherwise, the array of column membership sets is not null and
//            // not empty, so set the number of columns to its length.
//            columnCount = columns.length;
//
//            // Allocate the array of column labels
//            columnLabels = new String[columnCount];
//
//            // Loop through the columns
//            for (int i = 0; i < columnCount; i++) {
//                columnLabels[i] = columns[i].getName();
//            }
//        }
//
//        /**
//         * Create the grid of population references
//         */
//
//        // Allocate the grid of population references
//        PopulationReference[][][] grid = new PopulationReference[layerCount][rowCount][columnCount];
//
//        if (((rows == null) || (rows.length <= 0)) && ((columns == null) || (columns.length <= 0))) {
//            // If the array of row membership sets is null or empty and the
//            // array of column membership sets is null or empty, then populate
//            // the grid of population references with the populations in the
//            // population table.
//
//            // Get the array of population references in the population table
//            PopulationReference[] pops = table.getPopulations(type, numEvents);
//
//            // Loop through the layers
//            for (int layer = 0; layer < layerCount; layer++) {
//                if (layer < pops.length) {
//                    // If the index of the current layer is a valid index in the
//                    // array of population references, then set the population
//                    // reference to the corresponding population reference.
//                    grid[layer][0][0] = pops[layer];
//                } else {
//                    // Otherwise, the index of the current layer is not a valid
//                    // index in the array of population references, so set the
//                    // population reference to null.
//                    grid[layer][0][0] = null;
//                }
//            }
//        } else if ((rows == null) || (rows.length <= 0)) {
//            // If the array of row membership sets is null or empty, then
//            // populate the grid of population references with the populations
//            // in the population table after filtering with the array of column
//            // membership sets.
//            // The condition should not be negated (or switched for the negation
//            // of the column version) since that would catch the case where both
//            // the row and the column membership sets are valid.
//
//            PopulationTable columnTable;
//            PopulationReference[] pops;
//
//            // Loop through the array of column membership sets
//            for (int k = 0; k < columns.length; k++) {
//                // Get the column table
//                columnTable = table.filterTable(columns[k]);
//
//                // Get the array of population references in the column table
//                pops = columnTable.getPopulations(type, numEvents);
//
//                // Loop through the layers
//                for (int layer = 0; layer < layerCount; layer++) {
//                    if (layer < pops.length) {
//                        // If the index of the current layer is a valid index in
//                        // the array of population references, then set the
//                        // population reference to the corresponding population
//                        // reference.
//                        grid[layer][0][k] = pops[layer];
//                    } else {
//                        // Otherwise, the index of the current layer is not a
//                        // valid index in the array of population references, so
//                        // set the population reference to null.
//                        grid[layer][0][k] = null;
//                    }
//                }
//            }
//        } else if ((columns == null) || (columns.length <= 0)) {
//            // If the array of column membership sets is null or empty, then
//            // populate the grid of population references with the populations
//            // in the population table after filtering with the array of row
//            // membership sets.
//            // The condition should not be negated (or switched for the negation
//            // of the row version) since that would catch the case where both
//            // the row and the column membership sets are valid.
//
//            PopulationTable rowTable;
//            PopulationReference[] pops;
//
//            // Loop through the array of row membership sets
//            for (int j = 0; j < rows.length; j++) {
//                // Get the row table
//                rowTable = table.filterTable(rows[j]);
//
//                // Get the array of population references in the row table
//                pops = rowTable.getPopulations(type, numEvents);
//
//                // Loop through the layers
//                for (int layer = 0; layer < layerCount; layer++) {
//                    if (layer < pops.length) {
//                        // If the index of the current layer is a valid index in
//                        // the array of population references, then set the
//                        // population reference to the corresponding population
//                        // reference.
//                        grid[layer][j][0] = pops[layer];
//                    } else {
//                        // Otherwise, the index of the current layer is not a
//                        // valid index in the array of population references, so
//                        // set the population reference to null.
//                        grid[layer][j][0] = null;
//                    }
//                }
//            }
//        } else {
//            // Otherwise, the array of row membership sets and the array of
//            // column membership sets are not null and not empty, so populate
//            // the grid of population references with the populations in the
//            // population table after filtering with the array of row membership
//            // sets and the array of column membership sets.
//
//            PopulationTable rowTable, cellTable;
//            PopulationReference[] pops;
//
//            // Loop through the array of row membership sets
//            for (int j = 0; j < rows.length; j++) {
//                // Get the row table
//                rowTable = table.filterTable(rows[j]);
//
//                // Loop through the array of column membership sets
//                for (int k = 0; k < columns.length; k++) {
//                    // Get the cell table
//                    cellTable = rowTable.filterTable(columns[k]);
//
//                    // Get the array of population references in the cell table
//                    pops = cellTable.getPopulations(type, numEvents);
//
//                    // Loop through the layers
//                    for (int layer = 0; layer < layerCount; layer++) {
//                        if (layer < pops.length) {
//                            // If the index of the current layer is a valid
//                            // index in the array of population references, then
//                            // set the population reference to the corresponding
//                            // population reference.
//                            grid[layer][j][k] = pops[layer];
//                        } else {
//                            // Otherwise, the index of the current layer is not
//                            // a valid index in the array of population
//                            // references, so set the population reference to
//                            // null.
//                            grid[layer][j][k] = null;
//                        }
//                    }
//                }
//            }
//        }
//
//        // Create a new population grid using the grid of population references,
//        // row labels, and column labels and return it
//        return new PopulationGrid(grid, rowLabels, columnLabels, layerCount, rowCount, columnCount);
//    }

    /**
     * Static helper methods
     */

    /**
     * <p>
     * Returns the maximum number of layers of tables created by filtering the
     * population table in the <code>PopulationTable</code> object table with
     * the row membership sets in the <code>MembershipSet</code> array of
     * membership sets rows and the column membership sets in the
     * <code>MembershipSet</code> array of membership sets columns.
     * </p>
     *
     * @param table
     * <code>PopulationTable</code> object to the population table
     * containing the populations.
     * @param rows
     * <code>MembershipSet</code> array of row membership sets.
     * @param columns
     * <code>MembershipSet</code> array of column membership sets.
     * @return int maximum number of layers of tables created by filtering the
     * population table in the <code>PopulationTable</code> object table
     * with the row membership sets in the <code>MembershipSet</code>
     * array of membership sets rows and the column membership sets in
     * the <code>MembershipSet</code> array of membership sets columns.
     */
//    private static int getLayerCount(PopulationTable table, MembershipSet[] rows, MembershipSet[] columns) {
//        if (table == null) {
//            // If the population table is null, then return 0.
//            return 0;
//        } else if (((rows == null) || (rows.length <= 0)) && ((columns == null) || (columns.length <= 0))) {
//            // If the array of row membership sets is null or empty and the
//            // array of column membership sets is null or empty, then return the
//            // number of populations in the population table.
//            return table.getPopulationCount();
//        } else if ((rows == null) || (rows.length <= 0)) {
//            // If the array of row membership sets is null or empty, then return
//            // the maximum number of populations after filtering with the array
//            // of column membership sets.
//            // The condition should not be negated (or switched for the negation
//            // of the column version) since that would catch the case where both
//            // the row and the column membership sets are valid.
//
//            // Initialize the maximum number of populations (the number of
//            // layers) to 0
//            int layerCount = 0;
//
//            PopulationTable columnTable;
//            int populationCount;
//
//            // Loop through the array of column membership sets
//            for (int k = 0; k < columns.length; k++) {
//                // Get the column table
//                columnTable = table.filterTable(columns[k]);
//
//                // Get the number of populations in the column table
//                populationCount = columnTable.getPopulationCount();
//
//                if (populationCount > layerCount) {
//                    // If the number of populations in the column table is
//                    // greater than the maximum number of populations, then set
//                    // the maximum number of populations to it.
//                    layerCount = populationCount;
//                }
//            }
//
//            // Return the maximum number of populations (the number of layers)
//            return layerCount;
//        } else if ((columns == null) || (columns.length <= 0)) {
//            // If the array of column membership sets is null or empty, then
//            // return the maximum number of populations after filtering with the
//            // array of row membership sets.
//            // The condition should not be negated (or switched for the negation
//            // of the row version) since that would catch the case where both
//            // the row and the column membership sets are valid.
//
//            // Initialize the maximum number of populations (the number of
//            // layers) to 0
//            int layerCount = 0;
//
//            PopulationTable rowTable;
//            int populationCount;
//
//            // Loop through the array of row membership sets
//            for (int j = 0; j < rows.length; j++) {
//                // Get the row table
//                rowTable = table.filterTable(rows[j]);
//
//                // Get the number of populations in the row table
//                populationCount = rowTable.getPopulationCount();
//
//                if (populationCount > layerCount) {
//                    // If the number of populations in the row table is greater
//                    // than the maximum number of populations, then set the
//                    // maximum number of populations to it.
//                    layerCount = populationCount;
//                }
//            }
//
//            // Return the maximum number of populations (the number of layers)
//            return layerCount;
//        } else {
//            // Otherwise, the array of row membership sets and the array of
//            // column membership sets are not null and not empty, so return the
//            // maximum number of populations after filtering with the array of
//            // row membership sets and the array of column membership sets.
//
//            // Initialize the maximum number of populations (the number of
//            // layers) to 0
//            int layerCount = 0;
//
//            PopulationTable rowTable, cellTable;
//            int populationCount;
//
//            // Loop through the array of row membership sets
//            for (int j = 0; j < rows.length; j++) {
//                // Get the row table
//                rowTable = table.filterTable(rows[j]);
//
//                // Loop through the array of column membership sets
//                for (int k = 0; k < columns.length; k++) {
//                    // Get the cell table
//                    cellTable = rowTable.filterTable(columns[k]);
//
//                    // Get the number of populations in the cell table
//                    populationCount = cellTable.getPopulationCount();
//
//                    if (populationCount > layerCount) {
//                        // If the number of populations in the cell table is
//                        // greater than the maximum number of populations, then
//                        // set the maximum number of populations to it.
//                        layerCount = populationCount;
//                    }
//                }
//            }
//
//            // Return the maximum number of populations (the number of layers)
//            return layerCount;
//        }
//    }

    /**
     * <p>
     * Returns the minimum in the array of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the array of values is null or empty or all the values in the array of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * double array of values.
     * @return double minimum or Double.NaN if the array of values is null or
     * empty or all the values in the array of values are Double.NaN.
     */
    public static double getMin(double[] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the array of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Initialize the minimum to Double.NaN
        double min = Double.NaN;

        // Initialize whether the current value is the first number to true
        boolean firstP = true;

        // Loop through the array of values
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                // If the current value is a number, then process it.
                if (firstP) {
                    // If the current value is the first number, then set the
                    // minimum to it.
                    min = values[i];

                    // Set whether the current value is the first number to
                    // false
                    firstP = false;
                } else if (values[i] < min) {
                    // If the current value is not the first number and is less
                    // than the current minimum, then set the minimum to it.
                    min = values[i];
                }
            }
        }

        // Return the minimum
        return min;
    }

    /**
     * <p>
     * Returns the minimum in the grid of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the grid of values is null or empty or all the values in the grid of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * array of double arrays containing the grid of values.
     * @return double minimum or Double.NaN if the grid of values is null or
     * empty or all the values in the grid of values are Double.NaN.
     */
    public static double getMin(double[][] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the grid of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Allocate the array of minimums
        double[] mins = new double[values.length];

        // Loop through the rows
        for (int i = 0; i < values.length; i++) {
            // Get the minimum of the current row
            mins[i] = getMin(values[i]);
        }

        // Return the minimum of the array of minimums
        return getMin(mins);
    }

    /**
     * <p>
     * Returns the minimum in the grid of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the grid of values is null or empty or all the values in the grid of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * array of arrays of double arrays containing the grid of
     * values.
     * @return double minimum or Double.NaN if the grid of values is null or
     * empty or all the values in the grid of values are Double.NaN.
     */
    public static double getMin(double[][][] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the grid of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Allocate the array of minimums
        double[] mins = new double[values.length];

        // Loop through the layers
        for (int i = 0; i < values.length; i++) {
            // Get the minimum of the current layer
            mins[i] = getMin(values[i]);
        }

        // Return the minimum of the array of minimums
        return getMin(mins);
    }

    /**
     * <p>
     * Returns the maximum in the array of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the array of values is null or empty or all the values in the array of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * double array of values.
     * @return double maximum or Double.NaN if the array of values is null or
     * empty or all the values in the array of values are Double.NaN.
     */
    public static double getMax(double[] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the array of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Initialize the maximum to Double.NaN
        double max = Double.NaN;

        // Initialize whether the current value is the first number to true
        boolean firstP = true;

        // Loop through the array of values
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                // If the current value is a number, then process it.
                if (firstP) {
                    // If the current value is the first number, then set the
                    // maximum to it.
                    max = values[i];

                    // Set whether the current value is the first number to
                    // false
                    firstP = false;
                } else if (values[i] > max) {
                    // If the current value is not the first number and is
                    // greater than the current maximum, then set the maximum to
                    // it.
                    max = values[i];
                }
            }
        }

        // Return the maximum
        return max;
    }

    /**
     * <p>
     * Returns the maximum in the grid of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the grid of values is null or empty or all the values in the grid of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * array of double arrays containing the grid of values.
     * @return double maximum or Double.NaN if the grid of values is null or
     * empty or all the values in the grid of values are Double.NaN.
     */
    public static double getMax(double[][] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the grid of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Allocate the array of maximums
        double[] maxs = new double[values.length];

        // Loop through the rows
        for (int i = 0; i < values.length; i++) {
            // Get the maximum of the current row
            maxs[i] = getMax(values[i]);
        }

        // Return the maximum of the array of maximums
        return getMax(maxs);
    }

    /**
     * <p>
     * Returns the maximum in the grid of values values ignoring Double.NaN.
     * </p>
     *
     * <p>
     * If the grid of values is null or empty or all the values in the grid of
     * values are Double.NaN, then Double.NaN is returned.
     * </p>
     *
     * @param values
     * array of arrays of double arrays containing the grid of
     * values.
     * @return double maximum or Double.NaN if the grid of values is null or
     * empty or all the values in the grid of values are Double.NaN.
     */
    public static double getMax(double[][][] values) {
        if ((values == null) || (values.length <= 0)) {
            // If the grid of values is null or empty, then return Double.NaN.
            return Double.NaN;
        }

        // Allocate the array of maximums
        double[] maxs = new double[values.length];

        // Loop through the layers
        for (int i = 0; i < values.length; i++) {
            // Get the maximum of the current layer
            maxs[i] = getMax(values[i]);
        }

        // Return the maximum of the array of maximums
        return getMax(maxs);
    }

    /**
     * Testing methods
     */

    /**
     * <p>
     * Prints the population grid.
     * </p>
     */
    private void print() {
        // Loop through each element of the population grid
        for (int i = 0; i < layerCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                for (int k = 0; k < columnCount; k++) {
                    if (k > 0) {
                        // If the column is not the first column, then print out
                        // a tab.
                        System.out.print("\t");
                    }

                    if (grid[i][j][k] == null) {
                        // If the population reference at (i, j, k) is null,
                        // then print out "null".
                        System.out.print("null");
                    } else {
                        // Otherwise, the population reference at (i, j, k) is
                        // not null, so print out the filename.
                        System.out.print(grid[i][j][k].getFilename());
                    }
                }

                // Print a newline to end the row
                System.out.println();
            }

            // Print a newline to separate the current layer from the next layer
            System.out.println();
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
        /**
         * Some tests with NaN to prove that it behaves the way we want
         */
        System.out.println("(Double.NaN == Double.NaN) = " + (Double.NaN == Double.NaN));
        System.out.println("(Double.NaN != Double.NaN) = " + (Double.NaN != Double.NaN));
        System.out.println("(Double.NaN < 0) = " + (Double.NaN < 0));
        System.out.println("(Double.NaN == 0) = " + (Double.NaN == 0));
        System.out.println("(Double.NaN > 0) = " + (Double.NaN > 0));
        System.out.println("(Double.NaN / 100) = " + (Double.NaN / 100));
        System.out.println("Double.isNaN(Double.NaN / 100) = " + Double.isNaN(Double.NaN / 100));
        System.out.println("Math.log10(Double.NaN / 100) = " + Math.log10(Double.NaN / 100));
        System.out.println("Double.isNaN(Math.log10(Double.NaN / 100)) = " + Double.isNaN(Math.log10(Double.NaN / 100)));
        System.out.println("Math.min(Double.NaN, 0) = " + Math.min(Double.NaN, 0));
        System.out.println("Math.max(Double.NaN, 0) = " + Math.max(Double.NaN, 0));
    }
}