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
package de.loskutov.eclipseskins.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.ui.buttons.CloseButton;
import de.loskutov.eclipseskins.ui.buttons.MaximizeButton;
import de.loskutov.eclipseskins.ui.buttons.MenuButton;
import de.loskutov.eclipseskins.ui.buttons.MinimizeButton;
import de.loskutov.eclipseskins.ui.buttons.PartListButton;

/**
 * View title control
 *
 * @author wmitsuda
 * @author Andrei
 */
public class PartTitle extends Canvas implements PaintListener {

    public static final int TEXT_GAP_X = 2;

    public static final int TEXT_GAP_Y = 1;

    private CloseButton closeButton;

    private MaximizeButton maxButton;

    private MinimizeButton minButton;

    protected MenuButton menuButton;

    protected PartListButton partListBtn;

    protected PartListButton closedPartListBtn;

    private ThemeWrapper currentTheme;

    private boolean active;

    private final boolean isView;

    protected IPresentablePart part;

    private String text;

    private Point cachedSize;

    private int borderSize;

    private Font fontTitle;

    private Color colorBorderFocus;

    private Color colorBorderNoFocus;

    private Color colorTextFocus;

    private Color colorTextNoFocus;

    private Color colorTitleFocus;

    private Color colorTitleNoFocus;

    private int state;

