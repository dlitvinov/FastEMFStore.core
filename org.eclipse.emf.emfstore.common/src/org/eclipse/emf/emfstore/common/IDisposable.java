/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * Technische Universitaet Muenchen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.common;

/**
 * Disposable interface for marking classes that need to perform clean up tasks when 
 * de-initialized.
 * 
 * @author emueller
 */
public interface IDisposable {

	/**
	 * Marker method for classes that need to perform clean-up tasks.
	 */
	void dispose();
}
