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

import de.loskutov.eclipseskins.presentation.VSStackPresentation;

public final class SystemMenuToggleToolbar extends Action {

    private VSStackPresentation presentation;

    public SystemMenuToggleToolbar(VSStackPresentation presentation) {
        this.presentation = presentation;
        setId("toggleToolbar");
        setChecked(presentation.getFlag(VSStackPresentation.F_TOOLBAR_VISIBLE));
    }

    public void dispose() {
        presentation = null;
    }

    public void run() {
        boolean value = ! presentation.getFlag(VSStackPresentation.F_TOOLBAR_VISIBLE);
        setChecked(value);
        presentation.setFlag(VSStackPresentation.F_TOOLBAR_VISIBLE, value);
    }
}
