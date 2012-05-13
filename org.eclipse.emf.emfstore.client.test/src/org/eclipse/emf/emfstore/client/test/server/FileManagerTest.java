package org.eclipse.emf.emfstore.client.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.emfstore.client.model.filetransfer.FileDownloadStatus;
import org.eclipse.emf.emfstore.client.model.filetransfer.FileDownloadStatus.Status;
import org.eclipse.emf.emfstore.client.test.model.requirement.RequirementFactory;
import org.eclipse.emf.emfstore.client.test.model.requirement.UseCase;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.FileIdentifier;
import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
import org.junit.Test;

public class FileManagerTest extends TransmissionTests {

	@Test
	public void testTransfer() throws EmfStoreException, IOException, InterruptedException {
		File file = File.createTempFile("foo", "tmp");
		file.deleteOnExit();
		LogMessage msg = VersioningFactory.eINSTANCE.createLogMessage();
		FileIdentifier id = getProjectSpace1().addFile(file);
		// dummy change, addFile is not recognized as a change
		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
		getProjectSpace1().getProject().addModelElement(useCase);
		getProjectSpace1().commit(msg, null, null);

		getProjectSpace2().update();
		FileDownloadStatus status = getProjectSpace2().getFile(id);
		assertTrue(status != null);
		// wait for file to be completely transferred
		while (status.getStatus() != Status.FINISHED) {
			Thread.sleep(500);
		}
		assertEquals(IOUtils.toByteArray(new FileInputStream(file)),
			IOUtils.toByteArray(new FileInputStream(status.getTransferredFile())));
	}
}
