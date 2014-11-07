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

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

import java.util.Random;

import org.junit.Test;

public class MaskedIntIteratorTest {

	public void test( final int length, final int numberOfZeroes ) {
		long seed = System.currentTimeMillis();
		Random random = new Random( seed );
		System.err.println( "Seed: " + seed );
		// Reads the length and number of 0s
		final int x[] = new int[ length ];
		boolean keep[] = new boolean[ length ];
		IntArrayList res = new IntArrayList();
		IntArrayList blocks = new IntArrayList();
		int i, j, p = 0;
		boolean dep;

		// Generate
		for ( i = 0; i < length; i++ ) p = x[ i ] = p + random.nextInt( 1000 );
		for ( i = 0; i < length-numberOfZeroes; i++ ) keep[ i ] = true;
		for ( i = 0; i < length; i++ ) {
			j = i + (int)( Math.random() * ( length - i ) );
			dep = keep[ i ]; keep[ i ] = keep[ j ]; keep[ j ] = dep;
		}

		// Compute result
		for ( i = 0; i < length; i++ ) if ( keep[ i ] ) res.add( x[ i ] );
		res.trim();
		int result[] = res.elements();

		// Prepare blocks
		boolean lookAt = true;
		int curr = 0;
		for ( i = 0; i < length; i++ ) {
			if ( keep[ i ] == lookAt ) curr++;
			else {
				blocks.add( curr );
				lookAt = !lookAt;
				curr = 1;
			}
		}
		blocks.trim();
		final int bs[] = blocks.elements();

		// Output 
		System.out.println( "GENERATED:" );
		for ( i = 0; i < length; i++ ) {
			if ( keep[ i ] ) System.out.print( '*' );
			System.out.print( x[ i ] + "  " );
		}
		System.out.println( "\nBLOCKS:" );
		for ( i = 0; i < bs.length; i++ )
			System.out.print( bs[ i ] + "  " );
		System.out.println( "\nEXPECTED RESULT:" );
		for ( i = 0; i < result.length; i++ ) 
			System.out.print( result[ i ] + "  " );
		System.out.println();

		LazyIntIterator maskedIterator = new MaskedIntIterator( bs, LazyIntIterators.lazy( new IntArrayList( x ).iterator() ) );

		for ( i = 0; i < result.length; i++ ) assertEquals( i + ": ", result[ i ], maskedIterator.nextInt() );
		assertEquals( -1, maskedIterator.nextInt() );

		// Test skips
		maskedIterator = new MaskedIntIterator( bs, LazyIntIterators.lazy( new IntArrayList( x ).iterator() ) );
		IntIterator results = IntIterators.wrap( result );
		
		for ( i = 0; i < result.length; i++ ) {
			int toSkip = random.nextInt( 5 );
			assertEquals( results.skip( toSkip ), maskedIterator.skip( toSkip ) );
			if ( results.hasNext() ) assertEquals( i + ": ", results.nextInt(), maskedIterator.nextInt() );
		}
		assertEquals( -1, maskedIterator.nextInt() );

	}

	@Test
	public void test() {
		for( int i = 0; i < 20; i++ )
			for( int j = 0; j < 20; j++ )
				test( i, j );
	}

}
