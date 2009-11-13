/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions;

import org.eclipse.jface.dialogs.IInputValidator;

public class SessionNameValidator implements IInputValidator {

    boolean allowOverride;

    public SessionNameValidator() {
        super();
    }

    public SessionNameValidator(boolean allowOverride) {
        this();
        this.allowOverride = allowOverride;
    }

    public String isValid(String newText) {
        if (newText == null || newText.trim().length() == 0) {
            return "Session name must be non empty";
        }
        char[] cs = newText.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] != ' ' && !Character.isJavaIdentifierPart(cs[i])) {
                return "Session name could contain only letters or digits";
            }
        }
        if(allowOverride) {
            return null;
        }
        return Sessions.getInstance().getSession(newText) != null ? "Session"
                + " with given name already exist" : null;
    }
}