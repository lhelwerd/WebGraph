package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2003-2014 Paolo Boldi and Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;

/** This interface extends {@link IntIterator} and is used to scan a graph, that is, to read its nodes and their successor lists
 *  sequentially. The {@link #nextInt()} method returns the node that will be scanned. After a call to this method,  calling
 *  {@link #successors()} or {@link #successorArray()} will return the list of successors.
 *  
 *  <p>Implementing subclasses can override either {@link #successors()} or 
 *  {@link #successorArray()}, but at least one of them <strong>must</strong> be implemented.
 */

public abstract class NodeIterator extends AbstractIntIterator {
	
	/** Returns the outdegree of the current node.
	 *
	 *  @return the outdegree of the current node.
	 */
	public abstract int outdegree();

	/** Returns a lazy iterator over the successors of the current node.  The iteration terminates
	 * when -1 is returned.
	 * 
	 * <P>This implementation just wraps the array returned by {@link #successorArray()}.
	 * 
	 *  @return a lazy iterator over the successors of the current node.
	 */
	public LazyIntIterator successors() {
		return LazyIntIterators.wrap( successorArray(), outdegree() );
	}

	/** Returns a reference to an array containing the successors of the current node.
	 * 
	 * <P>The returned array may contain more entries than the outdegree of the current node.
	 * However, only those with indices from 0 (inclusive) to the outdegree of the current node (exclusive)
	 * contain valid data.
	 * 
	 * <P>This implementation just unwrap the iterator returned by {@link #successors()}.
	 * 
	 * @return an array whose first elements are the successors of the current node; the array must not
	 * be modified by the caller.
	 */
	public int[] successorArray() {
		final int[] successor = new int[ outdegree() ];
		LazyIntIterators.unwrap( successors(), successor );
		return successor;
	}
}
