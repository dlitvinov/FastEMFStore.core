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
package org.eclipse.emf.emfstore.client.ui.views.changes;

import java.util.List;

import org.eclipse.emf.emfstore.client.ui.views.scm.SCMContentProvider;
import org.eclipse.emf.emfstore.client.ui.views.scm.SCMLabelProvider;
import org.eclipse.emf.emfstore.common.model.Project;
import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * A composite that contains multiple tabs displaying the operation from a different view - e.g. grouped by model
 * element, or ungrouped.
 * 
 * @author Shterev
 */
public class TabbedChangesComposite extends Composite {

	private TabFolder folder;
	private List<ChangePackage> changePackages;
	private Composite tabComposite;
	private TreeViewer compactTabTreeViewer;
	private TreeViewer tabTreeViewer;
	private SCMContentProvider contentProvider;

	/**
	 * Default constructor.
	 * 
	 * @param parent the composite's parent
	 * @param style the style
	 * @param changePackages the input of change packages as a list
	 * @param project the project
	 */
	public TabbedChangesComposite(Composite parent, int style, List<ChangePackage> changePackages, Project project) {
		super(parent, style);

		setLayout(new GridLayout());
		folder = new TabFolder(this, style);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(folder);

		tabComposite = new Composite(folder, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(tabComposite);

		tabTreeViewer = new TreeViewer(tabComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabTreeViewer.getControl());

		contentProvider = new SCMContentProvider();
		contentProvider.setShowRootNodes(true);
		SCMLabelProvider detailedLabelProvider = new SCMLabelProvider(project);
		detailedLabelProvider.setChangePackageVisualizationHelper(new ChangePackageVisualizationHelper(changePackages,
			project));
		tabTreeViewer.setContentProvider(contentProvider);
		tabTreeViewer.setLabelProvider(detailedLabelProvider);
		tabTreeViewer.setInput(changePackages);
		tabTreeViewer.expandToLevel(1);

		TabItem opTab = new TabItem(folder, style);
		opTab.setText("Operations");
		opTab.setControl(tabComposite);
	}

}
