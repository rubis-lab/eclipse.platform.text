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

/**
 * 
 * 
 * @since 3.0
 */
public interface IInformationControlExtension3 {
	/**
	 * Returns <code>true</code> if this control will handle mouse events itself and the manager
	 * should only listen to dispose events.
	 * 
	 * @return <code>true</code> if this control handles mouse events on its own.
	 */
	boolean isMouseController();
}
