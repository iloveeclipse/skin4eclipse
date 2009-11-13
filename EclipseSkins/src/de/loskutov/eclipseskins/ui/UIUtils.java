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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ToolItem;

import de.loskutov.eclipseskins.PresentationPlugin;

/**
 * Utilities...
 *
 * @author wmitsuda
 * @author Andrei
 */
public class UIUtils {

    /**
     * used to get icon from props file
     */
    public static final String ICON = ".icon";
    /**
     * used to get icon from props file
     */
    public static final String ICON_SELECTED = ".icon.selected";
    /**
     * used to get icon from props file
     */
    public static final String ICON_DISABLED = ".icon.disabled";

    /**
     * used to get tooltip from props file
     */
    public static final String TOOLTIP = ".tooltip";
    /**
     * used to get tooltip from props file
     */
    public static final String TOOLTIP_SELECTED = ".tooltip.selected";
    /**
     * used to get text from props file
     */
    public static final String TEXT = ".text";
    /**
     * used to get action from props file
     */
    public static final String ACTION = "action.";
    /**
     * used to get button from props file
     */
    public static final String BUTTON = "button.";



    public static void drawRestoreIcon(PaintEvent e, int gap,
            Rectangle clientArea) {
        int width4 = (clientArea.width ) / 4;
        int width3 = 2 * width4 / 3;
        int height4 = (clientArea.height - 2) / 4;
        int height3 = 2 * height4 / 3;
        GC gc = e.gc;
        gc.fillRectangle(clientArea.x + width4 + width3 + 1 + gap,
                clientArea.y + height4 + 1 + gap, 2 * width3, 2);
        gc.drawRectangle(clientArea.x + width4 + width3 + 1 + gap,
                clientArea.y + height4 + 1 + gap, 2 * width3, 2 * height3);

        gc.fillRectangle(clientArea.x + width4 + gap, clientArea.y + height4
                + 2 * height3 + gap, 2 * width3, 2);
        gc.drawRectangle(clientArea.x + width4 + gap, clientArea.y + height4
                + 2 * height3 + gap, 2 * width3, 2 * height3);
    }

    public static void initButton(final ToolItem button, String id){
        String prefix = BUTTON + id;
        final String tooltip = PresentationPlugin.getResourceString(prefix + TOOLTIP);
        button.setToolTipText(tooltip);

        String imageStr = PresentationPlugin.getResourceString(prefix + ICON);
        final Image image = PresentationPlugin.getImage(imageStr);
        button.setImage(image);
        imageStr = PresentationPlugin.getResourceString(prefix + ICON_SELECTED);
        final Image imageSelected = PresentationPlugin.getImage(imageStr);
        if(imageSelected != image) {
            String tooltipKey = prefix + TOOLTIP_SELECTED;
            final String tooltip2 = PresentationPlugin.getResourceString(tooltipKey);
            final boolean existTooltip2 = !tooltipKey.equals(tooltip2) && tooltip2 != tooltip;
            if(button.getSelection()){
                button.setImage(imageSelected);
                if(existTooltip2){
                    button.setToolTipText(tooltip2);
                }
            }
            button.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected(SelectionEvent e) {
                    boolean selection = button.getSelection();
                    button.setImage(selection? imageSelected : image);
                    if(existTooltip2) {
                        button.setToolTipText(selection? tooltip2 : tooltip);
                    }
                }
            });
        }
    }


    public static void initAction(IAction action){
        String id = action.getId();
        String textKey = ACTION + id + TEXT;
        String text = PresentationPlugin.getResourceString(textKey);
        if(text != null && !textKey.equals(text)) {
            action.setText(text);
        }
        String tooltipKey = ACTION + id + TOOLTIP;
        String tooltip = PresentationPlugin.getResourceString(tooltipKey);
        if(tooltip != null && !tooltipKey.equals(tooltip)) {
            action.setToolTipText(tooltip);
        }
        String iconKey = ACTION + id + ICON;
        String icon = PresentationPlugin.getResourceString(iconKey);
        ImageDescriptor imageDescriptor = null;
        if(icon != null && !icon.equals(iconKey) && icon.length() > 0){
            imageDescriptor = PresentationPlugin.getImageDescriptor(icon);
            if(imageDescriptor != null) {
                action.setImageDescriptor(imageDescriptor);
            }
        }
        iconKey = ACTION + id + ICON_DISABLED;
        icon = PresentationPlugin.getResourceString(iconKey);
        if(icon != null && !icon.equals(iconKey) && icon.length() > 0){
            imageDescriptor = PresentationPlugin.getImageDescriptor(icon);
            if(imageDescriptor != null) {
                action.setDisabledImageDescriptor(imageDescriptor);
            }
        }
    }

}
