/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.sessions.SessionNameValidator;
import de.loskutov.eclipseskins.sessions.Sessions;

/**
 * see org.eclipse.ui.internal.presentations.BasicPartList,
 * org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl,
 * org.eclipse.jdt.internal.ui.text.AbstractInformationControl
 * @author Andrei
 */
public class PartListControl extends AbstractPartListControl {

    protected ToolItem pinListButton;

    protected WindowListener windowListener;

    protected boolean visibleTabsAreBold;

    protected boolean showVisibleTabsToo;

    protected boolean separateVisibleAndInvisible;

    protected ToolItem saveButton;

    private final class ToggleShowVisibleTabsTooAction extends Action {
        public ToggleShowVisibleTabsTooAction() {
            super();
            setId("showVisibleTabsToo");
            showVisibleTabsToo = getBooleanFromTheme(ThemeConstants.TAB_LIST_SHOW_VISIBLE_TABS_TOO);
            setChecked(showVisibleTabsToo);
        }

        public void run() {
            showVisibleTabsToo = isChecked();
            setDependendActionsState(showVisibleTabsToo);
            setInput(null);
            // re-set position
            setPosition(rootControl.getLocation(), true);
            setBooleanToTheme(ThemeConstants.TAB_LIST_SHOW_VISIBLE_TABS_TOO,
                    showVisibleTabsToo);
        }

        protected void setDependendActionsState(boolean showVisibleTabsToo) {
            if (!showVisibleTabsToo) {
                Action action = (Action) actionMap.get("visibleTabsAreBold");
                action.setEnabled(false);
                action = (Action) actionMap.get("separateVisibleAndInvisible");
                action.setEnabled(false);
            } else {
                Action action = (Action) actionMap.get("visibleTabsAreBold");
                action.setEnabled(true);
                action = (Action) actionMap.get("separateVisibleAndInvisible");
                action.setEnabled(true);
            }
        }
    }

    private final class ToggleVisibleTabsAreBoldAction extends Action {
        public ToggleVisibleTabsAreBoldAction() {
            super();
            setId("visibleTabsAreBold");
            visibleTabsAreBold = getBooleanFromTheme(ThemeConstants.TAB_LIST_VISIBLE_TABS_ARE_BOLD);
            setChecked(visibleTabsAreBold);
            setEnabled(showVisibleTabsToo);
        }

        public void run() {
            visibleTabsAreBold = isChecked();
            setInput(null);
            // re-set position
            setPosition(rootControl.getLocation(), true);
            setBooleanToTheme(ThemeConstants.TAB_LIST_VISIBLE_TABS_ARE_BOLD,
                    visibleTabsAreBold);
        }
    }

    private final class ToggleSeparateVisibleAndInvisibleAction extends Action {
        public ToggleSeparateVisibleAndInvisibleAction() {
            super();
            setId("separateVisibleAndInvisible");
            separateVisibleAndInvisible = getBooleanFromTheme(ThemeConstants.TAB_LIST_SEPARATE_VISIBLE_AND_INVISIBLE);
            setChecked(separateVisibleAndInvisible);
            setEnabled(showVisibleTabsToo);
        }

        public void run() {
            separateVisibleAndInvisible = isChecked();
            setInput(null);
            setBooleanToTheme(ThemeConstants.TAB_LIST_SEPARATE_VISIBLE_AND_INVISIBLE,
                    separateVisibleAndInvisible);
        }
    }

    private final class MoveAction extends Action {
        MoveAction() {
            super("Move", IAction.AS_PUSH_BUTTON);
        }

        public void run() {
            isDeactivateListenerActive = false;
            Tracker tracker = new Tracker(rootControl.getDisplay(), SWT.NONE);
            tracker.setStippled(true);
            Rectangle[] r = new Rectangle[] { fFilterText.getShell().getBounds() };
            tracker.setRectangles(r);
            if (tracker.open()) {
                rootControl.setBounds(tracker.getRectangles()[0]);
                if (fBounds != null && restoresLocation()) {
                    IPreferenceStore store = getStore();
                    store.setValue(ThemeConstants.PART_LIST_LOCATION_X, fBounds.x);
                    store.setValue(ThemeConstants.PART_LIST_LOCATION_Y, fBounds.y);
                }
            }
            isDeactivateListenerActive = true;
        }
    }

