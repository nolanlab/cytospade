/**
* Scaling.java
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
* The only entry point for the scales.
* </p>
*
* <p>
* It provides a factory method getScale for obtaining each of the available
* scales using the scale type flag of the scale.
* </p>
*
* <p>
* It also provides a factory method getScaleArgument for obtaining the
* corresponding scale argument using the scale type flag of the scale and a
* <code>String</code> argument. The factory method parses the argument and
* returns the corresponding <code>ScaleArgument</code> object.
* </p>
*
* <p>
* Since this is the only entry point, to add a new scale, a new scale type flag
* would need to be added and all the methods will need to be changed to support
* the new scale. The most important changes are in the getScaleFlags method and
* the getScale method so that the new scale type flag is returned (so that the
* client knows about the new scale) and the appropriate scale is returned. If
* the new scale takes a scale argument, then the scale type flag must also be
* added to the getScaleArgument method. Changes to the other methods are
* encouraged so that the clients can display an appropriate label and
* description for the new scale. Any new scale should be a singleton and
* immutable. If none of this makes sense to you, you should probably not try to
* add a new scale.
* </p>
*/
public final class Scaling {
    /**
* Scale type constant flags
*/

    /**
* The constant flag for the linear scale
*/
    public static final int LINEAR = 1;

    /**
* The constant flag for the log scale
*/
    public static final int LOG = 2;

    /**
* The constant flag for the ln scale
*/
    public static final int LN = 3;

    /**
* The constant flag for the arcsinh scale
*/
    public static final int ARCSINH = 4;

    /**
* The constant flag for the biexp scale
*/
    public static final int BIEXP = 5;

    /**
* The number of scales
*/
    private static final int SCALE_COUNT = 3;

    /**
* The scales
*/

    /**
* The linear scale
*/
    private static final Scale LINEAR_SCALE = new LinearScale();

    /**
* The log scale
*/
    private static final Scale LOG_SCALE = new LogScale();

    /**
* The ln scale
*/
    private static final Scale LN_SCALE = new LnScale();

    /**
* The arcsinh scale
*/
    private static final Scale ARCSINH_SCALE = new ArcsinhScale();

    /**
* <p>
* A private constructor to suppress the default constructor so the class
* cannot be instantiated.
* </p>
*/
    private Scaling() {
    }

    /**
* <p>
* Returns an array of scale type flags of the available scales.
* </p>
*
* @return int array of scale type flags of the available scales.
*/
    public static int[] getScaleFlags() {
        // Allocate an array of scale type flags of the available scales
        int[] flags = new int[SCALE_COUNT];

        // Populate the array of scale type flags with the scale type flags of
        // the available scales
        flags[0] = Scaling.LINEAR;
        flags[1] = Scaling.LOG;
        flags[2] = Scaling.ARCSINH;

        // Return the array of scale type flags of the available scales
        return flags;
    }

    /**
* <p>
* Returns the label of the scale indicated by the scale type flag type.
* </p>
*
* @param type
* int scale type flag of the scale whose label to return.
* @return <code>String</code> label of the scale indicated by the scale
* type flag in type.
*/
    public static String getScaleLabel(int type) {
        // Return the correct label based on the scale type flag
        switch (type) {
        case LINEAR:
            return "Linear";
        case LOG:
            return "Log";
        case LN:
            return "Ln";
        case ARCSINH:
            return "Arcsinh";

            // Otherwise, the scale type flag of the scale is not recognized, so
            // return "Unknown".
        default:
            return "Unknown";
        }
    }

    /**
* <p>
* Returns the description of the scale indicated by the scale type flag
* type.
* </p>
*
* @param type
* int scale type flag of the scale whose description to return.
* @return <code>String</code> description of the scale indicated by the
* scale type flag in type.
*/
    public static String getScaleDescription(int type) {
        // Return the correct description based on the scale type flag
        switch (type) {
        case LINEAR:
            return "A linear scale.";
        case LOG:
            return "A log scale of base 10.";
        case LN:
            return "A log scale of base e (Euler's constant).";
        case ARCSINH:
            return "An arcsinh scale.";

            // Otherwise, the scale type flag of the scale is not recognized, so
            // return an error.
        default:
            return "Error: An error has occurred.";
        }
    }

