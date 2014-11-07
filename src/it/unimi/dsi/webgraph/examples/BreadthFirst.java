package it.unimi.dsi.webgraph.examples;


/*		 
 * Copyright (C) 2003-2014 Paolo Boldi and Sebastiano Vigna 
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


import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/** The main method of this class loads an arbitrary {@link it.unimi.dsi.webgraph.ImmutableGraph}
 * and performs a breadth-first visit of the graph (optionally starting just from a given node, if provided,
 * in which case it prints the eccentricity of the node, i.e., the maximum distance from the node).
 */

public class BreadthFirst {
	
	private BreadthFirst() {}
	
	@SuppressWarnings("unchecked")
	static public void main( String arg[] ) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JSAPException, IOException {
		SimpleJSAP jsap = new SimpleJSAP( BreadthFirst.class.getName(), "Visits a graph in breadth-first fashion, possibly starting just from a given node.",
				new Parameter[] {
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph." ),
						new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ),
						new FlaggedOption( "start", JSAP.INTEGER_PARSER, Integer.toString( -1 ), JSAP.NOT_REQUIRED, 's', "start", "The starting node; if missing or -1, the visit will be complete." ),
						new FlaggedOption( "maxDist", JSAP.INTEGER_PARSER, Integer.toString( Integer.MAX_VALUE ), JSAP.NOT_REQUIRED, 'm', "maxDist", "Maximum distance (nodes at larger distance from the root are not enqueued" ),
						new Switch( "print", 'p', "print", "Print nodes as they are enqueued. If set, ordinary output is suppressed." ),
						new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the graph." ),
					}		
				);
		
		JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );

		final ProgressLogger pl = new ProgressLogger();
		pl.logInterval = jsapResult.getLong( "logInterval" );
		final String basename = jsapResult.getString( "basename" );
		final ImmutableGraph graph;
		if ( jsapResult.userSpecified( "graphClass" ) ) graph = (ImmutableGraph)(jsapResult.getClass( "graphClass" )).getMethod( "load", CharSequence.class, ProgressLogger.class ).invoke( null, basename, pl );
		else graph = ImmutableGraph.load( basename, pl );
		
		final int maxDist = jsapResult.getInt( "maxDist" );
		final boolean print = jsapResult.getBoolean( "print" );
		// We parse the starting node.
		final int start = jsapResult.getInt( "start" );
		final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
		final int n = graph.numNodes();
		final int[] dist = new int[ n ];
		
		IntArrays.fill( dist, Integer.MAX_VALUE ); // Initially, all distances are infinity.
		final int lo = start == -1 ? 0 : start;
		final int hi = start == -1 ? n : start + 1;
		
		int curr = lo, succ, ecc = 0, reachable = 0;
		
		pl.start( "Starting visit..." );
		pl.expectedUpdates = hi - lo;
		pl.itemsName = "nodes";
		
		for( int i = lo; i < hi; i++ ) {
			if ( dist[ i ] == Integer.MAX_VALUE ) { // Not already visited
				queue.enqueue( i );
				if ( print ) System.out.println( i );
				dist[ i ] = 0;

				LazyIntIterator successors;

				while( ! queue.isEmpty() ) {
					curr = queue.dequeueInt();
					successors = graph.successors( curr );
					int d = graph.outdegree( curr );
					while( d-- != 0 ) {
						succ = successors.nextInt();
						if ( dist[ succ ] == Integer.MAX_VALUE && dist[ curr ] + 1 <= maxDist ) {
							reachable++;
							dist[ succ ] = dist[ curr ] + 1;
							ecc = Math.max( ecc, dist[ succ ] );
							queue.enqueue( succ );
							if ( print ) System.out.println( succ );
						}
					}
				}
			}
			pl.update();
		}
		pl.done();
		
		if ( !print )
			if ( start == -1 ) System.out.println( "The maximum depth of a tree in the breadth-first spanning forest is " + ecc );
			else {
				System.out.println( "The eccentricity of node " + start + " is " + ecc + " (" + reachable + " reachable nodes)" );
			}
	}
}
