/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package de.loskutov.eclipseskins;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * @author Andrei
 */
public class PresentationPlugin extends AbstractUIPlugin {
    // The shared instance.
    private static PresentationPlugin plugin;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    public static boolean DEBUG;

    /**
     * The constructor.
     */
    public PresentationPlugin() {
        super();
        if(plugin != null) {
            throw new IllegalStateException("Presentation plugin is singleton!");
        }
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        DEBUG = isDebugging();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        resourceBundle = null;
    }

    /**
     * Returns the shared instance.
     */
    public static PresentationPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = PresentationPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        try {
            if (resourceBundle == null) {
                resourceBundle = ResourceBundle.getBundle(getClass().getName());
            }
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        if (path == null) {
            return null;
        }
        ImageRegistry imageRegistry = getDefault().getImageRegistry();
        ImageDescriptor imageDescriptor = imageRegistry.getDescriptor(path);
        if (imageDescriptor == null) {
            imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                    "de.loskutov.eclipseskins.extvs", path);
            imageRegistry.put(path, imageDescriptor);
        }
        return imageDescriptor;
    }

    public static Image getImage(String path) {
        if (path == null) {
            return null;
        }
        // prefetch image, if not jet there
        ImageDescriptor imageDescriptor = getImageDescriptor(path);
        if (imageDescriptor != null) {
            return getDefault().getImageRegistry().get(path);
        }
        log("Couldn't load image: " + path, null);
        return null;
    }

    public static void log(String message, Throwable error) {
        if(message == null && error != null){
            message = error.getMessage() != null? error.getMessage() : "Strange error :(";
        }
        getDefault().getLog().log(
                new Status(IStatus.ERROR, "VSPresentation", 0, message, error));
    }

    public static void errorDialog(String message, Throwable error) {
        Shell shell = getShell();
        if (message == null) {
            message = "Exception occured:";
        }
        message = message + " " + error.getMessage();

        getDefault().getLog().log(
                new Status(IStatus.ERROR, getId(), IStatus.OK, message, error));

        MessageDialog.openError(shell, "Extended VS Presentation", message);
    }

    public static Shell getShell() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    public static String getId(){
        return getDefault().getBundle().getSymbolicName();
    }
}
