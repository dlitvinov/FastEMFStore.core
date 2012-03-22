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
package org.eclipse.emf.emfstore.client.ui.views.changes;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.emfstore.server.model.provider.AbstractOperationCustomLabelProvider;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;

/**
 * Default label provider for operations.
 * 
 * @author emueller
 * 
 */
public class DefaultOperationLabelProvider extends AbstractOperationCustomLabelProvider {

	/**
	 * The label to be shown for unknown element.
	 */
	protected static final String UNKOWN_ELEMENT = "(Unkown Element)";

	/**
	 * The max amount of characters a name may consist of.
	 */
	protected static final int MAX_NAME_LENGTH = 30;

	private AdapterFactoryLabelProvider adapterFactoryLabelProvider;
	private ComposedAdapterFactory adapterFactory;

	/**
	 * Constructor.
	 */
	public DefaultOperationLabelProvider() {
		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.server.model.provider.AbstractOperationCustomLabelProvider#getDescription(org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation)
	 */
	@Override
	public String getDescription(AbstractOperation operation) {

		if (operation instanceof CompositeOperation) {
			CompositeOperation compositeOperation = (CompositeOperation) operation;
			// artificial composite because of opposite reference,
			// take description of main operation
			if (compositeOperation.getMainOperation() != null) {
				return getDescription(compositeOperation.getMainOperation());
			}
		}

		return adapterFactoryLabelProvider.getText(operation);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.server.model.provider.AbstractOperationCustomLabelProvider#getImage(org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation)
	 */
	@Override
	public Object getImage(AbstractOperation operation) {
		return adapterFactoryLabelProvider.getImage(operation);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.server.model.provider.AbstractOperationCustomLabelProvider#canRender(org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation)
	 */
	@Override
	public int canRender(AbstractOperation operation) {
		return CAN_RENDER_DEFAULT;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.server.model.provider.AbstractOperationCustomLabelProvider#getModelElementName(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected String getModelElementName(EObject modelElement) {
		if (modelElement == null) {
			return UNKOWN_ELEMENT;
		}

		// TODO: provide sensible label for given model element
		return " \"" + trim(adapterFactoryLabelProvider.getText(modelElement)) + "\"";
	}

	private String trim(Object object) {
		String string = object.toString();
		String result = string.trim();

		if (result.length() == 0) {
			return "(empty name)";
		}

		if (result.length() > MAX_NAME_LENGTH) {
			return result.substring(0, MAX_NAME_LENGTH) + "...";
		}

		return result;
	}

	public void dispose() {
		if (adapterFactory != null) {
			adapterFactory.dispose();
		}
		if (adapterFactoryLabelProvider != null) {
			adapterFactoryLabelProvider.dispose();
		}
	}

}
