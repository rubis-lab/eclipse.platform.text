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

package org.eclipse.ui.texteditor;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.Annotation;


/**
 * Annotation representing a marker on a resource in the workspace.
 * This class may be instantiated or be subclassed.
 *
 * @see org.eclipse.core.resources.IMarker
 */
public class MarkerAnnotation extends Annotation implements IAnnotationExtension {
	
	/** The marker this annotation represents */
	private IMarker fMarker;

	/**
	 * Creates a new annotation for the given marker.
	 *
	 * @param marker the marker
	 */
	public MarkerAnnotation(IMarker marker) {
		Assert.isNotNull(marker);
		fMarker= marker;
	}
	
	/**
	 * The <code>MarkerAnnotation</code> implementation of this
	 * <code>Object</code> method returns <code>true</code> iff the other
	 * object is also a <code>MarkerAnnotation</code> and the marker handles are
	 * equal.
	 */
	public boolean equals(Object o) {
		if (o != null && o.getClass() == getClass())
			return fMarker.equals(((MarkerAnnotation) o).fMarker);
		return false;
	}
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fMarker.hashCode();
	}
		
	/**
	 * Returns this annotation's underlying marker.
	 *
	 * @return the marker
	 */
	public IMarker getMarker() {
		return fMarker;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getMarkerType()
	 * @since 3.0
	 */
	public String getMarkerType() {
		try {
			return fMarker.getType();
		} catch (CoreException e) {
			return null;
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getSeverity()
	 * @since 3.0
	 */
	public int getSeverity() {
		return fMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#isTemporary()
	 * @since 3.0
	 */
	public boolean isTemporary() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getMessage()
	 * @since 3.0
	 */
	public String getMessage() {
		return fMarker.getAttribute(IMarker.MESSAGE, null);
	}
}
