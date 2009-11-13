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
package de.loskutov.eclipseskins.ui.buttons;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ui.TabArea;

/**
 * Implements a navigation button (left/right arrow)
 *
 * @author wmitsuda
 * @author Andrei
 */
public class NavigatorButton extends AbstractButton {

    protected boolean isLeftArrow;

    protected TabArea tabArea;

    protected MoveJob moveJob;

    protected Image image;
    protected Image imageDisabled;
    int height;
    int width;
    Point size;

    public NavigatorButton(Composite parent, int style, boolean isLeftArrow,
            TabArea tabArea) {
        super(parent, style, false);
        this.isLeftArrow = isLeftArrow;
        this.tabArea = tabArea;
        moveJob = new MoveJob("Move TabArea");
        String iconKey = isLeftArrow? "icon.moveleft" : "icon.moveright";
        image = PresentationPlugin.getImage(PresentationPlugin.getResourceString(iconKey));
        imageDisabled = PresentationPlugin.getImage(PresentationPlugin
                .getResourceString(iconKey + ".disabled"));

        height = image.getImageData().height;
        width = image.getImageData().width;

        size = new Point(width + 5, height + 5);
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                if (!mouseDown) {
                    return;
                }
                if (mouseOver) {
                    moveJob.start();
                } else {
                    moveJob.stop();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                moveJob.start();
            }
            public void mouseUp(MouseEvent e) {
                moveJob.stop();
            }
        });
        if(isLeftArrow){
            setToolTipText("Scroll tab list right");
        } else {
            setToolTipText("Scroll tab list left");
        }
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return size;
    }

    protected void paintControl(PaintEvent e, int gap, Rectangle clientArea) {
        // Draw arrow
        GC gc = e.gc;
        int xPad = (clientArea.width - width) / 2;
        int yPad = (clientArea.height - height) / 2;
        if(xPad < 0){
            xPad = 0;
        }
        if(yPad < 0){
            yPad = 0;
        }
        if(selectable) {
            gc.drawImage(image, clientArea.x + xPad, clientArea.y + yPad);
        } else {
            gc.drawImage(imageDisabled, clientArea.x + xPad, clientArea.y + yPad);
        }
    }


    public void dispose() {
        moveJob = null;
        tabArea = null;
        image = null;
        imageDisabled = null;
        super.dispose();
    }

    private class MoveJob extends Job {
        private Runnable moveAction;
        private boolean running;
        public MoveJob(String name) {
            super(name);
            setSystem(true);
            setPriority(Job.INTERACTIVE);
            moveAction = new Runnable() {
                public void run() {
                    if (isLeftArrow) {
                        tabArea.moveRight();
                    } else {
                        tabArea.moveLeft();
                    }
                }
            };
        }

        protected IStatus run(IProgressMonitor monitor) {
            while (!monitor.isCanceled() && !isDisposed()) {
                getDisplay().syncExec(moveAction);
            }
            monitor.done();
            return Status.CANCEL_STATUS;
        }

        public void start() {
            if (!running) {
                running = true;
                schedule();
            }
        }

        public void stop() {
            if (running) {
                running = false;
                cancel();
            }
        }
    }

}
