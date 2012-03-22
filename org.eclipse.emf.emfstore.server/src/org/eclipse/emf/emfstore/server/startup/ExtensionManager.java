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
package org.eclipse.emf.emfstore.server.startup;

import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
import org.eclipse.emf.emfstore.server.EmfStoreInterface;
import org.eclipse.emf.emfstore.server.accesscontrol.AccessControlImpl;
import org.eclipse.emf.emfstore.server.connection.ConnectionHandler;
import org.eclipse.emf.emfstore.server.model.ProjectHistory;
import org.eclipse.emf.emfstore.server.model.ServerSpace;

/**
 * This class calls the startup listeners, registered at the extension point.
 * 
 * @author wesendon
 */
public final class ExtensionManager {

	private ExtensionManager() {
	}

	/**
	 * Calls the startup listeners.
	 * 
	 * @param projects list of projects
	 */
	public static void notifyStartupListener(EList<ProjectHistory> projects) {
		for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.server.startuplistener", true)
			.getExtensionElements()) {
			try {
				element.getClass("class", StartupListener.class).startedUp(projects);
			} catch (ExtensionPointException e) {
				// fail silently
			}
		}
	}

	/**
	 * Notifies the post startup listener.
	 * 
	 * @param serverspace serverspace
	 * @param accessControl accessscontrol
	 * @param connectionHandlers set of connection handlers
	 */
	public static void notifyPostStartupListener(ServerSpace serverspace, AccessControlImpl accessControl,
		Set<ConnectionHandler<? extends EmfStoreInterface>> connectionHandlers) {
		for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.server.poststartuplistener", true)
			.getExtensionElements()) {
			try {
				element.getClass("class", PostStartupListener.class).postStartUp(serverspace, accessControl,
					connectionHandlers);
			} catch (ExtensionPointException e) {
				// fail silently
			}
		}
	}

}
