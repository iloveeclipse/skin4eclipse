/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;

/**
 * @author Andrei
 *
 */
public abstract class AbstractPartListControl implements IInformationControl,
IInformationControlExtension, IInformationControlExtension2,
IInformationControlExtension3 {

    protected TabArea tabArea;

    protected ToolTipHandler tooltip;

    /** Border thickness in pixels. */
    protected static final int BORDER = 1;

    /** Right margin in pixels. */
    protected static final int RIGHT_MARGIN = 3;

    /** The control's shell */
    protected Shell rootControl;

    /** The composite */
    protected Composite fComposite;

    /** The control's text widget */
    protected Text fFilterText;

    /** The control's table widget */
    protected TableViewer fTableViewer;

    protected Label separator;

    protected Composite fViewMenuButtonComposite;

    protected IAction fShowViewMenuAction;

    protected MenuManager fViewMenuManager;

    protected ToolBar fToolBar;

    protected ToolItem viewMenuButton;
    protected ToolItem sortListButton;

    protected ThemeWrapper currentTheme;

    protected Map/* <String,Action> */actionMap;

    protected Listener fDeactivateListener;

    protected Listener layoutListener;

    protected NamePatternFilter namePatternFilter;

    /**
     * Remembers the bounds for this information control.
     *
     * @since 3.0
     */
    protected Rectangle fBounds;

    protected Rectangle fTrim;

    protected boolean showFullPath;

    protected boolean sortTabList;

    protected boolean isDeactivateListenerActive;

    protected boolean isEditorPartList;

    protected boolean isDisposed;

    protected boolean isPinned;

    protected boolean isVisible;

    protected boolean mainWindowActive;

    protected boolean isActive;

    protected boolean forcePosition;

    public boolean menuAboutToShow;


    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setBackgroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setBackgroundColor(Color background) {
        if(PresentationPlugin.DEBUG) {
            System.out.println("backgr:" + background);
        }

        fTableViewer.getTable().setBackground(background);
        fFilterText.setBackground(background);
        fComposite.setBackground(background);
        fViewMenuButtonComposite.setBackground(background);
        if (fToolBar != null && !fToolBar.isDisposed()) {
            fToolBar.setBackground(background);
        }
    }

    protected void showViewMenu() {
        menuAboutToShow = true;
        Menu aMenu = getViewMenuManager().createContextMenu(rootControl);
        Rectangle bounds = fToolBar.getBounds();
        Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
        topLeft = rootControl.toDisplay(topLeft);
        aMenu.setLocation(topLeft.x, topLeft.y);
        aMenu.setVisible(true);
    }

    protected MenuManager getViewMenuManager() {
        if (fViewMenuManager == null) {
            fViewMenuManager = new MenuManager();
            fillViewMenu(fViewMenuManager);
        }
        return fViewMenuManager;
    }

    /**
     * @param viewMenuManager
     */
    protected abstract void fillViewMenu(IMenuManager viewMenuManager);

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#isFocusControl()
     */
    public boolean isFocusControl() {
        return fFilterText.isFocusControl() || fTableViewer.getControl().isFocusControl();
    }

    public void setFocus() {
        rootControl.forceFocus();
        fFilterText.setFocus();
    }

    public void addFocusListener(FocusListener listener) {
        rootControl.addFocusListener(listener);
    }

    public void removeFocusListener(FocusListener listener) {
        rootControl.removeFocusListener(listener);
    }

    public boolean hasContents() {
        return fTableViewer != null && fTableViewer.getInput() != null;
    }

    public void setSizeConstraints(int maxWidth, int maxHeight) {
        //fMaxWidth= maxWidth;
        //fMaxHeight= maxHeight;
    }

    public Point computeSizeHint() {
        return rootControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    public void setLocation(Point location) {
        if(rootControl != null){
            fTrim = rootControl.computeTrim(0, 0, 0, 0);
            Point textLocation = fComposite.getLocation();
            location.x += fTrim.x - textLocation.x;
            location.y += fTrim.y - textLocation.y;
            rootControl.setLocation(location);
        }
    }

    public void setPosition(Point displayCoordinates, boolean forcePosition) {
        this.forcePosition = forcePosition;
        if(!forcePosition && restoresLocation()){
            restorePosition(displayCoordinates);
            return;
        }
        setLocation(fixLocation(displayCoordinates.x, displayCoordinates.y));
    }

    protected final Point fixLocation(int x, int y) {
        Monitor mon = tabArea.getMonitor();
        Rectangle bounds = mon.getClientArea();
        Point size = computeSizeHint();
        if (x + size.x > bounds.x + bounds.width) {
            x = bounds.x + bounds.width - size.x;
        }
        if (y + size.y > bounds.y + bounds.height) {
            y = bounds.y + bounds.height - size.y;
        }
        return new Point(x, y);
    }

    /*
     * @see org.eclipse.jface.text.IInformationControlExtension3#restoresSize()
     */
    public boolean restoresSize() {
        return restoresLocation();
    }

    /*
     * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
     */
    public Rectangle computeTrim() {
        if (fTrim != null) {
            return fTrim;
        }
        return new Rectangle(0, 0, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(int width, int height) {
        rootControl.setSize(width, height);
    }


    /**
     * {@inheritDoc}
     * @since 3.0
     */
    public Rectangle getBounds() {
        return fBounds;
    }

    protected void setSortEnabled(boolean enabled) {
        sortTabList = enabled;
        setInput(null);
        setBooleanToTheme(ThemeConstants.TAB_LIST_SORT, sortTabList);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
     */
    public void setInformation(String information) {
        // no-op
    }

    protected IPreferenceStore getStore() {
        return PresentationPlugin.getDefault().getPreferenceStore();
    }

    protected boolean getBooleanFromTheme(String id){
        return currentTheme.getBoolean(id);
    }

    protected void setBooleanToTheme(String id, boolean value){
        currentTheme.setBoolean(id, value);
        PresentationPlugin.getDefault().getPreferenceStore().setValue(id, value);
    }

    protected void setInfoSystemColor() {
        Display display = rootControl.getDisplay();
        setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    public void setInput(Object information) {
        if(fTableViewer == null) {
            return;
        }
        int tableHeight = 0;
        int tableWidth = 0;

        if (information instanceof Rectangle) {
            Rectangle new_size = (Rectangle) information;
            tableHeight = new_size.height;
            tableWidth = new_size.width;
        } else{
            fFilterText.setText(""); //$NON-NLS-1$
            fTableViewer.setInput(tabArea);
        }

        // Resize the table's height accordingly to the new input
        Table viewerTable = fTableViewer.getTable();

        Point tableSize;
        if(tableWidth != 0 && tableHeight != 0) {
            tableSize = new Point(tableWidth, tableHeight);
        } else {
            tableSize = viewerTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        }

        Rectangle displayBounds = fComposite.getDisplay().getBounds();
        int tableMaxHeight = displayBounds.height / 2;
        int tableMaxWidth = displayBounds.width / 2;

        // removes padding if necessary
        int itemHeight = viewerTable.getItemHeight();
        tableHeight = (tableSize.y <= tableMaxHeight) ? tableSize.y
                - itemHeight - itemHeight / 2 : tableMaxHeight;

        TableItem[] tableItems = viewerTable.getItems();
        for (int i = 0; i < tableItems.length; i++) {
            Rectangle bounds = tableItems[i].getBounds(0);
            tableMaxWidth = Math.max(bounds.width, tableMaxWidth);
        }
        tableWidth = (tableSize.x <= tableMaxWidth) ? tableSize.x : tableMaxWidth;

        GridData layoutData = (GridData) viewerTable.getLayoutData();
        layoutData.heightHint = tableHeight;
        layoutData.widthHint = tableWidth;

        Point fCompSize = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        fComposite.setSize(fCompSize);
        Shell shell = fComposite.getShell();
        shell.setSize(fCompSize);
        shell.layout();
    }

    protected Text createFilterText(Composite parent) {
        Text filterText = new Text(parent, SWT.NONE);
        filterText.setToolTipText("Press <Esc> to hide tab list");
        GridData data= new GridData(GridData.FILL_HORIZONTAL);

        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.CENTER;
        filterText.setLayoutData(data);

        filterText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 0x0D) {
                    // return
                    gotoSelectedElement();
                    return;
                }
                Table table = fTableViewer.getTable();
                if (e.keyCode == SWT.ARROW_DOWN) {
                    table.setFocus();
                    table.setSelection(0);
                }
                if (e.keyCode == SWT.ARROW_UP) {
                    table.setFocus();
                    table.setSelection(table.getItemCount() - 1);
                }
                if (e.character == 0x1B) {
                    // ESC
                    dispose();
                }
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });
        return filterText;
    }


    protected void installFilter() {
        fFilterText.setText(""); //$NON-NLS-1$

        fFilterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text = ((Text) e.widget).getText();
                int length = text.length();
                if (length > 0 && text.charAt(length - 1) != '*') {
                    text = text + ".*";
                }
                setMatcherString(text);
            }
        });
    }

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning: ? => any character * =>
     * any string
     * </p>
     */
    protected void setMatcherString(String pattern) {
        namePatternFilter.setPattern(pattern);
        // refresh viewer to refilter
        fTableViewer.getControl().setRedraw(false);
        fTableViewer.refresh();
        selectFirstMatch();
        fTableViewer.getControl().setRedraw(true);
    }

    protected TableViewer createTableViewer(Composite parent, int style) {

        /*
         * start create table etc
         *
         */
        Table table = new Table(parent, SWT.SINGLE | (style & ~SWT.MULTI));
        table.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        TableViewer tableViewer = new TableViewer(table) {

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.TableViewer#internalRefresh(java.lang.Object)
             */
            protected void internalRefresh(Object element) {
                boolean usingMotif = "motif".equals(SWT.getPlatform()); //$NON-NLS-1$
                try {
                    // This avoids a "graphic is disposed" error on Motif by not letting
                    // it redraw while we remove entries.  Some items in this table are
                    // being removed and may have icons which may have already been
                    // disposed elsewhere.
                    if (usingMotif) {
                        getTable().setRedraw(false);
                    }
                    super.internalRefresh(element);
                } finally {
                    if (usingMotif) {
                        getTable().setRedraw(true);
                    }
                }
            }
        };
        namePatternFilter = new NamePatternFilter();
        tableViewer.addFilter(namePatternFilter);
        configureTableViewer(tableViewer);
        return tableViewer;
    }

    /**
     * @param tableViewer
     */
    protected abstract void configureTableViewer(TableViewer tableViewer);

    /**
     * Implementers can modify
     */
    protected Object getSelectedElement() {
        return ((IStructuredSelection) fTableViewer.getSelection())
        .getFirstElement();
    }

    /**
     * Implementers can modify
     */
    protected IStructuredSelection getSelectedElements() {
        return (IStructuredSelection) fTableViewer.getSelection();
    }

    /**
     * Selects the first element in the table which matches the current filter
     * pattern.
     */
    protected void selectFirstMatch() {
        Table table = fTableViewer.getTable();
        Object element = namePatternFilter.findElement(table.getItems(),
                (ILabelProvider) fTableViewer.getLabelProvider());
        if (element != null) {
            fTableViewer.setSelection(new StructuredSelection(element), true);
        } else {
            fTableViewer.setSelection(StructuredSelection.EMPTY);
        }
    }


    public void setVisible(boolean visible) {
        this.isVisible = visible;
        if(PresentationPlugin.DEBUG) {
            System.out.println("set list visible: " + visible);
        }

        if(rootControl != null && !rootControl.isDisposed()) {
            if(!visible && isPinned && isActive){
                if(PresentationPlugin.DEBUG) {
                    System.out.println("set list inVisible cancelled");
                }

            } else {
                rootControl.setVisible(visible);
            }
        } else {
            if(PresentationPlugin.DEBUG) {
                System.out.println("set list visible: " + visible
                        + ", shell is already disposed!!!");
            }
        }
    }

    /**
     * @param displayCoordinates
     */
    protected abstract void restorePosition(Point displayCoordinates);
    /**
     *
     */
    protected abstract void gotoSelectedElement();

    /*
     *
     */
    public void addDisposeListener(DisposeListener listener) {
        rootControl.addDisposeListener(listener);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
     */
    public void removeDisposeListener(DisposeListener listener) {
        rootControl.removeDisposeListener(listener);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.IInformationControl#setForegroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setForegroundColor(Color foreground) {
        if(PresentationPlugin.DEBUG) {
            System.out.println("foregr:" + foreground);
        }

        fTableViewer.getTable().setForeground(foreground);
        fFilterText.setForeground(foreground);
        fComposite.setForeground(foreground);
        fViewMenuButtonComposite.setForeground(foreground);
        if (fToolBar != null) {
            fToolBar.setForeground(foreground);
        }
    }

    final class ToggleShowFullPathAction extends Action {
        public ToggleShowFullPathAction(){
            super();
            setId("showFullPath");
            showFullPath = getBooleanFromTheme(ThemeConstants.TAB_LIST_SHOW_FULL_PATH);
            setChecked(showFullPath);
        }
        public void run() {
            showFullPath = isChecked();
            setInput(null);
            // re-set position
            setPosition(rootControl.getLocation(), true);
            setBooleanToTheme(ThemeConstants.TAB_LIST_SHOW_FULL_PATH, showFullPath);
        }
    }

    final class WindowListener implements IWindowListener {
        private final IWorkbenchWindow myWindow;

        WindowListener(IWorkbenchWindow activeWorkbenchWindow) {
            this.myWindow = activeWorkbenchWindow;
        }

        public void windowActivated(IWorkbenchWindow window) {
            if (myWindow != window) {
                return;
            }
            if (PresentationPlugin.DEBUG) {
                System.out.println("window activate");
            }

            mainWindowActive = true;
        }

        public void windowDeactivated(IWorkbenchWindow window) {
            if (myWindow != window) {
                return;
            }
            if (PresentationPlugin.DEBUG) {
                System.out.println("window deactivate");
            }

            mainWindowActive = false;
            if (isPinned && isVisible && isDeactivateListenerActive && rootControl != null
                    && !rootControl.isDisposed()) {

                rootControl.getDisplay().timerExec(50, new Runnable() {
                    public void run() {
                        if (!isActive) {
                            if (PresentationPlugin.DEBUG) {
                                System.out.println("re-call from deactivate window");
                            }

                            setVisible(false);
                            dispose();
                        }
                    }
                });
            }
        }

        public void windowClosed(IWorkbenchWindow window) {
            // no-op
        }

        public void windowOpened(IWorkbenchWindow window) {
            // no-op
        }
    }

    final class MyMouseMoveListener implements MouseMoveListener {
        private final int count;
        private TableItem fLastItem;
        private int lastY;
        private int divCount;

        MyMouseMoveListener(int count) {
            super();
            this.count = count;
        }

        public void mouseMove(MouseEvent e) {
            Table table = fTableViewer.getTable();
            int itemHeightdiv4 = table.getItemHeight() / 4;
            int tableHeight = table.getBounds().height;
            Point tableLoc = table.toDisplay(0,0);
            if (divCount == count) {
                divCount = 0;
            }
            divCount ++;
            if (table.equals(e.getSource()) && divCount == count) {
                TableItem o = table.getItem(new Point(e.x, e.y));
                if(o == null){
                    return;
                }
                if (lastY != e.y) {
                    lastY = e.y;
                    if (!o.equals(fLastItem)) {
                        fLastItem = o;
                        table.setSelection(new TableItem[] { fLastItem });
                    } else if (e.y < itemHeightdiv4) {
                        // Scroll up
                        Item item = fTableViewer.scrollUp(e.x + tableLoc.x, e.y + tableLoc.y);
                        if (item instanceof TableItem) {
                            fLastItem = (TableItem) item;
                            table.setSelection(new TableItem[] { fLastItem });
                        }
                    } else if (e.y > tableHeight - itemHeightdiv4) {
                        // Scroll down
                        Item item = fTableViewer.scrollDown(e.x + tableLoc.x, e.y + tableLoc.y);
                        if (item instanceof TableItem) {
                            fLastItem = (TableItem) item;
                            table.setSelection(new TableItem[] { fLastItem });
                        }
                    }
                }
            }
        }
    }

    final class DeactivateListener implements Listener {
        protected DeactivateListener() {
            super();
        }

        public void shellActivate(Event e) {
            if (PresentationPlugin.DEBUG) {
                System.out.println("activate list");
            }

            isActive = true;
            if(rootControl != null) {
                if (e.widget == rootControl && rootControl.getShells().length == 0) {
                    isDeactivateListenerActive = true;
                }
            }
            if (isPinned && !rootControl.isVisible()) {
                setVisible(true);
            }
        }

        public void shellDeactivate(Event e) {
            if (PresentationPlugin.DEBUG) {
                System.out.println("deactivate list:\n" + "\t visible: " + isVisible
                        + ", pinned: " + isPinned + ", window active: "
                        + mainWindowActive + ", list active: " + isActive);
            }

            // WORKAROUND for disposing control too early on menu events on gtk (linux)
            if(menuAboutToShow && UIUtils.isGtk){
                if (PresentationPlugin.DEBUG) {
                    System.out.println("shellDeactivate on gtk ignored!!!");
                }

                menuAboutToShow = false;
                return;
            }
            isActive = false;

            if (isPinned) {
                if (isVisible && mainWindowActive) {
                    setVisible(false);
                    dispose();
                } else if (isVisible && isDeactivateListenerActive && rootControl != null
                        && !rootControl.isDisposed()) {
                    rootControl.getDisplay().timerExec(50, new Runnable() {
                        public void run() {
                            if (!mainWindowActive) {
                                if (PresentationPlugin.DEBUG) {
                                    System.out.println("re-call from deactivate list");
                                }

                                setVisible(false);
                                dispose();
                            }
                        }
                    });
                }
            } else if (!isEditorPartList || isDeactivateListenerActive) {
                setVisible(false);
                dispose();
            }
        }

        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Activate:
                shellActivate(event);
                break;
            case SWT.Deactivate:
                shellDeactivate(event);
                break;
            default:
                break;
            }
        }

    }

}
