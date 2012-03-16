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
package org.eclipse.emf.emfstore.client.model.impl;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.model.Configuration;
import org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver;
import org.eclipse.emf.emfstore.client.model.changeTracking.commands.EMFStoreCommandStack;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.EmptyRemovalsFilter;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.FilterStack;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.NotificationFilter;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.TouchFilter;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.TransientFilter;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.impl.IdEObjectCollectionImpl;
import org.eclipse.emf.emfstore.common.model.util.EObjectChangeNotifier;
import org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;

/**
 * The state persister is responsible for serializing the state of an {@link IdEObjectCollection}.
 * 
 * @author koegel
 * @author emueller
 * 
 */
public class StatePersister implements CommandObserver, IdEObjectCollectionChangeObserver {

	/**
	 * Set containing all dirty resources.
	 */
	private DirtyResourceSet dirtyResourceSet;

	/**
	 * Indicates whether a command is running.
	 */
	private boolean commandIsRunning;

	/**
	 * Indicates whether a resource may be split when a model element has been
	 * added.
	 */
	private boolean splitResource;
	private EMFStoreCommandStack commandStack;
	private FilterStack filterStack;

	private static Resource currentResource;

	/**
	 * Constructor.
	 * 
	 * @param changeNotifier
	 *            the {@link EObjectChangeNotifier} that is used to trigger the state persister
	 *            upon changes
	 * @param commandStack
	 *            an instance of an {@link EMFStoreCommandStack}
	 * @param collection
	 *            the collection that should be persisted
	 */
	public StatePersister(EObjectChangeNotifier changeNotifier, EMFStoreCommandStack commandStack,
		IdEObjectCollectionImpl collection) {
		this.commandStack = commandStack;
		this.commandStack.addCommandStackObserver(this);
		this.dirtyResourceSet = new DirtyResourceSet(collection);
		filterStack = new FilterStack(new NotificationFilter[] { new TouchFilter(), new TransientFilter(),
			new EmptyRemovalsFilter() });
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandStarted(org.eclipse.emf.common.command.Command)
	 */
	public void commandStarted(Command command) {
		commandIsRunning = true;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandCompleted(org.eclipse.emf.common.command.Command)
	 */
	public void commandCompleted(Command command) {
		commandIsRunning = false;
		saveDirtyResources();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandFailed(org.eclipse.emf.common.command.Command,
	 *      java.lang.Exception)
	 */
	public void commandFailed(Command command, Exception exception) {
		commandIsRunning = false;
	}

	private void cleanResources(EObject deletedElement) {
		Resource resource = deletedElement.eResource();
		if (resource != null) {
			resource.getContents().remove(deletedElement);
			dirtyResourceSet.addDirtyResource(resource);
		}
		for (EObject child : ModelUtil.getAllContainedModelElements(deletedElement, false)) {
			Resource childResource = child.eResource();
			if (childResource != null) {
				childResource.getContents().remove(child);
				dirtyResourceSet.addDirtyResource(childResource);
			}
		}
	}

	/**
	 * Adds the given model element's resource to the set of dirty resources.
	 * 
	 * @param modelElement
	 *            the model element
	 */
	private void addToDirtyResources(EObject modelElement) {
		Resource resource = modelElement.eResource();

		if (resource != null) {
			dirtyResourceSet.addDirtyResource(resource);
		}
	}

	/**
	 * Save all dirty resources to disk now if autosave is active.
	 */
	public void saveDirtyResources() {
		if (Configuration.isAutoSaveEnabled()) {
			dirtyResourceSet.save();
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#notify(org.eclipse.emf.common.notify.Notification,
	 *      org.eclipse.emf.emfstore.common.model.IdEObjectCollection, org.eclipse.emf.ecore.EObject)
	 */
	public void notify(Notification notification, IdEObjectCollection rootEObject, EObject modelElement) {
		// filter unwanted notifications that did not change anything in the
		// state
		if (filterStack.check(new NotificationInfo(notification))) {
			return;
		}

		addToDirtyResources(modelElement);

		if (!commandIsRunning) {
			saveDirtyResources();
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#modelElementAdded(org.eclipse.emf.emfstore.common.model.IdEObjectCollection,
	 *      org.eclipse.emf.ecore.EObject)
	 */
	public void modelElementAdded(IdEObjectCollection rootEObject, EObject modelElement) {
		XMIResource oldResource = (XMIResource) modelElement.eResource();

		// do not split if splitting disabled or the element is a map entry
		if (oldResource != null && Configuration.isResourceSplittingEnabled()
			&& !(modelElement instanceof BasicEMap.Entry)) {
			addToNewResourceIfRequired(modelElement, oldResource);
		}

		addToDirtyResources(modelElement);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#modelElementRemoved(org.eclipse.emf.emfstore.common.model.IdEObjectCollection,
	 *      org.eclipse.emf.ecore.EObject)
	 */
	public void modelElementRemoved(IdEObjectCollection rootEObject, EObject modelElement) {
		cleanResources(modelElement);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#collectionDeleted(org.eclipse.emf.emfstore.common.model.IdEObjectCollection)
	 */
	public void collectionDeleted(IdEObjectCollection collection) {
		if (commandStack != null) {
			commandStack.removeCommandStackObserver(this);
		}
	}

	private void addToNewResourceIfRequired(final EObject modelElement, XMIResource oldResource) {

		if (currentResource == null || currentResource.getURI() == null) {
			currentResource = oldResource;
		}
		URI oldUri = currentResource.getURI();
		String oldFileName = oldUri.toFileString();

		if (!oldUri.isFile()) {
			throw new IllegalStateException("Project contains ModelElements that are not part of a file resource.");
		}

		File oldFile = new File(oldFileName);
		if (oldFile.length() > Configuration.getMaxResourceFileSizeOnExpand()) {

			String newFileName;
			try {
				newFileName = File.createTempFile("frag", Configuration.getProjectFragmentFileExtension(),
					new File(oldFile.getParent())).getAbsolutePath();
			} catch (IOException e) {
				throw new IllegalStateException("File fragment \"" + "\" already exists - ProjectSpace corrupted.\n"
					+ "Cause: " + e.getMessage());
			}

			URI fileURI = URI.createFileURI(newFileName);
			XMIResource newResource = (XMIResource) currentResource.getResourceSet().createResource(fileURI);

			newResource.getContents().add(modelElement);
			currentResource = newResource;
		}
	}

	/**
	 * Sets whether a resource split may occur when a model element is added.
	 * 
	 * @param splitResource
	 *            whether resource splitting should occur
	 */
	public void setSplitResource(boolean splitResource) {
		this.splitResource = splitResource;
	}

	/**
	 * Determines whether resource splitting is enabled.
	 * 
	 * @return true, if resource splitting may occur
	 */
	public boolean isSplitResource() {
		return splitResource;
	}

}
