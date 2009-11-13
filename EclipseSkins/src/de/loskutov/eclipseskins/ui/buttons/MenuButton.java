/*******************************************************************************
 * Copyright (c) 2005 Willian Mitsuda.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Willian Mitsuda - initial API and implementation
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.ui.buttons;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * Implements a "show menu" button (down arrow)
 *
 * @author wmitsuda
 * @author Andrei
 */
public class MenuButton extends AbstractButton {

    public MenuButton(Composite parent, int style) {
        super(parent, style, true);
        setToolTipText("Show menu");
    }

    protected void paintControl(PaintEvent e, int gap, Rectangle clientArea) {
        GC gc = e.gc;
        Color color;
        if (active) {
            color = colorTextFocus;
        } else {
            color = colorTextNoFocus;
        }
        // both foreground and background the same
        gc.setBackground(color);
        gc.setForeground(color);

        // Draw arrow

        int height = 4;
        int width = height * 2;
        int padX = (clientArea.width - width) / 2;
        int padY = (clientArea.height - height) / 2;
        int yTopCoord = clientArea.y + padY;
        int[] points = new int[] { clientArea.x + padX,
                yTopCoord,
                clientArea.x + padX + width,
                yTopCoord,
                clientArea.x + padX + width / 2,
                yTopCoord + height };

        gc.fillPolygon(points);
        gc.drawPolygon(points);
    }

}
