package org.eclipse.emf.emfstore.client.test.server;

import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.Workspace;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.client.test.SetupHelper;
import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnitId;
import org.eclipse.emf.emfstore.server.model.accesscontrol.roles.RolesPackage;
import org.junit.After;
import org.junit.Before;

public abstract class TransmissionTests extends ServerTests {

	private static ProjectSpace projectSpace1;
	private static ProjectSpace projectSpace2;
	private static Usersession usersession1;
	private static Usersession usersession2;
	private ACOrgUnitId user1;
	private ACOrgUnitId user2;

	@Before
	public void setUpTests() throws EmfStoreException {
		user1 = setupUsers("writer1", RolesPackage.eINSTANCE.getWriterRole());
		user2 = setupUsers("writer2", RolesPackage.eINSTANCE.getWriterRole());
		usersession1 = setUpUsersession("writer1", "foo");
		usersession2 = setUpUsersession("writer2", "foo");
		new EMFStoreCommand() {

			@Override
			protected void doRun() {
				Workspace workspace = WorkspaceManager.getInstance().getCurrentWorkspace();
				workspace.getServerInfos().add(getServerInfo());
				workspace.getUsersessions().add(usersession1);
				workspace.getUsersessions().add(usersession2);
				workspace.save();

				try {
					usersession1.logIn();
					usersession2.logIn();
					setProjectSpace1(workspace.checkout(usersession1, getProjectInfo()));
					setProjectSpace2(workspace.checkout(usersession2, getProjectInfo()));
				} catch (AccessControlException e) {
					throw new RuntimeException(e);
				} catch (EmfStoreException e) {
					throw new RuntimeException(e);
				}
			}
		}.run(false);

	}

	@After
	public void tearDownUsers() throws EmfStoreException {
		SetupHelper.deleteUserOnServer(user1);
		SetupHelper.deleteUserOnServer(user2);

	}

	protected static ProjectSpace getProjectSpace1() {
		return projectSpace1;
	}

	protected static void setProjectSpace1(ProjectSpace projectSpace1) {
		TransmissionTests.projectSpace1 = projectSpace1;
	}

	protected static ProjectSpace getProjectSpace2() {
		return projectSpace2;
	}

	protected static void setProjectSpace2(ProjectSpace projectSpace2) {
		TransmissionTests.projectSpace2 = projectSpace2;
	}
}
