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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.ThemeWrapper;
import de.loskutov.eclipseskins.ui.buttons.NavigatorButton;
import de.loskutov.eclipseskins.ui.buttons.ShowTabListButton;

/**
 * Area where the controls of a tab area are placed
 *
 * @author wmitsuda
 * @author Andrei
 */
public class TabAreaButtons extends Canvas implements PaintListener {

    protected NavigatorButton leftButton;

    protected NavigatorButton rightButton;

    protected ShowTabListButton showTabListButton;

    protected boolean showNavigator;

    protected ThemeWrapper currentTheme;
    protected final boolean isView;

    private boolean isVisible;
    private Point size;

    public TabAreaButtons(Composite parent, int style,
            final boolean isView, TabArea tabArea, ThemeWrapper currentTheme) {
        super(parent, style);
        this.isView = isView;
        tabArea.setTabAreaButtons(this);
        if(isView){
            showTabListButton = new ShowTabListButton(this, style, tabArea);
        }
        leftButton = new NavigatorButton(this, style, true, tabArea);
        rightButton = new NavigatorButton(this, style, false, tabArea);
        setCurrentTheme(currentTheme);
        addPaintListener(this);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        if(size != null){
            return size;
        }
        int width = 0;
        int height = 0;
        if (hHint == SWT.DEFAULT) {
            hHint = 16;
        }
        Point leftSize = leftButton.computeSize(SWT.DEFAULT, hHint, true);
        width += leftSize.x;
        Point rightSize = rightButton.computeSize(SWT.DEFAULT, hHint, true);
        width += rightSize.x;
        height = Math.max(leftSize.y, rightSize.y);
        if(isView){
            Point tabListBtnSize = showTabListButton.computeSize(SWT.DEFAULT, hHint, true);
            width += tabListBtnSize.x;
        }

        size = new Point(width + 1, height);
        return size;
    }

    public void setLeftEnabled(boolean enabled) {
        leftButton.setSelectable(enabled);
    }

    public void setRightEnabled(boolean enabled) {
        rightButton.setSelectable(enabled);
    }

    public void setShowNavigator(boolean showNavigator) {
        this.showNavigator = showNavigator;
        if(!showNavigator){
            if(isView) {
                showTabListButton.setVisible(false);
            }
            leftButton.setVisible(false);
            rightButton.setVisible(false);
        } else if(isVisible){
            // Draw buttons
            if(isView) {
                showTabListButton.setVisible(true);
            }
            leftButton.setVisible(true);
            rightButton.setVisible(true);
        }
    }

    protected ThemeWrapper getCurrentTheme(){
        return currentTheme;
    }

    public void redraw(){
        if(showNavigator){
            rightButton.redraw();
            leftButton.redraw();
            if(isView) {
                showTabListButton.redraw();
            }
        }
    }

    public void setCurrentTheme(ThemeWrapper theme){
        size = null;
        currentTheme = theme;
        rightButton.setCurrentTheme(theme);
        leftButton.setCurrentTheme(theme);
        if(isView) {
            showTabListButton.setCurrentTheme(theme);
        }
    }

    public void dispose(){
        if (isDisposed ()) {
            return;
        }
        leftButton.dispose();
        leftButton = null;
        rightButton.dispose();
        rightButton = null;
        if(isView){
            showTabListButton.dispose();
            showTabListButton = null;
        }
        currentTheme = null;
        super.dispose();
    }

    public void paintControl(PaintEvent e) {
        Rectangle clientArea = getClientArea();
        ThemeWrapper theme = getCurrentTheme();

        // Fill background
        GC gc = e.gc;
        gc.setClipping(clientArea);
        gc.setBackground(theme.getColor(ThemeConstants.TAB_COLOR_NOFOCUS));
        gc.fillRectangle(clientArea);

        // Draw separator line between tabs and content area
        int separatorHeight = 1;

        boolean topPosition;
        if (isView) {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.VIEW_TAB_POSITION);
        } else {
            topPosition = SWT.TOP == theme.getInt(ThemeConstants.EDITOR_TAB_POSITION);
        }

        if (topPosition) {
            gc.setForeground(e.display
                    .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
            if(isView){
                gc.drawLine(clientArea.x,
                        clientArea.y + clientArea.height - separatorHeight - 1,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - separatorHeight - 1);

                gc.setForeground(theme.getColor(ThemeConstants.TAB_COLOR_FOCUS));
                gc.drawLine(clientArea.x,
                        clientArea.y + clientArea.height - separatorHeight,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - separatorHeight);
            } else {
                gc.drawLine(clientArea.x,
                        clientArea.y + clientArea.height - separatorHeight,
                        clientArea.x + clientArea.width - 1,
                        clientArea.y + clientArea.height - separatorHeight);
            }
        } else {
            gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            gc.drawLine(clientArea.x, clientArea.y, clientArea.x
                    + clientArea.width - 1, clientArea.y);

            gc.setForeground(e.display
                    .getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            gc.drawLine(clientArea.x, clientArea.y + 1, clientArea.x
                    + clientArea.width - 1, clientArea.y + 1);
        }
        gc.setClipping((Rectangle)null);
    }

    public void setBounds(int x1, int y1, int width, int height) {
        super.setBounds(x1, y1, width, height);
        if(width == 0 || height == 0){
            return;
        }
        layoutButtons();
    }

    protected void layoutButtons() {
        Rectangle clientArea = getClientArea();
        if(clientArea.width == 0 || clientArea.height == 0){
            return;
        }
        // Draw buttons
        int x = clientArea.x + clientArea.width;
        ThemeWrapper theme = getCurrentTheme();
        int yPadx2 = 2 * theme.getInt(ThemeConstants.TAB_PADDING_Y);

        int y;
        if(isView){
            boolean topPosition = SWT.TOP == theme.getInt(ThemeConstants.VIEW_TAB_POSITION);
            Point tabListBtnSize = showTabListButton.computeSize(SWT.DEFAULT,
                    clientArea.height - yPadx2);
            x -= tabListBtnSize.x;
            y = clientArea.y + (clientArea.height - tabListBtnSize.y) / 2;
            if(topPosition){
                y -= 2;
            } else {
                y += 2;
            }
            showTabListButton.setBounds(x, y, tabListBtnSize.x, tabListBtnSize.y);
        }

        Point rightSize = rightButton.computeSize(SWT.DEFAULT,
                clientArea.height - yPadx2);
        x -= rightSize.x;
        y = clientArea.y + (clientArea.height - rightSize.y) / 2;
        rightButton.setBounds(x, y, rightSize.x, rightSize.y);

        Point leftSize = leftButton.computeSize(SWT.DEFAULT,
                clientArea.height - yPadx2);
        x -= leftSize.x;
        y = clientArea.y + (clientArea.height - leftSize.y) / 2;
        leftButton.setBounds(x, y, leftSize.x, leftSize.y);
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        super.setVisible(visible);
//        if(isVisible && showNavigator){
//            // Draw buttons
//            rightButton.setVisible(true);
//            leftButton.setVisible(true);
//            if(isView) {
//                showTabListButton.setVisible(true);
//            }
//        }
    }
}
