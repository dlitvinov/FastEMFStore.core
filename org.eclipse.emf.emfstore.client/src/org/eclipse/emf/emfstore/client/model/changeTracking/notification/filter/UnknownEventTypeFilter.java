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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo;

/**
 * A notification filter that filters all notifications that have an unknow type.
 * All notification with an event type >= {@link Notification#EVENT_TYPE_COUNT} are considered to be unknown.
 * 
 * @author emueller
 * 
 */
public class UnknownEventTypeFilter implements NotificationFilter {

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.NotificationFilter#check(org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo)
	 */
	public boolean check(NotificationInfo notificationInfo) {
		return notificationInfo.getEventType() >= Notification.EVENT_TYPE_COUNT;
	}

}
