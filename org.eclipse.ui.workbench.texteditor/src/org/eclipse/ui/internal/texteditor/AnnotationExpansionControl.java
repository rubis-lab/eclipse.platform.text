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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.AnnotationEvent;
import org.eclipse.ui.texteditor.IAnnotationExtension;
import org.eclipse.ui.texteditor.IAnnotationListener;

/**
 * A control that can display a number of annotations. The control can decide how it layouts the 
 * annotations to present them to the user. 
 * 
 * <p>Each annotation can have its custom context menu and hover.</p>
 * 
 * @since 3.0
 */
public class AnnotationExpansionControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, IInformationControlExtension3 {

	
	public interface ICallback {
		void run(IInformationControlExtension2 control);
	}
	
	/**
	 * Input used by the control to display the annotations.
	 * TODO move to top-level class
	 * TODO encapsulate fields
	 * 
	 * @since 3.0
	 */
	public static class AnnotationHoverInput {
		public Annotation[] fAnnotations;
		public ISourceViewer fViewer;
		public IVerticalRulerInfo fRulerInfo;
		public IAnnotationListener fAnnotationListener;
		public IDoubleClickListener fDoubleClickListener;
		public ICallback redoAction;
		public IAnnotationModel model;
	}
	
	private final class Item {
		Annotation fAnnotation;
		Canvas canvas;
		StyleRange[] oldStyles;
		
		public void selected() {
			Display disp= fShell.getDisplay();
			canvas.setCursor(fHandCursor);
			// TODO: shade - for now: set grey background
			canvas.setBackground(getSelectionColor(disp));
			
			// hightlight the viewer background at its position
			oldStyles= setViewerBackground(fAnnotation);
			
			// set the selection
			fSelection= this;
			
			if (fInput.fAnnotationListener != null) {
				AnnotationEvent event= new AnnotationEvent(fAnnotation);
				fInput.fAnnotationListener.annotationSelected(event);
			}
		}
		
		public void defaultSelected() {
			if (fInput.fAnnotationListener != null) {
				AnnotationEvent event= new AnnotationEvent(fAnnotation);
				fInput.fAnnotationListener.annotationDefaultSelected(event);
			}

			dispose();
		}
		
		public void showContextMenu(Menu menu) {
			if (fInput.fAnnotationListener != null) {
				AnnotationEvent event= new AnnotationEvent(fAnnotation);
				fInput.fAnnotationListener.annotationContextMenuAboutToShow(event, menu);
			}
		}

		public void deselect() {
			// hide the popup
			fHoverManager.disposeInformationControl();
			
			// deselect
			fSelection= null;
			
			resetViewerBackground(oldStyles);
			oldStyles= null;
			
			Display disp= fShell.getDisplay();
			canvas.setCursor(null);
			// TODO: remove shading - for now: set standard background
			canvas.setBackground(disp.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			
		}

	}
	
	/**
	 * Disposes of an item 
	 */
	private final class MyDisposeListener implements DisposeListener {
		/*
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			Item item= (Item) ((Widget) e.getSource()).getData();
			item.deselect();
			item.canvas= null;
			item.fAnnotation= null;
			item.oldStyles= null;
			
			((Widget) e.getSource()).setData(null);
		}
	}
	
	/**
	 * Listener on context menu invocation on the items
	 */
	private final class MyMenuDetectListener implements Listener {
		/*
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event.type == SWT.MenuDetect) {
				// TODO: show per-item menu
				// for now: show ruler context menu
				if (fInput != null) {
					Control ruler= fInput.fRulerInfo.getControl();
					if (ruler != null && !ruler.isDisposed()) {
						Menu menu= ruler.getMenu();
						if (menu != null && !menu.isDisposed()) {
							menu.setLocation(event.x, event.y);
							menu.addMenuListener(new MenuListener() {

								public void menuHidden(MenuEvent e) {
									dispose();
								}

								public void menuShown(MenuEvent e) {
								}
								
							});
							menu.setVisible(true);
						}
					}
				}
			}
		}
	}

	/**
	 * Listener on mouse events on the items.
	 */
	private final class MyMouseListener extends MouseAdapter {
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			Item item= (Item) ((Widget) e.getSource()).getData();
			if (e.button == 1 && item.fAnnotation == fInput.fAnnotations[0] && fInput.fDoubleClickListener != null) {
				fInput.fDoubleClickListener.doubleClick(null);
				// special code for JDT to renew the annotation set.
				if (fInput.redoAction != null)
					fInput.redoAction.run(AnnotationExpansionControl.this);
			}
//			dispose();
			// TODO special action to invoke double-click action on the vertical ruler
			// how about
//					Canvas can= (Canvas) e.getSource();
//					Annotation a= (Annotation) can.getData();
//					if (a != null) {
//						a.getDoubleClickAction().run();
//					}
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			Item item= (Item) ((Widget) e.getSource()).getData();
			// TODO for now, to make double click work: disable single click on the first item
			// disable later when the annotationlistener selectively handles input
			if (item != null && e.button == 1 && item.fAnnotation != fInput.fAnnotations[0])
				item.defaultSelected();
		}
	}

