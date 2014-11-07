package it.unimi.dsi.webgraph.labelling;

/*		 
 * Copyright (C) 2007-2014 Paolo Boldi and Sebastiano Vigna 
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */


import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

import java.io.IOException;

/** An integer represented in fixed width. The provided width must
 * be smaller than 32.
 */

public class FixedWidthIntLabel extends AbstractIntLabel {
	/** The bit width used to represent the value of this label. */
	protected final int width;

	/** Creates a new fixed-width int label.
	 * 
	 * @param key the (only) key of this label.
	 * @param width the label width (in bits).
	 * @param value the value of this label.
	 */
	public FixedWidthIntLabel( String key, int width, int value ) {
		super( key, value );
		if ( width < 0 || width > 31 ) throw new IllegalArgumentException( "Width out of range: " + width );
		if ( value < 0 || value >= 1L << width ) throw new IllegalArgumentException( "Value out of range: " + Integer.toString( value ) );
		this.width = width;
	}

	/** Creates a new fixed-width int label of value 0.
	 * 
	 * @param key the (only) key of this label.
	 * @param width the label width (in bits).
	 */
	public FixedWidthIntLabel( String key, int width ) {
		this( key, width, 0 );
	}

	/** Creates a new fixed-width integer label using the given key and width
	 *  with value 0.
	 * 
	 * @param arg two strings containing the key and the width of this label.
	 */
	public FixedWidthIntLabel( String... arg ) {
		this( arg[ 0 ], Integer.parseInt( arg[ 1 ] ) );
	}

	public Label copy() {
		return new FixedWidthIntLabel( key, width, value );
	}

	public int fromBitStream( final InputBitStream inputBitStream, final int sourceUnused ) throws IOException {
		value = inputBitStream.readInt( width );
		return width;
	}

	public int toBitStream( final OutputBitStream outputBitStream, final int sourceUnused ) throws IOException {
		return outputBitStream.writeInt( value, width );
	}

	/** Returns the width of this label (as provided at construction time).
	 * @return the width of this label.
	 */
	public int fixedWidth() {
		return width;
	}	

	public String toString() {
		return key + ":" + value + " (width:" + width + ")";
	}
	
	public String toSpec() {
		return this.getClass().getName() + "(" + key + ","  + width + ")";
	}
}
