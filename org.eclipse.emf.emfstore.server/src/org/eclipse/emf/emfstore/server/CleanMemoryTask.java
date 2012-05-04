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
package org.eclipse.emf.emfstore.server;

import java.util.TimerTask;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.emfstore.common.IDisposable;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.core.MonitorProvider;
import org.eclipse.emf.emfstore.server.model.ProjectHistory;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.Version;

/**
 * This task is used to clean the memory by proxifying the project states.
 * 
 * @author wesendon
 */
public class CleanMemoryTask extends TimerTask {

	/**
	 * Whether to log memory cleaning actions.
	 */
	private static final boolean LOG_UNLOADING = false;

	/**
	 * Time the memory task pauses in order to let others acquire the monitor instance.
	 */
	private static final Integer PREEMPTION_INTERVAL = 100;

	/**
	 * Determines how many change packages should be kept, i.e. how many should
	 * not be unloaded while performing the memory cleaning.
	 */
	private static final Integer KEEP_CHANGES_PACKAGES = 0;

	private final ResourceSet resourceSet;

	/**
	 * Default constructor.
	 * 
	 * @param resourceSet
	 *            the {@link ResourceSet} that should be considered for unloading
	 * 
	 */
	public CleanMemoryTask(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (unloadSomethingIfRequired()) {
			try {
				// enable other participants to acquire the monitor in order to
				// perform synchronized tasks
				Thread.sleep(PREEMPTION_INTERVAL);
			} catch (InterruptedException e) {
				// continue
			}
		}
	}

	private boolean unloadSomethingIfRequired() {
		synchronized (MonitorProvider.getInstance().getMonitor()) {
			for (Resource res : resourceSet.getResources()) {
				if (res.isLoaded()) {

					// unload project states except current one
					Project project = getElement(res, Project.class);

					if (project != null) {
						Version version = getParent(project, Version.class);
						if (version != null && version.getNextVersion() != null) {
							log("unloading: " + project);
							unload(res);
							return true;
						}
					}

					// unload change packages
					ChangePackage cp = getElement(res, ChangePackage.class);

					if (cp != null) {
						Version version = getParent(cp, Version.class);
						ProjectHistory history = getParent(version, ProjectHistory.class);
						if (version != null
							&& history != null
							&& version.getPrimarySpec().getIdentifier() <= (history.getVersions().size()
								- KEEP_CHANGES_PACKAGES - 1)) {
							log("unloading: " + cp);
							unload(res);
							// version.setChanges(null);
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	private void log(String str) {
		if (LOG_UNLOADING) {
			ModelUtil.logInfo(str);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getElement(Resource res, Class<T> clazz) {
		if (res.getContents().size() == 1 && clazz.isInstance(res.getContents().get(0))) {
			return (T) res.getContents().get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> T getParent(EObject obj, Class<T> clazz) {
		if (obj != null && obj.eContainer() != null && clazz.isInstance(obj.eContainer())) {
			return (T) obj.eContainer();
		}
		return null;
	}

	private void unload(Resource res) {
		// sanity check: this check is specific to our 1 element per resource
		// structure for projects and change packages
		if (res.getContents().size() != 1) {
			return;
		}
		EObject eObject = res.getContents().get(0);

		// unload to proxify
		res.unload();
		res.getResourceSet().getResources().remove(res);

		if (eObject instanceof IdEObjectCollection && eObject instanceof IDisposable) {
			IDisposable disposable = (IDisposable) eObject;
			disposable.dispose();
		}

		// sanity check
		if (!eObject.eIsProxy()) {
			ModelUtil.logWarning("Couldn't unload: " + eObject);
			return;
		}

		// unset all contained children
		for (EReference child : eObject.eClass().getEAllContainments()) {
			eObject.eUnset(child);
		}
	}
}
