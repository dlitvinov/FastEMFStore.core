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
package org.eclipse.emf.emfstore.common.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object ' <em><b>Project</b></em>'.
 * 
 * @implements IAdaptable <!-- end-user-doc -->
 * 
 *             <p>
 *             The following features are supported:
 *             <ul>
 *             <li>
 *             {@link org.eclipse.emf.emfstore.common.model.Project#getModelElements
 *             <em>Model Elements</em>}</li>
 *             <li>
 *             {@link org.eclipse.emf.emfstore.common.model.Project#getCutElements
 *             <em>Cut Elements</em>}</li>
 *             </ul>
 *             </p>
 * 
 * @see org.eclipse.emf.emfstore.common.model.ModelPackage#getProject()
 * @model
 * @extends NotifiableIdEObjectCollection
 */
public interface Project extends EObject, IAdaptable, NotifiableIdEObjectCollection {

	/**
	 * Returns the value of the '<em><b>Model Elements</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EObject}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Model Elements</em>' containment reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model Elements</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.common.model.ModelPackage#getProject_ModelElements()
	 * @model containment="true" resolveProxies="true" ordered="false"
	 * @generated
	 */
	EList<EObject> getModelElements();

	/**
	 * Returns the value of the '<em><b>Cut Elements</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EObject}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cut Elements</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cut Elements</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.common.model.ModelPackage#getProject_CutElements()
	 * @model containment="true" resolveProxies="true"
	 * @generated
	 */
	EList<EObject> getCutElements();

	/**
	 * Deletes a project by notifying all project change observers about the
	 * deletion.
	 */
	void delete();

} // Project
