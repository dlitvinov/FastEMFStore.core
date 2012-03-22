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
package org.eclipse.emf.emfstore.client.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.emfstore.client.model.Configuration;
import org.eclipse.emf.emfstore.common.model.util.ModelUtil;

/**
 * Implements a list for a containment reference that will automatically split into several resources and save them
 * whenever necessary due to operations on the list. The containmentList is part of a root object that holds the
 * containment list. Elements added to the list will be saved to files in the given path with a randomly assigned names
 * and the given extension. The list does not support the set method.
 * 
 * @author koegel
 * @param <T>
 */
public class AutoSplitAndSaveResourceContainmentList<T extends EObject> implements List<T> {

	private static final String ROOT_NAME = "root";
	private static final int MAX_CAPACITY = 30;
	private static final int MAX_FILE_SIZE = 100000;
	private final EList<T> list;
	private Resource currentResource;
	private int currentResourceElementCount;
	private final ResourceSet resourceSet;
	private final String path;
	private final String extension;
	private Resource rootResource;
	private HashSet<Resource> dirtyResourceSet;

	/**
	 * Constructor.
	 * 
	 * @param root parent element of list
	 * @param list the containment list
	 * @param resourceSet the resourceSet to create the resources in
	 * @param path the path for new resources
	 * @param extension the file extension for new resources
	 */
	public AutoSplitAndSaveResourceContainmentList(EObject root, EList<T> list, ResourceSet resourceSet, String path,
		String extension) {
		if (list == null || resourceSet == null || path == null || extension == null || root == null) {
			throw new IllegalArgumentException();
		}
		this.resourceSet = resourceSet;
		this.dirtyResourceSet = new HashSet<Resource>();
		this.path = path;
		this.extension = extension;
		this.list = list;

		Resource eResource = root.eResource();
		if (eResource == null) {
			URI fileURI = URI.createFileURI(path + File.separatorChar + ROOT_NAME + extension);
			rootResource = resourceSet.createResource(fileURI);
			rootResource.getContents().add(root);
			markAsDirty(rootResource);
			saveDirtyResources();
		} else {
			rootResource = eResource;
		}

		// init first resource
		initCurrentResource(resourceSet);
	}

	private void saveDirtyResources() {
		if (Configuration.isAutoSaveEnabled()) {
			save();
		}
	}

	private void initCurrentResource(ResourceSet resourceSet) {
		currentResource = createRandomResource(resourceSet, this.path);
		currentResourceElementCount = 0;
	}

	private Resource createRandomResource(ResourceSet resourceSet, String path) {
		URI fileURI = URI.createFileURI(path + File.separatorChar + UUID.randomUUID().toString() + extension);
		return resourceSet.createResource(fileURI);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		addToResource(element);
		list.add(index, element);
		markAsDirty(rootResource);
		saveDirtyResources();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(T o) {
		addToResource(o);
		boolean result = list.add(o);
		markAsDirty(rootResource);
		saveDirtyResources();
		return result;
	}

	private void addToResource(T o) {
		if (o.eResource() != null) {
			return;
		}

		URI uri = currentResource.getURI();
		File file = new File(uri.toFileString());

		if (currentResourceElementCount > MAX_CAPACITY || file.length() > MAX_FILE_SIZE) {
			currentResource = createRandomResource(resourceSet, path);
			currentResourceElementCount = 0;
		}

		currentResource.getContents().add(o);
		markAsDirty(currentResource);
		currentResourceElementCount += 1;
	}

	private void removeFromResource(EObject o) {
		Resource eResource = o.eResource();
		if (eResource == null) {
			return;
		}
		eResource.getContents().remove(o);
		markAsDirty(eResource);
		if (eResource == currentResource) {
			currentResourceElementCount -= 1;
		}
	}

	private void markAsDirty(Resource resource) {
		dirtyResourceSet.add(resource);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		for (T element : c) {
			addToResource(element);
		}
		boolean result = list.addAll(c);
		markAsDirty(rootResource);
		saveDirtyResources();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		for (T element : c) {
			addToResource(element);
		}
		boolean result = list.addAll(index, c);
		markAsDirty(rootResource);
		saveDirtyResources();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#clear()
	 */
	public void clear() {
		list.clear();
		File dir = new File(path);
		File[] listFiles = dir.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.isDirectory() || !file.getName().endsWith(extension)) {
					continue;
				}
				if (file.getName().endsWith(ROOT_NAME + extension)) {
					continue;
				}
				file.delete();
			}
		}
		initCurrentResource(resourceSet);
		markAsDirty(rootResource);
		saveDirtyResources();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return list.get(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#iterator()
	 */
	public Iterator<T> iterator() {
		return list.iterator();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#remove(int)
	 */
	public T remove(int index) {
		T t = list.remove(index);
		removeFromResource(t);
		markAsDirty(rootResource);
		saveDirtyResources();
		return t;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		boolean remove = list.remove(o);
		if (o instanceof EObject) {
			removeFromResource((T) o);
		}
		markAsDirty(rootResource);
		saveDirtyResources();
		return remove;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		boolean result = list.removeAll(c);
		for (Object o : c) {
			if (o instanceof EObject) {
				removeFromResource((T) o);
			}
		}
		markAsDirty(rootResource);
		saveDirtyResources();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> c) {
		Set<Object> removedElements = new HashSet<Object>();
		removedElements.addAll(list);
		removedElements.removeAll(c);
		boolean result = list.retainAll(c);
		for (Object o : removedElements) {
			if (o instanceof EObject) {
				removeFromResource((T) o);
			}
		}
		markAsDirty(rootResource);
		saveDirtyResources();
		return result;
	}

	public void save() {
		// int threads = Runtime.getRuntime().availableProcessors();
		// ExecutorService execService = Executors.newFixedThreadPool(threads);

		// int resourcesPerThread = (dirtyResourceSet.size() + threads - 1) / threads;
		// final ArrayList<Resource> resources = new ArrayList<Resource>(dirtyResourceSet);

		// for (int i = 0; i < resources.size(); i += resourcesPerThread) {
		// final int min = i;
		// final int max = Math.min(min + resourcesPerThread, resources.size());
		// execService.submit(new Runnable() {
		// public void run() {
		// for (int j = min; j <= max; j++) {
		// try {
		// resources.get(j).save(ModelUtil.getResourceSaveOptions());
		// } catch (IOException e) {
		// String message = "Saving to resource failed!";
		// ModelUtil.log(message, e, IStatus.ERROR);
		// throw new IllegalStateException(message, e);
		// }
		// }
		// }
		// });
		// }

		// execService.shutdown();
		// try {
		// // wait for 30 minutes to finish save
		// boolean terminated = execService.awaitTermination(1800000, TimeUnit.MILLISECONDS);
		// if (!terminated) {
		// throw new IllegalStateException("FAIL: Save did not complete within time limit.");
		// }
		// } catch (InterruptedException e1) {
		// String message = "Saving to resource failed!";
		// throw new IllegalStateException(message, e1);
		// }

		for (Resource res : dirtyResourceSet) {
			try {
				res.save(ModelUtil.getResourceSaveOptions());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		dirtyResourceSet.clear();
	}

	/**
	 * Not implemented. Will throw exception on call.
	 * 
	 * @param index the index
	 * @param element the new element
	 * @return will always throw an exception
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#size()
	 */
	public int size() {
		return list.size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	public <D> D[] toArray(D[] a) {
		return list.toArray(a);
	}

}