    /**
     * The view menu's Remember Size and Location action.
     *
     * @since 3.0
     */
    private final class RememberBoundsAction extends Action {
        RememberBoundsAction() {
            super("Remember location", IAction.AS_CHECK_BOX);
            setChecked(restoresLocation());
        }

        public void run() {
            boolean newValue = isChecked();
            // store new value
            getStore().setValue(ThemeConstants.STORE_PART_LIST_LOCATION, newValue);
        }
    }

    private final class ResizeAction extends Action {
        ResizeAction() {
            super("Resize", IAction.AS_PUSH_BUTTON);
        }

        public void run() {
            Tracker tracker = new Tracker(rootControl.getDisplay(), SWT.RESIZE);
            tracker.setStippled(true);
            Rectangle[] r = new Rectangle[] { fFilterText.getShell().getBounds() };
            tracker.setRectangles(r);
            isDeactivateListenerActive = false;
            if (tracker.open()) {
                Rectangle rectangle = tracker.getRectangles()[0];
                rootControl.setBounds(rectangle);
                setInput(rectangle);
            }
            isDeactivateListenerActive = true;
        }
    }

    final class MyMouseListener extends MouseAdapter {
        private Menu menu;

        MyMouseListener() {
            super();
        }

        public void mouseUp(MouseEvent e) {
            Table table = fTableViewer.getTable();
            if (table.getSelectionCount() < 1) {
                return;
            }

            if (e.button == 1) {
                if (table.equals(e.getSource())) {
                    Object o = table.getItem(new Point(e.x, e.y));
                    TableItem selection = table.getSelection()[0];
                    if (selection.equals(o)) {
                        gotoSelectedElement();
                    }
                }
            }
            if (e.button == 3) {
                TableItem tItem = fTableViewer.getTable().getItem(new Point(e.x, e.y));
                if (tItem != null) {
                    getMenu().setVisible(true);
                }
            }
        }

