/**
* BidirectionalColorGradient.java
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
* A class for a bidirectional gradient of colors ranging from the minimum value
* to the maximum value through an inflection point.
* </p>
*
* <p>
* The bidirectional color gradient should not be created directly through its
* constructor. Instead, it should be created from its factory method because
* corresponding the left and the right unidirectional color gradients must be
* created correctly.
* </p>
*/
public final class BidirectionalColorGradient implements ColorGradient {
    /**
* The left unidirectional color gradient
*/
    private final UnidirectionalColorGradient leftGradient;

    /**
* The right unidirectional color gradient
*/
    private final UnidirectionalColorGradient rightGradient;

    /**
* The value of the inflection point
*/
    private final double inflection;

    /**
* <p>
* Creates a bidirectional color gradient using the left unidirectional
* color gradient in the <code>UnidirectionalColorGradient</code> object
* leftGradient and the right unidirectional color gradient in the
* <code>UnidirectionalColorGradient</code> object rightGradient.
* </p>
*
* <p>
* The value of the inflection point is set to the maximum of the left
* unidirectional color gradient, which should be the same as the minimum of
* the right unidirectional color gradient.
* </p>
*
* @param leftGradient
* <code>UnidirectionalColorGradient</code> object to the left
* unidirectional color gradient.
* @param rightGradient
* <code>UnidirectionalColorGradient</code> object to the right
* unidirectional color gradient.
*/
    private BidirectionalColorGradient(UnidirectionalColorGradient leftGradient, UnidirectionalColorGradient rightGradient) {
        // Set the left unidirectional color gradient
        this.leftGradient = leftGradient;

        // Set the right unidirectional color gradient
        this.rightGradient = rightGradient;

        // Set the value of the inflection point to the maximum value of the
        // left unidirectional color gradient
        this.inflection = this.leftGradient.getMaximum();
    }

    /**
* <p>
* Returns the minimum value of the color gradient.
* </p>
*
* @return double minimum value of the color gradient.
*/
    public double getMinimum() {
        // Return the minimum of the left unidirectional color gradient
        return leftGradient.getMinimum();
    }

    /**
* <p>
* Returns the maximum value of the color gradient.
* </p>
*
* @return double maximum value of the color gradient.
*/
    public double getMaximum() {
        // Return the maximum of the right unidirectional color gradient
        return rightGradient.getMaximum();
    }

    /**
* <p>
* Returns the value of the inflection point of the bidirectional color
* gradient.
* </p>
*
* @return double value of the inflection point of the bidirectional color
* gradient.
*/
    double getInflection() {
        return inflection;
    }

    /**
* <p>
* Returns the minimum color of the color gradient.
* </p>
*
* @return <code>java.awt.Color</code> object to the minimum color of the
* color gradient.
*/
    public Color getMinimumColor() {
        // Return the minimum color of the left unidirectional color gradient
        return leftGradient.getMinimumColor();
    }

    /**
* <p>
* Returns the maximum color of the color gradient.
* </p>
*
* @return <code>java.awt.Color</code> object to the maximum color of the
* color gradient.
*/
    public Color getMaximumColor() {
        // Return the maximum color of the right unidirectional color gradient
        return rightGradient.getMaximumColor();
    }

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
    public Color getColor(double value) {
        if (value <= inflection) {
            // If the value is less than or equal to the value of the inflection
            // point, then use the left unidirectional color gradient to return
            // the color.
            return leftGradient.getColor(value);
        } else {
            // Otherwise, the value is greater than the value of the inflection
            // point, so use the right unidirectional color gradient to return
            // the color.
            return rightGradient.getColor(value);
        }
    }

    /**
* Factory methods
*/

    /**
* <p>
* Returns the bidirectional color gradient of the color set indicated by
* the constant flag of the color set colorSet.
* </p>
*
* @param colorSet
* int constant flag of the color set.
* @param min
* double minimum value of the color gradient.
* @param inflection
* double value of the inflection point of the color gradient.
* @param max
* double maximum value of the color gradient.
* @return <code>BidirectionalColorGradient</code> object to the
* bidirectional color gradient of the color set indicated by the
* constant flag of the color set colorSet.
*/
    public static BidirectionalColorGradient getBidirectionalColorGradient(int colorSet, double min, double inflection, double max) {
        // Get the left unidirectional color gradient, which is a maximum
        // unidirectional color gradient
        UnidirectionalColorGradient leftGradient = UnidirectionalColorGradient.getUnidirectionalColorGradient(colorSet, true, min, inflection);

        // Get the right unidirectional color gradient, which is a minimum
        // unidirectional color gradient
        UnidirectionalColorGradient rightGradient = UnidirectionalColorGradient.getUnidirectionalColorGradient(colorSet, false, inflection, max);

        if ((leftGradient == null) || (rightGradient == null)) {
            // If the left unidirectional color gradient or the right
            // unidirectional color gradient is null, then return null.
            return null;
        } else {
            // Otherwise, the left unidirectional color gradient and the right
            // unidirectional color gradient are not null, so create the
            // bidirectional color gradient and return it.
            return new BidirectionalColorGradient(leftGradient, rightGradient);
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
        // Create a bidirectional color gradient
        BidirectionalColorGradient gradient = BidirectionalColorGradient
                .getBidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT, 0.0d, 5.0d, 10.0d);

        Color color;

        // Loop through the array of values
        for (int i = 0; i <= 10; i++) {
            // Get the current color in the color gradient
            color = gradient.getColor((double) i);

            System.out.print(i);
            System.out.print(": (");
            System.out.print(color.getRed());
            System.out.print(", ");
            System.out.print(color.getGreen());
            System.out.print(", ");
            System.out.print(color.getBlue());
            System.out.println(")");
        }
    }
}