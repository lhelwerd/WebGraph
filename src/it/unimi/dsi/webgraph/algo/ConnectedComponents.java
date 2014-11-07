package it.unimi.dsi.webgraph.algo;

/*		 
 * Copyright (C) 2011-2014 Sebastiano Vigna 
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

import it.unimi.dsi.Util;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.UnionImmutableGraph;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Computes the connected components of a <em>symmetric</em> (a.k.a&#46; <em>undirected</em>) graph
 * using a {@linkplain ParallelBreadthFirstVisit parallel breadth-first visit}.
 * 
 * <p>The {@link #compute(ImmutableGraph, int, ProgressLogger)} method of this class will return an
 * instance that contains the data computed by visiting the graph (using an instance of
 * {@link ParallelBreadthFirstVisit}). Note that it is your responsibility to pass a symmetric graph
 * to {@link #compute(ImmutableGraph, int, ProgressLogger)}. Otherwise, results will be
 * unpredictable.
 * 
 * <p>After getting an instance, it is possible to run the {@link #computeSizes()} and
 * {@link #sortBySize(int[])} methods to obtain further information. This scheme has been devised to
 * exploit the available memory as much as possible&mdash;after the components have been computed,
 * the returned instance keeps no track of the graph, and the related memory can be freed by the
 * garbage collector.
 * 
 * <h2>Performance issues</h2>
 * 
 * <p>This class uses an instance of {@link ParallelBreadthFirstVisit} to ensure a high degree of
 * parallelism (see its documentation for memory requirements).
 */

public class ConnectedComponents {
	private static final Logger LOGGER = LoggerFactory.getLogger( ConnectedComponents.class );

	/** The number of connected components. */
	public final int numberOfComponents;

	/** The component of each node. */
	public final int component[];

	protected ConnectedComponents( final int numberOfComponents, final int[] component ) {
		this.numberOfComponents = numberOfComponents;
		this.component = component;
	}

	/**
	 * Computes the diameter of a symmetric graph.
	 * 
	 * @param symGraph a symmetric graph.
	 * @param threads the requested number of threads (0 for {@link Runtime#availableProcessors()}).
	 * @param pl a progress logger, or <code>null</code>.
	 * @return an instance of this class containing the computed components.
	 */
	public static ConnectedComponents compute( final ImmutableGraph symGraph, final int threads, final ProgressLogger pl ) {
		ParallelBreadthFirstVisit visit = new ParallelBreadthFirstVisit( symGraph, threads, false, pl );
		visit.visitAll();
		final AtomicIntegerArray visited = visit.marker;
		final int numberOfComponents = visit.round + 1;
		visit = null;
		final int[] component = new int[ visited.length() ];
		for ( int i = component.length; i-- != 0; )
			component[ i ] = visited.get( i );
		return new ConnectedComponents( numberOfComponents, component );
	}

	/**
	 * Returns the size array for this set of connected components.
	 * 
	 * @return the size array for this set of connected components.
	 */
	public int[] computeSizes() {
		final int[] size = new int[ numberOfComponents ];
		for ( int i = component.length; i-- != 0; )
			size[ component[ i ] ]++;
		return size;
	}

	/**
	 * Renumbers by decreasing size the components of this set.
	 * 
	 * <p>After a call to this method, both the internal status of this class and the argument array
	 * are permuted so that the sizes of connected components are decreasing in the component index.
	 * 
	 * @param size the components sizes, as returned by {@link #computeSizes()}.
	 */
	public void sortBySize( final int[] size ) {
		final int[] perm = Util.identity( size.length );
		IntArrays.quickSort( perm, 0, perm.length, new AbstractIntComparator() {
			public int compare( final int x, final int y ) {
				return size[ y ] - size[ x ];
			}
		} );
		final int[] copy = size.clone();
		for ( int i = size.length; i-- != 0; )
			size[ i ] = copy[ perm[ i ] ];
		Util.invertPermutationInPlace( perm );
		for ( int i = component.length; i-- != 0; )
			component[ i ] = perm[ component[ i ] ];
	}

	public static void main( String arg[] ) throws IOException, JSAPException {
		SimpleJSAP jsap = new SimpleJSAP( ConnectedComponents.class.getName(),
				"Computes the connected components of a symmetric graph of given basename. The resulting data is saved " +
				"in files stemmed from the given basename with extension .wcc (a list of binary integers specifying the " +
				"component of each node) and .wccsizes (a list of binary integer specifying the size of each component). " +
				"The symmetric graph can also be specified using a generic (non-symmetric) graph and its transpose.",
				new Parameter[] {
					new Switch( "sizes", 's', "sizes", "Compute component sizes." ),
					new Switch( "renumber", 'r', "renumber", "Renumber components in decreasing-size order." ),
					new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ), 
					new Switch( "mapped", 'm', "mapped", "Do not load the graph in main memory, but rather memory-map it." ),
					new FlaggedOption( "threads", JSAP.INTSIZE_PARSER, "0", JSAP.NOT_REQUIRED, 'T', "threads", "The number of threads to be used. If 0, the number will be estimated automatically." ),
					new FlaggedOption( "basenamet", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 't', "transpose", "The basename of the transpose, in case the graph is not symmetric." ),
					new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of a symmetric graph (or of a generic graph, if the transpose is provided, too)." ),
					new UnflaggedOption( "resultsBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, "The basename of the resulting files." ),
				}
		);

		JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );

		final String basename = jsapResult.getString( "basename" );
		final String basenamet = jsapResult.getString( "basenamet" );
		final String resultsBasename = jsapResult.getString( "resultsBasename", basename );
		final int threads = jsapResult.getInt( "threads" );
		ProgressLogger pl = new ProgressLogger( LOGGER, jsapResult.getLong( "logInterval" ), TimeUnit.MILLISECONDS );

		ImmutableGraph graph = jsapResult.userSpecified( "mapped" ) ? ImmutableGraph.loadMapped( basename ) : ImmutableGraph.load( basename, pl );
		ImmutableGraph grapht = basenamet == null ? null : jsapResult.userSpecified( "mapped" ) ? ImmutableGraph.loadMapped( basenamet ) : ImmutableGraph.load( basenamet, pl );
		final ConnectedComponents components = ConnectedComponents.compute( basenamet != null ? new UnionImmutableGraph( graph, grapht ) : graph, threads, pl );

		if ( jsapResult.getBoolean( "sizes" ) || jsapResult.getBoolean( "renumber" ) ) {
			final int size[] = components.computeSizes();
			if ( jsapResult.getBoolean( "renumber" ) ) components.sortBySize( size );
			if ( jsapResult.getBoolean( "sizes" ) ) BinIO.storeInts( size, resultsBasename + ".wccsizes" );
		}
		BinIO.storeInts( components.component, resultsBasename + ".wcc" );
	}
}
