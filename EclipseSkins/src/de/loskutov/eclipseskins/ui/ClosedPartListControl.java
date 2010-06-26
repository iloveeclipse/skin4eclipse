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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.InputDialog;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.presentation.VSStackPresentation.ClosedPart;
import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.sessions.SessionNameValidator;
import de.loskutov.eclipseskins.sessions.Sessions;

/**
 * see org.eclipse.ui.internal.presentations.BasicPartList,
 * org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl,
 * org.eclipse.jdt.internal.ui.text.AbstractInformationControl
 * @author Andrei
 */
public class ClosedPartListControl extends AbstractPartListControl  {

    protected ToolItem deleteListButton;
    protected WindowListener windowListener;
    protected ToolItem saveButton;

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
                TableItem tItem = fTableViewer.getTable().getItem(
                        new Point(e.x, e.y));
                if (tItem != null) {
                    getMenu().setVisible(true);
                }
            }
        }

        private Menu getMenu() {
            if(menu != null){
                return menu;
            }
            menu = new Menu(fTableViewer.getTable());
            MenuItem mItem = new MenuItem(menu, SWT.NONE);
            mItem.setText(PresentationPlugin.getResourceString("action.reopen.text"));
            String iconPath = PresentationPlugin.getResourceString("action.reopen.icon");
            mItem.setImage(PresentationPlugin.getImage(iconPath));
            mItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent selectionEvent) {
                    gotoSelectedElement();
                }
            });

            mItem = new MenuItem(menu, SWT.NONE);
            mItem.setText(PresentationPlugin.getResourceString("action.reopenAll.text"));
            iconPath = PresentationPlugin.getResourceString("action.reopenAll.icon");
            mItem.setImage(PresentationPlugin.getImage(iconPath));
            mItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent selectionEvent) {
                    VSStackPresentation presentation = tabArea.getPresentation();
                    dispose();
                    presentation.reopenClosedParts(presentation.getClosedPartList());
                }
            });

            mItem = new MenuItem(menu, SWT.NONE);
            mItem.setText(PresentationPlugin.getResourceString("action.delete.text"));
            iconPath = PresentationPlugin.getResourceString("action.delete.icon");
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
    public ClosedPartListControl(Shell parent, final TabArea tabArea,
            final boolean isEditorPartList) {
        this.isEditorPartList = isEditorPartList;
        this.tabArea = tabArea;
        this.currentTheme = tabArea.getCurrentTheme();
        this.sortTabList = getBooleanFromTheme(ThemeConstants.TAB_LIST_SORT);
        mainWindowActive = false;
        isPinned = false;
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

        separator = new Label(fComposite, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.LINE_DOT);
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
        final int ignoreEventCount = UIUtils.isGtk ? 4 : 1;

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
        rootControl.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e) {
                clearReferences();
            }
        });
        isDeactivateListenerActive = true;
    }

    public void dispose() {
        if(isDisposed){
            return;
        }
        if(PresentationPlugin.DEBUG) {
            System.out.println("dispose list");
        }
        if (rootControl != null && !rootControl.isDisposed()) {
            rootControl.dispose();
        }
        clearReferences();
        isDisposed = true;
    }

    protected void clearReferences(){
        fBounds = null;
        fFilterText = null;
        viewMenuButton = null;
        sortListButton = null;
        deleteListButton = null;
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
        return isEditorPartList
        && getStore().getBoolean(ThemeConstants.STORE_PART_LIST_LOCATION);
    }



    protected void createViewMenu(Composite toolbar) {
        fToolBar = new ToolBar(toolbar, SWT.FLAT);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.END;
        data.verticalAlignment = GridData.BEGINNING;
        fToolBar.setLayoutData(data);
        saveButton = new ToolItem(fToolBar, SWT.PUSH | SWT.FLAT);
        UIUtils.initButton(saveButton, "saveList");
        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                saveList();
            }
        });
        sortListButton = new ToolItem(fToolBar, SWT.CHECK | SWT.FLAT);
        sortListButton.setSelection(sortTabList);
        UIUtils.initButton(sortListButton, "sortTabList");
        sortListButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSortEnabled(sortListButton.getSelection());
            }
        });
        deleteListButton = new ToolItem(fToolBar, SWT.PUSH | SWT.FLAT);
        UIUtils.initButton(deleteListButton, "clearClosedList");
        deleteListButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                deleteAllItems();
            }
        });

        viewMenuButton = new ToolItem(fToolBar, SWT.PUSH | SWT.FLAT);
        UIUtils.initButton(viewMenuButton, "viewMenu");
        viewMenuButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showViewMenu();
            }
        });
    }

    protected void saveList() {
        ClosedPart[] partList = tabArea.getClosedPartList();
        List/*<EditorInfo>*/ infos = new ArrayList();
        for (int i = 0; i < partList.length; i++) {
            EditorInfo info = partList[i].getEditorInfo();
            if(info != null && info.isConsistent()){
                info.setNumber(i);
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
    protected void deleteAllItems() {
        VSStackPresentation presentation = tabArea.getPresentation();
        dispose();
        presentation.clearClosedPartList();
    }

    protected boolean getBooleanFromTheme(String id){
        if(ThemeConstants.TAB_LIST_SORT.equals(id)){
            id = ThemeConstants.CLOSED_TAB_LIST_SORT;
        } else if(ThemeConstants.TAB_LIST_SHOW_FULL_PATH.equals(id)){
            id = ThemeConstants.CLOSED_TAB_LIST_SHOW_FULL_PATH;
        }
        return currentTheme.getBoolean(id);
    }

    protected void setBooleanToTheme(String id, boolean value){
        if(ThemeConstants.TAB_LIST_SORT.equals(id)){
            id = ThemeConstants.CLOSED_TAB_LIST_SORT;
        } else if(ThemeConstants.TAB_LIST_SHOW_FULL_PATH.equals(id)){
            id = ThemeConstants.CLOSED_TAB_LIST_SHOW_FULL_PATH;
        }
        currentTheme.setBoolean(id, value);
        PresentationPlugin.getDefault().getPreferenceStore().setValue(id, value);
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

        Action action = new ToggleShowFullPathAction();
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
                return new ClosedPart[0];
            }
            return tabArea.getClosedPartList();
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // ignore
        }
    }

    private final class BasicStackListLabelProvider extends LabelProvider implements
    IFontProvider, IColorProvider {

        public BasicStackListLabelProvider() {
            //no-op
        }

        public String getText(Object element) {
            ClosedPart presentablePart = (ClosedPart) element;
            if(showFullPath){
                String text = presentablePart.tooltip;
                if(text == null || text.length() == 0){
                    text =  presentablePart.name;
                }
                return text;
            }
            return presentablePart.name;
        }

        public Image getImage(Object element) {
            ClosedPart presentablePart = (ClosedPart) element;
            return presentablePart.image;
        }

        public Font getFont(Object element) {
            return tabArea.getFont();
        }

        public Color getForeground(Object element) {
            return rootControl.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
        }

        public Color getBackground(Object element) {
            return rootControl.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        }
    }

    private final class BasicStackListViewerSorter extends ViewerSorter {

        public BasicStackListViewerSorter() {
            //no-op
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
            if(!sortTabList){
                return 0;
            }
            String name1;
            String name2;

            if (viewer == null || !(viewer instanceof ContentViewer)) {
                name1 = e1.toString();
                name2 = e2.toString();
            } else {
                IBaseLabelProvider prov = ((ContentViewer) viewer)
                .getLabelProvider();
                if (prov instanceof ILabelProvider) {
                    ILabelProvider lprov = (ILabelProvider) prov;
                    name1 = lprov.getText(e1);
                    name2 = lprov.getText(e2);
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
    }

    protected void configureTableViewer(TableViewer tableViewer) {
        tableViewer.setContentProvider(new BasicStackListContentProvider());
        tableViewer.setSorter(new BasicStackListViewerSorter());
        tableViewer.setLabelProvider(new BasicStackListLabelProvider());
    }

    protected void gotoSelectedElement() {
        Object selectedElement = getSelectedElement();
        if(!(selectedElement instanceof ClosedPart)){
            dispose();
            return;
        }
        //close the shell
        dispose();
        ((ClosedPart) selectedElement).reopen();
    }

    /**
     * Delete all selected elements.
     *
     * @return <code>true</code> if there are no elements left after deletion.
     */
    protected boolean deleteSelectedElements() {
        IStructuredSelection selection = getSelectedElements();
        if (selection != null) {
            for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                ClosedPart closedPart = (ClosedPart) iterator.next();
                closedPart.remove();
            }
            if(tabArea.getClosedPartList().length == 0){
                dispose();
            } else {
                fTableViewer.refresh();
            }
            return true;
        }
        return false;
    }

    protected void restorePosition(Point proposedLocation){
        // no-op
    }


}
