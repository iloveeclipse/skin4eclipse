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
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.presentation.VSStackPresentation;
import de.loskutov.eclipseskins.presentation.VSStackPresentation.ClosedPart;

/**
 * Represents the area where the tabs are placed
 *
 * @author wmitsuda
 * @author Andrei
 */
public final class TabArea extends Canvas implements PaintListener {

    private static int Y_GAP = 1;

    private static final String TAG_PART = "part";

    private static final String TAG_ID = "id";

    private static final IPresentablePart[] EMPTY = new IPresentablePart[0];

    private int xPosition;
    private int totalWidth;
    private Rectangle tabArea;
    private final List/*<PartTab>*/ tabs;
    private PartTab selectedTab;
    private TabAreaButtons tabButtons;
    private ThemeWrapper currentTheme;

    private int hiddenTabCount;
    private int lastComputedWidth;
    private final List layoutListeners;

    private final boolean isView;
    private boolean initDone;
    private boolean hasEnoughSpace;
    private boolean isEditorListPinned;
    private boolean themeChanged;
    private Font boldFont;
    private Font regularFont;
    private int boldFontHeight;
    private int regularFontHeight;

    private Point tabButtSize;
    private int height;

    private VSStackPresentation presentation;

    /** true to keep selected sort order */
    private boolean alwaysSortTabs;

    /** sort order, false for unchanged default sort order (alphabetically) */
    private boolean sortOrder;

    public boolean isEditorListPinned() {
        return isEditorListPinned;
    }

    public void setEditorListPinned(boolean isEditorListPinned) {
        this.isEditorListPinned = isEditorListPinned;
        currentTheme.setBoolean(ThemeConstants.TAB_LIST_PINNED, isEditorListPinned);
    }

    public TabArea(int style, VSStackPresentation presentation) {
        super((Composite)presentation.getControl(), style);
        this.presentation = presentation;
        this.isView = presentation.isView();
        tabs = new ArrayList();
        layoutListeners = new ArrayList();
        addPaintListener(this);
        setCurrentTheme(presentation.getCurrentTheme());
    }

    public boolean isShowIcon() {
        if(isView){
            return getCurrentTheme().getBoolean(ThemeConstants.SHOW_VIEW_ICON);
        }
        return getCurrentTheme().getBoolean(ThemeConstants.SHOW_EDITOR_ICON);
    }

    public int computeHeight() {
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:comupteHeight");
        }
        if(height != 0){
            return height;
        }

