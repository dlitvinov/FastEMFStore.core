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
package org.eclipse.emf.emfstore.client.model.observers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.common.observer.IObserver;

/**
 * Enables the modification of attributes of newly created model elements.<br>
 * <b>IMPORTANT</b>: do not modify any references. This interfaces is only
 * intended to modify attributes of model elements like creation date/creator
 * and the like.
 * 
 * @author emueller
 */
public interface PostCreationObserver extends IObserver {

	/**
	 * Called when a new model element has been created. Use this method to to
	 * modify attributes of a newly created model element.
	 * 
	 * @param modelElement
	 *            the model element that has been created
	 */
	void onCreation(EObject modelElement);
}
