/**
* ColorGradient.java
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

// Import the Color class
import java.awt.Color;

/**
* <p>
* An interface for color gradients.
* </p>
*/
public interface ColorGradient {
    /**
* Color set constant flags --- The following flags are used to indicate the
* color set of the gradient.
*/

    /**
* The blue to yellow color set
*/
    public static final int BLUE_TO_YELLOW_GRADIENT = 1;

    /**
* The green to red color set
*/
    public static final int GREEN_TO_RED_GRADIENT = 2;

    /**
* The greyscale color set
*/
    public static final int GREYSCALE_GRADIENT = 3;

    /**
* <p>
* Returns the minimum value of the color gradient.
* </p>
*
* @return double minimum value of the color gradient.
*/
    public double getMinimum();

    /**
* <p>
* Returns the maximum value of the color gradient.
* </p>
*
* @return double maximum value of the color gradient.
*/
    public double getMaximum();

    /**
* <p>
* Returns the minimum color of the color gradient.
* </p>
*
* @return <code>java.awt.Color</code> object to the minimum color of the
* color gradient.
*/
    public Color getMinimumColor();

    /**
* <p>
* Returns the maximum color of the color gradient.
* </p>
*
* @return <code>java.awt.Color</code> object to the maximum color of the
* color gradient.
*/
    public Color getMaximumColor();

    /**
* <p>
* Returns the color in the color gradient corresponding to the value value.
* </p>
*
* @param value
* double value.
* @return <code>Color</code> object to the color in the color gradient
* corresponding to the value value.
*/
    public Color getColor(double value);
}