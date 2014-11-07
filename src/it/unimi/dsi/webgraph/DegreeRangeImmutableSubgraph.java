package it.unimi.dsi.webgraph;

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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.lang.ObjectParser;

import java.io.IOException;

/** A subclass of {@link ImmutableSubgraph} exposing the subgraph formed by nodes whose outdegree is in a given range.
 *
 * <p>Note that the {@linkplain #DegreeRangeImmutableSubgraph(String, String, String, String) string-based constructors} can be
 * used with an {@link ObjectParser} to specify a graph on the command line.
 */

public class DegreeRangeImmutableSubgraph extends ImmutableSubgraph {
	protected static int[] createMap( final ImmutableGraph graph, final int minDegree, final int maxDegree ) {
		final IntArrayList map = new IntArrayList();
		final int n = graph.numNodes();
		final NodeIterator nodeIterator = graph.nodeIterator();
		for( int i = 0; i < n; i++ ) {
			nodeIterator.nextInt();
			final int d = nodeIterator.outdegree();
			if ( d >= minDegree && d < maxDegree ) map.add( i );
		}
		return map.toIntArray();
	}

	/** Create a subgraph formed by the nodes with outdegree in a specified range.
	 * 
	 * @param graph the supergraph.
	 * @param minDegree the minimum outdegree (inclusive).
	 * @param maxDegree the maximum outdegree (exclusive).
	 */
	public DegreeRangeImmutableSubgraph( final ImmutableGraph graph, final int minDegree, final int maxDegree ) {
		super( graph, createMap( graph, minDegree, maxDegree ) );
	}

	/** Create a subgraph formed by the nodes with outdegree in a specified range.
	 * 
	 * <p>This is a string-based constructor that can be used with an {@link ObjectParser}.
	 * 
	 * @param graph the supergraph.
	 * @param minDegree the minimum outdegree (inclusive).
	 * @param maxDegree the meximum outdegree (exclusive).
	 */
	public DegreeRangeImmutableSubgraph( final String graph, final String minDegree, final String maxDegree ) throws IOException {
		this( graph, minDegree, maxDegree, "false" );
	}

	/** Create a subgraph formed by the nodes with outdegree in a specified range.
	 * 
	 * <p>This is a string-based constructor that can be used with an {@link ObjectParser}.
	 * 
	 * @param graph the supergraph.
	 * @param minDegree the minimum outdegree (inclusive).
	 * @param maxDegree the maximum outdegree (exclusive).
	 * @param mapped if true, the supergraph will be loaded with {@link ImmutableGraph#loadMapped(CharSequence, it.unimi.dsi.logging.ProgressLogger)} instead
	 * of {@link ImmutableGraph#load(CharSequence, it.unimi.dsi.logging.ProgressLogger)}.
	 */
	public DegreeRangeImmutableSubgraph( final String graph, final String minDegree, final String maxDegree, final String mapped ) throws IOException {
		this( Boolean.parseBoolean( mapped ) ? ImmutableGraph.loadMapped( graph ) : ImmutableGraph.load( graph ), Integer.parseInt( minDegree ), Integer.parseInt( maxDegree ) );
	}
}
