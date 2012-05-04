/*******************************************************************************
 * Copyright (c) 2012 EclipseSource Muenchen GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Max Hohenegger (bug 371196)
 ******************************************************************************/

package org.eclipse.emf.emfstore.client.ui.controller;

import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.exceptions.LoginCanceledException;
import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
import org.eclipse.emf.emfstore.client.ui.handlers.AbstractEMFStoreUIController;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UIShareProjectController extends AbstractEMFStoreUIController {

	public UIShareProjectController(Shell shell) {
		super(shell);
	}

	public void share(ProjectSpace projectSpace) {
		ProgressMonitorDialog progressDialog = openProgress();
		try {
			((ProjectSpaceImpl) projectSpace).shareProject(null,
					progressDialog.getProgressMonitor());
			MessageDialog.openInformation(getShell(), "Share completed",
					"The project was successfully shared.");
		} catch (LoginCanceledException e) {
			// fail silently
		} catch (EmfStoreException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Error", e.getMessage());
		} finally {
			closeProgress();
		}
	}
}
