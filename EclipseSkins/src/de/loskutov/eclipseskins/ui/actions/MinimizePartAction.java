/*******************************************************************************
 * Copyright (c) 2005 Willian Mitsuda.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Willian Mitsuda - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.ui.actions;

import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Action which minimizes a part
 * 
 * @author wmitsuda
 */
public class MinimizePartAction extends AbstractPresentationAction {

	public MinimizePartAction(IStackPresentationSite site) {
		super(site);
		setText("Mi&nimize");
	}

	public void run() {
		site.setState(IStackPresentationSite.STATE_MINIMIZED);
	}

}
