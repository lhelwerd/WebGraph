package it.unimi.dsi.webgraph.labelling;

/*		 
 * Copyright (C) 2007-2014 Paolo Boldi 
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

/** An integer represented in fixed width, that fakely provides context sensitivity: 
 * when storing label <var>v</var> onto the arc (<var>x</var>,<var>y</var>),
 * the value <var>v</var>*(<var>x</var>+1) is stored instead. The provided width must
 * be smaller than 32.
 */

public class FakeCSFixedWidthIntLabel extends AbstractIntLabel {
	/** The bit width used to represent the value of this label. */
	private final int width;

	/** Creates a new fixed-width int label.
	 * 
	 * @param key the (only) key of this label.
	 * @param width the label width (in bits).
	 * @param value the value of this label.
	 */
	public FakeCSFixedWidthIntLabel( String key, int width, int value ) {
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
	public FakeCSFixedWidthIntLabel( String key, int width ) {
		this( key, width, 0 );
	}

	/** Creates a new fixed-width integer label using the given key and width
	 *  with value 0.
	 * 
	 * @param arg two strings containing the key and the width of this label.
	 */
	public FakeCSFixedWidthIntLabel( String... arg ) {
		this( arg[ 0 ], Integer.parseInt( arg[ 1 ] ) );
	}

	public Label copy() {
		return new FakeCSFixedWidthIntLabel( key, width, value );
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

	public int fromBitStream( InputBitStream inputBitStream, int source ) throws IOException, UnsupportedOperationException {
		int v = inputBitStream.readInt( width );
		value = v / ( source + 1 );
		return width;
	}

	public int toBitStream( OutputBitStream outputBitStream, int source ) throws IOException, UnsupportedOperationException {
		return outputBitStream.writeInt( ( source + 1 ) * value, width );		
	}
	
	public String toSpec() {
		return this.getClass().getName() + "(" + key + ","  + width + ")";
	}
}
