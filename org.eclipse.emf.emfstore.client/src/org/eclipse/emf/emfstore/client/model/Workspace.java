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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
import org.eclipse.emf.emfstore.client.model.exceptions.ProjectUrlResolutionException;
import org.eclipse.emf.emfstore.client.model.exceptions.ServerUrlResolutionException;
import org.eclipse.emf.emfstore.client.model.exceptions.UnkownProjectException;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.emf.emfstore.server.model.url.ProjectUrlFragment;
import org.eclipse.emf.emfstore.server.model.url.ServerUrl;
import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;

/**
 * <!-- begin-user-doc --> A representation of the model object ' <em><b>Workspace</b></em>'.
 * 
 * @implements IAdaptable <!-- end-user-doc -->
 * 
 *             <p>
 *             The following features are supported:
 *             <ul>
 *             <li>{@link org.eclipse.emf.emfstore.client.model.Workspace#getProjectSpaces <em>Project Spaces</em>}</li>
 *             <li>{@link org.eclipse.emf.emfstore.client.model.Workspace#getServerInfos <em>Server Infos</em>}</li>
 *             <li>{@link org.eclipse.emf.emfstore.client.model.Workspace#getUsersessions <em>Usersessions</em>}</li>
 *             </ul>
 *             </p>
 * 
 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getWorkspace()
 * @model
 * @generated
 */
public interface Workspace extends EObject, IAdaptable {

	/**
	 * Checkout a project to the workspace in a given version.
	 * 
	 * @param usersession
	 *            The user session that should be used to checkout the project.
	 * @param projectInfo
	 *            An {@link ProjectInfo} instance describing the project and its version.
	 * @throws EmfStoreException
	 *             If an error occurs during the checkout.
	 * @return the project space containing the project
	 * @model
	 * @generated NOT
	 */
	ProjectSpace checkout(Usersession usersession, ProjectInfo projectInfo) throws EmfStoreException;

	/**
	 * Checkout a project to the workspace in a given version.
	 * 
	 * @param usersession
	 *            The user session that should be used to checkout the project.
	 * @param projectInfo
	 *            An {@link ProjectInfo} instance describing the project and its version.
	 * @param targetSpec
	 *            The target version.
	 * @throws EmfStoreException
	 *             If an error occurs during the checkout.
	 * @return the project space containing the project
	 * @model
	 * @generated NOT
	 */
	ProjectSpace checkout(Usersession usersession, ProjectInfo projectInfo, PrimaryVersionSpec targetSpec)
		throws EmfStoreException;

	/**
	 * Creates a new local project that is not shared with the server yet.
	 * 
	 * @param projectName the project name
	 * @param projectDescription the project description
	 * @return the project space that the new project resides in
	 */
	ProjectSpace createLocalProject(String projectName, String projectDescription);

	/**
	 * Creates an empty project on the server.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that contains information about the server on which
	 *            the project should be created.
	 * @param projectName
	 *            The name of the project.
	 * @param projectDescription
	 *            A description of the project to be created.
	 * @return a {@link ProjectInfo} object containing information about the created project
	 * @throws EmfStoreException
	 *             If an error occurs while creating the remote project
	 */
	ProjectInfo createRemoteProject(ServerInfo serverInfo, String projectName, String projectDescription)
		throws EmfStoreException;

	/**
	 * Creates an empty project on the server.
	 * 
	 * @param usersession
	 *            The {@link Usersession} that should be used to create the remote project.<br/>
	 *            If <code>null</code>, the session manager will search for a session.
	 * @param projectName
	 *            The name of the project.
	 * @param projectDescription
	 *            A description of the project to be created.
	 * @return a {@link ProjectInfo} object containing information about the created project
	 * @throws EmfStoreException
	 *             If an error occurs while creating the remote project
	 */
	ProjectInfo createRemoteProject(Usersession usersession, String projectName, String projectDescription)
		throws EmfStoreException;

	/**
	 * Deletes the given project space.
	 * 
	 * @param projectSpace
	 *            the project space to be deleted
	 * @throws IOException
	 *             If deleting the obsolete project space files fails
	 */
	void deleteProjectSpace(ProjectSpace projectSpace) throws IOException;

