package facs;

import facs.scale.Scale;
import facs.scale.ScaleArgument;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class PopulationL {

    /**
     * <p>
     * Scales the channels indicated by the channel indices xChannel, yChannel,
     * and zChannel with the scales in the <code>Scale</code> objects xScale,
     * yScale, and zScale, respectively, and bins the channels to the number of
     * bins in numXBins, numYBins, and numZBins, respectively, and returns an
     * array of arrays of arrays of ints of size numXBins by numYBins by
     * numZBins containing the array of bin values.
     * </p>
     *
     * @param xScale
     * <code>Scale</code> object to the scale of the x-axis.
     * @param yScale
     * <code>Scale</code> object to the scale of the y-axis.
     * @param xScaleArgument
     * <code>ScaleArgument</code> object to the scale argument for
     * the scale of the x-axis.
     * @param yScaleArgument
     * <code>ScaleArgument</code> object to the scale argument for
     * the scale of the y-axis.
     * @param numXBins
     * int number of bins on the x-axis.
     * @param numYBins
     * int number of bins on the y-axis.
     * @param xMin
     * double minimum value in the range of values to bin on the x
     * channel.
     * @param xMax
     * double maximum value in the range of values to bin on the x
     * channel.
     * @param yMin
     * double minimum value in the range of values to bin on the y
     * channel.
     * @param yMax
     * double maximum value in the range of values to bin on the y
     * channel.
     * @param xChannelEvents
     * double[] X channel events.
     * @param yChannelEvents
     * double[] Y channel events.
     * @return array of arrays of arrays of ints containing the array of bin
     * values.
     * @throws IOException
     */
    public static int[][]getBinValues(final Scale xScale, final Scale yScale, final ScaleArgument xScaleArgument,
            final ScaleArgument yScaleArgument, final int numXBins, final int numYBins,double xMin, double xMax, double yMin,
            double yMax, final double[] xChannelEvents, final double[] yChannelEvents) throws IOException {

        double temp;

        if (xMax < xMin) {
            // If the maximum value in the range of values to bin on the x
            // channel is less than the minimum value in the range of values to
            // bin on the x channel, then swap the minimum and the maximum.
            temp = xMin;
            xMin = xMax;
            xMax = temp;
        }

        if (yMax < yMin) {
            // If the maximum value in the range of values to bin on the y
            // channel is less than the minimum value in the range of values to
            // bin on the y channel, then swap the minimum and the maximum.
            temp = yMin;
            yMin = yMax;
            yMax = temp;
        }

        // Set the maximum bin as the last bin in the array of bin values
        int maxXBin = numXBins - 1;
        int maxYBin = numYBins - 1;

        /**
         * Calculate the scale factor used to bin based on the range of the
         * channel
         */
        double scaledXMin, scaledYMin, xScaleFactor, yScaleFactor;

        if (xScaleArgument == null) {
            // If the scale argument is null, then use the version of getValue()
            // without the scale argument.
            scaledXMin = xScale.getValue(xMin);
            xScaleFactor = (double) numXBins / (xScale.getValue(xMax) - scaledXMin);
        } else {
            // Otherwise, the scale argument is not null, so use the version of
            // getValue() with the scale argument.
            scaledXMin = xScale.getValue(xMin, xScaleArgument);
            xScaleFactor = (double) numXBins / (xScale.getValue(xMax, xScaleArgument) - scaledXMin);
        }

        if (yScaleArgument == null) {
            // If the scale argument is null, then use the version of getValue()
            // without the scale argument.
            scaledYMin = yScale.getValue(yMin);
            yScaleFactor = (double) numYBins / (yScale.getValue(yMax) - scaledYMin);
        } else {
            // Otherwise, the scale argument is not null, so use the version of
            // getValue() with the scale argument.
            scaledYMin = yScale.getValue(yMin, yScaleArgument);
            yScaleFactor = (double) numYBins / (yScale.getValue(yMax, yScaleArgument) - scaledYMin);
        }

        // Allocate the array of bin values
        int[][] bins = new int[numXBins][numYBins];

        // Initialize all the bin values to 0 - not necessary, but it doesn't
        // hurt
        for (int i = 0; i < numXBins; i++) {
            for (int j = 0; j < numYBins; j++) {
                    bins[i][j] = 0;
            }
        }

        int xBin, yBin;

        final int eventCount = xChannelEvents.length;

        // Loop through all the events in the population
        for (int eventNumber = 0; eventNumber < eventCount; eventNumber++) {
            /**
             * Calculate the bin value
             */

            if (xScaleArgument == null) {
                // If the scale argument is null, then use the version of
                // getValue() without the scale argument.
             // Use relative positions
                xBin = (int) ((xScale.getValue(xChannelEvents[eventNumber]) - scaledXMin) * xScaleFactor);
            } else {
                // Otherwise, the scale argument is not null, so use the version
                // of getValue() with the scale argument.
             // Use relative positions
             xBin = (int) ((xScale.getValue(xChannelEvents[eventNumber], xScaleArgument) - scaledXMin) * xScaleFactor);
            }

            if (yScaleArgument == null) {
                // If the scale argument is null, then use the version of
                // getValue() without the scale argument.
             // Use relative positions
             yBin = (int) ((yScale.getValue(yChannelEvents[eventNumber]) - scaledYMin) * yScaleFactor);
            } else {
                // Otherwise, the scale argument is not null, so use the version
                // of getValue() with the scale argument.
             // Use relative positions
             yBin = (int) ((yScale.getValue(yChannelEvents[eventNumber], yScaleArgument) - scaledYMin) * yScaleFactor);
            }

            if (xBin < 0) {
                // If the calculated x bin is less than the first bin, then set
                // the x bin to the first bin.
                xBin = 0;
            } else if (xBin > maxXBin) {
                // If the calculated x bin is greater than the maximum bin, then
                // set the x bin to the maximum bin.
                xBin = maxXBin;
            }

            if (yBin < 0) {
                // If the calculated y bin is less than the first bin, then set
                // the y bin to the first bin.
                yBin = 0;
            } else if (yBin > maxYBin) {
                // If the calculated y bin is greater than the maximum bin, then
                // set the y bin to the maximum bin.
                yBin = maxYBin;
            }

            // Increment the bin value in the calculated bin
            bins[xBin][yBin]++;
        }

        // Return the array of bin values
        return bins;
    }


    /**
     * <p>
     * Sorts a 2D array of bin values and returns a 2D grid of doubles with the
     * bin values in the first column, the x indices in the second column, and
     * the y indices in the third column.
     * </p>
     *
     * <p>
     * I am sorry, even though I have gone back and edited this, it still looks
     * like a major hack. I think local classes or anonymous classes may be a
     * better solution.
     * </p>
     *
     * @param binValues
     * array of int arrays containing the bin values to sort.
     * @return array of double arrays containing the sorted bin values along the
     * first column and the next two columns are the corresponding bin
     * location.
     */
    public static double[][] sortBinValues(int[][] binValues) {
        if ((binValues == null) || (binValues.length == 0)) {
            // If the grid of bin values is null or empty, then return an empty
            // grid of sorted bin values.
            return new double[0][0];
        }

        // Get the number of bins on the x-axis
        int numXBins = binValues.length;

        // Get the number of bins on the y-axis
        int numYBins = binValues[0].length;

        // Allocate the grid of sorted bin values
        double[][] sortedBinValues = new double[numXBins * numYBins][3];

        // Initialize the index i the grid of sorted bin values to 0
        int index = 0;

        // Loop through the bins on the x-axis
        for (int i = 0; i < binValues.length; i++) {
            // Loop through the bins on the y-axis
            for (int j = 0; j < binValues[i].length; j++) {
                sortedBinValues[index][0] = (double) binValues[i][j];
                sortedBinValues[index][1] = (double) i;
                sortedBinValues[index][2] = (double) j;

                // Increment the index in the grid of sorted bin values
                index++;
            }
        }

        // Sort the bin values
        Arrays.sort(sortedBinValues, COLUMN_COMPARATOR);

        // Return the grid of sorted bin values
        return sortedBinValues;
    }

    /**
     * <p>
     * Sorts a 2D array of density values and returns a 2D grid of doubles with
     * the sorted density values in the first column, the x indices in the
     * second column, and the y indices in the third column.
     * </p>
     *
     * <p>
     * I am sorry, even though I have gone back and edited this, it still looks
     * like a major hack. I think local classes or anonymous classes may be a
     * better solution.
     * </p>
     *
     * @param densityValues
     * array of double arrays containing the density values to sort.
     * @return array of double arrays containing the sorted density values along
     * the first column and the next two columns are the corresponding
     * bin location.
     */
    public static double[][] sortDensityValues(double[][] densityValues) {
        if ((densityValues == null) || (densityValues.length == 0)) {
            // If the grid of density values is null or empty, then return an
            // empty grid of sorted density values.
            return new double[0][0];
        }

        // Get the number of bins on the x-axis
        int numXBins = densityValues.length;

        // Get the number of bins on the y-axis
        int numYBins = densityValues[0].length;

        // Allocate the grid of sorted density values
        double[][] sortedDensityValues = new double[numXBins * numYBins][3];

        // Initialize the index i the grid of sorted density values to 0
        int index = 0;

        // Loop through the bins on the x-axis
        for (int i = 0; i < densityValues.length; i++) {
            // Loop through the bins on the y-axis
            for (int j = 0; j < densityValues[i].length; j++) {
                sortedDensityValues[index][0] = (double) densityValues[i][j];
                sortedDensityValues[index][1] = (double) i;
                sortedDensityValues[index][2] = (double) j;

                // Increment the index in the grid of sorted density values
                index++;
            }
        }

        // Sort the density values
        Arrays.sort(sortedDensityValues, COLUMN_COMPARATOR);

        // Return the grid of sorted bin values
        return sortedDensityValues;
    }


    /**
     * <p>
     * Returns the values of the triweight kernel density function evaluated
     * over the grid.
     * </p>
     *
     * @param binValues
     * array of int arrays containing the array of bin values.
     * @param smoothing
     * double smoothing factor.
     * @return array of double arrays containing the array of triweight kernel
     * density values.
     */
    public static double[][] getTriweightKernelDensityValues(int[][] binValues, double smoothing) {
        if ((binValues == null) || (binValues.length <= 0)) {
            // If the array of bin values is null or empty, then return an empty
            // array of density values.
            return new double[0][0];
        }

        // Set the number of dimensions to 2
        final int dimensions = 2;

        // Get the number of bins
        int numXBins = binValues.length;
        int numYBins = binValues[0].length;

        /**
         * Count the number of events
         */

        // Initialize the number of events to 0
        int numEvents = 0;

        // Loop through all the bins adding the number of events in each bin to
        // the number of events
        for (int i = 0; i < numXBins; i++) {
            for (int j = 0; j < numYBins; j++) {
                numEvents += binValues[i][j];
            }
        }

        // Step 1. compute the bandwidth in each dimension
        double xmean = 0;
        for (int x = 0; x < numXBins; x++) {
            for (int y = 0; y < numYBins; y++) {
                xmean += binValues[x][y] * x;
            }
        }
        xmean /= numEvents;
        double xsd = 0;
        for (int x = 0; x < numXBins; x++) {
            for (int y = 0; y < numYBins; y++) {
                xsd += binValues[x][y] * (x - xmean) * (x - xmean);
            }
        }
        xsd = Math.sqrt(xsd / numEvents);
        double a = Math.pow(4.0 / ((dimensions + 2) * numEvents), 1.0 / (dimensions + 4));
        double xBandwidth = smoothing * xsd * a;

        double ymean = 0;
        for (int x = 0; x < numXBins; x++) {
            for (int y = 0; y < numYBins; y++) {
                ymean += binValues[x][y] * y;
            }
        }
        ymean /= numEvents;
        double ysd = 0;
        for (int x = 0; x < numXBins; x++) {
            for (int y = 0; y < numYBins; y++) {
                ysd += binValues[x][y] * (y - ymean) * (y - ymean);
            }
        }
        ysd = Math.sqrt(ysd / numEvents);
        double yBandwidth = smoothing * ysd * a;

        // Step 2. do some calculations ahead of time and store
        // them in an array. this is for efficiency
        int nx = (int) xBandwidth;
        int ny = (int) yBandwidth;
        double[] kernelEvaluationsX = new double[nx + 1];
        double[] kernelEvaluationsY = new double[ny + 1];

        for (int i = 0; i < nx + 1; i++) {
            kernelEvaluationsX[i] = computeTriweightAtPoint(i / xBandwidth);
        }
        for (int i = 0; i < ny + 1; i++) {
            kernelEvaluationsY[i] = computeTriweightAtPoint(i / yBandwidth);
        }

        // Step 3. do the density estimate over the grid
        double[][] densityValues = new double[numXBins][numYBins];
        int startP, endP, startQ, endQ;
        for (int i = 0; i < numXBins; i++) {
            for (int j = 0; j < numYBins; j++) {
                if (binValues[i][j] != 0) {
                    startP = Math.max(-nx, -i);
                    endP = Math.min(nx, numXBins - 1 - i);
                    for (int p = startP; p <= endP; p++) {
                        startQ = Math.max(-ny, -j);
                        endQ = Math.min(ny, numYBins - 1 - j);
                        for (int q = startQ; q <= endQ; q++) {
                            densityValues[i + p][j + q] += binValues[i][j] * kernelEvaluationsX[Math.abs(p)] * kernelEvaluationsY[Math.abs(q)];
                        }
                    }
                }
            }
        }

        // Return the density values
        return densityValues;
    }

    /**
     * <p>
     * Returns the value of the triweight kernel density function at p.
     * </p>
     *
     * @param p
     * double value at which to evaluate the triweight kernel density
     * function.
     */
    static double computeTriweightAtPoint(double p) {
        if ((p < -1) || (p > 1)) {
            // If the value of p is less than -1 or greater than 1, then return
            // 0.0.
            return 0.0d;
        } else {
            // Otherwise, calculate the triweight kernel density at p.
            double oneminuspsquared = 1 - (p * p);

            return (oneminuspsquared * oneminuspsquared * oneminuspsquared);
        }
    }

    /**
     * The column comparator for sorting bin and density values
     */
    private static final ColumnComparatorDouble COLUMN_COMPARATOR = new ColumnComparatorDouble(0);

    /**
     * Comparators
     */

    /**
     * <p>
     * A private static nested comparator class to sort a 2D array of doubles
     * based on the entries in the specified column.
     * </p>
     *
     * <p>
     * The class is immutable.
     * </p>
     */
    private final static class ColumnComparatorDouble implements Comparator<double[]> {
        /**
         * The index of the specified column
         */
        private final int col;

        /**
         * <p>
         * Creates a <code>ColumnComparatorDouble</code> object that compares
         * the column specified by the column index col.
         * </p>
         *
         * @param col
         * int index of the column to compare.
         */
        private ColumnComparatorDouble(int col) {
            // Set the column used for the comparison
            if (col < 0) {
                // If the index of the column is less than 0, then set the index
                // of the column to 0.
                this.col = 0;
            } else {
                // Otherwise, the index of the column is greater than or equal
                // to 0, so set the index of the column.
                this.col = col;
            }
        }

        /**
         * <p>
         * Compares the specified columns of the two arrays of doubles and
         * returns the result of the comparison.
         * </p>
         *
         * @param a
         * array of doubles to the first array.
         * @param b
         * array of doubles to the second array.
         * @return -1, 0, or 1 as the first array is less than, equal to, or
         * greater than the second array.
         */
        public int compare(double[] a, double[] b) {
            if ((col >= a.length) && (col >= b.length)) {
                // If the index of the column is larger than the lengths of both
                // arrays, then return 0.
                return 0;
            } else if (col >= a.length) {
                // If the index of the column is only greater than the length of
                // the first array, then return -1 to put the first array ahead
                // of the second array.
                return -1;
            } else if (col >= b.length) {
                // If the index of the column is only greater than the length of
                // the second array, then return 1 to put the second array ahead
                // of the first array.
                return 1;
            } else {
                // Otherwise, the index of the column is valid for both arrays,
                // so return the value of the actual comparison of the specified
                // column.
                return (int) Math.signum(a[col] - b[col]);
            }
        }
    }

}
