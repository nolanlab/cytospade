/**
* ScaleArgument.java
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
* An empty interface for the scale arguments of scales.
* </p>
*
* <p>
* There are no required methods in the <code>ScaleArgument</code> interface. It
* is up to the author of the corresponding scale class to check the class of
* the scale argument passed to it and handle it accordingly.
* </p>
*/
public interface ScaleArgument {

    /**
* Return a unique identifier for this class
* @return A String representation of this class for caching
*/
    public String cacheKey();
}