	/**
	 * Deletes a project on the server.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo}, that contains the information on which server the
	 *            project is located on.
	 * @param projectId
	 *            The ID of the project.
	 * @param deleteFiles
	 *            Whether files should be deleted too
	 * @throws EmfStoreException
	 *             If an error occurs while deleting the project.
	 */
	void deleteRemoteProject(ServerInfo serverInfo, ProjectId projectId, boolean deleteFiles) throws EmfStoreException;

	/**
	 * Deletes a project on the server.
	 * 
	 * @param usersession
	 *            The {@link Usersession} that should be used to delete the project.<br/>
	 *            If <code>null</code>, the session manager will search for a session.
	 * @param projectId
	 *            The ID of the project.
	 * @param deleteFiles
	 *            Whether files should be deleted too
	 * @throws EmfStoreException
	 *             If an error occurs while deleting the project.
	 */
	void deleteRemoteProject(Usersession usersession, ProjectId projectId, boolean deleteFiles)
		throws EmfStoreException;

	/**
	 * Exports a project space to a file.
	 * 
	 * @param projectSpace
	 *            The project space that should be exported
	 * @param file
	 *            The file to export to
	 * @throws IOException
	 *             If creating the export file fails
	 */
	void exportProjectSpace(ProjectSpace projectSpace, File file) throws IOException;

	/**
	 * Exports a project space to a file.
	 * 
	 * @param projectSpace
	 *            The project space that should be exported
	 * @param file
	 *            The file to export to
	 * @param progressMonitor
	 *            The progress monitor that should be used during the xport
	 * @throws IOException
	 *             If creating the export file fails
	 */
	void exportProjectSpace(ProjectSpace projectSpace, File file, IProgressMonitor progressMonitor) throws IOException;

	/**
	 * Exports the whole workspace.
	 * 
	 * @param file
	 *            The file to export to
	 * @throws IOException
	 *             If creating the export file fails
	 */
	void exportWorkSpace(File file) throws IOException;

	/**
	 * Exports the whole workspace to the given file.
	 * 
	 * @param file
	 *            The file to export to
	 * @param progressMonitor
	 *            The progress monitor that should be used during the export
	 * @throws IOException
	 *             If creating the export file fails
	 */
	void exportWorkSpace(File file, IProgressMonitor progressMonitor) throws IOException;

	/**
	 * Returns an {@link AdminBroker} related to the given {@link ServerInfo}.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that should be used to retrieve the admin broker.
	 * @return an {@link AdminBroker} related to the given server info.
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the admin broker
	 * @throws AccessControlException
	 *             If access is denied
	 * @generated NOT
	 */
	AdminBroker getAdminBroker(ServerInfo serverInfo) throws EmfStoreException, AccessControlException;

	/**
	 * Returns an {@link AdminBroker} related to the given {@link Usersession}.
	 * 
	 * @param session
	 *            The user session that should be used to retrieve the admin broker.<br/>
	 *            If <code>null</code>, the session manager will search for a session.
	 * @return an {@link AdminBroker} related to the given user session.
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the admin broker
	 * @throws AccessControlException
	 *             If access is denied
	 * @generated NOT
	 */
	AdminBroker getAdminBroker(Usersession session) throws EmfStoreException, AccessControlException;

	/**
	 * Return this editing domain belonging to this workspace.
	 * 
	 * @return the editing domain
	 * @generated NOT
	 */
	EditingDomain getEditingDomain();

	/**
	 * Retrieves history information for a project.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that contains information about the server the
	 *            project is located on
	 * @param projectId
	 *            The ID of a project
	 * @param query
	 *            A history query.
	 * @return a list of {@link HistoryInfo} instances
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the history information
	 * @generated NOT
	 */
	List<HistoryInfo> getHistoryInfo(ServerInfo serverInfo, ProjectId projectId, HistoryQuery query)
		throws EmfStoreException;

	/**
	 * Retrieves history information for a project.
	 * 
	 * @param usersession
	 *            The {@link Usersession} that should be used to retrieve the history information.
	 *            If <code>null</code>, the session manager will search for a session.
	 * @param projectId
	 *            The ID of a project
	 * @param query
	 *            A history query.
	 * @return a list of {@link HistoryInfo} instances
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the history information
	 * @generated NOT
	 */
	List<HistoryInfo> getHistoryInfo(Usersession usersession, ProjectId projectId, HistoryQuery query)
		throws EmfStoreException;

