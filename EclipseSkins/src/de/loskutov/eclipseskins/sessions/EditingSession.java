/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.presentation.VSImprovedPresentationFactory;

/**
 * @author Andrei
 *
 */
public class EditingSession {

    private String name;

    private final List/*<EditorInfo>*/editors;

    private final File mementoPath;

    private boolean restored;

    public EditingSession(String name, File mementoPath) {
        this.mementoPath = mementoPath;
        editors = new ArrayList();
        setName(name);
    }

    public void setName(String name) {
        if (name == null) {
            this.name = "";
        } else {
            this.name = name;
        }
    }

    /**
     * Restore the most-recently-used history from the given memento.
     */
    protected void restoreState() {
        if (isRestored()) {
            return;
        }
        IMemento memento = createMemento();
        if (memento == null) {
            PresentationPlugin.log("Couldn't restore session " + getName(), null);
            return;
        }
        IMemento[] mementos = memento.getChildren("editor");
        for (int i = 0; i < mementos.length; i++) {
            EditorInfo item = new EditorInfo(mementos[i]);
            if (item.isConsistent()) {
                add(item);
            }
        }
        setRestored(true);
    }

    protected IMemento createMemento() {
        if (!mementoPath.isFile()) {
            return null;
        }
        FileInputStream input = null;
        BufferedReader reader;
        IMemento mem = null;
        try {
            input = new FileInputStream(mementoPath);
            reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
            mem = XMLMemento.createReadRoot(reader);
        } catch (FileNotFoundException e) {
            PresentationPlugin.log("Couldn't restore session " + getName(), e);
            return mem;
        } catch (IOException e) {
            PresentationPlugin.log("Couldn't restore session " + getName(), e);
            return mem;
        } catch (CoreException e) {
            PresentationPlugin.log("Couldn't restore session " + getName(), e);
            return mem;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    PresentationPlugin.log(null, e);
                }
            }
        }
        return mem;
    }

    public void add(EditorInfo item) {
        if (item.isConsistent() && !editors.contains(item)) {
            editors.add(item);
        }
    }

    public boolean isConsistent() {
        if (!isRestored()) {
            restoreState();
        }
        return editors.size() > 0;
    }

    public boolean saveState(IMemento newMemento) {
        List list = getSortedEditors();
        if(list.size() == 0){
            return false;
        }
        newMemento.putString("name", getName());
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            EditorInfo editorInfo = (EditorInfo) iterator.next();
            editorInfo.saveState(newMemento.createChild("editor"));
        }
        return true;
    }

    private List getSortedEditors() {
        Collections.sort(editors, new Comparator(){

            public int compare(Object o, Object o2) {
                if(o == o2){
                    return 0;
                }
                if(!(o instanceof EditorInfo) || !(o2 instanceof EditorInfo)){
                    return 0;
                }

                EditorInfo one = (EditorInfo) o;
                EditorInfo another = (EditorInfo) o2;

                if(!one.isConsistent() || !another.isConsistent()){
                    return 0;
                }
                if(one.getNumber() == another.getNumber()){
                    if(one.getName() != null && another.getName() != null) {
                        return one.getName().compareTo(another.getName());
                    }
                }
                return one.getNumber() - another.getNumber();
            }

        });
        return editors;
    }

    public void createEditorsSnapshot() {
        if(VSImprovedPresentationFactory.isPresentationActive()){
            List editorsShapshot = VSImprovedPresentationFactory.createEditorsShapshot();
            for (Iterator iterator = editorsShapshot.iterator(); iterator.hasNext();) {
                EditorInfo info = (EditorInfo) iterator.next();
                add(info);
            }
        } else {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference[] editorReferences = page.getEditorReferences();
            for (int i = 0; i < editorReferences.length; i++) {
                IEditorReference ref = editorReferences[i];
                try {
                    EditorInfo info = new EditorInfo(ref.getEditorInput(), ref.getId(), i);
                    add(info);
                } catch (PartInitException e) {
                    PresentationPlugin.log("Couldn't save editor state: " + ref.getId(), e);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public File getPath() {
        return mementoPath;
    }

    /**
     *
     * @return unmodifiable editors list
     */
    public List/*<EditorInfo>*/getEditors() {
        restoreState();
        return new ArrayList(editors);
    }

    protected void setRestored(boolean restored) {
        this.restored = restored;
    }

    protected boolean isRestored() {
        return restored;
    }

}
