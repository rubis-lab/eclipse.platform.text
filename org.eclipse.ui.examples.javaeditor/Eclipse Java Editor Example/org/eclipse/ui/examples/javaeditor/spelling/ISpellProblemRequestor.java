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

import org.eclipse.ui.examples.javaeditor.spelling.SpellReconcileStrategy.SpellProblem;

/**
 * A callback interface for receiving spelling problems as they are discovered
 * by the spell checker.
 */
public interface ISpellProblemRequestor {

	/**
	 * Notification of a spelling problem.
	 * 
	 * @param problem
	 *                   The discovered spelling problem
	 */
	void acceptProblem(SpellProblem problem);

	/**
	 * Notification sent before starting the problem detection process.
	 * Typically, this would tell a problem collector to clear previously
	 * recorded problems.
	 */
	void beginReporting();

	/**
	 * Notification sent after having completed problem detection process.
	 * Typically, this would tell a problem collector that no more problems
	 * should be expected in this iteration.
	 */
	void endReporting();

	/**
	 * Predicate allowing the problem requestor to signal whether or not it is
	 * currently interested by problem reports. When answering <code>false</false>, problem will
	 * not be discovered any more until the next iteration.
	 * 
	 * This  predicate will be invoked once prior to each problem detection iteration.
	 * 
	 * @return boolean - indicates whether the requestor is currently interested by problems.
	 */
	boolean isActive();
}
