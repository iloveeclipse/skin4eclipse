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
package de.loskutov.eclipseskins.ui.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.presentations.PresentablePart;

import de.loskutov.eclipseskins.sessions.EditorInfo;

public class CopyPathToClipboardAction extends CopyTitleToClipboardAction {

    public CopyPathToClipboardAction() {
        super();
        setId("copyPathToClipboard");
    }

    public void run() {
        if (!(part instanceof PresentablePart)) {
            return;
        }
        PresentablePart ppart = (PresentablePart) part;
        IWorkbenchPartReference partRef = ppart.getPane().getPartReference();
        IWorkbenchPart workbenchPart = partRef.getPart(true);
        if(!(workbenchPart instanceof IEditorPart)) {
            super.run();
            return;
        }
        IEditorPart editorPart = (IEditorPart) workbenchPart;
        IEditorInput input = editorPart.getEditorInput();

        EditorInfo info = new EditorInfo(input, partRef.getId(), 0);

        String path2 = getPath(info, shouldCopyFullPath());
        if(path2 != null) {
            copyToClipboard(path2);
        }
    }

    protected String getPath(EditorInfo info, boolean copyFullPath) {
        String path2 = info.getPath();

        if(path2 == null) {
            path2 = info.getName();
            if(path2 == null && part != null) {
                path2 = getTooltip();
            } else {
                path2 = info.getName();
            }
        } else {
            if(!copyFullPath) {
                path2 = new File(path2).getName();
            } else {
                try {
                    path2 = new File(path2).getCanonicalPath();
                } catch (IOException e) {
                    path2 = new File(path2).getAbsolutePath();
                }
            }
        }
        return path2;
    }

}
