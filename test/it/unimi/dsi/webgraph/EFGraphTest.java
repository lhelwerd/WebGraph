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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Properties;

import org.junit.Test;

public class EFGraphTest extends WebGraphTestCase {

	public static File storeTempGraph( final ImmutableGraph g ) throws IOException, IllegalArgumentException, SecurityException {
		File basename = File.createTempFile( EFGraphTest.class.getSimpleName(), "test" );
		EFGraph.store( g, basename.toString() );
		return basename;
	}

	public static File storeTempGraph( final ImmutableGraph g, final int log2Quantum, final int cacheSize, final ByteOrder byteOrder ) throws IOException, IllegalArgumentException, SecurityException {
		File basename = File.createTempFile( EFGraphTest.class.getSimpleName(), "test" );
		EFGraph.store( g, basename.toString(), log2Quantum, cacheSize, byteOrder, null );
		return basename;
	}

	@Test
	public void testCompression() throws IOException, IllegalArgumentException, SecurityException {
		for( int n = 1; n < 10; n++ ) { // Graph construction parameter
			for( int type = 1; type < 3; type++ ) {
				final ImmutableGraph g = type == 0 ? ArrayListMutableGraph.newCompleteGraph( n, false ).immutableView() :
					type == 1 ? ArrayListMutableGraph.newCompleteBinaryIntree( n ).immutableView() :
						ArrayListMutableGraph.newCompleteBinaryOuttree( n ).immutableView();

				for( ByteOrder byteOrder: new ByteOrder[] { ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN } ) {
					for( int cacheSize = 1; cacheSize < 128 * 1024; cacheSize *= 2 ) {
						for( int log2Quantum = 0; log2Quantum < 8; log2Quantum++ ) {
							System.err.println( "Testing type " + type + ", n=" + n + ", byteOrder=" + byteOrder + ", cacheSize=" + cacheSize + ", log2Quantum=" + log2Quantum + "..." );
							final File basename = EFGraphTest.storeTempGraph( g, log2Quantum, cacheSize, byteOrder );
							final Properties properties = new Properties();
							final FileInputStream propertyFile = new FileInputStream( basename + EFGraph.PROPERTIES_EXTENSION );
							properties.load( propertyFile );
							propertyFile.close();

							//System.err.println(properties);
							
							ImmutableGraph h;

							System.err.println( "Testing standard..." );
							h = EFGraph.load( basename.toString() );
							WebGraphTestCase.assertGraph( h );
							assertEquals( g, h );

							System.err.println( "Testing mapped..." );
							h = EFGraph.loadMapped( basename.toString() );
							WebGraphTestCase.assertGraph( h );
							assertEquals( g, h );

							basename.delete();
							deleteGraph( basename );
						}
					}
				}
			}
		}
	}

	@Test
	public void testErdosRenyi() throws IOException {
		for( int size: new int[] { 10, 100, 1000, 10000 } ) {
			for( boolean upperBound: new boolean[] { false, true } ) {
				final String basename = File.createTempFile( getClass().getSimpleName(), "test" ).toString();
				final ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( size, .001, 0, false ) ).immutableView();	
				EFGraph.store( g, upperBound ? size * size : size, basename, 3, 1024, ByteOrder.nativeOrder(), null );
				final EFGraph efGraph = (EFGraph)ImmutableGraph.load( basename );
				assertEquals( g, efGraph );

				for( int i = 0; i < size; i++ ) {
					for( int j = i + 1; j < size; j++ ) {
						LongOpenHashSet a = new LongOpenHashSet();
						LongOpenHashSet b = new LongOpenHashSet();
						LazyIntIterator sa = g.successors( i );
						LazyIntIterator sb = g.successors( j );
						for( long s; ( s = sa.nextInt() ) != -1; ) a.add( s );
						for( long t; ( t = sb.nextInt() ) != -1; ) b.add( t );

						a.retainAll( b );
						b.clear();
						final LazyIntSkippableIterator sx = efGraph.successors( i );
						final LazyIntSkippableIterator sy = efGraph.successors( j );

						int x = sx.nextInt();
						int y = sy.nextInt();

						while( x != -1 && x != LazyIntSkippableIterator.END_OF_LIST &&  y != -1 && y != LazyIntSkippableIterator.END_OF_LIST ) {
							if ( x == y ) {
								b.add ( x );
								x = sx.nextInt();
							}
							else if( x < y ) x = sx.skipTo( y );
							else y = sy.skipTo( x );
						}

						assertEquals( a, b );
					}
				}

				
				new File( basename ).delete();
				new File( basename + EFGraph.GRAPH_EXTENSION ).delete();
				new File( basename + EFGraph.OFFSETS_EXTENSION ).delete();
				new File( basename + EFGraph.PROPERTIES_EXTENSION ).delete();
			}
		}
	}

	@Test
	public void testSkipFirst() throws IOException {
		final String basename = File.createTempFile( getClass().getSimpleName(), "test" ).toString();
		final ImmutableGraph g = new ArrayListMutableGraph( new ErdosRenyiGraph( 1000, .01, 0, false ) ).immutableView();	
		EFGraph.store( g, 1000, basename, 3, 1024, ByteOrder.nativeOrder(), null );
		final EFGraph efGraph = (EFGraph)ImmutableGraph.load( basename );
		assertEquals( g, efGraph );

		for( int i = 0; i < 1000; i++ ) {
			for( int j = 0; j < 1000; j++ ) {
				LazyIntSkippableIterator sa = efGraph.successors( i );
				final int x = sa.skipTo( j );
				sa = efGraph.successors( i );
				for(;;) {
					final int y = sa.nextInt();
					if ( y >= j ) {
						assertEquals( y, x );
						break;
					}
					else if ( y == -1 ) {
						if ( x != LazyIntSkippableIterator.END_OF_LIST ) fail();
						break;
					}
				}
			}
		}
		new File( basename ).delete();
		new File( basename + EFGraph.GRAPH_EXTENSION ).delete();
		new File( basename + EFGraph.OFFSETS_EXTENSION ).delete();
		new File( basename + EFGraph.PROPERTIES_EXTENSION ).delete();
		
	}

}
