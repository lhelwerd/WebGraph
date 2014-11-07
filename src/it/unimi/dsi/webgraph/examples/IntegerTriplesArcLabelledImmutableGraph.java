package it.unimi.dsi.webgraph.examples;

/*		 
 * Copyright (C) 2007-2014 Paolo Boldi and Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.webgraph.AbstractLazyIntIterator;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableSequentialGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.GammaCodedIntLabel;
import it.unimi.dsi.webgraph.labelling.Label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

/** A class exposing a list of triples as an {@link ArcLabelledImmutableGraph}. The triples are
 * interpreted as labelled arcs: the first element is the source, the second element is the target,
 * and the third element must be a nonnegative integer that will be saved using a {@link GammaCodedIntLabel}.
 * 
 * <p>This class is mainly a useful example of how to expose of your data <i>via</i> an {@link ArcLabelledImmutableGraph}, and
 * it is also used to build test cases, but it is not efficient or particularly refined.
 * 
 * <p>A main method reads from standard input a list of TAB-separated triples and writes the corresponding graph
 * using {@link BVGraph} and {@link BitStreamArcLabelledImmutableGraph}.
 */

public class IntegerTriplesArcLabelledImmutableGraph extends ArcLabelledImmutableSequentialGraph {
	/** The list of triples. */
	final private int[][] triple;
	/** The prototype of the labels used by this class. */
	final private GammaCodedIntLabel prototype;
	/** The number of nodes, computed at construction time by triple inspection. */
	final private int n;
	
	/** Creates a new arc-labelled immutable graph using a specified list of triples.
	 * 
	 * <p>Note that it is impossible to specify isolated nodes with indices larger than
	 * the largest node with positive indegree or outdegree, as the number of nodes is computed
	 * by maximising over all indices in <code>triple</code>. 
	 * 
	 * @param triple a list of triples specifying labelled arcs (see the {@linkplain IntegerTriplesArcLabelledImmutableGraph class documentation});
	 * order is not relevant, but multiple arcs are not allowed.
	 */
	public IntegerTriplesArcLabelledImmutableGraph( int[][] triple ) {
		this.triple = triple;
		prototype = new GammaCodedIntLabel( "FOO" );
		int m = 0;
		for( int i = 0; i < triple.length; i++ ) m = Math.max( m, Math.max( triple[ i ][ 0 ], triple[ i ][ 1 ] ) );
		Arrays.sort( triple, new Comparator<int[]>() {
			public int compare( int[] p, int[] q ) {
				final int t =  p[ 0 ] - q[ 0 ]; // Compare by source
				if ( t != 0 ) return t;
				return p[ 1 ] - q[ 1 ]; // Compare by destination
			}
		} );
		
		n = m + 1;
	}
	
	@Override
	public Label prototype() {
		return prototype;
	}

	@Override
	public int numNodes() {
		return n;
	}

	@Override
	public ArcLabelledNodeIterator nodeIterator( int from ) {
		if ( from == 0 ) return nodeIterator();
		throw new UnsupportedOperationException();
	}

	private final class ArcIterator extends AbstractLazyIntIterator implements LabelledArcIterator  {
		private final int d;
		private int k = 0; // Index of the last returned triple is pos+k
		private final int pos;
		private final GammaCodedIntLabel label;

		private ArcIterator( int d, int pos, GammaCodedIntLabel label ) {
			this.d = d;
			this.pos = pos;
			this.label = label;
		}

		public Label label() {
			if ( k == 0 ) throw new IllegalStateException();
			label.value = triple[ pos + k ][ 2 ];
			return label;
		}

		public int nextInt() {
			if ( k >= d ) return -1;
			return triple[ pos + ++k ][ 1 ];
		}
	}

	@Override
	public ArcLabelledNodeIterator nodeIterator() {
		return new ArcLabelledNodeIterator() {
			/** Last node returned by this iterator. */
			private int last = -1;
			/** Last triple examined by this iterator. */
			private int pos = -1;
			/** A local copy of the prototye. */
			private GammaCodedIntLabel label = prototype.copy();

			@Override
			public LabelledArcIterator successors() {
				if ( last < 0 ) throw new IllegalStateException();
				final int d = outdegree(); // Triples to be returned are pos+1,pos+2,...,pos+d 
				return new ArcIterator( d, pos, label );
			}

			@Override
			public int outdegree() {
				if ( last < 0 ) throw new IllegalStateException();
				int p;
				for ( p = pos + 1; p < triple.length && triple[ p ][ 0 ] == last; p++ );
				return p - pos - 1;
			}

			public boolean hasNext() {
				return last < n - 1;
			}
			
			@Override
			public int nextInt() {
				if ( !hasNext() ) throw new NoSuchElementException();
				if ( last >= 0 ) pos += outdegree();
				return ++last;
			}
			
		};
	}
	
	public static void main( String arg[] ) throws JSAPException, IOException {
		final SimpleJSAP jsap = new SimpleJSAP( IntegerTriplesArcLabelledImmutableGraph.class.getName(), 
				"Reads from standard input a list of triples <source,dest,label>, where the three " +
				"components are separated by a TAB, and saves the " +
				"corresponding arc-labelled graph using a BVGraph and a BitStreamArcLabelledImmutableGraph. " +
				"Labels are represeted using GammaCodedIntLabel.",
				new Parameter[] {
						//new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph." ),
						new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the resulting arc-labelled graph." ),
					}		
				);
		
		final JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );
		final String basename = jsapResult.getString( "basename" );

		// We read triples from stdin, parse them and feed them to the constructor.
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in, "ASCII" ) );
		ObjectArrayList<int[]> list = new ObjectArrayList<int[]>();
		
		String line;
		while( ( line = br.readLine() ) != null ) {
			final String p[] = line.split( "\t" );
			list.add( new int[] { Integer.parseInt(  p[ 0 ] ),Integer.parseInt( p[ 1 ] ), Integer.parseInt(  p[ 2 ] ) } ); 
		}
		
		final ArcLabelledImmutableGraph g = new IntegerTriplesArcLabelledImmutableGraph( list.toArray( new int[0][] ) );
		BVGraph.store( g, basename + ArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX );
		BitStreamArcLabelledImmutableGraph.store( g, basename, basename + ArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX );
	}
}
