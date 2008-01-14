/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.widgets.Control;


/**
 * Extension interface for {@link org.eclipse.jface.text.IInformationControl}.
 * Adds API to test the visibility of the control and to test whether another
 * control is a child of the information control.
 * 
 * <b>This API is work in progress and should not be used by clients.<b>
 * 
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.4
 */
public interface IInformationControlExtension5 {

	/**
	 * Tests whether the given control is this information control
	 * or a child of this information control.
	 * 
	 * @param control the control to test
	 * @return <code>true</code> iff the given control is this information control
	 * or a child of this information control
	 */
	public boolean containsControl(Control control);
	
	/**
	 * @return <code>true</code> iff the information control is currently visible
	 */
	public abstract boolean isVisible();
	
	/**
	 * Returns whether the mouse is allowed to move into this information control.
	 * Note that this feature only works if this information control also implements
	 * {@link IInformationControlExtension5}.
	 * 
	 * @return <code>true</code> to allow the mouse to move into this information control,
	 * <code>false</code> to close the information control when the mouse is moved into it
	 */
	public boolean allowMoveIntoControl();
	
	/**
	 * FIXME: extract into separate interface. Adds a delayed input change
	 * listener. Has no effect if the given listener is already registered.
	 * 
	 * @param delayedInputChangeListener the listener to add
	 */
	void addDelayedInputChangeListener(IDelayedInputChangeListener delayedInputChangeListener);

	/**
	 * FIXME: extract into separate interface. Removes a delayed input change
	 * listener. Has no effect if the given listener was not already registered.
	 * 
	 * @param delayedInputChangeListener the listener to remove
	 */
	void removeDelayedInputChangeListener(IDelayedInputChangeListener delayedInputChangeListener);
}