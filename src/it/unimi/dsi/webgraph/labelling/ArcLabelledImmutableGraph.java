package it.unimi.dsi.webgraph.labelling;

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

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;

import java.io.IOException;
import java.io.InputStream;

/** An abstract implementation of a graph labelled on its arcs.
 * 
 * <p>The main purpose of this class is that of override covariantly the return
 * type of {@link #nodeIterator()} and {@link #nodeIterator(int)} so that
 * it is an {@link ArcLabelledNodeIterator}, and the return type of
 * all static load methods and of {@link #copy()} so that it is an {@link ArcLabelledImmutableGraph} (the
 * methods themselves just delegate to the corresponding method in {@link ImmutableGraph}).
 * 
 * <p>The only additional instance methods are {@link #labelArray(int)} and {@link #prototype()}.
 * 
 * <h2>Saving labels</h2>
 * 
 * <P>A subclass of this class <strong>may</strong> implement
 * <UL>
 * <LI><code>store(ArcLabelledImmutableGraph, CharSequence, CharSequence, ProgressLogger)</code>;
 * <LI><code>store(ArcLabelledImmutableGraph, CharSequence, CharSequence)</code>.
 * </UL> 
 * 
 * <p>These methods must save the labels of the given arc-labelled graph using the first given character
 * sequence as a basename, and a suitable property file using the second given basename. Note that the graph
 * will <strong>not</strong> be saved&mdash;use the <code>store()</code>
 * method of an {@link ImmutableGraph} implementation for that purpose.
 * 
 * <p>For istance, assuming <code>g</code> is an arc-labelled graph the idiomatic way
 * of storing it on disk using {@link BVGraph} for the underlying graph and 
 * {@link BitStreamArcLabelledImmutableGraph} for the labels is
 * <pre>
 * BVGraph.store( g, "foo" );
 * BitStreamArcLabelledImmutableGraph.store( g, "bar", "foo" );
 * </pre>
 * 
 * <h2>Underlying graphs</h2>
 * 
 * <p>Often, implementations of this class will just wrap an <em>underlying graph</em> (i.e.,
 * an instance of {@link ImmutableGraph}). In that case, we suggest that if the implementation
 * uses property files the basename of the underlying graph is specified using the property
 * key {@link #UNDERLYINGGRAPH_PROPERTY_KEY}. If the basename must be generated starting
 * from the arc-labelled graph basename, we suggest to just add at the end the string
 * {@link #UNDERLYINGGRAPH_SUFFIX}.
 */

public abstract class ArcLabelledImmutableGraph extends ImmutableGraph {
	
	/** The standard property key for the underlying graph. All implementations decorating
	 * with labels an underlying graph are strongly encouraged to use this property
	 * name to specify the basename of the underlying graph. */
	public static final String UNDERLYINGGRAPH_PROPERTY_KEY = "underlyinggraph";
	/** The standard suffix added to basenames in order to give a basename
	 * to the underlying graph, when needed. */
	public static final String UNDERLYINGGRAPH_SUFFIX = "-underlying";

	
	public abstract ArcLabelledImmutableGraph copy();
	
	public ArcLabelledNodeIterator nodeIterator() {
		return nodeIterator( 0 );
	}

	/** Returns a node iterator for scanning the graph sequentially, starting from the given node.
	 *
	 *  <P>This implementation strengthens that provided in {@link ImmutableGraph}, but
	 *  calls the labelled random-access method {@link #successors(int)}.
	 *
	 *  @param from the node from which the iterator will iterate.
	 *  @return an {@link ArcLabelledNodeIterator} for accessing nodes, successors and their labels sequentially.
	 *  
	 *  @see ImmutableGraph#nodeIterator()
	 */
	public ArcLabelledNodeIterator nodeIterator( final int from ) {
		return new ArcLabelledNodeIterator() {
				int curr = from - 1;
				final int n = numNodes();

				public int nextInt() {
					if ( ! hasNext() ) throw new java.util.NoSuchElementException();
					return ++curr;
				}

				public boolean hasNext() {
					return ( curr < n - 1 );
				}

				public LabelledArcIterator successors() {
					if ( curr == from - 1 ) throw new IllegalStateException();
					return ArcLabelledImmutableGraph.this.successors( curr );
				}

				public int outdegree() {
					if ( curr == from - 1 ) throw new IllegalStateException();
					return ArcLabelledImmutableGraph.this.outdegree( curr );
				}
			};
	}

