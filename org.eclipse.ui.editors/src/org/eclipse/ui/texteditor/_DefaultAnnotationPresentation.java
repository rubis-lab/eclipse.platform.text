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

package org.eclipse.ui.texteditor;


import org.eclipse.core.resources.IMarker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source._AbstractAnnotationPresentation;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * Default implementation of <code>IAnnotationPresentation</code>.
 * This class may be instantiated or be subclassed.
 *
 * @since 3.0
 */
public class _DefaultAnnotationPresentation extends _AbstractAnnotationPresentation  {
	
	
	/** Internal image registry */
	private static Map fgImageRegistry;
	
	/**
	 * Returns an image for the given display as specified by the given image descriptor.
	 * @param display the display
	 * @param descriptor the image descriptor
	 * @return an image for the display as specified by the descriptor
	 */
	protected static Image getImage(Display display, ImageDescriptor descriptor) {
		Map map= getImageRegistry(display);
		Image image= (Image) map.get(descriptor);
		if (image == null) {
			image= descriptor.createImage();
			map.put(descriptor, image);
		}
		return image;
	}
	
	/**
	 * Disposes the image previously created for the given image descriptor.
	 * 
	 * @param display the display
	 * @param descriptor the image descriptor
	 */
	protected static void disposeImage(Display display, ImageDescriptor descriptor) {
		Map map= getImageRegistry(display);
		Image image= (Image) map.get(descriptor);
		if (image != null) {
			if (!image.isDisposed())
				image.dispose();
			map.remove(descriptor);
		}
	}
	
	/**
	 * Returns an image registry for the given display. If no such registry exists
	 * the resgitry is created.
	 * @param display the display
	 * @return the image registry for the given display
	 */
	protected static Map getImageRegistry(Display display) {
		if (fgImageRegistry == null) {
			fgImageRegistry= new HashMap();
			display.disposeExec(new Runnable() {
				public void run() {
					if (fgImageRegistry != null) {
						Map map= fgImageRegistry;
						fgImageRegistry= null;
						Iterator e= map.values().iterator();
						while (e.hasNext()) {
							Image image= (Image) e.next();
							if (!image.isDisposed())
								image.dispose();
						}
					}
				}
			});
		}
		return fgImageRegistry;
	}
	
	private Annotation fAnnotation;
	private AnnotationPreference fPreference;
	private ImageDescriptor fImageDescriptor;
	
	
	public _DefaultAnnotationPresentation(Annotation annotation, AnnotationPreference preference, Object adapterKey) {
		super(annotation.getAnnotationType(), adapterKey);
		fAnnotation= annotation;
		fPreference= preference;
	}
	
	/*
	 * @see org.eclipse.jface.text.source._IAnnotationPresentation#getLayer()
	 */
	public int getLayer() {
		if (fPreference != null)
			return fPreference.getPresentationLayer();
		return IAnnotationAccessExtension.DEFAULT_LAYER;
	}
	
	/*
	 * @see org.eclipse.jface.text.source._AnnotationPresentation#getAnnotationTypeLabel()
	 */
	public String getAnnotationTypeLabel() {
		if (fPreference != null)
			return fPreference.getPreferenceLabel();
		return fAnnotation.getAnnotationType();
	}
	
