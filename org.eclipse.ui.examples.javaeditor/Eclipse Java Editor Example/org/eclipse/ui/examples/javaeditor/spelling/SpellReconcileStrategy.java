/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package org.eclipse.ui.examples.javaeditor.spelling;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconcile strategy to spell-check comments.
 */
public class SpellReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, ISpellEventListener {

	/**
	 * Spelling problem to be accepted by problem requestors.
	 */
	public class SpellProblem {

		/** The id of the problem */
		public static final int Spelling= 0x80000000;

		/** The end offset of the problem */
		private int fEnd= 0;

		/** The line number of the problem */
		private int fLine= 1;

		/** Was the word found in the dictionary? */
		private boolean fMatch;

		/** Does the word start a new sentence? */
		private boolean fSentence= false;

		/** The start offset of the problem */
		private int fStart= 0;

		/** The word which caused the problem */
		private final String fWord;

		/**
		 * Creates a new spelling problem
		 * 
		 * @param word
		 *                   The word which caused the problem
		 */
		protected SpellProblem(final String word) {
			fWord= word;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getArguments()
		 */
		public String[] getArguments() {

			String prefix= ""; //$NON-NLS-1$
			String postfix= ""; //$NON-NLS-1$

			try {

				final IRegion line= fDocument.getLineInformationOfOffset(fStart);

				prefix= fDocument.get(line.getOffset(), fStart - line.getOffset());
				postfix= fDocument.get(fEnd + 1, line.getOffset() + line.getLength() - fEnd);

			} catch (BadLocationException exception) {
				// Do nothing
			}
			return new String[] { fWord, prefix, postfix, fSentence ? Boolean.toString(true) : Boolean.toString(false), fMatch ? Boolean.toString(true) : Boolean.toString(false)};
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getID()
		 */
		public int getID() {
			return Spelling;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getMessage()
		 */
		public String getMessage() {

			if (fSentence && fMatch)
				return MessageFormat.format("The word ''{0}'' should be spelt with an upper case letter", new String[] { fWord }); //$NON-NLS-1$

			return MessageFormat.format("The word ''{0}'' is not correctly spelt", new String[] { fWord }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getOriginatingFileName()
		 */
		public char[] getOriginatingFileName() {
			return fEditor.getEditorInput().getName().toCharArray();
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceEnd()
		 */
		public final int getSourceEnd() {
			return fEnd;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceLineNumber()
		 */
		public final int getSourceLineNumber() {
			return fLine;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceStart()
		 */
		public final int getSourceStart() {
			return fStart;
		}

		/**
		 * Was the problem word found in the dictionary?
		 * 
		 * @return <code>true</code> iff the word was found, <code>false</code>
		 *               otherwise
		 */
		public final boolean isDictionaryMatch() {
			return fMatch;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#isError()
		 */
		public final boolean isError() {
			return false;
		}

		/**
		 * Does the problem word start a new sentence?
		 * 
		 * @return <code>true</code> iff it starts a new sentence, <code>false</code>
		 *               otherwise
		 */
		public final boolean isSentenceStart() {
			return fSentence;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#isWarning()
		 */
		public final boolean isWarning() {
			return true;
		}

		/**
		 * Sets whether the problem word was found in the dictionary.
		 * 
		 * @param match
		 *                   <code>true</code> iff the word was found, <code>false</code>
		 *                   otherwise
		 */
		public final void setDictionaryMatch(final boolean match) {
			fMatch= match;
		}

		/**
		 * Sets whether the problem word starts a new sentence.
		 * 
		 * @param sentence
		 *                   <code>true</code> iff the word starts a new sentence,
		 *                   <code>false</code> otherwise.
		 */
		public final void setSentenceStart(final boolean sentence) {
			fSentence= sentence;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceEnd(int)
		 */
		public final void setSourceEnd(final int end) {
			fEnd= end;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceLineNumber(int)
		 */
		public final void setSourceLineNumber(final int line) {
			fLine= line;
		}

		/*
		 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceStart(int)
		 */
		public final void setSourceStart(final int start) {
			fStart= start;
		}
	}

	/** The document to operate on */
	private IDocument fDocument= null;

	/** The text editor to operate on */
	private final ITextEditor fEditor;

	/** The document partitioning */
	private final String fPartitioning;

	/** The problem requestor */
	private final ISpellProblemRequestor fRequestor;

	/**
	 * Creates a new comment reconcile strategy.
	 * 
	 * @param editor
	 *                   The text editor to operate on
	 * @param partitioning
	 *                   The document partitioning
	 */
	public SpellReconcileStrategy(final ITextEditor editor, final String partitioning) {
		fEditor= editor;
		fPartitioning= partitioning;

		final IAnnotationModel model= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		Assert.isLegal(model instanceof ISpellProblemRequestor);

		fRequestor= (ISpellProblemRequestor) model;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellEventListener#handle(org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellEvent)
	 */
	public void handle(final ISpellEvent event) {

		final SpellProblem problem= new SpellProblem(event.getWord());

		problem.setSourceStart(event.getBegin());
		problem.setSourceEnd(event.getEnd());
		problem.setSentenceStart(event.isStart());
		problem.setDictionaryMatch(event.isMatch());

		try {
			problem.setSourceLineNumber(fDocument.getLineOfOffset(event.getBegin()) + 1);
		} catch (BadLocationException exception) {
			// Do nothing
		}

		fRequestor.acceptProblem(problem);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		SpellCheckEngine.getInstance().addListener(this);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcile(subRegion);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(final IRegion region) {

		try {

			fRequestor.beginReporting();

			ITypedRegion partition= null;
			final ITypedRegion[] partitions= TextUtilities.computePartitioning(fDocument, fPartitioning, 0, fDocument.getLength());

			final ISpellChecker checker= SpellCheckEngine.getInstance();

			for (int index= 0; index < partitions.length; index++) {

				partition= partitions[index];
				if (!partition.getType().equals(IDocument.DEFAULT_CONTENT_TYPE))
					checker.execute(new SpellCheckIterator(fDocument, partition, SpellCheckEngine.getLocale()));
			}

		} catch (BadLocationException exception) {
			// Do nothing
		} finally {
			fRequestor.endReporting();
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public final void setDocument(final IDocument document) {
		fDocument= document;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void setProgressMonitor(final IProgressMonitor monitor) {
		// Do nothing
	}
}