    public PartTitle(final VSStackPresentation presentation, int style) {
        super((Composite) presentation.getControl(), style);
        addPaintListener(this);
        final IStackPresentationSite site = presentation.getSite();
        isView = presentation.isView();
        closeButton = new CloseButton(this, style, true);
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (part != null) {
                    presentation.close(new IPresentablePart[] { part });
                }
            }
        });

        maxButton = new MaximizeButton(this, style, true, site);
        maxButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (site.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                    site.setState(IStackPresentationSite.STATE_RESTORED);
                } else {
                    site.setState(IStackPresentationSite.STATE_MAXIMIZED);
                }
            }
        });

        minButton = new MinimizeButton(this, style, true, site);
        minButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (site.getState() == IStackPresentationSite.STATE_MINIMIZED) {
                    site.setState(IStackPresentationSite.STATE_RESTORED);
                } else {
                    site.setState(IStackPresentationSite.STATE_MINIMIZED);
                }
            }
        });

        if (isView) {
            menuButton = new MenuButton(this, style);
            menuButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (part != null && part.getMenu() != null) {
                        // fix for bug 225780 https://bugs.eclipse.org/bugs/show_bug.cgi?id=225780
                        part.setFocus();

                        Rectangle bounds = menuButton.getBounds();
                        part.getMenu().showMenu(
                                menuButton.getParent().toDisplay(bounds.x,
                                        bounds.y + bounds.height));
                    }
                }
            });
        } else {
            closedPartListBtn = new PartListButton(this, presentation, style, true);
            closedPartListBtn.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if(presentation.getClosedPartsCount() > 0) {
                        presentation.showClosedPartList(getPreferredClosedPartListLocation());
                    }
                }
            });
            closedPartListBtn.setToolTipText("Closed editors");

            partListBtn = new PartListButton(this, presentation, style, false);
            partListBtn.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if(presentation.getPartsCount() > 0) {
                        presentation.showPartList(getPreferredPartListLocation(), true);
                    }
                }
            });
            partListBtn.setToolTipText("Opened editors");
        }

        setCurrentTheme(presentation.getCurrentTheme());
    }

    public Point getPreferredPartListLocation() {
        Rectangle bounds = partListBtn.getBounds();
        return toDisplay(bounds.x, bounds.y + bounds.height);
    }

    public Point getPreferredClosedPartListLocation() {
        Rectangle bounds = closedPartListBtn.getBounds();
        return toDisplay(bounds.x, bounds.y + bounds.height);
    }

    protected ThemeWrapper getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(ThemeWrapper theme) {
        currentTheme = theme;
        maxButton.setCurrentTheme(theme);
        if (isView) {
            menuButton.setCurrentTheme(theme);
        } else {
            partListBtn.setCurrentTheme(theme);
            closedPartListBtn.setCurrentTheme(theme);
        }
        minButton.setCurrentTheme(theme);
        closeButton.setCurrentTheme(theme);

        borderSize = theme.getInt(ThemeConstants.BORDER_SIZE);
        colorTitleFocus = theme.getColor(ThemeConstants.TITLE_COLOR_FOCUS);
        colorTitleNoFocus = theme.getColor(ThemeConstants.TITLE_COLOR_NOFOCUS);
        colorBorderFocus = theme.getColor(ThemeConstants.BORDER_COLOR_FOCUS);
        colorBorderNoFocus = theme.getColor(ThemeConstants.BORDER_COLOR_NOFOCUS);
        colorTextFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_FOCUS);
        colorTextNoFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_NOFOCUS);
        fontTitle = theme.getFont(ThemeConstants.TITLE_FONT);

        // reset text size
        cachedSize = null;
    }

    public void setActive(boolean active) {
        this.active = active;
        closeButton.setActive(active);
        maxButton.setActive(active);
        minButton.setActive(active);
        if (isView) {
            menuButton.setActive(active);
        } else {
            partListBtn.setActive(active);
            closedPartListBtn.setActive(active);
        }
        //        redraw();
    }

    public void setText(String text) {
        this.text = text;
        setToolTipText(text);
    }

    public String getText() {
        return text;
    }

    public void setPresentablePart(IPresentablePart part) {
        if (PresentationPlugin.DEBUG_STATE) {
            System.out.println("title: set part: " + part.getName());
        }
        this.part = part;
        String toolTip = part.getTitleToolTip();
        if(toolTip == null || toolTip.length() == 0){
            toolTip = part.getTitle();
        }
        setToolTipText(toolTip);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {

        if (cachedSize != null) {
            return cachedSize;
        }

        ThemeWrapper theme = getCurrentTheme();
        // Compute border size
        int borderSize_2 = theme.getInt(ThemeConstants.BORDER_SIZE) * 2;
        cachedSize = new Point(0, TEXT_GAP_Y * 2 + borderSize_2);
        GC gc = new GC(this);
        Font font = theme.getFont(ThemeConstants.TITLE_FONT);
        gc.setFont(font);
        Point stringExtent = gc.stringExtent("W");
        gc.dispose();

        cachedSize.y += stringExtent.y;

        // Compute close button size
        Point closeSize = closeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
        cachedSize.x = closeSize.x; // Math.max(stringExtent.x, closeSize.x);
        cachedSize.y = Math.max(cachedSize.y, closeSize.y);

        return cachedSize;
    }

    /**
     * Paint the control and set buttons bounds
     */
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle clientArea = getClientArea();
        gc.setClipping(clientArea);

        int borderSize_2 = borderSize * 2;
        if (active) {
            gc.setBackground(colorTitleFocus);
        } else {
            gc.setBackground(colorTitleNoFocus);
        }
        // Draw background
        gc.fillRectangle(clientArea);
        // draw border
        if (borderSize > 0) {
            if (active) {
                gc.setForeground(colorBorderFocus);
            } else {
                gc.setForeground(colorBorderNoFocus);
            }
            gc.setLineWidth(borderSize);
            gc.drawRectangle(clientArea.x + borderSize / 2,
                    clientArea.y + borderSize / 2, clientArea.width - borderSize,
                    clientArea.height - borderSize);
        }
        // draw text
        if (active) {
            gc.setForeground(colorTextFocus);
        } else {
            gc.setForeground(colorTextNoFocus);
        }
        gc.setClipping(borderSize + TEXT_GAP_X, borderSize + TEXT_GAP_Y, clientArea.width
                - borderSize_2 - 2 * TEXT_GAP_X, clientArea.height - borderSize_2 - 2
                * TEXT_GAP_Y);

        gc.setFont(fontTitle);
        gc.drawText(text != null ? text : "", borderSize + TEXT_GAP_X, borderSize
                + TEXT_GAP_Y);
        gc.setClipping((Rectangle) null);
    }

    public void redraw() {
        /*if(isMinimized()){
            minButton.redraw();
        } else*/ {
            if (isView) {
                menuButton.redraw();
            } else {
                partListBtn.redraw();
                closedPartListBtn.redraw();
            }
            minButton.redraw();
            maxButton.redraw();
            closeButton.redraw();
        }
        super.redraw();
    }

    public void dispose() {
        if (isDisposed()) {
            return;
        }
        currentTheme = null;
        minButton.dispose();
        minButton = null;
        closeButton.dispose();
        closeButton = null;
        maxButton.dispose();
        maxButton = null;
        if (isView) {
            menuButton.dispose();
            menuButton = null;
        } else {
            partListBtn.dispose();
            partListBtn = null;
            closedPartListBtn.dispose();
            closedPartListBtn = null;
        }
        text = null;
        super.dispose();
    }

    private boolean isMinimized(){
        return state == IStackPresentationSite.STATE_MINIMIZED;
    }

    public void setBounds(int x1, int y1, int width, int height) {
        super.setBounds(x1, y1, width, height);

        int borderSize_2 = borderSize * 2;
        Rectangle clientArea = getClientArea();

        int x = clientArea.x + clientArea.width - borderSize;

        int myHeight = clientArea.height - borderSize_2;

        boolean minimized = isMinimized();

        if(!minimized){
            // Position close button
            Point closeSize = closeButton.computeSize(SWT.DEFAULT, myHeight);
            x -= closeSize.x;
            closeButton.setBounds(x, clientArea.y + borderSize, closeSize.x, closeSize.y);

            // Position maximize button
            Point maxSize = maxButton.computeSize(SWT.DEFAULT, myHeight);
            x -= maxSize.x;
            maxButton.setBounds(x, clientArea.y + borderSize, maxSize.x, maxSize.y);
        }

        // Position minimize button
        Point minSize = minButton.computeSize(SWT.DEFAULT, myHeight);
        x -= minSize.x;
        minButton.setBounds(x, clientArea.y + borderSize, minSize.x, minSize.y);

        if(!minimized){
            if (isView) {
                if (shouldShowMenu()) {
                    // Position menu button
                    Point menuSize = menuButton.computeSize(SWT.DEFAULT, myHeight);
                    x -= menuSize.x;
                    menuButton
                    .setBounds(x, clientArea.y + borderSize, menuSize.x, menuSize.y);
                    menuButton.setVisible(true);
                } else {
                    menuButton.setVisible(false);
                }
            } else {
                // Position closed parts button
                Point closedPartListSize = closedPartListBtn.computeSize(SWT.DEFAULT,
                        myHeight);
                x -= closedPartListSize.x;
                closedPartListBtn.setBounds(x, clientArea.y + borderSize, minSize.x,
                        minSize.y);

                // Position opened parts button
                Point partListSize = partListBtn.computeSize(SWT.DEFAULT, myHeight);
                x -= partListSize.x;
                partListBtn.setBounds(x, clientArea.y + borderSize, minSize.x, minSize.y);
            }
        }
    }

    private boolean shouldShowMenu() {
        // XXX part.getMenu() causes NPE from Eclipse 3.3 to 3.5 on "minimize",
        // but only if ThemeConstants.ENABLE_NEW_MIN_MAX is true
        boolean newMinMaxEnabled = getCurrentTheme().getBoolean(ThemeConstants.ENABLE_NEW_MIN_MAX);
        if(newMinMaxEnabled){
            return part != null;
        }
        // without this useless MIN_MAX gimmick it just works as expected
        return part != null && part.getMenu() != null;
    }

    public void setState(int state) {
        this.state = state;
        if(isMinimized()){
            closeButton.setVisible(false);
            maxButton.setVisible(false);
            if (isView) {
                menuButton.setVisible(false);
            } else {
                partListBtn.setVisible(false);
                closedPartListBtn.setVisible(false);
            }
        } else {
            closeButton.setVisible(true);
            maxButton.setVisible(true);
            if (isView) {
                menuButton.setVisible(true);
            } else {
                partListBtn.setVisible(true);
                closedPartListBtn.setVisible(true);
            }
        }
    }
}
