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
package org.eclipse.jface.text;

import org.eclipse.swt.graphics.Rectangle;

 
/**
 * Extension interface for <code>IInformationControl</code>. Adds API which
 * allows to get this information control's bounds i.e. location and size.
 * <p>
 * Note: An information control which implements this interface can ignore
 * calls to {@link org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)}
 * or use it as hint for its very first appearance.
 * </p>
 * 
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.0
 */ 
public interface IInformationControlExtension3 {
	
	/**
	 * Returns a rectangle describing the receiver's size and location
	 * relative to its parent (or its display if its parent is null).
	 * <p>
	 * Note: If the receiver is already disposed then this methods must
	 * return the last valid location and size.
	 * </p>
	 *
	 * @return the receiver's bounding rectangle
	 */
	Rectangle getBounds();
	
	/**
	 * Computes the trim for this control.
	 * x and y denote the upper left corner of the trimming relative
	 * to this control's location i.e. this will most likely be
	 * negative values. Width and height represent the border sizes.
	 * 
	 * @return the receivers trim
	 */
	Rectangle computeTrim();
}
