/**
* ArcsinhScaleArgument.java
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
* The scale argument of the arcsinh scale.
* </p>
*
* <p>
* Basically, the class is just a wrapper around the compression width.
* </p>
*
* <p>
* If there is a problem with parsing the compression width, then the
* compression width is set to 1. This is because the compression width is used
* to divide x by the inverse hyperbolic sine scale. Thus, setting the
* compression width to 1 would not change x, mathematically, making it a good
* default value.
* </p>
*/
public class ArcsinhScaleArgument implements ScaleArgument {
    /**
* The compression width
*/
    private final int compressionWidth;

    /**
* <p>
* Creates a <code>ArcsinhScaleArgument</code> object using the compression
* width compressionWidth.
* </p>
*
* @param compressionWidth
* <code>String</code> compression width.
*/
    ArcsinhScaleArgument(String compressionWidth) {
        if ((compressionWidth == null) || (compressionWidth.length() <= 0)) {
            // If the compression width is null or empty, then set the
            // compression width to 1.
            this.compressionWidth = 1;
        } else {
            // Otherwise, the compression width is not null or empty, so try to
            // parse the compression width.

            // Initialize the width to 1
            int width = 1;

            try {
                // Try to parse the compression width from the string
                width = Integer.parseInt(compressionWidth);
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurred, then set the width to 1.
                width = 1;
            }

            // Set the compression width to the width
            this.compressionWidth = width;
        }
    }

    /**
* <p>
* Returns the compression width.
* </p>
*
* @return int compression width.
*/
    public int getCompressionWidth() {
        return compressionWidth;
    }

    public String cacheKey() {
        return "ArcsinhScaleArgument("+compressionWidth+")";
    }
}