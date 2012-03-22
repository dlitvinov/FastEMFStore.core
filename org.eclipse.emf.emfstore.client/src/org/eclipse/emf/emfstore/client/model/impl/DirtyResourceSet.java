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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
import org.eclipse.emf.emfstore.common.EMFStoreResource;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.ModelElementId;
import org.eclipse.emf.emfstore.common.model.impl.IdEObjectCollectionImpl;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;

/**
 * Tracks a set of dirty resources for saving.
 * 
 * @author koegel
 */
public class DirtyResourceSet {

	private Set<Resource> resources;
	private final IdEObjectCollectionImpl collection;
	private boolean resourcesPending;

	/**
	 * Constructor.
	 * 
	 * @param collection
	 *            the {@link IdEObjectCollection} that is supposed to contain the model elements
	 *            that will be saved by the dirty resource set
	 */
	public DirtyResourceSet(IdEObjectCollectionImpl collection) {
		this.collection = collection;
		resources = new HashSet<Resource>();
	}

	/**
	 * Add a new dirty resource.
	 * 
	 * @param resource
	 *            the resource
	 */
	public void addDirtyResource(Resource resource) {
		resources.add(resource);
	}

	/**
	 * Save all dirty resources in this set.
	 */
	public void save() {
		Set<Resource> resourcesToRemove = new HashSet<Resource>();

		for (Resource resource : resources) {

			if (resource.getURI() == null || resource.getURI().toString().equals("")) {
				continue;
			}

			if (resource instanceof EMFStoreResource) {
				((EMFStoreResource) resource).setIdToEObjectMap(collection.getIdToEObjectMap(),
					collection.getEObjectToIdMap());
			} else {
				Set<EObject> modelElements = ModelUtil.getAllContainedModelElements(resource, false, false);

				for (EObject modelElement : modelElements) {
					setModelElementIdOnResource((XMIResource) resource, modelElement);
				}
			}

			try {
				resource.save(ModelUtil.getResourceSaveOptions());
				resourcesToRemove.add(resource);
			} catch (IOException e) {
				// ignore exception
			}
		}

		resources.removeAll(resourcesToRemove);

		if (resources.size() > 0 || resourcesPending) {
			String message = resources.size() + " unsaved resources remained in the dirty resource set!";
			WorkspaceUtil.logWarning(message, null);
			resourcesPending = (resources.size() != 0);
		} else {
			resourcesPending = false;
		}
	}

	private void setModelElementIdOnResource(XMIResource resource, EObject modelElement) {

		if (modelElement instanceof IdEObjectCollection) {
			return;
		}

		ModelElementId modelElementId = getIDForEObject(modelElement);

		String modelElementIdString = modelElementId.getId();
		resource.setID(modelElement, modelElementIdString);
	}

	private ModelElementId getIDForEObject(EObject modelElement) {
		ModelElementId modelElementId = collection.getModelElementId(modelElement);

		if (modelElementId == null) {
			modelElementId = collection.getDeletedModelElementId(modelElement);
		}

		if (modelElementId == null) {
			WorkspaceUtil.handleException(new IllegalStateException("No ID for model element" + modelElement));
		}

		return modelElementId;
	}

}
