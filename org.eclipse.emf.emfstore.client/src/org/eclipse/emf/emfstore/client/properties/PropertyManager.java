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
package org.eclipse.emf.emfstore.client.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.accesscontrol.AccessControlHelper;
import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
import org.eclipse.emf.emfstore.common.model.EMFStorePropertyType;
import org.eclipse.emf.emfstore.common.model.PropertyStringValue;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;

/**
 * This class is responsible for the modification of EMFStore based properties. <br/>
 * There are two kinds of properties, local and shared ones.
 * <ul>
 * <li>Local properties are project space specific and thus are not shared across project space boundaries.</li>
 * <li>
 * On the contrary, shared properties may be shared across project space boundaries via the synchronize call.<br/>
 * If a property is shared it may either be versioned or not. <br/>
 * Unversioned properties follow the principle 'last-write-wins' principle, i.e. if two callers modify the same property
 * the one that executed the call later will have overwritten the the value written by the first caller.<br/>
 * This is not the case with versioned properties.<br/>
 * If two callers modify a versioned property the latter one will receive an exception telling him that the property
 * that he has modify is outdated. The caller then should catch the exception and handle it appropriately, e.g. by
 * updating the state of the shared properties and then retransmitting his changes.</li>
 * </ul>
 * Shared and local properties both have their own namespace meaning that a
 * shared property named <code>"foo"</code> has nothing in common with a local
 * property called <code>"foo"</code>.
 * 
 * @author haunolder
 * @author emueller
 **/
public final class PropertyManager {

	private final ProjectSpaceImpl projectSpace;
	private Map<String, EMFStoreProperty> sharedProperties;
	private Map<String, EMFStoreProperty> localProperties;

	/**
	 * PropertyManager constructor.
	 * 
	 * @param projectSpace
	 *            the project space that should get managed by the property
	 *            manager
	 **/
	public PropertyManager(ProjectSpace projectSpace) {
		this.projectSpace = (ProjectSpaceImpl) projectSpace;
		this.localProperties = createMap(EMFStorePropertyType.LOCAL);
		this.sharedProperties = createMap(EMFStorePropertyType.SHARED);
	}

	/**
	 * Set a local property. If the property already exists it will be updated.
	 * 
	 * @param propertyName
	 *            the name of the local property
	 * @param value
	 *            the actual value of the property
	 **/
	public void setLocalProperty(String propertyName, EObject value) {
		EMFStoreProperty prop = findProperty(propertyName);

		if (prop == null) {
			prop = createProperty(propertyName, value, false);
			prop.setType(EMFStorePropertyType.LOCAL);
			projectSpace.getProperties().add(prop);
		} else {
			prop.setValue(value);
		}

		localProperties.put(propertyName, prop);
		projectSpace.saveProjectSpaceOnly();
	}

	/**
	 * Sets a local string property. If the property already exists it will be
	 * updated.
	 * 
	 * @param propertyName
	 *            the name of the local property
	 * @param value
	 *            the value of the local property
	 **/
	public void setLocalStringProperty(String propertyName, String value) {
		PropertyStringValue propertyValue = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
			.createPropertyStringValue();
		propertyValue.setValue(value);
		setLocalProperty(propertyName, propertyValue);
	}

	/**
	 * Retrieves a local property.
	 * 
	 * @param propertyName
	 *            the name of the local property
	 * @return the local property
	 **/
	public EMFStoreProperty getLocalProperty(String propertyName) {
		return localProperties.get(propertyName);
	}

	/**
	 * Retrieves a local string property
	 * 
	 * @param propertyName
	 *            the name of a local string property
	 * @return the string value if it exists, otherwise <code>null</code>
	 **/
	public String getLocalStringProperty(String propertyName) {
		EMFStoreProperty property = localProperties.get(propertyName);
		if (property == null || property.getValue() == null) {
			return null;
		}
		return ((PropertyStringValue) property.getValue()).getValue();
	}

	/**
	 * Sets the property with the given name to the given value.
	 * 
	 * @param propertyName
	 *            the name of the property to be set
	 * @param value
	 *            the actual value of the property
	 */
	public void setSharedVersionedProperty(String propertyName, EObject value) {
		setSharedProperty(propertyName, value, true);
	}

