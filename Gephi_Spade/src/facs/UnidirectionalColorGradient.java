/**
* UnidirectionalColorGradient.java
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
* A class for a unidirectional gradient of colors ranging from the minimum
* value to the maximum value.
* </p>
*
* <p>
* I resisted rewriting this class for a long time in an attempt to avoid
* influencing its development, but the previous incarnation of this class just
* didn't have the needed functionality. Most atrocious was the fact that the
* previous incarnation was built around an array of colors. This created a
* number of rough edges as there were no checks on the array of colors.
* </p>
*
* <p>
* In reinventing the color gradient, I focused on creating a unidirectional
* color gradient based on two parameters. The two parameters are, obviously,
* the minimum and the maximum. In order to actually create the color gradient,
* the associated colors for each of these parameters are also needed, so the
* color gradient is really based on four parameters. I kept the array of colors
* support so the class can do a fancier gradient (that range over more than two
* colors) and so the class is compatible with legacy uses of the color gradient
* (most notably in 2D plots).
* </p>
*/
public final class UnidirectionalColorGradient implements ColorGradient {
    /**
* The default color
*/
    private static final Color DEFAULT_COLOR = Color.black;

    /**
* The available gradient colors
*/

    /**
* The blue to yellow gradient
*/

    /**
* The blue to black colors
*/
    private static final Color[] BLUE_TO_BLACK = { new Color(200, 200, 255), new Color(0, 0, 255), new Color(0, 0, 0) };

    /**
* The black to yellow colors
*/
    private static final Color[] BLACK_TO_YELLOW = { new Color(0, 0, 0), new Color(255, 255, 0), new Color(255, 255, 200) };

    /**
* The greyscale gradient
*/

    /**
* The grey black colors
*/
    private static final Color[] GREY_TO_BLACK = { new Color(247, 247, 247), new Color(123, 123, 123), new Color(0, 0, 0) };

    /**
* The black to yellow colors
*/
    private static final Color[] BLACK_TO_GREY = { new Color(0, 0, 0), new Color(123, 123, 123), new Color(247, 247, 247) };

    /**
* The green to red gradient (aka, the standard microarray colors)
*/

    /**
* The green to black colors
*/
    private static final Color[] GREEN_TO_BLACK = { new Color(200, 255, 200), new Color(0, 255, 0), new Color(0, 0, 0) };

    /**
* The black to red colors
*/
    private static final Color[] BLACK_TO_RED = { new Color(0, 0, 0), new Color(255, 0, 0), new Color(255, 200, 200) };

    /**
* The minimum value
*/
    private final double min;

    /**
* The maximum value
*/
    private final double max;

    /**
* Two colors based gradient
*/

    /**
* The minimum color
*/
    private Color minColor;

    /**
* The maximum color
*/
    private Color maxColor;

    /**
* The boolean flag indicating whether the minimum and the maximum colors
* are the same
*/
    private final boolean sameP;

    /**
* Array of colors based gradient
*/

    /**
* The number of colors in the array of colors
*/
    private int colorCount;

    /**
* The array of colors to use in the color gradient
*/
    private Color[] colors;

    /**
* The array of values corresponding to the array of colors to use in the
* color gradient
*/
    private double[] values;

    /**
* <p>
* Creates a unidirectional color gradient ranging from the minimum value
* min to the maximum value max with the color in the
* <code>java.awt.Color</code> object minColor as the color for the minimum
* and the color in the <code>java.awt.Color</code> object maxColor as the
* color for the maximum.
* </p>
*
* @param min
* double minimum value of the color gradient.
* @param max
* double maximum value of the color gradient.
* @param minColor
* <code>java.awt.Color</code> object to the minimum color of the
* color gradient.
* @param maxColor
* <code>java.awt.Color</code> object to the maximum color of the
* color gradient.
*/
    UnidirectionalColorGradient(double min, double max, Color minColor, Color maxColor) {
        if (min > max) {
            // If the minimum value is greater than the maximum value, then swap
            // the minimum and the maximum.
            this.min = max;
            this.max = min;
            this.minColor = maxColor;
            this.maxColor = minColor;
        } else {
            // Otherwise, the minimum value is less than or equal to the maximum
            // value, so use the minimum and the maximum as is.
            this.min = min;
            this.max = max;
            this.minColor = minColor;
            this.maxColor = maxColor;
        }

        if (this.minColor == null) {
            // If the minimum color is null, then set it to the default color.
            this.minColor = DEFAULT_COLOR;
        }

        if (this.maxColor == null) {
            // If the maximum color is null, then set it to the default color.
            this.maxColor = DEFAULT_COLOR;
        }

        // Set whether the minimum and the maximum colors are the same to
        // whether the colors are actually the same
        sameP = this.minColor.equals(this.maxColor);

        // Initialize the array of colors to null
        colorCount = -1;
        colors = null;
        values = null;
    }

