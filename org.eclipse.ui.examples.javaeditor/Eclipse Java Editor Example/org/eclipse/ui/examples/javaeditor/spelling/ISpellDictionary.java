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

import java.util.Set;

/**
 * Interface of dictionaries to use for spell-checking.
 */
public interface ISpellDictionary {

	/**
	 * Externalizes the specified word.
	 * 
	 * @param word
	 *                   The word to externalize in the dictionary
	 */
	public void addWord(String word);

	/**
	 * Returns the ranked word proposals for an incorrectly spelt word.
	 * 
	 * @param word
	 *                   The word to retrieve the proposals for
	 * @param sentence
	 *                   <code>true</code> iff the proposals start a new sentence,
	 *                   <code>false</code> otherwise
	 * @return Array of ranked word proposals
	 */
	public Set getProposals(String word, boolean sentence);

	/**
	 * Is the specified word correctly spelt?
	 * 
	 * @param word
	 *                   The word to spell-check
	 * @return <code>true</code> iff this word is correctly spelt, <code>false</code>
	 *               otherwise.
	 */
	public boolean isCorrect(String word);
}
