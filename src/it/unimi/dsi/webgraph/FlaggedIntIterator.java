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

/** An iterator returning the element of an underlying iterator but filters 
 * them using a mask block list and a flag list.
 * 
 *  <p>A <em>mask</em> is an array of integers. The sum of the values contained in the mask
 *  must not exceed the number of elements returned by the underlying iterator. Moreover, all integers in the mask
 *  must be positive.
 *
 *  The flag list specifies what should happen with the elements in the block: 0 = exact copy, 1 = skip, 2 = ref+1, 3 = ref+2
 *
 *  An instance of this class will returns the kept values only, in increasing order.
 *
 *  Based on MaskedIntIterator.
 */

public class FlaggedIntIterator implements LazyIntIterator {
	private final static boolean ASSERTS = false;
	
	/** The underlying iterator. */
	private final LazyIntIterator underlying;
	/** The mask. */
	private final int mask[];
	/** The mask. */
	private final int maskLen;
	/** The flags. */
	private final int flags[];
	/** This index in mask. */
	private int currMask;
	/** How many integers are left in the current block. If <code>0</code> we must go to the next block; if
	 *	<code>-1</code> all remaining values must be kept. */
	private int left;

	/** Creates a new masked iterator using a given mask and underlying iterator.
	 * 
	 * @param mask a mask, or <code>null</code>, meaning an empty mask (everything is copied).
	 * @param flags a flags list
	 * @param underlying an underlying iterator.
	 */
	public FlaggedIntIterator( final int mask[], final int flags[], final LazyIntIterator underlying ) {
		this( mask, mask == null ? 0 : mask.length, flags, underlying );
	}

	/** Creates a new masked iterator using a given mask, mask length and underlying iterator.
	 * 
	 * @param mask a mask, or <code>null</code>, meaning an empty mask (everything is copied).
	 * @param maskLen an explicit mask length.
	 * @param flags a flags list
	 * @param underlying an underlying iterator.
	 */
	public FlaggedIntIterator( final int mask[], final int maskLen, final int flags[], final LazyIntIterator underlying ) {

		this.mask = mask;
		this.maskLen = maskLen;
		this.flags = flags;
		this.underlying = underlying;

		left = 0;
		currMask = -1;
		advance();
	}

	private int flag( final int next ) {
		if ( currMask >= maskLen ) {
			return next;
		}
		if ( flags[ currMask ] == 0 ) {
			return next;
		}
		else if ( flags[ currMask ] == 1 ) {
			return -1;
		}
		return next + flags[ currMask ] - 1;

	}

	public int nextInt() {
		if ( left == 0 ) return -1;
 		final int next = flag( underlying.nextInt() );

		if ( left == -1 || next == -1 ) return next;
		if ( left > 0 ) {
			left--;
			advance();
		}
		return next;
	}

	private void advance() {
		if ( ASSERTS ) assert left != -1;
		if ( left == 0 && currMask < maskLen ) {
			currMask++;
			if ( currMask < maskLen && flags[ currMask ] == 1 ) {
				underlying.skip( mask[ currMask ] );
				currMask++;
			}
			left = currMask < maskLen ? mask[ currMask ] : -1;
		}
	}

	public int skip( final int n ) {
		int skipped = 0;

		while( skipped < n && left != 0 ) {
			if ( left == -1 ) {
				final int result = underlying.skip( n - skipped );
				skipped += result;
				if ( skipped < n ) break; // End of underlying list reached
			}
			else {
				if ( n - skipped < left ) {
					underlying.skip( n - skipped );
					left -= ( n - skipped );
					return n;
				}
				else {
					underlying.skip( left );
					skipped += left;
					left = 0;
					advance();
				}
			}
		}
			
		return skipped;
	}
}
