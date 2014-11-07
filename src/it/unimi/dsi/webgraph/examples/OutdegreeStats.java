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


import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.io.TextIO;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

/** The main method of this class loads an arbitrary {@link it.unimi.dsi.webgraph.ImmutableGraph}
 * and performs a sequential scan to establish the minimum, maximum and average outdegree.
 */

public class OutdegreeStats {
	
	private OutdegreeStats() {}
	
	static public void main( String arg[] ) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JSAPException, IOException {
		SimpleJSAP jsap = new SimpleJSAP( OutdegreeStats.class.getName(), "Prints on standard error the maximum, minimum and average degree of a graph, and outputs on standard output the numerosity of each outdegree value (first line is the number of nodes with outdegree 0).",
				new Parameter[] {
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph." ),
						new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ),
						new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the graph." ),
					}		
				);
		
		JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );

		final Class<?> graphClass = jsapResult.getClass( "graphClass" );
		final String basename = jsapResult.getString( "basename" );

		final ProgressLogger pl = new ProgressLogger();
		pl.logInterval = jsapResult.getLong( "logInterval" );
		final ImmutableGraph graph;
		// We fetch by reflection the class specified by the user
		if ( graphClass != null ) graph = (ImmutableGraph)graphClass.getMethod( "loadOffline", CharSequence.class ).invoke( null, basename );
		else graph = ImmutableGraph.loadOffline( basename, pl );

		final NodeIterator nodeIterator = graph.nodeIterator();
		int count[] = IntArrays.EMPTY_ARRAY;
		int curr, d, maxd = 0, maxNode = 0, mind = Integer.MAX_VALUE, minNode = 0;
		long totd = 0;
			
		pl.expectedUpdates = graph.numNodes();
		pl.start("Scanning...");

		for( int i = graph.numNodes(); i-- != 0; ) {
			curr = nodeIterator.nextInt();
			d = nodeIterator.outdegree();
				
			if ( d < mind ) {
				mind = d;
				minNode = curr;
			}
			
			if ( d > maxd ){
				maxd = d;
				maxNode = curr; 
			}
			
			totd += d;
			
			if ( d >= count.length ) count = IntArrays.grow( count, d + 1 );
			count[ d ]++;
			
			pl.lightUpdate();
		}
		
		pl.done();
		
		System.err.println( "The minimum outdegree is " + mind + ", attained by node " + minNode );
		System.err.println( "The maximum outdegree is " + maxd + ", attained by node " + maxNode );
		System.err.println( "The average outdegree is " + (double)totd / graph.numNodes() );
		
		TextIO.storeInts( count, 0, maxd + 1, System.out );
	}
}
