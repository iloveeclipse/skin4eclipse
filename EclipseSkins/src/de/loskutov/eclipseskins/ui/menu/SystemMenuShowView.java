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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.presentation.VSStackPresentation;

public class SystemMenuShowView extends ContributionItem {

    protected IStackPresentationSite site;

    private VSStackPresentation presentation;

    List others = new LinkedList();

    private static final String DATA_ITEM = "SystemMenuShowView.DATA_ITEM";

    public SystemMenuShowView(VSStackPresentation presentation) {
        super("showView");
        this.presentation = presentation;
        this.site = presentation.getSite();
        //        setId("showView");
    }

    public void dispose() {
        presentation = null;
        site = null;
        others = null;
    }

    public void fill(Menu menu, int index) {

        // Don't include a part list if there's only one part
        if (others == null || others.size() <= 0
                || presentation.getFlag(VSStackPresentation.F_TAB_AREA_VISIBLE)) {
            return;
        }

        new MenuItem(menu, SWT.SEPARATOR, index++);

        Collections.sort(others, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                IPresentablePart part0 = (IPresentablePart) arg0;
                IPresentablePart part1 = (IPresentablePart) arg1;

                return part0.getName().compareToIgnoreCase(part1.getName());
            }
        });

        for (int i = 0; i < others.size(); i++) {
            IPresentablePart part = (IPresentablePart) others.get(i);

            MenuItem item = new MenuItem(menu, SWT.NONE, index++);
            item.setText(part.getName());
            item.setImage(part.getTitleImage());
            item.addSelectionListener(selectionListener);
            item.setData(DATA_ITEM, part);
        }
    }

    public boolean isDynamic() {
        return true;
    }

    public void update() {
        setTarget(site.getSelectedPart());
    }

    /**
     * @since 3.1
     */
    public void setTarget(IPresentablePart current) {
        others.clear();
        others.addAll(Arrays.asList(site.getPartList()));
        others.remove(current);
    }

    private SelectionAdapter selectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            MenuItem item = (MenuItem) e.widget;

            IPresentablePart part = (IPresentablePart) item.getData(DATA_ITEM);

            if (part != null) {
                site.selectPart(part);
            }
        }
    };
}
