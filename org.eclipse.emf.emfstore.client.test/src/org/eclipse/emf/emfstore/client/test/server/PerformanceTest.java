/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.client.test.server;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.client.test.SetupHelper;
import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
import org.eclipse.emf.emfstore.common.model.ModelFactory;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.common.model.util.SerializationException;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutator;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorConfiguration;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorUtil;
import org.eclipse.emf.emfstore.server.CleanMemoryTask;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This TestCase tests all methods in the main {@link org.unicase.emfstore.EmfStore} interface.
 * 
 * @author Dmitry Litvinov
 */
public class PerformanceTest extends ServerTests {

	private static final String MODELS_DIR = new File("../../../Models").getAbsolutePath() + '/';
	private static final String OUTPUT_DIR = "../../TestResults/";
	private static final String[] MODELS = new String[] // { "200000" };
	{ "1000", "10000", "50000", "100000", "200000", "500000" };
	private static final int NUM_ITERATIONS = 50;

	private static MemoryMeter memoryMeter;
	private static FileWriter writer;
	private static File file;

	private Usersession usersession;
	private ProjectSpace projectSpace2;
	private final String modelKey = "http://org/eclipse/example/bowling";
	private final int width = 10;
	private final int depth = 2;
	private final long seed = 1234567800;
	private PrimaryVersionSpec version;

	/**
	 * Start server and gain sessionid.
	 * 
	 * @throws EmfStoreException in case of failure
	 * @throws IOException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws EmfStoreException, IOException {
		ServerTests.setUpBeforeClass();
		memoryMeter = new MemoryMeter();
		memoryMeter.start();
	}

	/**
	 * Overrides parent implementation.
	 * 
	 * @throws EmfStoreException in case of failure
	 */
	@Override
	@Before
	public void beforeTest() throws EmfStoreException {

	}

	@AfterClass
	public static void finish() throws EmfStoreException, IOException {
		memoryMeter.finish();
		ServerTests.tearDownAfterClass();
		if (writer != null) {
			writer.close();
		}
	}

	/**
	 * Opens projects of different sizes, shares them with the server and then deletes them. r
	 * 
	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
	 * @throws EmfStoreException in case of failure.
	 */
	@Test
	public void shareProjectTest() throws EmfStoreException {
		for (String modelName : MODELS) {
			final SetupHelper setupHelper = new SetupHelper(MODELS_DIR + modelName + ".ecp");
			setupHelper.setupWorkSpace();
			setupHelper.setupTestProjectSpace();
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					setupHelper.loginServer();
					usersession = setupHelper.getUsersession();
					WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().add(usersession);
				}
			}.run(false);

			final ProjectSpace projectSpace = setupHelper.getTestProjectSpace();

