/*******************************************************************************
 * Copyright (c) 2012 EclipseSource Muenchen GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Max Hohenegger (initial implementation)
 ******************************************************************************/
package org.eclipse.emf.emfstore.client.model.exceptions;

import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;

/**
 * If the login fails, this exception will be thrown. This is usually the case if the user
 * cancels the share dialog.
 * 
 * @author Max Hohenegger
 */
@SuppressWarnings("serial")
public class LoginCanceledException extends EmfStoreException {

	/**
	 * Constructor.
	 * 
	 * @param message reason why this exception will be thrown
	 */
	public LoginCanceledException(String message) {
		super(message);
	}

}
