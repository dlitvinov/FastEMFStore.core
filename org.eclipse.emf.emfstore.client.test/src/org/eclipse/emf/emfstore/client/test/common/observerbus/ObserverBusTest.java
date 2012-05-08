package org.eclipse.emf.emfstore.client.test.common.observerbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.A;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.AImpl;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.B;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.BImpl;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.C;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.CImpl;
import org.eclipse.emf.emfstore.client.test.common.observerbus.assets.DImpl;
import org.eclipse.emf.emfstore.common.observer.ObserverBus;
import org.eclipse.emf.emfstore.common.observer.ObserverCall;
import org.eclipse.emf.emfstore.common.observer.ObserverCall.Result;
import org.junit.Before;
import org.junit.Test;

public class ObserverBusTest {

	private ObserverBus observerBus;

	@Test
	public void testUnregister() {
		C observer = new C() {
			public String fourtyTwo() {
				return "42";
			}
		};
		getObserverBus().register(observer);
		assertEquals("42", getObserverBus().notify(C.class).fourtyTwo());
		getObserverBus().unregister(observer);
		assertFalse("42".equals(getObserverBus().notify(C.class).fourtyTwo()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSuperUnregister() {
		DImpl d = new DImpl();
		getObserverBus().register(d, C.class);
		assertEquals("42", getObserverBus().notify(C.class).fourtyTwo());
		getObserverBus().unregister(d);
		assertFalse("42".equals(getObserverBus().notify(C.class).fourtyTwo()));
	}

	@Before
	public void reset() {
		observerBus = new ObserverBus();
	}

	private ObserverBus getObserverBus() {
		return observerBus;
	}

	@Test
	public void simpleObserverTest() {
		getObserverBus().register(new AImpl());
		assertEquals(getObserverBus().notify(A.class).returnTwo(), 2);
	}

	@Test
	public void simpleNoObserverTest() {
		// Default value for Int is returned (=0)
		// Would exception be better?
		assertEquals(getObserverBus().notify(A.class).returnTwo(), 0);
	}

	@Test
	public void simpleVoidObserverTest() {
		getObserverBus().register(new BImpl());
		CImpl tester = new CImpl();
		getObserverBus().notify(B.class).setMSGToFoo(tester);
		assertEquals(tester.msg, "foo");
	}

	@Test
	public void simpleWithTwoObserverTest() {
		getObserverBus().register(new AImpl());
		getObserverBus().register(new AImpl());
		A observerProxy = getObserverBus().notify(A.class);
		assertEquals(observerProxy.returnTwo(), 2);
		List<Result> callResults = ((ObserverCall) observerProxy).getObserverCallResults();
		assertEquals(callResults.size(), 2);
		assertEquals(callResults.get(0).getResult(), 2);
		assertEquals(callResults.get(1).getResult(), 2);
	}

	@Test
	public void simpleObserverInheritanceTest() {
		getObserverBus().register(new AImpl());
		// B inherits from A
		getObserverBus().register(new BImpl());
		A observerProxy = getObserverBus().notify(A.class);
		assertEquals(observerProxy.returnTwo(), 2);
		List<Result> callResults = ((ObserverCall) observerProxy).getObserverCallResults();
		assertEquals(callResults.size(), 2);
		assertEquals(callResults.get(0).getResult(), 2);
		assertEquals(callResults.get(1).getResult(), 2);
	}

	@Test
	public void simpleObserverInheritanceAndUnRegAllTest() {
		getObserverBus().register(new AImpl());
		// B inherits from A
		BImpl b = new BImpl();
		getObserverBus().register(b);
		getObserverBus().unregister(b);

		A observerProxy = getObserverBus().notify(A.class);
		assertEquals(observerProxy.returnTwo(), 2);
		List<Result> callResults = ((ObserverCall) observerProxy).getObserverCallResults();
		assertEquals(callResults.size(), 1);
		assertEquals(callResults.get(0).getResult(), 2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simpleObserverInheritanceAndUnRegSubTest() {
		getObserverBus().register(new AImpl());
		// B inherits from A
		BImpl b = new BImpl();
		getObserverBus().register(b);
		getObserverBus().unregister(b, B.class);

		A observerProxy = getObserverBus().notify(A.class);
		assertEquals(observerProxy.returnTwo(), 2);
		List<Result> callResults = ((ObserverCall) observerProxy).getObserverCallResults();
		assertEquals(callResults.size(), 2);
		assertEquals(callResults.get(0).getResult(), 2);
		assertEquals(callResults.get(1).getResult(), 2);
	}

	@Test
	public void callObserverException() {
		getObserverBus().register(new AImpl());
		getObserverBus().register(new BImpl());

		A proxy = getObserverBus().notify(A.class);
		proxy.returnFoobarOrException();

		List<Result> results = ((ObserverCall) proxy).getObserverCallResults();
		assertEquals(results.size(), 2);
		assertFalse(results.get(0).exceptionOccurred());
		assertTrue(results.get(1).exceptionOccurred());
	}

	@Test
	public void registerMultipleCallOne() {
		getObserverBus().register(new AImpl());
		getObserverBus().register(new BImpl());
		getObserverBus().register(new CImpl());

		getObserverBus().notify(C.class).fourtyTwo();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerForOneInterfaceTest() {
		getObserverBus().register(new BImpl(), B.class);

		A a = getObserverBus().notify(A.class);
		B b = getObserverBus().notify(B.class);

		a.returnTwo();
		assertTrue(((ObserverCall) a).getObserverCallResults().size() == 0);
		b.returnTwo();
		assertTrue(((ObserverCall) b).getObserverCallResults().size() == 1);
	}

	public String fourtyTwo() {
		return "42";
	}
}
