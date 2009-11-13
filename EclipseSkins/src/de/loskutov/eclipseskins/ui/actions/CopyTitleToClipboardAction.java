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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.presentations.IPresentablePart;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;

public class CopyTitleToClipboardAction extends Action {
    protected IPresentablePart part;

    public CopyTitleToClipboardAction() {
        super();
        setId("copyTitleToClipboard");
    }

    public void run() {
        if (this.part == null) {
            return;
        }
        String toolTip = getTooltip();
        copyToClipboard(toolTip);
    }

    protected String getTooltip() {
        String toolTip;
        if (shouldCopyFullPath()) {
            toolTip = part.getTitleToolTip();
            if (toolTip != null) {
                // strip the "virtual" project name from path
                int idx = toolTip.indexOf('/');
                if (idx > 0 && idx + 1 < toolTip.length()) {
                    toolTip = toolTip.substring(idx + 1);
                }
            } else {
                toolTip = part.getName();
            }
        } else {
            toolTip = part.getName();
        }

        if (toolTip == null) {
            toolTip = "";
        }
        return toolTip;
    }

    protected boolean shouldCopyFullPath() {
        return PresentationPlugin.getDefault().getPreferenceStore().getBoolean(
                ThemeConstants.COPY_FULL_TAB_TITLE);
    }

    protected void copyToClipboard(String toolTip) {
        Object[] data = new Object[] { toolTip };
        Transfer[] transfer = new Transfer[] { TextTransfer.getInstance() };
        Clipboard clipboard = new Clipboard(Display.getCurrent());
        clipboard.setContents(data, transfer);
        clipboard.dispose();
    }

    /**
     * Sets the target of this action to the given part.
     *
     * @param presentablePart
     *            the target part for this action, or <code>null</code> if
     *            there is no appopriate target part
     */
    public void setTarget(IPresentablePart presentablePart) {
        this.part = presentablePart;
        setEnabled(presentablePart != null);
    }
}
