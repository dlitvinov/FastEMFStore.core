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
package org.eclipse.emf.emfstore.client.ui.views.scm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Content provider for the scm views.
 * 
 * @author emueller
 */
public class SCMContentProvider extends AdapterFactoryContentProvider implements ITreeContentProvider {

	private boolean showRootNodes = true;
	private boolean reverseNodes = true;

	/**
	 * Default constructor.
	 * 
	 * @param treeViewer
	 *            the tree viewer. the project.
	 */
	public SCMContentProvider() {
		super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
	}

	/**
	 * Sets the flag to reverse the order of the nodes. Default value is true -
	 * i.e. the more recent operations are on top.
	 * 
	 * @param reverseNodes
	 *            the new value
	 */
	public void setReverseNodes(boolean reverseNodes) {
		this.reverseNodes = reverseNodes;
	}

	/**
	 * Returns if the nodes should be reversed.
	 * 
	 * @return true if the nodes should be reversed in order
	 */
	public boolean isReverseNodes() {
		return reverseNodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object object) {
		if (object instanceof List<?> && showRootNodes) {
			return ((List<?>) object).toArray();
		} else if (object instanceof List<?>) {
			// valid inputs are a list of HistoryInfos as well as a list
			// of ChangePackage
			List<?> list = (List<?>) object;

			if (list.size() == 0) {
				return list.toArray();
			}

			List<AbstractOperation> result = new ArrayList<AbstractOperation>(list.size());

			if (isListOf(list, HistoryInfo.class)) {
				for (HistoryInfo info : (List<HistoryInfo>) list) {
					if (info.getChangePackage() != null) {
						result.addAll(getReversedOperations(info.getChangePackage()));
					}
				}
			} else {
				for (ChangePackage changePackage : (List<ChangePackage>) list) {
					result.addAll(getReversedOperations(changePackage));
				}
			}

			return result.toArray();
		} else if (object instanceof EObject) {
			return new Object[] { object };
		}

		return super.getElements(object);
	}

	private List<AbstractOperation> getReversedOperations(ChangePackage changePackage) {
		List<AbstractOperation> ops = new ArrayList<AbstractOperation>(changePackage.getOperations());
		Collections.reverse(ops);
		return ops;
	}

	private boolean isListOf(List<?> list, Class<? extends EObject> clazz) {
		Object firstElement = list.get(0);

		return clazz.isInstance(firstElement);
	}

	private Object[] filter(Object[] input, Class<? extends EObject> clazz) {
		List<Object> result = new ArrayList<Object>(input.length);
		for (Object object : input) {
			if (!clazz.isInstance(object)) {
				result.add(object);
			}
		}

		return result.toArray();
	}

	@Override
	public Object[] getChildren(Object object) {

		if (object instanceof HistoryInfo) {
			HistoryInfo historyInfo = (HistoryInfo) object;
			return getChildren(historyInfo.getChangePackage());
		} else if (object instanceof ChangePackage) {
			return filter(super.getChildren(object), LogMessage.class);
		}

		return super.getChildren(object);
	}

	public boolean isShowRootNodes() {
		return showRootNodes;
	}

	public void setShowRootNodes(boolean showRootNodes) {
		this.showRootNodes = showRootNodes;
	}

}
