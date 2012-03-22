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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class EMFStoreResource extends XMIResourceImpl {

	public EMFStoreResource(URI uri) {
		super(uri);
		this.setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
	}
	
	public void setIdToEObjectMap(Map<String, EObject> idToEObjectMap, Map<EObject, String> eObjectToIdMap) {
		this.idToEObjectMap = idToEObjectMap;
		this.eObjectToIDMap = eObjectToIdMap;
	}
}
