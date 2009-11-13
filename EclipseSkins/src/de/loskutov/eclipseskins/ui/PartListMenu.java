/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;


import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * see  org.eclipse.ui.internal.presentations.defaultpresentation.DefaultPartList
 * @author Andrei
 */
public class PartListMenu {

    private TabArea tabArea;
    protected final boolean editorPartList;
    private final boolean closed;

    /**
     *
     * @param tabArea
     * @param editorPartList true to lock the state of editor list and always show all parts.
     * false is default, the part list state will be controlled through preferences
     */
    public PartListMenu(TabArea tabArea, boolean editorPartList, boolean closed) {
        this.tabArea = tabArea;
        this.editorPartList = editorPartList;
        this.closed = closed;
    }

    public void show(Control control, Point displayCoordinates, boolean forcePosition) {
        AbstractPartListControl editorList;
        if(closed){
            editorList = new ClosedPartListControl(control.getShell(),
                    tabArea, editorPartList);
        } else {
            editorList = new PartListControl(control.getShell(),
                    tabArea, editorPartList, forcePosition);
        }
        editorList.setInput(tabArea);

        editorList.setPosition(displayCoordinates, forcePosition);

        editorList.setVisible(true);
        editorList.setFocus();
    }

    public void dispose() {
        tabArea = null;
    }

}
