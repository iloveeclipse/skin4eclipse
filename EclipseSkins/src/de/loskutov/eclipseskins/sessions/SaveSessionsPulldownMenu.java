/*******************************************************************************
 * Copyright (c) 2008 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;


public class SaveSessionsPulldownMenu implements IWorkbenchWindowPulldownDelegate2 {
    private Menu menu;

    public SaveSessionsPulldownMenu() {
        super();
    }

    public Menu getMenu(Control parent) {
        setMenu(new Menu(parent));
        fillMenu(menu);
        initMenu();
        return menu;
    }

    public Menu getMenu(Menu parent) {
        setMenu(new Menu(parent));
        fillMenu(menu);
        initMenu();
        return menu;
    }

    private void setMenu(Menu newMenu) {
        if (menu != null) {
            menu.dispose();
        }
        menu = newMenu;
    }

    private void fillMenu(Menu menu2) {
        Sessions sessions = Sessions.getInstance();

        List sessionsList = sessions.getSessions(false);

        addSessionToMenu(menu2, null);
        Separator sep = new Separator();
        sep.fill(menu2, -1);

        EditingSession lastUsed = sessions.getSession(Sessions.RECENTLY_CLOSED);

        if (lastUsed != null) {
            sessionsList.remove(lastUsed);
        }

        for (int i = 0; i < sessionsList.size(); i++) {
            EditingSession session = (EditingSession) sessionsList.get(i);
            addSessionToMenu(menu2, session.getName());
        }
    }

    private void addSessionToMenu(Menu menu2, String name) {
        Action action = new SaveSessionAction(name);
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(menu2, -1);
    }

    protected void initMenu() {
        menu.addMenuListener(new MenuAdapter() {
            public void menuShown(MenuEvent e) {
                Menu m = (Menu) e.widget;
                MenuItem[] items = m.getItems();
                for (int i = 0; i < items.length; i++) {
                    items[i].dispose();
                }
                fillMenu(m);
            }
        });
    }

    public void dispose() {
        if (menu != null) {
            menu.dispose();
        }
    }

    public void init(IWorkbenchWindow window) {
        //
    }

    public void run(IAction action) {
        //
    }

    public void selectionChanged(IAction action, ISelection selection) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        action.setEnabled(window.getActivePage().getEditorReferences().length > 0);
    }

}
