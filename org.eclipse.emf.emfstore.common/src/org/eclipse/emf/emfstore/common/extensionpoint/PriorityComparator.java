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

import java.util.Comparator;

/**
 * A comparator for {@link ExtensionElement}. This allows to sort the elements in the {@link ExtensionPoint} in order to
 * represent priority of registed elements.
 * 
 * This comparator by default uses a field priority, which is expected to hold an priority number and then sorty by this
 * number.
 * 
 * @author wesendon
 * 
 */
public class PriorityComparator implements Comparator<ExtensionElement> {

	private final String fieldname;
	private final boolean desc;

	/**
	 * Default constructor.
	 */
	public PriorityComparator() {
		this("priority", false);
	}

	/**
	 * Constructor which allows to config the ordering.
	 * 
	 * @param descending if true, priorities are sorted in descending order, ascending otherwise
	 */
	public PriorityComparator(boolean descending) {
		this("priority", descending);
	}

	/**
	 * Constructor allows to config fieldname and ordering.
	 * 
	 * @param fieldname the attribute id of the priority field
	 * @param descending if true, priorities are sorted in descending order, ascending otherwise
	 */
	public PriorityComparator(String fieldname, boolean descending) {
		this.fieldname = fieldname;
		this.desc = descending;

	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(ExtensionElement o1, ExtensionElement o2) {
		try {
			o1.setThrowException(true);
			o2.setThrowException(true);
			return o1.getInteger(this.fieldname).compareTo(o2.getInteger(this.fieldname)) * ((desc) ? -1 : 1);
		} catch (ExtensionPointException e) {
			return 0;
		}
	}

}
