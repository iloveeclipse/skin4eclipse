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
 *    Fabio Zadrozny - option to prevent ESC from closing detached editors
 *******************************************************************************/
package de.loskutov.eclipseskins.presentation;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.ui.PartTab;
import de.loskutov.eclipseskins.ui.menu.StandardViewMenu;

/**
 * Stack presentation for views
 *
 * @author wmitsuda
 * @author Andrei
 * @author fabioz
 */
public class VSViewStackPresentation extends VSStackPresentation {

    public static final int CONTROL_GAP = 1;

    public VSViewStackPresentation(Composite parent, final IStackPresentationSite site,
            boolean showResizeCommands) {
        super(parent, site, showResizeCommands);

        if(isDetached()){
            // add listener to allow not to close detached views when ESC is pressed.
            Shell shell = parent.getShell();
            shell.addListener(SWT.Traverse, new Listener(){
                public void handleEvent(Event event) {
                    if (event.type == SWT.Traverse && event.detail == SWT.TRAVERSE_ESCAPE
                            && event.keyCode == SWT.ESC && event.stateMask == SWT.NONE) {
                        if(!getPrefs().getBoolean(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS)){
                            // Prevent the ESC from closing the detached views
                            // if the user has chosen that option.
                            event.doit = false;
                        }
                    }
                }
            });
        }
    }

    protected void addActionsToSystemMenu(final IStackPresentationSite site) {
        mainSystemMenu = new StandardViewMenu(this, true);
        reducedSystemMenu = new StandardViewMenu(this, false);
    }

