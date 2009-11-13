/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * @author Andrei
 */
public class SaveSessionAction extends Action implements IWorkbenchWindowActionDelegate {

    private String name;

    public SaveSessionAction() {
        super();
    }

    public SaveSessionAction(String name) {
        super(getName(name));
        this.name = name;
    }

    private static String getName(String name){
        return name == null? "&New Session" : name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        //
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        //
    }

    public void run() {
        run(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if(name != null){
            Sessions.getInstance().createSession(name, true);
            return;
        }
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        InputDialog dialog = new InputDialog(window.getShell(), "Save session",
                "Enter session name", null, new SessionNameValidator(true));
        int result = dialog.open();
        if (result == Window.CANCEL) {
            return;
        }
        Sessions.getInstance().createSession(dialog.getValue());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        action.setEnabled(window.getActivePage().getEditorReferences().length > 0);
    }

}
