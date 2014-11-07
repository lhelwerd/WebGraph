package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2003-2014 Paolo Boldi 
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
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.Random;

import org.junit.Test;

public class MergedIntIteratorTest {

	public void testMerge( int n0, int n1 ) {
		Random r = new Random();
		int x0[] = new int[ n0 ];
		int x1[] = new int[ n1 ];
		int i, p = 0;

		// Generate
		for ( i = 0; i < n0; i++ ) p = x0[ i ] = p + r.nextInt( 10 );
		p = 0;
		for ( i = 0; i < n1; i++ ) p = x1[ i ] = p + (int)( Math.random() * 10 );
		
		IntAVLTreeSet s0 = new IntAVLTreeSet( x0 );
		IntAVLTreeSet s1 = new IntAVLTreeSet( x1 );
		IntAVLTreeSet res = new IntAVLTreeSet( s0 );
		res.addAll( s1 );

		MergedIntIterator m = new MergedIntIterator( LazyIntIterators.lazy( s0.iterator() ), LazyIntIterators.lazy( s1.iterator() ) );
		IntIterator it = res.iterator();

		int x;
		while ( ( x = m.nextInt() ) != -1 ) assertEquals( it.nextInt(), x );
		assertEquals( Boolean.valueOf( it.hasNext() ), Boolean.valueOf( m.nextInt() != -1 ) );
	}

	@Test
	public void testMerge() {
		for( int i = 0; i < 10; i++ ) {
			testMerge( i, i );
			testMerge( i, i + 1 );
			testMerge( i, i * 2 );
		}
	}
}
