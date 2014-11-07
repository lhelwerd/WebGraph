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
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.util.HyperLogLogCounterArray;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.WebGraphTestCase;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;


public class HyperBallTest extends WebGraphTestCase {
	// Below this threshold errors due to block-by-block summing start to appear.
	protected static final double THRESHOLD = 1E-9;

	/** Checks that the state of two HyperBall implementation (as
	 * returned by {@link HyperLogLogCounterArray#registers()}) are exactly the same. */
	public final static void assertState( final int size, final int log2m, final LongBigList[] a, final LongBigList[] b ) {
		final int m = 1 << log2m;
		for( int i = 0; i < size; i++ ) {
			for( int j = 0; j < m; j++ ) {
				final long index = ( (long)i << log2m ) + j;
				final int chunk = (int)( index >>> HyperLogLogCounterArray.CHUNK_SHIFT ); 
				final long offset = index & HyperLogLogCounterArray.CHUNK_MASK;
				assertEquals( "Counter " + i + ", register " + j + ": ", a[ chunk ].getLong( offset ), b[ chunk ].getLong( offset ) );
			}
		}
	}

	@Test
	public void testTrivial() throws IOException {
		ImmutableGraph g = ArrayListMutableGraph.newCompleteBinaryIntree( 10 ).immutableView();
		HyperBall hyperBall = new HyperBall( g, g, 7, null, 0, 0, 0, false, false, false, null, 0 );
		hyperBall.run( Long.MAX_VALUE, -1 );
		hyperBall.run( Long.MAX_VALUE, -1 );
		hyperBall.close();

		hyperBall = new HyperBall( g, g, 7, null, 0, 0, 0, true, false, false, null, 0 );
		hyperBall.run( Long.MAX_VALUE, -1 );
		hyperBall.run( Long.MAX_VALUE, -1 );
		hyperBall.close();
		
	}
	
	protected static void assertRelativeError( double sequentialCurrent, double current, double threshold ) {
		assertTrue( sequentialCurrent + " != " + current + ", " + Math.abs( current - sequentialCurrent ) / current + " > " + threshold, Math.abs( current - sequentialCurrent ) / current <= THRESHOLD );
	}
	
	/* All tests in this class check that 2 times the theoretical relative standard deviation
	 * is attained in 9 trials out of 10. The theory (in particular, the Vysochanskii-Petunin inequality) 
	 * indeed says it should happen 90% of the times. */
	