        private Menu getMenu() {
            if (menu != null) {
                return menu;
            }
            menu = new Menu(fTableViewer.getTable());
            MenuItem mItem = new MenuItem(menu, SWT.NONE);
            mItem.setText(PresentationPlugin.getResourceString("action.close.text"));
            String iconPath = PresentationPlugin.getResourceString("action.close.icon");
            mItem.setImage(PresentationPlugin.getImage(iconPath));
            mItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent selectionEvent) {
                    removeSelectedItems();
                }
            });
            return menu;
        }
    }

    /**
     * Creates an information control with the given shell as parent. The given
     * styles are applied to the shell and the table widget.
     *
     * @param parent  the parent shell
     */
    public PartListControl(Shell parent, final TabArea tabArea,
            final boolean isEditorPartList, boolean forcePosition) {
        this.forcePosition = forcePosition;
        this.isEditorPartList = isEditorPartList;
        this.tabArea = tabArea;
        this.currentTheme = tabArea.getCurrentTheme();
        this.sortTabList = getBooleanFromTheme(ThemeConstants.TAB_LIST_SORT);
        mainWindowActive = false;
        isPinned = isEditorPartList && tabArea.isEditorListPinned();
        int shellStyle = SWT.RESIZE | SWT.ON_TOP | SWT.NO_TRIM;
        rootControl = new Shell(parent, shellStyle);
        rootControl.setBackground(rootControl.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        // Composite for filter text and viewer

        fComposite = new Composite(rootControl, SWT.RESIZE);
        GridLayout layout = new GridLayout(1, false);
        fComposite.setLayout(layout);
        fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fViewMenuButtonComposite = new Composite(fComposite, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fViewMenuButtonComposite.setLayout(layout);
        fViewMenuButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fFilterText = createFilterText(fViewMenuButtonComposite);
        createViewMenu(fViewMenuButtonComposite);

        // Horizontal separator line

        separator = new Label(fComposite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // init actions before table - TODO refactor
        getViewMenuManager();

        fTableViewer = createTableViewer(fComposite, SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = fTableViewer.getTable();

        tooltip = new ToolTipHandler(fComposite.getShell());
        tooltip.activateHoverHelp(table);

        table.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC) {
                    dispose();
                } else if (e.character == SWT.DEL) {
                    removeSelectedItems();
                    e.character = SWT.NONE;
                    e.doit = false;
                }
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        table.addSelectionListener(new SelectionAdapter() {
            /**
             * we override the widget*Default*Selected method, because the
             * widgetSelected method doesn't allow right click on elements in the list
             * without to select them
             */
            public void widgetDefaultSelected(SelectionEvent e) {
                gotoSelectedElement();
            }
        });

        /*
         * Bug in GTK, see SWT bug: 62405 Editor drop down performance slow on
         * Linux-GTK on mouse move.
         * Rather then removing the support altogether this feature has been
         * worked around for GTK only as we expect that newer versions of GTK
         * will no longer exhibit this quality and we will be able to have the
         * desired support running on all platforms. See
         * comment https://bugs.eclipse.org/bugs/show_bug.cgi?id=62405#c22
         * TODO: remove this code once bug 62405 is fixed for the mainstream GTK
         * version
         */
        final int ignoreEventCount = Platform.getWS().equals(Platform.WS_GTK) ? 4 : 1;

        table.addMouseMoveListener(new MyMouseMoveListener(ignoreEventCount));

        table.addMouseListener(new MyMouseListener());

        int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        rootControl.setLayout(new BorderFillLayout(border));

        setInfoSystemColor();
        installFilter();

        rootControl.addControlListener(new ControlAdapter() {
            public void controlMoved(ControlEvent e) {
                fBounds = rootControl.getBounds();
                if (fTrim != null) {
                    Point location = fComposite.getLocation();
                    fBounds.x = fBounds.x - fTrim.x + location.x;
                    fBounds.y = fBounds.y - fTrim.y + location.y;
                }
            }

            public void controlResized(ControlEvent e) {
                fBounds = rootControl.getBounds();
                if (fTrim != null) {
                    Point location = fComposite.getLocation();
                    fBounds.x = fBounds.x - fTrim.x + location.x;
                    fBounds.y = fBounds.y - fTrim.y + location.y;
                }
            }
        });
        fDeactivateListener = new DeactivateListener();
        rootControl.addListener(SWT.Activate, fDeactivateListener);
        rootControl.addListener(SWT.Deactivate, fDeactivateListener);
        rootControl.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                clearReferences();
            }
        });
        isDeactivateListenerActive = true;
        layoutListener = new Listener() {
            public void handleEvent(Event event) {
                if (fTableViewer != null) {
                    fTableViewer.refresh();
                    //                    setInput(tabArea);
                }
            }
        };
        tabArea.addLayoutListener(layoutListener);

        IWorkbench workbench = PresentationPlugin.getDefault().getWorkbench();
        windowListener = new WindowListener(workbench.getActiveWorkbenchWindow());
        workbench.addWindowListener(windowListener);
    }

    public void dispose() {
        if (isDisposed) {
            return;
        }
        if (PresentationPlugin.DEBUG) {
            System.out.println("dispose list");
        }

        if (rootControl != null && !rootControl.isDisposed()) {
            rootControl.dispose();
        }
        clearReferences();
        isDisposed = true;
    }

    protected void clearReferences() {
        if(PresentationPlugin.DEBUG) {
            System.out.println("part list disposed");
        }

        if (tabArea != null) {
            tabArea.removeLayoutListener(layoutListener);
        }
        if (windowListener != null) {
            IWorkbench workbench = PresentationPlugin.getDefault().getWorkbench();
            workbench.removeWindowListener(windowListener);
        }
        fBounds = null;
        fFilterText = null;
        viewMenuButton = null;
        pinListButton = null;
        sortListButton = null;
        saveButton = null;
        fViewMenuButtonComposite = null;
        layoutListener = null;
        fDeactivateListener = null;
        windowListener = null;
        separator = null;
        tooltip = null;
        fTableViewer = null;
        fComposite = null;
        rootControl = null;
        currentTheme = null;
        tabArea = null;
        namePatternFilter = null;
    }

    /*
     * @see org.eclipse.jface.text.IInformationControlExtension3#restoresLocation()
     */
    public boolean restoresLocation() {
        return isEditorPartList && !forcePosition
        && getStore().getBoolean(ThemeConstants.STORE_PART_LIST_LOCATION);
    }

    protected void createViewMenu(Composite toolbar) {
        fToolBar = new ToolBar(toolbar, SWT.FLAT);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.END;
        data.verticalAlignment = GridData.BEGINNING;
        fToolBar.setLayoutData(data);
        if (isEditorPartList) {
            saveButton = new ToolItem(fToolBar, SWT.PUSH | SWT.FLAT);
            UIUtils.initButton(saveButton, "saveList");
            saveButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    saveList();
                }
            });
        }
        sortListButton = new ToolItem(fToolBar, SWT.CHECK | SWT.FLAT);
        sortListButton.setSelection(sortTabList);
        UIUtils.initButton(sortListButton, "sortTabList");
        sortListButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSortEnabled(sortListButton.getSelection());
            }
        });

        if (isEditorPartList) {
            pinListButton = new ToolItem(fToolBar, SWT.CHECK | SWT.FLAT);
            pinListButton.setSelection(isPinned);
            UIUtils.initButton(pinListButton, "pinList");
            pinListButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    toggleListPin(pinListButton.getSelection());
                }
            });
        }
        viewMenuButton = new ToolItem(fToolBar, SWT.PUSH | SWT.FLAT);
        UIUtils.initButton(viewMenuButton, "viewMenu");
        viewMenuButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showViewMenu();
            }
        });
    }

    protected void saveList() {
        IPresentablePart[] partList = tabArea.getPartList();
        VSStackPresentation presentation = tabArea.getPresentation();
        List/*<EditorInfo>*/ infos = new ArrayList();
        for (int i = 0; i < partList.length; i++) {
            EditorInfo info = presentation.createEditorInfo(partList[i]);
            if(info != null){
                infos.add(info);
            }
        }
        if(infos.isEmpty()){
            // TODO show message
            return;
        }
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        InputDialog dialog = new InputDialog(window.getShell(), "Save session",
                "Enter session name", null, new SessionNameValidator(true));
        int result = dialog.open();
        if(result == Window.CANCEL){
            return;
        }
        Sessions.getInstance().createSession(dialog.getValue(), infos);
    }

    /**
     *
     */
    protected void toggleListPin(boolean selected) {
        isPinned = selected;
        if (tabArea != null) {
            tabArea.setEditorListPinned(selected);
        }
        getStore().setValue(ThemeConstants.TAB_LIST_PINNED, selected);
    }

    /**
     * Fills the view menu.
     * Clients can extend or override.
     *
     * @param viewMenu the menu manager that manages the menu
     * @since 3.0
     */
    protected void fillViewMenu(IMenuManager viewMenu) {
        actionMap = new HashMap();
        if (isEditorPartList && !forcePosition) {
            viewMenu.add(new MoveAction());
            viewMenu.add(new ResizeAction());
            viewMenu.add(new RememberBoundsAction());
            viewMenu.add(new Separator("SystemMenuEnd")); //$NON-NLS-1$
        }
        Action action;
        if (isEditorPartList) {
            action = new ToggleShowFullPathAction();
            UIUtils.initAction(action);
            viewMenu.add(action);
            actionMap.put(action.getId(), action);
        }

        if (isEditorPartList) {
            showVisibleTabsToo = true;
        } else {
            action = new ToggleShowVisibleTabsTooAction();
            UIUtils.initAction(action);
            viewMenu.add(action);
            actionMap.put(action.getId(), action);
        }

        action = new ToggleVisibleTabsAreBoldAction();
        UIUtils.initAction(action);
        viewMenu.add(action);
        actionMap.put(action.getId(), action);

        action = new ToggleSeparateVisibleAndInvisibleAction();
        UIUtils.initAction(action);
        viewMenu.add(action);
        actionMap.put(action.getId(), action);
    }

    /**
     * Removes the selected items from the list and closes their corresponding tabs
     * Selects the next item in the list or disposes it if its presentation is disposed
     */
    protected void removeSelectedItems() {
        Table table = fTableViewer.getTable();
        int selInd = table.getSelectionIndex();
        if (deleteSelectedElements()) {
            return;
        }
        fTableViewer.refresh();
        if (selInd >= table.getItemCount()) {
            selInd = table.getItemCount() - 1;
        }
        if (selInd >= 0) {
            table.setSelection(selInd);
        }
    }

    private final class BasicStackListContentProvider implements
    IStructuredContentProvider {

        public BasicStackListContentProvider() {
            //no-op
        }

        public void dispose() {
            //no-op
        }

        public Object[] getElements(Object inputElement) {
            if (tabArea == null) {
                return new IPresentablePart[0];
            }
            if (showVisibleTabsToo) {
                // show all tabs
                return tabArea.getPartList();
            }
            return tabArea.getHiddenPartList();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // ignore
        }
    }

    private final class BasicStackListLabelProvider extends LabelProvider implements
    IFontProvider, IColorProvider {

        private static final String CURRENT = " [current]";

        private Font boldFont;

        public BasicStackListLabelProvider() {
            //no-op
        }

        public String getText(Object element) {
            IPresentablePart selectedPart = tabArea.getSelectedPart();
            IPresentablePart presentablePart = (IPresentablePart) element;
            boolean isSelected = selectedPart == presentablePart;

            String label = getLabel(presentablePart);
            if (isSelected) {
                label += CURRENT;
            }
            return label;
        }

        private String getLabel(IPresentablePart presentablePart) {
            if (showFullPath) {
                String text = presentablePart.getTitleToolTip();
                if (text == null || text.length() == 0) {
                    text = presentablePart.getName();
                }
                if (presentablePart.isDirty()) {
                    return PartTab.DIRTY_PREFIX + text;
                }
                return text;
            }
            if (presentablePart.isDirty()) {
                return PartTab.DIRTY_PREFIX + presentablePart.getName();
            }
            return presentablePart.getName();
        }

        public Image getImage(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            return presentablePart.getTitleImage();
        }

        public Font getFont(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            PartTab partTab = tabArea.getTab(presentablePart);
            boolean visible = !partTab.isHidden();
            IPresentablePart selectedPart = tabArea.getSelectedPart();
            boolean isSelected = selectedPart == presentablePart;
            boolean showBold = isSelected || (visible && visibleTabsAreBold);

            if (!showBold) {
                return tabArea.getFont();
            }

            if (boldFont == null) {
                Control control = tabArea;
                Font originalFont = control.getFont();
                FontData fontData[] = originalFont.getFontData();
                // Adding the bold attribute
                for (int i = 0; i < fontData.length; i++) {
                    fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
                }
                boldFont = new Font(control.getDisplay(), fontData);
            }
            return boldFont;
        }

        public void dispose() {
            if (boldFont != null) {
                boldFont.dispose();
                boldFont = null;
            }
            super.dispose();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        public Color getForeground(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            PartTab partTab = tabArea.getTab(presentablePart);
            boolean dirty = partTab.getPart().isDirty();
            if (dirty) {
                return rootControl.getDisplay().getSystemColor(SWT.COLOR_RED);
            }
            return rootControl.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        public Color getBackground(Object element) {
            IPresentablePart presentablePart = (IPresentablePart) element;
            PartTab partTab = tabArea.getTab(presentablePart);
            boolean visible = !partTab.isHidden();
            boolean tabAreaVisible = tabArea.getCurrentTheme().getBoolean(
                    ThemeConstants.EDITOR_TAB_AREA_VISIBLE);
            if (!showVisibleTabsToo || visible || !tabAreaVisible) {
                return rootControl.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            }
            return rootControl.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
        }
    }

    private final class BasicStackListViewerSorter extends ViewerSorter {

        public BasicStackListViewerSorter() {
            //no-op
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) {
                return cat1 - cat2;
            }
            if (!sortTabList) {
                return 0;
            }

            // cat1 == cat2

            String name1;
            String name2;

            if (viewer == null || !(viewer instanceof ContentViewer)) {
                name1 = e1.toString();
                name2 = e2.toString();
            } else {
                IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
                if (prov instanceof ILabelProvider) {
                    ILabelProvider lprov = (ILabelProvider) prov;
                    name1 = lprov.getText(e1);
                    name2 = lprov.getText(e2);
                    // ILabelProvider's implementation in BasicStackList calls
                    // DefaultEditorPresentation.getLabelText which returns the name of dirty
                    // files begining with dirty prefix, sorting should not take dirty prefix in consideration
                    String prefix = PartTab.DIRTY_PREFIX;
                    if (name1 != null && name1.startsWith(prefix)) {
                        name1 = name1.substring(prefix.length());
                    }
                    if (name2 != null && name2.startsWith(prefix)) {
                        name2 = name2.substring(prefix.length());
                    }
                } else {
                    name1 = e1.toString();
                    name2 = e2.toString();
                }
            }
            if (name1 == null) {
                name1 = "";
            }
            if (name2 == null) {
                name2 = "";
            }
            return getComparator().compare(name1, name2);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
         */
        public int category(Object element) {

            IPresentablePart part = (IPresentablePart) element;
            PartTab tabItem = tabArea.getTab(part);
            if (separateVisibleAndInvisible) {
                if (!tabItem.isHidden()) {
                    return 1; // visible
                }
                return 0; // not visible
            }
            return 0;
        }
    }

    protected void configureTableViewer(TableViewer tableViewer) {
        tableViewer.setContentProvider(new BasicStackListContentProvider());
        tableViewer.setSorter(new BasicStackListViewerSorter());
        tableViewer.setLabelProvider(new BasicStackListLabelProvider());
    }

    protected void gotoSelectedElement() {
        Object selectedElement = getSelectedElement();
        if (selectedElement == null) {
            if (!isPinned) {
                dispose();
            }
            return;
        }
        IStackPresentationSite stackPresentationSite = tabArea.getPresentation()
        .getSite();
        TabArea tabsArea = tabArea;
        IPresentablePart oldSelectedPart = tabsArea.getSelectedPart();
        IPresentablePart newSelectedPart = (IPresentablePart) selectedElement;
        //close the shell
        if (!isPinned) {
            dispose();
        }
        if (newSelectedPart == oldSelectedPart) {
            // part stack does not fire an event if the part is already selected,
            // but we should notify taberea if the *tab* for this part is not visible
            // and should be now shown
            tabsArea.selectPart(newSelectedPart);
        } else {
            stackPresentationSite.selectPart(newSelectedPart);
        }
    }

    /**
     * Delete all selected elements.
     *
     * @return <code>true</code> if there are no elements left after deletion.
     */
    protected boolean deleteSelectedElements() {
        IStructuredSelection selection = getSelectedElements();
        if (selection != null) {
            ArrayList list = new ArrayList(selection.size());
            for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                IPresentablePart presentablePart = (IPresentablePart) iterator.next();
                list.add(presentablePart);
            }
            VSStackPresentation presentation = tabArea.getPresentation();
            presentation.close((IPresentablePart[]) list
                    .toArray(new IPresentablePart[list.size()]));
            IPresentablePart[] partList = presentation.getSite().getPartList();
            if (partList == null || partList.length == 0) {
                dispose();
            }
            return true;
        }
        return false;
    }

    protected void restorePosition(Point proposedLocation) {
        Point location = null;
        IPreferenceStore store = getStore();
        int x = store.getInt(ThemeConstants.PART_LIST_LOCATION_X);
        int y = store.getInt(ThemeConstants.PART_LIST_LOCATION_Y);

        if (x > 0 || y > 0) {
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            location = fixLocation(x, y);
        }

        if (location == null) {
            location = fixLocation(proposedLocation.x, proposedLocation.y);
        }
        setLocation(location);
    }

}
