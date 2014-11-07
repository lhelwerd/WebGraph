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
 

/** An iterator returning the integers contained in a sequence of intervals. */
public class IntIntervalSequenceIterator implements LazyIntIterator {
	
	/** The left extremes. */
	private final int left[];
	/** The lengths. */
	private final int len[];
	/** The number of remaining intervals (including the current one). It is zero exactly when the iterator is exhausted. */
	private int remaining;
	/** The index of the current interval. */
	private int currInterval;
	/** The current position in the current interval: the next integer to be output is {@link #currLeft} + {@link #currIndex}. */
	private int currIndex;
	/** The left point of the current interval. */
	private int currLeft;

	/** Creates a new interval-sequence iterator by specifying
	 * arrays of left extremes and lengths. Note that the two arrays are <em>not</em> copied, 
	 * so they are supposed not to be changed during the iteration.
	 * 
	 * @param left an array containing the left extremes of the intervals generating this iterator.
	 * @param len an array (of the same length as <code>left</code>) containing the number of integers (greater than zero) in each interval.
	 */

	public IntIntervalSequenceIterator( final int left[], final int len[] ) {
		this( left, len, left.length );
	}
	
	/** Creates a new interval-sequence iterator by specifying
	 * arrays of left extremes and lengths, and the number of valid entries. Note that the two arrays are <em>not</em> copied, 
	 * so they are supposed not to be changed during the iteration.
	 * 
	 * @param left an array containing the left extremes of the intervals generating this iterator.
	 * @param len an array (of the same length as <code>left</code>) containing the number of integers (greater than zero) in each interval.
	 * @param n the number of valid entries in <code>left</code> and <code>len</code>.
	 */

	public IntIntervalSequenceIterator( final int left[], final int len[], final int n ) {
		this.left = left;
		this.len = len;
		this.remaining = n;
		if ( n != 0 ) currLeft = left[ 0 ];
	}
	
	private void advance() {
		remaining--;
		if ( remaining != 0 ) currLeft = left[ ++currInterval ];
		currIndex = 0;
	}

	public int nextInt() {
		if ( remaining == 0 ) return -1;

		final int next = currLeft + currIndex++;
		if ( currIndex == len[ currInterval ] ) advance();
		return next;
	}

	public int skip( final int n ) {
		int skipped = 0;

		while( skipped < n && remaining != 0 ) {
			if ( n - skipped < len[ currInterval ] - currIndex ) {
				currIndex += ( n - skipped );
				return n;
			}
			else {
				skipped += len[ currInterval ] - currIndex;
				advance();
			}
		}
			
		return skipped;
	}
}
