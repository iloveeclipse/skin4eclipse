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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;

/**
 * @author Andrei
 *
 */
public class Sessions {

    public static final String RECENTLY_CLOSED = "Recently closed";

    private static final String SESSIONS_STATE_FILENAME = "sessions.xml";

    private static final String SESSION_SUFFIX = "_session.xml";

    private final Map/*<String, EditingSession>*/sessionsMap;

    private static Sessions instance;

    public static synchronized Sessions getInstance() {
        if (instance == null) {
            instance = new Sessions();
        }
        return instance;
    }

    private Sessions() {
        super();
        sessionsMap = new TreeMap();
        readSessions();
    }

    /**
     * @param name may be null or empty
     * @return may return null
     */
    public EditingSession getSession(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        return (EditingSession) sessionsMap.get(name);
    }

    private void add(EditingSession editingSession, String sessionName) {
        sessionsMap.put(sessionName, editingSession);
    }

    private void remove(String sessionName) {
        EditingSession session = (EditingSession) sessionsMap.remove(sessionName);
        if (session != null) {
            boolean deleted = session.getPath().delete();
            if(!deleted){
                PresentationPlugin.log("Couldn't delete session file: " + session.getPath(), null);
            }
            storeSessions();
        }
    }

    /**
     * @return non modifiable list of known sessions
     */
    public List/*<EditingSession>*/getSessions(boolean refreshFromFile) {
        if (refreshFromFile) {
            sessionsMap.clear();
            readSessions();
        }
        return new ArrayList(sessionsMap.values());
    }

    /**
     * Returns the file used as the persistence store,
     * or <code>null</code> if there is no available file.
     *
     * @return the file used as the persistence store, or <code>null</code>
     */
    private File getSessionsFile() {
        IPath path = PresentationPlugin.getDefault().getStateLocation();
        if (path == null) {
            return null;
        }
        path = path.append(SESSIONS_STATE_FILENAME);
        return path.toFile();
    }

    private File getSessionFile(String sessionName) {
        IPath dirPath = PresentationPlugin.getDefault().getStateLocation();
        if (dirPath == null) {
            return null;
        }
        int i = 0;
        File toFile = dirPath.append(sessionName + SESSION_SUFFIX).toFile();
        while (toFile.exists() && i < 10000) {
            toFile = dirPath.append(sessionName + (i++) + SESSION_SUFFIX).toFile();
        }
        return toFile;
    }

