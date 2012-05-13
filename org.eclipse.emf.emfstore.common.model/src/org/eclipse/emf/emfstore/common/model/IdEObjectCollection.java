/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.common.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * A collection of {@link EObject}s where each one can be identified via a {@link ModelElementId}. {@link EObject}s can
 * be added and deleted and checked
 * whether they are part of the collection.
 * 
 * @author emueller
 * 
 */
public interface IdEObjectCollection extends EObject {

	/**
	 * Adds the given model element to the collection.
	 * 
	 * @param modelElement
	 *            the model element that should be added to the collection
	 */
	void addModelElement(EObject modelElement);

	/**
	 * Adds the given model element to the collection. An additional map may be
	 * passed in, in order to assign the model element and any of its children
	 * already determined {@link ModelElementId}s.
	 * 
	 * @param modelElement
	 *            the model element that should get added to the collection
	 * @param modelElementToIdMap
	 *            A map containing {@link ModelElementId}s for the model element
	 *            and its children
	 */
	void addModelElement(EObject modelElement, Map<EObject, ModelElementId> modelElementToIdMap);

	/**
	 * Checks whether a given model element is contained in the collection.
	 * 
	 * @param modelElement
	 *            the model element to check for, whether it is contained in the
	 *            collection
	 * @return true, if the model element is contained in the collection, false
	 *         otherwise
	 */
	boolean containsInstance(EObject modelElement);

	/**
	 * Returns all directly contained model element of the collection, i.e. a
	 * hierarchical representation of the model elements.
	 * 
	 * @return a collection of directly contained model elements
	 */
	Collection<EObject> getModelElements();

	/**
	 * Checks whether the {@link EObject} with the given {@link ModelElementId} is contained in the collection.
	 * 
	 * @param eObjectId
	 *            the {@link ModelElementId} of the {@link EObject}, which
	 *            should get checked, whether it is contained in the collection
	 * @return true, if the {@link EObject} with the {@link ModelElementId} in
	 *         question is contained in the collection
	 */
	boolean contains(ModelElementId eObjectId);

	/**
	 * Retrieve the {@link ModelElementId} of the given model element.
	 * 
	 * @param modelElement
	 *            the model element
	 * @return the {@link ModelElementId} of the given model element
	 */
	ModelElementId getModelElementId(EObject modelElement);

	/**
	 * Returns the model element with the given {@link ModelElementId}.
	 * 
	 * @param modelElementId
	 *            the {@link ModelElementId} of the model element, that should
	 *            get retrieved
	 * @return the model element that has the given {@link ModelElementId} assigned
	 */
	EObject getModelElement(ModelElementId modelElementId);

	/**
	 * Deletes the given model element from the collection.
	 * 
	 * @param modelElement
	 *            the model element that should get deleted
	 */
	void deleteModelElement(EObject modelElement);

	/**
	 * Returns a flat representation of all model elements in the collection.
	 * 
	 * @return a set of all model elements contained in the collection
	 */
	Set<EObject> getAllModelElements();

	/**
	 * Initializes the ID caches of the collection, i.e. the collection will
	 * call {@link IdEObjectCollection#getModelElements()} and for each model
	 * element the {@link ModelElementId} is fetched via {@link IdEObjectCollection#getModelElementId(EObject)}. Then a
	 * mapping
	 * between the model element and its {@link ModelElementId} is created
	 * within the cache.
	 */
	void initCaches();

	/**
	 * Initializes the ID caches of the project with the given mappings.
	 * 
	 * @param eObjectToIdMap
	 *            a mapping from EObjects to IDs
	 * @param idToEObjectMap
	 *            the reverse mapping of <code>eObjectToIdMap</code>
	 */
	void initCaches(Map<EObject, String> eObjectToIdMap, Map<String, EObject> idToEObjectMap);

	/**
	 * Retrieve a list of all model elements of a certain type in the
	 * collection.
	 * 
	 * @param <T>
	 *            a sub-type of model element
	 * @param modelElementClass
	 *            the {@link EClass}
	 * @param list
	 *            a list of model elements, can be empty, but must be of the
	 *            same type as the <code>modelElementClass</code> indicates.
	 * @param includeSubclasses
	 *            whether to also include all subclasses of the given {@link EClass} in the list
	 * @return a list of model elements of the given type
	 */
	<T extends EObject> EList<T> getAllModelElementsbyClass(EClass modelElementClass, EList<T> list,
		Boolean includeSubclasses);

	/**
	 * Retrieve a list of all model elements of a certain type in the
	 * collection.
	 * 
	 * @param <T>
	 *            a sub-type of model element
	 * @param modelElementClass
	 *            the {@link EClass}
	 * @param list
	 *            a list of model elements, can be empty, but must be of the
	 *            same type as the <code>modelElementClass</code> indicates.
	 * @return a list of model elements of the given type
	 */
	<T extends EObject> EList<T> getAllModelElementsbyClass(EClass modelElementClass, EList<T> list);

	/**
	 * Retrieve a list of model elements of a certain type in the collection
	 * that are directly contained in the collection.
	 * 
	 * @param <T>
	 *            a sub-type of model element
	 * @param modelElementClass
	 *            the {@link EClass}
	 * @param list
	 *            a list of model elements, can be empty, but must be of the
	 *            same type as the <code>modelElementClass</code> indicates.
	 * @return a list of model elements of the given type
	 */
	<T extends EObject> EList<T> getModelElementsByClass(EClass modelElementClass, EList<T> list);

	/**
	 * Assigns all EObjects that are contained in the collection and as keys in
	 * the given map the respective {@link ModelElementId}s.<br/>
	 * <br/>
	 * If the EObjects in the map are not yet contained in the collection the
	 * IDs will be cached until the objects are eventually added.
	 * 
	 * @param eObjectToIdMap
	 *            a map containing the model elements and the IDs
	 */
	void preAssignModelElementIds(Map<EObject, ModelElementId> eObjectToIdMap);
}
