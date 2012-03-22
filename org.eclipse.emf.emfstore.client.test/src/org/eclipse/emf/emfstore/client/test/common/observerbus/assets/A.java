package org.eclipse.emf.emfstore.client.test.common.observerbus.assets;

import org.eclipse.emf.emfstore.common.observer.IObserver;

public interface A extends IObserver {

	public int returnTwo();

	public String returnFoobarOrException();
}
