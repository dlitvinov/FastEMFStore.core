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

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.model.ProjectId;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerEvent;
import org.eclipse.emf.emfstore.server.model.versioning.events.server.ServerProjectEvent;

/**
 * EventManager accepts events and distributes them to the listeners.
 * EventManger runs in it's own thread.<br />
 * <br />
 * <b>TODO</b>: Enable listener to listen to specified events only.<br/>
 * <b>TODO</b>: Don't allow listeners to block eventmanager (e.g. connection timeout)
 * 
 * @author wesendon
 */
public final class EventManager extends Thread {

	/**
	 * Initializes the singleton instance statically.
	 */
	private static class SingletonHolder {
		public static final EventManager INSTANCE = new EventManager();
	}

	/**
	 * Returns the instance of {@link EventManager}.
	 * 
	 * @return instance
	 */
	public static EventManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private LinkedBlockingQueue<ServerEvent> queue;
	private ArrayList<ListenerContainer> listeners;

	/**
	 * Private constructor.
	 */
	private EventManager() {
		super("EventManager");
		queue = new LinkedBlockingQueue<ServerEvent>();
		listeners = new ArrayList<ListenerContainer>();
		start();
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ArrayList<ListenerContainer> tmp = new ArrayList<ListenerContainer>();
		while (!isInterrupted()) {
			try {
				ServerEvent event = queue.take();
				if (event != null) {
					synchronized (this) {
						for (ListenerContainer e : listeners) {
							boolean successful = e.handleEvent(ModelUtil.clone(event));
							if (!successful) {
								tmp.add(e);
							}
						}
						listeners.removeAll(tmp);
						tmp.clear();
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Register a listener. Listen to specific type of event isn't implemented
	 * yet.
	 * 
	 * @param listener
	 *            the listener
	 * @param projectId
	 *            project id, can be null
	 * @param clazz
	 *            not implemented yet
	 */
	public void registerListener(EMFStoreEventListener listener, ProjectId projectId, EClass clazz) {
		if (listener == null) {
			return;
		}
		synchronized (this) {
			listeners.add(new ListenerContainer(listener, projectId, clazz));
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 *            a listener
	 */
	public void unregisterListener(EMFStoreEventListener listener) {
		if (listener == null) {
			return;
		}
		synchronized (this) {
			listeners.remove(listener);
		}
	}

	/**
	 * Use this method to distribute your event.
	 * 
	 * @param event
	 *            the event
	 */
	public void sendEvent(ServerEvent event) {
		if (event != null) {
			try {
				// queue is thread safe
				queue.put(event);
			} catch (InterruptedException e) {
				// fail silently
			}
		}
	}

	/**
	 * Container for listener.
	 * 
	 * @author wesendon
	 */
	private class ListenerContainer {

		private final EMFStoreEventListener listener;
		private final ProjectId projectId;
		@SuppressWarnings("unused")
		private final EClass clazz;

		public ListenerContainer(EMFStoreEventListener listener, ProjectId projectId, EClass clazz) {
			this.listener = listener;
			this.projectId = projectId;
			this.clazz = clazz;
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
}
