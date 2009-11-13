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

import java.util.List;

import org.eclipse.ui.presentations.IPresentablePart;

import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.sessions.TemporaryEditingSession;

public final class CopyAllPathsToClipboardAction extends CopyPathToClipboardAction {

    public CopyAllPathsToClipboardAction() {
        super();
        setId("copyAllPathsToClipboard");
    }

    public void run() {

        TemporaryEditingSession tes = new TemporaryEditingSession("Copy session");
        tes.setRestored(true);
        tes.createEditorsSnapshot();
        List editors = tes.getEditors();
        StringBuffer sb = new StringBuffer();

        boolean shouldCopyFullPath = shouldCopyFullPath();
        String newLine = System.getProperty("line.separator");
        for (int i = 0; i < editors.size(); i++) {
            EditorInfo info = (EditorInfo) editors.get(i);
            String path2 = getPath(info, shouldCopyFullPath);
            if(path2 == null || path2.length() == 0){
                continue;
            }
            sb.append(path2).append(newLine);
        }
        if(sb.length() != 0){
            copyToClipboard(sb.toString());
        }
    }

    public void setTarget(IPresentablePart presentablePart) {
        // no-op
    }

}
