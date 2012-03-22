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
package org.eclipse.emf.emfstore.common.extensionpoint;

import static org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint.handleErrorOrNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This is the companion class to {@link ExtensionPoint}. It wrapps a {@link IConfigurationElement} for convenience
 * purposes.
 * As {@link ExtensionPoint} it can be configured to return null or throw an runtime exception
 * {@link ExtensionPointException}
 * 
 * @author wesendon
 * 
 */
public class ExtensionElement {

	private final IConfigurationElement element;
	private boolean exceptionInsteadOfNull;

	/**
	 * Default constructor.
	 * 
	 * @param element element to be wrapped
	 */
	public ExtensionElement(IConfigurationElement element) {
		this(element, false);
	}

	/**
	 * Constructor, allowing to set whether exceptions should be thrown instead of returning null.
	 * 
	 * @param element element to be wrapped
	 * @param throwExceptions if true exceptions are thrown instead of returning null
	 */
	public ExtensionElement(IConfigurationElement element, boolean throwExceptions) {
		this.element = element;
		this.exceptionInsteadOfNull = throwExceptions;
	}

	/**
	 * Returns a Boolean attribute.
	 * 
	 * @param name attribute id
	 * @return Boolean, null or an {@link ExtensionPointException} is thrown
	 */
	public Boolean getBoolean(String name) {
		return Boolean.parseBoolean(getAttribute(name));
	}

	/**
	 * Returns an Integer attribute.
	 * 
	 * @param name attribute id
	 * @return Integer, null or an {@link ExtensionPointException} is thrown
	 */
	public Integer getInteger(String name) {
		try {
			return Integer.parseInt(getAttribute(name));
		} catch (NumberFormatException e) {
			return (Integer) handleErrorOrNull(exceptionInsteadOfNull, e);
		}
	}

	/**
	 * Returns an attribute as string.
	 * 
	 * @param name attribute id
	 * @return String, null or an {@link ExtensionPointException} is thrown
	 */
	public String getAttribute(String name) {
		String attribute = this.element.getAttribute(name);
		if (attribute == null) {
			handleErrorOrNull(exceptionInsteadOfNull, null);
		}
		return attribute;
	}

	/**
	 * Returns a class, or rather the registered instance of this class.
	 * 
	 * @param class_id attribute id
	 * @param returnType expected class type
	 * @return Instance, null or a {@link ExtensionPointException} is thrown
	 */
	@SuppressWarnings("unchecked")
	public <T> T getClass(String class_id, Class<T> returnType) {
		try {
			Object executableExtension = this.element.createExecutableExtension(class_id);
			if (returnType.isInstance(executableExtension)) {
				return (T) executableExtension;
			}
			return (T) handleErrorOrNull(exceptionInsteadOfNull, null);
		} catch (CoreException e) {
			return (T) handleErrorOrNull(exceptionInsteadOfNull, e);
		}
	}

	/**
	 * Returns the wrapped element.
	 * 
	 * @return {@link IConfigurationElement}
	 */
	public IConfigurationElement getIConfigurationElement() {
		return this.element;
	}

	/**
	 * Set wrapper to throw exceptions or otherwise return null.
	 * 
	 * @param throwException if true, exceptions are thrown
	 */
	public void setThrowException(boolean throwException) {
		this.exceptionInsteadOfNull = throwException;
	}

	/**
	 * Returns whether exceptions are thrown or null is returned.
	 * 
	 * @return boolean
	 */
	public boolean getThrowException() {
		return this.exceptionInsteadOfNull;
	}
}