	@Test
	public void testClique() throws IOException {
		for( int log2m: new int[] { 4, 5, 6, 8 } ) {
			final double rsd = HyperBall.relativeStandardDeviation( log2m );
			for( int size: new int[] { 10, 100, 500 } ) {
				int correct = 0;
				for( int attempt = 0; attempt < 10; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = ArrayListMutableGraph.newCompleteGraph( size, false ).immutableView();
					HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10, 10, attempt % 2 == 0, false, false, null, attempt );
					SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
					hyperBall.init();
					sequentialHyperBall.init();
					hyperBall.iterate();
					final double current = hyperBall.neighbourhoodFunction.getDouble( 1 );
					final double sequentialCurrent = sequentialHyperBall.iterate();

					assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );

					if ( Math.abs( size * size - current ) <= 2 * rsd * size * size ) correct++;

					assertRelativeError( sequentialCurrent, current, THRESHOLD );
					
					hyperBall.close();
					sequentialHyperBall.close();
				}
				assertTrue( size + ":" + rsd + " " + correct + " < " + 9, correct >= 9 );
			}
		}
	}

	@Test
	public void testErdosRenyi() throws IOException {
		for( int log2m: new int[] { 4, 5, 6, 8 } ) {
			for( int size: new int[] { 10, 100, 500 } ) {
				for( int attempt = 0; attempt < 10; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .1, attempt, false ) ).immutableView();
					HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, false, false, null, attempt );
					SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
					hyperBall.init();
					sequentialHyperBall.init();
					do {
						hyperBall.iterate();
						final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
						final double sequentialCurrent = sequentialHyperBall.iterate();
						assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );
						assertRelativeError( sequentialCurrent, current, THRESHOLD );
					} while( hyperBall.modified() != 0 );

					hyperBall.init();
					sequentialHyperBall.init();
					do {
						hyperBall.iterate();
						final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
						final double sequentialCurrent = sequentialHyperBall.iterate();
						assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );
						assertRelativeError( sequentialCurrent, current, THRESHOLD );
					} while( hyperBall.modified() != 0 );

					hyperBall.close();
					sequentialHyperBall.close();
				}
			}
		}
	}

	@Test
	public void testCycle() throws IOException {
		for( int log2m: new int[] { 4, 5, 6 } ) {
			final double rsd = HyperBall.relativeStandardDeviation( log2m );
			for( int size: new int[] { 100, 500, 1000 } ) {
				final int[] correct = new int[ size + 1 ];
				for( int attempt = 0; attempt < 10; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = ArrayListMutableGraph.newDirectedCycle( size ).immutableView();
					HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, false, false, null, attempt );
					SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
					hyperBall.init();
					sequentialHyperBall.init();
					for( int i = 2; i <= size; i++ ) {
						hyperBall.iterate();
						final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
						final double sequentialCurrent = sequentialHyperBall.iterate();
						assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );
						assertRelativeError( sequentialCurrent, current, THRESHOLD );
						if ( Math.abs( size * i - current ) <= 2 * rsd * size * i ) correct[ i ]++;
					}
					hyperBall.close();
					sequentialHyperBall.close();
				}
				for( int i = 2; i <= size; i++ ) assertTrue( size + ":" + rsd + " " + correct[ i ] + " < " + 9, correct[ i ] >= 9 );
			}
		}

	}

	@Test
	public void testLine() throws IOException {
		for( int log2m: new int[] { 4, 5, 6 } ) {
			final double rsd = HyperBall.relativeStandardDeviation( log2m );
			for( int size: new int[] { 100, 500, 1000 } ) {
				final int[] correct = new int[ size + 1 ];
				for( int attempt = 0; attempt < 10; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ArrayListMutableGraph directedCycle = ArrayListMutableGraph.newDirectedCycle( size );
					directedCycle.removeArc( 0, 1 );
					ImmutableGraph g = directedCycle.immutableView();
					HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, false, false, null, attempt );
					SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
					hyperBall.init();
					sequentialHyperBall.init();
					for( int i = 2; i <= size; i++ ) {
						hyperBall.iterate();
						final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
						final double sequentialCurrent = sequentialHyperBall.iterate();
						assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );
						assertRelativeError( sequentialCurrent, current, THRESHOLD );
						long result = 0;
						for( int j = 0; j < i; j++ ) result += ( size - j );
						if ( Math.abs( result - current ) <= 2 * rsd * size * i ) correct[ i ]++;
					}
					hyperBall.close();
					sequentialHyperBall.close();
				}
				for( int i = 2; i <= size; i++ ) assertTrue( size + ":" + rsd + " " + correct[ i ] + " < " + 9, correct[ i ] >= 9 );
			}
		}

	}

	@Test
	public void testOutdirectedStar() throws IOException {
		for( int log2m: new int[] { 4, 5, 6 } ) {
			final double rsd = HyperBall.relativeStandardDeviation( log2m );
			for( int size: new int[] { 100, 500, 1000 } ) {
				int correct = 0;
				for( int attempt = 0; attempt < 10; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ArrayListMutableGraph mg = new ArrayListMutableGraph( size );
					for( int i = 1; i < size; i++ ) mg.addArc( 0, i );
					ImmutableGraph g = mg.immutableView();
					HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, false, false, null, attempt );
					SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
					hyperBall.init();
					sequentialHyperBall.init();
					hyperBall.iterate();
					final double current = hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 );
					final double sequentialCurrent = sequentialHyperBall.iterate();
					assertState( size, log2m, sequentialHyperBall.registers(), hyperBall.registers() );
					assertRelativeError( sequentialCurrent, current, THRESHOLD );
					if ( Math.abs( size * 2 - 1 - current ) <= 2 * rsd * ( size * 2 - 1 ) ) correct++;
					hyperBall.close();
					sequentialHyperBall.close();
				}
				assertTrue( size + ":" + rsd + " " + correct + " < " + 9, correct >= 9 );
			}
		}
	}

	@Test
	public void testTree() throws IOException {
		for( int log2m: new int[] { 4, 5, 6, 7, 8, 10, 12 } ) {
			double rsd = HyperBall.relativeStandardDeviation( log2m );
			ImmutableGraph g = ArrayListMutableGraph.newCompleteBinaryIntree( 3 ).immutableView();
			final int[] correct = new int[ 3 ];
			for( int attempt = 0; attempt < 10; attempt++ ) {
				System.err.println( "log2m: " + log2m + " attempt: " + attempt );
				HyperBall hyperBall = new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, false, false, null, attempt );
				SequentialHyperBall sequentialHyperBall = new SequentialHyperBall( g, log2m, null, attempt );
				hyperBall.init();
				sequentialHyperBall.init();

				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 29 ) <= 2 * rsd * 29 ) correct[ 0 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );
				
				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 41 ) <= 2 * rsd * 41 ) correct[ 1 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );
				
				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 49 ) <= 2 * rsd * 49 ) correct[ 2 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );

				// Test that you can reuse the object
				
				hyperBall.init();
				sequentialHyperBall.init();

				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 29 ) <= 2 * rsd * 29 ) correct[ 0 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );
				
				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 41 ) <= 2 * rsd * 41 ) correct[ 1 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );
				
				hyperBall.iterate();
				if ( Math.abs( hyperBall.neighbourhoodFunction.getDouble( hyperBall.neighbourhoodFunction.size() - 1 ) - 49 ) <= 2 * rsd * 49 ) correct[ 2 ]++;
				sequentialHyperBall.iterate();
				assertState( g.numNodes(), log2m, sequentialHyperBall.registers(), hyperBall.registers() );
				
				hyperBall.close();
				sequentialHyperBall.close();
			}
			//System.err.println( Arrays.toString( correct ));
			for( int i = 0; i < 3; i++ ) assertTrue( rsd + " " + correct[ i ] + " < " + 9, correct[ i ] >= 9 );
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void testInitClosed() throws IOException {
		ImmutableGraph g = ArrayListMutableGraph.newCompleteBinaryIntree( 3 ).immutableView();
		HyperBall hyperBall = new HyperBall( g, 8 );
		hyperBall.close();
		hyperBall.init();
	}

	@Test(expected=IllegalStateException.class)
	public void testInitIterate() throws IOException {
		ImmutableGraph g = ArrayListMutableGraph.newCompleteBinaryIntree( 3 ).immutableView();
		HyperBall hyperBall = new HyperBall( g, 8 );
		hyperBall.close();
		hyperBall.iterate();
	}

	private int[] distancesFrom( final ImmutableGraph graph, final int from ) {
		final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
		final int n = graph.numNodes();
		final int[] dist = new int[ n ];
		IntArrays.fill( dist, Integer.MAX_VALUE ); // Initially, all distances are infinity.

		queue.enqueue( from );
		dist[ from ] = 0;

		LazyIntIterator successors;

		while( ! queue.isEmpty() ) {
			int curr = queue.dequeueInt();
			successors = graph.successors( curr );
			int d = graph.outdegree( curr );
			while( d-- != 0 ) {
				int succ = successors.nextInt();
				if ( dist[ succ ] == Integer.MAX_VALUE ) {
					dist[ succ ] = dist[ curr ] + 1;
					queue.enqueue( succ );
				}
			}
		}
		
		return dist;		
	}
	
	@Test
	public void testErdosRenyiEccentricity() throws IOException {
		Random rand = new Random( 1 );
		for( int log2m: new int[] { 15 } ) {
			for( int size: new int[] { 10, 100, 500 } ) {
				for( int attempt = 0; attempt < 5; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .1, attempt + 1, false ) ).immutableView();
					HyperBall hyperBall = 
						new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, true, false, null, attempt );
					hyperBall.init();
					do {
						hyperBall.iterate();
					} while( hyperBall.modified() != 0 );

					int n = g.numNodes();
					for ( int i = 0; i < 10; i++ ) {
						int from = rand.nextInt( n );
						int dist[] = distancesFrom( g, from );
						long totDist = 0;
						int reachable = 0;
						for ( int k = 0; k < n; k++ ) 
							if ( dist[ k ] < Integer.MAX_VALUE ) {
								reachable++;
								totDist += dist[ k ];
							}
						assertEquals( 1.0, reachable / hyperBall.count( from ), 0.20 );
						
						double expEcc = (double)totDist / reachable;
						double computedEcc = hyperBall.sumOfDistances[ from ] / hyperBall.count( from );
						if ( expEcc == 0 ) assertEquals( 0.0, computedEcc, 1E-3 );
						else assertEquals( 1.0, expEcc / computedEcc, 0.15 );
					}
					
					hyperBall.close();
				}
			}
		}
	}

	@Test
	public void testErdosRenyiHarmonic() throws IOException {
		Random rand = new Random( 1 );
		for( int log2m: new int[] { 15 } ) {
			for( int size: new int[] { 10, 100, 500 } ) {
				for( int attempt = 0; attempt < 5; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .1, attempt, false ) ).immutableView();
					HyperBall hyperBall = 
						new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, true, true, null, attempt );
					hyperBall.init();
					do {
						hyperBall.iterate();
					} while( hyperBall.modified() != 0 );

					int n = g.numNodes();
					for ( int i = 0; i < 10; i++ ) {
						int from = rand.nextInt( n );
						int dist[] = distancesFrom( g, from );
						double totDist = 0;
						for ( int k = 0; k < n; k++ ) 
							if ( dist[ k ] < Integer.MAX_VALUE && dist[ k ] > 0 ) 
								totDist += 1.0 / dist[ k ];
						double expHarm = n / totDist;
						double computedHarm = n / hyperBall.sumOfInverseDistances[ from ];
						if ( totDist != 0 ) assertEquals( 1.0, expHarm / computedHarm, 0.3 );
					}
					
					hyperBall.close();
				}
			}
		}
	}

	
	@Test
	public void testErdosRenyiGain() throws IOException {
		for( int log2m: new int[] { 15 } ) {
			for( int size: new int[] { 10, 100, 500 } ) {
				for( int attempt = 0; attempt < 5; attempt++ ) {
					System.err.println( "log2m: " + log2m + " size: " + size + " attempt: " + attempt );
					ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .1, attempt, false ) ).immutableView();
					HyperBall hyperBall = 
						new HyperBall( g, attempt % 3 == 0 ? null : Transform.transpose( g ), log2m, null, 0, 10 * (attempt % 3), 10, attempt % 2 == 0, true, true, new Int2DoubleFunction[] {
							new HyperBall.AbstractDiscountFunction() {
								private static final long serialVersionUID = 1L;
								@Override
								public double get( int distance ) {
									return distance;
								}
							},
							new HyperBall.AbstractDiscountFunction() {
								private static final long serialVersionUID = 1L;
								@Override
								public double get( int distance ) {
									return 1. / distance;
								}
							}								
						}, 
							attempt );
					hyperBall.init();
					do {
						hyperBall.iterate();
					} while( hyperBall.modified() != 0 );

					int n = g.numNodes();
					for ( int i = 0; i < n; i++ ) {
						assertEquals( hyperBall.sumOfDistances[ i ], hyperBall.discountedCentrality[ 0 ][ i ], 1E-5 );
						assertEquals( hyperBall.sumOfInverseDistances[ i ], hyperBall.discountedCentrality[ 1 ][ i ], 1E-5 );
					}
					hyperBall.close();
				}
			}
		}
	}
	}