        if(boldFontHeight == 0){
            computeFontsHeight();
        }
        // we assume, that bold font has probably bigger size,
        // and that view never will have bold tabs :)
        int textHeight = isView? regularFontHeight : boldFontHeight;
        int tabHeight = textHeight;
        boolean showIcon = isShowIcon();
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            int imageHeight = tab.computeHeight(textHeight, showIcon);
            tabHeight = Math.max(tabHeight, imageHeight);
        }
        height = tabHeight + Y_GAP;
        return height;
    }

    protected void computeFontsHeight(){
        GC gc = new GC(this);
        gc.setFont(getBoldFont());
        boldFontHeight = gc.stringExtent("W").y;
        gc.setFont(getRegularFont());
        regularFontHeight = gc.stringExtent("W").y;
        gc.dispose();
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:comupteSize(hint): " + wHint);
        }
        int minWidth = 0;
        int maxWidth = 0;

        tabButtSize = tabButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            Point tabSize = tab.computeSize(false, null);
            minWidth += tabSize.x;
            maxWidth += tab.computeSize(true, null).x;
        }
        if(maxWidth <= wHint - tabButtSize.x){
            lastComputedWidth = maxWidth;
            hasEnoughSpace = true;
        } else {
            lastComputedWidth = minWidth;
            hasEnoughSpace = false;
        }
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:hasSpace: " + hasEnoughSpace);
        }

        return new Point(lastComputedWidth, computeHeight());
    }

    public int getEstimatedWidth(){
        return lastComputedWidth;
    }

    public void addLayoutListener(Listener listener){
        if(layoutListeners.contains(listener)){
            return;
        }
        layoutListeners.add(listener);
    }

    public void removeLayoutListener(Listener listener){
        if(!layoutListeners.contains(listener)){
            return;
        }
        layoutListeners.remove(listener);
    }

    public boolean hasEnoughSpace(){
        return hasEnoughSpace;
    }

    public void addTab(int index, PartTab tab) {
        if(PresentationPlugin.DEBUG_STATE) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:add tab");
        }
        if (tabs.contains(tab)) {
            tabs.remove(tab);
        }
        if(index >= 0 && index < tabs.size()){
            tabs.add(index, tab);
        } else {
            tabs.add(tab);
        }
        height = 0;
        if(alwaysSortTabs){
            sortTabs(false);
        } else {
            layoutTabs();
        }
        //        redraw();
    }

    public void removeTab(PartTab tab) {
        if(tab == null || presentation == null) {
            return;
        }
        if(PresentationPlugin.DEBUG_STATE) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:remove tab");
        }
        boolean removed = tabs.remove(tab);
        if(removed) {
            if(tabs.size() == 0){
                selectedTab = null;
            }
            if(!presentation.isClosing()){
                // recompute overall tab width
                computeSize(getParent().getClientArea().width, SWT.DEFAULT, true);
                if(alwaysSortTabs){
                    sortTabs(false);
                } else {
                    layoutTabs();
                }
                //                redraw();
            }
        }
    }

    public void selectPart(IPresentablePart part) {
        initDone = true;
        if(PresentationPlugin.DEBUG_STATE) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:select part");
        }
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            boolean selected = tab.getPart() == part;
            if (selected) {
                selectedTab = tab;
            }
            tab.setSelected(selected);
        }
        // recompute overall tab width
        computeSize(getParent().getClientArea().width, SWT.DEFAULT, true);
        layoutTabs();
        // now it could happen, that the selected tab is invisible
        if(!isSelectedTabVisible()){
            setSelectedTabVisible();
        }
        //        redraw();
    }

    public int indexOf(PartTab tab) {
        if(tabs == null){
            return -1;
        }
        return tabs.indexOf(tab);
    }

    public PartTab getTab(int index){
        if(tabs == null || index < 0 || index >= tabs.size()){
            return null;
        }
        return (PartTab) tabs.get(index);
    }

    private boolean isSelectedTabVisible() {
        return selectedTab != null && !selectedTab.isHidden();
    }

    public boolean isNextTabSelected(PartTab tab) {
        int index = indexOf(tab);
        if (index + 1 >= tabs.size()) {
            return false;
        }
        return ((PartTab) tabs.get(index + 1)).isSelected();
    }

    public void moveLeft() {
        Rectangle clientArea = getClientArea();
        if (xPosition + totalWidth > clientArea.width) {
            xPosition -= getCurrentTheme().getInt(ThemeConstants.MOVE_TAB_AMOUNT);
            if (xPosition + totalWidth < clientArea.x + clientArea.width) {
                xPosition = clientArea.x + clientArea.width - totalWidth;
            }
            tabButtons.setLeftEnabled(true);
            layoutTabs();
            //            redraw();
        } else {
            tabButtons.setRightEnabled(false);
        }
    }

    public void moveRight() {
        if (xPosition < 0) {
            xPosition += getCurrentTheme().getInt(ThemeConstants.MOVE_TAB_AMOUNT);
            Rectangle clientArea = getClientArea();
            if (xPosition > clientArea.x) {
                xPosition = clientArea.x;
            }
            tabButtons.setRightEnabled(true);
            layoutTabs();
            //            redraw();
        } else {
            tabButtons.setLeftEnabled(false);
        }
    }


    public void setTabAreaButtons(TabAreaButtons tabButtons) {
        this.tabButtons = tabButtons;
    }

    public PartTab getTab(IPresentablePart part) {
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            if(tab.getPart() == part){
                return tab;
            }
        }
        return null;
    }

    public ThemeWrapper getCurrentTheme(){
        return currentTheme;
    }

    public void setCurrentTheme(ThemeWrapper theme){
        themeChanged = true;
        height = 0;
        currentTheme = theme;
        isEditorListPinned = theme.getBoolean(ThemeConstants.TAB_LIST_PINNED);
        if(boldFont != null){
            // we need to dispose bold font as we have created it here
            if(!boldFont.isDisposed()) {
                boldFont.dispose();
            }
            boldFont = null;
        }
        // regular font do not need to be disposed, as it cames from theme directly
        regularFont = null;
        boldFontHeight = 0;
        regularFontHeight = 0;
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            tab.themeChanged();
        }
        boolean oldSortProperty = alwaysSortTabs;
        alwaysSortTabs = theme.getBoolean(
                isView ? ThemeConstants.ALWAYS_SORT_VIEW_TABS
                        : ThemeConstants.ALWAYS_SORT_EDITOR_TABS);
        if(alwaysSortTabs && !oldSortProperty){
            sortTabs(false);
        }
    }

    public void redraw(){
        if(PresentationPlugin.DEBUG_PAINT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:redraw");
        }
        //        super.redraw();
        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            tab.redraw();
        }
    }

    public void dispose(){
        if (isDisposed()) {
            return;
        }
        presentation = null;

        if(tabButtons != null && !tabButtons.isDisposed()){
            tabButtons.dispose();
            tabButtons = null;
        }
        if(selectedTab != null && !selectedTab.isDisposed()){
            selectedTab.dispose();
            selectedTab = null;
        }
        if(layoutListeners != null){
            layoutListeners.clear();
        }
        if(tabs != null) {
            for (int i = 0; i < tabs.size(); i++) {
                PartTab tab = (PartTab) tabs.get(i);
                if(tab != null && !tab.isDisposed()){
                    tab.dispose();
                }
            }
            tabs.clear();
        }
        if(boldFont != null){
            // we need to dispose bold font as we have created it here
            if(!boldFont.isDisposed()) {
                boldFont.dispose();
            }
            boldFont = null;
        }
        // regular font do not need to be disposed, as it cames from theme directly
        regularFont = null;
        currentTheme = null;
        super.dispose();
    }

    public void paintControl(PaintEvent e) {
        if(PresentationPlugin.DEBUG_PAINT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:paint");
        }
        if(!initDone){
            layoutTabs();
        }
        GC gc = e.gc;
        Rectangle clientArea = getClientArea();
        gc.setClipping(clientArea);
        ThemeWrapper theme = getCurrentTheme();
        gc.setBackground(theme.getColor(ThemeConstants.TAB_COLOR_NOFOCUS));
        //        gc.setBackground(e.display.getSystemColor(SWT.COLOR_GREEN));
        gc.fillRectangle(clientArea);

        boolean topPosition;
        if (isView) {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.VIEW_TAB_POSITION);
        } else {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.EDITOR_TAB_POSITION);
        }
        // divisor between tabarea and higher/lower component
        if (topPosition) {
            gc.setForeground(e.display
                    .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
            if (isView){

                //             divisor on bottom
                gc.drawLine(tabArea.x,
                        clientArea.y + clientArea.height - 2,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - 2);
                gc.setForeground(theme.getColor(ThemeConstants.TAB_COLOR_FOCUS));
                //           2nd  divisor on bottom
                gc.drawLine(tabArea.x,
                        clientArea.y + clientArea.height - 1,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - 1);
            } else {
                gc.drawLine(tabArea.x,
                        clientArea.y + clientArea.height - 1,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - 1);
            }

        } else {
            gc.setForeground(e.display
                    .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            // divisor on top
            gc.drawLine(clientArea.x, clientArea.y,
                    clientArea.x + clientArea.width - 1, clientArea.y);

            gc.setForeground(e.display
                    .getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            // divisor on top
            gc.drawLine(tabArea.x, clientArea.y + 1,
                    clientArea.x + clientArea.width - 1, clientArea.y + 1);
        }
        gc.setClipping((Rectangle)null);
    }

    public void restoreState(IPresentationSerializer context, IMemento memento,
            VSStackPresentation presentation1) {
        if(memento == null){
            return;
        }
        IMemento[] parts = memento.getChildren(TAG_PART);

        if(presentation1.isView()){
            /*
             * the views seems do not have their tabs already created,
             * so we need simply to add new parts to presentation
             */
            for (int i = 0; i < parts.length; i++) {
                String id = parts[i].getString(TAG_ID);
                if (id != null) {
                    IPresentablePart part = context.getPart(id);
                    if (part != null) {
                        presentation1.addPart(part, id);
                    }
                }
            }
        } else {
            /*
             * for editors, there is always the tabs exists...
             * So we need "only" to reorder them to last saved state
             */
            for (int i = 0; i < parts.length; i++) {
                String id = parts[i].getString(TAG_ID);
                IPresentablePart part = null;
                if (id != null) {
                    part = context.getPart(id);
                    if (part == null) {
                        break;
                    }
                }
                for (int j = i; j < tabs.size(); j++) {
                    if(((PartTab)tabs.get(j)).getPart() == part){
                        if(j == i){
                            break; // already right position
                        }
                        PartTab partTab = (PartTab) tabs.remove(j);
                        tabs.add(i, partTab);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param context
     * @param memento
     */
    public void saveState(IPresentationSerializer context, IMemento memento) {
        for (int i = 0; i < tabs.size(); i++) {
            PartTab partTab = (PartTab) tabs.get(i);
            IMemento childMem = memento.createChild(TAG_PART);
            childMem.putString(TAG_ID, context.getId(partTab.getPart()));
        }
    }

    public IPresentablePart getSelectedPart() {
        if(selectedTab == null || selectedTab.isDisposed()){
            return null;
        }
        return selectedTab.getPart();
    }

    /**
     * @return list of parts, connected with tabs
     */
    public IPresentablePart[] getPartList() {
        if(tabs == null || tabs.isEmpty()){
            return EMPTY;
        }
        List parts = new ArrayList(tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            PartTab partTab = (PartTab) tabs.get(i);
            parts.add(partTab.getPart());
        }
        return (IPresentablePart[]) parts.toArray(new IPresentablePart[parts.size()]);
    }

    /**
     * @return list of parts, connected with tabs
     */
    public IPresentablePart[] getPartList(IPresentablePart part, boolean left) {
        if(tabs == null || tabs.isEmpty()){
            return EMPTY;
        }
        List parts = new ArrayList(tabs.size());
        if(left) {
            for (int i = 0; i < tabs.size(); i++) {
                PartTab partTab = (PartTab) tabs.get(i);
                IPresentablePart currPart = partTab.getPart();
                if(currPart == part){
                    break;
                }
                parts.add(currPart);
            }
        } else {
            for (int i = tabs.size() - 1; i >= 0; i--) {
                PartTab partTab = (PartTab) tabs.get(i);
                IPresentablePart currPart = partTab.getPart();
                if(currPart == part){
                    break;
                }
                parts.add(currPart);
            }
        }
        return (IPresentablePart[]) parts.toArray(new IPresentablePart[parts.size()]);
    }

    public IPresentablePart[] getHiddenPartList() {
        if(tabs == null || tabs.isEmpty()){
            return EMPTY;
        }
        List parts = new ArrayList(tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            PartTab partTab = (PartTab) tabs.get(i);
            if(partTab.isHidden()) {
                parts.add(partTab.getPart());
            }
        }
        return (IPresentablePart[]) parts.toArray(new IPresentablePart[parts.size()]);
    }

    public int getTabCount(){
        if(tabs == null){
            return 0;
        }
        return tabs.size();
    }

    public void setMenuFocus(final PartTab tab, boolean enabled) {
        if(enabled){
            // reset possible focus flag on all tabs
            for (int i = 0; i < tabs.size(); i++) {
                PartTab element = (PartTab) tabs.get(i);
                element.setHasMenuFocus(false);
            }
        }
        tab.setHasMenuFocus(enabled);
    }

    public void setBounds(int x, int y, int width, int height) {
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:setBounds");
        }
        Rectangle oldBounds = getBounds();
        super.setBounds(x, y, width, height);
        if(!initDone || width == 0 || height == 0){
            if(PresentationPlugin.DEBUG_LAYOUT) {
                System.out.println((presentation.isView()? "view" : "edit") + "Tabs: setBounds cancelled as invisible");
            }
            return;
        }
        if(!tabs.isEmpty() && (themeChanged
                || (oldBounds.x != x || oldBounds.y != y || oldBounds.width != width
                        || oldBounds.height != height))) {
            // recompute overall tab width
            computeSize(getParent().getClientArea().width, SWT.DEFAULT, true);
            layoutTabs();
            themeChanged = false;
        } else {
            if(PresentationPlugin.DEBUG_LAYOUT) {
                System.out.println((presentation.isView()? "view" : "edit") + "Tabs: setBounds cancelled as unneeded");
            }
        }
    }

    protected void layoutTabs() {
        if(PresentationPlugin.DEBUG_LAYOUT) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:layout");
        }
        Rectangle clientArea = getClientArea();
        if(!initDone || clientArea.width == 0 || clientArea.height == 0 || tabs.isEmpty()){
            if(initDone){
                // listeners may be interested even if we do not need to relayout us
                //                notifyListeners();
            }
            return;
        }
        // TODO check if we do not really need to recompute it here
        //      tabButtSize = tabButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        tabArea = Geometry.copy(clientArea);

        // calculate real total width
        totalWidth = 0;

        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            int width = tab.computeSize(hasEnoughSpace, null).x;
            totalWidth += width;
        }

        if(hasEnoughSpace){
            xPosition = 0;
        } else {
            if (totalWidth < clientArea.width - tabButtSize.x) {
                xPosition = clientArea.x;
            }
            if (xPosition < clientArea.x
                    && xPosition + totalWidth < clientArea.x + clientArea.width - tabButtSize.x) {
                xPosition = clientArea.x + clientArea.width - totalWidth;
            }
            tabArea.x += xPosition;
        }

        int oldHiddenCount = hiddenTabCount;
        hiddenTabCount = 0;

        for (int i = 0; i < tabs.size(); i++) {
            PartTab tab = (PartTab) tabs.get(i);
            Point tabSize = tab.computeSize(hasEnoughSpace, null);
            // minimum visible pixel to be counted as "visible" tab is a half tab width
            int minVisibleArea = tabSize.x / 2;
            // invisible with "minVisibleArea" tolerance
            boolean inVisible = tabArea.x + tabSize.x - minVisibleArea < clientArea.x
            || tabArea.x + minVisibleArea > clientArea.x + clientArea.width;
            // really really not visible on screen
            boolean reallyInVisible = tabArea.x + tabSize.x < clientArea.x
            || tabArea.x > clientArea.x + clientArea.width;
            tab.setHidden(inVisible);
            if(inVisible){
                if(reallyInVisible){
                    tab.setVisible(false);
                } else {
                    tab.setVisible(true);
                }
                // for user, the tab isn't really accessible now
                hiddenTabCount ++;
            } else {
                tab.setVisible(true);
            }

            // TODO does we need to distinct between top/bottom instead here?
            if (isView) {
                tab.setBounds(tabArea.x, tabArea.y + 1, tabSize.x, tabArea.height - Y_GAP);
            } else {
                tab.setBounds(tabArea.x, tabArea.y + tabArea.height
                        - tabSize.y, tabSize.x, tabSize.y);
            }
            tabArea.x += tabSize.x;
        }

        boolean showLeft = xPosition < clientArea.x - 2;
        boolean showRight = xPosition + totalWidth  > clientArea.x + clientArea.width + 2;
        boolean navigatorShown = showLeft || showRight || hiddenTabCount > 0;
        tabButtons.setLeftEnabled(showLeft);
        tabButtons.setRightEnabled(showRight);
        tabButtons.setShowNavigator(navigatorShown);
        if(oldHiddenCount != hiddenTabCount){
            // number of hidden editors changed
            tabButtons.redraw();
        }
        initDone = true;
        notifyListeners();
    }

    private void notifyListeners() {
        Event event = new Event();
        event.count = hiddenTabCount;
        for (int i = 0; i < layoutListeners.size(); i++) {
            Listener listener = (Listener) layoutListeners.get(i);
            listener.handleEvent(event);
        }
    }

    private void setSelectedTabVisible() {
        if(selectedTab == null){
            return;
        }

        Rectangle clientArea = getClientArea();
        Rectangle bounds = selectedTab.getBounds();
        boolean needLayout = false;
        if (bounds.x + bounds.width > clientArea.x + clientArea.width) {
            xPosition -= bounds.x + bounds.width - (clientArea.x + clientArea.width);
            needLayout = true;
        } else if (bounds.x < clientArea.x) {
            xPosition += clientArea.x - bounds.x;
            needLayout = true;
        }
        if(needLayout) {
            if(PresentationPlugin.DEBUG_STATE) {
                System.out.println((presentation.isView()? "view" : "edit") + "Tabs:setSelectedTabVisible");
            }
            layoutTabs();
            //            redraw();
        }
    }

    public void setVisible(boolean visible) {
        if(PresentationPlugin.DEBUG_STATE) {
            System.out.println((presentation.isView()? "view" : "edit") + "Tabs:setVisible: " + visible + ", was: " + this.isVisible());
        }
        super.setVisible(visible);
        if(visible && !initDone){
            layoutTabs();
        }
    }

    public ClosedPart[] getClosedPartList() {
        return presentation.getClosedPartList();
    }

    public VSStackPresentation getPresentation() {
        return presentation;
    }

    public Font getBoldFont() {
        if(boldFont == null){
            Font font = getRegularFont();
            FontData[] fontData = font.getFontData();
            boldFont = new Font(getDisplay(), fontData[0].getName(), fontData[0]
                                                                              .getHeight(), fontData[0].getStyle() | SWT.BOLD);
        }
        return boldFont;
    }

    public Font getRegularFont() {
        if(regularFont == null){
            regularFont = getCurrentTheme().getFont(ThemeConstants.TAB_FONT);
        }
        return regularFont;
    }

    public void sortTabs() {
        sortTabs(true);
    }

    private void sortTabs(boolean allowRevert) {
        List initialList = new ArrayList(tabs);
        Collections.sort(tabs, new TabComparator(sortOrder));
        if(allowRevert && sameOrder(initialList, tabs)){
            Collections.reverse(tabs);
            sortOrder = !sortOrder;
        }

        layoutTabs();
        redraw();
    }

    private boolean sameOrder(List initialList, List tabs2) {
        for (int i = 0; i < initialList.size(); i++) {
            Object o1 = initialList.get(i);
            Object o2 = tabs2.get(i);
            if(o1 != o2){
                return false;
            }
        }
        return true;
    }

    private final static class TabComparator implements Comparator {
        private final boolean changedOrder;

        public TabComparator(boolean reverseOrder) {
            this.changedOrder = reverseOrder;
        }

        public int compare(Object o1, Object o2) {
            if(o1 == o2 || !(o1 instanceof PartTab) || !(o2 instanceof PartTab)){
                return 0;
            }
            PartTab one = (PartTab) o1;
            PartTab another = (PartTab) o2;
            String name1 = one.getPart().getName();
            String name2 = another.getPart().getName();
            if(name1 == null || name2 == null){
                return 0;
            }
            int order = name1.compareTo(name2);
            return changedOrder? -order : order;
        }
    }

}

