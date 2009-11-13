/*******************************************************************************
 * Copyright (c) 2005 Willian Mitsuda.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Willian Mitsuda - initial API and implementation
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *    Fabio Zadrozny - ctrl+page up/down traverses editors
 *                   - closed editors persisted across presentations
 *                   - closed editors remembered on ctrl+W or Ctrl+F4
 *******************************************************************************/
package de.loskutov.eclipseskins.presentation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.PresentationUtil;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.themes.ITheme;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.preferences.PreferenceInitializer;
import de.loskutov.eclipseskins.sessions.EditorInfo;
import de.loskutov.eclipseskins.ui.PartListMenu;
import de.loskutov.eclipseskins.ui.PartTab;
import de.loskutov.eclipseskins.ui.PartTitle;
import de.loskutov.eclipseskins.ui.TabArea;
import de.loskutov.eclipseskins.ui.TabAreaButtons;
import de.loskutov.eclipseskins.ui.actions.MaximizePartAction;
import de.loskutov.eclipseskins.ui.actions.MinimizePartAction;
import de.loskutov.eclipseskins.ui.actions.RestorePartAction;
import de.loskutov.eclipseskins.ui.menu.StandardViewMenu;

/**
 * Superclass of VS-like stack presentations
 *
 * @author wmitsuda
 * @author Andrei
 * @author fabioz
 */
public abstract class VSStackPresentation extends StackPresentation implements PaintListener {

    protected StandardViewMenu mainSystemMenu;

    protected StandardViewMenu reducedSystemMenu;

    protected PartTitle title;

    protected ThemeWrapper currentTheme;

    protected RestorePartAction restoreAction;

    protected MinimizePartAction minimizeAction;

    protected MaximizePartAction maximizeAction;

    protected IPresentablePart currentPart;

    protected IPresentablePart lastSelectedPart;

    protected Map/*<IPresentablePart,PartTab>*/parts;

    /**
     * Note that the closed parts is static because we want to maintain the
     * list of closed editors when creating a new presentation (it should
     * always be available)
     */
    protected final static List/*<ClosedPart>*/closedParts = new ArrayList();

    protected Composite presentationControl;

    protected TabArea tabArea;

    protected TabAreaButtons tabButtons;

    protected PartListMenu partListMenu;

    protected boolean showResizeCommands;

    protected boolean activeFocus;

    private Listener dragPartListener;

    private IPropertyListener propListener;

    private Listener dragStackListener;

    private MouseListener focusListener;

    private MouseListener systemMenuListener;

    private MouseListener resizeListener;

    protected PartTab tabWithMenuFocus;

    protected int borderSize;

    protected Color colorBorderFocus;

    protected Color colorBorderNoFocus;

    protected Color systemColorNormalShadow;

    protected Color systemColorBackgr;

    /** partstack private (not UI presentation global) flags */
    protected BitSet stackFlags;

    public static final int F_TAB_AREA_VISIBLE = 1;

    public static final int F_TOOLBAR_VISIBLE = 2;

    /**
     * Listener to changes made to the current theme. The presentation will
     * redraw when the theme changes.
     */
    protected IPropertyChangeListener themeChangeListener;

    protected IPropertyChangeListener storeChangeListener;

    protected IPropertyChangeListener tabPositionChangeListener;

    protected boolean isVisible;

    protected boolean disableStoreListener;

    private PartListMenu closedPartListMenu;

    protected Rectangle oldBounds;

    /**
     * We don't want the chance of traversing while we're already doing a traverse.
     */
    private boolean ignoreTraverse;

    /**
     * Handles a received traverse.
     *
     * @param event the event that triggered the traverse.
     */
    private void onTraverseEvent(Event event) {
        if (ignoreTraverse) {
            return;
        }
        if (event.detail != SWT.TRAVERSE_PAGE_NEXT && event.detail != SWT.TRAVERSE_PAGE_PREVIOUS) {
            return;
        }
        if (isDisposed() || getPartsCount() <= 0) {
            return;
        }
        ignoreTraverse = true;
        try {
            event.type = SWT.None;
            event.doit = true;
            onPageTraversal(event);
            event.detail = SWT.TRAVERSE_NONE;
        } finally {
            ignoreTraverse = false;
        }
    }

    /**
     * Called to actually do the traverse.
     */
    private void onPageTraversal(Event event) {
        IPresentablePart selectedPart = getSite().getSelectedPart();
        int index = getPartNumber(selectedPart);
        if (event.detail == SWT.TRAVERSE_PAGE_NEXT){
            index++;
        } else if(event.detail == SWT.TRAVERSE_PAGE_PREVIOUS){
            index--;
        }
        PartTab tab = tabArea.getTab(index);
        if(tab == null){
            return;
        }
        getSite().selectPart(tab.getPart());
    }

