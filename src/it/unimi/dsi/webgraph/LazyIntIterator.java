package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2007-2014 Sebastiano Vigna 
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

/** A lazy iterator over the integers.
 * 
 * <p>An instance of this class represent a (skippable) iterator over the integers.
 * The iterator is exhausted when an implementation-dependent special marker is
 * returned. This fully lazy architecture halves the number of method
 * calls w.r.t. Java's eager iterators.
 */

public interface LazyIntIterator {
	/** The next integer returned by this iterator, or the special
	 * marker if this iterator is exhausted.
	 * 
	 * @return next integer returned by this iterator, or the special
	 * marker if this iterator is exhausted.
	 */
	public int nextInt();
	
	/** Skips a given number of elements.
	 * 
	 * @param n the number of elements to skip.
	 * @return the number of elements actually skipped (which might
	 * be less than <code>n</code> if this iterator is exhausted).
	 */
	public int skip( int n );	
}
