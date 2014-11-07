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
import static org.junit.Assert.assertFalse;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;
import it.unimi.dsi.webgraph.labelling.Label;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** A base test class providing additional assertions
 * for {@linkplain it.unimi.dsi.webgraph.ImmutableGraph immutable graphs}.
 */

public abstract class WebGraphTestCase {
	
	private static void copy( InputStream in, OutputStream out ) throws IOException {
		int c;
		while( ( c = in.read() ) != -1 ) out.write( c );
		out.close();
	}
	
	/** Returns a path to a temporary graph that copies a resource graph with given basename.
	 * 
	 * @param basename the basename.
	 * @return the graph.
	 * @throws IOException
	 */
	public String getGraphPath( final String basename ) throws IOException {
		File file = File.createTempFile( getClass().getSimpleName(), "graph" );
		file.delete();
		
		copy( BVGraphTest.class.getResourceAsStream( basename + BVGraph.GRAPH_EXTENSION ), new FileOutputStream( file.getCanonicalPath() + BVGraph.GRAPH_EXTENSION ) );
		copy( BVGraphTest.class.getResourceAsStream( basename + BVGraph.OFFSETS_EXTENSION ), new FileOutputStream( file.getCanonicalPath() + BVGraph.OFFSETS_EXTENSION ) );
		copy( BVGraphTest.class.getResourceAsStream( basename + BVGraph.PROPERTIES_EXTENSION ), new FileOutputStream( file.getCanonicalPath() + BVGraph.PROPERTIES_EXTENSION ) );

		return file.getCanonicalPath();
	}

	/** Cleans up a temporary graph.
	 * 
	 * @param basename the basename.
	 */
	
	public static void deleteGraph( final String basename ) {
		deleteGraph( new File( basename ) );
	}


	/** Cleans up a temporary graph.
	 * 
	 * @param basename the basename.
	 */
	public static void deleteGraph( final File basename ) {
		new File( basename + BVGraph.GRAPH_EXTENSION ).delete();
		new File( basename + BVGraph.OFFSETS_EXTENSION ).delete();
		new File( basename + BVGraph.OFFSETS_BIG_LIST_EXTENSION ).delete();
		new File( basename + ImmutableGraph.PROPERTIES_EXTENSION ).delete();
	}
	
	/** Performs a stress-test of an immutable graph. All available methods
	 * for accessing outdegrees and successors are cross-checked.
	 * 
	 * @param g the immutable graph to be tested.
	 */
	
	public static void assertGraph( ImmutableGraph g ) {
		NodeIterator nodeIterator0 = g.nodeIterator(), nodeIterator1 = g.nodeIterator();
		int d, s0[];
		Label l0[];
		LazyIntIterator s1;
		int m = 0;
		int curr;
		// Check that iterator and array methods return the same values in sequential scans.
		for( int i = g.numNodes(); i-- != 0; ) {
			curr = nodeIterator0.nextInt();
			assertEquals( curr, nodeIterator1.nextInt() );
			d = nodeIterator0.outdegree();
			m += d;
			assertEquals( d, nodeIterator1.outdegree() );

			s0 = nodeIterator0.successorArray();
			s1 = nodeIterator1.successors();
			for( int k = 0; k < d; k++ ) assertEquals( s0[ k ], s1.nextInt() );
			assertEquals( -1, s1.nextInt() );

			if ( g instanceof ArcLabelledImmutableGraph ) {
				l0 = ((ArcLabelledNodeIterator)nodeIterator0).labelArray();
				s1 = ((ArcLabelledNodeIterator)nodeIterator1).successors();
				for( int k = 0; k < d; k++ ) {
					s1.nextInt();
					assertEquals( l0[ k ], ((LabelledArcIterator)s1).label() );
				}
			}

			assertEquals( -1, s1.nextInt() );
		}

		try {
			assertEquals( m, g.numArcs() );
		}
		catch( UnsupportedOperationException ignore ) {} // A graph might not support numArcs().
		assertFalse( nodeIterator0.hasNext() );
		assertFalse( nodeIterator1.hasNext() );
		
		if ( ! g.randomAccess() ) return;
		
		// Check that sequential iterator methods and random methods do coincide.  
		String msg;

		for( int s = 0; s < g.numNodes() - 1; s++ ) {
			nodeIterator1 = g.nodeIterator( s );
			for( int i = g.numNodes() - s; i-- != 0; ) {
				curr = nodeIterator1.nextInt();
				msg = "Node " + curr + ", starting from " + s + ":";
				d = g.outdegree( curr );
				assertEquals( msg, d, nodeIterator1.outdegree() );
				s0 = g.successorArray( curr );
				s1 = nodeIterator1.successors();
				for( int k = 0; k < d; k++ ) assertEquals( msg, s0[ k ], s1.nextInt() );
				s1 = g.successors( curr );
				for( int k = 0; k < d; k++ ) assertEquals( msg, s0[ k ], s1.nextInt() );
				assertEquals( msg, -1, s1.nextInt() );

				if ( g instanceof ArcLabelledImmutableGraph ) {
					l0 = ((ArcLabelledImmutableGraph)g).labelArray( curr );
					s1 = ((ArcLabelledNodeIterator)nodeIterator1).successors();
					for( int k = 0; k < d; k++ ) {
						s1.nextInt();
						assertEquals( msg, l0[ k ], ((LabelledArcIterator)s1).label() );
					}
					s1 = g.successors( curr );
					for( int k = 0; k < d; k++ ) {
						s1.nextInt();
						assertEquals( msg, l0[ k ], ((LabelledArcIterator)s1).label() );
					}
					assertEquals( msg, -1, s1.nextInt() );
				}
			}
		}

		// Check that cross-access works.  

		nodeIterator0 = g.nodeIterator();
		for( int s = 0; s < g.numNodes(); s++ ) {
			d = g.outdegree( s );
			nodeIterator0.nextInt();
			LazyIntIterator successors = g.successors( s );
			int[] succ = nodeIterator0.successorArray();
			for( int i = 0; i < d; i++ ) {
				final int t = successors.nextInt();
				assertEquals( succ[ i ], t );
				g.outdegree( t );
			}
			
		}
		// Check copies
		assertEquals( g, g.copy() );
	}
}
