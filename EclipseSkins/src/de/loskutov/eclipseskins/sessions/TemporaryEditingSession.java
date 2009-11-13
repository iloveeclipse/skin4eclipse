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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import de.loskutov.eclipseskins.PresentationPlugin;

/**
 * @author Andrei
 *
 */
public class TemporaryEditingSession extends EditingSession {

    public TemporaryEditingSession(String name) {
        super(name, null);
    }

    public void setRestored(boolean restored) {
        super.setRestored(restored);
    }

    protected void restoreState() {
        if (isRestored()) {
            return;
        }
        IMemento memento = createMemento();
        if (memento == null) {
            String content = getClipboardContent();
            if(content == null) {
                return;
            }
            addFilesFromPlainText(content);
            setRestored(true);
        } else {
            IMemento[] mementos = memento.getChildren("editor");
            for (int i = 0; i < mementos.length; i++) {
                EditorInfo item = new EditorInfo(mementos[i]);
                if (item.isConsistent()) {
                    add(item);
                }
            }
            setRestored(true);
        }
    }

    private void addFilesFromPlainText(String content) {
        String[] strings = content.split("\\n|\\n\\r|\\r");
        for (int i = 0; i < strings.length; i++) {
            String path = strings[i].trim();
            if(path.length() == 0){
                continue;
            }
            File file = new File(path);
            if(!file.isFile()){
                continue;
            }
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                path = file.getAbsolutePath();
            }
            IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(path));
            EditorInfo item = new EditorInfo(fileStore);
            if (item.isConsistent()) {
                add(item);
            }
        }
    }

    protected IMemento createMemento() {
        Reader reader;
        IMemento mem = null;
        try {
            String content = getClipboardContent();
            if (content == null) {
                return null;
            }
            if (!content.startsWith("<?xml")) {
                return null;
            }
            reader = new StringReader(content);
            mem = XMLMemento.createReadRoot(reader);
        } catch (CoreException e) {
            PresentationPlugin.log("Couldn't restore session " + getName(), e);
            return mem;
        }
        return mem;
    }

    private static String getClipboardContent() {
        Clipboard cb = null;
        String content = null;
        try {
            cb = new Clipboard(Display.getDefault());
            content = (String) cb.getContents(TextTransfer.getInstance());
            if (content == null || content.length() == 0) {
                return null;
            }

        } finally {
            if (cb != null) {
                cb.dispose();
            }
        }
        return content;
    }

    public void storeToClipboard() {
        XMLMemento memento = XMLMemento.createWriteRoot("session");
        boolean ok = saveState(memento);
        if(!ok){
            return;
        }
        StringWriter writer = new StringWriter();
        Clipboard cb = null;
        try {
            cb = new Clipboard(Display.getDefault());
            memento.save(writer);
            cb.setContents(new String[] { writer.toString() }, new Transfer[] { TextTransfer
                    .getInstance() });
        } catch (IOException e) {
            PresentationPlugin.log("Couldn't store session to clipboard" + getName(), e);
        } finally {
            if (cb != null) {
                cb.dispose();
            }
        }
    }

}
