/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInputFactory;

import de.loskutov.eclipseskins.PresentationPlugin;

/**
 * @author Andrei
 * see org.eclipse.ui.internal.NavigationHistoryEditorInfo
 * org.eclipse.ui.IEditorReference
 * org.eclipse.ui.internal.EditorHistoryItem
 * org.eclipse.ui.internal.ReopenEditorMenu
 */
public class EditorInfo {

    private static final String DEFAULT_TEXT_EDITOR = "org.eclipse.ui.DefaultTextEditor";

    private IEditorInput input;

    private String editorId;

    private int number;

    private IFileStore file;

    public EditorInfo(IFileStore file) {
        this.file = file;
    }

    /**
     * Constructs a new item.
     */
    public EditorInfo(IEditorInput input, String editorId, int number) {
        this.input = input;
        this.editorId = editorId;
        this.number = number;
    }

    /**
     * Constructs a new item from a memento.
     */
    public EditorInfo(IMemento memento) {
        restoreState(memento);
    }

    /**
     * Returns the editor id.
     *
     * @return the editor id.
     */
    private String getEditorId() {
        return editorId;
    }

    /**
     * Returns the editor input.
     *
     * @return the editor input.
     */
    private IEditorInput getInput() {
        return input;
    }

    public boolean isOpenable() {
        return (file != null) || (getInput() != null && getEditorId() != null);
    }

    public boolean isConsistent() {
        return (file != null)
        || (getInput() != null && getInput().getPersistable() != null && getEditorId() != null);
    }

    /**
     * Restores the object state from the memento.
     */
    private void restoreState(IMemento memento) {
        if (memento == null) {
            return;
        }

        String factoryId = memento.getString("factoryID");
        if (factoryId == null) {
            PresentationPlugin.log("Couldn't restore editor info from memento: "
                    + memento.getID() + ", 'factoryID' is null", null);
            return;
        }
        IMemento persistableMemento = memento.getChild("persistable");
        if (persistableMemento == null) {
            PresentationPlugin.log("Couldn't restore editor info from memento: "
                    + memento.getID() + ", 'persistable' element is null", null);
            return;
        }
        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryId);
        if (factory == null) {
            // try to use default file factory, it would work if the memento contains a path to file
            String path = persistableMemento.getString("path");
            if (path == null) {
                PresentationPlugin.log("Couldn't find factory for id: "
                        + factoryId + ", I give up now...", null);
                return;
            }
            factory = PlatformUI.getWorkbench().getElementFactory(
                    FileEditorInputFactory.getFactoryId());
            PresentationPlugin.log("Couldn't find factory for id: "
                    + factoryId + ", will try to use default file factory", null);
        }
        IAdaptable adaptable = factory.createElement(persistableMemento);
        if (adaptable == null || (adaptable instanceof IEditorInput) == false) {
            return;
        }
        input = (IEditorInput) adaptable;
        editorId = memento.getString("id");
        Integer intNumber = memento.getInteger("number");
        number = intNumber == null? 0 : intNumber.intValue();
    }

    /**
     * Saves the object state in the given memento.
     *
     * @param memento the memento to save the object state in
     */
    public void saveState(IMemento memento) {
        if (!isConsistent() || input == null) {
            return;
        }
        IPersistableElement persistable = input.getPersistable();
        memento.putString("factoryID", persistable.getFactoryId());
        memento.putString("id", editorId);
        memento.putInteger("number", number);
        /*
         * Store IPersistable of the IEditorInput in a separate section
         * since it could potentially use a tag already used in the parent
         * memento and thus overwrite data.
         */
        IMemento persistableMemento = memento.createChild("persistable");
        persistable.saveState(persistableMemento);
    }


    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof EditorInfo) || !isConsistent()) {
            return false;
        }
        return toString().equals(obj.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public void setNumber(int i) {
        number = i;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("EditorInfo [id=");
        sb.append(editorId).append(", input: ").append(input).append(", file: ").append(file)
        .append("]");
        return sb.toString();
    }

    private void setEditorId(String editorId) {
        this.editorId = editorId;
    }

    public String getPath() {
        if(file != null){
            File localFile;
            try {
                localFile = file.toLocalFile(EFS.NONE,
                        new NullProgressMonitor());
                return getPath(localFile);
            } catch (CoreException e) {
                // ignore
            }
        }
        if(!isConsistent()){
            return null;
        }
        return createPath();
    }

    private String createPath() {
        Object adapter = input.getAdapter(IFile.class);
        if(adapter instanceof IFile){
            String location = getPath((IFile) adapter);
            if(location != null){
                return location;
            }
        }
        if(input instanceof IFileEditorInput){
            IFileEditorInput editorInput = (IFileEditorInput) input;
            String location = getPath(editorInput.getFile());
            if(location != null){
                return location;
            }
        }
        if(input instanceof IStorageEditorInput){
            String location = getPath((IStorageEditorInput) input);
            if(location != null){
                return location;
            }
        }
        if(input instanceof IURIEditorInput){
            IURIEditorInput editorInput = (IURIEditorInput) input;
            String location = getPath(editorInput.getURI());
            if(location != null){
                return location;
            }
        }
        if(input instanceof IPathEditorInput){
            IPathEditorInput editorInput = (IPathEditorInput) input;
            String location = getPath(editorInput.getPath());
            if(location != null){
                return location;
            }
        }

        return null;
    }

    static String getPath(IPath path) {
        if(path != null){
            return getPath(path.toFile());
        }
        return null;
    }

    private static String getPath(File localFile) {
        if(localFile!= null && localFile.isFile()){
            try {
                return localFile.getCanonicalPath();
            } catch (IOException e) {
                return localFile.getAbsolutePath();
            }
        }
        return null;
    }

    static String getPath(URI uri) {
        File localFile = getLocalFile(uri);
        return getPath(localFile);
    }

    static String getPath(IStorageEditorInput storageInp) {
        try {
            IStorage storage = storageInp.getStorage();
            return getPath(storage.getFullPath());
        } catch (CoreException e) {
            return null;
        }
    }

    static String getPath(IFile ifile){
        IPath location = ifile.getLocation();
        return getPath(location);
    }

    static File getLocalFile(URI uri) {
        if (uri != null) {
            try {
                IFileStore store = EFS.getStore(uri);
                if(store != null) {
                    return store.toLocalFile(EFS.NONE,
                            new NullProgressMonitor());
                }
            } catch (CoreException e) {
                // ignore
            }
        }
        return null;
    }

    public IEditorPart openEditor(IWorkbenchPage page) throws PartInitException {
        if(file != null){
            return IDE.openEditorOnFileStore(page, file);
        }
        if(isOpenable()){
            IDE.openEditor(page, input, editorId);
        }
        return null;
    }

    public IEditorPart openDefaultEditor(IWorkbenchPage page) throws PartInitException {
        setEditorId(DEFAULT_TEXT_EDITOR);
        if(input != null) {
            return page.openEditor(input, DEFAULT_TEXT_EDITOR);
        }
        return null;
    }

    public String getName() {
        if(input != null){
            return input.getName();
        }
        if(file != null){
            return file.getName();
        }
        return null;
    }

}
