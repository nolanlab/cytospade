/**
* ScaleDefaults.java
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

// Import the scale package
import facs.scale.*;

/**
* <p>
* A class containing the default values for the scale parameters of the
* different file types. This class is designed to be the bottleneck for the
* scale defaults so that any changes to the defaults can be localized to this
* class.
* </p>
*
* <p>
* This is the embodiment of what Jonathan wishes the scale cofactors and ranges
* to be. So if you have problems with them, you should take it up with him.
* </p>
*
* <p>
* The default settings for each scale type are as follows:
* </p>
*
* <p>
* Linear:
* </p>
* <ul>
* <li>Min: 1</li>
* <li>Max: Scale Max Range</li>
* <li>Cofactor: 1</li>
* </ul>
*
* <p>
* Log:
* </p>
* <ul>
* <li>Min: 1</li>
* <li>Max: Scale Max Range</li>
* <li>Cofactor: 1</li>
* </ul>
*
* <p>
* Arcsinh:
* </p>
* <ul>
* <li>Min: -200</li>
* <li>Max: Scale Max Range</li>
* <li>Cofactor: 150</li>
* </ul>
*
* <p>
* The default scales are determined by whether the channel is stored in log
* format and whether the channel should be displayed in log format. If the
* channel is stored in log format, as is the case for pre-logged Calibur data,
* then the log scale is used. Otherwise, if the channel is not stored in log
* format, then whether the channel should be displayed in log format is used to
* determine the scale to use. If the channel should be displayed in log format,
* then the arcsinh scale is used. Otherwise, the linear scale is used.
* </p>
*/
public final class ScaleDefaults {
    /**
* The constants for the scale defaults
*/

    /**
* The default channel minimum to use for the linear scale
*/
    private static final double LINEAR_SCALE_MINIMUM = 1.0d;

    /**
* The default scale argument string to use for the linear scale
*/
    private static final String LINEAR_SCALE_ARGUMENT = "1";

    /**
* The default channel minimum to use for the log scale
*/
    private static final double LOG_SCALE_MINIMUM = 1.0d;

    /**
* The default scale argument string to use for the log scale
*/
    private static final String LOG_SCALE_ARGUMENT = "1";

    /**
* The default channel minimum to use for the arcsinh scale
*/
    public static final double ARCSINH_SCALE_MINIMUM_FLUOR = -200.0d;
    public static final double ARCSINH_SCALE_MINIMUM_CYTOF = -20.0d;

    /**
* The default scale argument string to use for the arcsinh scale
*/
    private static final String ARCSINH_SCALE_ARGUMENT = "150";

    /**
* <p>
* A private constructor to suppress the default constructor so the class
* cannot be instantiated.
* </p>
*/
    private ScaleDefaults() {
    }

    /**
* <p>
* Returns the default scale for the channel based on whether the channel is
* stored in log format in the boolean flag isLog and whether the channel
* should be displayed in log format in the boolean flag isLogDisplay.
* </p>
*
* @param isLog
* boolean flag indicating whether the channel is stored in log
* format.
* @param isLogDisplay
* boolean flag indicating whether the channel should be
* displayed in log format.
* @return <code>Scale</code> object to the default scale for the channel.
*/
    public static Scale getDefaultScale(boolean isLog, boolean isLogDisplay) {
        return Scaling.getScale(ScaleDefaults.getDefaultScaleFlag(isLog, isLogDisplay));
    }

    /**
* <p>
* Returns the scale type flag of the default scale for the channel based on
* whether the channel is stored in log format in the boolean flag isLog and
* whether the channel should be displayed in log format in the boolean flag
* isLogDisplay.
* </p>
*
* @param isLog
* boolean flag indicating whether the channel is stored in log
* format.
* @param isLogDisplay
* boolean flag indicating whether the channel should be
* displayed in log format.
* @return int scale type flag of the default scale for the channel.
*/
    public static int getDefaultScaleFlag(boolean isLog, boolean isLogDisplay) {
        if (isLog) {
            // If the channel is stored in log format, then return the log
            // scale.
            return Scaling.LOG;
        } else {
            // Otherwise, the channel is not stored in log format, so check
            // whether the channel should be displayed in log format.
            if (isLogDisplay) {
                // If the channel should be displayed in log format, then return
                // the arcsinh scale.
                return Scaling.ARCSINH;
            } else {
                // Otherwise, return the linear scale.
                return Scaling.LINEAR;
            }
        }
    }

    /**
* <p>
* Returns the default scale argument for the scale with scale type flag
* type.
* </p>
*
* @param type
* int scale type flag of the scale.
* @return <code>ScaleArgument</code> object to the default scale argument
* for the scale with scale type flag type.
*/
    public static ScaleArgument getDefaultScaleArgument(int type) {
            return Scaling.getScaleArgument(type, ScaleDefaults.getDefaultScaleArgumentString(type));
    }

    /**
* <p>
* Returns the default scale argument string for the scale with scale type
* flag type.
* </p>
*
* @param type
* int scale type flag of the scale.
* @return <code>String</code> default scale argument string for the scale
* with scale type flag type.
*/
    public static String getDefaultScaleArgumentString(int type) {
        // Return the default scale argument string based on the type flag
        switch (type) {
        case Scaling.LINEAR:
            return LINEAR_SCALE_ARGUMENT;
        case Scaling.LOG:
            return LOG_SCALE_ARGUMENT;
        case Scaling.ARCSINH:
            return ARCSINH_SCALE_ARGUMENT;

            // Otherwise, return null.
        default:
            return null;
        }
    }

    /**
* <p>
* Returns the default channel minimum for the scale with scale type flag
* type.
* </p>
*
* @param type
* int scale type flag of the scale.
* @return double default channel minimum for the scale with scale type flag
* type.
*/
    public static double getDefaultChannelMinimum(int type) {
        // Return the default channel minimum based on the type flag
        switch (type) {
        case Scaling.LINEAR:
            return LINEAR_SCALE_MINIMUM;
        case Scaling.LOG:
            return LOG_SCALE_MINIMUM;
        case Scaling.ARCSINH:
            return ARCSINH_SCALE_MINIMUM_FLUOR;

            // Otherwise, return 1.0d.
        default:
            return 1.0d;
        }
    }
}

