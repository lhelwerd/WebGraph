package it.unimi.dsi.webgraph.algo;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import org.junit.Test;
import org.slf4j.helpers.NOPLogger;

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

public class ParallelBreadthFirstVisitTest {
	private final ProgressLogger pl = new ProgressLogger( NOPLogger.NOP_LOGGER );

	@Test
	public void testTree() {
		ImmutableGraph graph = ArrayListMutableGraph.newCompleteBinaryOuttree( 10 ).immutableView();
		ParallelBreadthFirstVisit visit = new ParallelBreadthFirstVisit( graph, 0, false, pl );
		visit.visit( 0 );
		final int d[] = new int[ graph.numNodes() ];
		for( int i = 0; i < visit.cutPoints.size() - 1; i++ )
			for( int j = visit.cutPoints.getInt( i ); j < visit.cutPoints.getInt( i + 1 ); j++ ) d[ visit.queue.getInt( j ) ] = i;
		for( int i = 0; i < graph.numNodes(); i++ ) assertEquals( Integer.toString( i ), Fast.mostSignificantBit( i + 1 ), d[ i ] );
	}

	@Test
	public void testStar() {
		ArrayListMutableGraph graph = new ArrayListMutableGraph( 1 + 10 + 100 + 1000 );
		for( int i = 1; i <= 10; i++ ) {
			graph.addArc( 0, i );
			graph.addArc( i, 0 );
			for( int j = 1; j <= 10; j++ ) {
				graph.addArc( i, i * 10 + j );
				graph.addArc( i * 10 + j, i );
				for( int k = 1; k <= 10; k++ ) {
					graph.addArc( i * 10 + j, ( i * 10 + j ) * 10 + k );
					graph.addArc( ( i * 10 + j ) * 10 + k, i * 10 + j );
				}
			}
		}

		ParallelBreadthFirstVisit visit = new ParallelBreadthFirstVisit( graph.immutableView(), 0, false, pl );
		int componentSize = visit.visit( 0 );
		for( int i = 1; i < graph.numNodes(); i++) {
			visit.clear();
			assertEquals( "Source: " + i, componentSize, visit.visit( i ) );
		}
	}
}
