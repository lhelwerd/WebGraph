package it.unimi.dsi.webgraph.algo;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.WebGraphTestCase;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import org.junit.Test;

public class StronglyConnectedComponentsTest extends WebGraphTestCase {

	public static void sameComponents( final int l, final StronglyConnectedComponentsTarjan componentsRecursive, final StronglyConnectedComponents componentsIterative ) {
		final LongOpenHashSet[] recursiveComponentsSet = new LongOpenHashSet[ componentsRecursive.numberOfComponents ];
		final LongOpenHashSet[] iterativeComponentsSet = new LongOpenHashSet[ componentsIterative.numberOfComponents ];
		
		for( int i = recursiveComponentsSet.length; i-- != 0; ) {
			recursiveComponentsSet[ i ] = new LongOpenHashSet();
			iterativeComponentsSet[ i ] = new LongOpenHashSet();
		}

		for( int i = l; i-- != 0; ) {
			recursiveComponentsSet[ componentsRecursive.component[ i ] ].add( i );
			iterativeComponentsSet[ componentsIterative.component[ i ] ].add( i );
		}

		assertEquals( new ObjectOpenHashSet<LongOpenHashSet>( recursiveComponentsSet ), new ObjectOpenHashSet<LongOpenHashSet>( iterativeComponentsSet ) );
	}

	@Test
	public void testBuckets() {
		final ImmutableGraph g = new ArrayListMutableGraph( 9, 
				new int[][] { { 0, 0 }, { 1, 0 }, { 1, 2 }, 
				{ 2, 1 }, { 2, 3 }, { 2, 4 }, { 2, 5 },
				{ 3, 4 }, { 4, 3 },
				{ 5, 5 }, { 5, 6 }, { 5, 7 }, { 5, 8 },
				{ 6, 7 },
				{ 8, 7 } }
		).immutableView();

		StronglyConnectedComponents components = StronglyConnectedComponents.compute( g, true, null );

		LongArrayBitVector buckets = LongArrayBitVector.ofLength( g.numNodes() );
		buckets.set( 0, true );
		buckets.set( 3, true );
		buckets.set( 4, true );
		assertEquals( buckets, components.buckets );
		assertEquals( 3, buckets.count() );
		
		final int[] size = components.computeSizes();
		components.sortBySize( size );
		
		assertEquals( 2, size[ 0 ] );
		assertEquals( 2, size[ 1 ] );
		assertEquals( 1, size[ 2 ] );
		assertEquals( 1, size[ 3 ] );
		assertEquals( 1, size[ 4 ] );
		assertEquals( 1, size[ 5 ] );
		assertEquals( 1, size[ 6 ] );		

		StronglyConnectedComponents.compute( g, false, null ); // To increase coverage
	}


	@Test
	public void testBuckets2() {
		final ImmutableGraph g = new ArrayListMutableGraph( 4, 
				new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 1, 3 }, { 3, 3 } }
		).immutableView();

		StronglyConnectedComponents components = StronglyConnectedComponents.compute( g, true, null );

		LongArrayBitVector buckets = LongArrayBitVector.ofLength( g.numNodes() );
		buckets.set( 3 );
		assertEquals( buckets, components.buckets );
		assertEquals( 1, buckets.count() );
	}

	@Test
	public void testCompleteGraph() {
		StronglyConnectedComponents components = StronglyConnectedComponents.compute( ArrayListMutableGraph.newCompleteGraph( 5, false ).immutableView(), true, null );
		assertEquals( 5, components.buckets.count() );
		for( int i = 5; i-- != 0; ) assertEquals( 0, components.component[ i ] );
		assertEquals( 5, components.computeSizes()[ 0 ] );
	}

	@Test
	public void testNoBuckets() {
		StronglyConnectedComponents.compute( ArrayListMutableGraph.newCompleteGraph( 5, false ).immutableView(), false, null );
	}

	@Test
	public void testWithProgressLogger() {
		StronglyConnectedComponents.compute( ArrayListMutableGraph.newCompleteGraph( 5, false ).immutableView(), true, new ProgressLogger() );
	}

	@Test
	public void testTree() {
		StronglyConnectedComponents components = StronglyConnectedComponents.compute( ArrayListMutableGraph.newCompleteBinaryIntree( 3 ).immutableView(), true, null );
		assertEquals( 0, components.buckets.count() );
		assertEquals( 15, components.numberOfComponents );
	}

	@Test
	public void testErdosRrenyi() {
		for( int size: new int[] { 10, 100, 1000 } ) {
			for( int attempt = 0; attempt < 5; attempt++ ) {
				final ImmutableGraph view = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .05, attempt + 1, false ) ).immutableView();
				final StronglyConnectedComponentsTarjan componentsRecursive = StronglyConnectedComponentsTarjan.compute( view, true, null );
				final StronglyConnectedComponents componentsIterative = StronglyConnectedComponents.compute( view, true, null );
				assertEquals( componentsRecursive.numberOfComponents, componentsIterative.numberOfComponents );
				sameComponents( size, componentsRecursive, componentsIterative );
			}
		}
	}
}