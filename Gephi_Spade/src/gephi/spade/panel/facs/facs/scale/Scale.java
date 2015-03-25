/**
* Scale.java
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
* An interface for scales (objects that perform scaling).
* </p>
*
* <p>
* A class that implements the <code>Scale</code> interface should be immutable.
* A scale represents a mathematical function and those are invariant, with the
* exception of the arguments.
* </p>
*
* <p>
* The idea of having an interface is so that the scales in Cytobank could be
* modular and new scales can be added as people think of them. The interface
* only requires two variants of the getValue method. Basically, a scale can be
* thought of as a mathematical function f(x) that transforms x. In order to
* avoid precision errors in the scale, the getValue methods only take and
* return double precision values. This way, rounding can be done by the clients
* that use the scale without loss of precision in the scales.
* </p>
*
* <p>
* The first version of the getValue method simply takes the value of x to
* transform as an argument. All scales should default to this method and ensure
* that the properly transformed values of x is returned in the event that the
* client does not use the second version and pass the optional scale argument.
* </p>
*
* <p>
* The second version of the getValue method takes an optional scale argument in
* the <code>ScaleArgument</code> object arg. This was designed so that scale
* functions that require additional arguments can implement the
* <code>ScaleArgument</code> interface to contain the extra arguments and pass
* those around.
* </p>
*
* <p>
* The getValue methods should be designed to return the value as quickly as
* possible with minimal overhead, because each scale will be called repeatedly
* to scale each event in a population, so efficiency, minimal overhead, and
* speed is of the essence.
* </p>
*/
public interface Scale {
    /**
* <p>
* Returns the transformed value of x.
* </p>
*
* @param x
* double value of x.
* @return double transformed value of x.
*/
    public double getValue(double x);

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
    public double getValue(double x, ScaleArgument arg);

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
    public double unbin(int binIndex, int numBins, double scaleMin, double scaleMax, ScaleArgument arg);


    /**
* Return a unique identifier for this class
* @return A String representation of this class for caching
*/
    public String cacheKey();
}

