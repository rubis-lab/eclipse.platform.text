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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * @since 3.0
 */
public interface _IAnnotationPresentation extends IAnnotationAdapter {
		
	/**
	 * Returns the layer for this annotation presentation. Annotations are considered
	 * being located at layers and are considered being painted starting with
	 * layer 0 upwards. Thus an annotation at layer 5 will be drawn on top of
	 * all co-located annotations at the layers 4 - 0.
	 * 
	 * @return the layer of the given annotation
	 */
	int getLayer();
	
	/**
	 * Draws a graphical representation within the given bounds.
	 * 
	 * @param GC the drawing GC
	 * @param canvas the canvas to draw on
	 * @param bounds the bounds inside the canvas to draw on
	 */
	void paint(GC gc, Canvas canvas, Rectangle bounds);
	
	/**
	 * Returns the label for the type of the annotation.
	 * 
	 * @return the label of the annotation type
	 */
	String getAnnotationTypeLabel();
	
	/**
	 * Returns the text associated with this annotation.
	 * 
	 * @return the text associated with this annotation
	 */
	String getText();
}