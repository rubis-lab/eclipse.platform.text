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
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Layout;

/**
 * 
 * 
 * @since 3.0
 */
public interface IExpansionLayouter {
	Layout getLayout(int itemCount);
	Object getLayoutData();
	int getAnnotationSize();
	int getBorderWidth();
	/**
	 * 
	 * 
	 * @return
	 */
	Region getShellRegion(int itemCount);
}