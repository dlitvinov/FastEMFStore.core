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

package org.eclipse.emf.emfstore.common.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.NotifiableIdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.util.EObjectChangeNotifier;
import org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;

/**
 * A collection that is able of maintaining a list of {@link IdEObjectCollectionChangeObserver}.
 * 
 * @author koegel
 * @author naughton
 * @author emueller
 */
public abstract class NotifiableIdEObjectCollectionImpl extends IdEObjectCollectionImpl implements
	NotifiableIdEObjectCollection {

	// observer related attributes
	private boolean isNotifiying;
	private List<IdEObjectCollectionChangeObserver> observers;
	private Set<IdEObjectCollectionChangeObserver> exceptionThrowingObservers;
	private Set<IdEObjectCollectionChangeObserver> observersToRemove;
	private Set<IdEObjectCollectionChangeObserver> undetachableObservers;
	private Set<IdEObjectCollectionChangeObserver> observersToAttach;

	private EObjectChangeNotifier changeNotifier;

	/**
	 * Constructor.
	 */
	protected NotifiableIdEObjectCollectionImpl() {
		super();
		initObservers();
	}

	/**
	 * Constructor. Adds the contents of the given {@link XMIResource} as model
	 * elements to the collection. If the {@link XMIResource} also has XMI IDs
	 * assigned to the {@link EObject}s it contains, they will be used for
	 * creating the model element IDs within the project, if not, the model element IDs will get created on
	 * the fly.
	 * 
	 * An {@link EObjectChangeNotifier} is also attached to the given {@link XMIResource}.
	 * 
	 * @param xmiResource
	 *            a {@link XMIResource}
	 * @throws IOException
	 *             if the given {@link XMIResource} could not be loaded
	 */
	public NotifiableIdEObjectCollectionImpl(XMIResource xmiResource) throws IOException {
		super(xmiResource);
		initObservers();
		changeNotifier = new EObjectChangeNotifier(this, xmiResource);
	}

	/**
	 * Initializes all observers.
	 */
	private void initObservers() {
		observers = new ArrayList<IdEObjectCollectionChangeObserver>();
		observersToRemove = new HashSet<IdEObjectCollectionChangeObserver>();
		exceptionThrowingObservers = new HashSet<IdEObjectCollectionChangeObserver>();
		undetachableObservers = new HashSet<IdEObjectCollectionChangeObserver>();
		observersToAttach = new HashSet<IdEObjectCollectionChangeObserver>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.ProjectChangeObserver#modelElementAdded(org.eclipse.emf.emfstore.common.model.Project,
	 *      org.eclipse.emf.ecore.EObject)
	 */
	public void modelElementAdded(final IdEObjectCollection project, final EObject eObject) {
		addModelElementAndChildrenToCache(eObject);
		EObjectChangeObserverNotificationCommand command = new EObjectChangeObserverNotificationCommand() {
			public void run(IdEObjectCollectionChangeObserver projectChangeObserver) {
				projectChangeObserver.modelElementAdded(project, eObject);
			}
		};
		notifyIdEObjectCollectionChangeObservers(command);
	}

	/**
	 * Notifies all collection change observers.
	 * 
	 * @param command
	 *            the notification command
	 */
	protected synchronized void notifyIdEObjectCollectionChangeObservers(
		EObjectChangeObserverNotificationCommand command) {
		isNotifiying = true;
		for (IdEObjectCollectionChangeObserver changeObserver : this.observers) {
			try {
				command.run(changeObserver);
				// BEGIN SUPRESS CATCH EXCEPTION
			} catch (RuntimeException ex) {
				// END SUPRESS CATCH EXCEPTION
				if (exceptionThrowingObservers.contains(changeObserver)) {
					if (!undetachableObservers.contains(changeObserver)) {
						observersToRemove.add(changeObserver);
						ModelUtil.logException(
							"EObject Change Observer threw an exception again, it has been detached, UI may not update now: "
								+ changeObserver.getClass().getName(), ex);
					} else {
						ModelUtil.logException(
							"EObject Change Observer threw an exception again, but it will not be detached."
								+ changeObserver.getClass().getName(), ex);
					}
				} else {
					exceptionThrowingObservers.add(changeObserver);
					ModelUtil.logWarning("EObject Change Observer threw an exception: "
						+ changeObserver.getClass().getName(), ex);
				}
			}
		}
		isNotifiying = false;
		for (IdEObjectCollectionChangeObserver observer : this.observersToRemove) {
			removeIdEObjectCollectionChangeObserver(observer);
		}

		for (IdEObjectCollectionChangeObserver observer : this.observersToAttach) {
			addIdEObjectCollectionChangeObserver(observer);
		}

		this.observersToRemove.clear();
	}

	@Override
	public void initCaches() {
		super.initCaches();
		if (changeNotifier == null) {
			changeNotifier = new EObjectChangeNotifier(this, this);
		}
	};

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.Project#initCaches(java.util.Map, java.util.Map)
	 */
	@Override
	public void initCaches(Map<EObject, String> eObjectToIdMap, Map<String, EObject> idToEObjectMap) {
		super.initCaches(eObjectToIdMap, idToEObjectMap);
		if (changeNotifier == null) {
			changeNotifier = new EObjectChangeNotifier(this, this);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.ProjectChangeObserver#notify(org.eclipse.emf.common.notify.Notification,
	 *      org.eclipse.emf.emfstore.common.model.Project, org.eclipse.emf.ecore.EObject)
	 */
	public void notify(final Notification notification, final IdEObjectCollection project, final EObject modelElement) {
		EObjectChangeObserverNotificationCommand command = new EObjectChangeObserverNotificationCommand() {
			public void run(IdEObjectCollectionChangeObserver projectChangeObserver) {
				projectChangeObserver.notify(notification, project, modelElement);
			}
		};

		Resource resource = modelElement.eResource();

		if (resource != null && resource instanceof ResourceImpl) {
			ResourceImpl resourceImpl = (ResourceImpl) resource;
			if (resourceImpl.isLoading()) {
				return;
			}
		}

		notifyIdEObjectCollectionChangeObservers(command);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.Project#addIdEObjectCollectionChangeObserver(org.eclipse.emf.emfstore.common.model.util.ProjectChangeObserver)
	 */
	public synchronized void addIdEObjectCollectionChangeObserver(
		IdEObjectCollectionChangeObserver eObjectChangeObserver) {
		initCaches();

		if (isNotifiying) {
			observersToAttach.add(eObjectChangeObserver);
			return;
		}

		this.observers.add(eObjectChangeObserver);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.Project#removeIdEObjectCollectionChangeObserver(org.eclipse.emf.emfstore.common.model.util.ProjectChangeObserver)
	 */
	public synchronized void removeIdEObjectCollectionChangeObserver(
		IdEObjectCollectionChangeObserver projectChangeObserver) {
		if (isNotifiying) {
			observersToRemove.add(projectChangeObserver);
			return;
		}
		this.observers.remove(projectChangeObserver);
		exceptionThrowingObservers.remove(projectChangeObserver);
		undetachableObservers.remove(projectChangeObserver);
	}

	/**
	 * Make a project change observer undetachable.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void setUndetachable(IdEObjectCollectionChangeObserver observer) {
		undetachableObservers.add(observer);
	}

	/**
	 * Handle the removal of an element from the containment hierachy.
	 * 
	 * @param projectImpl
	 *            the project
	 * @param modelElement
	 *            the model element
	 */
	public void modelElementRemoved(final IdEObjectCollection projectImpl, final EObject modelElement) {
		removeModelElementAndChildrenFromCache(modelElement);
		EObjectChangeObserverNotificationCommand command = new EObjectChangeObserverNotificationCommand() {
			public void run(IdEObjectCollectionChangeObserver projectChangeObserver) {
				projectChangeObserver.modelElementRemoved(projectImpl, modelElement);
			}
		};
		notifyIdEObjectCollectionChangeObservers(command);
	}

	/**
	 * Copies the current collection.
	 * 
	 * @param <T>
	 *            the actual collection type to be copied
	 * 
	 * @return the copied collection
	 */
	@Override
	public <T extends IdEObjectCollection> T copy() {

		if (this.changeNotifier != null) {
			this.changeNotifier.disableNotifications(true);
		}

		T result = super.copy();

		if (changeNotifier != null) {
			changeNotifier.disableNotifications(false);
		}

		return result;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#collectionDeleted(org.eclipse.emf.emfstore.common.model.IdEObjectCollection)
	 */
	public void collectionDeleted(IdEObjectCollection collection) {

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.NotifiableIdEObjectCollection#getChangeNotifier()
	 */
	public EObjectChangeNotifier getChangeNotifier() {
		return changeNotifier;
	}
}
