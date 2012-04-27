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
package org.eclipse.emf.emfstore.client.ui.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.accesscontrol.AccessControlHelper;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommandWithResult;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;

/**
 * Checks if the user has admin access to the server.
 * 
 * @author Shterev
 */
public class IsServerAdminTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
	 *      java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, final Object expectedValue) {
		if ((receiver instanceof ServerInfo || receiver instanceof ProjectInfo) && expectedValue instanceof Boolean) {

			ServerInfo serverInfo = null;

			if (receiver instanceof ServerInfo) {
				serverInfo = (ServerInfo) receiver;
			} else if (receiver instanceof ProjectInfo) {
				ProjectInfo projectInfo = (ProjectInfo) receiver;
				serverInfo = findServerInfo(projectInfo);
			}

			final ServerInfo finalServerInfo = serverInfo;
			EMFStoreCommandWithResult<Boolean> command = new EMFStoreCommandWithResult<Boolean>() {
				@Override
				protected Boolean doRun() {
					Usersession usersession = finalServerInfo.getLastUsersession();
					boolean isAdmin = false;
					if (usersession != null && usersession.getACUser() != null) {
						AccessControlHelper accessControlHelper = new AccessControlHelper(usersession);
						try {
							accessControlHelper.checkServerAdminAccess();
							isAdmin = true;
						} catch (AccessControlException e) {
						}
					}

					return new Boolean(isAdmin).equals(expectedValue);
				}
			};
			Boolean result = command.run(false);
			return result;

		}
		return false;
	}

	private ServerInfo findServerInfo(ProjectInfo projectInfo) {
		for (ServerInfo serverInfo : WorkspaceManager.getInstance().getCurrentWorkspace().getServerInfos()) {
			if (projectInfo.eContainer() != null && projectInfo.eContainer().equals(serverInfo)) {
				return serverInfo;
			} else {
				for (ProjectInfo info : serverInfo.getProjectInfos()) {
					if (info.getProjectId().equals(projectInfo.getProjectId())) {
						return serverInfo;
					}
				}
			}
		}

		return null;
	}
}
