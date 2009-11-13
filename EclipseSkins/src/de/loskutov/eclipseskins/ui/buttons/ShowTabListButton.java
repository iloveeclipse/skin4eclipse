/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.ui.buttons;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.ui.PartListMenu;
import de.loskutov.eclipseskins.ui.TabArea;

/**
 * Implements a "show tab list" button (asterisk with number of hidden tabs)
 *
 * @author Andrei
 */
public class ShowTabListButton extends AbstractButton implements Listener {

    protected TabArea tabArea;
    protected PartListMenu menu;
    private int height;
    private int width;
    private Point size;
    private int hiddenTabsCount;

    public ShowTabListButton(Composite parent, int style, TabArea tabArea) {
        super(parent, style, true);
        this.tabArea = tabArea;
        tabArea.addLayoutListener(this);
        menu = new PartListMenu(tabArea, false, false);
        setToolTipText("Show tab list");
        addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                if(menu != null){
                    Point toDisplay = toDisplay(getLocation());
                    toDisplay.y += getSize().y;
                    menu.show(ShowTabListButton.this, toDisplay, true);
                }
            }
        });
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        if(size == null){
            GC gc = null;
            try {
                gc = new GC(this);
                // Compute text size
                size = gc.stringExtent("*8");
                size.y = size.y + size.y/2;
                width = size.x;
                height = size.y;
            } finally {
                if (gc != null) {
                    gc.dispose();
                }
            }
        }
        return size;
    }

    public void setCurrentTheme(ThemeWrapper theme) {
        super.setCurrentTheme(theme);
        size = null;
        computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    }

    protected void paintControl(PaintEvent e, int gap, Rectangle clientArea) {
        GC gc = e.gc;

        if(hiddenTabsCount == 0){
            if(isEnabled()) {
                setEnabled(false);
            }
            setSelectable(false);
        } else {
            if(!isEnabled()) {
                setEnabled(true);
            }
            setSelectable(true);
        }
        Color color;
        if (selectable) {
            color = colorTextFocus;
        } else {
            color = colorTextNoFocus;
        }
        gc.setForeground(color);
        // Draw arrow
        int xPad = (clientArea.width - width);
        int yPad = (clientArea.height - height);
        if(xPad <= 0){
            xPad = 2;
        }
        if(yPad <= 0){
            yPad = 1;
        }
        String chevronString = hiddenTabsCount > 9 ? ".." : String.valueOf(hiddenTabsCount);
        gc.drawString("*", clientArea.x + xPad, clientArea.y + yPad, true);
        gc.drawString(chevronString, clientArea.x + xPad + 3, clientArea.y + yPad + 4, true);
    }

    public void dispose() {
        tabArea = null;
        if(menu != null) {
            menu.dispose();
            menu = null;
        }
        super.dispose();
    }

    public void handleEvent(Event event) {
        hiddenTabsCount = event.count;
    }

}
