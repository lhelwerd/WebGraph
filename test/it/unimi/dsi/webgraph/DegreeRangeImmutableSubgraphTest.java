package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2003-2014 Sebastiano Vigna 
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
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import org.junit.Test;

public class DegreeRangeImmutableSubgraphTest extends WebGraphTestCase {
	
	@Test
	public void test() {
		for( int i = 10; i < 100000; i *= 10 ) {
			final double p = 5. / i;
			ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( i, p, 0, false ) ).immutableView();
			final int[] map = new int[ g.numNodes() ];
			final int min = 2;
			final int max = 4;
			for( int j = 0, k = 0; j < g.numNodes(); j++ )
				map[ j ] = g.outdegree( j ) >= min && g.outdegree( j ) < max ? k++ : -1;
			DegreeRangeImmutableSubgraph s = new DegreeRangeImmutableSubgraph( g, min, max );
			assertGraph( s );
			assertEquals( Transform.map( g, map ), s );
			s = new DegreeRangeImmutableSubgraph( g, 0, i );
			assertGraph( s );
			assertEquals( g, s );
		}
	}
}
