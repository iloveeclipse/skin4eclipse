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
 *    Fabio Zadrozny - draw toolbar even if no editors are available
 *******************************************************************************/
package de.loskutov.eclipseskins.presentation;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.ui.PartTab;
import de.loskutov.eclipseskins.ui.menu.StandardEditorMenu;

/**
 * Stack presentation for editors
 *
 * @author wmitsuda
 * @author Andrei
 * @author fabioz
 */
public class VSEditorStackPresentation extends VSStackPresentation {

    public static final int CONTROL_GAP = 3;

    public VSEditorStackPresentation(Composite parent,
            final IStackPresentationSite site, boolean showResizeCommands) {
        super(parent, site, showResizeCommands);
    }

    protected void addActionsToSystemMenu(final IStackPresentationSite site) {
        mainSystemMenu = new StandardEditorMenu(this, true);
        reducedSystemMenu = new StandardEditorMenu(this, false);
    }

    public void dispose() {
        VSImprovedPresentationFactory.remove(this);
        super.dispose();
    }

    protected void layout() {
        if(!isVisible){
            if(PresentationPlugin.DEBUG_LAYOUT) {
                System.out.println("editor: layout cancelled as invisible");
            }
            return;
        }
        // Note that we do make the layout even if it's empty
        // because we want to show the toolbar with the closed editors.
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println("editor: do layout");
        }

        ThemeWrapper theme = getCurrentTheme();
        boolean tabsVisible = theme.getBoolean(ThemeConstants.EDITOR_TAB_AREA_VISIBLE);
        Rectangle clientArea = presentationControl.getClientArea();

        Point titleSize = title.computeSize(clientArea.width, SWT.DEFAULT);
        title.setBounds(clientArea.x, clientArea.y, clientArea.width, titleSize.y);
        title.setVisible(true);
        int tabAreaHeight = tabsVisible? tabArea.computeHeight() : 0;

        boolean minimized = isMinimized();

        if (minimized && currentPart != null) {
            if(PresentationPlugin.DEBUG_LAYOUT) {
                System.out.println("editor:cancel layout as minimized");
            }

            currentPart.getControl().setVisible(false);
        } else if (currentPart != null) {
            // Compute content area size
            int topHeight = titleSize.y;
            boolean topPosition = SWT.TOP == theme.getInt(ThemeConstants.EDITOR_TAB_POSITION);
            Point tabButtonsSize = tabButtons.computeSize(SWT.DEFAULT, tabAreaHeight, true);
            int borderSize_x_2 = borderSize * 2;
            int tabYpos;
            int contentYpos;
            if(topPosition){
                tabYpos = clientArea.y + topHeight;
                contentYpos = tabYpos + tabAreaHeight + CONTROL_GAP;
                clientArea.height -= tabAreaHeight + CONTROL_GAP + topHeight + borderSize;
            } else {
                contentYpos = clientArea.y + topHeight;
                clientArea.height -= tabAreaHeight + topHeight + borderSize;
                tabYpos = contentYpos + clientArea.height;
            }

            if(clientArea.height > tabAreaHeight && tabsVisible){
                // Layout tabs
                tabArea.setBounds(clientArea.x + borderSize,
                        tabYpos,
                        clientArea.width - borderSize_x_2 - tabButtonsSize.x,
                        tabAreaHeight);

                tabButtons.setBounds(clientArea.x + clientArea.width - tabButtonsSize.x - borderSize,
                        tabYpos,
                        tabButtonsSize.x,
                        tabAreaHeight);
            } else {
                // not enough space for tabs
                clientArea.height += tabAreaHeight;
                if(topPosition){
                    contentYpos -= tabAreaHeight + CONTROL_GAP;
                }
            }

            Point contentDisplayPos = presentationControl.toDisplay(
                    clientArea.x + borderSize, contentYpos);
            Control control = currentPart.getControl();
            Composite parent = control.getParent();
            Point contentControlPos = parent.toControl(contentDisplayPos);

            control.setBounds(contentControlPos.x, contentControlPos.y,
                    clientArea.width - borderSize_x_2, clientArea.height);
            control.setVisible(true);
        }
        if (minimized || clientArea.height < tabAreaHeight){
            tabArea.setVisible(false);
            tabButtons.setVisible(false);
        } else {
            tabArea.setVisible(tabsVisible);
            tabButtons.setVisible(tabsVisible);
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
     * Called whenever a property changes for one of the parts in this
     * presentation.
     *
     * @param part
     * @param property
     */
    protected void childPropertyChanged(IPresentablePart part, int property) {
        PartTab partTab = getTab(part);

        // If there is no tab for this part, just ignore the property change
        if (partTab == null) {
            return;
        }
        // no additional checks case is for "save all"
        partTab.refresh(property);
        switch (property) {
        case IPresentablePart.PROP_CONTENT_DESCRIPTION:
        case IPresentablePart.PROP_PART_NAME:
        case IPresentablePart.PROP_TITLE:
            setTitleText();
            break;
        default:
            break;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipseskins.vspresentation.VSStackPresentation#isView()
     */
    public boolean isView() {
        return false;
    }

    public void paintControl(PaintEvent e) {
        if(presentationControl == null){
            return;
        }
        if(PresentationPlugin.DEBUG_PAINT) {
            System.out.println("editor:paint");
        }

        Rectangle clientArea = presentationControl.getClientArea();
        GC gc = e.gc;
        gc.setClipping(clientArea);
        int borderSize_2 = borderSize * 2;

        if (parts.isEmpty()) {
            gc.setBackground(systemColorNormalShadow);
            gc.fillRectangle(clientArea.x + 2, clientArea.y + 2,
                    clientArea.width - 4, clientArea.height - 4);
            gc.setForeground(systemColorBackgr);
            gc.setLineWidth(borderSize);
            gc.drawRectangle(clientArea.x + 1, clientArea.y + 1,
                    clientArea.width - 3, clientArea.height - 3);
        } else {
            gc.setBackground(systemColorBackgr);
            gc.fillRectangle(clientArea.x + borderSize, clientArea.y + borderSize,
                    clientArea.width - borderSize_2, clientArea.height - borderSize_2);
        }

        if(borderSize > 0){
            gc.setLineWidth(borderSize);
            if (activeFocus) {
                gc.setForeground(colorBorderFocus);
            } else {
                gc.setForeground(colorBorderNoFocus);
            }
            gc.drawRectangle(clientArea.x + borderSize/2, clientArea.y + borderSize/2,
                    clientArea.width - borderSize, clientArea.height - borderSize);
        }
        gc.setClipping((Rectangle)null);
    }

    public List/*EditorInfo*/ getEditorInfos() {
        List list = new ArrayList();
        IPresentablePart[] partList = tabArea.getPartList();
        for (int i = 0; i < partList.length; i++) {
            EditorInfo info = createEditorInfo(partList[i]);
            if(info != null){
                info.setNumber(i);
                list.add(info);
            }
        }
        return list;
    }

}
