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
 * them using a inclusion-exclusion block list.
 * 
 *  <p>A <em>mask</em> is an array of integers. The sum of the values contained in the mask
 *  must not exceed the number of elements returned by the underlying iterator. Moreover, all integers in the mask
 *  must be positive, except possibly for the first one, which may be zero.
 *
 *  <P>Mask values are interpreted as specifying inclusion-exclusion blocks.
 *  Suppose that the underlying iterator returns <var>N</var> values, and that the mask is
 *  <var>n</var><sub>0</sub>, <var>n</var><sub>1</sub>, &hellip;, <var>n</var><sub>k</sub>.
 *  Then, the first <var>n</var><sub>0</sub> values returned by the underlying iterator must be kept,
 *  the next <var>n</var><sub>1</sub> values must be ignored, the next <var>n</var><sub>2</sub> must be
 *  kept and so on. The last <var>N</var>&minus;(<var>n</var><sub>0</sub>+&hellip;+<var>n</var><sub>k</sub>)
 *  must be kept if <var>k</var> is odd, and must be ignored otherwise.
 *  An instance of this class will returns the kept values only, in increasing order.
 */

public class MaskedIntIterator implements LazyIntIterator {
	private final static boolean ASSERTS = false;
	
	/** The underlying iterator. */
	private final LazyIntIterator underlying;
	/** The mask. */
	private final int mask[];
	/** The mask. */
	private final int maskLen;
	/** This index in mask always represents an exclusion block. */
	private int currMask;
	/** How many integers are left in the current inclusion block. If <code>0</code> everything left must be discarded; if
	 *	<code>-1</code> all remaining values must be kept. */
	private int left;

	/** Creates a new masked iterator using a given mask and underlying iterator.
	 * 
	 * @param mask a mask, or <code>null</code>, meaning an empty mask (everything is copied).
	 * @param underlying an underlying iterator.
	 */
	public MaskedIntIterator( final int mask[], final LazyIntIterator underlying ) {
		this( mask, mask == null ? 0 : mask.length, underlying );
	}

	/** Creates a new masked iterator using a given mask, mask length and underlying iterator.
	 * 
	 * @param mask a mask, or <code>null</code>, meaning an empty mask (everything is copied).
	 * @param maskLen an explicit mask length.
	 * @param underlying an underlying iterator.
	 */
	public MaskedIntIterator( final int mask[], final int maskLen, final LazyIntIterator underlying ) {

		this.mask = mask;
		this.maskLen = maskLen;
		this.underlying = underlying;

		if ( maskLen != 0 ) {
			left = mask[ currMask++ ];
			advance();
		}
		else left = -1;
	}

	public int nextInt() {
		if ( left == 0 ) return -1;
 		final int next = underlying.nextInt();

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
			underlying.skip( mask[ currMask++ ] );
			left = currMask < maskLen ? mask[ currMask++ ] : -1;
		}
	}

	public int skip( final int n ) {
		int skipped = 0;

		while( skipped < n && left != 0 ) {
			if ( left == -1 ) {
				final int result = underlying.skip( n - skipped );
				skipped += result;
				if ( skipped < n ) break;
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