    /**
* <p>
* Creates a unidirectional color gradient ranging from the minimum value
* min to the maximum value max with the colors in the array of colors in
* the array of <code>java.awt.Color</code> objects colors.
* </p>
*
* @param min
* double minimum value of the color gradient.
* @param max
* double maximum value of the color gradient.
* @param colors
* array of <code>java.awt.Color</code> objects containing the
* array of colors to use in the color gradient.
*/
    UnidirectionalColorGradient(double min, double max, Color[] colors) {
        // Initialize whether the minimum and the maximum values are flipped to
        // false
        boolean flippedP = false;

        if (min > max) {
            // If the minimum value is greater than the maximum value, then swap
            // the minimum and the maximum.
            this.min = max;
            this.max = min;

            // Set whether the minimum and the maximum values are flipped to
            // true
            flippedP = true;
        } else {
            // Otherwise, the minimum value is less than or equal to the maximum
            // value, so use the minimum and the maximum as is.
            this.min = min;
            this.max = max;
        }

        if (colors == null) {
            // If the array of colors is null, then do not use the array of
            // colors.

            // Set the minimum color and the maximum color to the default color
            this.minColor = DEFAULT_COLOR;
            this.maxColor = DEFAULT_COLOR;

            // Set whether the minimum and the maximum colors are the same to
            // true
            sameP = true;

            // Initialize the array of colors to null
            colorCount = -1;
            colors = null;
            values = null;
        } else {
            // Otherwise, the array of colors is not null, so copy the array of
            // colors.

            // Get the number of colors in the array of colors
            colorCount = colors.length;

            if (colorCount > 2) {
                // If the number of colors in the array of colors is greater
                // than 2, then copy the array of colors and calculate the
                // corresponding values.

                // Allocate the array of colors
                this.colors = new Color[colorCount];

                // Copy the array of colors
                System.arraycopy(colors, 0, this.colors, 0, colorCount);

                if (flippedP) {
                    // If the minimum and the maximum values are flipped, then
                    // flip the array of colors.
                    Color temp;
                    int index;

                    // Loop through the first half of the array of colors
                    // swapping the colors with the second half
                    for (int i = 0; i <= (colorCount / 2 - 1); i++) {
                        // Calculate the index of the corresponding color in the
                        // second half
                        index = colorCount - 1 - i;

                        // Swap the current color and its corresponding color
                        temp = this.colors[i];
                        this.colors[i] = this.colors[index];
                        this.colors[index] = temp;
                    }
                }

                // Allocate the array of values
                values = new double[colorCount];

                // Set the first element to the minimum value
                values[0] = this.min;

                // Set the last element to the maximum value
                values[colorCount - 1] = this.max;

                // Calculate the increment between colors
                double increment = (this.max - this.min) / (double) (colorCount - 1);

                // Loop through the rest of the array of values
                for (int i = 1; i < (colorCount - 1); i++) {
                    // Set the current value as i increments from the minimum
                    // value
                    values[i] = this.min + ((double) i * increment);
                }

                // Set whether the minimum and the maximum colors are the same
                // to false
                sameP = false;
            } else {
                // Otherwise, the number of colors in the array of colors is
                // equal to 0, 1, or 2, so do not use the array of colors.

                if (colorCount == 2) {
                    // If there are only two colors in the array of colors, then
                    // set the minimum color to the first color and the maximum
                    // color to the second color or vice versa.
                    if (flippedP) {
                        // If the minimum and the maximum values are flipped,
                        // then set the minimum color to the second color and
                        // the maximum color to the first color.
                        this.minColor = colors[1];
                        this.maxColor = colors[0];
                    } else {
                        // Otherwise, the minimum and the maximum values are not
                        // flipped, so set the minimum color to the first color
                        // and the maximum color to the second color.
                        this.minColor = colors[0];
                        this.maxColor = colors[1];
                    }

                    // Set whether the minimum and the maximum colors are the
                    // same to whether the colors are actually the same
                    sameP = this.minColor.equals(this.maxColor);
                } else if (colorCount == 1) {
                    // If there is only one color in the array of colors, then
                    // set the minimum color and the maximum color to the one
                    // color.
                    this.minColor = colors[0];
                    this.maxColor = colors[0];

                    // Set whether the minimum and the maximum colors are the
                    // same to true
                    sameP = true;
                } else {
                    // Otherwise, the array of colors is empty, so set the
                    // minimum color and the maximum color to the default color.
                    this.minColor = DEFAULT_COLOR;
                    this.maxColor = DEFAULT_COLOR;

                    // Set whether the minimum and the maximum colors are the
                    // same to true
                    sameP = true;
                }

                // Initialize the array of colors to null
                colorCount = -1;
                colors = null;
                values = null;
            }
        }
    }

