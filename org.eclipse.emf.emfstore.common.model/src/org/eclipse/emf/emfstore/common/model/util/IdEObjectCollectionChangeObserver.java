/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.common.model.util;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.observer.IObserver;

/**
 * An observer interface for tracking changes upon an {@link IdEObjectCollection}.
 * 
 * @author emueller
 * 
 */
public interface IdEObjectCollectionChangeObserver extends IObserver {

	/**
	 * A notification on a model element of the collection occurred.
	 * See {@link Notification} documentation for details on the notifications.
	 * This method will be called even if the given notification results from an add
	 * or remove of a model element of the project.
	 * 
	 * @param notification
	 *            the notification
	 * @param collection
	 *            the collection holding the <code>modelElement</code>
	 * @param modelElement
	 *            the model element the notification originates from
	 */
	void notify(Notification notification, IdEObjectCollection collection, EObject modelElement);

	/**
	 * The given model element was added to the collection.
	 * This means it is now in the collection's containment tree.
	 * 
	 * @param collection
	 *            the collection
	 * @param modelElement
	 *            the modelElement
	 */
	void modelElementAdded(IdEObjectCollection collection, EObject modelElement);

	/**
	 * The given model element and its siblings have been removed from the
	 * collection. Note that you will NOT receive a separate notification for each
	 * sibling.
	 * 
	 * @param collection
	 *            the collection
	 * @param modelElement
	 *            the model element
	 */
	void modelElementRemoved(IdEObjectCollection collection, EObject modelElement);

	/**
	 * If the {@link IdEObjectCollection} is deleted.
	 * 
	 * @param collection the {@link IdEObjectCollection} that has been deleted
	 */
	void collectionDeleted(IdEObjectCollection collection);
}
