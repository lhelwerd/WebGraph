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

/** An abstract implementation of a lazy integer iterator, implementing {@link #skip(int)}
 * by repeated calls to {@link LazyIntIterator#nextInt() nextInt()}. */

public abstract class AbstractLazyIntIterator implements LazyIntIterator {

	public int skip( final int n ) {
		int i;
		for( i = 0; i < n && nextInt() != -1; i++ );
		return i;
	}

}
