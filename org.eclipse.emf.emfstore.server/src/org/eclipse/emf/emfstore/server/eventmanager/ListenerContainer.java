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
package org.eclipse.emf.emfstore.server.eventmanager;

import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerEvent;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerProjectEvent;

/**
 * Container for listener.
 * 
 * @author wesendon
 */
public class ListenerContainer {

	private final EMFStoreEventListener listener;
	private final ProjectId projectId;

	public ListenerContainer(EMFStoreEventListener listener, ProjectId projectId) {
		this.listener = listener;
		this.projectId = projectId;
	}

	public boolean handleEvent(ServerEvent event) {
		if (projectId != null && event instanceof ServerProjectEvent) {
			if (!projectId.equals(((ServerProjectEvent) event).getProjectId())) {
				return true;
			}
		}
		return listener.handleEvent(event);
	}
}