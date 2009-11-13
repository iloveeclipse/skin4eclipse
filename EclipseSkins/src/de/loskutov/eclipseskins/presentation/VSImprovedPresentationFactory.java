/*******************************************************************************
 * Copyright (c) 2005 Willian Mitsuda.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Willian Mitsuda - initial API and implementation
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.presentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Factory for VS-based very improved stack presentation
 *
 * @author wmitsuda
 * @author Andrei
 */
public class VSImprovedPresentationFactory extends AbstractPresentationFactory {

    private static Set/*<VSEditorStackPresentation>*/ editorPresentations = new HashSet();

    public StackPresentation createEditorPresentation(Composite parent,
            IStackPresentationSite site) {
        VSEditorStackPresentation presentation = new VSEditorStackPresentation(parent, site, true);
        synchronized(VSImprovedPresentationFactory.class){
            editorPresentations.add(presentation);
        }
        return presentation;
    }

    public StackPresentation createViewPresentation(Composite parent,
            IStackPresentationSite site) {
        return new VSViewStackPresentation(parent, site, true);
    }

    public StackPresentation createStandaloneViewPresentation(Composite parent,
            IStackPresentationSite site, boolean showTitle) {
        return new VSViewStackPresentation(parent, site, true);
    }

    static synchronized void remove(VSEditorStackPresentation presentation){
        editorPresentations.remove(presentation);
    }

    public static List/*EditorInfo*/ createEditorsShapshot(){
        List list = new ArrayList();
        for (Iterator iterator = editorPresentations.iterator(); iterator.hasNext();) {
            VSEditorStackPresentation pres = (VSEditorStackPresentation) iterator.next();
            list.addAll(pres.getEditorInfos());
        }
        return list;
    }

    public static boolean isPresentationActive(){
        return !editorPresentations.isEmpty();
    }

}
