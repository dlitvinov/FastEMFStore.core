package org.eclipse.emf.emfstore.client.model.impl;

import java.util.List;

import org.eclipse.emf.emfstore.common.observer.IObserver;
import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;

public interface OperationRecorderListener extends IObserver {

	void operationsRecorded(List<? extends AbstractOperation> operations);

}
