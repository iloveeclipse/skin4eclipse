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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.sessions.SessionNameValidator;
import de.loskutov.eclipseskins.sessions.Sessions;

public class SystemMenuSaveSession extends Action {

    private IStackPresentationSite site;
    private VSStackPresentation presentation;
    private String sessionName;

    public SystemMenuSaveSession(VSStackPresentation presentation, String sessionName) {
        setText(getName(sessionName));
        this.presentation = presentation;
        this.sessionName = sessionName;
        this.site = presentation.getSite();
    }

    private static String getName(String name){
        return name == null? "New Session" : name;
    }

    public void dispose() {
        presentation = null;
        site = null;
    }

    public void run() {
        IPresentablePart[] partList = site.getPartList();
        List/*<EditorInfo>*/ infos = new ArrayList();
        for (int i = 0; i < partList.length; i++) {
            EditorInfo info = presentation.createEditorInfo(partList[i]);
            if(info != null){
                infos.add(info);
            }
        }
        if(infos.isEmpty()){
            // TODO show message
            return;
        }
        boolean createNew = sessionName == null;
        if(createNew){
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            InputDialog dialog = new InputDialog(window.getShell(), "Save session",
                    "Enter session name", null, new SessionNameValidator(true));
            int result = dialog.open();
            if(result == Window.CANCEL){
                return;
            }
            sessionName = dialog.getValue();
        }
        Sessions.getInstance().createSession(sessionName, infos, !createNew);
    }

}