    /**
     * Indicates if we're already disposed.
     */
    private boolean isDisposed(){
        return currentPart == null;
    }

    protected VSStackPresentation(Composite parent, final IStackPresentationSite site,
            boolean showResizeCommands) {
        super(site);

        stackFlags = new BitSet();
        /*
         * the flags would be overriden if stack is restored, but for brand new stacks
         * this flags need to be set here
         * TODO read defaults from props
         */
        stackFlags.set(F_TAB_AREA_VISIBLE, true);
        stackFlags.set(F_TOOLBAR_VISIBLE, true);

        // SWT.NO_BACKGROUND prevents flickering
        presentationControl = new Canvas(parent, SWT.NO_BACKGROUND);

        // from here, we do not need to create something else, if workbench is going down
        // but presentation control should be always created, because the workbench UI need it
        if (isClosing()) {
            return;
        }
        parts = new HashMap();
        restoreAction = new RestorePartAction(site);
        minimizeAction = new MinimizePartAction(site);
        maximizeAction = new MaximizePartAction(site);
        oldBounds = new Rectangle(0, 0, 0, 0);
        this.showResizeCommands = showResizeCommands;

        createListeners();
        getWorkbench().getThemeManager().addPropertyChangeListener(themeChangeListener);

        PresentationPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
                storeChangeListener);

        PlatformUI.getPreferenceStore().addPropertyChangeListener(
                tabPositionChangeListener);

        presentationControl.addMouseListener(focusListener);
        presentationControl.addPaintListener(this);

