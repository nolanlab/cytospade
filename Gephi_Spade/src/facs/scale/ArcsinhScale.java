/**
* ArcsinhScale.java
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
package facs.scale;

/**
* <p>
* An arcsinh scale.
* </p>
*
* <p>
* Since there is no arcsinh function in Java, we implement arcsinh using:
* </p>
*
* <p align="center"><code>arcsinh(x) = ln(x + sqrt(x^2 + 1))</code></p>
*
* <p>
* The natural logarithm (ln) in Java corresponds to the Math.log method.
* </p>
*/
public final class ArcsinhScale implements Scale {
    /**
* <p>
* A blank constructor for <code>ArcsinhScale</code>.
* </p>
*/
    ArcsinhScale() {
    }

    /**
* <p>
* Returns the transformed value of x.
* </p>
*
* @param x
* double value of x.
* @return double transformed value of x.
*/
    public double getValue(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0d));
    }

    /**
* <p>
* Returns the transformed value of x.
* </p>
*
* @param x
* double value of x.
* @return double untransformed value of x.
*/
    public double undoValue(double x) {
        // Return the sinh of x
        return 0.5d * (Math.exp(x) - Math.exp(-x));
    }

    /**
* <p>
* Returns the transformed value of x.
* </p>
*
* @param x
* double value of x.
* @param arg
* <code>ScaleArgument</code> object to the optional scale
* argument.
* @return double transformed value of x.
*/
    public double getValue(double x, ScaleArgument arg) {
        if (arg == null) {
            // If the optional scale argument is null, then return the
            // transformed value of x.
            return getValue(x);
        } else if (arg instanceof ArcsinhScaleArgument) {
            // If the optional scale argument is an arcsinh scale argument, then
            // cast it to an arcsinh scale argument and get the compression
            // width from it.
            int compressionWidth = ((ArcsinhScaleArgument) arg).getCompressionWidth();

            if ((compressionWidth == 0) || (compressionWidth == 1)) {
                // If the compression width is 0 or 1, then ignore it and return
                // the transformed value of x.
                return getValue(x);
            } else {
                // Otherwise, the compression width is not 0 or 1, so return the
                // transformed value of x divided by the compression width.
                return getValue(x / (double) compressionWidth);
            }
        } else {
            // Otherwise, the optional scale argument is not an arcsinh scale
            // argument, so just ignore it and return the transformed value of
            // x.
            return getValue(x);
        }
    }

    /**
* <p>
* Returns the un-transformed value of a given bin, in terms of real scale
* values, given the number of bins and the region of the scale these bins
* represent.
* </p>
*
* @param binIndex
* <code>int</code> the index of the bin to un-transform.
* @param numBins
* <code>int</code> the total number of bins.
* @param scaleMin
* <code>double</code> value of the minimum scale value.
* @param scaleMax
* <code>double</code> value of the maximum scale value.
* @param arg
* <code>ScaleArgument</code> object to the optional scale
* argument.
*
* @return double un-transformed value of x.
*/
    public double unbin(int binIndex, int numBins, double scaleMin, double scaleMax, ScaleArgument arg) {
        double unbinnedValue = 0.0d;

        // Calculate the linear fraction of the scale the bin represents
        double fractionOfScale = (double) binIndex / (double) numBins;

        // Determine the compression width
        int compressionWidth = ((ArcsinhScaleArgument) arg).getCompressionWidth();

        // Calculate the total linear range of the scale
        double scaleRange = getValue(scaleMax / (double) compressionWidth) - getValue(scaleMin / (double) compressionWidth);

        // Calculate the linear value the bin represents
        double valueExponent = getValue(scaleMin / (double) compressionWidth) + (fractionOfScale * scaleRange);

        // Calculate the unbinned value
        unbinnedValue = undoValue(valueExponent) * (double) compressionWidth;

        return unbinnedValue;
    }

    /**
* Return a unique identifier for this class
* @return A String representation of this class for caching
*/

    public String cacheKey() {
        return "ArcsinhScale";
    }
}