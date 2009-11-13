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

public final class ShowPartListAction extends Action {
    private IStackPresentationSite site;
    public ShowPartListAction(IStackPresentationSite site){
        super();
        setId("showList");
        this.site = site;
    }

    public void run() {
        if (!(site instanceof DefaultStackPresentationSite)) {
            return;
        }
        DefaultStackPresentationSite dsite = (DefaultStackPresentationSite) site;
        dsite.getPresentation().showPartList();
    }
}