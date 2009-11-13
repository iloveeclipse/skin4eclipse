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
import org.eclipse.ui.PlatformUI;

import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;

public final class SystemMenuToggleTabs extends Action {

    private VSStackPresentation presentation;

    private final boolean isView;

    public SystemMenuToggleTabs(VSStackPresentation presentation, boolean isView) {
        this.presentation = presentation;
        this.isView = isView;
        setId("toggleTabs");
        if (isView) {
            setChecked(presentation.getFlag(VSStackPresentation.F_TAB_AREA_VISIBLE));
        } else {
            setChecked(presentation.getCurrentTheme().getBoolean(
                    ThemeConstants.EDITOR_TAB_AREA_VISIBLE));
        }
    }

    public void dispose() {
        presentation = null;
    }

    public void run() {
        boolean value;
        ThemeWrapper theme = presentation.getCurrentTheme();
        String editorKey = ThemeConstants.EDITOR_TAB_AREA_VISIBLE;
        if(isView){
            value = !presentation.getFlag(VSStackPresentation.F_TAB_AREA_VISIBLE);
        } else {
            value = !theme.getBoolean(editorKey);
        }
        setChecked(value);
        if (isView) {
            presentation.setFlag(VSStackPresentation.F_TAB_AREA_VISIBLE, value);
        } else {
            theme.setBoolean(editorKey, value);
            PlatformUI.getPreferenceStore().setValue(editorKey, value);
        }
    }

}
