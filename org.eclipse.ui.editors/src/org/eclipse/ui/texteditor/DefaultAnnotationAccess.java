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

import java.util.Iterator;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationAdapter;
import org.eclipse.jface.text.source._IAnnotationPresentation;


/**
 * @since 3.0
 */
public class DefaultAnnotationAccess implements IAnnotationAccess, IAnnotationAccessExtension {
	
	/** The marker annotation preferences */
	protected MarkerAnnotationPreferences fMarkerAnnotationPreferences;
	
	public DefaultAnnotationAccess(MarkerAnnotationPreferences markerAnnotationPreferences) {
		fMarkerAnnotationPreferences= markerAnnotationPreferences;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	 */
	public Object getType(Annotation annotation) {
		return annotation.getAnnotationType();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isMultiLine(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isMultiLine(Annotation annotation) {
		return true;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isTemporary(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isTemporary(Annotation annotation) {
		return !annotation.isPersistent();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getLabel(org.eclipse.jface.text.source.Annotation)
	 */
	public String getTypeLabel(Annotation annotation) {
		_IAnnotationPresentation presentation= getAnnotationPresentation(annotation);
		return presentation != null ? presentation.getAnnotationTypeLabel() : null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getLayer(org.eclipse.jface.text.source.Annotation)
	 */
	public int getLayer(Annotation annotation) {
		_IAnnotationPresentation presentation= getAnnotationPresentation(annotation);
		return presentation != null ? presentation.getLayer() : IAnnotationAccessExtension.DEFAULT_LAYER;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getText(org.eclipse.jface.text.source.Annotation)
	 */
	public String getText(Annotation annotation) {
		_IAnnotationPresentation presentation= getAnnotationPresentation(annotation);
		return presentation != null ? presentation.getText() : null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#paint(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {
		_IAnnotationPresentation presentation= getAnnotationPresentation(annotation);
		if (presentation != null)
			presentation.paint(gc, canvas, bounds);
	}
	
	/**
	 * Returns the annotation preference for the given annotation.
	 * 
	 * @param the annotation
	 * @return the annotation preference or <code>null</code> if none
	 */	
	private AnnotationPreference getAnnotationPreference(Annotation annotation) {
		String annotationType= annotation.getAnnotationType();
		Iterator e= fMarkerAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (info.getAnnotationType().equals(annotationType))
				return info;
		}
		return null;
	}
	
	protected _IAnnotationPresentation createAnnotationPresentation(Annotation annotation) {
		return new _DefaultAnnotationPresentation(annotation, getAnnotationPreference(annotation), _IAnnotationPresentation.class);
	}
	
	private _IAnnotationPresentation getAnnotationPresentation(Annotation annotation) {
		IAnnotationAdapter adapter= annotation.getAnnotationAdapter(_IAnnotationPresentation.class);
		if (adapter instanceof IAnnotationAdapter)
			return (_IAnnotationPresentation) adapter;
		
		_IAnnotationPresentation presentation= createAnnotationPresentation(annotation);
		annotation.setAnnotationAdapter(_IAnnotationPresentation.class, presentation);
		return presentation;
	}
}
