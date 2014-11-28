package it.unimi.dsi.webgraph.test;

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

import it.unimi.dsi.Util;
import it.unimi.dsi.lang.ObjectParser;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.util.XorShift1024StarRandom;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph.LoadMethod;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.IOException;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
 

public class SpeedTest {
	private final static int WARMUP = 3;
	private final static int REPEAT = 10;
	private SpeedTest() {}

    @SuppressWarnings("boxing")
	static public void main( String arg[] ) throws IllegalArgumentException, SecurityException, JSAPException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException {
		final SimpleJSAP jsap = new SimpleJSAP( SpeedTest.class.getName(), "Tests the access speed of an ImmutableGraph. By default, the graph is enumerated sequentially, but you can specify a number of nodes to be accessed randomly.\n\nThis class executes " + WARMUP + " warmup iterations, and then averages the timings of the following " + REPEAT + " iterations.",
				new Parameter[] {
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'g', "graphClass", "Forces a Java class for the source graph." ),
						new Switch( "spec", 's', "spec", "The basename is a specification of the form <ImmutableGraphImplementation>(arg,arg,...)." ),
						new FlaggedOption( "seed", JSAP.LONG_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'S', "seed", "A seed for the pseudorandom number generator." ),
						new FlaggedOption( "random", JSAP.LONGSIZE_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'r', "random", "Perform a random-access test on this number of nodes instead of enumerating sequentially the whole graph." ),
						new Switch( "first", 'f', "first", "Just enumerate the first successor of each tested node." ),
						new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the graph." ),
					}		
				);

		final JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );
		
		final boolean random = jsapResult.userSpecified( "random" );
		final boolean spec = jsapResult.getBoolean( "spec" );
		final boolean first = jsapResult.userSpecified( "first" );
		final Class<?> graphClass = jsapResult.getClass( "graphClass" );
		final String basename = jsapResult.getString( "basename" );
		if ( graphClass != null && spec ) throw new IllegalArgumentException( "Options --graph-class and --spec are incompatible." );
		
		final ProgressLogger pl = new ProgressLogger();
		final long seed = jsapResult.userSpecified( "seed" ) ? jsapResult.getLong( "seed" ) : Util.randomSeed();
		final XorShift1024StarRandom r = new XorShift1024StarRandom();
		
		System.err.println( "Seed: " + seed );
		
		// The number of overall links, unless first is true, in which case the number of tested nodes.
		long totLinks = 0;
		long cumulativeTime = 0;

		final long samples;
		final ImmutableGraph graph;

		if ( random ) {
			if ( jsapResult.userSpecified( "graphClass" ) ) graph = (ImmutableGraph)graphClass.getMethod( LoadMethod.STANDARD.toMethod(), CharSequence.class, ProgressLogger.class ).invoke( null, basename, pl );
			else if ( spec ) graph = ObjectParser.fromSpec( basename, ImmutableGraph.class, GraphClassParser.PACKAGE );
			else graph = ImmutableGraph.load( basename, pl );
			
			final int n = graph.numNodes();
			samples = jsapResult.getLong( "random" );

			r.setSeed( seed );
			if ( first ) totLinks = samples;
			else for( long i = samples; i-- != 0; ) totLinks += graph.outdegree( r.nextInt( n ) );
			
			System.err.println( first ? "Accessing the first link on " + samples + " random nodes using ImmutableGraph.successors()..." : "Accessing links on " + samples + " random nodes using ImmutableGraph.successors()..." );
			
			for( int k = WARMUP + REPEAT; k-- != 0; ) {
				r.setSeed( seed );
				ThreadMXBean bean = ManagementFactory.getThreadMXBean();
				long time = -bean.getCurrentThreadCpuTime();
				if ( first ) 
					for( long i = samples; i-- != 0; ) graph.successors( r.nextInt( n ) ).nextInt();
				else 
					for( long i = samples; i-- != 0; ) 
						for( LazyIntIterator links = graph.successors( r.nextInt( n ) ); links.nextInt() != - 1; );

				time += bean.getCurrentThreadCpuTime();
				
				if ( k < REPEAT ) cumulativeTime += time;
				System.err.printf( "Intermediate time: %3fs nodes: %d; arcs %d; nodes/s: %.3f arcs/s: %.3f ns/node: %3f, ns/link: %.3f\n", 
						time / 1E9, samples, totLinks, ( samples * 1E9 ) / time, ( totLinks * 1E9 ) / time, time / (double)samples, time / (double)totLinks );
			}
		}
		else {
			if ( first ) throw new IllegalArgumentException( "Option --first requires --random." );
			if ( jsapResult.userSpecified( "graphClass" ) ) graph = (ImmutableGraph)graphClass.getMethod( LoadMethod.SEQUENTIAL.toMethod(), CharSequence.class, ProgressLogger.class ).invoke( null, basename, pl );
			else if ( spec )  graph = ObjectParser.fromSpec( basename, ImmutableGraph.class, GraphClassParser.PACKAGE );
			else graph = ImmutableGraph.loadSequential( basename, pl );

			samples = graph.numNodes();

			System.err.println( "Accessing links sequentially using ImmutableGraph.successorArray()..." );
			
			for( int k = WARMUP + REPEAT; k-- != 0; ) {
				ThreadMXBean bean = ManagementFactory.getThreadMXBean();
				long time = -bean.getCurrentThreadCpuTime();
				final NodeIterator nodeIterator = graph.nodeIterator();
				totLinks = 0;
				for( long i = samples; i-- != 0; ) {
					nodeIterator.nextInt();
					totLinks += nodeIterator.outdegree();
					nodeIterator.successorArray();
				}
				time += bean.getCurrentThreadCpuTime();
				
				if ( k < REPEAT ) cumulativeTime += time;
				System.err.printf( "Intermediate time: %3fs nodes: %d; arcs %d; nodes/s: %.3f arcs/s: %.3f ns/node: %3f, ns/link: %.3f\n", 
						time / 1E9, samples, totLinks, ( samples * 1E9 ) / time, ( totLinks * 1E9 ) / time, time / (double)samples, time / (double)totLinks );
			}
		}

		final double averageTime = cumulativeTime / (double)REPEAT;
		System.out.printf( "Time: %.3fs nodes: %d; arcs %d; nodes/s: %.3f arcs/s: %.3f ns/node: %3f, ns/link: %.3f\n", 
				averageTime / 1E9, samples, totLinks, ( samples * 1E9 ) / averageTime, ( totLinks * 1E9 ) / averageTime, averageTime / samples, averageTime / totLinks );
    }
}
