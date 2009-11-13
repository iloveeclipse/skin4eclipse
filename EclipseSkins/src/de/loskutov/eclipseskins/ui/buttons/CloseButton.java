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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * Implements a close button (X-button)
 *
 * @author wmitsuda
 * @author Andrei
 */
public class CloseButton extends AbstractButton {

    public CloseButton(Composite parent, int style, boolean isTitle) {
        super(parent, style, isTitle);
    }

    protected void paintControl(PaintEvent e, int gap, Rectangle clientArea) {
        GC gc = e.gc;

        if (active) {
            gc.setForeground(colorTextFocus);
        } else {
            gc.setForeground(colorTextNoFocus);
        }

        // Draw X
        int width = clientArea.width / 4;
        int height = clientArea.height / 4 - 1;
        int limit = (2 * width) - 1;
        for (int i = 0, j = 0; i < limit; i++, j++) {
            int xStart = clientArea.x + width + i + 1 + gap;
            int xEnd = clientArea.x + width + i + 2 + gap;
            int yLevel1 = clientArea.y + height + j + gap + 3;
            int yLevel2 = clientArea.y + 3 * height - j + gap + 3;

            gc.drawLine(xStart, yLevel1, xEnd, yLevel1);
            gc.drawLine(xStart, yLevel2, xEnd, yLevel2);
        }
    }

}