    /**
* <p>
* Returns the minimum value of the color gradient.
* </p>
*
* @return double minimum value of the color gradient.
*/
    public double getMinimum() {
        return min;
    }

    /**
* <p>
* Returns the maximum value of the color gradient.
* </p>
*
* @return double maximum value of the color gradient.
*/
    public double getMaximum() {
        return max;
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
        if (colorCount <= -1) {
            // If the number of colors in the array of colors is less than or
            // equal to -1, then the color gradient does not use the array of
            // colors.
            return minColor;
        } else if (colorCount > 2) {
            // If the number of colors in the array of colors is greater than 2,
            // then the color gradient uses the array of colors.
            return colors[0];
        }

        // Otherwise, return the default color.
        return DEFAULT_COLOR;
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
        if (colorCount <= -1) {
            // If the number of colors in the array of colors is less than or
            // equal to -1, then the color gradient does not use the array of
            // colors.
            return maxColor;
        } else if (colorCount > 2) {
            // If the number of colors in the array of colors is greater than 2,
            // then the color gradient uses the array of colors.
            return colors[colorCount - 1];
        }

        // Otherwise, return the default color.
        return DEFAULT_COLOR;
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
        if (colorCount <= -1) {
            // If the number of colors in the array of colors is less than or
            // equal to -1, then the color gradient does not use the array of
            // colors.

            if (sameP) {
                // If the minimum and the maximum colors are the same, then
                // return the minimum color.
                return minColor;
            }

            if (value <= min) {
                // If the value is less than or equal to the minimum value, then
                // return the minimum color.
                return minColor;
            }

            if (value >= max) {
                // If the value is greater than or equal to the maximum value,
                // then return the maximum color.
                return maxColor;
            }

            // Return the color corresponding to the value value in the color
            // gradient
            return interpolate(minColor, maxColor, (value - min) / (max - min));
        } else if (colorCount > 2) {
            // If the number of colors in the array of colors is greater than 2,
            // then the color gradient uses the array of colors.

            if (value <= min) {
                // If the value is less than or equal to the minimum value, then
                // return the first element in the array of colors.
                return colors[0];
            }

            if (value >= max) {
                // If the value is greater than or equal to the maximum value,
                // then return the last element in the array of colors.
                return colors[colorCount - 1];
            }

            // Loop through the array of values
            // We always look downward towards the minimum when looping.
            for (int i = 1; i < colorCount; i++) {
                if (value == values[i]) {
                    // If the value is equal to the current value, then return
                    // the corresponding color.
                    return colors[i];
                } else if (value < values[i]) {
                    // If the value is less than the current value, then it is
                    // between the previous value and the current value, so
                    // interpolate the color.
                    return interpolate(colors[i - 1], colors[i], (value - values[i - 1]) / (values[i] - values[i - 1]));
                }
            }
        }

        // At this point, the color in the color gradient corresponding to the
        // value was not found.
        // Obviously, this is impossible since all the if statements catch the
        // different cases in the range, so return the default color.
        return DEFAULT_COLOR;
    }

    /**
* Static helper methods
*/