	/**
	 * Set a shared string property.
	 * 
	 * @param propertyName
	 *            the name of the shared property
	 * @param string
	 *            the string value that should be set
	 * 
	 * @see this{@link #synchronizeSharedProperties()}
	 **/
	public void setSharedStringProperty(String propertyName, String string) {
		PropertyStringValue propertyValue = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
			.createPropertyStringValue();
		propertyValue.setValue(string);
		setSharedProperty(propertyName, propertyValue, false);
	}

	/**
	 * Set a versioned shared string property.
	 * 
	 * @param propertyName
	 *            the name of the shared property
	 * @param string
	 *            the string value that should be set
	 * 
	 * @see this{@link #synchronizeSharedProperties()}
	 **/
	public void setSharedVersionedStringProperty(String propertyName, String string) {
		PropertyStringValue propertyValue = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
			.createPropertyStringValue();
		propertyValue.setValue(string);
		setSharedProperty(propertyName, propertyValue, true);
	}

	/**
	 * Set a shared property.
	 * 
	 * @param propertyName
	 *            the name of the shared property
	 * @param value
	 *            the value of the shared property
	 * @param isVersioned
	 *            whether the shared property should be versioned or not
	 **/
	private void setSharedProperty(String propertyName, EObject value, boolean isVersioned) {

		EMFStoreProperty prop = findProperty(propertyName);

		if (prop == null) {
			prop = createProperty(propertyName, value, isVersioned);
			prop.setType(EMFStorePropertyType.SHARED);
			this.projectSpace.getProperties().add(prop);
		} else {
			prop.setValue(value);
		}

		this.projectSpace.getChangedSharedProperties().add(prop);
		projectSpace.saveProjectSpaceOnly();
	}

	/**
	 * Set a shared property.
	 * 
	 * @param propertyName
	 *            the name of the shared property
	 * @param value
	 *            the value of the shared property
	 **/
	public void setSharedProperty(String propertyName, EObject value) {
		setSharedProperty(propertyName, value, false);
	}

	/**
	 * Updates a shared versioned property within the project space to the one
	 * given, i.e. the name of the property is first used to look it up within
	 * the project space. If found, the value and version attributes are
	 * updated, otherwise the property will be created.
	 * 
	 * @param property
	 *            the updated property
	 */
	private void updateProperty(EMFStoreProperty property) {

		EMFStoreProperty prop = findProperty(property.getKey());

		if (prop == null) {
			prop = createProperty(property.getKey(), property.getValue(), property.getVersion() != 0);
			prop.setType(EMFStorePropertyType.SHARED);
			this.projectSpace.getProperties().add(prop);
		} else {
			prop.setValue(property.getValue());
		}

		prop.setVersion(property.getVersion());

		this.sharedProperties.put(property.getKey(), prop);
		projectSpace.saveProjectSpaceOnly();
	}

	/**
	 * Retrieves the shared property with the given name.
	 * 
	 * @param propertyName
	 *            the name of the shared property
	 * @return value the actual value of the shared property
	 **/
	public EMFStoreProperty getSharedProperty(String propertyName) {
		return sharedProperties.get(propertyName);
	}

	/**
	 * Retrieves a shared string property.
	 * 
	 * @param propertyName
	 *            of the shared property as String
	 * @return the string value if it exists, otherwise <code>null</code>
	 **/
	public String getSharedStringProperty(String propertyName) {
		EMFStoreProperty property = sharedProperties.get(propertyName);
		if (property == null || property.getValue() == null) {
			return null;
		}
		return ((PropertyStringValue) property.getValue()).getValue();
	}

