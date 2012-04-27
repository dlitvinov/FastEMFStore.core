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
package org.eclipse.emf.emfstore.server;

import static org.eclipse.emf.emfstore.server.ServerConfiguration.EMFSTORE_HOME;
import static org.eclipse.emf.emfstore.server.ServerConfiguration.isInternalReleaseVersion;
import static org.eclipse.emf.emfstore.server.ServerConfiguration.isReleaseVersion;
import static org.eclipse.emf.emfstore.server.ServerConfiguration.isTesting;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.emfstore.common.model.util.ModelUtil;

/**
 * This is the default workspace location provider. If no other location provider is registered, this provider is used.
 * The default location provider offers profiles, which allows to have multiple workspaces within one root folder.
 * Allowing this isn't mandatory. It is encouraged to subclass this class when implementing an own location provider,
 * since it offers convenience methods. By convention, every path should end with an folder separator char.
 * 
 * @author wesendon
 */
public class DefaultServerWorkspaceLocationProvider implements LocationProvider {

	/**
	 * Get root folder.
	 * 
	 * @return path as string
	 */
	protected String getRootDirectory() {
		String parameter = getStartParameter(EMFSTORE_HOME);
		return (parameter == null) ? addFolders(getUserHome(), ".emfstore", "server") : parameter;
	}

	/**
	 * {@inheritDoc} By default this implementation stores all workspaces in a profile, which can be selected by the
	 * {@link #getSelectedProfile()} method. If you want to use profiles, you should use or override
	 * {@link #getSelectedProfile()}. If you don't want profiles override this method and just return your path.
	 * 
	 * @see org.eclipse.emf.emfstore.server.LocationProvider#getWorkspaceDirectory()
	 */
	public String getWorkspaceDirectory() {
		String rootDirectory = getRootDirectory();
		File file = new File(rootDirectory);

		if (!file.isAbsolute()) {
			String currentDir = new File(".").getAbsolutePath();
			// strip last dot away from path
			currentDir = currentDir.substring(0, currentDir.length() - 1);
			String absolutePath = currentDir + getRootDirectory();
			try {
				// convert to canonical path, since absolutePath still may contain '.' or '..' references
				rootDirectory = new File(absolutePath).getCanonicalPath();
			} catch (IOException e) {
				// fall back to user home as default if path is invalid
				rootDirectory = getUserHome();
				ModelUtil.logWarning("Invalid root directory specified.  Using default " + getUserHome() + ".", e);
			}
		}

		return addFolders(rootDirectory, "profiles", getSelectedProfile());
	}

	/**
	 * If you want to use profiles, use or override this method. By default the application argument
	 * -profile=YourProfileName is checked, otherwise the profile names default, default_dev, default_internal and
	 * default_test are used depending on the application's configuration. This method is called by
	 * {@link #getWorkspaceDirectory()}.
	 * 
	 * @return name of profile
	 */
	protected String getSelectedProfile() {
		String parameter = getStartParameter("-profile");
		if (parameter == null) {
			parameter = "default";
			if (isTesting()) {
				parameter += "_test";
			} else if (!isReleaseVersion()) {
				if (isInternalReleaseVersion()) {
					parameter += "_internal";
				} else {
					parameter += "_dev";
				}
			}
		}
		return parameter;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.emfstore.server.LocationProvider#getBackupDirectory()
	 */
	public String getBackupDirectory() {
		return addFolders(getRootDirectory(), "backup");
	}

	/**
	 * Extracts parameter from application startup arguments. It uses following pattern: parameter+"="+valueOfParameter
	 * 
	 * @param parameter name of parameter
	 * @return value of parameter as string or null
	 */
	protected String getStartParameter(String parameter) {
		return ServerConfiguration.getStartArgument(parameter);
	}

	/**
	 * Convenience method to add folders to a string path.
	 * 
	 * @param path the path
	 * @param folders folder to add
	 * @return new path as string
	 */
	protected static String addFolders(String path, String... folders) {
		StringBuffer result = new StringBuffer(path);
		if (!path.endsWith(File.separator)) {
			result.append(File.separatorChar);
		}
		for (String folder : folders) {
			result.append(folder);
			if (!folder.endsWith(File.separator)) {
				result.append(File.separatorChar);
			}
		}
		return result.toString();
	}

	/**
	 * Return the user home folder.
	 * 
	 * @return the full path as string
	 */
	protected static String getUserHome() {
		StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("user.home"));
		sb.append(File.separatorChar);
		return sb.toString();
	}
}