	/**
	 * Listener on mouse track events on the items.
	 */
	private final class MyMouseTrackListener implements MouseTrackListener {
		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseEnter(MouseEvent e) {
			Item item= (Item) ((Widget) e.getSource()).getData();
			if (item != null)
				item.selected();
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseExit(MouseEvent e) {
			
			Item item= (Item) ((Widget) e.getSource()).getData();
			if (item != null)
				item.deselect();
			
			// if the event lies outside the entire popup, dispose
			org.eclipse.swt.graphics.Region region= fShell.getRegion();
			Canvas can= (Canvas) e.getSource();
			Point p= can.toDisplay(e.x, e.y);
			if (region == null) {
				Rectangle bounds= fComposite.getBounds();
				p= fComposite.toControl(p);
				if (!bounds.contains(p))
					dispose();
			} else {
				p= fShell.toControl(p);
				if (!region.contains(p))
					dispose();
			}
			
			
		}
		
		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseHover(MouseEvent e) {
			// bring up custom per-annotation hover based on the current selection
			fHoverManager.showInformation();
		}
	}

	/**
	 * Listener on paint events on the items. Paints the annotation image on the given <code>GC</code>.
	 */
	private final class MyPaintListener implements PaintListener {
		/*
		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
		 */
		public void paintControl(PaintEvent e) {
			Canvas can= (Canvas) e.getSource();
			Annotation a= ((Item) can.getData()).fAnnotation;
			if (a != null) {
				Rectangle rect= new Rectangle(fLayouter.getBorderWidth(), fLayouter.getBorderWidth(), fLayouter.getAnnotationSize(), fLayouter.getAnnotationSize());
				a.paint(e.gc, can, rect);
			}
		}
	}

	/**
	 * Our own private hover manager used to shop per-item pop-ups.
	 */
	private final class HoverManager extends AbstractInformationControlManager {
		
		public HoverManager() {
			super(new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent);
				}
			});
			
			setAnchor(ANCHOR_TOP);
			setFallbackAnchors(new Anchor[] { ANCHOR_BOTTOM, ANCHOR_RIGHT} );
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformation()
		 */
		protected void computeInformation() {
			if (fSelection != null) {
				Rectangle subjectArea= new Rectangle(0,0,fSelection.canvas.getSize().x, fSelection.canvas.getSize().y);
				Object information= fSelection.fAnnotation;
				if (information instanceof IAnnotationExtension)
					information= ((IAnnotationExtension)information).getMessage();
				else
					information= null;
				
				setInformation(information, subjectArea);
			}
			
		}
	}
	
	/** Model data. */
	protected AnnotationHoverInput fInput;
	/** The control's shell */
	private Shell fShell;
	/** The composite combining all the items. */
	protected Composite fComposite;
	/** The hand cursor. */
	private Cursor fHandCursor;
	/** The currently selected item, or <code>null</code> if none is selected. */
	private Item fSelection;
	/** The hover manager for the per-item hovers. */
	private HoverManager fHoverManager;
	
	/* listener legion */
	private final MyPaintListener fPaintListener;
	private final MyMouseTrackListener fMouseTrackListener;
	private final MyMouseListener fMouseListener;
	private final MyMenuDetectListener fMenuDetectListener;
	private final DisposeListener fDisposeListener;
	private final IViewportListener fViewportListener;
	
	private IExpansionLayouter fLayouter;
	
