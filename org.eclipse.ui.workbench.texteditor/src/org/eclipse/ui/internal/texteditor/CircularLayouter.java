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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * 
 * 
 * @since 3.0
 */
public class CircularLayouter implements IExpansionLayouter {
	
	class RadialLayout extends Layout {
		private int fRadius= 67;
		
		RadialLayout(int radius){
			fRadius= radius;
		}

		/*
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			// fixed size for now
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			}
			return new Point(2*fRadius, 2*fRadius);
		}

		/*
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
			Control[] children= composite.getChildren();
			
			final int half= ANNOTATION_SIZE / 2;
			// circle radius on which the centers of the composites are laid out
			final int weightRadius= fRadius - half - 3*BORDER_WIDTH; // true for circular annotations...
			// center of the origins (center of the circle, shifted by half an annotation, see above
			final int center= fRadius - half;
			
			int[] locations= weights(weightRadius, center, center, children.length - 1);
			
			if (fUseCenterPosition) {
				children[0].setBounds(fRadius - half - BORDER_WIDTH, fRadius - half - BORDER_WIDTH, ANNOTATION_SIZE + 2*BORDER_WIDTH, ANNOTATION_SIZE + 2*BORDER_WIDTH);
				
				for (int i= 0; i < children.length - 1; i++) {
					children[i + 1].setBounds(locations[2*i], locations[2*i + 1], ANNOTATION_SIZE + 2*BORDER_WIDTH, ANNOTATION_SIZE + 2*BORDER_WIDTH);
				}
			} else {
				for (int i= 0; i < children.length; i++) {
					children[i].setBounds(locations[2*i], locations[2*i + 1], ANNOTATION_SIZE + 2*BORDER_WIDTH, ANNOTATION_SIZE + 2*BORDER_WIDTH);
				}
			}
			
		}
		
	}
	private static final int ANNOTATION_SIZE= 14;
	private static final int BORDER_WIDTH= 2;
	
	
	private int fRadius= 67;
	private boolean fUseCenterPosition;
	
	public CircularLayouter(boolean useCenterPosition) {
		fUseCenterPosition= useCenterPosition;
	}

	public Layout getLayout(int itemCount) {
		return new RadialLayout(fRadius);
	}

	public Object getLayoutData() {
		return null;
	}

	public int getAnnotationSize() {
		return ANNOTATION_SIZE;
	}

	public int getBorderWidth() {
		return BORDER_WIDTH;
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.IExpansionLayouter#getShellRegion()
	 */
	public Region getShellRegion(int itemCount) {
		int items= fUseCenterPosition ? itemCount - 1 : itemCount;
		int circumference= (int) (items * (ANNOTATION_SIZE + BORDER_WIDTH) * 3.0);
		fRadius= (int) (circumference / (2 * Math.PI));
		fRadius= (int) Math.max(fRadius, 2.5 * (ANNOTATION_SIZE + BORDER_WIDTH));
		if (items == 0)
			fRadius= (int) (1.2 * ANNOTATION_SIZE);
		
		Region region = new Region();
		// diameter is itemCount 
		region.add(circle(fRadius, fRadius, fRadius));
//		region.subtract(circle((int) (fRadius / 2.5), fRadius, fRadius));
		
		// we could also cut away the unneeded sector
		
		return region;
	}

	static int[] circle(int r, int offsetX, int offsetY) {
		int[] polygon = new int[8 * r + 4];
		//x^2 + y^2 = r^2
		for (int i = 0; i < 2 * r + 1; i++) {
			int x = i - r;
			int y = (int)Math.sqrt(r*r - x*x);
			polygon[2*i] = offsetX + x;
			polygon[2*i+1] = offsetY + y;
			polygon[8*r - 2*i - 2] = offsetX + x;
			polygon[8*r - 2*i - 1] = offsetY - y;
		}
		return polygon;
	}
	
	static int[] weights(int r, int offsetX, int offsetY, int count) {
		int[] polygon = new int[2 * count];
		double step= 2 * Math.PI / count;
		double d= Math.PI * 1.5;
		for (int i= 0; i < count; d-= step, i++) {
			int x= (int) Math.round(Math.sin(d) * r);
			int y= (int) Math.round(Math.cos(d) * r);
			
			polygon[2*i] = offsetX + x;
			polygon[2*i+1] = offsetY + y;
		}
		return polygon;
	}
}
