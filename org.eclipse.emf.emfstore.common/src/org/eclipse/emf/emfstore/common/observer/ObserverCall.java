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
package org.eclipse.emf.emfstore.common.observer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProxyObserver, returned by the {@link ObserverBus} when calling {@link ObserverBus#notify(Class))}, normally it has
 * the type of the IObserver in order to call observer interface with type safety.
 * However, the same proxies can be casted into {@link ObserverCall} in order to gain all results by the registered
 * observers.
 * 
 * @author wesendon
 * 
 */
public interface ObserverCall {

	/**
	 * Returns the results of each notified observer. This method will always return the returns from the last call
	 * executed on the proxy.
	 * 
	 * @return list of results
	 */
	List<Result> getObserverCallResults();

	/**
	 * This class represents an result from an observer call. It contains the observer, the called method and the result
	 * or an exception, if occured.
	 * 
	 * 
	 * @author wesendon
	 * 
	 */
	public class Result {

		private final IObserver observer;
		private final Method method;
		private final Object result;
		private final Throwable exception;

		/**
		 * This constructor is used if <b>NO</b> exception occurred.
		 * 
		 * @param observer observer
		 * @param method method
		 * @param result result
		 */
		public Result(IObserver observer, Method method, Object result) {
			this.observer = observer;
			this.method = method;
			this.result = result;
			this.exception = null;
		}

		/**
		 * This constructor is used if an exception <b>HAS</b> occurred.
		 * 
		 * @param observer observer
		 * @param e exception
		 * @param method method
		 */
		public Result(IObserver observer, Throwable e, Method method) {
			this.observer = observer;
			this.exception = e;
			this.method = method;
			this.result = null;
		}

		/**
		 * Specifies whether this Result contains an exception.
		 * 
		 * @return boolean
		 */
		public boolean exceptionOccurred() {
			return this.exception != null;
		}

		/**
		 * .
		 * 
		 * @return exception or null
		 */
		public Throwable getException() {
			return this.exception;
		}

		/**
		 * The observer.
		 * 
		 * @return this can't be null
		 */
		public IObserver getObserver() {
			return this.observer;
		}

		/**
		 * The result.
		 * 
		 * @return the result or null
		 */
		public Object getResult() {
			return this.result;
		}

		/**
		 * Returns the result or the default value for primitive types.
		 * 
		 * @return result, null or in case of primitive type, the default value.
		 */
		public Object getResultOrDefaultValue() {
			Object result = getResult();
			if (result == null) {
				result = getDefaultValue(this.method);
			}
			return result;
		}

		/**
		 * Returns the default value for a given method. Which is null or the default primitive value.
		 * 
		 * @param m method
		 * @return null or default primitive value
		 */
		public static Object getDefaultValue(Method m) {
			if (m.getReturnType().isPrimitive()) {
				return DEFAULTPRIMITIVEVALUES.get(m.getReturnType().getSimpleName());
			}
			return null;
		}

		private static final Map<String, Object> DEFAULTPRIMITIVEVALUES = new HashMap<String, Object>();
		static {
			DEFAULTPRIMITIVEVALUES.put("int", new Integer(0));
			DEFAULTPRIMITIVEVALUES.put("boolean", new Boolean(false));
			DEFAULTPRIMITIVEVALUES.put("long", new Long(0));
			DEFAULTPRIMITIVEVALUES.put("float", new Float(0));
			DEFAULTPRIMITIVEVALUES.put("double", new Double(0));
			DEFAULTPRIMITIVEVALUES.put("byte", Byte.MIN_VALUE);
			DEFAULTPRIMITIVEVALUES.put("short", Short.MIN_VALUE);
		}
	}
}