    protected void layout() {
        if (!isVisible) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName() + ": cancel layout as invisible");
            }

            return;
        }
        if (parts.isEmpty()) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName() + ": cancel layout as no parts");
            }

            tabArea.setVisible(false);
            tabButtons.setVisible(false);
            return;
        }
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ": do layout");
        }

        Rectangle clientArea = presentationControl.getClientArea();
        Point titleSize = title.computeSize(clientArea.width, SWT.DEFAULT, true);
        title.setBounds(clientArea.x, clientArea.y, clientArea.width, titleSize.y);
        title.setVisible(true);
        boolean hasContent = currentPart != null;
        Control toolBar = hasContent ? currentPart.getToolBar() : null;

        // handle minimized state
        if (isMinimized()) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName() + ": cancel layout as minimized");
            }

            if (hasContent) {
                // WORKAROUND if perspective was switched and the view in the target perspective was
                // minimized, so we get strange painting problems without to set the bounds of control
                //                currentPart.getControl().setBounds(0, 0, 0,0);
                currentPart.getControl().setVisible(false);
                if (toolBar != null) {
                    // the same workaround as with control before
                    //                    toolBar.setBounds(0, 0, 0, 0);
                    toolBar.setVisible(false);
                }
            }
            tabArea.setVisible(false);
            tabButtons.setVisible(false);
            return;
        }

        /*
         * now we really do layout :)
         */
        ThemeWrapper theme = getCurrentTheme();
        boolean tabsVisible = getFlag(F_TAB_AREA_VISIBLE);
        boolean toolbarVisible = getFlag(F_TOOLBAR_VISIBLE);
        boolean topPosition = SWT.TOP == theme.getInt(ThemeConstants.VIEW_TAB_POSITION);
        int borderSize_x_2 = borderSize * 2;
        int topHeight = titleSize.y;

        Rectangle contentArea = Geometry.copy(clientArea);
        // Only show tabs, if there are more then one part to display
        int tabButtonsPosX = 0;
        int tabAreaPosY = contentArea.y;
        int maxToolbarWidth = 0;
        Point toolBarSize;
        if (toolBar != null && toolbarVisible) {
            toolBarSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        } else {
            toolBarSize = new Point(0, 0);
        }

        boolean allowToolbarInTabArea = false;
        if (parts.size() > 1) {
            // Layout tabs
            int tabAreaHeight;
            if (tabsVisible) {
                tabAreaHeight = tabArea.computeHeight();
                maxToolbarWidth = clientArea.width - tabArea.getEstimatedWidth()
                - borderSize;
                allowToolbarInTabArea = tabArea.hasEnoughSpace() && maxToolbarWidth > 0
                && maxToolbarWidth > toolBarSize.x;
                if (allowToolbarInTabArea) {
                    tabAreaHeight = Math.max(tabAreaHeight, toolBarSize.y);
                    if (tabAreaHeight == toolBarSize.y) {
                        tabAreaHeight += borderSize_x_2;
                        if (!topPosition) {
                            tabAreaHeight += borderSize;
                        }
                    }
                }
            } else {
                tabAreaHeight = 0;
            }

            if (clientArea.height - topHeight < tabAreaHeight) {
                tabsVisible = false;
                tabArea.setVisible(false);
                tabButtons.setVisible(false);
                contentArea.height -= borderSize;
            } else {
                contentArea.height -= tabAreaHeight + borderSize;
                if (topPosition) {
                    tabAreaPosY += topHeight;
                    topHeight += tabAreaHeight;
                    contentArea.height += tabAreaHeight;
                } else {
                    tabAreaPosY += contentArea.height;
                }
                Point tabButtonsSize = tabButtons.computeSize(SWT.DEFAULT, tabAreaHeight,
                        true);
                if (tabsVisible) {
                    tabArea.setBounds(clientArea.x + borderSize, tabAreaPosY,
                            clientArea.width - tabButtonsSize.x - borderSize_x_2,
                            tabAreaHeight);
                    tabButtonsPosX = clientArea.x + clientArea.width - tabButtonsSize.x
                    - borderSize;
                    tabButtons.setBounds(tabButtonsPosX, tabAreaPosY, tabButtonsSize.x,
                            tabAreaHeight);
                    tabArea.setVisible(true);
                    tabButtons.setVisible(true);

                } else {
                    tabsVisible = false;
                    tabArea.setVisible(false);
                    tabButtons.setVisible(false);
                }
            }
        } else {
            tabsVisible = false;
            tabArea.setVisible(false);
            tabButtons.setVisible(false);
            contentArea.height -= borderSize;
        }

        // Layout view toolBar
        if (toolBar != null) {
            boolean tooLessHeight = contentArea.height - topHeight - CONTROL_GAP <= toolBarSize.y;
            if (!toolbarVisible
                    || (tooLessHeight && (!tabsVisible || !allowToolbarInTabArea))) {
                toolBar.setVisible(false);
            } else {

                if (tabsVisible && allowToolbarInTabArea) {
                    int toolbarYpos;
                    if (topPosition) {
                        toolbarYpos = tabAreaPosY;
                    } else {
                        toolbarYpos = tabAreaPosY + CONTROL_GAP * 2;
                    }
                    // tab area may have space for toolbar...
                    Point toolBarDisplayPos = presentationControl.toDisplay(clientArea.x
                            + clientArea.width - toolBarSize.x - borderSize, toolbarYpos);
                    Point toolBarControlPos = toolBar.getParent().toControl(
                            toolBarDisplayPos);
                    toolBar.setBackground(theme
                            .getColor(ThemeConstants.TAB_COLOR_NOFOCUS));
                    toolBar.setBounds(toolBarControlPos.x, toolBarControlPos.y,
                            toolBarSize.x, toolBarSize.y);

                } else {
                    // no place on tab area or it is invisible
                    int toolbarYpos = clientArea.y + topHeight;
                    Point toolBarDisplayPos = presentationControl.toDisplay(clientArea.x,
                            toolbarYpos);
                    Point toolBarControlPos = toolBar.getParent().toControl(
                            toolBarDisplayPos);
                    toolBar.setBackground(theme.getColor(ThemeConstants.TAB_COLOR_FOCUS));
                    toolBar.setBounds(toolBarControlPos.x + borderSize,
                            toolBarControlPos.y, clientArea.width - borderSize_x_2,
                            toolBarSize.y);
                    topHeight += toolBarSize.y + CONTROL_GAP;
                }
                toolBar.setVisible(true);
            }

        }

        // Compute content area size
        contentArea.y += topHeight;
        contentArea.height -= topHeight;

        // Layout content
        if (hasContent && contentArea.height > 2) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName() + ": layout content");
            }

            Point contentDisplayPos = presentationControl.toDisplay(contentArea.x
                    + borderSize, contentArea.y);
            Control control = currentPart.getControl();
            Point contentControlPos = control.getParent().toControl(contentDisplayPos);
            control.setBounds(contentControlPos.x, contentControlPos.y, contentArea.width
                    - borderSize_x_2, contentArea.height);
            control.setVisible(true);
        } else if (hasContent) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName()
                        + ": layout content canceled as invisible");
            }

            Control control = currentPart.getControl();
            control.setVisible(false);
        }
    }

    public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredResult) {

        Point p = title.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        if (isMinimized()) {
            return width ? p.x : p.y;
        }
        int minSize = width ? p.x : p.y;
        return Math.max(minSize, preferredResult);
    }

    /**
     * @param sourcePart
     * @param propId
     */
    protected void childPropertyChanged(IPresentablePart sourcePart, int propId) {
        switch (propId) {
        case IPresentablePart.PROP_TOOLBAR:
            // this one for changes of view if view has a toolbar
            if (isVisible) {
                layout();
                redraw();
            }
            break;
        case IPresentablePart.PROP_CONTENT_DESCRIPTION:
            // this one for changes of status line near the title
        case IPresentablePart.PROP_DIRTY:
        case IPresentablePart.PROP_PART_NAME:
        case IPresentablePart.PROP_TITLE:
            setTitleText();

            PartTab partTab = getTab(sourcePart);
            // If there is no tab for this part, just ignore the property change
            if (partTab != null) {
                // no additional checks case is for "save all"
                partTab.setToolTipText(getPartTooltip(sourcePart));
            }

            if (isVisible) {
                redraw();
            }
            break;
        default:
            break;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipseskins.vspresentation.VSStackPresentation#isView()
     */
    public boolean isView() {
        return true;
    }

    public void paintControl(PaintEvent e) {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ": paint");
        }

        GC gc = e.gc;
        Rectangle clientArea = presentationControl.getClientArea();
        gc.setClipping(clientArea);
        if (borderSize > 0) {
            gc.setLineWidth(borderSize);
        }
        if (activeFocus) {
            gc.setForeground(colorBorderFocus);
        } else {
            gc.setForeground(colorBorderNoFocus);
        }
        gc.setBackground(systemColorNormalShadow);
        gc.fillRectangle(clientArea);

        if (borderSize > 0) {
            gc.drawRectangle(clientArea.x + borderSize / 2,
                    clientArea.y + borderSize / 2, clientArea.width - borderSize,
                    clientArea.height - borderSize);
        }
        gc.setClipping((Rectangle) null);
    }
}
