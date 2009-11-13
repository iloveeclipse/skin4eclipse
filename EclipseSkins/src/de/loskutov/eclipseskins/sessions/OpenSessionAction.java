/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;

public class OpenSessionAction extends Action {



    private static final class OpenEditorsJob extends Job {

        private final IWorkbenchWindow window;

        private final IWorkbenchPage page;

        final List/*<EditorInfo>*/editorInfos;

        private OpenEditorsJob(String name, List/*<EditorInfo>*/editorInfos, IWorkbenchWindow window,
                IWorkbenchPage page) {
            super(name);
            this.window = window;
            this.page = page;
            this.editorInfos = editorInfos;
        }

        public IStatus run(final IProgressMonitor monitor) {


            final List/*<EditorInfo>*/failedInfos = new ArrayList();
            monitor.beginTask(getName(), editorInfos.size());
            final Display display = window.getShell().getDisplay();
            for (int i = 0; i < editorInfos.size(); i++) {
                if (monitor.isCanceled()) {
                    monitor.done();
                    return Status.CANCEL_STATUS;
                }
                monitor.internalWorked(1);
                final EditorInfo info = (EditorInfo) editorInfos.get(i);
                if (!info.isConsistent()) {
                    failedInfos.add(info);
                    continue;
                }
                monitor.subTask(info.getName());
                Runnable r = new Runnable() {
                    public void run() {
                        while (!monitor.isCanceled() && display.readAndDispatch()) {
                            // let proceed cancellation events
                        }
                        try {
                            info.openEditor(page);
                        } catch (PartInitException e) {
                            PresentationPlugin.log("Failed to restore: "
                                    + info + ", will try to open default text editor", e);
                            try {
                                info.openDefaultEditor(page);
                            } catch (PartInitException e2) {
                                PresentationPlugin.log("Can't oped default editor on: " + info, e);
                                failedInfos.add(info);
                            }
                        }
                    }
                };
                display.syncExec(r);
            }

            if (!failedInfos.isEmpty()) {
                final StringBuffer sb = new StringBuffer();
                for (int i = 0; i < failedInfos.size(); i++) {
                    EditorInfo info = (EditorInfo) failedInfos.get(i);
                    sb.append("\n" + info);
                }

                Runnable r = new Runnable() {
                    public void run() {
                        MessageDialog.openError(window.getShell(), "Sessions",
                                "Some of editors couldn't be restored: "
                                        + sb.toString());
                    }
                };
                display.syncExec(r);
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    }

    private final EditingSession session;

    public OpenSessionAction(EditingSession session) {
        super(session.getName());
        this.session = session;
    }

    public void run() {
        final IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();

        /*
         * get editors, because if this is a "recently closed" session, editors may
         * change because of updating info on close of current editors
         */
        List/*<EditorInfo>*/editorInfos = session.getEditors();
        if(editorInfos.size() == 0){
            MessageDialog.openInformation(null, "Open Session", "No editors to open!");
            return;
        }

        final IWorkbenchPage page = window.getActivePage();
        final IPreferenceStore prefs = PresentationPlugin.getDefault()
                .getPreferenceStore();
        boolean close = prefs.getBoolean(ThemeConstants.CLOSE_EDITORS);
        if (close && page.getEditorReferences().length > 0) {
            if (prefs.getBoolean(ThemeConstants.ASK_BEFORE_CLOSE)) {
                close = MessageDialog.openQuestion(window.getShell(), "Load Session",
                        "Close opened editors?");
            }
            if (close) {
                Sessions.getInstance().createSession(Sessions.RECENTLY_CLOSED, true);
                boolean ok = page.closeAllEditors(true);
                if (!ok) {
                    return;
                }
            }
        }

        Job job = new OpenEditorsJob("Opening editors", editorInfos, window, page);
        PlatformUI.getWorkbench().getProgressService().showInDialog(
                window.getShell(), job);
        job.schedule();
    }
}