	public AnnotationExpansionControl(Shell parent, int shellStyle) {
		this(parent, shellStyle, new LinearLayouter());
	}
	
	public AnnotationExpansionControl(Shell parent, int shellStyle, IExpansionLayouter layouter) {
		fPaintListener= new MyPaintListener();
		fMouseTrackListener= new MyMouseTrackListener();
		fMouseListener= new MyMouseListener();
		fMenuDetectListener= new MyMenuDetectListener();
		fDisposeListener= new MyDisposeListener();
		fViewportListener= new IViewportListener() {

			public void viewportChanged(int verticalOffset) {
				dispose();
			}
			
		};
		fLayouter= layouter;
		
		fShell= new Shell(parent, shellStyle);
		Display display= fShell.getDisplay();
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fComposite= new Composite(fShell, SWT.NONE);
		
		GridLayout layout= new GridLayout(1, true);
		layout.marginHeight= 1;
		layout.marginWidth= 1;
		fShell.setLayout(layout);
		fComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER));
		fComposite.addMouseTrackListener(new MouseTrackAdapter() {

			public void mouseExit(MouseEvent e) {
				org.eclipse.swt.graphics.Region region= fShell.getRegion();
				if (region == null) {
					Rectangle bounds= fComposite.getBounds();
					if (!bounds.contains(e.x, e.y))
						dispose();
				} else if (!region.contains(e.x, e.y)) {
					dispose();
				}
			}

		});
		
		fHandCursor= new Cursor(display, SWT.CURSOR_HAND);
		fShell.setCursor(fHandCursor);
		
		setInfoSystemColor();
		
