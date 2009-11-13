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

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.sessions.EditingSession;
import de.loskutov.eclipseskins.sessions.Sessions;
import de.loskutov.eclipseskins.ui.UIUtils;

public class SystemMenuSave extends MenuManager {

    private final VSStackPresentation presentation;

    public SystemMenuSave(VSStackPresentation presentation) {
        super("Save Tab List as Session", getDescriptor(), null);
        this.presentation = presentation;
        add(new SystemMenuSaveSession(presentation, null));
    }

    static ImageDescriptor getDescriptor(){
        String key = UIUtils.ACTION + "saveList" + UIUtils.ICON;
        String icon = PresentationPlugin.getResourceString(key);
        if(icon != null && !icon.equals(key) && icon.length() > 0){
            return PresentationPlugin.getImageDescriptor(icon);
        }
        return null;
    }

    public boolean isDynamic() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
     */
    protected void update(boolean force, boolean recursive) {
        removeAll();
        add(new SystemMenuSaveSession(presentation, null));

        Sessions sessions = Sessions.getInstance();
        List sessionsList = sessions.getSessions(false);

        EditingSession lastUsed = sessions.getSession(Sessions.RECENTLY_CLOSED);

        if (lastUsed != null) {
            sessionsList.remove(lastUsed);
        }

        if(sessionsList.size() > 0){
            add(new Separator());
        }

        for (int i = 0; i < sessionsList.size(); i++) {
            EditingSession session = (EditingSession) sessionsList.get(i);
            add(new SystemMenuSaveSession(presentation, session.getName()));
        }
        super.update(force, recursive);
    }

}
