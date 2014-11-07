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

import java.util.Arrays;

/** An abstract (single-attribute) list-of-integers label.
*
* <p>This class provides basic methods for a label holding a list of integers.
* Concrete implementations may impose further requirements on the integer.
* 
* <p>Implementing subclasses must provide constructors, {@link Label#copy()},
* {@link Label#fromBitStream(it.unimi.dsi.io.InputBitStream, int)}, {@link Label#toBitStream(it.unimi.dsi.io.OutputBitStream, int)}
* and possibly override {@link #toString()}.
*/

public abstract class AbstractIntListLabel extends AbstractLabel implements Label {
	/** The key of the attribute represented by this label. */
	protected final String key;
	/** The values of the attribute represented by this label. */
	public int[] value;

	/** Creates an int label with given key and value.
	 * 
	 * @param key the (only) key of this label.
	 * @param value the value of this label.
	 */
	public AbstractIntListLabel( String key, int[] value ) {
		this.key = key;
		this.value = value;
	}

	public String wellKnownAttributeKey() {
		return key;
	}

	public String[] attributeKeys() {
		return new String[] { key };
	}

	public Class<?>[] attributeTypes() {
		return new Class[] { int[].class };
	}

	public Object get( String key ) {
		if ( this.key.equals( key ) ) return value;
		throw new IllegalArgumentException();
	}

	public Object get() {
		return value; 
	}

	public String toString() {
		return key + ":" + Arrays.toString( value );
	}
	
	@Override
	public boolean equals( Object x ) {
		if ( x instanceof AbstractIntListLabel ) return Arrays.equals( value, ( (AbstractIntListLabel)x ).value );
		else return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode( value );
	}
}