	/**
	 * Transmit changed shared properties to the server. Clears the
	 * changedSharedProperties List and fills shareProperties with the actual
	 * properties from the server.
	 * 
	 * @throws AccessControlException
	 *             if the caller has no write access to the project space
	 * @throws EmfStoreException
	 *             if the project space being manipulated is not yet shared or
	 *             an error occurs within EMFStore
	 * @throws EMFStorePropertiesOutdatedException
	 *             if any changed property is outdated
	 **/
	public void synchronizeSharedProperties() throws AccessControlException, EmfStoreException,
		EMFStorePropertiesOutdatedException {

		// check if project is shared, if not throw checked exception if it is
		// shared
		if (projectSpace.getUsersession() == null) {
			throw new EmfStoreException("Project has not been shared yet.");
		}

		new AccessControlHelper(projectSpace.getUsersession()).checkWriteAccess(projectSpace.getProjectId());

		List<EMFStoreProperty> changedProperties = new ArrayList<EMFStoreProperty>(
			projectSpace.getChangedSharedProperties());

		List<EMFStoreProperty> rejectedProperties = WorkspaceManager
			.getInstance()
			.getConnectionManager()
			.setEMFProperties(this.projectSpace.getUsersession().getSessionId(), changedProperties,
				this.projectSpace.getProjectId());

		// setEMFProperties returns us a list of properties as found one the
		// server,
		// i.e. we have to deal with different object identities
		List<EMFStoreProperty> nonRejectedProperties = filterNonRejected(changedProperties, rejectedProperties);
		projectSpace.getChangedSharedProperties().removeAll(nonRejectedProperties);

		// update properties to reflect current state on server
		List<EMFStoreProperty> sharedProperties = WorkspaceManager.getInstance().getConnectionManager()
			.getEMFProperties(this.projectSpace.getUsersession().getSessionId(), this.projectSpace.getProjectId());

		for (EMFStoreProperty prop : sharedProperties) {
			updateProperty(prop);
		}

		if (rejectedProperties.size() > 0) {
			throw new EMFStorePropertiesOutdatedException(rejectedProperties);
		}
	}

	/**
	 * Filters a list of changed properties to find only those that have not
	 * been rejected.
	 * 
	 * @param changedProperties
	 *            the list of changed properties
	 * @param rejectedProperties
	 *            the list containing all properties that have been rejected
	 * @return a list of properties that have not been rejected
	 */
	private List<EMFStoreProperty> filterNonRejected(List<EMFStoreProperty> changedProperties,
		List<EMFStoreProperty> rejectedProperties) {
		List<EMFStoreProperty> result = new ArrayList<EMFStoreProperty>();

		for (EMFStoreProperty changed : changedProperties) {

			boolean isNotRejected = true;

			for (EMFStoreProperty rejected : rejectedProperties) {
				if (changed.getKey().equals(rejected.getKey())) {
					isNotRejected = false;
					break;
				}
			}

			// a found property has been rejected, so we only pay attention to
			// those
			if (isNotRejected) {
				result.add(changed);
			}
		}

		return result;
	}

	/**
	 * Creates a map based on the project properties of the given {@link EMFStorePropertyType}.
	 * 
	 * @param type
	 *            the {@link EMFStorePropertyType} of the properties that should
	 *            be contained in the map
	 */
	private Map<String, EMFStoreProperty> createMap(EMFStorePropertyType type) {
		Map<String, EMFStoreProperty> map = new HashMap<String, EMFStoreProperty>();
		EList<EMFStoreProperty> properties = this.projectSpace.getProperties();

		for (EMFStoreProperty prop : properties) {
			if (prop.getType() == type) {
				map.put(prop.getKey(), prop);
			}
		}

		return map;
	}

	/**
	 * Creates a property with the given key and value.
	 * 
	 * @param key
	 *            the name of the property
	 * @param value
	 *            the actual value of the property
	 * @return the newly created property
	 */
	private EMFStoreProperty createProperty(String key, EObject value, boolean isVersioned) {
		EMFStoreProperty prop = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createEMFStoreProperty();
		prop.setKey(key);
		prop.setValue(value);

		if (isVersioned) {
			prop.setVersion(EMFStoreProperty.VERSIONED);
		}

		return prop;
	}

	/**
	 * Returns the property with the given name if it is contained in properties
	 * map of the project space.
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @return the property or <code>null</code> if no such property has been
	 *         found
	 */
	private EMFStoreProperty findProperty(String propertyName) {

		EMFStoreProperty property = localProperties.get(propertyName);

		if (property == null) {
			property = sharedProperties.get(propertyName);
		}

		if (property == null) {
			// actually we should never get here
			for (EMFStoreProperty p : projectSpace.getProperties()) {
				if (p.getKey().equals(propertyName)) {
					property = p;
				}
			}

			if (property != null) {
				if (property.getType() == EMFStorePropertyType.LOCAL) {
					localProperties.put(propertyName, property);
				} else {
					sharedProperties.put(propertyName, property);
				}
			}
		}

		return property;
	}

}
