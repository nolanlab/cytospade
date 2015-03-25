/**
* PlotRegion.java
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

/**
* <p>
* A class to encapsulate the information to show for a region.
* </p>
*
* <p>
* The name of the class is a bit misleading. Even though the class is named
* "PlotRegion", it actually encapsulates information for a region, so it is not
* only used in a plot, but it is also used in generating statistics as well. It
* is named "PlotRegion" for two reasons. First, it evolved out of the region
* used in a plot. Second, the term "region" has other connotations in a flow
* context, so I wanted to avoid using that term.
* </p>
*
* <p>
* The <code>PlotRegion</code> class recognizes the following values for a
* region parameter:
* </p>
*
* <ul>
* <li>"none": Do not show the region. This takes precedence over all other
* values.</li>
* <li>"region": Draw the region. This only applies in a plotting context.</li>
* <li>"label": Show the label of the region, which is the name of the gate.</li>
* <li>"eventCount": Show the number of events in the region.</li>
* <li>"mean": Show the mean of the region.</li>
* <li>"median": Show the median of the region.</li>
* <li>"percent": Show the percent of the region.</li>
* </ul>
*
* <p>
* The class is immutable.
* </p>
*/
public final class PlotRegion implements Comparable<PlotRegion> {
    /**
* The ID of the region
*/
    private final int id;

    /**
* The boolean flag indicating whether to show the region
*/
    private final boolean showP;

    /**
* The boolean flag indicating whether to draw the region
*/
    private final boolean drawP;

    /**
* The boolean flag indicating whether to show the label of the region
*/
    private final boolean showLabelP;

    /**
* The boolean flag indicating whether to show the number of events in the
* region
*/
    private final boolean showEventCountP;

    /**
* The boolean flag indicating whether to show the mean of the region
*/
    private final boolean showMeanP;

    /**
* The boolean flag indicating whether to show the median of the region
*/
    private final boolean showMedianP;

    /**
* The boolean flag indicating whether to show the percent of the region
*/
    private final boolean showPercentP;

