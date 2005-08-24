/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

/**
 * Describes the context of an invocation of content assist. For a text
 * editor, the context would typically include the document (or the
 * viewer) and the selection range, while source code editors may
 * provide specific context information such as an AST.
 * <p>
 * An invocation context may also compute additional context information
 * on demand and cache it to make it available to all
 * {@link org.eclipse.jface.text.contentassist.ICompletionProposalComputer}s
 * contributing proposals to one content assist invocation.
 * </p>
 * 
 * @since 3.2
 */
public class ContentAssistInvocationContext {

}