	/**
	 * Retrieves the project space for the given project.
	 * 
	 * @param project
	 *            The project for which to retrieve the project space.
	 * @return the project space the given project is contained in
	 * @throws UnkownProjectException
	 *             If the project is not known to the workspace
	 */
	ProjectSpace getProjectSpace(Project project) throws UnkownProjectException;

	/**
	 * Returns the value of the '<em><b>Project Spaces</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.emfstore.client.model.ProjectSpace}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getWorkspace
	 * <em>Workspace</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Project Spaces</em>' reference list isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Project Spaces</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getWorkspace_ProjectSpaces()
	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getWorkspace
	 * @model opposite="workspace" containment="true" resolveProxies="true" keys="identifier"
	 * @generated
	 */
	EList<ProjectSpace> getProjectSpaces();

	/**
	 * Get the list of remotely available projects.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that should be used to retrieve the information about remote projects.<br/>
	 * @return a list containing the information about each remote project
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the remote project list.
	 * @generated NOT
	 */
	List<ProjectInfo> getRemoteProjectList(ServerInfo serverInfo) throws EmfStoreException;

	/**
	 * Get the list of remotely available projects.
	 * 
	 * @param usersession
	 *            The {@link Usersession} that should be used to retrieve the remote project list.<br/>
	 *            If <code>null</code>, the session manager will search for a session.
	 * @return a list containing the information about each remote project
	 * @throws EmfStoreException
	 *             If an error occurs while retrieving the remote project list.
	 * @generated NOT
	 */
	List<ProjectInfo> getRemoteProjectList(Usersession usersession) throws EmfStoreException;

	/**
	 * Returns the value of the '<em><b>Server Infos</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.emfstore.client.model.ServerInfo}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Server Infos</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Server Infos</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getWorkspace_ServerInfos()
	 * @model containment="true" resolveProxies="true"
	 * @generated
	 */
	EList<ServerInfo> getServerInfos();

	/**
	 * Returns the value of the '<em><b>Usersessions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.emfstore.client.model.Usersession}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Usersessions</em>' containment reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Usersessions</em>' containment reference list.
	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getWorkspace_Usersessions()
	 * @model containment="true" resolveProxies="true"
	 * @generated
	 */
	EList<Usersession> getUsersessions();

	/**
	 * Imports a project into a project space.
	 * 
	 * @param project
	 *            The project to be imported
	 * @param name
	 *            The name that should be assigned to the project being imported.
	 * @param description
	 *            A description of the project being imported
	 * @return the newly created project space in which the imported project is contained in
	 */
	ProjectSpace importProject(Project project, String name, String description);

	/**
	 * Import an existing project from a given file. The project space containing
	 * the project will be created upon execution.
	 * 
	 * @param absoluteFileName
	 *            The absolute path to a file to import from.
	 * @return the newly created project space in which the imported project is contained in
	 * @throws IOException
	 *             If importing the project fails
	 */
	ProjectSpace importProject(String absoluteFileName) throws IOException;

	/**
	 * Import an existing project space from a file.
	 * 
	 * @param absoluteFileName
	 *            The absolute path to a file to import from.
	 * @return the imported project space
	 * @throws IOException
	 *             If accessing the file or importing fails
	 */
	ProjectSpace importProjectSpace(String absoluteFileName) throws IOException;

	/**
	 * Initializes the workspace and its project spaces.
	 * 
	 * @generated NOT
	 */
	void init();

	/**
	 * Resolves a project URL fragment to the project space the project is in.<br/>
	 * Since a project may have been checked out multiple times, a set of project spaces is returned.
	 * 
	 * @param projectUrlFragment
	 *            the project URL fragment to resolve
	 * @return a set of matching project spaces
	 * @throws ProjectUrlResolutionException
	 *             if the project belonging to the given project URL fragment cannot be found in workspace
	 */
	Set<ProjectSpace> resolve(ProjectUrlFragment projectUrlFragment) throws ProjectUrlResolutionException;

