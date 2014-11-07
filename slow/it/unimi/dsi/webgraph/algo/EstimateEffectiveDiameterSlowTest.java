package it.unimi.dsi.webgraph.algo;

/*		 
 * Copyright (C) 2010-2014 Paolo Boldi & Sebastiano Vigna 
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
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.WebGraphTestCase;

import java.io.IOException;

import org.junit.Test;


public class EstimateEffectiveDiameterSlowTest extends WebGraphTestCase {
	
	@Test
	public void testLarge() throws IOException {
		String path = getGraphPath( "cnr-2000" );
		ImmutableGraph g = ImmutableGraph.load( path ); 
		final HyperBall hyperBall = new HyperBall( g, 8, 0 );
		hyperBall.run( Integer.MAX_VALUE, -1 );
		assertEquals( NeighbourhoodFunction.effectiveDiameter( .9, HyperBallSlowTest.cnr2000NF ), NeighbourhoodFunction.effectiveDiameter( .9, hyperBall.neighbourhoodFunction.toDoubleArray() ), 1 );
		hyperBall.close();
		deleteGraph( path );
	}
}
