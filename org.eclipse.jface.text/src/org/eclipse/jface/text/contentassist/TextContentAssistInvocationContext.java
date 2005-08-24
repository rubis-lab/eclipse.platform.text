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
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.contentassist.ContentAssistInvocationContext;

/**
 * A content assist invocation context for text viewers. The context knows the
 * viewer, the invocation offset and can lazily compute the identifier
 * prefix preceding the invocation offset.
 * 
 * @since 3.2
 */
public class TextContentAssistInvocationContext extends ContentAssistInvocationContext {
	
	private final ITextViewer fViewer;
	private final int fOffset;
	
	private CharSequence fPrefix;
	
	public TextContentAssistInvocationContext(ITextViewer viewer) {
		this(viewer, viewer.getSelectedRange().x);
	}

	public TextContentAssistInvocationContext(ITextViewer viewer, int offset) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
		fOffset= offset;
	}
	
	public int getInvocationOffset() {
		return fOffset;
	}
	
	public ITextViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Shortcut for <code>getViewer().getDocument()</code>.
	 * 
	 * @return the viewer's document
	 */
	public IDocument getDocument() {
		return getViewer().getDocument();
	}
	
	/**
	 * Computes the identifier (as specified by
	 * {@link Character#isJavaIdentifierPart(char)}) that immediately
	 * precedes the invocation offset.
	 * 
	 * @return the prefix preceding the content assist invocation offset
	 * @throws BadLocationException
	 */
	public CharSequence computeIdentifierPrefix() throws BadLocationException {
		if (fPrefix == null) {
			IDocument document= getDocument();
			int end= getInvocationOffset();
			int start= end;
			while (--start >= 0) {
				if (!Character.isJavaIdentifierPart(document.getChar(start)))
					break;
			}
			start++;
			fPrefix= document.get(start, end - start);
		}
		
		return fPrefix;
	}
}
