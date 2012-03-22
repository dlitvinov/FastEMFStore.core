package org.eclipse.emf.emfstore.client.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.client.test.WorkspaceTest;
import org.eclipse.emf.emfstore.client.test.testmodel.TestmodelFactory;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.server.CleanMemoryTask;
import org.eclipse.emf.emfstore.server.model.versioning.Version;
import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
import org.junit.Test;

public class CleanMemoryTest extends WorkspaceTest {

	@Test
	public void testUnload() throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		final Resource res = resourceSet.createResource(URI.createFileURI("C:\\Users\\Edgar\\Documents\\test.xmi"));
		final Resource res2 = resourceSet.createResource(URI.createFileURI("C:\\Users\\Edgar\\Documents\\test2.xmi"));
		final Version version = VersioningFactory.eINSTANCE.createVersion();
		final Version nextVersion = VersioningFactory.eINSTANCE.createVersion();
		new EMFStoreCommand() {

			@Override
			protected void doRun() {
				getProject().addModelElement(TestmodelFactory.eINSTANCE.createTestElement());
				version.setProjectState(getProject());
				res.getContents().add(getProject());
				res2.getContents().add(version);
				res2.getContents().add(nextVersion);
				nextVersion.setProjectState(ModelUtil.clone(getProject()));
				version.setNextVersion(nextVersion);
			}
		}.run(false);

		res.save(null);
		res2.save(null);
		CleanMemoryTask task = new CleanMemoryTask(resourceSet);
		task.run();
		// this is wrong?
		assertEquals(2, resourceSet.getResources().size());

		assertEquals(1, version.getProjectState().getModelElements().size());
		assertFalse(version.getProjectState() == getProject());
		assertEquals(2, resourceSet.getResources().size());
	}
}
