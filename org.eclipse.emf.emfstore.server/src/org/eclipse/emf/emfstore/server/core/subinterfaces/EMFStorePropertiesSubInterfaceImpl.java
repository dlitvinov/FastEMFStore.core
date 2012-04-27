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

package org.eclipse.emf.emfstore.server.core.subinterfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
import org.eclipse.emf.emfstore.server.core.AbstractEmfstoreInterface;
import org.eclipse.emf.emfstore.server.core.AbstractSubEmfstoreInterface;
import org.eclipse.emf.emfstore.server.core.MonitorProvider;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectHistory;
import org.eclipse.emf.emfstore.server.model.ProjectId;

/**
 * The {@link EMFStorePropertiesSubInterfaceImpl} class is responsible for
 * handling modifications of EMFStore properties.
 * 
 * @author groeber
 * @author emueller
 */
public class EMFStorePropertiesSubInterfaceImpl extends AbstractSubEmfstoreInterface {

	private static final String EMFSTORE_PROPERTIES_MONITOR = "EmfStorePropertiesMonitor";
	private Map<ProjectHistory, Map<String, EMFStoreProperty>> cache;

	/**
	 * @param parentInterface
	 *            the parent interface
	 * @throws FatalEmfStoreException
	 *             if any fatal error occurs
	 */
	public EMFStorePropertiesSubInterfaceImpl(AbstractEmfstoreInterface parentInterface) throws FatalEmfStoreException {
		super(parentInterface);
		cache = new HashMap<ProjectHistory, Map<String, EMFStoreProperty>>();
	}

	/**
	 * Set the Shared Properties from client on server.
	 * 
	 * @param properties
	 *            properties to be set
	 * @param projectId
	 *            Project where the properties should be saved
	 * @throws EmfStoreException
	 *             if the specified project does not exist
	 * 
	 * @return a list of properties that could not be updated since they are outdated
	 */
	public List<EMFStoreProperty> setProperties(List<EMFStoreProperty> properties, ProjectId projectId)
		throws EmfStoreException {

		synchronized (MonitorProvider.getInstance().getMonitor(EMFSTORE_PROPERTIES_MONITOR)) {

			List<EMFStoreProperty> rejectedProperties = new ArrayList<EMFStoreProperty>();
			ProjectHistory history = findHistory(projectId);

			if (history == null) {
				throw new EmfStoreException("The Project does not exist on the server. Cannot set the properties.");
			}

			EList<EMFStoreProperty> sharedProperties = history.getSharedProperties();
			Set<EMFStoreProperty> replacedProperties = new HashSet<EMFStoreProperty>();

			for (EMFStoreProperty property : properties) {
				EMFStoreProperty foundProperty = findProperty(history, property.getKey());

				if (foundProperty == null) {
					// property has not been shared yet
					sharedProperties.add(property);
					updateCache(history, property);

					if (property.isVersioned()) {
						property.increaseVersion();
					}
				} else {
					if (property.isVersioned()) {
						if (property.getVersion() == foundProperty.getVersion()) {
							// update property
							sharedProperties.set(sharedProperties.indexOf(foundProperty), property);
							replacedProperties.add(foundProperty);
							property.increaseVersion();
						} else {
							// received property is outdated, return current property
							rejectedProperties.add(foundProperty);
						}
					} else {
						sharedProperties.set(sharedProperties.indexOf(foundProperty), property);
						replacedProperties.add(foundProperty);
					}
				}
			}

			try {
				getServerSpace().save();
			} catch (IOException e) {
				// rollback
				sharedProperties.removeAll(properties);
				sharedProperties.addAll(replacedProperties);
				throw new EmfStoreException("Cannot set the properties on the server.", e);
			}

			return rejectedProperties;
		}
	}

	/**
	 * Return the Properties for a specific Project.
	 * 
	 * @param projectId
	 *            ProjectId for the properties
	 * @return EMap containing the Key string and the property value
	 * @throws EmfStoreException
	 *             if specified property does not exist
	 */
	public List<EMFStoreProperty> getProperties(ProjectId projectId) throws EmfStoreException {

		ProjectHistory history = findHistory(projectId);

		if (history != null) {
			List<EMFStoreProperty> temp = new ArrayList<EMFStoreProperty>();
			for (EMFStoreProperty prop : history.getSharedProperties()) {
				temp.add(prop);
			}
			return temp;
		}

		throw new EmfStoreException("The Project does not exist on the server. Cannot set the properties.");

	}

	/**
	 * Find the {@link ProjectHistory} belonging to the project with the given {@link ProjectId}.
	 * 
	 * @param projectId
	 *            a project ID
	 * @return the found project history or <code>null</code> if none has been found
	 */
	private ProjectHistory findHistory(ProjectId projectId) {
		EList<ProjectHistory> serverProjects = getServerSpace().getProjects();

		for (ProjectHistory history : serverProjects) {
			if (history.getProjectId().equals(projectId)) {
				return history;
			}
		}

		return null;
	}

	/**
	 * Finds the property with the given name within the given {@link ProjectHistory}.
	 * 
	 * @param projectHistory
	 *            the project history that should be looked up
	 * @param propertyName
	 *            the name of the property to be found
	 * @return the actual property or <code>null</code> if no such property has been found
	 */
	private EMFStoreProperty findProperty(ProjectHistory projectHistory, String propertyName) {
		Map<String, EMFStoreProperty> propertiesMap = initCacheForHistory(projectHistory);
		return propertiesMap.get(propertyName);
	}

	/**
	 * Initializes a cache entry for the given {@link ProjectHistory}.
	 * 
	 * @param projectHistory
	 *            the history information for which a property-related cache entry should be created
	 * @return the updated cache map containing the new cache entry
	 */
	private Map<String, EMFStoreProperty> initCacheForHistory(ProjectHistory projectHistory) {

		Map<String, EMFStoreProperty> propertiesMap = cache.get(projectHistory);

		if (propertiesMap == null) {
			propertiesMap = new HashMap<String, EMFStoreProperty>();
			for (EMFStoreProperty prop : projectHistory.getSharedProperties()) {
				propertiesMap.put(prop.getKey(), prop);
			}
		}

		return propertiesMap;
	}

	/**
	 * Updates the cache by adding the given {@link EMFStoreProperty} to the shared
	 * properties of the given {@link ProjectHistory}.
	 * 
	 * @param history
	 *            the history
	 * @param property
	 *            the property to be added to the history
	 */
	private void updateCache(ProjectHistory history, EMFStoreProperty property) {
		Map<String, EMFStoreProperty> properties = initCacheForHistory(history);
		properties.put(property.getKey(), property);
	}
}
