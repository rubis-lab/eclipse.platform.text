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
 * Manager for comment spell checkers.
 */
public class SpellCheckEngine {

	/** The locale of the spell checker */
	private static Locale fLocale= Locale.US;

	/** The platform spell dictionary */
	private static ISpellDictionary fPlatformDictionary= null;

	/** The singleton spell checker */
	private static ISpellChecker fSpellChecker= null;

	/**
	 * Returns a spell checker for the current preferences.
	 * <p>
	 * After use of the spell-checker its the method <code>SpellCheckManager#dispose</code>
	 * should be called.
	 * 
	 * @return The spell checker for the current preferences
	 */
	public static synchronized ISpellChecker getInstance() {

		if (fSpellChecker == null) {

			if (fPlatformDictionary == null)
				fPlatformDictionary= new SpellReconcileDictionary(fLocale);

			fSpellChecker= new DefaultSpellChecker();
			fSpellChecker.addDictionary(fPlatformDictionary);
		}
		return fSpellChecker;
	}

	/**
	 * Returns the locale of this spell check manager.
	 * 
	 * @return Returns the locale of this manager
	 */
	public static synchronized Locale getLocale() {
		return fLocale;
	}

	/**
	 * Sets the locale for this spell check manager.
	 * 
	 * @param locale
	 *                   The locale for this manager
	 */
	public static synchronized void setLocale(final Locale locale) {

		if (locale != fLocale) {

			if (fSpellChecker != null) {

				if (fPlatformDictionary != null)
					fSpellChecker.removeDictionary(fPlatformDictionary);

				fPlatformDictionary= new SpellReconcileDictionary(locale);
				fSpellChecker.addDictionary(fPlatformDictionary);
			}
			fLocale= locale;
		}
	}

	/**
	 * Creates a new spell check manager.
	 */
	private SpellCheckEngine() {
		// Not for instantiation
	}
}
