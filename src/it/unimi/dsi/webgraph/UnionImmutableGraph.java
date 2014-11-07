package it.unimi.dsi.webgraph;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An immutable graph representing the union of two given graphs. Here by &ldquo;union&rdquo;
 *  we mean that an arc will belong to the union iff it belongs to at least one of the two graphs (the number of
 *  nodes of the union is taken to be the maximum among the number of nodes of each graph). 
 */
public class UnionImmutableGraph extends ImmutableGraph {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger( Transform.class );
	@SuppressWarnings("unused")
	private static final boolean DEBUG = false;
	@SuppressWarnings("unused")
	private static final boolean ASSERTS = false;

	private static final int INITIAL_ARRAY_SIZE = 16;

	private final ImmutableGraph g0, g1;
	private final int n0, n1, numNodes;

	/** The node whose successors are cached, or -1 if no successors are currently cached. */
	private int cachedNode = -1;

	/** The outdegree of the cached node, if any. */
	private int outdegree ;

	/** The successors of the cached node, if any; note that the array might be larger. */
	private int cache[];

	/** Creates the union of two given graphs.
	 * 
	 * @param g0 the first graph.
	 * @param g1 the second graph. 
	 */
	public UnionImmutableGraph( ImmutableGraph g0, ImmutableGraph g1 ) {
		this.g0 = g0;
		this.g1 = g1;
		n0 = g0.numNodes();
		n1 = g1.numNodes();
		numNodes = Math.max( n0, n1 );
	}

	public UnionImmutableGraph copy() {
		return new UnionImmutableGraph( g0.copy(), g1.copy() );
	}
	
	public NodeIterator nodeIterator( final int from ) {
	
		return new NodeIterator() {
			/** If outdegree is nonnegative, the successors of the current node (this array may be, however, larger). */
			@SuppressWarnings("hiding")
			private int cache[] = IntArrays.EMPTY_ARRAY;
			/** The outdegree of the current node, or -1 if the successor array for the current node has not been computed yet. */
			@SuppressWarnings("hiding")
			private int outdegree = -1;
			private NodeIterator i0 = from < n0? g0.nodeIterator( from ) : null;
			private NodeIterator i1 = from < n1? g1.nodeIterator( from ) : null;
	
			public boolean hasNext() {
				return i0 != null && i0.hasNext() || i1 != null && i1.hasNext();
			}
			
			public int nextInt() {
				if ( ! hasNext() ) throw new java.util.NoSuchElementException();
				outdegree = -1;
				int result = -1;
				if ( i0 != null ) {
					if ( i0.hasNext() ) result = i0.nextInt();
					else i0 = null;
				}
				if ( i1 != null ) {
					if ( i1.hasNext() ) result = i1.nextInt();
					else i1 = null;
				}
				return result;
			}
	
			public int[] successorArray() {
				if ( outdegree != -1 ) return cache;
				if ( i0 == null ) {
					outdegree = i1.outdegree();
					return cache = i1.successorArray();
				} 
				if ( i1 == null ) {
					outdegree = i0.outdegree();
					return cache = i0.successorArray();
				}
									
				MergedIntIterator merge = new MergedIntIterator( i0.successors(), i1.successors() );
				outdegree = LazyIntIterators.unwrap( merge, cache );
				int upto, t;
				while ( ( t = merge.nextInt() ) != -1 ) {
					upto = cache.length;
					cache = IntArrays.grow( cache, upto + 1 );
					cache[ upto++ ] = t;
					outdegree++;
					outdegree += LazyIntIterators.unwrap( merge, cache, upto, cache.length - upto );
				}
				return cache;
			}
	
			public int outdegree() {
				successorArray(); // So that the cache is filled up
				return outdegree;
			}
	                    
		};
	
	}

	public int numNodes() {
		return numNodes;
	}

	@Override
	public boolean randomAccess() {
		return g0.randomAccess() && g1.randomAccess();
	}

	private void fillCache( int x ) {
		if ( x == cachedNode ) return; 
		MergedIntIterator merge = new MergedIntIterator( x < n0? g0.successors( x ) : LazyIntIterators.EMPTY_ITERATOR, x < n1? g1.successors( x ) : LazyIntIterators.EMPTY_ITERATOR );
		outdegree = 0;
		cache = new int[ INITIAL_ARRAY_SIZE ];
		outdegree += LazyIntIterators.unwrap( merge, cache );
		int upto, t;
		while ( ( t = merge.nextInt() ) != -1 ) {
			upto = cache.length;
			cache = IntArrays.grow( cache, upto + 1 );
			cache[ upto++] = t;
			outdegree++;
			outdegree += LazyIntIterators.unwrap( merge, cache, upto, cache.length - upto );
		}
		cachedNode = x;
	}

	public int[] successorArray( int x ) {
		fillCache( x );
		return cache; 
	}

	public int outdegree( int x ) {
		fillCache( x );
		return outdegree;
	}
}