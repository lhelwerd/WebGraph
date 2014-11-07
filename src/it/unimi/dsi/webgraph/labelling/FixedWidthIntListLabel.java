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


import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;

import java.io.IOException;
import java.util.Arrays;

/** A list of integers represented in fixed width. The provided width must
 * be smaller than 32. Each list is prefixed by its length written
 * in {@linkplain OutputBitStream#writeGamma(int) &gamma; coding}.
 */

public class FixedWidthIntListLabel extends AbstractIntListLabel {
	/** The bit width used to represent the value of this label. */
	private final int width;

	/** Creates a new fixed-width int label.
	 * 
	 * @param key the (only) key of this label.
	 * @param width the label width (in bits).
	 * @param value the value of this label.
	 */
	public FixedWidthIntListLabel( String key, int width, int[] value ) {
		super( key, value );
		if ( width < 0 || width > 31 ) throw new IllegalArgumentException( "Width out of range: " + width );
		for( int i = value.length; i-- != 0; ) if ( value[ i ] < 0 || value[ i ] >= 1L << width ) throw new IllegalArgumentException( "Value out of range: " + Integer.toString( value[ i ] ) );
		this.width = width;
	}

	/** Creates a new fixed-width label with an empty list.
	 * 
	 * @param key the (only) key of this label.
	 * @param width the label width (in bits).
	 */
	public FixedWidthIntListLabel( String key, int width ) {
		this( key, width, IntArrays.EMPTY_ARRAY );
	}

	/** Creates a new fixed-width integer label using the given key and width
	 *  with an empty list.
	 * 
	 * @param arg two strings containing the key and the width of this label.
	 */
	public FixedWidthIntListLabel( String... arg ) {
		this( arg[ 0 ], Integer.parseInt( arg[ 1 ] ) );
	}

	public Label copy() {
		return new FixedWidthIntListLabel( key, width, value.clone() );
	}

	public int fromBitStream( InputBitStream inputBitStream, final int sourceUnused ) throws IOException {
		long readBits = inputBitStream.readBits();
		value = new int[ inputBitStream.readGamma() ];
		for( int i = 0; i < value.length; i++ ) value[ i ] = inputBitStream.readInt( width );
		return (int)( inputBitStream.readBits() - readBits );
	}

	public int toBitStream( OutputBitStream outputBitStream, final int sourceUnused ) throws IOException {
		int bits = outputBitStream.writeGamma( value.length );
		for( int i = 0; i < value.length; i++ ) bits += outputBitStream.writeInt( value[ i ], width );
		return bits;
	}

	/** Returns -1 (the fixed width refers to a single integer, not to the entire list).
	 * @return -1;
	 */
	public int fixedWidth() {
		return -1;
	}	

	public String toString() {
		return key + ":" + Arrays.toString( value ) + " (width:" + width + ")";
	}

	public String toSpec() {
		return this.getClass().getName() + "(" + key + ","  + width + ")";
	}
}