		fHoverManager= new HoverManager();
		fHoverManager.install(fComposite);
	}
	
	private void setInfoSystemColor() {
		Display display= fShell.getDisplay();
		setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
	 */
	public void setInformation(String information) {
		setInput(null);
	}


	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		if (fInput != null && fInput.fViewer != null)
			fInput.fViewer.removeViewportListener(fViewportListener);
		
		if (input instanceof AnnotationHoverInput)
			fInput= (AnnotationHoverInput) input;
		else
			fInput= null;

		inputChanged(fInput, null);
	}

	protected void inputChanged(Object newInput, Object newSelection) {
		refresh();
	}

	protected void refresh() {
		adjustItemNumber();
		
		if (fInput == null)
			return;

		if (fInput.fAnnotations == null)
			return;
		
		if (fInput.fViewer != null)
			fInput.fViewer.addViewportListener(fViewportListener);
		
		fShell.setRegion(fLayouter.getShellRegion(fInput.fAnnotations.length));
		
		Layout layout= fLayouter.getLayout(fInput.fAnnotations.length);
		fComposite.setLayout(layout);
		
		Control[] children= fComposite.getChildren();
		for (int i= 0; i < fInput.fAnnotations.length; i++) {
			Canvas canvas= (Canvas) children[i];
			Item item= new Item();
			item.canvas= canvas;
			item.fAnnotation= fInput.fAnnotations[i];
			canvas.setData(item);
			canvas.redraw();
		}
		
	}

	protected void adjustItemNumber() {
		if (fComposite == null)
			return;	
		
		Control[] children= fComposite.getChildren();
		int oldSize= children.length;
		int newSize= fInput == null ? 0 : fInput.fAnnotations.length;
		
		Display display= fShell.getDisplay();
		
		// add missing items
		for (int i= oldSize; i < newSize; i++) {
			Canvas canvas= new Canvas(fComposite, SWT.NONE);
			Object gridData= fLayouter.getLayoutData();
			canvas.setLayoutData(gridData);
			canvas.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			
			canvas.addPaintListener(fPaintListener);
			
			canvas.addMouseTrackListener(fMouseTrackListener);
			
			canvas.addMouseListener(fMouseListener);
			
			canvas.addListener(SWT.MenuDetect, fMenuDetectListener);
			
			canvas.addDisposeListener(fDisposeListener);
		}
		
		// dispose of exceeding resources 
		for (int i= oldSize; i > newSize; i--) {
			Item item= (Item) children[i - 1].getData();
			item.deselect();
			children[i - 1].dispose();
		}
		
	}

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
	}

	/*
	 * @see IInformationControl#dispose()
	 */
	public void dispose() {
		if (fShell != null) {
			if (!fShell.isDisposed())
				fShell.dispose();
			fShell= null;
			fComposite= null;
			fHandCursor.dispose();
			fHandCursor= null;
			fHoverManager.dispose();
			fHoverManager= null;
			fSelection= null;
		}
	}

	/* 
	 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
	 */
	public boolean hasContents() {
		return fInput.fAnnotations != null && fInput.fAnnotations.length > 0;
	}

	/* 
	 * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		//fMaxWidth= maxWidth;
		//fMaxHeight= maxHeight;
	}

	/* 
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		fShell.setLocation(location);		
	}

	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		fShell.setSize(width, height);
	}

	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		fComposite.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fComposite.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		if (fComposite.isFocusControl())
			return true;
		else {
			Control[] children= fComposite.getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i].isFocusControl())
					return true;
			}
		}
		return false;
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fShell.forceFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		fShell.addFocusListener(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fShell.removeFocusListener(listener);
	}
	
	/**
	 * Returns <code>true</code> if this control will handle mouse events itself and the manager
	 * should only listen to dispose events.
	 * 
	 * @return <code>true</code> if this control handles mouse events on its own.
	 */
	public boolean isMouseController() {
		return true;
	}

	private StyleRange[] setViewerBackground(Annotation annotation) {
		StyledText text= fInput.fViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return null;
		
		Display disp= text.getDisplay();
		
		Position pos= fInput.model.getPosition(annotation);
		if (pos == null)
			return null;
		
		IRegion region= ((TextViewer)fInput.fViewer).modelRange2WidgetRange(new Region(pos.offset, pos.length));
		
		StyleRange[] ranges= text.getStyleRanges(region.getOffset(), region.getLength());
		
		List undoRanges= new ArrayList(ranges.length);
		for (int i= 0; i < ranges.length; i++) {
			undoRanges.add(ranges[i].clone());
		}
		
		int offset= region.getOffset();
		StyleRange current= undoRanges.size() > 0 ? (StyleRange) undoRanges.get(0) : null;
		int curStart= current != null ? current.start : region.getOffset() + region.getLength();
		int curEnd= current != null ? current.start + current.length : -1;
		int index= 0;
		
		// fill no-style regions
		while (curEnd < region.getOffset() + region.getLength()) {
			// add empty range
			if (curStart > offset) {
				StyleRange undoRange= new StyleRange(offset, curStart - offset, null, null);
				undoRanges.add(index, undoRange);
				index++;
			}
			
			// step
			index++;
			if (index < undoRanges.size()) {
				offset= curEnd;
				current= (StyleRange) undoRanges.get(index);
				curStart= current.start;
				curEnd= current.start + current.length;
			} else if (index == undoRanges.size()) {
				// last one
				offset= curEnd;
				current= null;
				curStart= region.getOffset() + region.getLength();
				curEnd= -1;
			} else
				curEnd= region.getOffset() + region.getLength();
		}
		
		// create modified styles (with background)
		List shadedRanges= new ArrayList(undoRanges.size());
		for (Iterator it= undoRanges.iterator(); it.hasNext(); ) {
			StyleRange range= (StyleRange) ((StyleRange) it.next()).clone();
			shadedRanges.add(range);
			range.background= getHighlightColor(disp);
		}
		
		// set the ranges one by one
		for (Iterator iter= shadedRanges.iterator(); iter.hasNext(); ) {
			text.setStyleRange((StyleRange) iter.next());
			
		}
		
		return (StyleRange[]) undoRanges.toArray(undoRanges.toArray(new StyleRange[0]));
	}

	private void resetViewerBackground(StyleRange[] oldRanges) {
		
		if (oldRanges == null)
			return;
		
		if (fInput == null)
			return;
		
		StyledText text= fInput.fViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		
		// set the ranges one by one
		for (int i= 0; i < oldRanges.length; i++) {
			text.setStyleRange(oldRanges[i]);
		}
	}
	
	private Color getHighlightColor(Display disp) {
		return disp.getSystemColor(SWT.COLOR_GRAY);
	}

	private Color getSelectionColor(Display disp) {
		return disp.getSystemColor(SWT.COLOR_GRAY);
	}

}
