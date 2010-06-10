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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.presentations.IPresentablePart;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;

/**
 * Represents a unique tab
 *
 * @author wmitsuda
 * @author Andrei
 */
public class PartTab extends Canvas implements PaintListener {

    public static final String DIRTY_PREFIX = "*";

    private static int LEADING_TEXT_GAP = 1;

    private IPresentablePart part;

    private String partText;
    private String partTextUnchanged;
    private TabArea tabArea;

    private final boolean isView;
    private final boolean boldSelected;
    private boolean selected;
    private boolean hasMenuFocus;
    private boolean isHidden;
    private boolean tabNameReduced;
    private boolean partIsDirty;

    private String altPartText;

    private Point cacheBoldLong;
    private Point cacheBoldShort;
    private Point cacheRegularLong;
    private Point cacheRegularShort;
    private final Point cacheNoText;

    private int heightWithImage;
    private int heightWithoutImage;
    private boolean isShowIcon;
    private boolean hideViewTitle;
    private Point maxSize;
    private Point minSize;

    boolean topPosition;
    Color colorTabNoFocus;
    Color colorTabFocus;
    Color colorTextFocus;
    Color colorTextNoFocus;
    Color colorTabTextFocus;
    Color colorTabTextNoFocus;
    Color colorDirtyTab;
    Color colorHighlight;
    Color colorDark;
    Color colorShadow;
    int xPad;
    int yPad;