	/*
	 * @see org.eclipse.jface.text.source._IAnnotationPresentation#getText()
	 */
	public String getText() {
		IAnnotationPresenter presenter= getAnnotationPresenter();
		if (presenter != null)
			presenter.getText(fAnnotation);
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.AnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
		IAnnotationPresenter presenter= getAnnotationPresenter();
		if (presenter != null) {
			disposeImage(canvas.getDisplay());
			presenter.paint(fAnnotation, gc, canvas, r);
		} else {
			Image image= getImage(canvas.getDisplay());
			if (image != null)
				drawImage(image, gc, canvas, r, SWT.CENTER, SWT.TOP);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.AnnotationPresentation#annotationDataChanged(org.eclipse.jface.text.source.Annotation)
	 */
	public void annotationChanged(Annotation annotation) {
		IAnnotationPresenter presenter= getAnnotationPresenter();
		if (presenter != null)
			presenter.annotationDataChanged(annotation);
		super.annotationChanged(annotation);
	}
	
	private Image getImage(Display display) {
		if (fImageDescriptor == null)
			fImageDescriptor= fPreference.getDefaultIcon();
		return getImage(display, fImageDescriptor);
	}
	
	private void disposeImage(Display display) {
		if (fImageDescriptor != null)
			disposeImage(display, fImageDescriptor);
		fImageDescriptor= null;
	}
	
	private IAnnotationPresenter getAnnotationPresenter() {
//		TODO desolve MarkerAnnotationPresenter into static icon contribution for markerAnnotationSpecification...
//		The only annotation presenter will initially be provided by debug
//		return fPreference.getAnnotationPresenter();
		return new MarkerAnnotationPresenter(fAnnotation);
	}
	
	
	
	// -----------------------------------------------------------------------------------------
	
	
	private static class MarkerAnnotationPresenter implements IAnnotationPresenter {
		
		private MarkerAnnotation fMarkerAnnotation;
		private ImageDescriptor fImageDescriptor;
		private String fText;
		
		public MarkerAnnotationPresenter(Annotation annotation) {
			if (annotation instanceof MarkerAnnotation)
				fMarkerAnnotation= (MarkerAnnotation) annotation;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IAnnotationImagePainter#paint(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle r) {
			Image image= getImage(canvas.getDisplay());
			if (image != null)
				drawImage(image, gc, canvas, r, SWT.CENTER, SWT.TOP);
		}
		
		private Image getImage(Display display) {
			if (fImageDescriptor == null)
				fImageDescriptor= createImageDescriptor();
			return _DefaultAnnotationPresentation.getImage(display, fImageDescriptor);
		}
		
		/*
		 * @see org.eclipse.ui.texteditor.IAnnotationPresenter#getText(org.eclipse.jface.text.source.Annotation)
		 */
		public String getText(Annotation annotation) {
			if (fText == null)
				fText= computeText();
			return fText;
		}

		/*
		 * @see org.eclipse.ui.texteditor.IAnnotationImagePainter#annotationDataChanged(org.eclipse.jface.text.source.Annotation)
		 */
		public void annotationDataChanged(Annotation annotation) {
			fImageDescriptor= null;
			fText= null;
		}
		
		private ImageDescriptor createImageDescriptor() {
			
			if (fMarkerAnnotation == null)
				return null;
			
			IMarker marker= fMarkerAnnotation.getMarker();
			if (marker == null || !marker.exists())
				return null;
			
			String name= null;
			if (MarkerUtilities.isMarkerType(marker, IMarker.TASK)) {
				name= ISharedImages.IMG_OBJS_TASK_TSK;
			} else if (MarkerUtilities.isMarkerType(marker, IMarker.BOOKMARK)) {
				name= ISharedImages.IMG_OBJS_BKMRK_TSK;
			} else if (MarkerUtilities.isMarkerType(marker, IMarker.PROBLEM)) {
				switch (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO)) {
					case IMarker.SEVERITY_INFO:
						name= ISharedImages.IMG_OBJS_INFO_TSK;
						break;
					case IMarker.SEVERITY_WARNING:
						name= ISharedImages.IMG_OBJS_WARN_TSK;
						break;
					case IMarker.SEVERITY_ERROR:
						name= ISharedImages.IMG_OBJS_ERROR_TSK;
						break;
				}
			}
			
			ISharedImages sharedImages= PlatformUI.getWorkbench().getSharedImages();
			return sharedImages.getImageDescriptor(name);
		}
		
		private String computeText() {
			if (fMarkerAnnotation == null)
				return null;
			return fMarkerAnnotation.getMessage();
		}
	}
}
