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

import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;

/** An abstract arc-labelled immutable graph that throws an {@link java.lang.UnsupportedOperationException}
 * on all random-access methods.
 * 
 * <p>The main purpose of this class is to be used as a base for the numerous anonymous
 * classes that do not support random access.
 */

public abstract class ArcLabelledImmutableSequentialGraph extends ArcLabelledImmutableGraph {
	/** Throws an {@link java.lang.UnsupportedOperationException}. */
	public int[] successorArray( final int x ) { throw new UnsupportedOperationException(); }
	/** Throws an {@link java.lang.UnsupportedOperationException}. */
	public Label[] labelArray( final int x ) { throw new UnsupportedOperationException(); }
	/** Throws an {@link java.lang.UnsupportedOperationException}. */
	public int outdegree( final int x ) { throw new UnsupportedOperationException(); }
	/** Throws an {@link java.lang.UnsupportedOperationException}. */
	public ArcLabelledNodeIterator nodeIterator( int x ) {
		if ( x == 0 ) return nodeIterator();
		throw new UnsupportedOperationException(); 
	}
	/** Throws an {@link java.lang.UnsupportedOperationException}. */
	public LabelledArcIterator successors( int x ) { throw new UnsupportedOperationException(); }
	/** Returns false.
	 * @return false.
	 */
	@Override
	public boolean randomAccess() { return false; }

	/** Throws an {@link UnsupportedOperationException}. */
	public ArcLabelledImmutableGraph copy() { throw new UnsupportedOperationException(); }
}
