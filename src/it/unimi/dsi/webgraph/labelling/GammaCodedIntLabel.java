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

/** A natural number represented in {@linkplain OutputBitStream#writeGamma(int) &gamma; coding}. */

public class GammaCodedIntLabel extends AbstractIntLabel {

	/** Creates a new label with given key and value.
	 * 
	 * @param key the (only) key.
	 * @param value the value of this label.
	 */
	public GammaCodedIntLabel( String key, int value ) {
		super( key, value );
		if ( value < 0 ) throw new IllegalArgumentException( "Value cannot be negative: " + value );
	}

	/** Creates a new &gamma;-coded label using the given key and value 0.
	 * 
	 * @param key one string containing the key of this label.
	 */
	public GammaCodedIntLabel( String... key  ) {
		super( key[ 0 ], 0 );
	}

	public GammaCodedIntLabel copy() {
		return new GammaCodedIntLabel( key, value );
	}

	/** Fills this label {@linkplain InputBitStream#readGamma() reading a &gamma;-coded natural number}
	 *  from the given input bit stream.
	 * 
	 * @param inputBitStream an input bit stream.
	 * @return the number of bits read to fill this lbael.
	 */
	
	public int fromBitStream( InputBitStream inputBitStream, final int sourceUnused ) throws IOException {
		long prevRead = inputBitStream.readBits();
		value = inputBitStream.readGamma();
		return (int)( inputBitStream.readBits() - prevRead );
	}

	/** Writes this label {@linkplain OutputBitStream#writeGamma(int) as a &gamma;-coded natural number}
	 *  to the given output bit stream.
	 * 
	 * @param outputBitStream an output bit stream.
	 * @return the number of bits written.
	 */

	public int toBitStream( OutputBitStream outputBitStream, final int sourceUnused ) throws IOException {
		return outputBitStream.writeGamma( value );
	}

	/** Returns -1 (as this label has not a fixed width).
	 * @return -1.
	 */
	
	public int fixedWidth() {
		return -1;
	}

	public String toString() {
		return key + ":" + value + " (gamma)";
	}

	public String toSpec() {
		return this.getClass().getName() + "(" + key + ")";
	}

}
