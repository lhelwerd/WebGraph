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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.util.XorShift64StarRandom;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;
import it.unimi.dsi.webgraph.examples.IntegerTriplesArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledGraphTest;
import it.unimi.dsi.webgraph.labelling.GammaCodedIntLabel;
import it.unimi.dsi.webgraph.labelling.Label;
import it.unimi.dsi.webgraph.labelling.LabelSemiring;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

public class TransformTest extends WebGraphTestCase {

	@Test
	public void testMapExpand() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = ArrayListMutableGraph.newCompleteGraph( 4, false ).immutableView();
		g2 = Transform.map( g, new int[] { 0, 2, 4, 6 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 7, new Transform.ArcFilter() {
			public boolean accept( int i, int j ) {
				return i % 2 == 0 && j % 2 == 0 && i != j;
			}
			
		}).immutableView(), g2 );

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { 0, 3, 3 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 4, new int[][] { { 0, 3 }, { 3, 0 }, { 3, 3 } } ).immutableView(), g2 );

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { 4, 4, 4 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 5, new int[][] { { 4, 4 } } ).immutableView(), g2 );

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { 6, 5, 4 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 7, new int[][] { { 6, 5 }, { 5, 4 }, { 4, 6 } } ).immutableView(), g2 );

	}

	@Test
	public void testMapPermutation() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { 2, 1, 0 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] { { 0, 2 }, { 2, 1 }, { 1, 0 } } ).immutableView(), g2 );
	}

	@Test
	public void testInjective() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = new ArrayListMutableGraph( 3, new int[][] { { 0, 1 }, { 1, 2 }, { 0, 2 } } ).immutableView();
		g2 = Transform.map( g, new int[] { 2, -1, 0 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] { { 2, 0 } } ).immutableView(), g2 );
	}

	@Test
	public void testMapCollapse() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { 0, 0, 0 } );
		assertGraph( g2 );
		assertEquals( 1, g2.numNodes() );
	}

	@Test
	public void testMapClear() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { -1, -1, -1 } );
		assertGraph( g2 );
		assertEquals( 0, g2.numNodes() );
	}

	@Test
	public void testMapKeepMiddle() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { -1, 0, -1 } );
		assertGraph( g2 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 1, false ).immutableView(), g2 );

		g = ArrayListMutableGraph.newDirectedCycle( 3 ).immutableView();
		g2 = Transform.map( g, new int[] { -1, 2, -1 } );
		assertGraph( g2 );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {} ).immutableView(), g2 );
	}
	
	/** Test introduced after finding a bug in remapping: accessing two successors in parallel gave wrong results. */
	@Test
	public void testMapTwoSuccessors() {
		ImmutableGraph g;
		ImmutableGraph g2;

		g = new ArrayListMutableGraph( 5, new int[][] {{0,1},{0,2},{0,4},{1,2},{1,3},{1,4},{4,2}} ).immutableView();
		// Now we test in parallel (on g) the successor lists of nodes 0 (1,2,4) and 1 (2,3,4)
		int[] expected0 = { 1, 2, 4 };
		int[] expected1 = { 2, 3, 4 };
		LazyIntIterator it0 = g.successors( 0 );
		LazyIntIterator it1 = g.successors( 1 );
		for ( int i = 0; i < 3; i++ ) {
			assertEquals( expected0[ i ], it0.nextInt() );
			assertEquals( expected1[ i ], it1.nextInt() );
		}
		assertEquals( -1, it0.nextInt() );
		assertEquals( -1, it1.nextInt() );
		g2 = Transform.map( g, new int[] { 0, 1, 2, 3, 4 } ); // The permutation is immaterial: we use the identity
		assertGraph( g2 );	
		// Now we test in parallel (on g2) the successor lists of nodes 0 (1,2,4) and 1 (2,3,4)
		it0 = g2.successors( 0 );
		it1 = g2.successors( 1 );
		for ( int i = 0; i < 3; i++ ) {
			assertEquals( expected0[ i ], it0.nextInt() );
			assertEquals( expected1[ i ], it1.nextInt() );
		}
		assertEquals( -1, it0.nextInt() );
		assertEquals( -1, it1.nextInt() );
	}
	

	@Test
	public void testLex() {
		ImmutableGraph g = new ArrayListMutableGraph( 3, new int[][] {{ 0, 2 }, { 1, 1 }, { 1, 2 }, { 2, 0 }, { 2, 1 }, { 2, 2 } } ).immutableView();
		int p[] = Transform.lexicographicalPermutation( g );
		assertArrayEquals( new int[] { 0, 1, 2 }, p );
		
		g = new ArrayListMutableGraph( 3, new int[][] {{ 0, 0 }, { 0, 1 }, { 0, 2 }, { 1, 1 }, { 1, 2 }, { 2, 2 } } ).immutableView();
		p = Transform.lexicographicalPermutation( g );
		assertArrayEquals( new int[] { 2, 1, 0 }, p );
	}


	@Test
	public void testFilters() throws IllegalArgumentException, SecurityException {
		ImmutableGraph graph = new ArrayListMutableGraph( 6, 
				new int[][] {
						{ 0, 1 },
						{ 0, 2 },
						{ 1, 1 },
						{ 1, 3 },
						{ 2, 1 },
						{ 4, 5 },
				}
		).immutableView();
		
		ImmutableGraph filtered = Transform.filterArcs( graph, new Transform.ArcFilter() {
			public boolean accept( int i, int j ) {
				return i < j;
			}
		}, null );
		
		assertGraph( filtered );

		NodeIterator nodeIterator = filtered.nodeIterator();
		LazyIntIterator iterator;
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 0, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 1, iterator.nextInt() );
		assertEquals( 2, iterator.nextInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 1, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 3, iterator.nextInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 2, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 3, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertEquals( 4, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 5, iterator.nextInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 5, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertFalse( nodeIterator.hasNext() );
	}


	@Test
	public void testLabelledFilters() throws IllegalArgumentException, SecurityException, IOException {
		IntegerTriplesArcLabelledImmutableGraph graph = new IntegerTriplesArcLabelledImmutableGraph( 
				new int[][] {
						{ 0, 1, 2 },
						{ 0, 2, 3 },
						{ 1, 1, 4 },
						{ 1, 3, 5 },
						{ 2, 1, 6 },
						{ 4, 5, 7 },
				}
		);
		
		ArcLabelledImmutableGraph filtered = Transform.filterArcs( graph, new Transform.LabelledArcFilter() {
			public boolean accept( int i, int j, Label label ) {
				return i < j;
			}
		}, null );

		ArcLabelledNodeIterator nodeIterator = filtered.nodeIterator();
		LabelledArcIterator iterator;
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 0, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 1, iterator.nextInt() );
		assertEquals( 2, iterator.label().getInt() );
		assertEquals( 2, iterator.nextInt() );
		assertEquals( 3, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 1, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 3, iterator.nextInt() );
		assertEquals( 5, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 2, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 3, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertEquals( 4, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( 5, iterator.nextInt() );
		assertEquals( 7, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		assertTrue( nodeIterator.hasNext() );
		assertEquals( 5, nodeIterator.nextInt() );
		iterator = nodeIterator.successors();
		assertEquals( -1, iterator.nextInt() );
		assertFalse( nodeIterator.hasNext() );

		File file = BitStreamArcLabelledGraphTest.storeTempGraph( graph );
		ArcLabelledImmutableGraph graph2 = ArcLabelledImmutableGraph.load( file.toString() );
		
		filtered = Transform.filterArcs( graph2, new Transform.LabelledArcFilter() {
			public boolean accept( int i, int j, Label label ) {
				return i < j;
			}
		}, null );

		iterator = filtered.successors( 0 );
		assertEquals( 1, iterator.nextInt() );
		assertEquals( 2, iterator.label().getInt() );
		assertEquals( 2, iterator.nextInt() );
		assertEquals( 3, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		iterator = filtered.successors( 1 );
		assertEquals( 3, iterator.nextInt() );
		assertEquals( 5, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		iterator = filtered.successors( 2 );
		assertEquals( -1, iterator.nextInt() );
		iterator = filtered.successors( 3 );
		assertEquals( -1, iterator.nextInt() );
		iterator = filtered.successors( 4 );
		assertEquals( 5, iterator.nextInt() );
		assertEquals( 7, iterator.label().getInt() );
		assertEquals( -1, iterator.nextInt() );
		iterator = filtered.successors( 5 );
		assertEquals( -1, iterator.nextInt() );

	}

	@Test
	public void testCompose() {
		ImmutableGraph g0 = new ArrayListMutableGraph( 3, new int[][]  { { 0, 1 }, { 0, 2 } } ).immutableView();
		ImmutableGraph g1 = new ArrayListMutableGraph( 3, new int[][]  { { 1, 0 }, { 2, 1 } } ).immutableView();
		
		ImmutableGraph c = Transform.compose( g0, g1 );
		
		NodeIterator n = c.nodeIterator();
		assertTrue( n.hasNext() );
		assertEquals( 0, n.nextInt() );
		LazyIntIterator i = n.successors();
		assertEquals( 0, i.nextInt() );
		assertEquals( 1, i.nextInt() );
		assertEquals( -1, i.nextInt() );
		assertEquals( 1, n.nextInt() );
		i = n.successors();
		assertEquals( -1, i.nextInt() );
		assertTrue( n.hasNext() );
		assertEquals( 2, n.nextInt() );
		i = n.successors();
		assertEquals( -1, i.nextInt() );
		assertFalse( n.hasNext() );

		assertEquals( c, c.copy() );
		assertEquals( c.copy(), c );
	}

	@Test
	public void testLabelledCompose() throws IllegalArgumentException, SecurityException, IOException {
		File file = BitStreamArcLabelledGraphTest.storeTempGraph( new IntegerTriplesArcLabelledImmutableGraph( 
				new int[][] {
						{ 0, 1, 2 },
						{ 0, 2, 10 },
						{ 0, 3, 1 },
						{ 1, 2, 4 },
						{ 3, 2, 1 },
				}
		) );
		ArcLabelledImmutableGraph graph = ArcLabelledImmutableGraph.load( file.toString() );
	
		ArcLabelledImmutableGraph composed = Transform.compose( graph, graph, new LabelSemiring() {
			private final GammaCodedIntLabel one = new GammaCodedIntLabel( "FOO" );
			private final GammaCodedIntLabel zero = new GammaCodedIntLabel( "FOO" );
			{
				one.value = 0;
				zero.value = Integer.MAX_VALUE;
			}
			
			public Label add( Label first, Label second ) {
				GammaCodedIntLabel result = new GammaCodedIntLabel( "FOO" );
				result.value = Math.min( first.getInt(), second.getInt() );
				return result;
			}

			public Label multiply( Label first, Label second ) {
				GammaCodedIntLabel result = new GammaCodedIntLabel( "FOO" );
				result.value = first.getInt() + second.getInt();
				return result;
			}

			public Label one() {
				return one;
			}

			public Label zero() {
				return zero;
			}
		} );

		ArcLabelledNodeIterator n = composed.nodeIterator();
		assertTrue( n.hasNext() );
		assertEquals( 0, n.nextInt() );
		LabelledArcIterator i = n.successors();
		assertEquals( 2, i.nextInt() );
		assertEquals( 2, i.label().getInt() );
		assertEquals( -1, i.nextInt() );
		assertEquals( 1, n.nextInt() );
		i = n.successors();
		assertEquals( -1, i.nextInt() );
		assertTrue( n.hasNext() );
		assertEquals( 2, n.nextInt() );
		i = n.successors();
		assertEquals( -1, i.nextInt() );
		assertTrue( n.hasNext() );
		assertEquals( 3, n.nextInt() );
		i = n.successors();
		assertEquals( -1, i.nextInt() );
		assertFalse( n.hasNext() );
	}
	
	@Test
	public void testTranspose() throws IOException {
		ImmutableGraph g = new ErdosRenyiGraph( 5, .5, 0, false );
		ImmutableGraph gt = Transform.transpose( g );
		assertEquals( gt, Transform.transposeOffline( g, 5 ) );
		assertEquals( g, Transform.transposeOffline( Transform.transposeOffline( g, 100 ), 5 ) );

		g = new ErdosRenyiGraph( 100, .50, 0, false );
		gt = Transform.transpose( g );
		assertEquals( gt, Transform.transposeOffline( g, 100 ) );
		assertEquals( g, Transform.transposeOffline( Transform.transposeOffline( g, 100 ), 100 ) );
		
		g = new ErdosRenyiGraph( 1000, .20, 0, false );
		gt = Transform.transpose( g );
		assertEquals( gt, Transform.transposeOffline( g, 10000 ) );
		assertEquals( g, Transform.transposeOffline( Transform.transposeOffline( g, 10000 ), 10000 ) );
	}

	@Test
	public void testMapOffline() throws IOException {
		ImmutableGraph g = new ErdosRenyiGraph( 10, .50, 0, false );
		int[] perm = Util.identity( g.numNodes() );
		Collections.shuffle( IntArrayList.wrap( perm ), new XorShift64StarRandom( 0 ) );
		int[] inv = Util.invertPermutation( perm );
		ImmutableGraph gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 100 ) );
		assertEquals( g, Transform.mapOffline( Transform.mapOffline( g, perm, 100 ), inv, 100 ) );
		assertEquals( gm, gm.copy() );

		perm = Util.identity( g.numNodes() );
		perm[ perm.length -1 ] = -1;
		gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 100 ) );
		assertEquals( gm, gm.copy() );

		perm = Util.identity( g.numNodes() );
		Collections.shuffle( IntArrayList.wrap( perm ), new XorShift64StarRandom( 0 ) );
		perm[ 0 ] = -1; perm[ perm.length / 2 ] = -1;
		gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 100 ) );
		assertEquals( gm, gm.copy() );

		perm = Util.identity( g.numNodes() );
		perm[ 1 ] = 0; perm[ perm.length - 2 ] = perm.length - 1;
		gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 100 ) );
		assertEquals( gm, gm.copy() );

		g = new ErdosRenyiGraph( 1000, .20, 0, false );
		perm = Util.identity( g.numNodes() );
		Collections.shuffle( IntArrayList.wrap( perm ), new XorShift64StarRandom( 0 ) );
		inv = Util.invertPermutation( perm );
		gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 10000 ) );
		assertEquals( g, Transform.mapOffline( Transform.mapOffline( g, perm, 10000 ), inv, 10000 ) );
		assertEquals( gm, gm.copy() );

		perm = Util.identity( g.numNodes() );
		Collections.shuffle( IntArrayList.wrap( perm ), new XorShift64StarRandom( 0 ) );
		perm[ 0 ] = -1; perm[ perm.length / 2 ] = -1; perm[ perm.length / 4 ] = -1; perm[ 3 * perm.length / 4 ] = -1;
		gm = Transform.map( new ArrayListMutableGraph( g ).immutableView(), perm );
		assertEquals( gm, Transform.mapOffline( g, perm, 10000 ) );
		assertEquals( gm, gm.copy() );
	}
	
	@Test
	public void testSymmetrize() throws IOException {
		ImmutableGraph g = new ErdosRenyiGraph( 5, .50, 0, false );
		ImmutableGraph gs = Transform.symmetrize( g );
		assertEquals( gs, Transform.symmetrizeOffline( g, 5 ) );
		assertEquals( gs, Transform.symmetrizeOffline( Transform.symmetrizeOffline( g, 100 ), 5 ) );
		assertEquals( gs, gs.copy() );

		g = new ErdosRenyiGraph( 100, .50, 0, false );
		gs = Transform.symmetrize( g );
		assertEquals( gs, Transform.symmetrizeOffline( g, 100 ) );
		assertEquals( gs, Transform.symmetrizeOffline( Transform.symmetrizeOffline( g, 100 ), 100 ) );
		assertEquals( gs, gs.copy() );
	
		g = new ErdosRenyiGraph( 1000, .20, 0, false );
		gs = Transform.symmetrize( g );
		assertEquals( gs, Transform.symmetrizeOffline( g, 10000 ) );
		assertEquals( gs, Transform.symmetrizeOffline( Transform.symmetrizeOffline( g, 10000 ), 10000 ) );
		assertEquals( gs, gs.copy() );
	}

}
