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
package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.provider;

import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
import org.eclipse.emf.emfstore.client.model.Workspace;

/**
 * Content provider for the EMFStore Browser View.
 * 
 * @author emueller
 * @author ovonwesend
 */
public class ESBrowserContentProvider extends AdapterFactoryContentProvider {

	/**
	 * Default constructor.
	 */
	public ESBrowserContentProvider() {
		super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
	}

	@Override
	public Object[] getElements(Object object) {
		if (object instanceof Workspace) {
			return ((Workspace) object).getServerInfos().toArray();
		}

		return super.getElements(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object object) {
		Object value = object;
		Object[] children;

		if (value instanceof Workspace) {
			children = ((Workspace) value).getServerInfos().toArray();
		} else if (value instanceof ServerInfo) {
			ServerInfo serverInfo = (ServerInfo) value;
			return serverInfo.getProjectInfos().toArray();
		} else {
			children = super.getChildren(object);
		}

		return children;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof ServerInfo) {
			return true;
		}
		return false;
	}

}