    /**
* <p>
* Returns a color that is the fraction fraction between the low color in
* the <code>java.awt.Color</code> object low and the high color in the
* <code>java.awt.Color</code> object high.
* </p>
*
* @param low
* <code>java.awt.Color</code> object to the low color.
* @param high
* <code>java.awt.Color</code> object to the high color.
* @param fraction
* double fraction of the distance between the two colors for
* which to return a color.
* @return <code>java.awt.Color</code> object to the color that is the
* fraction fraction between the low color in the
* <code>java.awt.Color</code> object low and the high color in the
* <code>java.awt.Color</code> object high.
*/
    private static Color interpolate(Color low, Color high, double fraction) {
        if (Double.isNaN(fraction)) {
            // If the fraction is not a number, then return the default color.
            return DEFAULT_COLOR;
        }

        if (low == null) {
            // If the low color is null, then set it to the default color.
            low = DEFAULT_COLOR;
        }

        if (high == null) {
            // If the high color is null, then set it to the default color.
            high = DEFAULT_COLOR;
        }

        if (low.equals(high)) {
            // If the low color is equal to the high color, then return the low
            // color.
            return low;
        }

        // At this point, we actually have to interpolate between the two
        // colors.

        // Cache the red, green, and blue components of the low color
        int lowR = low.getRed();
        int lowG = low.getGreen();
        int lowB = low.getBlue();

        // Calculate the red, green, and blue components of the new color
        int r = lowR + (int) (fraction * (double) (high.getRed() - lowR));
        int g = lowG + (int) (fraction * (double) (high.getGreen() - lowG));
        int b = lowB + (int) (fraction * (double) (high.getBlue() - lowB));

        if (r < 0) {
            // If the red component is less than 0, then set it to 0.
            r = 0;
        } else if (r > 255) {
            // If the red component is greater than 255, then set it to 255.
            r = 255;
        }

        if (g < 0) {
            // If the green component is less than 0, then set it to 0.
            g = 0;
        } else if (g > 255) {
            // If the green component is greater than 255, then set it to 255.
            g = 255;
        }

        if (b < 0) {
            // If the blue component is less than 0, then set it to 0.
            b = 0;
        } else if (b > 255) {
            // If the blue component is greater than 255, then set it to 255.
            b = 255;
        }

        // Create a new color based on the red, green, and blue components and
        // return it
        return new Color(r, g, b);
    }

    /**
* Factory methods
*/

    /**
* <p>
* Returns the minimum or the maximum unidirectional color gradient of the
* color set indicated by the color set constant flag colorSet.
* </p>
*
* <p>
* The maxP boolean flag controls whether the maximum unidirectional color
* gradient is returned.
* </p>
*
* @param colorSet
* int constant flag of the color set.
* @param maxP
* boolean flag indicating whether to return the maximum
* unidirectional color gradient.
* @param min
* double minimum value of the color gradient.
* @param max
* double maximum value of the color gradient.
* @return <code>UnidirectionalColorGradient</code> object to the minimum or
* the maximum unidirectional color gradient of the color set
* indicated by the color set constant flag colorSet.
*/
    public static UnidirectionalColorGradient getUnidirectionalColorGradient(int colorSet, boolean maxP, double min, double max) {
        if (colorSet == ColorGradient.BLUE_TO_YELLOW_GRADIENT) {
            // If the color set is blue to yellow, then return the minimum or
            // the maximum unidirectional blue to yellow color gradient.
            if (maxP) {
                // If the maximum unidirectional color gradient should be
                // returned, then return it.
                return new UnidirectionalColorGradient(min, max, BLUE_TO_BLACK);
            } else {
                // Otherwise, the minimum unidirectional color gradient should
                // be returned, so return it.
                return new UnidirectionalColorGradient(min, max, BLACK_TO_YELLOW);
            }
        } else if (colorSet == ColorGradient.GREEN_TO_RED_GRADIENT) {
            // If the color set is green to red, then return the minimum or the
            // maximum unidirectional green to red color gradient.
            if (maxP) {
                // If the maximum unidirectional color gradient should be
                // returned, then return it.
                return new UnidirectionalColorGradient(min, max, GREEN_TO_BLACK);
            } else {
                // Otherwise, the minimum unidirectional color gradient should
                // be returned, so return it.
                return new UnidirectionalColorGradient(min, max, BLACK_TO_RED);
            }
        } else if (colorSet == ColorGradient.GREYSCALE_GRADIENT) {
            // If the color set is green to red, then return the minimum or the
            // maximum unidirectional green to red color gradient.
            if (maxP) {
                // If the maximum unidirectional color gradient should be
                // returned, then return it.
                return new UnidirectionalColorGradient(min, max, GREY_TO_BLACK);
            } else {
                // Otherwise, the minimum unidirectional color gradient should
                // be returned, so return it.
                return new UnidirectionalColorGradient(min, max, BLACK_TO_GREY);
            }
        }

        // Otherwise, the color set of the unidirectional color gradient is not
        // recognized, so return null.
        return null;
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
        // Create a color gradient on the range [0, 10]
        // UnidirectionalColorGradient gradient = new
        // UnidirectionalColorGradient(0.0d, 10.0d, new Color(0, 0, 0), new
        // Color(200, 100, 50));

        // Create a color gradient on the range [5, 10]
        UnidirectionalColorGradient gradient = UnidirectionalColorGradient.getUnidirectionalColorGradient(ColorGradient.BLUE_TO_YELLOW_GRADIENT, false, 5.0d,
                10.d);

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

