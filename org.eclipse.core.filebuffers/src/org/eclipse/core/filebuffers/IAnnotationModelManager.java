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
package org.eclipse.core.filebuffers;

import java.util.Iterator;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @since 3.0
 */
public interface IAnnotationModelManager {
	
	/**
	 * Registers the given annotation model under the given key. If there is
	 * already an annotation model registered, an <code>IllegalStateException</code>
	 * is thrown.
	 * 
	 * @param key the key under which to register the annotation model
	 * @param model the annotation model to be registered
	 * @exception IllegalStateException if <code>key</code> is already in use
	 */
	void registerAnnotationModel(Object key, IAnnotationModel model) throws IllegalStateException;
	
	/**
	 * Unregisters the annotation model that has been registered under the
	 * given key. If no model is registered, this operation is without effect.
	 * 
	 * @param key the key
	 * @return the unregistered annotation model
	 * @exception IllegalStateException if the model for the given key is in use
	 */
	IAnnotationModel unregisterAnnotationModel(Object key) throws IllegalStateException;
	
	/**
	 * A client connects to the annotation model registered under the given key. If there is no such
	 * annotation model, an <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param key the key for the annotation model
	 */
	void connect(Object key) throws IllegalArgumentException;
	
	/**
	 * A client disconnects from the annotation model registered under the given key.
	 * 
	 * @param key the key for the annotation model
	 */
	void disconnect(Object key);
	
	/**
	 * Returns the annotation mode for the given key or <code>null</code> if
	 * there is no such model.
	 * 
	 * @param key the key for the annotation model
	 * @return the registered annotation model or <code>null</code>
	 */
	IAnnotationModel getAnnotationModel(Object key);
	
	/**
	 * Returns an iterator that enumerates all connected annotation models.
	 * 
	 * @return an iterator enumerating all connected annotation models.
	 */
	Iterator getAnnotationModelIterator();
}
