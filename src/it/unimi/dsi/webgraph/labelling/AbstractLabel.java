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

/** An abstract implementation throwing an {@link IllegalArgumentException} on all primitive-type methods. */

public abstract class AbstractLabel implements Label {

	public byte getByte() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public short getShort( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public int getInt( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public long getLong( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public float getFloat( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public double getDouble( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public char getChar( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public boolean getBoolean( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public byte getByte( String key ) throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public short getShort() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public int getInt() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public long getLong() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public float getFloat() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public double getDouble() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public char getChar() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}

	public boolean getBoolean() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}
}
