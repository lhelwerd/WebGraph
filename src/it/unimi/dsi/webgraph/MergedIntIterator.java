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

import it.unimi.dsi.fastutil.ints.IntIterator;

/** An iterator returning the union of the integers returned by two {@link IntIterator}s.
 *  The two iterators must return integers in an increasing fashion; the resulting
 *  {@link MergedIntIterator} will do the same. Duplicates will be eliminated.
 */

public class MergedIntIterator implements LazyIntIterator {
	/** The first component iterator. */
	private final LazyIntIterator it0;
	/** The second component iterator. */
	private final LazyIntIterator it1;
	/** The last integer returned by {@link #it0}. */
	private int curr0;
	/** The last integer returned by {@link #it1}. */
	private int curr1;

	/** Creates a new merged iterator by merging two given iterators; the resulting iterator will not emit more than <code>n</code> integers.
	 * 
	 * @param it0 the first (monotonically nondecreasing) component iterator.
	 * @param it1 the second (monotonically nondecreasing) component iterator.
	 */
	public MergedIntIterator( final LazyIntIterator it0, final LazyIntIterator it1 ) {
		this.it0 = it0;
		this.it1 = it1;
		curr0 = it0.nextInt();
		curr1 = it1.nextInt();
	}

	public int nextInt() {
		if ( curr0 < curr1 ) {
			if ( curr0 == -1 ) {
				final int result = curr1;
				curr1 = it1.nextInt();
				return result;
			}
			
			final int result = curr0;
			curr0 = it0.nextInt();
			return result;
		} 
		else {
			if ( curr1 == -1 ) {
				final int result = curr0;
				curr0 = it0.nextInt();
				return result;
			}
			
			final int result = curr1;
			if ( curr0 == curr1 ) curr0 = it0.nextInt();
			curr1 = it1.nextInt();
			return result;
		}
	}
	
	public int skip( final int s ) {
		int i;
		for( i = 0; i < s; i++ ) {
			if ( curr0 == -1 && curr1 == -1 ) break;

			if ( curr0 < curr1 ) {
				if ( curr0 == -1 ) curr1 = it1.nextInt();
				else curr0 = it0.nextInt();
			} 
			else {
				if ( curr1 == -1 ) curr0 = it0.nextInt();
				else  {
					if ( curr0 == curr1 ) curr0 = it0.nextInt();
					curr1 = it1.nextInt();
				}
			}
		}
		return i;
	}
}