        presentationControl.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                /**
                 * Perform any cleanup. This method should remove any listeners that were
                 * attached to other objects. This gets called when the presentation
                 * widget is disposed. This is safer than cleaning up in the dispose()
                 * method, since this code will run even if some unusual circumstance
                 * destroys the Shell without first calling dispose().
                 */
                // Remove any listeners that were attached to any
                // global Eclipse resources. This is necessary in order to prevent
                // memory leaks.
                getWorkbench().getThemeManager().removePropertyChangeListener(
                        themeChangeListener);
                PresentationPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(storeChangeListener);
                PlatformUI.getPreferenceStore()
                .removePropertyChangeListener(tabPositionChangeListener);
            }
        });

        tabArea = new TabArea(SWT.NO_BACKGROUND, this);
        tabArea.addMouseListener(focusListener);
        tabArea.addMouseListener(resizeListener);

        tabButtons = new TabAreaButtons(presentationControl, SWT.NO_BACKGROUND, isView(),
                tabArea, getCurrentTheme());
        tabButtons.addMouseListener(focusListener);
        tabButtons.addMouseListener(resizeListener);

        title = new PartTitle(this, SWT.NO_BACKGROUND);
        title.addMouseListener(resizeListener);
        title.addMouseListener(systemMenuListener);
        title.addMouseListener(focusListener);

        PresentationUtil.addDragListener(title, dragPartListener);
        PresentationUtil.addDragListener(presentationControl, dragPartListener);
        PresentationUtil.addDragListener(tabArea, dragStackListener);
        initColorsAndFonts(getCurrentTheme());

        // We want to know about traverse events
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                if(event.type == SWT.Traverse) {
                    onTraverseEvent(event);
                }
            }
        };
        // Add the traverse listener (Ctrl+Pg Up/Down).
        presentationControl.addListener(SWT.Traverse, listener);
    }

    /**
     *
     */
    private void createListeners() {
        propListener = new IPropertyListener() {
            public void propertyChanged(Object source, int propId) {
                if (presentationControl == null || presentationControl.isDisposed()) {
                    return;
                }
                if (!(source instanceof IPresentablePart)) {
                    return;
                }
                IPresentablePart sourcePart = (IPresentablePart) source;
                childPropertyChanged(sourcePart, propId);
            }
        };

        dragStackListener = new Listener() {
            public void handleEvent(Event event) {
                if (currentPart != null) {
                    Point loc = new Point(event.x, event.y);
                    Control ctrl = (Control) event.widget;
                    getSite().dragStart(ctrl.toDisplay(loc), false);
                }
            }
        };

        dragPartListener = new Listener() {
            public void handleEvent(Event event) {
                if (currentPart != null || tabWithMenuFocus != null) {
                    Point loc = new Point(event.x, event.y);
                    Control ctrl = (Control) event.widget;
                    IStackPresentationSite site = getSite();
                    if (tabWithMenuFocus != null && !tabWithMenuFocus.isDisposed()) {
                        IPresentablePart part = tabWithMenuFocus.getPart();
                        if (part != null) {
                            site.selectPart(part);
                            site.dragStart(part, ctrl.toDisplay(loc), false);
                        }
                    } else {
                        site.dragStart(currentPart, ctrl.toDisplay(loc), false);
                    }
                }
            }
        };

        focusListener = new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (PresentationPlugin.DEBUG) {
                    System.out.println("focus! button: " + e.button);
                }
                if (currentPart != null) {
                    currentPart.setFocus();
                }
            }
        };

        systemMenuListener = new MouseAdapter() {

            public void mouseDown(MouseEvent e) {
                Widget widget = e.widget;
                if (widget instanceof Control && e.button == 3) {
                    Point location = ((Control) widget).toDisplay(e.x, e.y);
                    if (widget instanceof PartTab) {
                        PartTab tab = (PartTab) widget;
                        setMenuFocus(tab, true);
                    }
                    showSystemMenu(location);
                }
            }

            public void mouseUp(MouseEvent e) {
                if (e.button == 2) {
                    // middle click
                    closeOnMiddleClick();
                }
            }
        };

        resizeListener = new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                IStackPresentationSite site = getSite();
                if (site.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                    site.setState(IStackPresentationSite.STATE_RESTORED);
                    tabArea.selectPart(site.getSelectedPart());
                } else {
                    site.setState(IStackPresentationSite.STATE_MAXIMIZED);
                }
            }
        };

        themeChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (presentationControl == null || presentationControl.isDisposed()) {
                    return;
                }
                ITheme theme = getWorkbench().getThemeManager().getCurrentTheme();
                if (currentTheme == null || !theme.getId().equals(currentTheme.getId())) {
                    ThemeWrapper themeWrapper = new ThemeWrapper(theme);
                    disableStoreListener = true;
                    PreferenceInitializer.performDefaults(PresentationPlugin.getDefault()
                            .getPreferenceStore(), themeWrapper);
                    disableStoreListener = false;
                    setCurrentTheme(themeWrapper);
                } else {
                    // re-set the theme
                    setCurrentTheme(currentTheme);
                }
            }
        };

        storeChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (disableStoreListener || presentationControl == null
                        || presentationControl.isDisposed()) {
                    return;
                }
                String property = event.getProperty();
                getCurrentTheme().propertyChanged(property, event.getNewValue());

                if (PreferenceInitializer.isLayoutRelated(property)) {
                    // re-set the theme
                    setCurrentTheme(currentTheme);
                }
            }
        };

        tabPositionChangeListener = new IPropertyChangeListener() {
            protected final String[] MY_PROPS = { ThemeConstants.EDITOR_TAB_POSITION,
                    ThemeConstants.VIEW_TAB_POSITION,
                    ThemeConstants.EDITOR_TAB_AREA_VISIBLE };

            public void propertyChange(PropertyChangeEvent event) {
                if (tabArea == null || !isMyProperty(event.getProperty())
                        || tabArea.isDisposed()) {
                    return;
                }

                getCurrentTheme().propertyChanged(event.getProperty(),
                        event.getNewValue());
                // re-set the theme
                setCurrentTheme(currentTheme);
            }

            protected boolean isMyProperty(String propertyName) {
                for (int i = 0; i < MY_PROPS.length; i++) {
                    if (MY_PROPS[i].equals(propertyName)) {
                        return true;
                    }
                }
                return false;
            }
        };

    }

    protected abstract void layout();

    protected abstract void childPropertyChanged(IPresentablePart part, int property);

    protected abstract void addActionsToSystemMenu(final IStackPresentationSite site);

    public abstract boolean isView();

    protected boolean isMinimized() {
        return getSite().getState() == IStackPresentationSite.STATE_MINIMIZED;
    }

    /**
     * The hack to get the "detached" state of view, which is not exposed througth public API
     * see org.eclipse.ui.internal.LayoutPart#isDocked()
     * @return true if this view is detached
     */
    public boolean isDetached() {
        Shell s = getControl().getShell();
        if (s == null) {
            return false;
        }
        Object data = s.getData();
        if (data != null && !(data instanceof IWorkbenchWindow)) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName() + " :is detached!!!");
            }
            return true;
        }
        return false;
    }

    public Control getControl() {
        return presentationControl;
    }

    public IPresentablePart getCurrent() {
        return currentPart;
    }

    public int getPartsCount() {
        return parts != null ? parts.size() : 0;
    }

    public int getClosedPartsCount() {
        return closedParts != null ? closedParts.size() : 0;
    }

    public void showSystemMenu() {
        Point p = title.getParent().toDisplay(title.getLocation());
        p.y += title.getBounds().height;
        showSystemMenu(p);
    }

    public void showPaneMenu() {
        // not used
    }

    public void showClosedPartList() {
        showClosedPartList(title.getPreferredClosedPartListLocation());
    }

    public void showClosedPartList(Point location) {
        if (closedPartListMenu == null) {
            closedPartListMenu = new PartListMenu(tabArea, isView(), true);
        }
        Control control = getControl();
        closedPartListMenu.show(control, location, true);
    }

    protected void showSystemMenu(Point location) {
        if (mainSystemMenu == null) {
            // lasy init: Create system menu only on demand
            addActionsToSystemMenu(getSite());
        }

        // here we can use another one selected part
        if (tabWithMenuFocus != null && tabWithMenuFocus.getPart() != currentPart) {
            reducedSystemMenu.show(getControl(), location, tabWithMenuFocus.getPart());
        } else if (currentPart != null) {
            mainSystemMenu.show(getControl(), location, currentPart);
        } else {
            // ??? could it happen ???
            mainSystemMenu.show(getControl(), location, getSite().getSelectedPart());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#showPartList()
     */
    public void showPartList() {
        showPartList(title.getPreferredPartListLocation(), false);
    }

    public void showPartList(Point location, boolean forcePosition) {
        if (partListMenu == null) {
            partListMenu = new PartListMenu(tabArea, true, false);
        }
        Control control = getControl();
        partListMenu.show(control, location, forcePosition);
    }

    public Control[] getTabList(IPresentablePart part) {
        List tabList = new ArrayList();
        tabList.add(getControl());
        if (part.getToolBar() != null) {
            tabList.add(part.getToolBar());
        }
        if (part.getControl() != null) {
            tabList.add(part.getControl());
        }
        return (Control[]) tabList.toArray(new Control[tabList.size()]);
    }

    public void setState(int state) {
        // if closing, the action was not created
        if (minimizeAction == null) {
            return;
        }
        minimizeAction.setEnabled(state != IStackPresentationSite.STATE_MINIMIZED);
        maximizeAction.setEnabled(state != IStackPresentationSite.STATE_MAXIMIZED);
        restoreAction.setEnabled(state != IStackPresentationSite.STATE_RESTORED);
        title.setState(state);
    }

    public void restoreState(IPresentationSerializer context, IMemento memento) {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":restoreState");
        }
        /*
         * we do not use setFlag() here to prevent layout and redraw on startup
         */
        Integer flag = memento.getInteger("toolbarVisible");
        if (flag != null) {
            stackFlags.set(F_TOOLBAR_VISIBLE, flag.intValue() == 1);
        } else {
            // TODO read from theme setting
            stackFlags.set(F_TOOLBAR_VISIBLE, true);
        }
        flag = memento.getInteger("tabAreaVisible");
        if (flag != null) {
            stackFlags.set(F_TAB_AREA_VISIBLE, flag.intValue() == 1);
        } else {
            // TODO read from theme setting
            stackFlags.set(F_TAB_AREA_VISIBLE, true);
        }
        tabArea.restoreState(context, memento, this);
    }

    public void saveState(IPresentationSerializer context, IMemento memento) {
        // if closing, the area was not created
        if (tabArea == null) {
            return;
        }
        memento.putInteger("toolbarVisible", getFlag(F_TOOLBAR_VISIBLE) ? 1 : 0);
        memento.putInteger("tabAreaVisible", getFlag(F_TAB_AREA_VISIBLE) ? 1 : 0);
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":saveState");
        }
        tabArea.saveState(context, memento);
    }

    public IStackPresentationSite getSite() {
        return super.getSite();
    }

    protected void setCurrentTheme(ThemeWrapper theme) {
        if (theme != currentTheme && currentTheme != null) {
            currentTheme.dispose();
        }
        currentTheme = theme;
        tabArea.setCurrentTheme(theme);
        tabButtons.setCurrentTheme(theme);
        title.setCurrentTheme(theme);
        initColorsAndFonts(theme);
        layout();
        redraw();
    }

    protected void initColorsAndFonts(ThemeWrapper theme) {
        borderSize = theme.getInt(ThemeConstants.BORDER_SIZE);
        colorBorderFocus = theme.getColor(ThemeConstants.BORDER_COLOR_FOCUS);
        colorBorderNoFocus = theme.getColor(ThemeConstants.BORDER_COLOR_NOFOCUS);
        Display display = presentationControl.getDisplay();
        systemColorNormalShadow = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        systemColorBackgr = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    }

    public ThemeWrapper getCurrentTheme() {
        if (currentTheme == null) {
            currentTheme = new ThemeWrapper(getWorkbench().getThemeManager()
                    .getCurrentTheme());
            PreferenceInitializer.initializeCustomPreferences(currentTheme);
        }
        return currentTheme;
    }

    protected static IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    public void setActive(int newState) {
        if (PresentationPlugin.DEBUG) {
            switch (newState) {
            case AS_INACTIVE:
                System.out.println(getDebugPartName() + ":setActive: INACTIVE");
                break;
            case AS_ACTIVE_FOCUS:
                System.out.println(getDebugPartName() + ":setActive: ACTIVE+FOCUS");
                break;
            case AS_ACTIVE_NOFOCUS:
                System.out.println(getDebugPartName() + ":setActive: ACTIVE_NO_FOC");
                break;
            default:
                break;
            }
        }
        activeFocus = newState == AS_ACTIVE_FOCUS;
        // if closing, the area was not created
        if (tabArea == null) {
            return;
        }
        title.setActive(activeFocus);
        if (newState != AS_INACTIVE) {
            setTitleText();
        }
        if (activeFocus && !isVisible && isView() && isDetached()) {
            // special case for detached views: they do not receive the "setVisible()" flag
            // so we change internal state to "visible" here and perform initial layout
            isVisible = true;
            layout();
        }
        redraw();
    }

    /**
     * Returns the tab associated with the given part or null if none
     *
     * @param part the part to check for
     * @return the tab associated with the given part or null if none
     */
    protected PartTab getTab(IPresentablePart part) {
        if(tabArea == null){
            return null;
        }
        return tabArea.getTab(part);
    }

    protected void setTitleText() {
        if (currentPart == null) {
            return;
        }
        title.setText(getPartTooltip(currentPart));
    }

    public String getPartTooltip(IPresentablePart part){
        String tooltip;
        if (!isView()) {
            tooltip = part.getTitleToolTip();
        } else {
            if (part.getTitleStatus() == null || part.getTitleStatus().trim().equals("")) {
                tooltip = part.getName();
            } else {
                tooltip = part.getName() + " - " + part.getTitleStatus();
            }
        }
        return tooltip;
    }

    public void selectPart(IPresentablePart toSelect) {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":selectPart "
                    + (toSelect != null ? toSelect.getName() : "null"));
        }
        if (currentPart != null && currentPart != toSelect) {
            currentPart.setVisible(false);
            if (currentPart.getToolBar() != null) {
                currentPart.getToolBar().setVisible(false);
            }
        }
        lastSelectedPart = currentPart;
        currentPart = toSelect;
        if (toSelect != null) {
            title.setPresentablePart(toSelect);
            setTitleText();
            /*
             * to visible should be after title stuff to prevent null pointer if we computing
             * title text on old (probably already disposed) part
             */
            toSelect.setVisible(true);
        }

        tabArea.selectPart(toSelect);
        // selected tab is already known
        tabWithMenuFocus = null;
        // getPartsCount() == 1 is for detached views - they don't get the "visible" flag from UI
        if (isVisible || isDetached()) {
            isVisible = true;
            layout();
            redraw();
        }
    }

    public void dispose() {
        currentPart = null;
        lastSelectedPart = null;
        if (partListMenu != null) {
            partListMenu.dispose();
            partListMenu = null;
        }
        if (closedPartListMenu != null) {
            closedPartListMenu.dispose();
            closedPartListMenu = null;
        }
        if (parts != null) {
            parts.clear();
            parts = null;
        }
        if (tabArea != null) {
            tabArea.dispose();
            tabArea = null;
        }
        if (title != null) {
            title.dispose();
            title = null;
        }
        currentTheme = null;
        if (presentationControl != null && !presentationControl.isDisposed()) {
            presentationControl.dispose();
            presentationControl = null;
        }
        if (mainSystemMenu != null) {
            mainSystemMenu.dispose();
            mainSystemMenu = null;
        }
        if (reducedSystemMenu != null) {
            reducedSystemMenu.dispose();
            reducedSystemMenu = null;
        }
    }

    protected void redraw() {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":redraw all");
        }
        title.redraw();
        presentationControl.redraw();
        tabArea.redraw();
        tabButtons.redraw();
    }

    public void setVisible(boolean isVisible) {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":setVisible: " + isVisible
                    + ", was: " + this.isVisible);
        }
        boolean wasHidden = this.isVisible == false;
        this.isVisible = isVisible;
        // if closing, the area was not created
        if (tabArea == null) {
            return;
        }

        if (isVisible) {
            if (currentPart != null && wasHidden) {
                selectPart(currentPart);
            } else {
                // 3.1 code does not need extra selection
                layout();
                redraw();
            }
        } else if (currentPart != null) {
            /*
             * we should always hide the view
             */
            currentPart.setVisible(false);
            if (currentPart.getToolBar() != null) {
                currentPart.getToolBar().setVisible(false);
            }
            presentationControl.setVisible(false);
            tabArea.setVisible(false);
            tabButtons.setVisible(false);
            title.setVisible(false);
        }
    }

    public StackDropResult dragOver(Control currentControl, Point location) {
        if (!tabArea.isVisible()) {
            return null;
        }

        for (Iterator i = parts.values().iterator(); i.hasNext();) {
            PartTab tab = (PartTab) i.next();
            Rectangle tabBounds = tab.getBounds();
            Composite parent = tab.getParent();
            if (tabBounds.contains(parent.toControl(location))) {
                Point p = parent.toDisplay(tabBounds.x, tabBounds.y);
                Integer position = new Integer(tabArea.indexOf(tab));
                return new StackDropResult(new Rectangle(p.x, p.y, tabBounds.width,
                        tabBounds.height), position);
            }
        }
        return null;
    }

    public void setBounds(Rectangle b) {
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":oldbounds " + oldBounds);
        }
        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":setbounds " + b);
        }
        // if closing, the area was not created
        if (tabArea == null) {
            if (PresentationPlugin.DEBUG) {
                System.out.println(getDebugPartName()
                        + " setBounds cancelled as tabArea is NULL");
            }
            return;
        }

        presentationControl.setBounds(b);

        if (b.width != 0 && b.height != 0) {
            // hack for 3.2 M3+ : set visible is not called anymore...
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=119511
            if (!isVisible) {
                // will call layout after that, so no extra layout call needed
                setVisible(true);
            } else {

                if (oldBounds.x != b.x || oldBounds.y != b.y
                        || oldBounds.width != b.width || oldBounds.height != b.height) {

                    layout();
                } else {
                    if (PresentationPlugin.DEBUG) {
                        System.out.println(getDebugPartName()
                                + " setBounds cancelled as unneeded");
                    }
                }
            }
        }
        oldBounds = b;
    }

    protected int getIndexFromCookie(Object cookie) {
        int index;
        if (cookie instanceof Integer) {
            index = ((Integer) cookie).intValue();
        } else if (cookie instanceof String) {
            try {
                index = Integer.parseInt((String) cookie);
            } catch (NumberFormatException e) {
                index = tabArea.getTabCount();
            }
        } else {
            index = tabArea.getTabCount();
        }
        return index;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipseskins.vspresentation.VSStackPresentation#createPartTab(org.eclipse.ui.presentations.IPresentablePart, int)
     */
    protected PartTab createPartTab(IPresentablePart newPart, int index) {
        PartTab partTab = new PartTab(tabArea, SWT.NO_BACKGROUND, newPart, isView());
        tabArea.addTab(index, partTab);
        return partTab;
    }

    public void addPart(final IPresentablePart newPart, Object cookie) {
        if (newPart == null) {
            return;
        }
        if (cookie != null && parts.containsKey(newPart)) {
            /*
             * this crazy code exists because after initialization of tabs, if all parts
             * are already there, PartStack adds all parts again, which is used only
             * to restore the previous tab order. The cookie is absolutely meaningless in
             * this case (it's looks like a random integer which does not have any relation
             * to previously used tab index).
             * As workaround we just add this tab again, which will remove it first and
             * then add it at the end of tab list... Default presentation does the same...
             */
            movePart(newPart, tabArea.getTabCount());
            return;
        }

        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + " :addPart " + newPart.getName());
        }
        newPart.addPropertyListener(propListener);
        final PartTab tab = createPartTab(newPart, getIndexFromCookie(cookie));
        tab.addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(MouseEvent e) {
                setMenuFocus(tab, true);
            }

            public void mouseExit(MouseEvent e) {
                setMenuFocus(tab, false);
            }
        });
        tab.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                if (e.button == 1 || e.button == 3) {
                    changeSelection(newPart, tab);
                }
            }
        });
        tab.addMouseListener(systemMenuListener);
        tab.addMouseListener(focusListener);
        tab.addMouseListener(resizeListener);
        PresentationUtil.addDragListener(tab, dragPartListener);
        parts.put(newPart, tab);
        //        selectPart(newPart);
        // this check could be performed only after the part was selected and is visible
        // because before it doesn't have initialized it's tooltip, which is a part of id
        removeExistingClosedPart(newPart);
    }

    private void movePart(IPresentablePart newPart, int index) {
        PartTab tab = tabArea.getTab(newPart);
        if (tab != null) {
            tabArea.addTab(index, tab);
        }
    }

    private void removeExistingClosedPart(IPresentablePart newPart) {
        if (isView() || closedParts.isEmpty()) {
            // not supported yet
            return;
        }
        ClosedPart closedPart = createEmptyClosedPart(newPart);
        if (closedParts.contains(closedPart)) {
            removeClosedPart(closedPart);
        }
    }

    public void removePart(IPresentablePart oldPart) {
        if (currentPart == oldPart) {
            currentPart = null;
        }
        if (oldPart == null || !parts.containsKey(oldPart)) {
            return;
        }

        //We must remember that we're closing a part (so, it's added
        //to the list of closed parts). Note that it should pass
        //here when closing any editor through any means.
        rememberClosedPart(oldPart);

        if (PresentationPlugin.DEBUG) {
            System.out.println(getDebugPartName() + ":removePart " + oldPart.getName());
        }
        oldPart.removePropertyListener(propListener);
        PartTab tab = (PartTab) parts.remove(oldPart);
        if (parts.size() == 0) {
            currentPart = null;
        }
        tabArea.removeTab(tab);
        if (tab != null && !tab.isDisposed()) {
            tab.dispose();
        }

        boolean closing = isClosing();

        if (!closing) {
            oldPart.setVisible(false);
            if (isVisible) {
                layout();
                redraw();
            }
        }
    }

    public boolean isClosing() {
        return getWorkbench().isClosing();
    }

    protected void setMenuFocus(final PartTab tab, boolean enabled) {
        if (enabled) {
            if (tab.isSelected()) {
                // selected tab is already known
                tabWithMenuFocus = null;
            } else {
                tabWithMenuFocus = tab;
            }
        } else {
            tabWithMenuFocus = null;
        }
        tabArea.setMenuFocus(tab, enabled);
    }

    public ClosedPart[] getClosedPartList() {
        return (ClosedPart[]) closedParts.toArray(new ClosedPart[closedParts.size()]);
    }

    public void clearClosedPartList() {
        ClosedPart[] partList = getClosedPartList();
        for (int i = 0; i < partList.length; i++) {
            removeClosedPart(partList[i]);
        }
        closedParts.clear();
        redraw();
    }

    public void reopenClosedParts(ClosedPart[] partsToReopen) {
        if (isView()) {
            // not supported yet
            return;
        }
        IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();

        for (int i = 0; i < partsToReopen.length; i++) {
            try {
                partsToReopen[i].openEditor(activePage);
            } catch (Exception e) {
                PresentationPlugin.getDefault().getLog().log(
                        new Status(IStatus.ERROR, "VSPresentation", 0,
                                "Could not open editor input for: "
                                + partsToReopen[i].name, e));
            } finally {
                removeClosedPart(partsToReopen[i]);
            }
        }
    }

    public void removeClosedPart(ClosedPart part) {
        int idx = closedParts.indexOf(part);
        if (idx >= 0 && idx < closedParts.size()) {
            ClosedPart removed = (ClosedPart) closedParts.remove(idx);
            if (removed != part) {
                // it could happen, that we just trying to check if there is an old part
                // which is equals to the new one, so that we need to dispose both
                removed.dispose();
            }
            part.dispose();
        }
    }

    private void rememberClosedPart(IPresentablePart oldPart) {
        if (isView()) {
            // XXX not supported yet
            return;
        }
        ClosedPart closedPart = createEmptyClosedPart(oldPart);
        boolean alreadyKnown = closedParts.contains(closedPart);
        if (alreadyKnown) {
            return;
        }
        if (tryToGetInput(closedPart, oldPart)) {
            closedParts.add(0, closedPart);
        } else {
            return;
        }

        Image titleImage = oldPart.getTitleImage();
        if (titleImage != null && !titleImage.isDisposed()) {
            // JFaceResources.getResources().createImageWithDefault(imageDescriptor);
            // The image from closed part will be disposed ASAP, so we need a backup here
            closedPart.image = new Image(getControl().getDisplay(), titleImage
                    .getImageData());
        }
    }

    /**
     * @param part not null
     * @return may return null
     */
    public EditorInfo createEditorInfo(IPresentablePart part){
        ClosedPart closedPart = createEmptyClosedPart(part);
        if (tryToGetInput(closedPart, part)){
            EditorInfo editorInfo = closedPart.getEditorInfo();
            if(editorInfo != null && editorInfo.isConsistent()) {
                return editorInfo;
            }
        }
        return null;
    }

    /**
     * @return "empty" closedPart, which still doesn't contain the editor input.
     */
    private ClosedPart createEmptyClosedPart(IPresentablePart presentablePart) {
        ClosedPart closedPart = new ClosedPart();
        closedPart.name = presentablePart.getName();
        closedPart.id = closedPart.name;

        closedPart.tooltip = presentablePart.getTitleToolTip();
        if (closedPart.tooltip == null) {
            closedPart.tooltip = "";
        }
        if (!isView()) {
            closedPart.id += " | " + closedPart.tooltip;
        }
        return closedPart;
    }

    private boolean tryToGetInput(ClosedPart closedPart, IPresentablePart part) {
        IWorkbenchPartReference partReference = getPartReference(part);
        if(partReference == null){
            partReference = getMatchingPart(closedPart);
        }
        if(!(partReference instanceof IEditorReference)){
            return false;
        }
        // XXX probably this will init editor which is not initialized yet,
        // just before it will be closed....
        try {
            String id = partReference.getId();
            IEditorInput editorInput = ((IEditorReference)partReference).getEditorInput();
            closedPart.setEditorInfo(new EditorInfo(editorInput, id, getPartNumber(part)));
            return closedPart.getEditorInfo().isOpenable();
        } catch (PartInitException e) {
            PresentationPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR, "VSPresentation", 0,
                            "Could not get editor input for: " + closedPart.name, e));
        }
        return false;
    }

    int getPartNumber(IPresentablePart part){
        return tabArea.indexOf((PartTab) parts.get(part));
    }

    static IWorkbenchPartReference getPartReference(IPresentablePart part){
        if(!(part instanceof PresentablePart)){
            return null;
        }
        PresentablePart ppart = (PresentablePart) part;
        if(ppart.getPane() != null) {
            return ppart.getPane().getPartReference();
        }
        return  null;
    }

    private static IWorkbenchPartReference getMatchingPart(ClosedPart closedPart){
        IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPartReference[] editorReferences = activePage.getEditorReferences();
        IWorkbenchPartReference partReference = null;
        for (int i = 0; i < editorReferences.length; i++) {
            IWorkbenchPartReference reference = editorReferences[i];
            if (!closedPart.name.equals(reference.getTitle())) {
                continue;
            }
            if (!closedPart.tooltip.equals(reference.getTitleToolTip())) {
                continue;
            }
            if (partReference != null) {
                // multiple instances of same editor are not supported by
                // "reopen" action
                return null;
            }
            partReference = reference;
        }
        return partReference;
    }


    public boolean hasClosedParts() {
        return !closedParts.isEmpty();
    }

    /**
     * Shortcut to close parts. They're remembered later in the removePart()
     * (which is able to catch Ctrl+F4 and Ctrl+W)
     */
    public void close(IPresentablePart[] partsToClose) {
        getSite().close(partsToClose);
    }

    public void closeOthers(IPresentablePart current, boolean left) {
        IPresentablePart[] partList = tabArea.getPartList(current, left);
        if(partList == null || partList.length == 0){
            return;
        }
        close(partList);
    }

    /**
     * @param flag one of F_* constants
     * @return true if this flag is set for current part stack
     */
    public final boolean getFlag(int flag) {
        return stackFlags.get(flag);
    }

    /**
     * Set the visualization flag which immediately causes relayout and redraw
     * @param flag one of F_* constants
     * @param value true to set flag (surprise :)
     */
    public final void setFlag(int flag, boolean value) {
        stackFlags.set(flag, value);
        layout();
        redraw();
    }

    protected String getDebugPartName() {
        return (isView() ? "view " : "editor ")
        + (currentPart != null ? currentPart.getName() : "(?)");
    }

    protected IPreferenceStore getPrefs() {
        return PresentationPlugin.getDefault().getPreferenceStore();
    }

    protected void closeOnMiddleClick() {
        IPresentablePart partToClose = currentPart;
        if (tabWithMenuFocus != null) {
            // shouldn't happen...
            partToClose = tabWithMenuFocus.getPart();
        }
        if (partToClose != null
                && getPrefs().getBoolean(ThemeConstants.CLOSE_TAB_ON_MIDDLE_CLICK)) {
            close(new IPresentablePart[] { partToClose });
        }
    }

    protected void changeSelection(final IPresentablePart partToSelect,
            final PartTab selecedTab) {
        if (getPrefs().getBoolean(ThemeConstants.USE_FAST_TAB_SWITCH)) {
            if (lastSelectedPart != currentPart && lastSelectedPart != null
                    && selecedTab.getPart() == currentPart) {
                getSite().selectPart(lastSelectedPart);
            } else {
                getSite().selectPart(partToSelect);
            }
        } else {
            getSite().selectPart(partToSelect);
        }
    }

    public final class ClosedPart {

        public String name;

        public String tooltip;

        public Image image;

        public String id;

        private EditorInfo info;

        public ClosedPart() {
            super();
        }

        public void openEditor(IWorkbenchPage activePage) throws PartInitException {
            if(info != null) {
                info.openEditor(activePage);
            }
        }

        public void remove() {
            removeClosedPart(this);
        }

        public void dispose() {
            setEditorInfo(null);
            if (image != null && !image.isDisposed()) {
                image.dispose();
            }
            image = null;
        }

        public void reopen() {
            reopenClosedParts(new ClosedPart[] { this });
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (id == null || !(o instanceof ClosedPart)) {
                return false;
            }
            return id.equals(((ClosedPart) o).id);
        }

        public int hashCode() {
            return id == null ? 0 : id.hashCode();
        }

        public void setEditorInfo(EditorInfo info) {
            this.info = info;
        }

        public EditorInfo getEditorInfo() {
            return info;
        }

    }

    public void sortTabs() {
        tabArea.sortTabs();
    }

}
