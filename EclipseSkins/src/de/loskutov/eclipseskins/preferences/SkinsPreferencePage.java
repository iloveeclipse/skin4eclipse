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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ui.UIUtils;


/**
 * @author Andrei
 */
public class SkinsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
SelectionListener {

    private static final class IntModifyListener implements ModifyListener {
        private final int defaultValue;
        private final Text text;
        private final int minValue;
        private final int maxValue;

        IntModifyListener(Text text, int defaultValue,
                int minValue, int maxValue) {
            super();
            this.text = text;
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public void modifyText(ModifyEvent e) {
            String number = ((Text) e.widget).getText();
            if (number != null) {
                number = number.trim();
            }
            try {
                int value = Integer.parseInt(number);
                if (value < minValue || value > maxValue) {
                    text.setText("" + defaultValue);
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
    }

    private static final String TOOLTIP_SUFFIX = "Tip";   //$NON-NLS-1$
    private static final int DEFAULT_MAX_TAB_WIDTH = 16;
    private static final int DEFAULT_MOVE_TAB_AMOUNT = 9;
    private static final int DEFAULT_BORDER_SIZE = 1;
    private static final int DEFAULT_PADDING_X = 2;
    private static final int DEFAULT_PADDING_Y = 2;
    private static final int DEFAULT_TOOLBAR_FIX = 0;

    protected Text tabWidthText;
    protected Text moveTabAmount;
    protected Text borderSize;
    protected Text tabPaddingX;
    protected Text tabPaddingY;
    protected Button useMaxTabWidth;
    protected Button showFileExt;
    protected Button showViewIcon;
    protected Button hideViewTitle;
    protected Button showEditorIcon;
    protected Button cropInTheMiddle;
    protected Button minimizeToCoolbar;
    protected Button closeTabOnMiddleClick;
    protected Button useFastTabSwitch;
    protected Button copyFullTabTitle;
    protected ITheme lastUsedTheme;
    private TabFolder tabFolder;
    private Button alwaysSortEditorTabs;
    private Button alwaysSortViewTabs;
    private Button escClosesDetachedViews;
    private Text gtkToolbarFix;

    public SkinsPreferencePage() {
        super();
        setPreferenceStore(PresentationPlugin.getDefault().getPreferenceStore());
    }

    public void dispose() {
        if(tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        lastUsedTheme = null;

        super.dispose();
    }

    /*
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {

        tabFolder = new TabFolder(parent, SWT.TOP);

        createTabUI();
        createTabMisc();
        return tabFolder;
    }

    private void createTabUI() {
        Composite defPanel = createContainer(tabFolder);

        TabItem tabUI = new TabItem(tabFolder, SWT.NONE);
        tabUI.setText("UI settings");
        tabUI.setControl(defPanel);

        //-------------------------------------------------------------------------------

        lastUsedTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        Group userPrefsComposite = new Group(defPanel, SWT.SHADOW_ETCHED_IN);
        GridLayout layout = new GridLayout();
        userPrefsComposite.setLayout(layout);
        GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
        userPrefsComposite.setLayoutData(gridData);

        userPrefsComposite.setText(
                PresentationPlugin.getResourceString("customUserPrefs")); //$NON-NLS-1$

        IPreferenceStore prefs = getPreferenceStore();
        minimizeToCoolbar = createLabeledCheck(
                "minimizeToCoolbar", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.ENABLE_NEW_MIN_MAX),
                userPrefsComposite);

        closeTabOnMiddleClick = createLabeledCheck(
                "closeTabOnMiddleClick", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK),
                userPrefsComposite);
        useFastTabSwitch = createLabeledCheck(
                "useFastTabSwitch", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.USE_FAST_TAB_SWITCH),
                userPrefsComposite);

        copyFullTabTitle = createLabeledCheck(
                "copyFullTabTitle", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.COPY_FULL_TAB_TITLE),
                userPrefsComposite);

        alwaysSortEditorTabs = createLabeledCheck(
                "alwaysSortEditorTabs", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.ALWAYS_SORT_EDITOR_TABS),
                userPrefsComposite);

        alwaysSortViewTabs = createLabeledCheck(
                "alwaysSortViewTabs", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.ALWAYS_SORT_VIEW_TABS),
                userPrefsComposite);

        escClosesDetachedViews = createLabeledCheck(
                "escClosesDetachedViews", //$NON-NLS-1$
                prefs.getBoolean(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS),
                userPrefsComposite);

        Group wrappedTabsComposite = new Group(defPanel, SWT.SHADOW_ETCHED_IN);
        layout = new GridLayout();
        wrappedTabsComposite.setLayout(layout);
        gridData =
            new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        wrappedTabsComposite.setLayoutData(gridData);

        wrappedTabsComposite.setText(
                PresentationPlugin.getResourceString("customThemePrefs")); //$NON-NLS-1$

        tabWidthText = createLabeledText(
                "maxTabWidth", //$NON-NLS-1$
                "" + getMergedIntPreference(ThemeConstants.MAX_TAB_WIDTH, lastUsedTheme),
                wrappedTabsComposite,
                false);
        tabWidthText.setTextLimit(2);

        useMaxTabWidth = createLabeledCheck(
                "useMaxTabWidth", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.USE_MAX_TAB_WIDTH, lastUsedTheme),
                wrappedTabsComposite);

        showFileExt = createLabeledCheck(
                "showFileExtensions", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.SHOW_FILE_EXTENSIONS, lastUsedTheme),
                wrappedTabsComposite);

        showViewIcon = createLabeledCheck(
                "showViewIcon", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.SHOW_VIEW_ICON, lastUsedTheme),
                wrappedTabsComposite);

        hideViewTitle = createLabeledCheck(
                "hideViewTitle", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.HIDE_VIEW_TITLE, lastUsedTheme),
                wrappedTabsComposite);

        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if(hideViewTitle.getSelection() && !showViewIcon.getSelection()){
                    showViewIcon.setSelection(true);
                }
            }
        };

        hideViewTitle.addSelectionListener(selectionAdapter);
        showViewIcon.addSelectionListener(selectionAdapter);

        showEditorIcon = createLabeledCheck(
                "showEditorIcon", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.SHOW_EDITOR_ICON, lastUsedTheme),
                wrappedTabsComposite);

        cropInTheMiddle = createLabeledCheck(
                "cropInTheMiddle", //$NON-NLS-1$
                getMergedBooleanPreference(ThemeConstants.CROP_IN_THE_MIDDLE, lastUsedTheme),
                wrappedTabsComposite);

        moveTabAmount = createLabeledText(
                "tabMoveAmount", //$NON-NLS-1$
                "" + getMergedIntPreference(ThemeConstants.MOVE_TAB_AMOUNT, lastUsedTheme),
                wrappedTabsComposite,
                false);
        moveTabAmount.setTextLimit(2);

        borderSize = createLabeledText(
                "borderSize", //$NON-NLS-1$
                "" + getMergedIntPreference(ThemeConstants.BORDER_SIZE, lastUsedTheme),
                wrappedTabsComposite,
                false);
        borderSize.setTextLimit(2);

        tabPaddingX = createLabeledText(
                "tabPaddingX", //$NON-NLS-1$
                "" + getMergedIntPreference(ThemeConstants.TAB_PADDING_X, lastUsedTheme),
                wrappedTabsComposite,
                false);
        tabPaddingX.setTextLimit(2);

        tabPaddingY = createLabeledText(
                "tabPaddingY", //$NON-NLS-1$
                "" + getMergedIntPreference(ThemeConstants.TAB_PADDING_Y, lastUsedTheme),
                wrappedTabsComposite,
                false);
        tabPaddingY.setTextLimit(2);
        if(UIUtils.isGtk){
            gtkToolbarFix = createLabeledText(
                    "gtkToolbarFix", //$NON-NLS-1$
                    "" + getMergedIntPreference(ThemeConstants.GTK_TOOLBAR_FIX, lastUsedTheme),
                    wrappedTabsComposite,
                    false);
            gtkToolbarFix.setTextLimit(2);
            gtkToolbarFix.addModifyListener(new IntModifyListener(gtkToolbarFix,
                    DEFAULT_TOOLBAR_FIX, 0, 22));
        }
        borderSize.addModifyListener(new IntModifyListener(borderSize,
                DEFAULT_BORDER_SIZE, 0, 100));
        moveTabAmount.addModifyListener(new IntModifyListener(moveTabAmount,
                DEFAULT_MOVE_TAB_AMOUNT, 1, 1000));
        tabWidthText.addModifyListener(new IntModifyListener(tabWidthText,
                DEFAULT_MAX_TAB_WIDTH, 1, 1000));
        tabPaddingX.addModifyListener(new IntModifyListener(tabPaddingX,
                DEFAULT_PADDING_X, 0, 100));
        tabPaddingY.addModifyListener(new IntModifyListener(tabPaddingY,
                DEFAULT_PADDING_Y, 0, 100));

        useMaxTabWidth.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                tabWidthText.setEditable(useMaxTabWidth.getSelection());
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // ignored
            }
        });
        tabWidthText.setEditable(useMaxTabWidth.getSelection());
    }

    private void createTabMisc() {
        TabItem tabManual = new TabItem(tabFolder, SWT.NONE);
        tabManual.setText("Misc...");

        Composite defPanel = createContainer(tabFolder);
        tabManual.setControl(defPanel);

        SupportPanel.createSupportLinks(defPanel);
    }

    /**
     * The default value is the value from current theme,
     * if no custom changes are stored in the preferences
     * @param key
     * @return if the value is set in preferences, then this value,
     * otherwise the value from preferences of current presentation theme
     */
    private int getMergedIntPreference(String key, ITheme currentTheme){
        boolean hasPrefs = getPreferenceStore().contains(key);
        int value = 0;
        if(hasPrefs){
            value = getPreferenceStore().getInt(key);
        } else {
            value = currentTheme.getInt(key);
        }
        return value;
    }

    /**
     * The default value is the value from current theme,
     * if no custom changes are stored in the preferences
     * @param key
     * @return if the value is set in preferences, then this value,
     * otherwise the value from preferences of current presentation theme
     */
    private boolean getMergedBooleanPreference(String key, ITheme currentTheme){
        boolean hasPrefs = getPreferenceStore().contains(key);
        boolean value = false;
        if(hasPrefs){
            value = getPreferenceStore().getBoolean(key);
        } else {
            value = currentTheme.getBoolean(key);
        }
        return value;
    }

    private Composite createContainer(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true));
        return composite;
    }

    /*
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // ignored
    }

    /*
     * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent selectionEvent) {
        widgetSelected(selectionEvent);
    }

    /*
     * @see SelectionListener#widgetSelected(SelectionEvent)
     */
    public void widgetSelected(SelectionEvent selectionEvent) {
        //       ignored
    }

    public boolean performOk() {
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        if(lastUsedTheme != currentTheme){
            // if we have custom values from old theme, and user has changed theme, then
            // we shouldn't re-set old values from old theme.
            return true;
        }
        IPreferenceStore store = getPreferenceStore();
        try {
            int maxTabWidth = Integer.parseInt(tabWidthText.getText());
            setValue(store, ThemeConstants.MAX_TAB_WIDTH, maxTabWidth);
        } catch (Exception e) {
            setValue(store, ThemeConstants.MAX_TAB_WIDTH, DEFAULT_MAX_TAB_WIDTH);
        }
        try {
            int tabMoveInt = Integer.parseInt(moveTabAmount.getText());
            setValue(store, ThemeConstants.MOVE_TAB_AMOUNT, tabMoveInt);
        } catch (Exception e) {
            store.setValue(ThemeConstants.MOVE_TAB_AMOUNT, DEFAULT_MOVE_TAB_AMOUNT);
        }
        try {
            int borderSizeInt = Integer.parseInt(borderSize.getText());
            setValue(store, ThemeConstants.BORDER_SIZE, borderSizeInt);
        } catch (Exception e) {
            store.setValue(ThemeConstants.BORDER_SIZE, DEFAULT_BORDER_SIZE);
        }
        try {
            int padXInt = Integer.parseInt(tabPaddingX.getText());
            setValue(store, ThemeConstants.TAB_PADDING_X, padXInt);
        } catch (Exception e) {
            store.setValue(ThemeConstants.TAB_PADDING_X, DEFAULT_PADDING_X);
        }
        try {
            int padYInt = Integer.parseInt(tabPaddingY.getText());
            setValue(store, ThemeConstants.TAB_PADDING_Y, padYInt);
        } catch (Exception e) {
            store.setValue(ThemeConstants.TAB_PADDING_Y, DEFAULT_PADDING_Y);
        }
        if(UIUtils.isGtk) {
            try {
                int gtkFix = Integer.parseInt(gtkToolbarFix.getText());
                setValue(store, ThemeConstants.GTK_TOOLBAR_FIX, gtkFix);
            } catch (Exception e) {
                store.setValue(ThemeConstants.GTK_TOOLBAR_FIX, DEFAULT_TOOLBAR_FIX);
            }
        }
        store.setValue(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK, closeTabOnMiddleClick
                .getSelection());

        store.setValue(ThemeConstants.ENABLE_NEW_MIN_MAX, minimizeToCoolbar
                .getSelection());

        // needed to set the workspace hidden pref

        IPreferenceStore apiPrefStore = PreferenceInitializer.getApiPrefStore();
        if (apiPrefStore.getBoolean(ThemeConstants.ENABLE_NEW_MIN_MAX) != minimizeToCoolbar
                .getSelection()) {
            PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();

            apiPrefStore.getBoolean(ThemeConstants.ENABLE_NEW_MIN_MAX);
        }
        apiPrefStore.setValue(
                ThemeConstants.ENABLE_NEW_MIN_MAX,
                minimizeToCoolbar.getSelection());

        store.setValue(ThemeConstants.USE_FAST_TAB_SWITCH, useFastTabSwitch
                .getSelection());

        store.setValue(ThemeConstants.COPY_FULL_TAB_TITLE, copyFullTabTitle
                .getSelection());

        store.setValue(ThemeConstants.ALWAYS_SORT_EDITOR_TABS, alwaysSortEditorTabs
                .getSelection());
        store.setValue(ThemeConstants.ALWAYS_SORT_VIEW_TABS, alwaysSortViewTabs
                .getSelection());
        store.setValue(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS, escClosesDetachedViews
                .getSelection());

        setValue(store, ThemeConstants.USE_MAX_TAB_WIDTH, useMaxTabWidth
                .getSelection());
        setValue(store, ThemeConstants.SHOW_FILE_EXTENSIONS, showFileExt
                .getSelection());
        setValue(store, ThemeConstants.CROP_IN_THE_MIDDLE, cropInTheMiddle
                .getSelection());
        setValue(store, ThemeConstants.SHOW_VIEW_ICON, showViewIcon
                .getSelection());
        setValue(store, ThemeConstants.HIDE_VIEW_TITLE, hideViewTitle
                .getSelection());
        setValue(store, ThemeConstants.SHOW_EDITOR_ICON, showEditorIcon
                .getSelection());
        return true;
    }

    private void setValue(IPreferenceStore store, String key, int value){
        boolean hasValue = store.contains(key);
        store.setValue(key, value);
        if(! hasValue){
            Integer integer = new Integer(value);
            store.firePropertyChangeEvent(key, null, integer);
            // problems with reading 0 values - they are the same as default
            store.putValue(key, "" + value);
        }
    }

    private void setValue(IPreferenceStore store, String key, boolean value){
        boolean hasValue = store.contains(key);
        store.setValue(key, value);
        if(! hasValue || ! value){
            /*
             * if default value is the same as given argument, then store
             * doesn't set it and later we can't recognize if the value was
             * customized by user... So we set it first to opposite value,
             * and then to the desired one.
             */
            if(! hasValue){
                store.setValue(key, !value);
                store.setValue(key, value);
            }
            // this set an string value as WORKAROUND
            store.putValue(key, "" + value);
        }
    }

    private Text createLabeledText(String titleId, String value, Composite defPanel1,
            boolean fillAllSpace) {
        Composite commonPanel = new Composite(defPanel1, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        commonPanel.setLayout(layout);
        commonPanel.setLayoutData(gridData);

        Label label = new Label(commonPanel, SWT.LEFT);
        label.setText(PresentationPlugin.getResourceString(titleId));
        label.setToolTipText(PresentationPlugin.getResourceString(titleId + TOOLTIP_SUFFIX));

        Text fText = new Text(commonPanel, SWT.SHADOW_IN | SWT.BORDER);
        if(fillAllSpace) {
            gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
            fText.setLayoutData(gridData);
        } else {
            gridData = new GridData();
            gridData.widthHint = 16;
            fText.setLayoutData(gridData);
        }
        fText.setText(value);
        fText.setToolTipText(PresentationPlugin.getResourceString(titleId + TOOLTIP_SUFFIX));
        return fText;
    }

    private Button createLabeledCheck(String titleId, boolean value, Composite defPanel1) {
        Button fButton = new Button(defPanel1, SWT.CHECK | SWT.LEFT);
        GridData data = new GridData();
        fButton.setLayoutData(data);
        fButton.setText(PresentationPlugin.getResourceString(titleId));
        fButton.setSelection(value);
        fButton.setToolTipText(PresentationPlugin.getResourceString(titleId + TOOLTIP_SUFFIX));
        return fButton;
    }

    //    private Button createLabeledRadio(String titleId, boolean value, Composite defPanel) {
    //        Button fButton = new Button(defPanel, SWT.RADIO);
    //        fButton.setText(PresentationPlugin.getResourceString(titleId));
    //        fButton.setSelection(value);
    //        fButton.setToolTipText(PresentationPlugin.getResourceString(titleId + TOOLTIP_SUFFIX));
    //        return fButton;
    //    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        super.performDefaults();
        IPreferenceStore store = getPreferenceStore();
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();

        PreferenceInitializer.performDefaults(store, currentTheme);
        useFastTabSwitch.setSelection(store
                .getDefaultBoolean(ThemeConstants.USE_FAST_TAB_SWITCH));
        closeTabOnMiddleClick.setSelection(store
                .getDefaultBoolean(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK));
        minimizeToCoolbar.setSelection(store
                .getDefaultBoolean(ThemeConstants.ENABLE_NEW_MIN_MAX));
        copyFullTabTitle.setSelection(store
                .getDefaultBoolean(ThemeConstants.COPY_FULL_TAB_TITLE));
        alwaysSortEditorTabs.setSelection(store
                .getDefaultBoolean(ThemeConstants.ALWAYS_SORT_EDITOR_TABS));
        alwaysSortViewTabs.setSelection(store
                .getDefaultBoolean(ThemeConstants.ALWAYS_SORT_VIEW_TABS));
        escClosesDetachedViews.setSelection(store
                .getDefaultBoolean(ThemeConstants.ESC_CLOSES_DETACHED_VIEWS));
        setValuesFromTheme(currentTheme);
        lastUsedTheme = currentTheme;
    }

    private void setValuesFromTheme(ITheme theme) {
        tabWidthText.setText("" + theme.getInt(ThemeConstants.MAX_TAB_WIDTH));
        moveTabAmount.setText("" + theme.getInt(ThemeConstants.MOVE_TAB_AMOUNT));
        borderSize.setText("" + theme.getInt(ThemeConstants.BORDER_SIZE));
        tabPaddingX.setText("" + theme.getInt(ThemeConstants.TAB_PADDING_X));
        tabPaddingY.setText("" + theme.getInt(ThemeConstants.TAB_PADDING_Y));
        if(UIUtils.isGtk) {
            gtkToolbarFix.setText("" + theme.getInt(ThemeConstants.GTK_TOOLBAR_FIX));
        }
        useMaxTabWidth.setSelection(theme.getBoolean(ThemeConstants.USE_MAX_TAB_WIDTH));
        showFileExt.setSelection(theme.getBoolean(ThemeConstants.SHOW_FILE_EXTENSIONS));
        cropInTheMiddle.setSelection(theme.getBoolean(ThemeConstants.CROP_IN_THE_MIDDLE));
        showViewIcon.setSelection(theme.getBoolean(ThemeConstants.SHOW_VIEW_ICON));
        hideViewTitle.setSelection(theme.getBoolean(ThemeConstants.HIDE_VIEW_TITLE));
        showEditorIcon.setSelection(theme.getBoolean(ThemeConstants.SHOW_EDITOR_ICON));
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(!visible){
            return;
        }
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        if(lastUsedTheme != currentTheme){
            setValuesFromTheme(currentTheme);
            lastUsedTheme = currentTheme;
        }
    }
}
