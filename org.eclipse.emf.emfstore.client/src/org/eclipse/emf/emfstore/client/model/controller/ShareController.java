package org.eclipse.emf.emfstore.client.model.controller;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.emfstore.client.model.Configuration;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceBase;
import org.eclipse.emf.emfstore.client.model.observers.LoginObserver;
import org.eclipse.emf.emfstore.client.model.observers.ShareObserver;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.ProjectInfo;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;

public class ShareController extends ServerCall<Void> {

	public ShareController(ProjectSpaceBase projectSpaceImpl, Usersession session, IProgressMonitor monitor) {
		super(projectSpaceImpl);

		// if session is null, session will be injected by sessionmanager
		setUsersession(session);
		setProgressMonitor(monitor);
	}

	@Override
	protected Void run() throws EmfStoreException {
		doRun();
		return null;
	}

	@SuppressWarnings("unchecked")
	private void doRun() throws EmfStoreException {

		getProgressMonitor().beginTask("Sharing Project", 100);
		getProgressMonitor().worked(1);
		getProgressMonitor().subTask("Preparing project for sharing");

		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
		logMessage.setAuthor(getUsersession().getUsername());
		logMessage.setClientDate(new Date());
		logMessage.setMessage("Initial commit");
		ProjectInfo createdProject;

		getProjectSpace().stopChangeRecording();
		Configuration.setAutoSave(false);

		getProgressMonitor().worked(10);
		if (getProgressMonitor().isCanceled()) {
			Configuration.setAutoSave(true);
			getProjectSpace().getStatePersister().saveDirtyResources(true);
			getProjectSpace().startChangeRecording();
			getProgressMonitor().done();
		}
		getProgressMonitor().subTask("Sharing project with server");

		createdProject = WorkspaceManager
			.getInstance()
			.getConnectionManager()
			.createProject(getUsersession().getSessionId(), getProjectSpace().getProjectName(),
				getProjectSpace().getProjectDescription(), logMessage, getProjectSpace().getProject());

		getProgressMonitor().worked(70);
		getProgressMonitor().subTask("Finalizing share");

		// set attributes after server call
		this.setUsersession(getUsersession());
		WorkspaceManager.getObserverBus().register(getProjectSpace(), LoginObserver.class);

		Configuration.setAutoSave(true);
		getProjectSpace().getStatePersister().saveDirtyResources(true);
		getProjectSpace().startChangeRecording();
		getProjectSpace().setBaseVersion(createdProject.getVersion());
		getProjectSpace().setLastUpdated(new Date());
		getProjectSpace().setProjectId(createdProject.getProjectId());
		getProjectSpace().setUsersession(getUsersession());
		getProjectSpace().saveProjectSpaceOnly();

		// TODO ASYNC implement File Upload with observer
		// If any files have already been added, upload them.
		getProjectSpace().getFileTransferManager().uploadQueuedFiles(new NullProgressMonitor());

		getProjectSpace().getOperations().clear();
		getProjectSpace().updateDirtyState();

		getProgressMonitor().done();
		WorkspaceManager.getObserverBus().notify(ShareObserver.class).shareDone(getProjectSpace());
	}

}
