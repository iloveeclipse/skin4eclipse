/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *    Fabio Zadrozny - option for esc not to close detached windows
 *******************************************************************************/

package de.loskutov.eclipseskins.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.ITheme;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;


/**
 * @author Andrei
 * @author fabioz
 */
public class PreferenceInitializer
extends
AbstractPreferenceInitializer {

    private static final String [] LAYOUT_PREFS = {
        ThemeConstants.MAX_TAB_WIDTH,
        ThemeConstants.BORDER_SIZE,
        ThemeConstants.USE_MAX_TAB_WIDTH,
        ThemeConstants.SHOW_FILE_EXTENSIONS,
        ThemeConstants.CROP_IN_THE_MIDDLE,
        ThemeConstants.SHOW_VIEW_ICON,
        ThemeConstants.SHOW_EDITOR_ICON,
        ThemeConstants.TAB_PADDING_X,
        ThemeConstants.TAB_PADDING_Y,
        ThemeConstants.EDITOR_TAB_POSITION,
        ThemeConstants.VIEW_TAB_POSITION,
        ThemeConstants.EDITOR_TAB_AREA_VISIBLE,
        ThemeConstants.ENABLE_NEW_MIN_MAX,
        ThemeConstants.ALWAYS_SORT_EDITOR_TABS,
        ThemeConstants.ALWAYS_SORT_VIEW_TABS,
        ThemeConstants.HIDE_VIEW_TITLE,
        ThemeConstants.GTK_TOOLBAR_FIX,
    };

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPreferences()
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PresentationPlugin.getDefault().getPreferenceStore();
        store.setDefault(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK, true);
        store.setDefault(ThemeConstants.USE_FAST_TAB_SWITCH, false);
        store.setDefault(ThemeConstants.COPY_FULL_TAB_TITLE, true);
        store.setDefault(ThemeConstants.EDITOR_LIST_PINNED, true);
        store.setDefault(ThemeConstants.STORE_PART_LIST_LOCATION, true);

        store.setDefault(ThemeConstants.ASK_BEFORE_CLOSE, true);
        store.setDefault(ThemeConstants.CLOSE_EDITORS, true);
        store.setDefault(ThemeConstants.OVERRIDE_EXISTING_SESSION, false);
        store.setDefault(ThemeConstants.ENABLE_NEW_MIN_MAX, true);
        store.setDefault(ThemeConstants.ALWAYS_SORT_EDITOR_TABS, false);
        store.setDefault(ThemeConstants.ALWAYS_SORT_VIEW_TABS, false);
        store.setDefault(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS, true);
        store.setDefault(ThemeConstants.GTK_TOOLBAR_FIX, 0);
        IPreferenceStore apiStore = getApiPrefStore();
        apiStore.setDefault(ThemeConstants.ENABLE_NEW_MIN_MAX, true);
        // all other values are non-default and will be initialized per theme
    }

    public static void performDefaults(IPreferenceStore store, ITheme theme){
        /*
         * remove custom values from store - the defaults should be
         * taken next time from the theme
         */
        store.setToDefault(ThemeConstants.MAX_TAB_WIDTH);
        store.setToDefault(ThemeConstants.MOVE_TAB_AMOUNT);
        store.setToDefault(ThemeConstants.BORDER_SIZE);
        store.setToDefault(ThemeConstants.USE_MAX_TAB_WIDTH);
        store.setToDefault(ThemeConstants.SHOW_FILE_EXTENSIONS);
        store.setToDefault(ThemeConstants.CROP_IN_THE_MIDDLE);
        store.setToDefault(ThemeConstants.SHOW_VIEW_ICON);
        store.setToDefault(ThemeConstants.SHOW_EDITOR_ICON);
        store.setToDefault(ThemeConstants.TAB_PADDING_X);
        store.setToDefault(ThemeConstants.TAB_PADDING_Y);
        store.setToDefault(ThemeConstants.EDITOR_TAB_POSITION);
        store.setToDefault(ThemeConstants.VIEW_TAB_POSITION);
        store.setToDefault(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK);
        store.setToDefault(ThemeConstants.USE_FAST_TAB_SWITCH);
        store.setToDefault(ThemeConstants.COPY_FULL_TAB_TITLE);
        store.setToDefault(ThemeConstants.EDITOR_LIST_PINNED);
        store.setToDefault(ThemeConstants.STORE_PART_LIST_LOCATION);
        store.setToDefault(ThemeConstants.ASK_BEFORE_CLOSE);
        store.setToDefault(ThemeConstants.CLOSE_EDITORS);
        store.setToDefault(ThemeConstants.OVERRIDE_EXISTING_SESSION);
        store.setToDefault(ThemeConstants.ENABLE_NEW_MIN_MAX);
        store.setToDefault(ThemeConstants.ALWAYS_SORT_EDITOR_TABS);
        store.setToDefault(ThemeConstants.ALWAYS_SORT_VIEW_TABS);
        store.setToDefault(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS);
        store.setToDefault(ThemeConstants.GTK_TOOLBAR_FIX);
        store.setToDefault(ThemeConstants.HIDE_VIEW_TITLE);
        /*
         * workbench prefs used here
         */
        if(theme instanceof ThemeWrapper){
            IPreferenceStore wStore = PlatformUI.getPreferenceStore();
            setInt(wStore, (ThemeWrapper)theme, ThemeConstants.VIEW_TAB_POSITION);
            setInt(wStore, (ThemeWrapper)theme, ThemeConstants.EDITOR_TAB_POSITION);
            setBoolean(wStore, (ThemeWrapper)theme, ThemeConstants.EDITOR_TAB_AREA_VISIBLE);
        }
    }

    private static void setInt(IPreferenceStore store, ThemeWrapper theme, String key){
        if (store.contains(key)) {
            theme.setInt(key, store.getInt(key));
        }
    }

    private static void setBoolean(IPreferenceStore store, ThemeWrapper theme, String key){
        if (store.contains(key)) {
            theme.setBoolean(key, store.getBoolean(key));
        }
    }

    public static void initializeCustomPreferences(ThemeWrapper theme) {
        IPreferenceStore store = PresentationPlugin.getDefault().getPreferenceStore();
        setInt(store, theme, ThemeConstants.MAX_TAB_WIDTH);
        setInt(store, theme, ThemeConstants.MOVE_TAB_AMOUNT);

        setInt(store, theme, ThemeConstants.BORDER_SIZE);
        setInt(store, theme, ThemeConstants.TAB_PADDING_X);
        setInt(store, theme, ThemeConstants.TAB_PADDING_Y);

        setBoolean(store, theme, ThemeConstants.USE_MAX_TAB_WIDTH);
        setBoolean(store, theme, ThemeConstants.SHOW_FILE_EXTENSIONS);
        setBoolean(store, theme, ThemeConstants.CROP_IN_THE_MIDDLE);
        setBoolean(store, theme, ThemeConstants.SHOW_EDITOR_ICON);
        setBoolean(store, theme, ThemeConstants.SHOW_VIEW_ICON);
        setBoolean(store, theme, ThemeConstants.HIDE_VIEW_TITLE);
        setBoolean(store, theme, ThemeConstants.ALWAYS_SORT_EDITOR_TABS);
        setBoolean(store, theme, ThemeConstants.ALWAYS_SORT_VIEW_TABS);
        setBoolean(store, theme, ThemeConstants.ESC_CLOSES_DETACHED_VIEWS);
        setInt(store, theme, ThemeConstants.GTK_TOOLBAR_FIX);

        /*
         * the followed prefs are not present on the Prefs GUI or are not own by our plugin
         *
         */

        // tab list prefs
        setBoolean(store, theme, ThemeConstants.TAB_LIST_SORT);
        setBoolean(store, theme, ThemeConstants.TAB_LIST_SEPARATE_VISIBLE_AND_INVISIBLE);
        setBoolean(store, theme, ThemeConstants.TAB_LIST_SHOW_FULL_PATH);
        setBoolean(store, theme, ThemeConstants.TAB_LIST_SHOW_VISIBLE_TABS_TOO);
        setBoolean(store, theme, ThemeConstants.TAB_LIST_VISIBLE_TABS_ARE_BOLD);
        setBoolean(store, theme, ThemeConstants.TAB_LIST_PINNED);

        // closed tab list prefs
        setBoolean(store, theme, ThemeConstants.CLOSED_TAB_LIST_SORT);
        setBoolean(store, theme, ThemeConstants.CLOSED_TAB_LIST_SHOW_FULL_PATH);

        /*
         * workbench prefs used here
         */
        IPreferenceStore wStore = PlatformUI.getPreferenceStore();
        setBoolean(wStore, theme, ThemeConstants.EDITOR_TAB_AREA_VISIBLE);
        setInt(wStore, theme, ThemeConstants.VIEW_TAB_POSITION);
        setInt(wStore, theme, ThemeConstants.EDITOR_TAB_POSITION);

        // Turn -on- the new Min/Max behaviour
        IPreferenceStore apiStore = getApiPrefStore();
        setBoolean(apiStore, theme, ThemeConstants.ENABLE_NEW_MIN_MAX);
    }

    public static IPreferenceStore getApiPrefStore() {
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        return apiStore;
    }

    /**
     *
     * @param preference
     * @return true for general layout preferences for the presentation, false for
     * all other prefs - like tab list prefs etc.
     */
    public static boolean isLayoutRelated(String preference){
        for (int i = 0; i < LAYOUT_PREFS.length; i++) {
            if(LAYOUT_PREFS[i].equals(preference)){
                return true;
            }
        }
        return false;
    }

}
