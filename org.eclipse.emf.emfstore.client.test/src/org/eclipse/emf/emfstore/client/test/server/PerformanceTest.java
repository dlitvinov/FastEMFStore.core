/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
package org.eclipse.emf.emfstore.client.test.server;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.emfstore.client.model.ProjectSpace;
import org.eclipse.emf.emfstore.client.model.Usersession;
import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
import org.eclipse.emf.emfstore.client.test.SetupHelper;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutator;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorConfiguration;
import org.eclipse.emf.emfstore.modelmutator.api.ModelMutatorUtil;
import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * This TestCase tests all methods in the main {@link org.unicase.emfstore.EmfStore} interface.
 * 
 * @author Dmitry Litvinov
 */
public class PerformanceTest extends ServerTests {

	private static final String MODELS_DIR = "C:\\EMFStore_workspace\\Models\\";
	private static final String[] MODELS = new String[] { "200000" };// , "10000", "50000", "100000", "200000", "500000"
																		// };
	private static final int NUM_ITERATIONS = 1000;
	private static FileWriter writer;
	private static File file;

	private Usersession usersession;
	private ProjectSpace projectSpace2;

	@AfterClass
	public static void finish() throws EmfStoreException, IOException {
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
			MemoryMeter memoryMeter = new MemoryMeter();
			memoryMeter.start();
			// try {
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				assert (getConnectionManager().getProjectList(usersession.getSessionId()).size() == getProjectsOnServerBeforeTest());
				memoryMeter.startMeasurments();
				memBefore[i] = usedMemory();
				long time = System.currentTimeMillis();
				// setupHelper.shareProject();
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						try {
							// if (usersession == null) {
							// usersession = ModelFactory.eINSTANCE.createUsersession();
							// ServerInfo serverInfo = SetupHelper.getServerInfo();
							// usersession.setServerInfo(serverInfo);
							// usersession.setUsername("super");
							// usersession.setPassword("super");
							// WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().add(usersession);
							// }
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
				memDuring[i] = memoryMeter.stopMeasurments();
				ModelUtil.logInfo("share project " + modelName + " iteration #" + (i + 1) + ": time=" + times[i]
					+ ", memory used before: " + memBefore[i] / 1024 / 1024 + "MB, during: " + memDuring[i] / 1024
					/ 1024 + "MB, after: " + memAfter[i] / 1024 / 1024 + "MB");
				// assertEqual(projectSpace.getProject(),
				// getConnectionManager().getProject(getSessionId(), projectInfo.getProjectId(),
				// VersionSpec.HEAD_VERSION));
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
				// } finally {
			ModelUtil.logInfo("times=" + Arrays.toString(times));
			ModelUtil.logInfo("shareProjectTest(): model " + modelName + ": average=" + average(times) + ", min="
				+ min(times) + ", max=" + max(times) + ", mean=" + mean(times));
			writedata("Share", modelName, times, memBefore, memDuring, memAfter);
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().remove(usersession);
				}
			}.run(false);
			usersession = null;
			SetupHelper.cleanupWorkspace();
			// }

		} // for loop with different models
	}

	private static void writedata(String title, String modelName, double[] times, long[] memBefore, long[] memDuring,
		long[] memAfter) {
		try {
			if (writer == null) {
				String fileName = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date()) + ".csv";
				file = new File(fileName);
				writer = new FileWriter(file);
			}
			writer.write(title + "\nTime:;" + modelName + ";" + average(times));
			for (double time : times) {
				String text = (";" + time).replace('.', ',');
				writer.write(text);
			}
			writer.write("\nMem Before:;;");
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				writer.write(";" + memBefore[i]);
			}
			if (memDuring != null) {
				writer.write("\nMem During:;;");
				for (int i = 0; i < NUM_ITERATIONS; i++) {
					writer.write(";" + memDuring[i]);
				}
			}
			writer.write("\nMem After:;;");
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				writer.write(";" + memAfter[i]);
			}
			writer.write("\n\n");
			writer.flush();
		} catch (IOException e) {
			ModelUtil.logException("Error occured while writing to file", e);
		}
	}

	// private double shareProject(ProjectSpace projectSpace) throws EmfStoreException {
	// assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest());
	//
	// long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	// long time = System.currentTimeMillis();
	// projectSpace.shareProject(projectSpace.getUsersession());
	// // ProjectInfo projectInfo = getConnectionManager().createProject(getSessionId(), projectSpace.getProjectName(),
	// // "TestProject", SetupHelper.createLogMessage("super", "a logmessage"), projectSpace.getProject());
	// time = System.currentTimeMillis() - time;
	// long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	// ModelUtil.logInfo("shareProject(): model " + projectSpace.getProject() + ": time=" + time / 1000.0
	// + ", memory used before: " + memBefore / 1024 / 1024 + "MB, after: " + memAfter / 1024 / 1024);
	//
	// assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest() + 1);
	// assertNotNull(projectSpace.getProject());
	// // assertEqual(projectSpace.getProject(),
	// // getConnectionManager().getProject(getSessionId(), projectInfo.getProjectId(), VersionSpec.HEAD_VERSION));
	// getConnectionManager().deleteProject(getSessionId(), projectSpace.getProjectId(), true);
	//
	// return time / 1000.0;
	// }

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
			// final ProjectInfo projectInfo = setupHelper.getTestProjectSpace().getProjectInfo();
			// try {
			// WorkspaceManager.getInstance().getCurrentWorkspace()
			// .deleteProjectSpace(setupHelper.getTestProjectSpace());
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			// SessionId sessionId = setupHelper.getUsersession().getSessionId();
			// ProjectInfo projectInfo = getConnectionManager().createProject(getSessionId(),
			// projectSpace.getProjectName(), "TestProject",
			// SetupHelper.createLogMessage("super", "a logmessage"), projectSpace.getProject());
			// assertTrue(getConnectionManager().getProjectList(sessionId).size() == getProjectsOnServerBeforeTest() +
			// 1);
			assertNotNull(setupHelper.getTestProject());
			// assertEqual(projectSpace.getProject(),
			// getConnectionManager().getProject(getSessionId(), projectInfo.getProjectId(),
			// VersionSpec.HEAD_VERSION));

			double[] times = new double[NUM_ITERATIONS];
			long[] memBefore = new long[NUM_ITERATIONS];
			long[] memDuring = new long[NUM_ITERATIONS];
			long[] memAfter = new long[NUM_ITERATIONS];
			MemoryMeter memoryMeter = new MemoryMeter();
			memoryMeter.start();
			final ProjectSpace projectSpace = setupHelper.getTestProjectSpace();
			// final Usersession usersession = setupHelper.getUsersession();
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				memoryMeter.startMeasurments();
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
				// setupHelper.getUsersession().checkout(projectSpace.getProjectInfo());
				times[i] = (System.currentTimeMillis() - time) / 1000.0;
				memAfter[i] = usedMemory();
				memDuring[i] = memoryMeter.stopMeasurments();
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
			ModelUtil.logInfo("checkoutProjectTest(): model " + modelName + ": average=" + average(times) + ", min="
				+ min(times) + ", max=" + max(times) + ", mean=" + mean(times));
			writedata("Checkout", modelName, times, memBefore, memDuring, memAfter);

			// SessionId sessionId = setupHelper.getUsersession().getSessionId();
			// getConnectionManager().deleteProject(sessionId, projectSpace.getProjectId(), true);

			// assertTrue(getConnectionManager().getProjectList(sessionId).size() == getProjectsOnServerBeforeTest());
			new EMFStoreCommand() {
				@Override
				protected void doRun() {
					try {
						getConnectionManager().deleteProject(setupHelper.getUsersession().getSessionId(),
							projectSpace.getProjectId(), true);
						// WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions()
						// .remove(setupHelper.getUsersession());
					} catch (EmfStoreException e) {
						e.printStackTrace();
					} finally {
						SetupHelper.cleanupServer();
					}
				}
			}.run(false);
		}
	}

	private String modelKey = "http://org/eclipse/example/bowling";
	private int width = 10;
	private int depth = 4;
	private long seed = 1234567800;
	private PrimaryVersionSpec version;

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

			// SessionId sessionId = setupHelper.getUsersession().getSessionId();
			// ProjectInfo projectInfo = getConnectionManager().createProject(getSessionId(),
			// projectSpace.getProjectName(), "TestProject",
			// SetupHelper.createLogMessage("super", "a logmessage"), projectSpace.getProject());
			// assertTrue(getConnectionManager().getProjectList(sessionId).size() == getProjectsOnServerBeforeTest() +
			// 1);
			// assertNotNull(setupHelper.getTestProject());
			// assertEqual(projectSpace.getProject(),
			// getConnectionManager().getProject(getSessionId(), projectInfo.getProjectId(),
			// VersionSpec.HEAD_VERSION));

			final ProjectSpace projectSpace1 = setupHelper1.getTestProjectSpace();
			final Project project1 = projectSpace1.getProject();
			double[] modelChangeTimes = new double[NUM_ITERATIONS];
			double[] commitTimes = new double[NUM_ITERATIONS];
			double[] updateTimes = new double[NUM_ITERATIONS];
			long[] memBeforeMut = new long[NUM_ITERATIONS];
			long[] memAfterMut = new long[NUM_ITERATIONS];
			// long[] memBeforeComm = new long[NUM_ITERATIONS];
			long[] memAfterComm = new long[NUM_ITERATIONS];
			// long[] memBeforeUpd = new long[NUM_ITERATIONS];
			long[] memAfterUpd = new long[NUM_ITERATIONS];
			for (int i = 0; i < NUM_ITERATIONS; i++) {
				memBeforeMut[i] = usedMemory();
				long time = System.currentTimeMillis();
				final ModelMutatorConfiguration mmc = new ModelMutatorConfiguration(
					ModelMutatorUtil.getEPackage(modelKey), project1, seed);
				mmc.setDepth(depth);
				mmc.setWidth(width);
				List<EStructuralFeature> eStructuralFeaturesToIgnore = new ArrayList<EStructuralFeature>();
				eStructuralFeaturesToIgnore.addAll(project1.eClass().getEAllContainments());
				eStructuralFeaturesToIgnore.remove(project1.eClass().getEStructuralFeature("modelElements"));
				mmc.seteStructuralFeaturesToIgnore(eStructuralFeaturesToIgnore);
				new EMFStoreCommand() {
					@Override
					protected void doRun() {
						ModelMutator.changeModel(mmc);
					}
				}.run(false);
				modelChangeTimes[i] = (System.currentTimeMillis() - time) / 1000.0;

				memAfterMut[i] = usedMemory();
				ModelUtil.logInfo("change model " + modelName + " iteration #" + (i + 1) + ": time="
					+ modelChangeTimes[i] + " memory used before:" + memBeforeMut[i] / 1024 / 1024 + "MB, after: "
					+ memAfterMut[i] / 1024 / 1024 + "MB");

				System.out.println("VERSION BEFORE commit:"
					+ projectSpace1.getProjectInfo().getVersion().getIdentifier());
				// memBeforeComm[i] = usedMemory();
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

				memAfterComm[i] = usedMemory();
				System.out.println("VERSION AFTER commit:" + (version != null ? version.getIdentifier() : null));
				ModelUtil.logInfo("commit project " + modelName + " iteration #" + (i + 1) + ": time=" + commitTimes[i]
					+ ", memory used before: " + memAfterMut[i] / 1024 / 1024 + "MB, after: " + memAfterComm[i] / 1024
					/ 1024 + "MB");

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

				memAfterUpd[i] = usedMemory();
				ModelUtil.logInfo("update project " + modelName + " iteration #" + (i + 1) + ": time=" + updateTimes[i]
					+ ", memory used before: " + memAfterComm[i] / 1024 / 1024 + "MB, after: " + memAfterUpd[i] / 1024
					/ 1024 + "MB");
				version = null;
			}
			ModelUtil.logInfo("commitAndUpdateProjectTest(): change model " + modelName + ": average="
				+ average(modelChangeTimes) + ", min=" + min(modelChangeTimes) + ", max=" + max(modelChangeTimes)
				+ ", mean=" + mean(modelChangeTimes));
			ModelUtil.logInfo("commitAndUpdateProjectTest(): commit project " + modelName + ": average="
				+ average(commitTimes) + ", min=" + min(commitTimes) + ", max=" + max(commitTimes) + ", mean="
				+ mean(commitTimes));
			ModelUtil.logInfo("commitAndUpdateProjectTest(): update project " + modelName + ": average="
				+ average(updateTimes) + ", min=" + min(updateTimes) + ", max=" + max(updateTimes) + ", mean="
				+ mean(updateTimes));
			writedata("Commit", modelName, commitTimes, memAfterMut, null, memAfterComm);
			writedata("Update", modelName, updateTimes, memAfterComm, null, memAfterUpd);

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

			// SessionId sessionId = setupHelper.getUsersession().getSessionId();
			// getConnectionManager().deleteProject(sessionId, projectSpace.getProjectId(), true);

			// assertTrue(getConnectionManager().getProjectList(sessionId).size() == getProjectsOnServerBeforeTest());
		}

		SetupHelper.cleanupWorkspace();
		SetupHelper.cleanupServer();
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
	class MemoryMeter extends Thread {
		/**
		 * Period to wait (in milliseconds) between memory measurements.
		 **/
		public static final int MEASURMENT_PERIOD = 250;

		private boolean running;
		private long maxUsedMemory;

		@Override
		public void run() {
			startMeasurments();
			try {
				while (true) {
					Thread.sleep(MEASURMENT_PERIOD);
					if (running) {
						long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
						if (usedMemory > maxUsedMemory) {
							maxUsedMemory = usedMemory;
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}

		public void startMeasurments() {
			running = true;
			maxUsedMemory = 0;
		}

		public long stopMeasurments() {
			running = false;
			return maxUsedMemory;
		}
	}
}
