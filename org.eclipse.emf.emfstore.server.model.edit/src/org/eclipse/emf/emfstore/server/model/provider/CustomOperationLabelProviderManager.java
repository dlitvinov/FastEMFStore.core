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
package org.eclipse.emf.emfstore.server.model.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.emfstore.common.IDisposable;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

/**
 * Class for managing customLabelProvider for specified operations.
 * 
 * @author Michael Kagel
 * @author emueller
 */
public class CustomOperationLabelProviderManager implements IDisposable {

	private static List<AbstractOperationCustomLabelProvider> list;

	/**
	 * Constructor.
	 */
	public CustomOperationLabelProviderManager() {
		if (list == null) {
			initExtensions();
		}
	}

	/**
	 * Provides a customLabelProvider for a specified operation.
	 * 
	 * @param operation for which the method should provide a customLabelProvider.
	 * @return The customLabelProvider for the given operation or null if there is no customLabelProvider.
	 */
	public AbstractOperationCustomLabelProvider getCustomLabelProvider(AbstractOperation operation) {

		AbstractOperationCustomLabelProvider highestVisualizer = null;

		int highestNumber = 0;
		int currentRenderState = 0;

		for (AbstractOperationCustomLabelProvider changePackageVisualizer : list) {
			currentRenderState = changePackageVisualizer.canRender(operation);
			// Take the highest provider
			if (currentRenderState > highestNumber) {
				highestNumber = currentRenderState;
				highestVisualizer = changePackageVisualizer;
			}
		}

		return highestVisualizer;
	}

	private void initExtensions() {

		list = new ArrayList<AbstractOperationCustomLabelProvider>();

		for (ExtensionElement element : new ExtensionPoint(
			"org.eclipse.emf.emfstore.server.model.edit.customOperationLabelProvider", true).getExtensionElements()) {
			try {
				AbstractOperationCustomLabelProvider provider = element.getClass("class",
					AbstractOperationCustomLabelProvider.class);
				list.add(provider);
			} catch (ExtensionPointException e) {
				ModelUtil.logException("Exception occured while initializing custom label provider extensions!", e);
			}
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.common.IDisposable#dispose()
	 */
	public void dispose() {
		list = null;
	}
}
