/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.IAnnotationModelManager;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @since 3.0
 */
public class AnnotationModelManager implements IAnnotationModelManager {
	
	private static class Value {
		public IAnnotationModel fModel;
		public int fReferenceCount;
	}
	
	
	private final Map fValues= new HashMap();
	
	public AnnotationModelManager() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelManager#registerAnnotationModel(java.lang.Object, org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void registerAnnotationModel(Object key, IAnnotationModel model) throws IllegalStateException {
		Value registered= (Value) fValues.get(key);
		if (registered != null)
			throw new IllegalStateException();
		
		registered= new Value();
		registered.fModel= model;
		registered.fReferenceCount= 0;
		fValues.put(key, registered);
	}

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelManager#unregisterAnnotationModel(java.lang.Object)
	 */
	public IAnnotationModel unregisterAnnotationModel(Object key) throws IllegalStateException {
		Value registered= (Value) fValues.get(key);
		if (registered == null)
			return null;
		if (registered.fReferenceCount > 0)
			throw new IllegalStateException();
		
		fValues.remove(key);
		return registered.fModel;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelManager#connect(java.lang.Object)
	 */
	public void connect(Object key) throws IllegalArgumentException {
		Value registered= (Value) fValues.get(key);
		if (registered == null)
			throw new IllegalArgumentException();
		
		++ registered.fReferenceCount;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelManager#disconnect(java.lang.Object)
	 */
	public void disconnect(Object key) {
		Value registered= (Value) fValues.get(key);
		if (registered != null)
			-- registered.fReferenceCount;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IAnnotationModelManager#getAnnotationModel(java.lang.Object)
	 */
	public IAnnotationModel getAnnotationModel(Object key) {
		Value registered= (Value) fValues.get(key);
		if (registered != null)
			return registered.fModel;
		return null;
	}
}
