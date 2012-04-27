/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
 * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 ******************************************************************************/
package org.eclipse.emf.emfstore.client.test.server;

import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.connectionmanager.AbstractSessionProvider;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.client.test.SetupHelper;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.junit.Assert;

/**
 * A session provider implementation meant to be used by any tests.
 * 
 * @author emueller
 */
public final class TestSessionProvider extends AbstractSessionProvider {

	private static Usersession usersession;

	/**
	 * Initializes the singleton statically.
	 */
	private static class SingletonHolder {
		public static final TestSessionProvider INSTANCE = new TestSessionProvider();
	}

	/**
	 * Returns the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static TestSessionProvider getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Returns the default {@link Usersession}.
	 * 
	 * @return the default user session
	 * @throws AccessControlException if login fails
	 * @throws EmfStoreException if anything else fails
	 */
	public Usersession getDefaultUsersession() throws AccessControlException, EmfStoreException {

		new EMFStoreCommand() {
			@Override
			protected void doRun() {
				try {
					usersession.logIn();
				} catch (AccessControlException e) {
					Assert.fail();
				} catch (EmfStoreException e) {
					Assert.fail();
				}
			}
		}.run(false);

		return usersession;
	}

	public TestSessionProvider() {

		final Workspace workspace = WorkspaceManager.getInstance().getCurrentWorkspace();
		usersession = org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE.createUsersession();
		usersession.setServerInfo(SetupHelper.getServerInfo());
		usersession.setUsername("super");
		usersession.setPassword("super");

		new EMFStoreCommand() {
			@Override
			protected void doRun() {
				workspace.getUsersessions().add(usersession);
			}
		}.run(false);

		workspace.save();
	}

	@Override
	public Usersession provideUsersession(ServerInfo serverInfo) throws EmfStoreException {
		return usersession;
	}

	@Override
	public void login(Usersession usersession) throws EmfStoreException {
		usersession.logIn();
	}

}
