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

package org.eclipse.ui.examples.javaeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.examples.javaeditor.spelling.ISpellProblemRequestor;
import org.eclipse.ui.examples.javaeditor.spelling.SpellReconcileStrategy.SpellProblem;
import org.eclipse.ui.texteditor.IAnnotationExtension;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class JavaDocumentProvider extends TextFileDocumentProvider {

	protected static class CompilationUnitAnnotationModel extends ResourceMarkerAnnotationModel implements ISpellProblemRequestor {
		private List fCollectedProblems;
		private List fCurrentlyOverlaid= new ArrayList();
		private List fGeneratedAnnotations;

		private boolean fIsActive= false;
		private List fPreviouslyOverlaid= null;
		private IProgressMonitor fProgressMonitor;

		private int fReportCount= 0;

		private ReverseMap fReverseMap= new ReverseMap();

		public CompilationUnitAnnotationModel(IResource resource) {
			super(resource);
		}

		/*
		 * @see IProblemRequestor#acceptProblem(IProblem)
		 */
		public void acceptProblem(SpellProblem problem) {
			if (isActive())
				fCollectedProblems.add(problem);
		}

		/*
		 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
		 */
		protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
			super.addAnnotation(annotation, position, fireModelChanged);

			Object cached= fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List) {
				List list= (List) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List list= new ArrayList(2);
				list.add(cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}

		/*
		 * @see IProblemRequestor#beginReporting()
		 */
		public void beginReporting() {

			if (fReportCount++ == 0) {

				fCollectedProblems= new ArrayList();
				setIsActive(true);
			}
		}

		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			return new MarkerAnnotation(marker);
		}

		protected Position createPositionFromProblem(SpellProblem problem) {
			int start= problem.getSourceStart();
			if (start < 0)
				return null;

			int length= problem.getSourceEnd() - problem.getSourceStart() + 1;
			if (length < 0)
				return null;

			return new Position(start, length);
		}

		/*
		 * @see IProblemRequestor#endReporting()
		 */
		public void endReporting() {

			if (--fReportCount == 0) {

				if (!isActive())
					return;

				if (fProgressMonitor != null && fProgressMonitor.isCanceled())
					return;

				boolean isCanceled= false;
				boolean temporaryProblemsChanged= false;

				synchronized (fAnnotations) {

					fPreviouslyOverlaid= fCurrentlyOverlaid;
					fCurrentlyOverlaid= new ArrayList();

					if (fGeneratedAnnotations.size() > 0) {
						temporaryProblemsChanged= true;
						removeAnnotations(fGeneratedAnnotations, false, true);
						fGeneratedAnnotations.clear();
					}

					if (fCollectedProblems != null && fCollectedProblems.size() > 0) {

						Iterator e= fCollectedProblems.iterator();
						while (e.hasNext()) {

							SpellProblem problem= (SpellProblem) e.next();

							if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
								isCanceled= true;
								break;
							}

							Position position= createPositionFromProblem(problem);
							if (position != null) {

								try {
									ProblemAnnotation annotation= new ProblemAnnotation(problem);
									addAnnotation(annotation, position, false);
									fGeneratedAnnotations.add(annotation);

									temporaryProblemsChanged= true;
								} catch (BadLocationException x) {
									// ignore invalid position
								}
							}
						}

						fCollectedProblems.clear();
					}

					fPreviouslyOverlaid.clear();
					fPreviouslyOverlaid= null;
				}

				if (temporaryProblemsChanged)
					fireModelChanged();
			}

			if (fReportCount < 0)
				fReportCount= 0;
		}

		/*
		 * @see IProblemRequestor#isActive()
		 */
		public boolean isActive() {
			return fIsActive && (fCollectedProblems != null);
		}

		/*
		 * @see AnnotationModel#removeAllAnnotations(boolean)
		 */
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			fReverseMap.clear();
		}

		/*
		 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
		 */
		protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {

			Position position= getPosition(annotation);
			Object cached= fReverseMap.get(position);
			if (cached instanceof List) {
				List list= (List) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
			super.removeAnnotation(annotation, fireModelChanged);
		}

		/*
		 * @see IProblemRequestorExtension#setIsActive(boolean)
		 */
		public void setIsActive(boolean isActive) {
			if (fIsActive != isActive) {
				fIsActive= isActive;
				if (fIsActive)
					startCollectingProblems();
				else
					stopCollectingProblems();
			}
		}

		/*
		 * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
		 */
		public void setProgressMonitor(IProgressMonitor monitor) {
			fProgressMonitor= monitor;
		}

		/**
		 * Tells this annotation model to collect temporary problems from now
		 * on.
		 */
		private void startCollectingProblems() {
			fCollectedProblems= new ArrayList();
			fGeneratedAnnotations= new ArrayList();
		}

		/**
		 * Tells this annotation model to no longer collect temporary problems.
		 */
		private void stopCollectingProblems() {
			if (fGeneratedAnnotations != null) {
				removeAnnotations(fGeneratedAnnotations, true, true);
				fGeneratedAnnotations.clear();
			}
			fCollectedProblems= null;
			fGeneratedAnnotations= null;
		}
	}

	/**
	 * Annotation representating an <code>IProblem</code>.
	 */
	static protected class ProblemAnnotation extends Annotation implements IAnnotationExtension {

		private Image fImage;
		private SpellProblem fProblem;

		public ProblemAnnotation(SpellProblem problem) {
			fProblem= problem;
			setLayer(MarkerAnnotation.PROBLEM_LAYER + 1);
		}

		/*
		 * @see IAnnotationExtension#getMarkerType()
		 */
		public String getMarkerType() {
			return IMarker.PROBLEM;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getMessage()
		 */
		public String getMessage() {
			return fProblem.getMessage();
		}

		/*
		 * @see IAnnotationExtension#getSeverity()
		 */
		public int getSeverity() {
			return IMarker.SEVERITY_WARNING;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IAnnotationExtension#isTemporary()
		 */
		public boolean isTemporary() {
			return true;
		}

		/*
		 * @see Annotation#paint
		 */
		public void paint(GC gc, Canvas canvas, Rectangle r) {

			if (fImage != null)
				drawImage(fImage, gc, canvas, r, SWT.CENTER, SWT.TOP);
		}
	}

	protected static class ReverseMap {

		static class Entry {
			Position fPosition;
			Object fValue;
		}
		private int fAnchor= 0;

		private List fList= new ArrayList(2);

		public ReverseMap() {
		}

		public void clear() {
			fList.clear();
		}

		public Object get(Position position) {

			Entry entry;

			// behind anchor
			int length= fList.size();
			for (int i= fAnchor; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}

			// before anchor
			for (int i= 0; i < fAnchor; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}

			return null;
		}

		private int getIndex(Position position) {
			Entry entry;
			int length= fList.size();
			for (int i= 0; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}

		public void put(Position position, Object value) {
			int index= getIndex(position);
			if (index == -1) {
				Entry entry= new Entry();
				entry.fPosition= position;
				entry.fValue= value;
				fList.add(entry);
			} else {
				Entry entry= (Entry) fList.get(index);
				entry.fValue= value;
			}
		}

		public void remove(Position position) {
			int index= getIndex(position);
			if (index > -1)
				fList.remove(index);
		}
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new CompilationUnitAnnotationModel(file);
	}
}
