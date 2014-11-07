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
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ScatteredArcsASCIIGraphTest extends WebGraphTestCase {

	@Test
	public void testConstructor() throws UnsupportedEncodingException, IOException {

		ScatteredArcsASCIIGraph g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n0 2\n1 0\n1 2\n2 0\n2 1".getBytes( "ASCII" ) ) );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "-1 15\n15 2\n2 -1\nOOPS!\n-1 2".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2},{1,2},{2,0}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { -1, 15, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "2 0\n2 1".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 0, 1 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "1 2".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "2 1".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 1 }, g.ids );
	
		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n2 1".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "\n0 1\n\n2 1".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "\n0 1\n# comment\n2\n2 1\n2 X".getBytes( "ASCII" ) ) );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n0 2\n1 0\n1 2\n2 0\n2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n0 2\n1  0\n1 \t 2\n2 0\n2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "2 0\n2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 0, 1 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "1 2".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 1 }, g.ids );
	
		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "\n0 1\n\n2 1".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "\n0 1\n# comment\n2\n2 1\n2 X".getBytes( "ASCII" ) ), true, false, 1 );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 0\n0 1\n0 2\n2 2\n1 0\n1 2\n2 0\n2 1".getBytes( "ASCII" ) ), true, true, 2 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

	}


	@Test
	public void testConstructorWithStrings() throws UnsupportedEncodingException, IOException {
		Object2LongFunction<String> map = new Object2LongArrayMap<String>();
		map.defaultReturnValue( -1 );
		
		map.clear();
		map.put( "0", 0 );
		map.put( "1", 1 );
		map.put( "2", 2 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n0 2\n1 0\n1 2\n2 0\n2 1".getBytes( "ASCII" ) ), map, null, 3 ) );

		map.clear();
		map.put( "-1", 1 );
		map.put( "15", 0 );
		map.put( "2", 2 );
		final ImmutableGraph g = new ArrayListMutableGraph( 3, new int[][] {{0,2},{1,0},{1,2},{2,1}} ).immutableView();
		assertEquals( g, new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "-1 15\n15 2\n2 -1\nOOPS!\n-1 2".getBytes( "ASCII" ) ), map, null, 3 ) );
		assertEquals( g, new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "-1 15\n15 2\n2 -1\nOOPS!\n-1 2\n32 2\n2 32".getBytes( "ASCII" ) ), map, null, 3 ) );

		map.clear();
		map.put( "topo", 0 );
		map.put( "cane", 1 );
		map.put( "topocane", 2 );
		assertEquals( g, new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "topocane cane\ncane topo\ncane topocane\ntopo topocane\n".getBytes( "ASCII" ) ), map, null, 3 ) );	
	}

	@Test(expected=IllegalArgumentException.class)
	public void testTargetOutOfRange() throws UnsupportedEncodingException, IOException {
		Object2LongFunction<String> map = new Object2LongArrayMap<String>();
		map.defaultReturnValue( -1 );
		map.put( "0", 0 );
		map.put( "1", 1 );
		map.put( "2", 2 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n0 2".getBytes( "ASCII" ) ), map, null, 2 ) );
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSourceOutOfRange() throws UnsupportedEncodingException, IOException {
		Object2LongFunction<String> map = new Object2LongArrayMap<String>();
		map.defaultReturnValue( -1 );
		map.put( "0", 0 );
		map.put( "1", 1 );
		map.put( "2", 2 );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ScatteredArcsASCIIGraph( new FastByteArrayInputStream( "0 1\n2 0".getBytes( "ASCII" ) ), map, null, 2 ) );
	}
	
	private static Iterator<long[]> toIterator( final String s ) {
		String[] arcs = s.split( "\n" );
		List<long[]> arcSet = new ArrayList<long[]>();
		for ( String arc: arcs ) {
			String[] parts = arc.split( " " );
			arcSet.add( new long[] { Long.parseLong( parts[ 0 ] ), Long.parseLong( parts[ 1 ] ) } );
		}
		return arcSet.iterator();
	}
	
	@Test
	public void testConstructorWithArray() throws IOException {
		ScatteredArcsASCIIGraph g = new ScatteredArcsASCIIGraph( toIterator( "0 1\n0 2\n1 0\n1 2\n2 0\n2 1" ), false, false, 100, null, null );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		System.out.println( Arrays.toString( g.ids ) );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "-1 15\n15 2\n2 -1\n-1 2"), false, false, 100, null, null  );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2},{1,2},{2,0}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { -1, 15, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "2 0\n2 1"), false, false, 100, null, null  );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 0, 1 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "1 2"), false, false, 100, null, null  );
		assertEquals( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "2 1"), false, false, 100, null, null  );
		assertEquals( new ArrayListMutableGraph( 2, new int[][] {{0,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 1 }, g.ids );
	
		g = new ScatteredArcsASCIIGraph( toIterator( "0 1\n2 1"), false, false, 100, null, null  );
		assertEquals( new ArrayListMutableGraph( 3, new int[][] {{0,1},{2,1}} ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "0 1\n0 2\n1 0\n1 2\n2 0\n2 1"), true, false, 1, null, null );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "2 0\n2 1"), true, false, 1, null, null );
		assertEquals( Transform.symmetrize( new ArrayListMutableGraph( 3, new int[][] {{0,1},{0,2}} ).immutableView() ), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 2, 0, 1 }, g.ids );

		g = new ScatteredArcsASCIIGraph( toIterator( "0 0\n0 1\n0 2\n2 2\n1 0\n1 2\n2 0\n2 1"), true, true, 2, null, null );
		assertEquals( ArrayListMutableGraph.newCompleteGraph( 3, false ).immutableView(), new ArrayListMutableGraph( g ).immutableView() );
		assertArrayEquals( new long[] { 0, 1, 2 }, g.ids );


	}

}
