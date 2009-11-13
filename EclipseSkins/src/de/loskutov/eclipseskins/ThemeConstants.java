/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins;

import org.eclipse.ui.IWorkbenchPreferenceConstants;


/**
 * Theme presentation constants
 * @author Andrei
 */
public interface ThemeConstants {
    String ID = "de.loskutov.EclipseSkins";

    String TAB_FONT = ID + ".font.tabs";
    String TITLE_FONT = ID + ".font.title";
    String BORDER_COLOR_FOCUS = ID + ".border.color.active";
    String BORDER_COLOR_NOFOCUS = ID + ".border.color.notactive";
    String TAB_COLOR_FOCUS = ID + ".tab.color.active";
    String TAB_DIRTY_TEXT_COLOR = ID + ".tab.text.color.dirty";
    String TAB_TEXT_COLOR_FOCUS = ID + ".tab.text.color.active";
    String TAB_TEXT_COLOR_NOFOCUS = ID + ".tab.text.color.notactive";
    String TAB_COLOR_NOFOCUS = ID + ".tab.color.notactive";

    String TITLE_COLOR_FOCUS = ID + ".title.color.active";
    String TITLE_COLOR_NOFOCUS = ID + ".title.color.notactive";

    String PART_LIST_BTN_COLOR = ID + ".partlist.btn.color";
    String CLOSED_PART_LIST_BTN_COLOR = ID + ".closed.partlist.btn.color";

    String TITLE_TEXT_COLOR_FOCUS = ID + ".titletext.color.active";
    String TITLE_TEXT_COLOR_NOFOCUS = ID + ".titletext.color.notactive";

    String BORDER_SIZE = ID + ".border.size";

    String MAX_TAB_WIDTH = ID + ".maxTabsWidth";
    String MOVE_TAB_AMOUNT = ID + ".moveTabAmount";
    String USE_MAX_TAB_WIDTH = ID + ".useMaxTabsWidth";
    String SHOW_FILE_EXTENSIONS = ID + ".showFileExtensions";
    String CROP_IN_THE_MIDDLE = ID + ".cropIndTheMiddle";
    String SHOW_VIEW_ICON = ID + ".showViewIcon";
    String SHOW_EDITOR_ICON = ID + ".showEditorIcon";

    // SWT.TOP or SWT.BOTTOM
    String VIEW_TAB_POSITION = IWorkbenchPreferenceConstants.VIEW_TAB_POSITION; //ID + ".viewTabsOnTop";
    String EDITOR_TAB_POSITION = IWorkbenchPreferenceConstants.EDITOR_TAB_POSITION ;//ID + ".editorTabsOnTop";

    String ENABLE_NEW_MIN_MAX = IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX;
    String EDITOR_TAB_AREA_VISIBLE = IWorkbenchPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS;

    // tab list prefs
    String TAB_LIST_SORT = ID + ".sortTabList";
    String TAB_LIST_SHOW_FULL_PATH = ID + ".showFullPath";
    String TAB_LIST_SHOW_VISIBLE_TABS_TOO = ID + ".showVisibleTabsToo";
    String TAB_LIST_VISIBLE_TABS_ARE_BOLD = ID + ".visibleTabsAreBold";
    String TAB_LIST_SEPARATE_VISIBLE_AND_INVISIBLE = ID + ".separateVisibleAndInvisible";
    String TAB_LIST_PINNED = ID + ".tabListPinned";
    String EDITOR_LIST_PINNED = ID + ".editorListPinned";

    // closed tab list prefs
    String CLOSED_TAB_LIST_SORT = ID + ".sortTabList.closed";
    String CLOSED_TAB_LIST_SHOW_FULL_PATH = ID + ".showFullPath.closed";

    String STORE_PART_LIST_LOCATION = ID + ".storePartListLocation";
    String PART_LIST_LOCATION_X = ID + ".partListLocation.x";
    String PART_LIST_LOCATION_Y = ID + ".partListLocation.y";

    String TAB_PADDING_X = ID + ".tabPaddingX";
    String TAB_PADDING_Y = ID + ".tabPaddingY";

    // some other ui prefs
    String CLOSE_TAB_ON_MIDDLE_CLICK = ID + ".closeTabOnMiddleClick";
    String USE_FAST_TAB_SWITCH = ID + ".useFastTabSwitch";

    String ALWAYS_SORT_VIEW_TABS = ID + ".alwaysSortViewTabs";
    String ALWAYS_SORT_EDITOR_TABS = ID + ".alwaysSortEditorTabs";

    // ESCAPE closes detached windows?
    String ESC_CLOSES_DETACHED_VIEWS = ID + ".escClosesDetachedViews";

    String COPY_FULL_TAB_TITLE = ID + ".copyFullTabTitle";

    String ASK_BEFORE_CLOSE = ID + ".askBeforeClose";

    String CLOSE_EDITORS = ID + ".closeEditors";

    String OVERRIDE_EXISTING_SESSION = ID + ".overrideExistingSession";

}
