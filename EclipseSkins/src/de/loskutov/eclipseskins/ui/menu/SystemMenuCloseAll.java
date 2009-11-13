/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui.menu;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.sessions.Sessions;

public class SystemMenuCloseAll extends Action {

    private IStackPresentationSite site;
    private VSStackPresentation presentation;

    public SystemMenuCloseAll(VSStackPresentation presentation) {
        this.presentation = presentation;
        this.site = presentation.getSite();
        setId("closeAll");
    }

    public void dispose() {
        presentation = null;
        site = null;
    }

    public void run() {
        if(!presentation.isView()){
            int editors = presentation.getPartsCount();
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            editors -= window.getActivePage().getEditorReferences().length;
            if(editors == 0){
                Sessions.getInstance().createSession(Sessions.RECENTLY_CLOSED, true);
            }
        }
        presentation.close(site.getPartList());
    }

    public void update() {
        IPresentablePart[] parts = site.getPartList();
        setEnabled(parts.length != 0);
    }

    public boolean shouldBeVisible() {
        return true;
    }

}
