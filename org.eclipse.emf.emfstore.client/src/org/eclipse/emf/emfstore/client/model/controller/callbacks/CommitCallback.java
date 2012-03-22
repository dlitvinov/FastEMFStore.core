package org.eclipse.emf.emfstore.client.model.controller.callbacks;

import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;

public interface CommitCallback {

	boolean baseVersionOutOfDate(ProjectSpace projectSpace);

	boolean inspectChanges(ProjectSpace projectSpace, ChangePackage changePackage);

	void noLocalChanges(ProjectSpace projectSpace);

	public final CommitCallback NOCALLBACK = new CommitCallback() {

		public boolean inspectChanges(ProjectSpace projectSpace, ChangePackage changePackage) {
			return true;
		}

		public boolean baseVersionOutOfDate(ProjectSpace projectSpace) {
			return false;
		}

		public void noLocalChanges(ProjectSpace projectSpace) {
			// do nothing
		}
	};
}
