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
import java.util.Map;


/**
 * Abstract annotation managed by an <code>IAnnotationModel</code>.
 * 
 * @since 3.0
 */
public class Annotation {
	
	private Map fDataMap;
	
	/**
	 * Creates a new annotation.
	 */
	public Annotation() {
	}
	
	/**
	 * Sets the data of this annotation for the given key.
	 * 
	 * @param key the key
	 * @param data the data
	 */
	public void setData(Object key, Object data) {
		if (data == null) {
			if (fDataMap != null) {
				fDataMap.remove(key);
				if (fDataMap.isEmpty())
					fDataMap= null;
			}
		} else {
			if (fDataMap == null)
				fDataMap= new HashMap();
			fDataMap.put(key, data);
		}
	}
	
	/**
	 * Returns the data of this annotation for the given key.
	 * 
	 * @param key the key
	 * @return the data of this annotation
	 */
	public Object getData(Object key) {
		if (fDataMap != null)
			return fDataMap.get(key);
		return null;
	}
	
	/**
	 * Disposes this annotation.
	 * TODO no client yet
	 */
	public void dispose() {
		if (fDataMap != null) {
			fDataMap.clear();
			fDataMap= null;
		}
	}
}
