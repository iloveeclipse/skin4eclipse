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

import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;

/**
 * Implements a close button (X-button)
 *
 * @author wmitsuda
 * @author Andrei
 */
public class PartListButton extends AbstractButton {
    private Color colorClosedPartList;
    private Color colorOpenedPartList;
    private final boolean closedPartsList;
    private final VSStackPresentation presentation;

    public PartListButton(Composite parent, VSStackPresentation presentation,
            int style, boolean closedPartsList) {
        super(parent, style, true);
        this.presentation = presentation;
        this.closedPartsList = closedPartsList;
    }

    protected void paintControl(PaintEvent e, int gap, Rectangle clientArea) {
        GC gc = e.gc;
        if (active) {
            gc.setForeground(colorTextFocus);
        } else {
            gc.setForeground(colorTextNoFocus);
        }

        if(closedPartsList){
            if(presentation.getClosedPartsCount() > 0){
                gc.setBackground(colorClosedPartList);
            } else {
                gc.setBackground(colorTitleNoFocus);
            }
        } else {
            if(presentation.getPartsCount() > 0){
                gc.setBackground(colorOpenedPartList);
            } else {
                gc.setBackground(colorTitleNoFocus);
            }
        }

        // draw triangle
        int height = 5;
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

    protected void initColorsAndFonts(ThemeWrapper theme) {
        super.initColorsAndFonts(theme);
        colorClosedPartList = theme.getColor(
                ThemeConstants.CLOSED_PART_LIST_BTN_COLOR);
        colorOpenedPartList = theme.getColor(
                ThemeConstants.PART_LIST_BTN_COLOR);
    }

    protected void mouseOverChanged(boolean hasMouse) {
        if(hasMouse){
            if(closedPartsList){
                int size = presentation.getClosedPartsCount();
                setToolTipText("Closed editors: " + size);
            } else {
                int size = presentation.getPartsCount();
                setToolTipText("Opened editors: " + size);
            }
        }
        super.mouseOverChanged(hasMouse);
    }
}
