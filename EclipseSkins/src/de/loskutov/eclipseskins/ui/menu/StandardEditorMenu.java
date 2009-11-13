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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.ui.UIUtils;
import de.loskutov.eclipseskins.ui.actions.CopyAllPathsToClipboardAction;
import de.loskutov.eclipseskins.ui.actions.CopyPathToClipboardAction;
import de.loskutov.eclipseskins.ui.actions.OpenUnnamedEditorAction;
import de.loskutov.eclipseskins.ui.actions.ShowClosedPartListAction;
import de.loskutov.eclipseskins.ui.actions.ShowPartListAction;

/**
 * @author Andrei
 */
public class StandardEditorMenu extends StandardViewMenu {

    private final SystemMenuCloseOthers closeOthers;
    private final SystemMenuCloseAll closeAll;
    private IAction openAgain;
    private final ShowClosedPartListAction showClosedEditorList;
    private final SystemMenuSortTabs sortEditorList;
    private final CopyPathToClipboardAction pathToClipboard;
    private final CopyAllPathsToClipboardAction allPathToClipboard;
    private final SystemMenuCloseLeftRight closeOthersL;
    private final SystemMenuCloseLeftRight closeOthersR;
    private final OpenUnnamedEditorAction openUnnamedEditor;

    protected boolean isViewMenu(){
        return false;
    }

    public StandardEditorMenu(VSStackPresentation presentation, boolean addSystemActions) {
        super(presentation, addSystemActions);
        IStackPresentationSite site = presentation.getSite();

        closeOthers = new SystemMenuCloseOthers(presentation);
        UIUtils.initAction(closeOthers);

        closeOthersL = new SystemMenuCloseLeftRight(presentation, true);
        UIUtils.initAction(closeOthersL);

        closeOthersR = new SystemMenuCloseLeftRight(presentation, false);
        UIUtils.initAction(closeOthersR);

        closeAll = new SystemMenuCloseAll(presentation);
        UIUtils.initAction(closeAll);

        if(addSystemActions) {
            openAgain = ActionFactory.NEW_EDITOR.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            openAgain.setId("openAgain");
            UIUtils.initAction(openAgain);
        }

        openUnnamedEditor = new OpenUnnamedEditorAction();

        pathToClipboard = new CopyPathToClipboardAction();
        UIUtils.initAction(pathToClipboard);

        allPathToClipboard = new CopyAllPathsToClipboardAction();
        UIUtils.initAction(allPathToClipboard);

        showClosedEditorList = new ShowClosedPartListAction(site);
        UIUtils.initAction(showClosedEditorList);
        menuManager.appendToGroup("list", showClosedEditorList);

        sortEditorList = new SystemMenuSortTabs(presentation);
        UIUtils.initAction(sortEditorList);
        menuManager.appendToGroup("list", sortEditorList);

        ShowPartListAction showEditorList = new ShowPartListAction(site);
        UIUtils.initAction(showEditorList);
        menuManager.insertBefore("showClosedList", showEditorList);

        SystemMenuSave saveList = new SystemMenuSave(presentation);
        menuManager.insertBefore("showList", saveList);

        menuManager.appendToGroup("close", closeOthersL);
        menuManager.appendToGroup("close", closeOthersR);
        menuManager.appendToGroup("close", closeOthers);
        menuManager.appendToGroup("close", closeAll);
        menuManager.add(new Separator());
        menuManager.add(pathToClipboard);
        menuManager.add(allPathToClipboard);
        menuManager.add(new Separator());
        if(addSystemActions){
            menuManager.add(openAgain);
        }
        menuManager.add(openUnnamedEditor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates,
            IPresentablePart currentSelection) {
        closeOthers.setTarget(currentSelection);
        closeOthersL.setTarget(currentSelection);
        closeOthersR.setTarget(currentSelection);
        pathToClipboard.setTarget(currentSelection);
        closeAll.update();
        sortEditorList.update();
        showClosedEditorList.update();
        super.show(parent, displayCoordinates, currentSelection);
    }

}
