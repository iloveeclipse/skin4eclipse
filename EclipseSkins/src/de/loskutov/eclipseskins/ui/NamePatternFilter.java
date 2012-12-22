/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Widget;

/**
 * The NamePatternFilter selects the elements which match the given string
 * patterns.
 */
public final class NamePatternFilter extends ViewerFilter {

	/**
     *
     */
    private static final String DIRTY_PREFIX = "*";
    private String pattern;

    public NamePatternFilter() {
        super();
    }

    public void setPattern(String pattern){
		if (pattern.length() == 0) {
			pattern = null;
        }
		this.pattern = pattern;
    }

    /*
     * (non-Javadoc) Method declared on ViewerFilter.
     */
    public boolean select(Viewer viewer, Object parentElement,
            Object element) {

        if (pattern == null || !(viewer instanceof TableViewer)) {
            return true;
        }
        TableViewer tableViewer = (TableViewer) viewer;

        String matchName = ((ILabelProvider) tableViewer.getLabelProvider())
                .getText(element);

        if(matchName == null) {
            return false;
        }
        // A dirty editor's label will start with dirty prefix, this prefix
        // should not be taken in consideration when matching with a pattern
        String prefix = DIRTY_PREFIX; // PartTab.DIRTY_PREFIX;
        if (matchName.startsWith(prefix)) {
            matchName = matchName.substring(prefix.length());
        }
        return matchName != null && matches(pattern, matchName);
    }

    private static boolean matches(String regex, CharSequence input) {
    	Pattern p;
        try {
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			// ignore
			return false;
		}
        Matcher m = p.matcher(input);
        return m.matches();
    }

    public Object findElement(Widget[] items, ILabelProvider labelProvider) {
        for (int i = 0; i < items.length; i++) {
            Object element = items[i].getData();
            if (pattern == null) {
                return element;
            }

            if (element != null) {
                String label = labelProvider.getText(element);
                if(label == null) {
                    return null;
                }
                // remove the dirty prefix from the editor's label
                String prefix = DIRTY_PREFIX; // PartTab.DIRTY_PREFIX;
                if (label.startsWith(prefix)) {
                    label = label.substring(prefix.length());
                }
                if (matches(pattern, label)) {
                    return element;
                }
            }
        }
        return null;
    }
}