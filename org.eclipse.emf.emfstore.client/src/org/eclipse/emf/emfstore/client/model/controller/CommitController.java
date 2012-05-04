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
package org.eclipse.emf.emfstore.client.model.controller;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
import org.eclipse.emf.emfstore.client.model.controller.callbacks.CommitCallback;
import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceBase;
import org.eclipse.emf.emfstore.client.model.observers.CommitObserver;
import org.eclipse.emf.emfstore.server.exceptions.BaseVersionOutdatedException;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;

/**
 * The controller responsible for performing a commit.
 * 
 * @author wesendon
 */
public class CommitController extends ServerCall<PrimaryVersionSpec> {

	private LogMessage logMessage;
	private CommitCallback callback;

	/**
	 * Constructor.
	 * 
	 * @param projectSpace
	 *            the project space whose pending changes should be commited
	 * @param logMessage
	 *            a log message documenting the commit
	 * @param callback
	 *            an callback that will be called during and at the end of the commit.
	 *            May be <code>null</code>.
	 * @param monitor
	 *            an {@link IProgressMonitor} that will be used to inform clients about the commit progress.
	 *            May be <code>null</code>.
	 */
	public CommitController(ProjectSpaceBase projectSpace, LogMessage logMessage, CommitCallback callback,
		IProgressMonitor monitor) {
		super(projectSpace);
		this.logMessage = (logMessage == null) ? createLogMessage() : logMessage;
		this.callback = callback == null ? CommitCallback.NOCALLBACK : callback;
		setProgressMonitor(monitor);
	}

	@Override
	protected PrimaryVersionSpec run() throws EmfStoreException {
		return commit(this.logMessage);
	}

	private PrimaryVersionSpec commit(LogMessage logMessage) throws EmfStoreException {
		getProgressMonitor().beginTask("Commiting Changes", 100);
		getProgressMonitor().worked(1);

		getProgressMonitor().subTask("Checking changes");
		// check if there are any changes
		if (!getProjectSpace().isDirty()) {
			callback.noLocalChanges(getProjectSpace());
			return getProjectSpace().getBaseVersion();
		}
		getProjectSpace().cleanCutElements();

		getProgressMonitor().subTask("Resolving new version");

		// check if we need to update first
		PrimaryVersionSpec resolvedVersion = getProjectSpace().resolveVersionSpec(VersionSpec.HEAD_VERSION);
		if (!getProjectSpace().getBaseVersion().equals(resolvedVersion)) {
			if (!callback.baseVersionOutOfDate(getProjectSpace())) {
				throw new BaseVersionOutdatedException();
			}
		}

		getProgressMonitor().worked(10);
		getProgressMonitor().subTask("Gathering changes");
		ChangePackage changePackage = getProjectSpace().getLocalChangePackage();
		changePackage.setLogMessage(logMessage);
		WorkspaceManager.getObserverBus().notify(CommitObserver.class).inspectChanges(getProjectSpace(), changePackage);

		getProgressMonitor().subTask("Presenting Changes");
		if (!callback.inspectChanges(getProjectSpace(), changePackage) || getProgressMonitor().isCanceled()) {
			return getProjectSpace().getBaseVersion();
		}

		getProgressMonitor().subTask("Sending changes to server");
		PrimaryVersionSpec newBaseVersion = getConnectionManager().createVersion(getUsersession().getSessionId(),
			getProjectSpace().getProjectId(), getProjectSpace().getBaseVersion(), changePackage,
			changePackage.getLogMessage());

		getProgressMonitor().worked(75);
		getProgressMonitor().subTask("Finalizing commit");
		getProjectSpace().setBaseVersion(newBaseVersion);
		getProjectSpace().getOperations().clear();

		getProjectSpace().saveProjectSpaceOnly();

		// TODO reimplement with ObserverBus and think about subtasks for commit
		getProjectSpace().getFileTransferManager().uploadQueuedFiles(new NullProgressMonitor());
		WorkspaceManager.getObserverBus().notify(CommitObserver.class)
			.commitCompleted(getProjectSpace(), newBaseVersion);

		getProjectSpace().updateDirtyState();
		return newBaseVersion;
	}

	private LogMessage createLogMessage() {
		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
		String commiter = "UNKOWN";
		if (getProjectSpace().getUsersession() != null && getProjectSpace().getUsersession().getACUser() != null
			&& getProjectSpace().getUsersession().getACUser().getName() != null) {
			commiter = getProjectSpace().getUsersession().getACUser().getName();
		}
		logMessage.setAuthor(commiter);
		logMessage.setClientDate(new Date());
		logMessage.setMessage("");
		return logMessage;
	}

}