			double[] times = new double[NUM_ITERATIONS];
			long[] memBefore = new long[NUM_ITERATIONS];
			long[] memDuring = new long[NUM_ITERATIONS];
			long[] memAfter = new long[NUM_ITERATIONS];
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				assert (getConnectionManager().getProjectList(usersession.getSessionId()).size() == getProjectsOnServerBeforeTest());
				memoryMeter.startMeasurements();
				memBefore[i] = usedMemory();
				long time = System.currentTimeMillis();
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							if (!usersession.isLoggedIn()) {
								usersession.logIn();
							}
							projectSpace.shareProject(usersession, null);
						} catch (EmfStoreException e) {
							ModelUtil.logException(e);
						}
					}
				}.run(false);
				times[i] = (System.currentTimeMillis() - time) / 1000.0;
				assert (getConnectionManager().getProjectList(usersession.getSessionId()).size() == getProjectsOnServerBeforeTest() + 1);
				assertNotNull(setupHelper.getTestProject());
				memAfter[i] = usedMemory();
				memDuring[i] = memoryMeter.stopMeasurements();
				ModelUtil.logInfo("share project " + modelName + " iteration #" + (i + 1) + ": time=" + times[i]
					+ ", memory used before: " + memBefore[i] / 1024 / 1024 + "MB, during: " + memDuring[i] / 1024
					/ 1024 + "MB, after: " + memAfter[i] / 1024 / 1024 + "MB");
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							getConnectionManager().deleteProject(usersession.getSessionId(),
								projectSpace.getProjectId(), true);
							// WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().remove(usersession);
						} catch (EmfStoreException e) {
							e.printStackTrace();
						}
					}
				}.run(false);
			} // for loop with iterations
			ModelUtil.logInfo("times=" + Arrays.toString(times));
			writedata("Share", modelName, times, memBefore, memDuring, memAfter);
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().remove(usersession);
				}
			}.run(false);
			usersession = null;
			SetupHelper.cleanupWorkspace();
		} // for loop with different models
	}

	private static void writedata(String title, String modelName, double[] times, long[] memBefore, long[] memDuring,
		long[] memAfter) {
		ModelUtil.logInfo(title + " model " + modelName + ": average=" + average(times) + ", min=" + min(times)
			+ ", max=" + max(times) + ", mean=" + mean(times));
		try {
			if (writer == null) {
				File outputDir = new File(OUTPUT_DIR);
				if (!outputDir.exists()) {
					outputDir.mkdir();
				}
				String fileName = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date()) + ".csv";
				file = new File(outputDir, fileName);
				writer = new FileWriter(file);
			}
			String avgTime = ("" + average(times)).replace('.', ',');
			writer.write(title + "\nTime:;" + modelName + ";" + avgTime);
			for (double time : times) {
				String text = (";" + time).replace('.', ',');
				writer.write(text);
			}
			if (memBefore != null) {
				writer.write("\nMem Before:;;");
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					writer.write(";" + memBefore[i]);
				}
			}
			if (memDuring != null) {
				writer.write("\nMem During:;;");
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					writer.write(";" + memDuring[i]);
				}
			}
			if (memAfter != null) {
				writer.write("\nMem After:;;");
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					writer.write(";" + memAfter[i]);
				}
			}
			writer.write("\n\n");
			writer.flush();
		} catch (IOException e) {
			ModelUtil.logException("Error occured while writing to file", e);
		}
	}

	/**
	 * Measures average time, spent for the checkout operation. Opens projects of different sizes, shares them with the
	 * server, checkouts and then deletes them.
	 * 
	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
	 * @throws EmfStoreException in case of failure.
	 */
	@Test
	public void checkoutProjectTest() throws EmfStoreException {
		for (String modelName : MODELS) {
			final SetupHelper setupHelper = new SetupHelper(MODELS_DIR + modelName + ".ecp");
			setupHelper.setupWorkSpace();
			setupHelper.setupTestProjectSpace();

			setupHelper.shareProject();
			assertNotNull(setupHelper.getTestProject());

			double[] times = new double[NUM_ITERATIONS];
			long[] memBefore = new long[NUM_ITERATIONS];
			long[] memDuring = new long[NUM_ITERATIONS];
			long[] memAfter = new long[NUM_ITERATIONS];
			final ProjectSpace projectSpace = setupHelper.getTestProjectSpace();
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				memoryMeter.startMeasurements();
				memBefore[i] = usedMemory();
				long time = System.currentTimeMillis();

				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							projectSpace2 = WorkspaceManager.getInstance().getCurrentWorkspace()
								.checkout(setupHelper.getUsersession(), projectSpace.getProjectInfo());
						} catch (EmfStoreException e) {
							e.printStackTrace();
						}
					}
				}.run(false);
				times[i] = (System.currentTimeMillis() - time) / 1000.0;
				memAfter[i] = usedMemory();
				memDuring[i] = memoryMeter.stopMeasurements();
				ModelUtil.logInfo("checkout project " + projectSpace.getProjectName() + " iteration #" + (i + 1)
					+ ": time=" + times[i] + ", memory used before: " + memBefore[i] / 1024 / 1024 + "MB, during: "
					+ memDuring[i] / 1024 / 1024 + "MB, after: " + memAfter[i] / 1024 / 1024 + "MB");
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							WorkspaceManager.getInstance().getCurrentWorkspace().deleteProjectSpace(projectSpace2);
							projectSpace2 = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.run(false);
			}

			ModelUtil.logInfo("times=" + Arrays.toString(times));
			writedata("Checkout", modelName, times, memBefore, memDuring, memAfter);

			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						getConnectionManager().deleteProject(setupHelper.getUsersession().getSessionId(),
							projectSpace.getProjectId(), true);
					} catch (EmfStoreException e) {
						e.printStackTrace();
					} finally {
						SetupHelper.cleanupServer();
					}
				}
			}.run(false);
		}
	}

	/**
	 * Measures average time, spent for the commit and update operations. Opens projects of different sizes, shares them
	 * with the server and checks it out as two different projects. Then the test generates changes in one of the
	 * projects, using the ModelMutator, commits them to the server, and updates the second project. The test performs
	 * model change, commit and update NUM_ITERATIONS times and calculates times for commit and update operations
	 * 
	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
	 * @throws EmfStoreException in case of failure.
	 */
	@Test
	public void commitAndUpdateProjectTest() throws EmfStoreException {
		for (String modelName : MODELS) {
			final SetupHelper setupHelper1 = new SetupHelper(MODELS_DIR + modelName + ".ecp");
			setupHelper1.setupWorkSpace();
			setupHelper1.setupTestProjectSpace();
			setupHelper1.shareProject();

			final SetupHelper setupHelper2 = new SetupHelper((String) null);
			setupHelper2.setupWorkSpace();
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						setupHelper2.loginServer();
						Usersession usersession2 = setupHelper2.getUsersession();
						setupHelper2.getWorkSpace().getUsersessions().add(usersession2);
						// projectSpace2 = usersession2.checkout(setupHelper1.getTestProjectSpace().getProjectInfo());
						projectSpace2 = WorkspaceManager.getInstance().getCurrentWorkspace()
							.checkout(usersession2, setupHelper1.getTestProjectSpace().getProjectInfo());
					} catch (EmfStoreException e) {
						e.printStackTrace();
					}
				}
			}.run(false);

			final ProjectSpace projectSpace1 = setupHelper1.getTestProjectSpace();
			double[] modelChangeTimes = new double[NUM_ITERATIONS];
			double[] commitTimes = new double[NUM_ITERATIONS];
			double[] updateTimes = new double[NUM_ITERATIONS];
			long[] memBeforeMut = new long[NUM_ITERATIONS];
			long[] memDuringMut = new long[NUM_ITERATIONS];
			long[] memAfterMut = new long[NUM_ITERATIONS];
			// long[] memBeforeComm = new long[NUM_ITERATIONS];
			long[] memDuringCommit = new long[NUM_ITERATIONS];
			long[] memAfterCommit = new long[NUM_ITERATIONS];
			// long[] memBeforeUpd = new long[NUM_ITERATIONS];
			long[] memDuringUpdate = new long[NUM_ITERATIONS];
			long[] memAfterUpdate = new long[NUM_ITERATIONS];
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				// readLine();
				memoryMeter.startMeasurements();
				memBeforeMut[i] = usedMemory();
				long time = System.currentTimeMillis();
				changeModel(projectSpace1);
				modelChangeTimes[i] = (System.currentTimeMillis() - time) / 1000.0;

				memDuringMut[i] = memoryMeter.stopMeasurements();
				memAfterMut[i] = usedMemory();
				ModelUtil.logInfo("change model " + modelName + " iteration #" + (i + 1) + ": time="
					+ modelChangeTimes[i] + " memory used before:" + memBeforeMut[i] / 1024 / 1024 + "MB, during: "
					+ memDuringMut[i] / 1024 / 1024 + "MB, after: " + memAfterMut[i] / 1024 / 1024 + "MB");

				System.out.println("VERSION BEFORE commit:"
					+ projectSpace1.getProjectInfo().getVersion().getIdentifier());
				time = System.currentTimeMillis();
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							projectSpace1.update();
						} catch (EmfStoreException e) {
							e.printStackTrace();
						}
					}
				}.run(false);
				memoryMeter.startMeasurements();
				time = System.currentTimeMillis();

				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							version = projectSpace1.commit(null, null, null);
						} catch (EmfStoreException e) {
							e.printStackTrace();
						}
					}
				}.run(false);

				commitTimes[i] = (System.currentTimeMillis() - time) / 1000.0;

				memDuringCommit[i] = memoryMeter.stopMeasurements();
				memAfterCommit[i] = usedMemory();
				ModelUtil.logInfo("commit project " + modelName + " iteration #" + (i + 1) + ": time=" + commitTimes[i]
					+ ", memory used before: " + memAfterMut[i] / 1024 / 1024 + "MB, during: " + memDuringCommit[i]
					/ 1024 / 1024 + "MB, after: " + memAfterCommit[i] / 1024 / 1024 + "MB");
				System.out.println("VERSION AFTER commit:" + (version != null ? version.getIdentifier() : null));

				memoryMeter.startMeasurements();
				time = System.currentTimeMillis();
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							projectSpace2.update();
						} catch (EmfStoreException e) {
							e.printStackTrace();
						}
					}
				}.run(false);
				updateTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				CleanMemoryTask task = new CleanMemoryTask(WorkspaceManager.getInstance().getCurrentWorkspace()
					.getResourceSet());
				task.run();
				memDuringUpdate[i] = memoryMeter.stopMeasurements();
				memAfterUpdate[i] = usedMemory();
				ModelUtil.logInfo("update project " + modelName + " iteration #" + (i + 1) + ": time=" + updateTimes[i]
					+ ", memory used before: " + memAfterCommit[i] / 1024 / 1024 + "MB, during: " + memDuringUpdate[i]
					/ 1024 / 1024 + "MB, after: " + memAfterUpdate[i] / 1024 / 1024 + "MB");
				version = null;
			}
			ModelUtil.logInfo("Mutate model " + modelName + ": average=" + average(modelChangeTimes) + ", min="
				+ min(modelChangeTimes) + ", max=" + max(modelChangeTimes) + ", mean=" + mean(modelChangeTimes));
			writedata("Commit", modelName, commitTimes, memAfterMut, memDuringCommit, memAfterCommit);
			writedata("Update", modelName, updateTimes, memAfterCommit, memDuringUpdate, memAfterUpdate);

			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						getConnectionManager().deleteProject(setupHelper1.getUsersession().getSessionId(),
							projectSpace1.getProjectId(), true);
						WorkspaceManager.getInstance().getCurrentWorkspace().deleteProjectSpace(projectSpace1);
						WorkspaceManager.getInstance().getCurrentWorkspace().deleteProjectSpace(projectSpace2);
						WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions()
							.remove(setupHelper1.getUsersession());
						WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions()
							.remove(setupHelper2.getUsersession());
					} catch (IOException e) {
						e.printStackTrace();
					} catch (EmfStoreException e) {
						e.printStackTrace();
					} finally {
						SetupHelper.cleanupWorkspace();
						SetupHelper.cleanupServer();
						projectSpace2 = null;
					}
				}
			}.run(false);
		}
	}

	private void readLine() {
		System.out.print("Press Enter...");
		new Scanner(System.in).nextLine();
		System.out.println("ok");
	}

	private long lastSeed = seed + 1;

	public void changeModel(ProjectSpace prjSpace) {
		lastSeed = lastSeed == seed ? seed + 1 : seed;
		final ModelMutatorConfiguration mmc = new ModelMutatorConfiguration(ModelMutatorUtil.getEPackage(modelKey),
			prjSpace.getProject(), lastSeed);
		mmc.setDepth(depth);
		mmc.setWidth(width);
		new EMFStoreCommand() {
			@Override
			protected void doRun() {
				ModelMutator modelChanger = new ModelMutator(mmc);
				long time;
				time = System.currentTimeMillis();
				modelChanger.deleteEObjects(10);
				System.out.println("Delete objects: " + (System.currentTimeMillis() - time) / 1000.0 + "sec");
				time = System.currentTimeMillis();
				modelChanger.createEObjects(10);
				System.out.println("Create objects: " + (System.currentTimeMillis() - time) / 1000.0 + "sec");
				time = System.currentTimeMillis();
				modelChanger.changeAttributes(1000);
				System.out.println("Change Attributes: " + (System.currentTimeMillis() - time) / 1000.0 + "sec");
				time = System.currentTimeMillis();
				modelChanger.changeContainmentReferences(1000);
				System.out.println("Change Containment References: " + (System.currentTimeMillis() - time) / 1000.0
					+ "sec");
				time = System.currentTimeMillis();
				modelChanger.changeCrossReferences(1000);
				System.out.println("Set References: " + (System.currentTimeMillis() - time) / 1000.0 + "sec");
			}
		}.run(false);
		System.out.println("Number of changes: " + prjSpace.getOperations().size());
	}

	public static long usedMemory() {
		Runtime.getRuntime().gc();
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	public static double min(double[] arr) {
		double min = Double.MAX_VALUE;
		for (double x : arr) {
			if (x < min) {
				min = x;
			}
		}
		return min;
	}

	public static double max(double[] arr) {
		double max = Double.MIN_VALUE;
		for (double x : arr) {
			if (x > max) {
				max = x;
			}
		}
		return max;
	}

	public static double average(double[] arr) {
		double sum = 0.0;
		for (double x : arr) {
			sum += x;
		}
		return (int) (sum / arr.length * 1000.0) / 1000.0;
	}

	public static double mean(double[] arr) {
		Arrays.sort(arr);
		int ind = arr.length / 2 - 1 + arr.length % 2;
		return arr[ind];
	}

	/**
	 * Class that measures memory, used during some operation(s) continuously and returns maximal value at the end.
	 */
	public static class MemoryMeter extends Thread {
		/**
		 * Period to wait (in milliseconds) between memory measurements.
		 **/
		private static final int MEASUREMENT_PERIOD = 250;

		private boolean stop = false;
		private boolean active;
		private volatile long maxUsedMemory;

		@Override
		public void run() {
			startMeasurements();
			try {
				while (!stop) {
					if (active) {
						long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if (usedMemory > maxUsedMemory) {
							maxUsedMemory = usedMemory;
						}
					}
					Thread.sleep(MEASUREMENT_PERIOD);
				}
			} catch (InterruptedException e) {
			}
		}

		public void startMeasurements() {
			active = true;
			maxUsedMemory = 0;
		}

		public long stopMeasurements() {
			active = false;
			long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			long curMaxMemory = maxUsedMemory;
			if (usedMemory > curMaxMemory) {
				curMaxMemory = usedMemory;
			}
			return curMaxMemory;
		}

		public void finish() {
			stop = true;
		}
	}

	@Test
	public void saveResourceTest() throws EmfStoreException {
		for (String modelName : MODELS) {
			final SetupHelper setupHelper = new SetupHelper(MODELS_DIR + modelName + ".ecp");
			setupHelper.setupWorkSpace();
			setupHelper.setupTestProjectSpace();
			final Project project = setupHelper.getTestProjectSpace().getProject();

			double[] saveXmlTimes = new double[NUM_ITERATIONS];
			long[] saveXmlMemBefore = new long[NUM_ITERATIONS];
			long[] saveXmlMemDuring = new long[NUM_ITERATIONS];
			double[] loadXmlTimes = new double[NUM_ITERATIONS];
			long[] loadXmlMemBefore = new long[NUM_ITERATIONS];
			long[] loadXmlMemDuring = new long[NUM_ITERATIONS];

			double[] saveBinTimes = new double[NUM_ITERATIONS];
			long[] saveBinMemBefore = new long[NUM_ITERATIONS];
			long[] saveBinMemDuring = new long[NUM_ITERATIONS];
			double[] loadBinTimes = new double[NUM_ITERATIONS];
			long[] loadBinMemBefore = new long[NUM_ITERATIONS];
			long[] loadBinMemDuring = new long[NUM_ITERATIONS];

			double[] saveBinZipTimes = new double[NUM_ITERATIONS];
			long[] saveBinZipMemBefore = new long[NUM_ITERATIONS];
			long[] saveBinZipMemDuring = new long[NUM_ITERATIONS];
			double[] loadBinZipTimes = new double[NUM_ITERATIONS];
			long[] loadBinZipMemBefore = new long[NUM_ITERATIONS];
			long[] loadBinZipMemDuring = new long[NUM_ITERATIONS];

			double[] saveZipTimes = new double[NUM_ITERATIONS];
			long[] saveZipMemBefore = new long[NUM_ITERATIONS];
			long[] saveZipMemDuring = new long[NUM_ITERATIONS];
			double[] loadZipTimes = new double[NUM_ITERATIONS];
			long[] loadZipMemBefore = new long[NUM_ITERATIONS];
			long[] loadZipMemDuring = new long[NUM_ITERATIONS];

			for (int i = 0; i < NUM_ITERATIONS; i++) {
				// ================= XML ==================
				memoryMeter.startMeasurements();
				saveXmlMemBefore[i] = usedMemory();
				try {
					long time = System.currentTimeMillis();
					String string = saveXMLResource(project, ModelUtil.getResourceSaveOptions());
					saveXmlTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
					saveXmlMemDuring[i] = memoryMeter.stopMeasurements();
					System.out.println("String size: " + string.length());

					loadXmlMemBefore[i] = usedMemory();
					memoryMeter.startMeasurements();
					time = System.currentTimeMillis();
					EObject eObject = loadXMLResource(string, ModelUtil.getResourceLoadOptions());
					loadXmlTimes[i] = (System.currentTimeMillis() - time) / 1000.0;

					assert (eObject instanceof Project);
					assertEqual(project, (Project) eObject);
				} catch (SerializationException e1) {
					ModelUtil.logException(e1);
				}
				loadXmlMemDuring[i] = memoryMeter.stopMeasurements();
				ModelUtil.logInfo("save XML " + modelName + " iteration #" + (i + 1) + ": time=" + saveXmlTimes[i]
					+ ", memory used before: " + saveXmlMemBefore[i] / 1024 / 1024 + "MB, during: "
					+ saveXmlMemDuring[i] / 1024 / 1024 + "MB");
				ModelUtil.logInfo("load XML " + modelName + " iteration #" + (i + 1) + ": time=" + loadXmlTimes[i]
					+ ", memory used before: " + loadXmlMemBefore[i] / 1024 / 1024 + "MB, during: "
					+ loadXmlMemDuring[i] / 1024 / 1024 + "MB");

				// // ================= BINARY ==================
				// memoryMeter.startMeasurements();
				// saveBinMemBefore[i] = usedMemory();
				// try {
				// long time = System.currentTimeMillis();
				// Map<Object, Object> saveOptions = new HashMap<Object, Object>();
				// saveOptions.put(XMLResource.OPTION_BINARY, Boolean.TRUE);
				// String string = saveXMLResource(project, saveOptions);
				// saveBinTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				// saveBinMemDuring[i] = memoryMeter.stopMeasurements();
				// System.out.println("String size: " + string.length());
				//
				// loadBinMemBefore[i] = usedMemory();
				// memoryMeter.startMeasurements();
				// time = System.currentTimeMillis();
				// Map<Object, Object> loadOptions = new HashMap<Object, Object>();
				// loadOptions.put(XMLResource.OPTION_BINARY, Boolean.TRUE);
				// EObject eObject = loadXMLResource(string, loadOptions);
				// loadBinTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				//
				// assert (eObject instanceof Project);
				// assertEqual(project, (Project) eObject);
				// } catch (SerializationException e1) {
				// ModelUtil.logException(e1);
				// }
				// loadBinMemDuring[i] = memoryMeter.stopMeasurements();
				// ModelUtil.logInfo("save binary " + modelName + " iteration #" + (i + 1) + ": time=" + saveBinTimes[i]
				// + ", memory used before: " + saveBinMemBefore[i] / 1024 / 1024 + "MB, during: "
				// + saveBinMemDuring[i] / 1024 / 1024 + "MB");
				// ModelUtil.logInfo("load binary " + modelName + " iteration #" + (i + 1) + ": time=" + loadBinTimes[i]
				// + ", memory used before: " + loadBinMemBefore[i] / 1024 / 1024 + "MB, during: "
				// + loadBinMemDuring[i] / 1024 / 1024 + "MB");

				// ================= BINARY ZIP ==================
				// memoryMeter.startMeasurements();
				// saveBinZipMemBefore[i] = usedMemory();
				// try {
				// long time = System.currentTimeMillis();
				// Map<Object, Object> saveOptions = new HashMap<Object, Object>(ModelUtil.getResourceSaveOptions());
				// saveOptions.put(XMLResource.OPTION_BINARY, Boolean.TRUE);
				// saveOptions.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
				// String string = saveXMLResource(project, saveOptions);
				// saveBinZipTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				// saveBinZipMemDuring[i] = memoryMeter.stopMeasurements();
				// System.out.println("String size: " + string.length());
				//
				// loadBinZipMemBefore[i] = usedMemory();
				// memoryMeter.startMeasurements();
				// time = System.currentTimeMillis();
				// Map<Object, Object> loadOptions = new HashMap<Object, Object>(ModelUtil.getResourceLoadOptions());
				// loadOptions.put(XMLResource.OPTION_BINARY, Boolean.TRUE);
				// loadOptions.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
				// EObject eObject = loadXMLResource(string, loadOptions);
				// loadBinZipTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				//
				// assert (eObject instanceof Project);
				// assertEqual(project, (Project) eObject);
				// } catch (SerializationException e1) {
				// ModelUtil.logException(e1);
				// }
				// loadBinZipMemDuring[i] = memoryMeter.stopMeasurements();
				// ModelUtil.logInfo("save binary Zip " + modelName + " iteration #" + (i + 1) + ": time="
				// + saveBinZipTimes[i] + ", memory used before: " + saveBinZipMemBefore[i] / 1024 / 1024
				// + "MB, during: " + saveBinZipMemDuring[i] / 1024 / 1024 + "MB");
				// ModelUtil.logInfo("load binary Zip " + modelName + " iteration #" + (i + 1) + ": time="
				// + loadBinZipTimes[i] + ", memory used before: " + loadBinZipMemBefore[i] / 1024 / 1024
				// + "MB, during: " + loadBinZipMemDuring[i] / 1024 / 1024 + "MB");
				//
				// // ================= BINARY ZIP ==================
				// memoryMeter.startMeasurements();
				// saveZipMemBefore[i] = usedMemory();
				// try {
				// long time = System.currentTimeMillis();
				// Map<Object, Object> saveOptions = new HashMap<Object, Object>(ModelUtil.getResourceSaveOptions());
				// saveOptions.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
				// String string = saveXMLResource(project, saveOptions);
				// saveZipTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				// saveZipMemDuring[i] = memoryMeter.stopMeasurements();
				// System.out.println("String size: " + string.length());
				//
				// loadZipMemBefore[i] = usedMemory();
				// memoryMeter.startMeasurements();
				// time = System.currentTimeMillis();
				// Map<Object, Object> loadOptions = new HashMap<Object, Object>(ModelUtil.getResourceLoadOptions());
				// loadOptions.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
				// EObject eObject = loadXMLResource(string, loadOptions);
				// loadZipTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
				//
				// assert (eObject instanceof Project);
				// assertEqual(project, (Project) eObject);
				// } catch (SerializationException e1) {
				// ModelUtil.logException(e1);
				// }
				// loadZipMemDuring[i] = memoryMeter.stopMeasurements();
				// ModelUtil.logInfo("save Zip " + modelName + " iteration #" + (i + 1) + ": time=" + saveZipTimes[i]
				// + ", memory used before: " + saveZipMemBefore[i] / 1024 / 1024 + "MB, during: "
				// + saveZipMemDuring[i] / 1024 / 1024 + "MB");
				// ModelUtil.logInfo("load Zip " + modelName + " iteration #" + (i + 1) + ": time=" + loadZipTimes[i]
				// + ", memory used before: " + loadZipMemBefore[i] / 1024 / 1024 + "MB, during: "
				// + loadZipMemDuring[i] / 1024 / 1024 + "MB");

				memoryMeter.startMeasurements();
				saveBinMemBefore[i] = usedMemory();
				try {
					long time = System.currentTimeMillis();
					String string = saveBinaryResource(project);
					saveBinTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
					saveBinMemDuring[i] = memoryMeter.stopMeasurements();

					loadBinMemBefore[i] = usedMemory();
					memoryMeter.startMeasurements();
					time = System.currentTimeMillis();
					EObject eObject = loadBinaryResource(string);
					loadBinTimes[i] = (System.currentTimeMillis() - time) / 1000.0;

					assert (eObject instanceof Project);
					assertEqual(project, (Project) eObject);
				} catch (SerializationException e1) {
					ModelUtil.logException(e1);
				}
				loadBinMemDuring[i] = memoryMeter.stopMeasurements();
				ModelUtil.logInfo("save binary resource " + modelName + " iteration #" + (i + 1) + ": time="
					+ saveBinTimes[i] + ", memory used before: " + saveBinMemBefore[i] / 1024 / 1024 + "MB, during: "
					+ saveBinMemDuring[i] / 1024 / 1024 + "MB");
				ModelUtil.logInfo("load binary resource " + modelName + " iteration #" + (i + 1) + ": time="
					+ loadBinTimes[i] + ", memory used before: " + loadBinMemBefore[i] / 1024 / 1024 + "MB, during: "
					+ loadBinMemDuring[i] / 1024 / 1024 + "MB");

			} // for loop with iterations
			ModelUtil.logInfo("Save XML times=" + Arrays.toString(saveXmlTimes));
			writedata("Save XML", modelName, saveXmlTimes, saveXmlMemBefore, saveXmlMemDuring, null);
			ModelUtil.logInfo("Load XML times=" + Arrays.toString(loadXmlTimes));
			writedata("Load XML", modelName, loadXmlTimes, loadXmlMemBefore, loadXmlMemDuring, null);

			// ModelUtil.logInfo("Save Binary times=" + Arrays.toString(saveBinTimes));
			// writedata("Save Binary", modelName, saveBinTimes, saveBinMemBefore, saveBinMemDuring, null);
			// ModelUtil.logInfo("Load Binary times=" + Arrays.toString(loadBinTimes));
			// writedata("Load Binary", modelName, loadBinTimes, loadBinMemBefore, loadBinMemDuring, null);
			//
			// ModelUtil.logInfo("Save Binary Zip times=" + Arrays.toString(saveBinZipTimes));
			// writedata("Save Binary Zip", modelName, saveBinZipTimes, saveBinZipMemBefore, saveBinZipMemDuring, null);
			// ModelUtil.logInfo("Load Binary Zip times=" + Arrays.toString(loadBinZipTimes));
			// writedata("Load Binary Zip", modelName, loadBinZipTimes, loadBinZipMemBefore, loadBinZipMemDuring, null);
			//
			// ModelUtil.logInfo("Save Zip times=" + Arrays.toString(saveZipTimes));
			// writedata("Save Zip", modelName, saveZipTimes, saveZipMemBefore, saveZipMemDuring, null);
			// ModelUtil.logInfo("Load Zip times=" + Arrays.toString(loadZipTimes));
			// writedata("Load Zip", modelName, loadZipTimes, loadZipMemBefore, loadZipMemDuring, null);

			ModelUtil.logInfo("Save Binary resource times=" + Arrays.toString(saveBinTimes));
			writedata("Save Binary resource", modelName, saveBinTimes, saveBinMemBefore, saveBinMemDuring, null);
			ModelUtil.logInfo("Load Binary resource times=" + Arrays.toString(loadBinTimes));
			writedata("Load Binary resource", modelName, loadBinTimes, loadBinMemBefore, loadBinMemDuring, null);

			SetupHelper.cleanupWorkspace();
		} // for loop with different models

	}

	// private static String saveXMLResource(EObject object, boolean overrideContainmentCheck,
	// boolean overrideHrefCheck, boolean overrideProxyCheck) throws SerializationException {

	// private static String saveXMLResource(EObject object) throws SerializationException {
	// XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(ModelUtil.VIRTUAL_URI);
	// ((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
	// EObject copy;
	// if (object instanceof IdEObjectCollection) {
	// copy = ModelUtil.copyIdEObjectCollection((IdEObjectCollection) object, res);
	// } else {
	// copy = ModelUtil.clone(object);
	// res.getContents().add(copy);
	// }
	//
	// // if (!overrideContainmentCheck && !(copy instanceof EClass)) {
	// // if (!CommonUtil.isSelfContained(copy) || !CommonUtil.isContainedInResource(copy, res)) {
	// // throw new SerializationException(copy);
	// // }
	// // }
	//
	// int step = 200;
	// int initialSize = step;
	// if (object instanceof Project) {
	// Project project = (Project) object;
	// initialSize = project.getAllModelElements().size() * step;
	// }
	// // if (!overrideProxyCheck) {
	// // ModelUtil.proxyCheck(res);
	// // }
	//
	// StringWriter stringWriter = new StringWriter(initialSize);
	// URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(stringWriter, "UTF-8");
	// try {
	// res.save(uws, ModelUtil.getResourceSaveOptions());
	// } catch (IOException e) {
	// throw new SerializationException(e);
	// }
	// String result = stringWriter.toString();
	//
	// // if (!overrideHrefCheck) {
	// // ModelUtil.hrefCheck(result);
	// // }
	//
	// return result;
	// }
	//
	// private static EObject loadXMLResource(String object) throws SerializationException {
	// // Resource res = new BinaryResourceImpl();
	// XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(ModelUtil.VIRTUAL_URI);
	// ((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
	// try {
	// // res.load(new InputSource(new StringReader(object)), getResourceLoadOptions());
	// res.load(new InputSource(new StringReader(object)), ModelUtil.getResourceLoadOptions());
	// // res.load(new ByteArrayInputStream(object.getBytes()), getResourceLoadOptions());
	// } catch (UnsupportedEncodingException e) {
	// throw new SerializationException(e);
	// } catch (IOException e) {
	// throw new SerializationException(e);
	// }
	//
	// EObject result = handleParsedEObject(res);
	// // res.getContents().add(result);
	// return result;
	// }

	private static String saveXMLResource(EObject object, Map<?, ?> options) throws SerializationException {
		Resource res;
		int step = 200;
		int initialSize = step;
		if (object instanceof Project) {
			Project project = (Project) object;
			initialSize = project.getAllModelElements().size() * step;
			res = project.eResource();
		} else {
			res = (new ResourceSetImpl()).createResource(ModelUtil.VIRTUAL_URI);
			((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
			res.getContents().add(object);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(initialSize);
		try {
			res.save(outputStream, options);
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		String result = outputStream.toString();

		// if (!overrideHrefCheck) {
		// ModelUtil.hrefCheck(result);
		// }

		return result;
	}

	private static EObject loadXMLResource(String string, Map<?, ?> options) throws SerializationException {
		XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(ModelUtil.VIRTUAL_URI);
		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
		try {
			// res.load(new InputSource(new StringReader(object)), options);
			// res.load(new InputSource(new StringReader(object)), getResourceLoadOptions());
			res.load(new ByteArrayInputStream(string.getBytes()), options);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(e);
		} catch (IOException e) {
			throw new SerializationException(e);
		}

		return handleParsedEObject(res);
	}

	private static String saveBinaryResource(EObject object) throws SerializationException {
		Resource res;
		int step = 200;
		int initialSize = step;
		// if (object instanceof Project) {
		// Project project = (Project) object;
		// initialSize = project.getAllModelElements().size() * step;
		// res = project.eResource();
		// } else {
		res = new BinaryResourceImpl(ModelUtil.VIRTUAL_URI);
		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
		res.getContents().add(object);
		// res.getContents().add()
		// }
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(initialSize);
		try {
			res.save(null);
			// if (before != null && !before.equals(asString(res.getURI().toFileString()))) {
			// System.err.println("The file is changed!");
			// }
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		String result = outputStream.toString();

		// if (!overrideHrefCheck) {
		// ModelUtil.hrefCheck(result);
		// }

		return result;
	}

	private static EObject loadBinaryResource(String string) throws SerializationException {
		BinaryResourceImpl res = new BinaryResourceImpl(ModelUtil.VIRTUAL_URI);
		res.setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
		try {
			// Map<Object, Object> newOptions = new HashMap<Object, Object>(ModelUtil.getResourceLoadOptions());
			// newOptions.put(XMLResource.OPTION_BINARY, Boolean.TRUE);
			// res.load(new InputSource(new StringReader(object)), newOptions);
			// res.load(new InputSource(new StringReader(object)), ModelUtil.getResourceLoadOptions());
			res.load(null);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(e);
		} catch (IOException e) {
			throw new SerializationException(e);
		}

		return handleParsedEObject(res);
	}

	public static EObject handleParsedEObject(XMIResource res) throws SerializationException {
		EObject result = res.getContents().get(0);

		if (result instanceof IdEObjectCollection) {
			IdEObjectCollection collection = (IdEObjectCollection) result;
			Map<EObject, String> eObjectToIdMap = new HashMap<EObject, String>();
			Map<String, EObject> idToEObjectMap = new HashMap<String, EObject>();

			for (EObject modelElement : collection.getAllModelElements()) {
				String modelElementId;
				if (ModelUtil.isIgnoredDatatype(modelElement)) {
					// create random ID for generic types, won't get serialized
					// anyway
					modelElementId = ModelFactory.eINSTANCE.createModelElementId().getId();
				} else {
					modelElementId = res.getID(modelElement);
				}

				if (modelElementId == null) {
					throw new SerializationException("Failed to retrieve ID for EObject contained in project: "
						+ modelElement);
				}

				eObjectToIdMap.put(modelElement, modelElementId);
				idToEObjectMap.put(modelElementId, modelElement);
			}

			collection.initCaches(eObjectToIdMap, idToEObjectMap);
		}

		EcoreUtil.resolveAll(result);
		// res.getContents().remove(result);

		return result;
	}

	public static EObject handleParsedEObject(BinaryResourceImpl res) throws SerializationException {
		EObject result = res.getContents().get(0);

		if (result instanceof IdEObjectCollection) {
			IdEObjectCollection collection = (IdEObjectCollection) result;
			Map<EObject, String> eObjectToIdMap = new HashMap<EObject, String>();
			Map<String, EObject> idToEObjectMap = new HashMap<String, EObject>();

			for (EObject modelElement : collection.getAllModelElements()) {
				String modelElementId;
				// if (ModelUtil.isIgnoredDatatype(modelElement)) {
				// create random ID for generic types, won't get serialized
				// anyway
				modelElementId = ModelFactory.eINSTANCE.createModelElementId().getId();
				// } else {
				// modelElementId = res.getID(modelElement);
				// }

				if (modelElementId == null) {
					throw new SerializationException("Failed to retrieve ID for EObject contained in project: "
						+ modelElement);
				}

				eObjectToIdMap.put(modelElement, modelElementId);
				idToEObjectMap.put(modelElementId, modelElement);
			}

			collection.initCaches(eObjectToIdMap, idToEObjectMap);
		}

		EcoreUtil.resolveAll(result);
		// res.getContents().remove(result);

		return result;
	}
}