	/**
	 * Resolves a server URL to a server.
	 * 
	 * @param serverUrl
	 *            the server URL to be resolved
	 * @return the resolved {@link ServerInfo}
	 * @throws ServerUrlResolutionException
	 *             if no matching server info can be found
	 */
	Set<ServerInfo> resolve(ServerUrl serverUrl) throws ServerUrlResolutionException;

	/**
	 * Resolves a {@link VersionSpec} to a {@link PrimaryVersionSpec}.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that should be used to resolve the given version specification.
	 * @param versionSpec
	 *            The specification to resolve.
	 * @param projectId
	 *            The ID of a project.
	 * @return the {@link PrimaryVersionSpec}
	 * @throws EmfStoreException
	 *             If an error occurs while resolving the {@link VersionSpec}
	 * @model
	 * @generated NOT
	 */
	PrimaryVersionSpec resolveVersionSpec(ServerInfo serverInfo, VersionSpec versionSpec, ProjectId projectId)
		throws EmfStoreException;

	/**
	 * Resolves a {@link VersionSpec} to a {@link PrimaryVersionSpec}.
	 * 
	 * @param session
	 *            The {@link Usersession} that should be used to resolve the given {@link VersionSpec}.<br/>
	 *            If <code>null</code>, the session manager will search for a session.
	 * @param versionSpec
	 *            The specification to resolve.
	 * @param projectId
	 *            The ID of a project.
	 * @return the {@link PrimaryVersionSpec}
	 * @throws EmfStoreException
	 *             If an error occurs while resolving the {@link VersionSpec}
	 * @model
	 * @generated NOT
	 */
	PrimaryVersionSpec resolveVersionSpec(Usersession session, VersionSpec versionSpec, ProjectId projectId)
		throws EmfStoreException;

	/**
	 * Make the current workspace state persistent.
	 */
	void save();

	/**
	 * Set the workspace connection manager.
	 * 
	 * @param connectionManager
	 *            The connection manager to be set.
	 * @generated NOT
	 */
	void setConnectionManager(ConnectionManager connectionManager);

	/**
	 * Returns the workspace resource set.
	 * 
	 * @return
	 *         The resource set of the workspace
	 * @generated NOT
	 */
	ResourceSet getResourceSet();

	/**
	 * Set the workspace resource set.
	 * 
	 * @param resourceSet
	 *            The resource set to be set.
	 * @generated NOT
	 */
	void setResourceSet(ResourceSet resourceSet);

	/**
	 * Updates the ACUser and it roles.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} that is used to update the ACUser.
	 * @throws EmfStoreException
	 *             if an error occurs while updating the ACUser
	 */
	void updateACUser(ServerInfo serverInfo) throws EmfStoreException;

	/**
	 * Updates the ACUser and it roles.
	 * 
	 * @param session
	 *            The {@link Usersession} that should be used to update the ACUser.
	 *            If <code>null</code>, the session manager will search for a session.
	 * @throws EmfStoreException
	 *             if an error occurs while updating the ACUser
	 */
	void updateACUser(Usersession session) throws EmfStoreException;

	/**
	 * Updates the ProjectInfos for the given {@link ServerInfo}.
	 * 
	 * @param serverInfo
	 *            The {@link ServerInfo} whose project information should be updated.
	 * @throws EmfStoreException
	 *             if an error occurs while updating the project information
	 */
	void updateProjectInfos(ServerInfo serverInfo) throws EmfStoreException;

	/**
	 * Updates the ProjectInfos for the current ServerInfo.
	 * 
	 * @param session
	 *            The {@link Usersession} that should be used to update the project information.
	 *            If <code>null</code>, the session manager will search for a session.
	 * @throws EmfStoreException
	 *             if an error occurs while updating the project information
	 */
	void updateProjectInfos(Usersession session) throws EmfStoreException;

	/**
	 * Adds an server info and saves.
	 * 
	 * @param serverInfo the server info to be added
	 */
	void addServerInfo(ServerInfo serverInfo);

	/**
	 * Removes an server info and saves.
	 * 
	 * @param serverInfo the server info to be removed
	 */
	void removeServerInfo(ServerInfo serverInfo);

} // Workspace