    private boolean storeSession(EditingSession session) {
        XMLMemento memento = XMLMemento.createWriteRoot("session");
        boolean ok = session.saveState(memento);
        if(!ok){
            return false;
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(session.getPath());
            memento.save(writer);
            // if saved, then update sessions file too
            storeSessions();
        } catch (IOException e) {
            PresentationPlugin.log("Couldn't store session " + session.getName(), e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    PresentationPlugin.log(null, e);
                }
            }
        }
        return true;
    }

    private void storeSessions() {
        File file = getSessionsFile();
        XMLMemento memento = XMLMemento.createWriteRoot("sessions");
        List sessions = getSessions(false);
        for (int i = 0; i < sessions.size(); i++) {
            IMemento childMem = memento.createChild("session");
            childMem.putString("name", ((EditingSession) sessions.get(i)).getName());
            childMem.putString("path", ((EditingSession) sessions.get(i)).getPath()
                    .getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            memento.save(writer);
        } catch (IOException e) {
            PresentationPlugin.log("Couldn't write sessions file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    PresentationPlugin.log(null, e);
                }
            }
        }
    }

    private void readSessions() {
        File sessionsStateFile = getSessionsFile();
        if (!sessionsStateFile.isFile()) {
            return;
        }
        FileInputStream input = null;
        BufferedReader reader;
        IMemento memento;
        try {
            input = new FileInputStream(sessionsStateFile);
            reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
            memento = XMLMemento.createReadRoot(reader);
        } catch (FileNotFoundException e) {
            PresentationPlugin.log("Couldn't read session file: " + sessionsStateFile, e);
            return;
        } catch (IOException e) {
            PresentationPlugin.log("Couldn't read session file: " + sessionsStateFile, e);
            return;
        } catch (CoreException e) {
            PresentationPlugin.log("Couldn't read session file: " + sessionsStateFile, e);
            return;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    PresentationPlugin.log(null, e);
                }
            }
        }

        IMemento[] mementos = memento.getChildren("session");
        for (int i = 0; i < mementos.length; i++) {
            String sessionName = mementos[i].getString("name");
            String sessionPath = mementos[i].getString("path");
            if (sessionPath == null) {
                PresentationPlugin.log("Couldn't read session, path is missing: "
                        + sessionName, null);
                continue;
            }
            EditingSession editingSession = new EditingSession(sessionName, new File(
                    sessionPath));
            add(editingSession, sessionName);
        }
    }

    public EditingSession createSession(String sessionName) {
        return createSession(sessionName, checkOverride(sessionName));
    }

    public EditingSession createSession(String sessionName, boolean overrideExisting) {
        if (overrideExisting) {
            deleteSession(sessionName);
        } else if (getSession(sessionName) != null) {
            return getSession(sessionName);
        }
        EditingSession editingSession = new EditingSession(sessionName,
                getSessionFile(sessionName));
        add(editingSession, sessionName);
        editingSession.createEditorsSnapshot();
        storeSession(editingSession);
        return editingSession;
    }

    public void createSession(String sessionName, List/*<EditorInfo>*/editorInfos) {
        createSession(sessionName, editorInfos, false);
    }

    public void createSession(String sessionName, List/*<EditorInfo>*/editorInfos, boolean overrideExisting) {
        if (!overrideExisting && !checkOverride(sessionName)) {
            return;
        }
        deleteSession(sessionName);
        EditingSession editingSession = new EditingSession(sessionName,
                getSessionFile(sessionName));
        for (int i = 0; i < editorInfos.size(); i++) {
            editingSession.add((EditorInfo) editorInfos.get(i));
        }
        add(editingSession, sessionName);
        storeSession(editingSession);
    }

    public EditingSession deleteSession(String sessionName) {
        EditingSession oldSession = getSession(sessionName);
        if (oldSession == null) {
            return null;
        }
        remove(sessionName);
        return oldSession;
    }

    public void renameSession(String oldSessionName, String newSessionName) {
        if (!checkOverride(newSessionName)) {
            return;
        }
        EditingSession oldSession = deleteSession(oldSessionName);
        if (oldSession == null) {
            return;
        }
        oldSession.setName(newSessionName);
        add(oldSession, newSessionName);
        storeSession(oldSession);
    }

    public void copySession(String oldSessionName, String newSessionName) {
        if (getSession(newSessionName) != null) {
            return;
        }
        EditingSession oldSession = getSession(oldSessionName);
        if (oldSession == null) {
            return;
        }
        EditingSession newSession = new EditingSession(newSessionName,
                getSessionFile(newSessionName));
        List editors = oldSession.getEditors();
        for (int i = 0; i < editors.size(); i++) {
            newSession.add((EditorInfo) editors.get(i));
        }
        add(newSession, newSessionName);
        storeSession(newSession);
    }

    /**
     *
     * @return true if the session with given name is not exist yet or it is allowed to
     * override it
     */
    public boolean checkOverride(String sessionName) {
        if (getSession(sessionName) == null) {
            return true;
        }
        final IPreferenceStore prefs = PresentationPlugin.getDefault()
                .getPreferenceStore();
        boolean override = prefs.getBoolean(ThemeConstants.OVERRIDE_EXISTING_SESSION);
        if (!override) {
            override = MessageDialog.openConfirm(null, "Save session",
                    "Session with given name already exist. Should the session\n'"
                            + sessionName + "'\nbe overriden?");
        }
        return override;
    }

}
