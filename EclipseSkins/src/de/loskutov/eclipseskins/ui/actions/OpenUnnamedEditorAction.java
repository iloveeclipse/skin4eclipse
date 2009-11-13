/*******************************************************************************
 * Copyright (c) 2008 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

import de.loskutov.eclipseskins.PresentationPlugin;


public class OpenUnnamedEditorAction extends Action {

    private final IWizardDescriptor wizardDesc;

    public OpenUnnamedEditorAction() {
        super();
        wizardDesc = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(
        "org.eclipse.ui.editors.wizards.UntitledTextFileWizard");
        if(wizardDesc != null){
            setText("Open New " + wizardDesc.getLabel());
            setToolTipText(wizardDesc.getDescription());
            setImageDescriptor(wizardDesc.getImageDescriptor());
        } else {
            setText("Error: org.eclipse.ui.editors.wizards.UntitledTextFileWizard is missing");
        }
    }

    public void run() {
        if (wizardDesc == null) {
            return;
        }
        INewWizard wizard;
        try {
            wizard = (INewWizard) wizardDesc.createWizard();
        } catch (CoreException e) {
            PresentationPlugin.log("Failed to create untitled editor", e);
            return;
        }
        wizard.init(PlatformUI.getWorkbench(), null);
        wizard.performFinish();

    }
}
