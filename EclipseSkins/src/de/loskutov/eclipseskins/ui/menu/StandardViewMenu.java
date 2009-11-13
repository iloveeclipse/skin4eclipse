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
package de.loskutov.eclipseskins.ui.menu;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.presentations.SystemMenuMaximize;
import org.eclipse.ui.internal.presentations.SystemMenuMinimize;
import org.eclipse.ui.internal.presentations.SystemMenuMove;
import org.eclipse.ui.internal.presentations.SystemMenuRestore;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.internal.presentations.util.ISystemMenu;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.ui.UIUtils;

/**
 * @author Andrei
 */
public class StandardViewMenu  implements ISystemMenu {

    /* package */ MenuManager menuManager;
    private SystemMenuRestore restore;
    private SystemMenuMove move;
    private SystemMenuMinimize minimize;
    private SystemMenuMaximize maximize;
    private SystemMenuClose close;
    private SystemMenuShowView showView;
    private SystemMenuSortTabs sortEditorList;

    protected boolean isViewMenu(){
        return true;
    }

    public StandardViewMenu(VSStackPresentation presentation, boolean addSystemActions) {
        menuManager = new MenuManager();
        IStackPresentationSite site = presentation.getSite();
        restore = new SystemMenuRestore(site);
        restore.setId("restore");
        UIUtils.initAction(restore);

        move = new SystemMenuMove(site, WorkbenchMessages.ViewPane_moveView, false);

        minimize = new SystemMenuMinimize(site);
        minimize.setId("minimize");
        UIUtils.initAction(minimize);

        maximize = new SystemMenuMaximize(site);
        maximize.setId("maximize");
        UIUtils.initAction(maximize);

        close = new SystemMenuClose(presentation);
        UIUtils.initAction(close);

        SystemMenuToggleTabs toggleTabs = new SystemMenuToggleTabs(presentation, isViewMenu());
        UIUtils.initAction(toggleTabs);

        SystemMenuToggleToolbar toggleToolbar = null;
        if(isViewMenu()){
            toggleToolbar = new SystemMenuToggleToolbar(presentation);
            UIUtils.initAction(toggleToolbar);

            showView = new SystemMenuShowView(presentation);
        }

        { // Initialize system menu
            menuManager.add(new Separator("misc"));
            menuManager.add(new Separator("restore"));
            menuManager.add(new UpdatingActionContributionItem(restore));

            menuManager.add(move);
            menuManager.add(new GroupMarker("size"));
            menuManager.add(new GroupMarker("state"));
            menuManager.add(new UpdatingActionContributionItem(minimize));

            menuManager.add(new UpdatingActionContributionItem(maximize));
            menuManager.add(new Separator("toggle"));
            menuManager.add(toggleTabs);
            if(isViewMenu()){
                menuManager.add(toggleToolbar);
            }
            if(isViewMenu()){
                menuManager.add(showView);
            }
            menuManager.add(new Separator("list"));
            menuManager.add(new Separator("close"));
            menuManager.appendToGroup("close", close);
            if(addSystemActions) {
                site.addSystemActions(menuManager);
            }
        } // End of system menu initialization

        if(isViewMenu()){
            sortEditorList = new SystemMenuSortTabs(presentation);
            UIUtils.initAction(sortEditorList);
            menuManager.appendToGroup("list", sortEditorList);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates, IPresentablePart currentSelection) {
        restore.update();
        move.setTarget(currentSelection);
        move.update();
        minimize.update();
        maximize.update();
        close.setTarget(currentSelection);
        if(isViewMenu()){
            showView.setTarget(currentSelection);
            sortEditorList.update();
        }

        Menu aMenu = menuManager.createContextMenu(parent);
        menuManager.update(true);
        aMenu.setLocation(displayCoordinates.x, displayCoordinates.y);
        aMenu.setVisible(true);
    }

    public void dispose() {
        menuManager.dispose();
        menuManager.removeAll();
    }

}