    /**
* <p>
* A constructor for <code>PlotRegion</code>.
* </p>
*
* @param id
* int ID of the region.
* @param showP
* boolean flag indicating whether to show the region.
* @param drawP
* boolean flag indicating whether to draw the region.
* @param showLabelP
* boolean flag indicating whether to show the label of the
* region.
* @param showEventCountP
* boolean flag indicating whether to show the number of events
* in the region,
* @param showMeanP
* boolean flag indicating whether to show the mean of the
* region.
* @param showMedianP
* boolean flag indicating whether to show the median of the
* region.
* @param showPercentP
* boolean flag indicating whether to show the percent of the
* region.
*/
    private PlotRegion(int id, boolean showP, boolean drawP, boolean showLabelP, boolean showEventCountP, boolean showMeanP, boolean showMedianP,
            boolean showPercentP) {
        // Set the ID of the region
        this.id = id;

        /**
* Set all the boolean flags
*/
        this.showP = showP;
        this.drawP = drawP;
        this.showLabelP = showLabelP;
        this.showEventCountP = showEventCountP;
        this.showMeanP = showMeanP;
        this.showMedianP = showMedianP;
        this.showPercentP = showPercentP;
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
* Comparison is performed by comparing the IDs of the two regions if the
* <code>Object</code> object obj is a <code>PlotRegion</code> object.
* </p>
*
* @param obj
* <code>Object</code> object to the reference object with which
* to compare.
* @return boolean flag indicating whether this region is equal to the
* object in the <code>Object</code> object obj.
*/
    public boolean equals(Object obj) {
        if (obj == null) {
            // If the object is null, then return false.
            return false;
        }

        if (obj instanceof PlotRegion) {
            // If the object is a region, then cast it to a region.
            PlotRegion region = (PlotRegion) obj;

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
* Returns whether to show the label of the region.
* </p>
*
* @return boolean flag indicating whether to show the label of the region.
*/
    public boolean isLabelShown() {
        return showLabelP;
    }

    /**
* <p>
* Returns whether to show the number of events in the region.
* </p>
*
* @return boolean flag indicating whether to show the number of events in
* the region.
*/
    public boolean isEventCountShown() {
        return showEventCountP;
    }

    /**
* <p>
* Returns whether to show the mean of the region.
* </p>
*
* @return boolean flag indicating whether to show the mean of the region.
*/
    public boolean isMeanShown() {
        return showMeanP;
    }

    /**
* <p>
* Returns whether to show the median of the region.
* </p>
*
* @return boolean flag indicating whether to show the median of the region.
*/
    public boolean isMedianShown() {
        return showMedianP;
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
* Returns whether to show the percent of the region.
* </p>
*
* <p>
* Syntactic sugar for the isPercentShown method.
* </p>
*
* @return boolean flag indicating whether to show the percent of the
* region.
*/
    public boolean isPercentageShown() {
        return isPercentShown();
    }

    /**
* Comparable interface
*/

    /**
* <p>
* Returns the comparison of this region with the region in the
* <code>PlotRegion</code> object region.
* </p>
*
* <p>
* This method is the <code>PlotRegion</code> implementation of the abstract
* method in the <code>Comparable&lt;PlotRegion&gt;</code> interface to
* allow regions to be sorted by ID.
* </p>
*
* <p>
* Comparison is performed by comparing the IDs of the two regions if the
* <code>Object</code> object obj is a <code>PlotRegion</code> object.
* </p>
*
* <p>
* The ordering is consistent with equals.
* </p>
*
* @param region
* <code>PlotRegion</code> object to the region with which to
* compare.
* @return int result of the comparison.
*/
    public int compareTo(PlotRegion region) {
        if (region == null) {
            // If the region is null, then throw a null pointer exception.
            throw new NullPointerException("The region is null.");
        }

        if (id < region.id) {
            // If the ID of this region is less than the ID of the other region,
            // then this region precedes the other region, so return -1.
            return -1;
        } else if (id == region.id) {
            // If the IDs of the regions are equal, then the regions are equal,
            // so then return 0.
            return 0;
        } else {
            // Otherwise, the ID of this region is greater than the ID of the
            // other region, so this region follows the other region, so return
            // 1.
            return 1;
        }
    }

    /**
* Factory methods
*/

    /**
* <p>
* Returns a region based on the ID id and the values of the region
* parameter in the <code>String</code> array values.
* </p>
*
* @param id
* int ID of the region.
* @param values
* <code>String</code> array of values of the region parameter.
* @return <code>PlotRegion</code> object to the region based on the values
* of the region parameter in the <code>String</code> array values.
*/
    static PlotRegion getPlotRegion(int id, String[] values) {
        /**
* Initialize all the boolean flags to false
*/
        boolean showP = false;
        boolean drawP = false;
        boolean showLabelP = false;
        boolean showEventCountP = false;
        boolean showMeanP = false;
        boolean showMedianP = false;
        boolean showPercentP = false;

        /**
* Parse the array of values of the region parameter
*/

        if (values != null) {
            // If the array of values is not null, then use them to set the
            // boolean flags of the region.
            showP = true;

            // Loop through the array of values
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    // If the current value is not null, then process it.
                    if (values[i].equals("none")) {
                        // If the current value is "none", then set whether to
                        // show the region to false.
                        showP = false;
                    } else if (values[i].equals("region")) {
                        // If the current value is "region", then set whether to
                        // draw the region to true.
                        drawP = true;
                    } else if (values[i].equals("label")) {
                        // If the current value is "label", then set whether to
                        // show the label of the region to true.
                        showLabelP = true;
                    } else if (values[i].equals("eventCount")) {
                        // If the current value is "eventCount", then set
                        // whether to show the number of events in the region to
                        // true.
                        showEventCountP = true;
                    } else if (values[i].equals("mean")) {
                        // If the current value is "mean", then set whether to
                        // show the mean of the region to true.
                        showMeanP = true;
                    } else if (values[i].equals("median")) {
                        // If the current value is "median", then set whether to
                        // show the median of the region to true.
                        showMedianP = true;
                    } else if (values[i].equals("percent")) {
                        // If the current value is "percent", then set whether
                        // to show the percent of the region to true.
                        showPercentP = true;
                    }
                }
            }
        }

        // Create the PlotRegion object and return it
        return new PlotRegion(id, showP, drawP, showLabelP, showEventCountP, showMeanP, showMedianP, showPercentP);
    }

    /**
* Testing methods
*/

    /**
* <p>
* A private static test method that takes the descriptor string in the
* <code>String</code> descriptor, creates a plot region from it, and prints
* it out.
* </p>
*/
    private static void testPlotRegion(String[] values) {
        // Get the region
        PlotRegion region = PlotRegion.getPlotRegion(-1, values);

        if (region != null) {
            // If the plot region is not null, then print out its fields.
            System.out.println("ID: " + region.getID());
            System.out.println("Shown?: " + region.isShown());
            System.out.println("Drawn?: " + region.isDrawn());
            System.out.println("Label?: " + region.isLabelShown());
            System.out.println("Event Count?: " + region.isEventCountShown());
            System.out.println("Mean?: " + region.isMeanShown());
            System.out.println("Median?: " + region.isMedianShown());
            System.out.println("Percent?: " + region.isPercentShown());
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
            // If the array of arguments is not empty, then pass the first
            // argument as an argument to testPlotRegion();
            testPlotRegion(args);
        }
    }
}