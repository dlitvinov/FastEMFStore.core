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
package org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo;
import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;

/**
 * This class filters a notification recording according to predefined stacks of
 * filters. Available stacks are created as static properties for easy
 * reference.
 * 
 * @author chodnick
 */
public final class FilterStack implements NotificationFilter {

	private static final NotificationFilter[] DEFAULT_STACK = { new TouchFilter(), new TransientFilter(),
		new UnknownEventTypeFilter(), new EmptyRemovalsFilter(), new IgnoreDatatypeFilter(),
		new IgnoreOutsideProjectReferencesFilter(), new IgnoreNullFeatureNotificationsFilter(),
		new NotifiableIdEObjectCollectionFilter() };

	/**
	 * The default filter stack.
	 */
	public static final NotificationFilter DEFAULT = new FilterStack(DEFAULT_STACK);

	private List<NotificationFilter> filterList;

	/**
	 * Constructor.
	 * 
	 * @param filters the filter the filter stack should consists of.
	 */
	public FilterStack(NotificationFilter[] filters) {
		filterList = new LinkedList<NotificationFilter>();
		Collections.addAll(filterList, filters);
		collectExtensionPoints();

	}

	private void collectExtensionPoints() {
		for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.client.notificationFilter", true)
			.getExtensionElements()) {
			try {
				filterList.add(element.getClass("class", NotificationFilter.class));
			} catch (ExtensionPointException e) {
				WorkspaceUtil.logException(e.getMessage(), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.NotificationFilter#check(org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo)
	 */
	public boolean check(NotificationInfo notificationInfo) {
		for (NotificationFilter f : filterList) {
			if (f.check(notificationInfo)) {
				return true;
			}
		}
		return false;
	}

}
