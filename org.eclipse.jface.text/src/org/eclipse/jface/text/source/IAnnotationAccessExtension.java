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


/**
 * Extension interface for <code>IAnnotationAccess</code>. Provides the
 * following information and functions in addition to <code>IAnnotationAccess</code>.
 * <ul>
 * <li>a label for the type of a given annotation type</li>
 * <li>the presentation of a given annotation</li>
 * </ul>
 * 
 * @since 3.0
 */
public interface IAnnotationAccessExtension {

	/**
	 * Returns the label for the given annotation's type.
	 * 
	 * @param annotation the annotation
	 * @return the label the given annotation's type or <code>null</code> if no such label exists
	 */
	String getTypeLabel(Annotation annotation);
	
	/**
	 * Returns the annotation presentation for the given annotation.
	 * 
	 * @param annotation the annotation
	 * @return the presentation for the given annotation
	 */
	AnnotationPresentation getAnnotationPresentation(Annotation annotation);
}
