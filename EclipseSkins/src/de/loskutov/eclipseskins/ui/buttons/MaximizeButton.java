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
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.ui.UIUtils;

/**
 * Implements a maximize button
 *
 * @author wmitsuda
 * @author Andrei
 */
public class MaximizeButton extends AbstractButton {

    private IStackPresentationSite site;

    public MaximizeButton(Composite parent, int style, boolean isTitle,
            IStackPresentationSite site) {
        super(parent, style, isTitle);
        this.site = site;
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

        if (site.getState() != IStackPresentationSite.STATE_MAXIMIZED) {
            // Draw maximize icon
            int width = clientArea.width / 4;
            int height = clientArea.height / 4;
            int xCoord = clientArea.x + width + 1 + gap;
            int yCoord = clientArea.y + height + 1 + gap;
            gc.fillRectangle(xCoord, yCoord, width * 2, 2);
            gc.drawRectangle(xCoord, yCoord, width * 2, height * 2);
        } else {
            // Draw restore icon
            UIUtils.drawRestoreIcon(e, gap, clientArea);
        }
    }

    public void dispose() {
        site = null;
        super.dispose();
    }
}
