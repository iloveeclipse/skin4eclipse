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

public class SystemMenuCloseLeftRight extends Action {

    private IStackPresentationSite site;
    private IPresentablePart current;
    private VSStackPresentation presentation;
    private final boolean left;

    public SystemMenuCloseLeftRight(VSStackPresentation presentation, boolean left) {
        this.presentation = presentation;
        this.left = left;
        this.site = presentation.getSite();
        setId(left? "closeOthersL" : "closeOthersR");
    }

    public void dispose() {
        presentation = null;
        site = null;
    }

    public void run() {
        presentation.closeOthers(current, left);
    }

    public void update() {
        setTarget(site.getSelectedPart());
    }

    public boolean shouldBeVisible() {
        return true;
    }

    /**
     * @since 3.1
     */
    public void setTarget(IPresentablePart current) {
        this.current = current;
        setEnabled(current != null && site.getPartList().length > 1);
    }
}
