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
import it.unimi.dsi.fastutil.ints.IntIterator;

import org.junit.Test;

public class ArrayListMutableGraphTest extends WebGraphTestCase {

	@Test
	public void testConstructor() throws IllegalArgumentException, SecurityException {
		for( int n = 1; n < 8; n++ ) {
			for( int type = 0; type < 3; type++ ) {
				System.err.println( "Testing type " + type + ", n=" + n + "..." );
				ArrayListMutableGraph g = type == 0 ? ArrayListMutableGraph.newCompleteGraph( n, false ) :
					type == 1 ? ArrayListMutableGraph.newCompleteBinaryIntree( n ) :
						ArrayListMutableGraph.newCompleteBinaryOuttree( n ); 
				final ImmutableGraph immutableView = g.immutableView();
				assertGraph( immutableView );
				assertEquals( g, new ArrayListMutableGraph( immutableView ) );
				int[][] arc = new int[ (int)g.numArcs() ][ 2 ];
				for( int i = 0, k = 0; i < g.numNodes(); i++ )
					for( IntIterator successors = g.successors( i ); successors.hasNext(); ) 
						arc[ k++ ] = new int[] { i, successors.nextInt() };
				
				assertEquals( g, new ArrayListMutableGraph( g.numNodes(), arc ) );
			}
		}
	}
	
	@Test
	public void testHashCode() {
		ArrayListMutableGraph g = ArrayListMutableGraph.newCompleteGraph( 10, false );
		assertEquals( g.immutableView().hashCode(), g.hashCode() );
		
	}
}
