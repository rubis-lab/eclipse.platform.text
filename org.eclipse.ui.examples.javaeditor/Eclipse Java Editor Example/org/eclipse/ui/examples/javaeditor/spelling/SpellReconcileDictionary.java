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

package org.eclipse.ui.examples.javaeditor.spelling;

import java.util.Locale;

/**
 * Dictionary used by the spell reconciling strategy.
 */
public class SpellReconcileDictionary extends PlatformSpellDictionary {

	/**
	 * Creates a new spell reconcile dictionary.
	 * 
	 * @param locale
	 *                   The locale for this dictionary
	 */
	public SpellReconcileDictionary(final Locale locale) {
		super(locale);
	}
}
