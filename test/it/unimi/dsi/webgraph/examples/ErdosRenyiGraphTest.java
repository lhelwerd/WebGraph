package it.unimi.dsi.webgraph.examples;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import org.junit.Test;



public class ErdosRenyiGraphTest {

	@Test
	public void test() {
		ImmutableGraph graph = new ErdosRenyiGraph( 10000, 1000000, 0, false );
		long arcs = 0;
		for( NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator.hasNext(); ) {
			final int curr = nodeIterator.nextInt();
			final int outdegree = nodeIterator.outdegree(); 
			arcs += outdegree;
			final int[] s = nodeIterator.successorArray();
			if ( outdegree != 0 ) assertTrue( "Node " + curr, s[ 0 ] != curr );
			for( int i = 1; i < outdegree; i++ ) {
				assertTrue( s[ i ] > s[ i - 1 ] );
				assertTrue( s[ i ] !=  curr );
			}
		}
		
		assertEquals( ( 1000000.0 - arcs ) / 1000000.0, 0, 1E-2 );
	}
	
	@Test
	public void testBinomialWithoutLoops() {
		ImmutableGraph g = new ErdosRenyiGraph( 5, .5, 0, false );
		new ArrayListMutableGraph( g ).immutableView();
	}

	@Test
	public void testCopy() {
		ImmutableGraph graph = new ErdosRenyiGraph( 10000, 1000000, 0, false );
		assertEquals( graph, graph.copy() );
		assertEquals( graph.copy(), graph );
	}
}
