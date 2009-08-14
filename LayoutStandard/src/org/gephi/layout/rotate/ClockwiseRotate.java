/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.layout.rotate;

import javax.swing.Icon;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class ClockwiseRotate implements LayoutBuilder {

    public Layout buildLayout() {
        return new RotateLayout(this, -90);
    }

    public String getName() {
        return NbBundle.getMessage(ClockwiseRotate.class, "clockwise_name");
    }

    public String getDescription() {
        return NbBundle.getMessage(ClockwiseRotate.class, "clockwise_description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}