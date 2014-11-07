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

import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;

/** An iterator returning nodes, their successors and labels on the arcs.
 * 
 * <p>The purpose of this abstract implementation is to override covariantly
 * the return type of of {@link NodeIterator#successors()}, so that
 * it has to be a {@link ArcLabelledNodeIterator.LabelledArcIterator}, and provide a general
 * implementation of a new {@link #labelArray()} method that returns
 * the labels of the arcs going out of the current node as an array.
 */
public abstract class ArcLabelledNodeIterator extends NodeIterator {

	/** An iterator returning successor and the labels of the arcs toward them.
	 *  The label can be accessed through {@link #label()}, which must be called just after
	 *  advancing the iterator.
	 *   
	 *  <p><strong>Warning</strong>: the returned label can be the same object
	 *  upon several calls to {@link #label()}; if you need to store it, 
	 *  you should {@linkplain Label#copy() copy it}.
	 */
	public interface LabelledArcIterator extends LazyIntIterator {
		/** The label of arc leading to the last returned successor. 
		 * 
		 * @return the label of arc leading to the last returned successor.
		 */
		public Label label();
	}

	public abstract ArcLabelledNodeIterator.LabelledArcIterator successors();
	
	/** Returns a reference to an array containing the labels of the arcs going out of the current node
	 * in the same order as the order in which the corresponding successors are returned by {@link #successors()}.
	 * 
	 * <P>The returned array may contain more entries than the outdegree of the current node.
	 * However, only those with indices from 0 (inclusive) to the outdegree of the current node (exclusive)
	 * contain valid data.
	 * 
	 * <P>This implementation just unwrap the iterator returned by {@link #successors()} and
	 * writes in a newly allocated array copies of the labels returned by {@link LabelledArcIterator#label()}.
	 * 
	 * @return an array whose first elements are the labels of the arcs going 
	 * out of the current node; the array must not be modified by the caller.
	 */
	
	public Label[] labelArray() {
		return unwrap( successors(), outdegree() );
	}

	/** Returns a new array of labels filled with exactly <code>howMany</code> labels from the given iterator.
	 *  Note that the iterator is required to have at least as many labels as needed.
	 * 
	 * @param iterator the iterator.
	 * @param howMany the number of labels.
	 * @return the new array where labels are copied.
	 */
	protected static Label[] unwrap( final ArcLabelledNodeIterator.LabelledArcIterator iterator, final int howMany ) {
		final Label[] result = new Label[ howMany ];
		for ( int i = 0; i < howMany; i++ ) {
			iterator.nextInt();
			result[ i ] = iterator.label().copy();
		}
		return result;
	}
}