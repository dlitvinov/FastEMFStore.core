/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */

package org.eclipse.emf.emfstore.client.test;

import org.eclipse.emf.emfstore.client.test.caching.AllCachingTests;
import org.eclipse.emf.emfstore.client.test.changeTracking.AllChangeTrackingTests;
import org.eclipse.emf.emfstore.client.test.common.observerbus.AllObserverBusTests;
import org.eclipse.emf.emfstore.client.test.conflictDetection.AllConflictDetectionTests;
import org.eclipse.emf.emfstore.client.test.integration.AllIntegrationTests;
import org.eclipse.emf.emfstore.client.test.server.AllServerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all tests.
 * 
 * @author koegel
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AllChangeTrackingTests.class, AllConflictDetectionTests.class, AllCachingTests.class,
	AllServerTests.class, AllIntegrationTests.class, AllObserverBusTests.class })
public class AllTests {

}
