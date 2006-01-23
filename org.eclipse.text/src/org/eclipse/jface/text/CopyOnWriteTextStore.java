/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (anton.leherbauer@windriver.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;

/**
 * Copy-on-write <code>ITextStore</code> wrapper.
 * <p>
 * This implementation uses an unmodifiable text store for the initial content.
 * Upon first modification attempt, the unmodifiable store is replaced with
 * a modifiable instance which must be supplied in the constructor.</p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @since 3.2
 */
public class CopyOnWriteTextStore implements ITextStore {

	/**
	 * An unmodifiable String based text store. It is not possible to modify the content
	 * other than using {@link #set}. Trying to {@link #replace} a text range will
	 * throw an <code>UnsupportedOperationException</code>.
	 */
	private static class StringTextStore implements ITextStore {

		/** Represents the content of this text store. */
		private String fText= ""; //$NON-NLS-1$

		/**
		 * Create an empty text store.
		 */
		private StringTextStore() {
			super();
		}

		/**
		 * Create a text store with initial content.
		 * @param text  the initial content
		 */
		private StringTextStore(String text) {
			super();
			set(text);
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#get(int)
		 */
		public char get(int offset) {
			return fText.charAt(offset);
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#get(int, int)
		 */
		public String get(int offset, int length) {
			return fText.substring(offset, offset + length);
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#getLength()
		 */
		public int getLength() {
			return fText.length();
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
		 */
		public void replace(int offset, int length, String text) {
			// modification not supported
			throw new UnsupportedOperationException();
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
		 */
		public void set(String text) {
			fText= text != null ? text : ""; //$NON-NLS-1$
		}

	}

	/** The underlying "real" text store */
	protected ITextStore fTextStore= new StringTextStore();

	/** A modifiable <code>ITextStore</code> instance */
	private final ITextStore fModifiableTextStore;

	/**
	 * Creates an empty text store. The given text store will be used upon first
	 * modification attempt.
	 * 
	 * @param modifiableTextStore
	 *            a modifiable <code>ITextStore</code> instance, may not be
	 *            <code>null</code>
	 */
	public CopyOnWriteTextStore(ITextStore modifiableTextStore) {
		Assert.isNotNull(modifiableTextStore);
		fTextStore= new StringTextStore();
		fModifiableTextStore= modifiableTextStore;
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#get(int)
	 */
	public char get(int offset) {
		return fTextStore.get(offset);
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#get(int, int)
	 */
	public String get(int offset, int length) {
		return fTextStore.get(offset, length);
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#getLength()
	 */
	public int getLength() {
		return fTextStore.getLength();
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
	 */
	public void replace(int offset, int length, String text) {
		if (fTextStore != fModifiableTextStore) {
			String content= fTextStore.get(0, fTextStore.getLength());
			fTextStore= fModifiableTextStore;
			fTextStore.set(content);
		}
		fTextStore.replace(offset, length, text);
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
	 */
	public void set(String text) {
		fTextStore= new StringTextStore(text);
		fModifiableTextStore.set(""); //$NON-NLS-1$
	}

}