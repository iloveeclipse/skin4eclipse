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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;

/**
 * Superclass of all view/editor buttons
 *
 * @author wmitsuda
 * @author Andrei
 */
public abstract class AbstractButton extends Canvas {

    protected ThemeWrapper currentTheme;

    protected boolean selectable;

    protected boolean mouseOver;

    protected List listeners;

    protected boolean active;

    protected boolean mouseDown;

    protected Color colorTitleFocus;

    protected Color colorTitleNoFocus;

    protected Color colorTextFocus;

    protected Color colorTextNoFocus;

    protected Color colorNoFocus;

    protected Color systemColorDarkShadow;

    protected Color systemColorLightShadow;

    protected AbstractButton(Composite parent, int style, final boolean isTitle) {
        super(parent, style);
        selectable = true;
        listeners = new ArrayList();
        // Selection/hover/etc...
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                mouseOverChanged(getClientArea().contains(e.x, e.y));
            }
        });
        addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(MouseEvent e) {
                mouseOverChanged(true);
            }

            public void mouseExit(MouseEvent e) {
                mouseOverChanged(false);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                mouseDownChanged(true);
            }

            public void mouseUp(MouseEvent e) {
                mouseDownChanged(false);
                if (getClientArea().contains(e.x, e.y)) {
                    Event evt = new Event();
                    evt.widget = AbstractButton.this;
                    SelectionEvent selectionEvent = new SelectionEvent(evt);
                    for (int i = 0; i < listeners.size(); i++) {
                        SelectionListener listener = (SelectionListener) listeners.get(i);
                        listener.widgetSelected(selectionEvent);
                    }
                }
            }
        });

        // Paint control
        addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                Rectangle clientArea = getClientArea();

                // Fill background
                GC gc = e.gc;
                gc.setClipping(clientArea);

                if (isTitle) {
                    if (active) {
                        gc.setBackground(colorTitleFocus);
                    } else {
                        gc.setBackground(colorTitleNoFocus);
                    }
                    gc.fillRectangle(clientArea);
                } else {
                    gc.setBackground(colorNoFocus);
                    gc.fillRectangle(clientArea);
                }

                // Paint inside
                int gap = 0;
                if (selectable && mouseOver && mouseDown) {
                    gap++;
                }
                AbstractButton.this.paintControl(e, gap, clientArea);

                // Paint borders
                if (selectable && mouseOver) {
                    drawButtonBorder(e.display, gc, clientArea);
                }
                gc.setClipping((Rectangle) null);
            }

        });
    }

    protected void drawButtonBorder(Display display, GC gc, Rectangle clientArea) {
        // Left-top
        if (mouseDown) {
            gc.setForeground(systemColorDarkShadow);
        } else {
            gc.setForeground(systemColorLightShadow);
        }
        gc.drawLine(clientArea.x, clientArea.y + clientArea.height - 1, clientArea.x,
                clientArea.y);
        gc.drawLine(clientArea.x, clientArea.y, clientArea.x + clientArea.width - 1,
                clientArea.y);

        // Right-bottom
        if (mouseDown) {
            gc.setForeground(systemColorLightShadow);
        } else {
            gc.setForeground(systemColorDarkShadow);
        }
        gc.drawLine(clientArea.x, clientArea.y + clientArea.height - 1, clientArea.x
                + clientArea.width - 1, clientArea.y + clientArea.height - 1);
        gc.drawLine(clientArea.x + clientArea.width - 1, clientArea.y, clientArea.x
                + clientArea.width - 1, clientArea.y + clientArea.height - 1);
    }

    protected ThemeWrapper getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(ThemeWrapper theme) {
        currentTheme = theme;
        initColorsAndFonts(theme);
    }

    protected void initColorsAndFonts(ThemeWrapper theme) {
        colorTitleFocus = theme.getColor(ThemeConstants.TITLE_COLOR_FOCUS);
        colorTitleNoFocus = theme.getColor(ThemeConstants.TITLE_COLOR_NOFOCUS);
        colorNoFocus = theme.getColor(ThemeConstants.TAB_COLOR_NOFOCUS);
        Display display = getDisplay();
        systemColorDarkShadow = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
        systemColorLightShadow = display
                .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        colorTextFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_FOCUS);
        colorTextNoFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_NOFOCUS);
    }

    protected abstract void paintControl(PaintEvent e, int gap, Rectangle clientArea);

    protected void mouseOverChanged(boolean hasMouse) {
        if (mouseOver != hasMouse) {
            mouseOver = hasMouse;
            redraw();
        }
    }

    protected void mouseDownChanged(boolean newValue) {
        if (mouseDown != newValue) {
            mouseDown = newValue;
            redraw();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        //        redraw();
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
            int max = Math.max(wHint, hHint);
            return new Point(max, max);
        }
        if (wHint == SWT.DEFAULT && hHint != SWT.DEFAULT) {
            return new Point(hHint, hHint);
        }
        if (hHint == SWT.DEFAULT && wHint != SWT.DEFAULT) {
            return new Point(wHint, wHint);
        }
        return new Point(16, 16);
    }

    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    public void setSelectable(boolean selectable) {
        //        boolean changed = this.selectable != selectable;
        this.selectable = selectable;
        //        if (changed) {
        //            redraw();
        //        }
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void dispose() {
        if (isDisposed()) {
            return;
        }
        listeners.clear();
        currentTheme = null;
        super.dispose();
    }
}
