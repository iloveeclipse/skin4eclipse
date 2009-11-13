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
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;

public final class SystemMenuSortTabs extends Action {

    private VSStackPresentation presentation;
    private final IStackPresentationSite site;


    public SystemMenuSortTabs(VSStackPresentation presentation) {
        this.presentation = presentation;
        setId("sortTabs");
        this.site = presentation.getSite();
    }

    public void dispose() {
        presentation = null;
    }

    public void run() {
        presentation.sortTabs();
    }

    public void update() {
        IPresentablePart[] parts = site.getPartList();
        setEnabled(parts.length > 1);
    }
}
