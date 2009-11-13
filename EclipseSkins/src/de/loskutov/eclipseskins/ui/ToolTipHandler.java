/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.presentations.IPresentablePart;

import de.loskutov.eclipseskins.presentation.VSStackPresentation.ClosedPart;

/**
 * Emulated tooltip handler
 * Notice that we could display anything in a tooltip besides text and images.
 */
public class ToolTipHandler {
    protected Shell  tipShell;
    protected Label  tipLabelImage, tipLabelText;
    protected Point  tipPosition; // the position being hovered over
    protected Widget tipWidget; // widget this tooltip is hovering over

    /**
     * Creates a new tooltip handler
     *
     * @param parent the parent Shell
     */
    public ToolTipHandler(Shell parent) {
        final Display display = parent.getDisplay();

        tipShell = new Shell(parent, SWT.ON_TOP);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 2;
        gridLayout.marginHeight = 2;
        tipShell.setLayout(gridLayout);

        tipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        tipLabelImage = new Label(tipShell, SWT.NONE);
        tipLabelImage.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        tipLabelImage.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        tipLabelImage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |
            GridData.VERTICAL_ALIGN_CENTER));

        tipLabelText = new Label(tipShell, SWT.NONE);
        tipLabelText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        tipLabelText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        tipLabelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |
            GridData.VERTICAL_ALIGN_CENTER));
    }

    protected void hideTooltip(){
        if (tipShell.isVisible()) {
            tipShell.setVisible(false);
            tipLabelImage.setImage(null);
        }
    }

    /**
     * Enables customized hover help for a specified control
     *
     * @control the control on which to enable hoverhelp
     */
    public void activateHoverHelp(final Control control) {
        /*
         * Get out of the way if we attempt to activate the control underneath the tooltip
         */
        control.addMouseListener(new MouseAdapter () {
            public void mouseDown (MouseEvent e) {
                hideTooltip();
            }
        });

        /*
         * Trap hover events to pop-up tooltip
         */
        control.addMouseTrackListener(new MouseTrackAdapter () {
            public void mouseExit(MouseEvent e) {
                hideTooltip();
            }

            public void mouseHover (MouseEvent event) {
                Point pt = new Point (event.x, event.y);

                Widget widget = event.widget;
                if (widget instanceof Table) {
                    Table w = (Table) widget;
                    widget = w.getItem (pt);
                }

                if (widget == null) {
                    hideTooltip();
                    tipWidget = null;
                    return;
                }

                if (widget == tipWidget) {
                    return;
                }

                tipWidget = widget;
                tipPosition = control.toDisplay(pt);
                Object data = widget.getData();
                if (data instanceof IPresentablePart) {
                    IPresentablePart part = (IPresentablePart) data;
                    String text = part.getTitleToolTip();
                    if(text == null || text.length() == 0){
                        text = part.getTitle();
                    }
                    Image image = part.getTitleImage();
                    tipLabelText.setText(text != null ? text : "");
                    tipLabelImage.setImage(image); // accepts null
                    tipShell.pack();
                    setHoverLocation(tipShell, tipPosition);
                    tipShell.setVisible(true);
                } else if(data instanceof ClosedPart){
                    ClosedPart part = (ClosedPart) data;
                    String text = part.tooltip;
                    if(text == null || text.length() == 0){
                        text = part.name;
                    }
                    Image image = part.image;
                    tipLabelText.setText(text != null ? text : "");
                    tipLabelImage.setImage(image); // accepts null
                    tipShell.pack();
                    setHoverLocation(tipShell, tipPosition);
                    tipShell.setVisible(true);
                }
            }
        });

    }

    /**
     * Sets the location for a hovering shell
     * @param shell the object that is to hover
     * @param position the position of a widget to hover over
     */
    protected void setHoverLocation(Shell shell, Point position) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = shell.getBounds();
        shellBounds.x = Math.max(Math.min(position.x + 8,
                displayBounds.width - shellBounds.width), 0);
        shellBounds.y = Math.max(Math.min(position.y + 16,
                displayBounds.height - shellBounds.height), 0);
        shell.setBounds(shellBounds);
    }
}
