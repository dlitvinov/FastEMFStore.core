package org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo;

public class UnknownEventTypeFilter implements NotificationFilter {

	public boolean check(NotificationInfo notificationInfo) {
		return notificationInfo.getEventType() >= Notification.EVENT_TYPE_COUNT;
	}

}