	public abstract ArcLabelledNodeIterator.LabelledArcIterator successors( int x );
	
	/** Returns a prototype of the labels used by this graph. The prototype can be
	 * used to produce new copies, but must not be modified by the caller.
	 * 
	 * @return a prototype for the labels of this graph.
	 */
	public abstract Label prototype();

	/** Returns a reference to an array containing the labels of the arcs going out of a given node
	 * in the same order as the order in which the corresponding successors are returned by {@link #successors(int)}.
	 * 
	 * <P>The returned array may contain more entries than the outdegree of <code>x</code>.
	 * However, only those with indices from 0 (inclusive) to the outdegree of <code>x</code> (exclusive)
	 * contain valid data.
	 * 
	 * <P>This implementation just unwrap the iterator returned by {@link #successors(int)} and
	 * writes in a newly allocated array copies of the labels returned by {@link LabelledArcIterator#label()}.
	 * 
	 * @return an array whose first elements are the labels of the arcs going 
	 * out of <code>x</code>; the array must not be modified by the caller.
	 */

	public Label[] labelArray( int x ) {
		return ArcLabelledNodeIterator.unwrap( successors( x ), outdegree( x ) );
	}

	public static ArcLabelledImmutableGraph loadSequential( CharSequence basename ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.loadSequential( basename );
	}

	public static ArcLabelledImmutableGraph loadSequential( CharSequence basename, ProgressLogger pl ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.loadSequential( basename, pl );
	}

	public static ArcLabelledImmutableGraph loadOffline( CharSequence basename ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.loadOffline( basename );
	}

	public static ArcLabelledImmutableGraph loadOffline( CharSequence basename, ProgressLogger pl ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.loadOffline( basename, pl );
	}

	public static ArcLabelledImmutableGraph load( CharSequence basename ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.load( basename );
	}

	public static ArcLabelledImmutableGraph load( CharSequence basename, ProgressLogger pl ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.load( basename, pl );
	}

	public static ArcLabelledImmutableGraph loadOnce( InputStream is ) throws IOException {
		return (ArcLabelledImmutableGraph)ImmutableGraph.loadOnce( is );
	}
	
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();

		long numArcs = -1;
		try {
			numArcs = numArcs();
		}
		catch( UnsupportedOperationException ignore ) {}
		
		s.append( "Nodes: " + numNodes() + "\nArcs: " + ( numArcs == -1 ? "unknown" : Long.toString( numArcs ) ) + "\n" );

		final ArcLabelledNodeIterator nodeIterator = nodeIterator();
		ArcLabelledNodeIterator.LabelledArcIterator successors;
		int curr;
		for ( int i = numNodes(); i-- != 0; ) {
			curr = nodeIterator.nextInt();
			s.append( "Successors of " + curr + " (degree " + nodeIterator.outdegree() + "):" );
			successors = nodeIterator.successors();
			int d = nodeIterator.outdegree();
			while ( d-- != 0 ) s.append( " " + successors.nextInt() + " [" + successors.label() + "]" );
			s.append( '\n' );
		}
		return s.toString();
	}
	
	@Override
	public boolean equals( Object x ) {
		if ( ! ( x instanceof ArcLabelledImmutableGraph ) ) return false;
		ArcLabelledImmutableGraph g = (ArcLabelledImmutableGraph)x;
		if ( g.numNodes() != numNodes() ) return false;
		ArcLabelledNodeIterator nodeIterator = nodeIterator();
		ArcLabelledNodeIterator gNodeIterator = g.nodeIterator();
		while ( nodeIterator.hasNext() ) {
			nodeIterator.nextInt(); gNodeIterator.nextInt();
			if ( nodeIterator.outdegree() != gNodeIterator.outdegree() ) return false;
			LabelledArcIterator arcIterator = nodeIterator.successors();
			LabelledArcIterator gArcIterator = gNodeIterator.successors();
			int d = nodeIterator.outdegree();
			while ( d-- != 0 ) {
				if ( arcIterator.nextInt() != gArcIterator.nextInt() 
						|| ! arcIterator.label().equals( gArcIterator.label() ) ) return false;
			}
		}
		return true;
	}
}
