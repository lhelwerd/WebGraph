package it.unimi.dsi.webgraph.algo;

/*		 
 * Copyright (C) 2011-2014 Sebastiano Vigna 
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


import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.WebGraphTestCase;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import org.junit.Test;


public class ConnectedComponentsTest extends WebGraphTestCase {
	public static void sameComponents( ImmutableGraph g ) {
		StronglyConnectedComponentsTarjan stronglyConnectedComponents = StronglyConnectedComponentsTarjan.compute( g, false, new ProgressLogger() );
		int[] size2 = stronglyConnectedComponents.computeSizes();
		stronglyConnectedComponents.sortBySize( size2 );

		for( int t = 0; t < 3; t++ ) {
			ConnectedComponents connectedComponents = ConnectedComponents.compute( g, t, new ProgressLogger() );
			int[] size = connectedComponents.computeSizes();
			connectedComponents.sortBySize( size );
			for( int i = g.numNodes(); i-- != 0; )
				for( int j = i; j-- != 0; )
					assert( ( connectedComponents.component[ i ] == connectedComponents.component[ j ] ) == ( stronglyConnectedComponents.component[ i ] == stronglyConnectedComponents.component[ j ] ) );
		}
	}
	
	@Test
	public void testSmall() {
		sameComponents( ArrayListMutableGraph.newBidirectionalCycle( 40 ).immutableView() );
	}
	
	@Test
	public void testBinaryTree() {
		sameComponents( Transform.symmetrize( ArrayListMutableGraph.newCompleteBinaryIntree( 10 ).immutableView() ) );
	}

	@Test
	public void testErdosRenyi() {
		for( int size: new int[] { 10, 100, 1000 } ) 
			for( int attempt = 0; attempt < 5; attempt++ ) 
				sameComponents( Transform.symmetrize( new ArrayListMutableGraph( new ErdosRenyiGraph( size, .001, attempt + 1, true ) ).immutableView() ) );
	}
}
