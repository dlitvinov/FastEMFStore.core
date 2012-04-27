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
package org.eclipse.emf.emfstore.client.model;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;

/**
 * <!-- begin-user-doc --> A representation of the model object ' <em><b>Server Info</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getUrl <em>Url</em>}</li>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getPort <em>Port</em>}</li>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getProjectInfos <em>Project Infos</em>}</li>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getLastUsersession <em>Last Usersession</em>}</li>
 * <li>{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getCertificateAlias <em>Certificate Alias</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo()
 * @model
 * @generated
 */
public interface ServerInfo extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Url</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Url</em>' attribute.
	 * @see #setUrl(String)
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_Url()
	 * @model required="true"
	 * @generated
	 */
	String getUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getUrl <em>Url</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value the new value of the '<em>Url</em>' attribute.
	 * @see #getUrl()
	 * @generated
	 */
	void setUrl(String value);

	/**
	 * Returns the value of the '<em><b>Port</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Port</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Port</em>' attribute.
	 * @see #setPort(int)
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_Port()
	 * @model required="true"
	 * @generated
	 */
	int getPort();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getPort <em>Port</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value the new value of the '<em>Port</em>' attribute.
	 * @see #getPort()
	 * @generated
	 */
	void setPort(int value);

	/**
	 * Returns the value of the '<em><b>Project Infos</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.emfstore.server.model.ProjectInfo}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Project Infos</em>' reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Project Infos</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_ProjectInfos()
	 * @model containment="true" resolveProxies="true"
	 * @generated
	 */
	EList<ProjectInfo> getProjectInfos();

	/**
	 * Returns the value of the '<em><b>Last Usersession</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last Usersession</em>' reference isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Last Usersession</em>' reference.
	 * @see #setLastUsersession(Usersession)
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_LastUsersession()
	 * @model
	 * @generated
	 */
	Usersession getLastUsersession();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getLastUsersession
	 * <em>Last Usersession</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value the new value of the '<em>Last Usersession</em>' reference.
	 * @see #getLastUsersession()
	 * @generated
	 */
	void setLastUsersession(Usersession value);

	/**
	 * Returns the value of the '<em><b>Certificate Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Certificate Alias</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Certificate Alias</em>' attribute.
	 * @see #setCertificateAlias(String)
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getServerInfo_CertificateAlias()
	 * @model required="true"
	 * @generated
	 */
	String getCertificateAlias();

	/**
	 * Sets the value of the '{@link org.eclipse.emf.emfstore.client.model.ServerInfo#getCertificateAlias
	 * <em>Certificate Alias</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value the new value of the '<em>Certificate Alias</em>' attribute.
	 * @see #getCertificateAlias()
	 * @generated
	 */
	void setCertificateAlias(String value);

} // ServerInfo
