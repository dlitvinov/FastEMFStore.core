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
package org.eclipse.emf.emfstore.common;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * 
 * @author emueller
 */
public class ResourceFactoryRegistry extends XMIResourceFactoryImpl implements Resource.Factory.Registry {

	@Override
	public Resource createResource(URI uri) {
		return new EMFStoreResource(uri);
	}

	public Factory getFactory(URI uri) {
		return this;
	}

	public Factory getFactory(URI uri, String contentType) {
		return this;
	}

	public Map<String, Object> getProtocolToFactoryMap() {
		return null;
	}

	public Map<String, Object> getExtensionToFactoryMap() {
		return null;
	}

	public Map<String, Object> getContentTypeToFactoryMap() {
		return null;
	}
}