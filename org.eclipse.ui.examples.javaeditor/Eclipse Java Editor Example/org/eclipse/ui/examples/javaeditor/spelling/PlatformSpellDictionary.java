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

import java.util.List;
import java.util.Locale;

/**
 * Platform wide locale sensitive dictionary for spell-checking.
 */
public abstract class PlatformSpellDictionary extends AbstractSpellDictionary {

	/** The locale of this dictionary */
	private final Locale fLocale;

	/**
	 * Creates a new platform spell dictionary.
	 * 
	 * @param locale
	 *                   The locale for this dictionary
	 */
	public PlatformSpellDictionary(final Locale locale) {
		fLocale= locale;

		loadDictionary(getName());
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary#addWord(java.lang.String)
	 */
	public final void addWord(final String word) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getName()
	 */
	protected final String getName() {
		return fLocale.toString().toLowerCase() + ".dictionary"; //$NON-NLS-1$
	}

	/**
	 * Returns the locale of this dictionary.
	 * 
	 * @return The locale of this dictionary
	 */
	public final Locale getLocale() {
		return fLocale;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary#isCorrect(java.lang.String)
	 */
	public boolean isCorrect(final String word) {

		final List candidates= getCandidates(getHashProvider().getHash(word));
		if (candidates.contains(word) || candidates.contains(word.toLowerCase(fLocale)))
			return true;

		return false;
	}
}
