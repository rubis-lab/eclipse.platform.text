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
package org.eclipse.jface.text.source;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Abstract annotation managed by an <code>IAnnotationModel</code>.
 * 
 * @since 3.0
 */
public class Annotation {
	
	private Map fDataMap;
	private Map fAnnotationAdapter;
	
	/**
	 * Creates a new annotation.
	 */
	public Annotation() {
	}
	
	/**
	 * Sets the data of this annotation for the given key. If the <code>key</code>
	 * is <code>null</code> any data stored under the given key is removed.
	 * 
	 * @param key the key
	 * @param data the data
	 */
	public void _setData(Object key, Object data) {
		if (data == null) {
			if (fDataMap != null) {
				fDataMap.remove(key);
				if (fDataMap.isEmpty())
					fDataMap= null;
				fireAnnotationDataChanged();
			}
		} else {
			if (fDataMap == null)
				fDataMap= new HashMap();
			fDataMap.put(key, data);
			fireAnnotationDataChanged();
		}
	}
	
	/**
	 * Returns the data of this annotation for the given key or <code>null</code> if no
	 * data has been set for the given key.
	 * 
	 * @param key the key
	 * @return the data for the given key
	 */
	public Object _getData(Object key) {
		if (fDataMap != null)
			return fDataMap.get(key);
		return null;
	}
	
	/**
	 * Sets the adapter of this annotation for the given key. If the <code>key</code>
	 * is <code>null</code> any adapter stored under the given key is
	 * removed.
	 * 
	 * @param key the key
	 * @param adapter the adapter for this given key
	 */
	public void setAnnotationAdapter(Object key, IAnnotationAdapter adapter) {
		if (adapter == null) {
			if (fAnnotationAdapter != null) {
				fAnnotationAdapter.remove(key);
				if (fAnnotationAdapter.isEmpty())
					fAnnotationAdapter= null;
			}
		} else {
			if (fAnnotationAdapter == null)
				fAnnotationAdapter= new HashMap();
			fAnnotationAdapter.put(key, adapter);
		}
	}
	
	/**
	 * Returns the adapter of this annotation for the given key or <code>null</code>
	 * if no adapter has been set for the given key.
	 * 
	 * @param key the key
	 * @return the adapter for the given key or <code>null</code>
	 */
	public IAnnotationAdapter getAnnotationAdapter(Object key) {
		if (fAnnotationAdapter != null)
			return (IAnnotationAdapter) fAnnotationAdapter.get(key);
		return null;
	}
	
	/**
	 * Disposes this annotation and notifies all annotation adapters about this.
	 * TODO no client yet
	 */
	public void dispose() {
		if (fDataMap != null) {
			fDataMap.clear();
			fDataMap= null;
		}
		
		if (fAnnotationAdapter != null) {
			Iterator e= fAnnotationAdapter.values().iterator();
			while (e.hasNext()) {
				IAnnotationAdapter adapter= (IAnnotationAdapter) e.next();
				adapter.annotationDisposed(this);
			}
			fAnnotationAdapter.clear();
			fAnnotationAdapter= null;
		}
	}
	
	/**
	 * Notifies all annotation adapters about the fact that the annotation's
	 * data changed.
	 */
	public void fireAnnotationDataChanged() {
		Iterator e= fAnnotationAdapter.values().iterator();
		while (e.hasNext()) {
			IAnnotationAdapter adapter= (IAnnotationAdapter) e.next();
			adapter.annotationDataChanged(this);
		}
	}
}
