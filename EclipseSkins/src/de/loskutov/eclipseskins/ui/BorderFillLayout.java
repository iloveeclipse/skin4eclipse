/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public final class BorderFillLayout extends Layout {

    /** The border widths. */
    final int fBorderSize;

    /**
     * Creates a fill layout with a border.
     */
    public BorderFillLayout(int borderSize) {
        if (borderSize < 0) {
            throw new IllegalArgumentException();
        }
        fBorderSize = borderSize;
    }

    /**
     * Returns the border size.
     */
    public int getBorderSize() {
        return fBorderSize;
    }

    /*
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
     *      int, int, boolean)
     */
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {

        Control[] children = composite.getChildren();
        Point minSize = new Point(0, 0);

        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                Point size = children[i].computeSize(wHint, hHint,
                        flushCache);
                minSize.x = Math.max(minSize.x, size.x);
                minSize.y = Math.max(minSize.y, size.y);
            }
        }

        minSize.x += fBorderSize * 2 + AbstractPartListControl.RIGHT_MARGIN;
        minSize.y += fBorderSize * 2;

        return minSize;
    }

    /*
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
     *      boolean)
     */
    protected void layout(Composite composite, boolean flushCache) {

        Control[] children = composite.getChildren();
        Rectangle clientArea = composite.getClientArea();
        Point minSize = new Point(clientArea.width, clientArea.height);

        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                child.setSize(minSize.x - fBorderSize * 2, minSize.y
                        - fBorderSize * 2);
                child.setLocation(fBorderSize, fBorderSize);
            }
        }
    }
}