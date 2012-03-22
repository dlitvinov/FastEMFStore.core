package org.eclipse.emf.emfstore.client.test.common.observerbus.assets;

public class BImpl implements B {

	public int returnTwo() {
		return 2;
	}

	public void setMSGToFoo(CImpl tester) {
		tester.msg = "foo";
	}

	public String returnFoobarOrException() {
		throw new IllegalArgumentException();
	}

}
