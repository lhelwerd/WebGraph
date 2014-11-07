package it.unimi.dsi.webgraph.algo;

/*
 *  Copyright (C) 2011 Paolo Boldi, Massimo Santini and Sebastiano Vigna
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
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.HyperBall;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.IOException;

import org.junit.Test;



//RELEASE-STATUS: DIST

public class GeometricCentralitiesTest {

	@Test
	public void testPath() throws InterruptedException {
		final ImmutableGraph graph = Transform.transpose( new ArrayListMutableGraph( 3, new int[][] { { 0, 1 }, { 1, 2 } } ).immutableView() );
		
		final GeometricCentralities centralities = new GeometricCentralities( graph );
		centralities.compute();
		
		assertEquals( 0, centralities.closeness[ 0 ], 0 );
		assertEquals( 1, centralities.closeness[ 1 ], 0 );
		assertEquals( 1./3, centralities.closeness[ 2 ], 0 );

		assertEquals( 1, centralities.lin[ 0 ], 0 );
		assertEquals( 4, centralities.lin[ 1 ], 0 );
		assertEquals( 3, centralities.lin[ 2 ], 0 );

		assertEquals( 0, centralities.harmonic[ 0 ], 0 );
		assertEquals( 1, centralities.harmonic[ 1 ], 0 );
		assertEquals( 3./2, centralities.harmonic[ 2 ], 0 );
	}

	@Test
	public void testCycle() throws InterruptedException {
		for( int size: new int[] { 10, 50, 100 } ) {	
			final ImmutableGraph graph = ArrayListMutableGraph.newDirectedCycle( size ).immutableView();
			final GeometricCentralities centralities = new GeometricCentralities( graph );
			centralities.compute();

			final double[] expected = new double[ size ];
			DoubleArrays.fill( expected, 2. / ( size * ( size - 1. ) ) );
			for( int i = size; i-- != 0; ) assertEquals( expected[ i ], centralities.closeness[ i ], 1E-15 );
			DoubleArrays.fill( expected, size * 2. / ( size - 1. ) );
			for( int i = size; i-- != 0; ) assertEquals( expected[ i ], centralities.lin[ i ], 1E-15 );
			double s = 0;
			for( int i = size; i-- != 1; ) s += 1. / i;
			DoubleArrays.fill( expected, s );
			for( int i = size; i-- != 0; ) assertEquals( expected[ i ], centralities.harmonic[ i ], 1E-14 );
		}
	}
	
	@Test
	public void testErdosRenyi() throws IOException, InterruptedException {
		for( int size: new int[] { 10, 100 } ) {
			for( double density: new double[] { 0.0001, 0.001, 0.01 } ) {
				final ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, density, 0, false ) ).immutableView();
				final HyperBall hanf = new HyperBall( g, Transform.transpose( g ), 20, null, 0, 0, 0, false, true, true, null, 0 );
				hanf.init();
				do hanf.iterate(); while( hanf.modified() != 0 );
				final GeometricCentralities centralities = new GeometricCentralities( g );
				centralities.compute();
				
				for( int i = 0; i < size; i++ )
					assertEquals( hanf.sumOfInverseDistances[ i ], centralities.harmonic[ i ], 1E-3 ); 
				for( int i = 0; i < size; i++ )
					assertEquals( hanf.sumOfDistances[ i ] == 0 ? 0 : 1 / hanf.sumOfDistances[ i ], centralities.closeness[ i ], 1E-5 ); 
				for( int i = 0; i < size; i++ )
					assertEquals( hanf.sumOfDistances[ i ] == 0 ? 1 : hanf.count( i ) * hanf.count( i ) / hanf.sumOfDistances[ i ], centralities.lin[ i ], 1E-3 ); 
				hanf.close();
			}
		}
	}
}