    /**
* <p>
* Returns the scale indicated by the scale type flag type.
* </p>
*
* @param type
* int scale type flag of the scale to return.
* @return <code>Scale</code> object to the scale indicated by the scale
* type flag type.
*/
    public static Scale getScale(int type) {
        // Return the correct scale based on the type flag
        switch (type) {
        case LINEAR:
            return LINEAR_SCALE;
        case LOG:
            return LOG_SCALE;
        case LN:
            return LN_SCALE;
        case ARCSINH:
            return ARCSINH_SCALE;

            // Otherwise, the scale type flag of the scale is not recognized, so
            // return return null.
        default:
            return null;
        }
    }

    /**
* <p>
* Returns the scale argument for the scale indicated by the scale type flag
* type and the <code>String</code> argument.
* </p>
*
* @param type
* int scale type flag of the scale whose scale argument to
* return.
* @param argument
* <code>String</code> argument based on which to create the
* scale argument.
* @return <code>ScaleArgument</code> object to the scale argument for the
* scale indicated by the scale type flag type and the
* <code>String</code> argument.
*/
    public static ScaleArgument getScaleArgument(int type, String argument) {
        if ((argument == null) || (argument.length() <= 0)) {
            // If the argument is null or empty, then quit.
            return null;
        }

        // Return the correct scale argument based on the scale type flag
        switch (type) {
        case ARCSINH:
            return new ArcsinhScaleArgument(argument);

            // Otherwise, the scale type flag of the scale is not recognized, so
            // return return null.
        default:
            return null;
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
        // Get the array of scale type flags of the available scales
        int[] flags = getScaleFlags();

        int flag;

        // Loop through the array of scale type flags of the available scales
        for (int i = 0; i < flags.length; i++) {
            // Get the current scale type flag
            flag = flags[i];

            // Print out the scale type flag
            System.out.print(Integer.toString(flag));
            System.out.print("\t");
            System.out.print(Scaling.getScaleLabel(flag));
            System.out.print("\t");
            System.out.print(Scaling.getScaleDescription(flag));
            System.out.println();
        }

        // Get the scale to test
        Scale scale = getScale(Scaling.ARCSINH);

        // Get the scale argument to test
        ScaleArgument scaleArg = getScaleArgument(Scaling.ARCSINH, "50");

        // Run some values through the scale
        System.out.println("scale(1) = " + scale.getValue(1));
        System.out.println("scale(1) = " + scale.getValue(1, scaleArg));
        System.out.println("scale(10) = " + scale.getValue(10));
        System.out.println("scale(10) = " + scale.getValue(10, scaleArg));
        System.out.println("scale(10d) = " + scale.getValue(10d));
        System.out.println("scale(10d) = " + scale.getValue(10d, scaleArg));
        System.out.println("scale(100d) = " + scale.getValue(100d));
        System.out.println("scale(100d) = " + scale.getValue(100d, scaleArg));
        System.out.println("scale(1000d) = " + scale.getValue(1000d));
        System.out.println("scale(1000d) = " + scale.getValue(1000d, scaleArg));
        System.out.println("scale(10000d) = " + scale.getValue(10000d));
        System.out.println("scale(10000d) = " + scale.getValue(10000d, scaleArg));

        // Get the scale argument to test
        scaleArg = getScaleArgument(Scaling.ARCSINH, "500");

        System.out.println("With argument at 500:");

        // Run some values through the scale
        System.out.println("scale(1) = " + scale.getValue(1));
        System.out.println("scale(1) = " + scale.getValue(1, scaleArg));
        System.out.println("scale(10) = " + scale.getValue(10));
        System.out.println("scale(10) = " + scale.getValue(10, scaleArg));
        System.out.println("scale(10d) = " + scale.getValue(10d));
        System.out.println("scale(10d) = " + scale.getValue(10d, scaleArg));
        System.out.println("scale(100d) = " + scale.getValue(100d));
        System.out.println("scale(100d) = " + scale.getValue(100d, scaleArg));
        System.out.println("scale(1000d) = " + scale.getValue(1000d));
        System.out.println("scale(1000d) = " + scale.getValue(1000d, scaleArg));
        System.out.println("scale(10000d) = " + scale.getValue(10000d));
        System.out.println("scale(10000d) = " + scale.getValue(10000d, scaleArg));
    }
}