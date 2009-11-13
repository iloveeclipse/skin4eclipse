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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.DefaultStackPresentationSite;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;

public final class ShowClosedPartListAction extends Action {
    private IStackPresentationSite site;
    public ShowClosedPartListAction(IStackPresentationSite site){
        super();
        setId("showClosedList");
        this.site = site;
        update();
    }

    public void run() {
        if (!(site instanceof DefaultStackPresentationSite)) {
            return;
        }
        DefaultStackPresentationSite dsite = (DefaultStackPresentationSite) site;
        VSStackPresentation presentation = (VSStackPresentation) dsite.getPresentation();
        presentation.showClosedPartList();
    }

    public void update(){
        if (!(site instanceof DefaultStackPresentationSite)) {
            return;
        }
        DefaultStackPresentationSite dsite = (DefaultStackPresentationSite) site;
        VSStackPresentation presentation = (VSStackPresentation) dsite.getPresentation();
        setEnabled(presentation.hasClosedParts());
    }
}