    public PartTab(final TabArea parent, int style, IPresentablePart part, boolean isView) {
        super(parent, style);
        cacheNoText = new Point(0, 0);
        tabArea = parent;
        this.part = part;
        this.boldSelected = ! isView;
        this.isView = isView;

        setToolTipText(tabArea.getPresentation().getPartTooltip(part));

        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                parent.removeTab(PartTab.this);
            }
        });
        addPaintListener(this);
        themeChanged();
    }

    public int computeHeight(int textHeight, boolean showIcon) {
        if (showIcon) {
            if(heightWithImage != 0){
                return heightWithImage;
            }
            ImageData imageData = part.getTitleImage().getImageData();
            heightWithImage = Math.max(textHeight, imageData.height) + yPad * 2 + 3;
            return heightWithImage;
        }
        if(heightWithoutImage != 0){
            return heightWithoutImage;
        }
        heightWithoutImage =  textHeight + yPad * 2 + 3;
        return heightWithoutImage;
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point p = computeSize(tabArea.hasEnoughSpace(), null);
        return p;
    }

    public Point computeSize(boolean assumeEnoughSpace, GC gc) {

        if(assumeEnoughSpace && maxSize != null){
            return maxSize;
        }
        if(!assumeEnoughSpace && minSize != null){
            return minSize;
        }

        Point textSize;

        if(partText == null) {
            computeTabText();
            boolean needGc = gc == null;
            if(gc == null) {
                gc = new GC(this);
            }
            gc.setFont(tabArea.getBoldFont());
            if(!tabNameReduced){
                cacheBoldLong = gc.stringExtent(partText);
                cacheBoldShort = gc.stringExtent(altPartText);
            } else {
                cacheBoldLong = gc.stringExtent(altPartText);
                cacheBoldShort = gc.stringExtent(partText);
            }

            gc.setFont(tabArea.getRegularFont());
            if(!tabNameReduced){
                cacheRegularLong = gc.stringExtent(partText);
                cacheRegularShort = gc.stringExtent(altPartText);
            } else {
                cacheRegularLong = gc.stringExtent(altPartText);
                cacheRegularShort = gc.stringExtent(partText);
            }
            if(needGc){
                gc.dispose();
            }
        }
        if(hideViewTitle){
            textSize = cacheNoText;
        } else {
            if (boldSelected && selected){
                if(assumeEnoughSpace){
                    textSize = cacheBoldLong;
                } else {
                    textSize = cacheBoldShort;
                }
            } else {
                if(assumeEnoughSpace){
                    textSize = cacheRegularLong;
                } else {
                    textSize = cacheRegularShort;
                }
            }
        }

        Point size;
        if (isShowIcon) {
            ImageData imageData = part.getTitleImage().getImageData();
            int height = Math.max(textSize.y, imageData.height) + yPad * 2 + 3;
            int width = textSize.x + imageData.width + xPad * 2 + LEADING_TEXT_GAP;
            size = new Point(width, height);
        } else {
            size = new Point(textSize.x + xPad * 2 + 2, textSize.y + yPad * 2 + 3);
        }

        if(assumeEnoughSpace){
            maxSize = size;
        } else {
            minSize = size;
        }
        return size;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        // rare case where we select a tab and at the same time the part changed the content
        // this happens on editors, re-used for the search results
        String newName = null;
        if(part != null) {
            newName = part.getName();
        }
        if(newName != null && !newName.equals(partTextUnchanged)){
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed input from "
                        + partTextUnchanged + " to " + newName);
            }

            setPartText(null);
            // save the real part name for the next time
            partTextUnchanged = newName;
        }
        if(boldSelected){
            maxSize = null;
            minSize = null;
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public IPresentablePart getPart() {
        return part;
    }

    public void paintControl(PaintEvent e) {

        Rectangle clientArea = getClientArea();
        GC gc = e.gc;
        gc.setClipping(clientArea);

        int leftBorder = clientArea.x;
        int rightBorder = clientArea.x + clientArea.width - 1;
        int topBorder = clientArea.y;
        int bottomBorder = clientArea.y + clientArea.height - 1;

        // Background
        if (!selected) {
            gc.setBackground(colorTabNoFocus);
        } else {
            gc.setBackground(colorTabFocus);
        }
        gc.fillRectangle(clientArea);

        // Draw horizontal divisor(s) line for not selected tabs (betweent tab and content)

        if (!selected) {
            if (topPosition) {
                gc.setForeground(colorHighlight);
                if(isView){
                    // bottom line
                    gc.drawLine(leftBorder, bottomBorder - 1, rightBorder, bottomBorder - 1);
                    gc.setForeground(colorTabFocus);
                    // 2nd bottom line
                    gc.drawLine(leftBorder, bottomBorder, rightBorder, bottomBorder);
                } else {
                    // bottom line
                    gc.drawLine(leftBorder, bottomBorder, rightBorder, bottomBorder);
                }
            } else {
                gc.setForeground(colorDark);
                // top line
                gc.drawLine(leftBorder, topBorder, rightBorder, topBorder);
            }
        }

        // Selected tab
        if (selected) {
            // Left border
            gc.setForeground(colorHighlight);
            gc.drawLine(leftBorder, topBorder, leftBorder, bottomBorder);

            // Right border
            gc.setForeground(colorDark);
            gc.drawLine(rightBorder, topBorder, rightBorder, bottomBorder);

            if (topPosition) {
                // Top border
                gc.setForeground(colorHighlight);
                gc.drawLine(leftBorder, topBorder, rightBorder, topBorder);
            } else {
                // Bottom border
                gc.setForeground(colorDark);
                gc.drawLine(leftBorder, bottomBorder - 1, rightBorder, bottomBorder - 1);
                gc.setForeground(colorTabNoFocus);
                gc.drawLine(leftBorder, bottomBorder, rightBorder, bottomBorder);
            }
        }

        if (selected) {
            gc.setForeground(colorTextFocus);
        } else {
            gc.setForeground(colorShadow);
            if (!((TabArea) getParent()).isNextTabSelected(this)) {
                gc.drawLine(rightBorder, topBorder + 3, rightBorder, bottomBorder - 2);
            }
            gc.setForeground(colorTextNoFocus);
        }

        int x = leftBorder + xPad;
        String partName;
        Point stringExtent;
        if(hideViewTitle){
            stringExtent = cacheNoText;
            partName = "";
        } else {
            partName = computeTabText();
            if (boldSelected && selected) {
                gc.setFont(tabArea.getBoldFont());
                if(tabNameReduced){
                    stringExtent = cacheBoldShort;
                } else {
                    stringExtent = cacheBoldLong;
                }
            } else {
                gc.setFont(tabArea.getRegularFont());
                if(tabNameReduced){
                    stringExtent = cacheRegularShort;
                } else {
                    stringExtent = cacheRegularLong;
                }
            }
        }

        int textY = clientArea.y + yPad + 1;

        if (isShowIcon) {
            Image image = part.getTitleImage();
            gc.drawImage(image, x, clientArea.y + yPad + 1);
            ImageData imageData = image.getImageData();
            x += imageData.width;
            if(stringExtent.y + 1 < imageData.height){
                textY +=  imageData.height - stringExtent.y - 1;
            }
        }

        x += LEADING_TEXT_GAP;

        if(hasMenuFocus && !isSelected()){
            gc.setForeground(colorTabTextFocus);
            // - 1 to not touch the tab border
            gc.drawLine(x, textY + stringExtent.y,
                    x + stringExtent.x - 1, textY + stringExtent.y);
        } else {
            if(isSelected()) {
                gc.setForeground(colorTabTextFocus);
            } else {
                gc.setForeground(colorTabTextNoFocus);
            }
        }

        if(partIsDirty()){
            gc.setForeground(colorDirtyTab);
        }

        // foreground color remains the same if tab has focus or has menu focus
        if(!hideViewTitle) {
            gc.drawString(partName, x, textY, true);
        }
        gc.setClipping((Rectangle)null);
    }

    /**
     * @param property one of IWorkbenchPartConstants.PROP_ constants
     *
     */
    public void refresh(int property) {
        switch (property) {
        case IWorkbenchPartConstants.PROP_DIRTY:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:dirty");
            }
            setToolTipText(tabArea.getPresentation().getPartTooltip(part));
            setPartText(null);
            partIsDirty = part.isDirty();
            tabArea.layoutTabs();
            break;
        case IWorkbenchPartConstants.PROP_TITLE:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed title");
            }
            setToolTipText(tabArea.getPresentation().getPartTooltip(part));
            setPartText(null);
            break;
        case IWorkbenchPartConstants.PROP_PART_NAME:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed name");
            }
            setToolTipText(tabArea.getPresentation().getPartTooltip(part));
            setPartText(null);
            break;
        case IWorkbenchPartConstants.PROP_INPUT:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed input");
            }
            setToolTipText(tabArea.getPresentation().getPartTooltip(part));
            setPartText(null);
            break;
        case IWorkbenchPartConstants.PROP_PREFERRED_SIZE:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed pref_size");
            }
            break;
        case IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed content descr");
            }
            break;
        default:
            if(PresentationPlugin.DEBUG) {
                System.out.println("PartTab:changed?");
            }
            return;
        }
        if(!isHidden()) {
            redraw();
        }
    }

    /**
     * Returns the decorated tab text for the given part. By default, we attach
     * a star to indicate dirty tabs.
     * @return the decorated tab text for the given part
     */
    private String computeTabText() {
        boolean hasEnoughSpace = tabArea.hasEnoughSpace();
        if (partText != null) {
            boolean tabNameDirty = partText.startsWith(DIRTY_PREFIX);
            if ((tabNameDirty && partIsDirty() || !tabNameDirty && !partIsDirty())
                    && (tabNameReduced && !hasEnoughSpace || !tabNameReduced
                            && hasEnoughSpace)) {
                return partText;
            }
        }
        setPartText(computeTabText(hasEnoughSpace));
        altPartText = computeTabText(!hasEnoughSpace);
        tabNameReduced = !hasEnoughSpace;
        return partText;
    }

    private String computeTabText(boolean assumeEnoughSpace) {
        String result = part.getName();

        ThemeWrapper theme = tabArea.getCurrentTheme();

        // in most cases multiple dot-separated name like from plugin id's
        // shouldn't be shortened by us. This is a hack.
        if(!theme.getBoolean(ThemeConstants.SHOW_FILE_EXTENSIONS) &&
                result.indexOf('.') == result.lastIndexOf('.')){
            int pointIdx = result.lastIndexOf('.');
            if(pointIdx > 0){
                int slash = result.indexOf('/');
                if(slash > 0 && slash < pointIdx){
                    result = result.substring(slash, pointIdx);
                } else {
                    result = result.substring(0, pointIdx);
                }
            }
        }

        if (partIsDirty()) {
            result = DIRTY_PREFIX + result;
        }
        if(!assumeEnoughSpace && theme.getBoolean(ThemeConstants.USE_MAX_TAB_WIDTH)){
            int maxLength = theme.getInt(ThemeConstants.MAX_TAB_WIDTH);

            if(result.length() > maxLength + 2){
                StringBuffer sb = new StringBuffer(result);
                int diff = result.length() - maxLength;
                if(theme.getBoolean(ThemeConstants.CROP_IN_THE_MIDDLE)){
                    int mid = maxLength / 2;
                    sb.delete(mid, mid + diff);
                    sb.insert(mid, "..");
                } else {
                    sb.delete(maxLength, sb.length());
                    sb.append("..");
                }
                result = sb.toString();
            }
        }
        return result;
    }

    private boolean partIsDirty() {
        return partIsDirty;
    }



    public void dispose(){
        if (isDisposed ()) {
            return;
        }
        part = null;
        tabArea = null;
        super.dispose();
    }

    public boolean isHidden() {
        return isHidden || !isVisible();
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public boolean hasMenuFocus() {
        return hasMenuFocus;
    }

    public void setHasMenuFocus(boolean hasMenuFocus) {
        boolean oldFocus = this.hasMenuFocus;
        this.hasMenuFocus = hasMenuFocus;
        if(oldFocus != hasMenuFocus && !isSelected()){
            redraw();
        }
    }

    /**
     *
     */
    public void themeChanged() {
        // cause to recompute text
        setPartText(null);
        ThemeWrapper theme = tabArea.getCurrentTheme();
        Display display = tabArea.getDisplay();

        colorTabNoFocus = theme.getColor(ThemeConstants.TAB_COLOR_NOFOCUS);
        colorTabFocus = theme.getColor(ThemeConstants.TAB_COLOR_FOCUS);
        colorTextFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_FOCUS);
        colorTextNoFocus = theme.getColor(ThemeConstants.TITLE_TEXT_COLOR_NOFOCUS);
        colorTabTextFocus = theme.getColor(ThemeConstants.TAB_TEXT_COLOR_FOCUS);
        colorTabTextNoFocus = theme.getColor(ThemeConstants.TAB_TEXT_COLOR_NOFOCUS);
        colorDirtyTab = theme.getColor(ThemeConstants.TAB_DIRTY_TEXT_COLOR);
        colorHighlight = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        colorDark = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
        colorShadow = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        xPad = theme.getInt(ThemeConstants.TAB_PADDING_X);
        yPad = theme.getInt(ThemeConstants.TAB_PADDING_Y);

        if (isView) {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.VIEW_TAB_POSITION);
        } else {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.EDITOR_TAB_POSITION);
        }
        if(isView){
            isShowIcon = theme.getBoolean(ThemeConstants.SHOW_VIEW_ICON);
            hideViewTitle = theme.getBoolean(ThemeConstants.HIDE_VIEW_TITLE);
            if(hideViewTitle && !isShowIcon){
                isShowIcon = true;
            }
        } else {
            isShowIcon = theme.getBoolean(ThemeConstants.SHOW_EDITOR_ICON);
        }
        heightWithImage = 0;
        heightWithoutImage = 0;
    }

    private void setPartText(String newText) {
        maxSize = null;
        minSize = null;
        partText = newText;
    }
}
